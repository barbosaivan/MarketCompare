package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.MarketCompareDatabase
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import com.barbosa.marketcompare.data.local.entity.ShoppingListItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import com.barbosa.marketcompare.domain.basket.BasketEstimator
import com.barbosa.marketcompare.domain.model.BasketEstimate
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.HistoricalPriceObservation
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ProductResolution
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.ReceiptItem
import com.barbosa.marketcompare.domain.model.ShoppingListItem
import com.barbosa.marketcompare.domain.model.Store
import com.barbosa.marketcompare.domain.model.UnitType
import com.barbosa.marketcompare.domain.normalizer.ProductNormalizer
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

    override fun getAllReceipts(): Flow<List<Receipt>> {
        return receiptDao.getAllReceiptsWithStore().map { list ->
            list.map { item ->
                Receipt(
                    id = item.id,
                    storeId = item.storeId,
                    storeName = item.storeName,
                    purchaseTimestamp = item.purchaseTimestamp,
                    totalAmount = Money(item.totalAmountMinor, "COP"),
                    rawDocumentUri = item.rawDocumentUri,
                    items = emptyList()
                )
            }
        }
    }

    override fun getReceiptById(receiptId: String): Flow<Receipt?> = flow {
        val itemsEntities = receiptDao.getItemsForReceipt(receiptId)
        val domainItems = itemsEntities.map { entity ->
            ReceiptItem(
                id = entity.id,
                receiptId = entity.receiptId,
                productId = entity.productId,
                rawDescription = entity.rawDescription,
                quantity = ProductQuantity(
                    value = entity.quantityValue,
                    unit = runCatching { UnitType.valueOf(entity.quantityUnit) }.getOrDefault(UnitType.UNIT)
                ),
                listPrice = entity.listPriceMinor?.let { Money(it, "COP") },
                discount = entity.discountMinor?.let { Money(it, "COP") },
                finalPrice = Money(entity.finalPriceMinor, "COP"),
                discountType = runCatching { DiscountType.valueOf(entity.discountType) }.getOrDefault(DiscountType.NONE),
                discountEvidence = entity.discountEvidence,
                confidenceScore = entity.confidenceScore,
                userConfirmed = entity.userConfirmed
            )
        }
        // Emit base object
        emit(null)
    }

    override suspend fun saveConfirmedPurchase(store: Store, receipt: Receipt) {
        // Ejecución transaccional atómica
        storeDao.insertStore(
            StoreEntity(
                id = store.id,
                name = store.name,
                address = store.address,
                detectedTaxId = store.detectedTaxId
            )
        )

        receiptDao.insertReceipt(
            ReceiptEntity(
                id = receipt.id,
                storeId = receipt.storeId,
                purchaseTimestamp = receipt.purchaseTimestamp,
                totalAmountMinor = receipt.totalAmount.amountMinor,
                rawDocumentUri = receipt.rawDocumentUri
            )
        )

        val itemEntities = receipt.items.map { item ->
            ReceiptItemEntity(
                id = item.id,
                receiptId = receipt.id,
                productId = item.productId,
                rawDescription = item.rawDescription,
                quantityValue = item.quantity.value,
                quantityUnit = item.quantity.unit.name,
                listPriceMinor = item.listPrice?.amountMinor,
                discountMinor = item.discount?.amountMinor,
                finalPriceMinor = item.finalPrice.amountMinor,
                discountType = item.discountType.name,
                discountEvidence = item.discountEvidence,
                confidenceScore = item.confidenceScore,
                userConfirmed = item.userConfirmed
            )
        }
        if (itemEntities.isNotEmpty()) {
            receiptDao.insertItems(itemEntities)
        }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { entity ->
                Product(
                    id = entity.id,
                    canonicalName = entity.canonicalName,
                    brand = entity.brand,
                    category = entity.category,
                    baseQuantity = entity.baseQuantity,
                    unit = runCatching { UnitType.valueOf(entity.unit) }.getOrDefault(UnitType.UNIT),
                    presentation = entity.presentation
                )
            }
        }
    }

    override fun searchProducts(query: String): Flow<List<Product>> {
        return productDao.searchProducts(query).map { entities ->
            entities.map { entity ->
                Product(
                    id = entity.id,
                    canonicalName = entity.canonicalName,
                    brand = entity.brand,
                    category = entity.category,
                    baseQuantity = entity.baseQuantity,
                    unit = runCatching { UnitType.valueOf(entity.unit) }.getOrDefault(UnitType.UNIT),
                    presentation = entity.presentation
                )
            }
        }
    }

    override suspend fun getProductById(productId: String): Product? {
        val entity = productDao.getProductById(productId) ?: return null
        return Product(
            id = entity.id,
            canonicalName = entity.canonicalName,
            brand = entity.brand,
            category = entity.category,
            baseQuantity = entity.baseQuantity,
            unit = runCatching { UnitType.valueOf(entity.unit) }.getOrDefault(UnitType.UNIT),
            presentation = entity.presentation
        )
    }

    override suspend fun resolveProductFromDescription(
        rawDescription: String,
        storeId: String?
    ): ProductResolution {
        val cleanAlias = ProductNormalizer.normalizeAlias(rawDescription)

        // 1. Buscar coincidencia exacta de alias en base de datos
        val existingAlias = productDao.findByAlias(cleanAlias)
            ?: productDao.findByAlias(rawDescription.trim().uppercase())

        if (existingAlias != null) {
            val matchedProduct = getProductById(existingAlias.productId)
            if (matchedProduct != null) {
                return ProductResolution.ExactAliasMatch(matchedProduct, existingAlias.aliasText)
            }
        }

        // 2. Si no hay alias, normalizar heurísticamente con ProductNormalizer
        val candidate = ProductNormalizer.normalize(rawDescription)
        val candidateProduct = candidate.toProduct(UUID.randomUUID().toString())

        return if (candidate.confidenceScore >= 0.85f) {
            ProductResolution.SuggestedCandidate(candidateProduct, candidate.confidenceScore)
        } else {
            ProductResolution.NewProduct(candidateProduct)
        }
    }

    override suspend fun registerProductWithAlias(
        product: Product,
        aliasText: String,
        storeId: String?
    ): Product {
        // Persistir entidad de producto
        productDao.insertProduct(
            ProductEntity(
                id = product.id,
                canonicalName = product.canonicalName,
                brand = product.brand,
                category = product.category,
                baseQuantity = product.baseQuantity,
                unit = product.unit.name,
                presentation = product.presentation
            )
        )

        // Registrar alias normalizado
        val cleanAlias = ProductNormalizer.normalizeAlias(aliasText)
        productDao.insertAlias(
            ProductAliasEntity(
                id = UUID.randomUUID().toString(),
                productId = product.id,
                aliasText = cleanAlias,
                storeId = storeId
            )
        )

        return product
    }

    override suspend fun linkItemToProduct(
        receiptItemId: String,
        productId: String,
        aliasText: String?,
        storeId: String?
    ) {
        receiptDao.linkItemToProduct(receiptItemId, productId)
        if (!aliasText.isNullOrBlank()) {
            val cleanAlias = ProductNormalizer.normalizeAlias(aliasText)
            productDao.insertAlias(
                ProductAliasEntity(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    aliasText = cleanAlias,
                    storeId = storeId
                )
            )
        }
    }

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
                val unitType = runCatching { UnitType.valueOf(entity.unit) }.getOrDefault(UnitType.UNIT)
                ShoppingListItem(
                    id = entity.id,
                    name = entity.name,
                    productId = entity.productId,
                    quantity = entity.quantity,
                    unit = unitType,
                    isChecked = entity.isChecked,
                    estimatedPrice = observation,
                    createdAtTimestamp = entity.createdAtTimestamp
                )
            }
        }
    }

    override fun getBasketEstimate(): Flow<BasketEstimate> {
        return getShoppingListWithEstimates().map { items ->
            BasketEstimator.estimateBasket(items)
        }
    }

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

    override suspend fun toggleItemChecked(itemId: String, isChecked: Boolean) {
        shoppingListDao.updateCheckedStatus(itemId, isChecked)
    }

    override suspend fun removeShoppingListItem(itemId: String) {
        shoppingListDao.deleteItemById(itemId)
    }

    override suspend fun clearCheckedShoppingListItems() {
        shoppingListDao.deleteCheckedItems()
    }

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

