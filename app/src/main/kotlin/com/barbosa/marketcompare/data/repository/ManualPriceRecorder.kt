package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ProductResolution
import java.util.UUID

internal class ManualPriceRecorder internal constructor(
    private val storeDao: StoreDao,
    private val productDao: ProductDao,
    private val receiptDao: ReceiptDao,
    private val catalogResolver: ProductCatalogResolver
) {
    suspend fun record(
        storeName: String,
        productName: String,
        price: Money,
        quantity: ProductQuantity,
        brand: String?,
        presentation: String?,
        discount: Money?,
        discountType: DiscountType
    ) {
        val cleanStore = storeName.trim()
        val cleanProduct = productName.trim()
        if (cleanStore.isEmpty() || cleanProduct.isEmpty()) return

        val store = getOrCreateStore(cleanStore)
        val productId = resolveOrCreateProduct(cleanProduct, store.id, quantity, brand, presentation)
        insertManualObservation(store.id, productId, cleanProduct, price, quantity, discount, discountType)
    }

    private suspend fun getOrCreateStore(name: String): StoreEntity {
        return storeDao.getStoreByName(name) ?: StoreEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            address = null,
            detectedTaxId = null
        ).also { storeDao.insertStore(it) }
    }

    private suspend fun resolveOrCreateProduct(
        name: String,
        storeId: String,
        qty: ProductQuantity,
        brand: String?,
        presentation: String?
    ): String {
        val candidate = when (val res = catalogResolver.resolveProductFromDescription(name, storeId)) {
            is ProductResolution.ExactAliasMatch -> res.product
            is ProductResolution.SuggestedCandidate -> res.candidate
            is ProductResolution.NewProduct -> res.candidate
        }
        val existing = productDao.getProductById(candidate.id)
        return if (existing != null) {
            existing.id
        } else {
            val newProduct = ProductEntity(
                id = candidate.id,
                canonicalName = candidate.canonicalName,
                brand = brand?.trim() ?: candidate.brand,
                category = candidate.category,
                baseQuantity = qty.value,
                unit = qty.unit.name,
                presentation = presentation?.trim() ?: candidate.presentation
            )
            productDao.insertProduct(newProduct)
            newProduct.id
        }
    }

    private suspend fun insertManualObservation(
        storeId: String,
        productId: String,
        productName: String,
        price: Money,
        quantity: ProductQuantity,
        discount: Money?,
        discountType: DiscountType
    ) {
        val receiptId = UUID.randomUUID().toString()
        receiptDao.insertReceipt(
            ReceiptEntity(
                id = receiptId,
                storeId = storeId,
                purchaseTimestamp = System.currentTimeMillis(),
                totalAmountMinor = price.amountMinor,
                rawDocumentUri = null
            )
        )
        val listPrice = if (discount != null) price.plus(discount) else price
        receiptDao.insertItems(
            listOf(
                ReceiptItemEntity(
                    id = UUID.randomUUID().toString(),
                    receiptId = receiptId,
                    productId = productId,
                    rawDescription = productName,
                    quantityValue = quantity.value,
                    quantityUnit = quantity.unit.name,
                    listPriceMinor = listPrice.amountMinor,
                    discountMinor = discount?.amountMinor,
                    finalPriceMinor = price.amountMinor,
                    discountType = discountType.name,
                    discountEvidence = null,
                    confidenceScore = 1.0f,
                    userConfirmed = true
                )
            )
        )
    }
}
