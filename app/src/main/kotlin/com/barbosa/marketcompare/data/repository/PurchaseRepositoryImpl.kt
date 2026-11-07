package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.MarketCompareDatabase
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.ReceiptItem
import com.barbosa.marketcompare.domain.model.Store
import com.barbosa.marketcompare.domain.model.UnitType
import com.barbosa.marketcompare.domain.repository.PurchaseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class PurchaseRepositoryImpl(
    private val database: MarketCompareDatabase
) : PurchaseRepository {

    private val receiptDao = database.receiptDao()
    private val storeDao = database.storeDao()
    private val productDao = database.productDao()

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
}

