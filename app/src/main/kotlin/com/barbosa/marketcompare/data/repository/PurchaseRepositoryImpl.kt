package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.MarketCompareDatabase
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ShoppingListItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import com.barbosa.marketcompare.data.local.entity.toDomain
import com.barbosa.marketcompare.data.local.entity.toEntity
import com.barbosa.marketcompare.domain.basket.BasketEstimator
import com.barbosa.marketcompare.domain.comparison.CrossStoreComparisonCalculator
import com.barbosa.marketcompare.domain.comparison.PriceComparisonRecord
import com.barbosa.marketcompare.domain.model.BasketEstimate
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.HistoricalPriceObservation
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ProductDetailData
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ProductResolution
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.ShoppingListItem
import com.barbosa.marketcompare.domain.model.Store
import com.barbosa.marketcompare.domain.model.StorePriceStatistics
import com.barbosa.marketcompare.domain.model.StoreSummary
import com.barbosa.marketcompare.domain.model.UnitType
import com.barbosa.marketcompare.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class PurchaseRepositoryImpl(
    private val database: MarketCompareDatabase
) : PurchaseRepository {

    private val receiptDao = database.receiptDao()
    private val storeDao = database.storeDao()
    private val productDao = database.productDao()
    private val shoppingListDao = database.shoppingListDao()
    private val catalogResolver = ProductCatalogResolver(productDao, receiptDao)
    private val manualPriceRecorder = ManualPriceRecorder(storeDao, productDao, receiptDao, catalogResolver)
    private val productDetailEditor = ProductDetailEditor(productDao, receiptDao, storeDao, shoppingListDao)

    override fun getAllReceipts(): Flow<List<Receipt>> =
        receiptDao.getAllReceiptsWithStore().map { list -> list.map { it.toDomain() } }

    override fun getReceiptById(receiptId: String): Flow<Receipt?> = flow { emit(null) }

    override fun getAllPriceObservations(): Flow<List<ProductPriceObservationItem>> {
        return receiptDao.observeAllPriceObservations().map { entities ->
            val minPriceMap = entities.groupBy { it.rawDescription.trim().lowercase() }
                .mapValues { (_, items) -> items.minOf { it.unitPriceMinor } }

            entities.map { entity ->
                val key = entity.rawDescription.trim().lowercase()
                val minPrice = minPriceMap[key] ?: entity.unitPriceMinor
                val sameProductCount = entities.count { it.rawDescription.trim().lowercase() == key }
                val isLowest = entity.unitPriceMinor <= minPrice && sameProductCount > 1
                entity.toDomain(isLowestPrice = isLowest)
            }
        }
    }

    override fun getAllStoreNames(): Flow<List<String>> = storeDao.getAllStoreNames()

    override fun getStoreSummaries(): Flow<List<StoreSummary>> {
        return storeDao.observeStoreSummaries().map { list ->
            list.map { item ->
                StoreSummary(
                    storeId = item.storeId,
                    storeName = item.storeName,
                    address = item.address,
                    productCount = item.productCount,
                    lastObservationTimestamp = item.lastObservationTimestamp ?: 0L
                )
            }
        }
    }

    override fun getProductsForStore(storeName: String): Flow<List<ProductPriceObservationItem>> =
        receiptDao.observeProductsForStore(storeName).map { entities -> entities.map { it.toDomain() } }

    override fun getPriceTimeline(productName: String): Flow<List<ProductPriceObservationItem>> =
        receiptDao.observePriceTimeline(productName.trim()).map { entities -> entities.map { it.toDomain() } }

    override fun getPriceObservationsBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<ProductPriceObservationItem>> =
        receiptDao.observePriceObservationsBetween(startTimestamp, endTimestamp).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getStoreStatistics(storeName: String): StorePriceStatistics? {
        val result = storeDao.getStoreStatistics(storeName.trim()) ?: return null
        return StorePriceStatistics(
            storeName = result.storeName,
            distinctProductsCount = result.distinctProductsCount,
            totalObservationsCount = result.totalObservationsCount,
            lowestPriceRecorded = result.lowestPriceMinor?.let { Money(it, "COP") },
            highestPriceRecorded = result.highestPriceMinor?.let { Money(it, "COP") },
            lastObservedTimestamp = result.lastObservedTimestamp ?: 0L
        )
    }

    override suspend fun getCrossStoreComparison(productName: String): ProductComparisonSummary? {
        val trimmed = productName.trim()
        if (trimmed.isEmpty()) return null
        val observations = receiptDao.getPriceObservationsForProduct(trimmed)
        val records = observations.map {
            PriceComparisonRecord(
                storeName = it.storeName,
                unitPriceMinor = it.unitPriceMinor,
                timestamp = it.purchaseTimestamp,
                quantityValue = it.quantityValue,
                quantityUnit = it.quantityUnit
            )
        }
        return CrossStoreComparisonCalculator.calculate(trimmed, records)
    }

    override suspend fun saveConfirmedPurchase(store: Store, receipt: Receipt) {
        storeDao.insertStore(StoreEntity(store.id, store.name, store.address, store.detectedTaxId))
        receiptDao.insertReceipt(
            ReceiptEntity(
                id = receipt.id,
                storeId = receipt.storeId,
                purchaseTimestamp = receipt.purchaseTimestamp,
                totalAmountMinor = receipt.totalAmount.amountMinor,
                rawDocumentUri = receipt.rawDocumentUri
            )
        )
        val itemEntities = receipt.items.map { it.toEntity() }
        if (itemEntities.isNotEmpty()) receiptDao.insertItems(itemEntities)
    }

    override suspend fun recordManualPriceObservation(
        storeName: String,
        productName: String,
        price: Money,
        quantity: ProductQuantity,
        brand: String?,
        presentation: String?,
        discount: Money?,
        discountType: DiscountType
    ) {
        manualPriceRecorder.record(
            storeName = storeName,
            productName = productName,
            price = price,
            quantity = quantity,
            brand = brand,
            presentation = presentation,
            discount = discount,
            discountType = discountType
        )
    }

    override suspend fun getProductDetailData(productIdOrName: String): ProductDetailData? =
        productDetailEditor.getProductDetailData(productIdOrName) { getCrossStoreComparison(it) }

    override suspend fun updateProduct(product: Product, previousName: String?) =
        productDetailEditor.updateProduct(product, previousName)

    override suspend fun updateObservation(
        observationId: String,
        newPrice: Money,
        newQuantity: ProductQuantity,
        newStoreName: String?
    ) = productDetailEditor.updateObservation(observationId, newPrice, newQuantity, newStoreName)

    override suspend fun deleteObservation(observationId: String) =
        productDetailEditor.deleteObservation(observationId)

    override fun getAllProducts(): Flow<List<Product>> =
        productDao.getAllProducts().map { entities -> entities.map { it.toDomain() } }

    override fun searchProducts(query: String): Flow<List<Product>> =
        productDao.searchProducts(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getProductById(productId: String): Product? =
        productDao.getProductById(productId)?.toDomain()

    override suspend fun resolveProductFromDescription(
        rawDescription: String,
        storeId: String?
    ): ProductResolution = catalogResolver.resolveProductFromDescription(rawDescription, storeId)

    override suspend fun registerProductWithAlias(
        product: Product,
        aliasText: String,
        storeId: String?
    ): Product = catalogResolver.registerProductWithAlias(product, aliasText, storeId)

    override suspend fun linkItemToProduct(
        receiptItemId: String,
        productId: String,
        aliasText: String?,
        storeId: String?
    ) = catalogResolver.linkItemToProduct(receiptItemId, productId, aliasText, storeId)

    override fun getShoppingListWithEstimates(): Flow<List<ShoppingListItem>> {
        return shoppingListDao.getAllShoppingListItems().map { entities ->
            entities.map { entity ->
                val obsResult = shoppingListDao.getLatestPriceObservation(entity.productId, entity.name)
                val observation = obsResult?.let {
                    BasketEstimator.createObservation(
                        unitPriceMinor = it.unitPriceMinor,
                        observedTimestamp = it.purchaseTimestamp,
                        storeName = it.storeName
                    )
                }
                entity.toDomain(observation)
            }
        }
    }

    override fun getBasketEstimate(): Flow<BasketEstimate> =
        getShoppingListWithEstimates().map { items -> BasketEstimator.estimateBasket(items) }

    override suspend fun addShoppingListItem(
        name: String,
        productId: String?,
        quantity: Long,
        unit: UnitType
    ) {
        val entity = ShoppingListItemEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            productId = productId,
            quantity = maxOf(1L, quantity),
            unit = unit.name,
            isChecked = false,
            createdAtTimestamp = System.currentTimeMillis()
        )
        shoppingListDao.insertItem(entity)
    }

    override suspend fun toggleItemChecked(itemId: String, isChecked: Boolean) =
        shoppingListDao.updateCheckedStatus(itemId, isChecked)

    override suspend fun removeShoppingListItem(itemId: String) =
        shoppingListDao.deleteItemById(itemId)

    override suspend fun clearCheckedShoppingListItems() =
        shoppingListDao.deleteCheckedItems()

    override suspend fun getLatestPriceObservation(
        productId: String?,
        productName: String
    ): HistoricalPriceObservation? {
        val result = shoppingListDao.getLatestPriceObservation(productId, productName) ?: return null
        return BasketEstimator.createObservation(
            unitPriceMinor = result.unitPriceMinor,
            observedTimestamp = result.purchaseTimestamp,
            storeName = result.storeName
        )
    }
}
