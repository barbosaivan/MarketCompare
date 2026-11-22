package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.ShoppingListDao
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import com.barbosa.marketcompare.data.local.entity.toDomain
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ProductDetailData
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.UnitType
import java.util.UUID

internal class ProductDetailEditor(
    private val productDao: ProductDao,
    private val receiptDao: ReceiptDao,
    private val storeDao: StoreDao,
    private val shoppingListDao: ShoppingListDao
) {

    suspend fun getProductDetailData(
        productIdOrName: String,
        getComparison: suspend (String) -> ProductComparisonSummary?
    ): ProductDetailData? {
        val product = resolveTargetProduct(productIdOrName) ?: return null
        val rawObservations = receiptDao.getPriceObservationsForProduct(product.canonicalName)
            .ifEmpty { receiptDao.getPriceObservationsForProduct(productIdOrName) }

        val lowestPriceMinor = rawObservations.minOfOrNull { it.unitPriceMinor } ?: 0L
        val observationItems = rawObservations.map { entity ->
            entity.toDomain(isLowestPrice = entity.unitPriceMinor == lowestPriceMinor)
        }

        val comparison = getComparison(product.canonicalName)
        return ProductDetailData(
            product = product,
            observations = observationItems,
            comparisonSummary = comparison
        )
    }

    private suspend fun resolveTargetProduct(productIdOrName: String): Product? {
        val productEntity = productDao.getProductById(productIdOrName)
            ?: productDao.getProductByName(productIdOrName)

        if (productEntity != null) {
            return productEntity.toDomain()
        }

        val alias = productDao.findByAlias(productIdOrName.trim().uppercase())
        if (alias != null) {
            val byAlias = productDao.getProductById(alias.productId)
            if (byAlias != null) return byAlias.toDomain()
        }

        val observations = receiptDao.getPriceObservationsForProduct(productIdOrName)
        if (observations.isNotEmpty()) {
            return Product(
                id = UUID.randomUUID().toString(),
                canonicalName = productIdOrName.trim(),
                brand = null,
                category = null,
                baseQuantity = 1L,
                unit = UnitType.UNIT,
                presentation = null
            )
        }
        return null
    }

    suspend fun updateProduct(product: Product, previousName: String?) {
        val entity = ProductEntity(
            id = product.id,
            canonicalName = product.canonicalName.trim(),
            brand = product.brand?.trim()?.ifBlank { null },
            category = product.category?.trim()?.ifBlank { null },
            baseQuantity = product.baseQuantity,
            unit = product.unit.name,
            presentation = product.presentation?.trim()?.ifBlank { null }
        )
        productDao.insertProduct(entity)

        if (!previousName.isNullExcludingBlank() || previousName != product.canonicalName) {
            val oldName = previousName ?: product.canonicalName
            receiptDao.updateItemDescriptions(product.id, oldName, product.canonicalName)
            shoppingListDao.updateItemNames(product.id, oldName, product.canonicalName)
        }

        productDao.insertAlias(
            ProductAliasEntity(
                id = UUID.randomUUID().toString(),
                productId = product.id,
                aliasText = product.canonicalName.trim().uppercase(),
                storeId = null
            )
        )
    }

    suspend fun updateObservation(
        observationId: String,
        newPrice: Money,
        newQuantity: ProductQuantity,
        newStoreName: String?
    ) {
        receiptDao.updateReceiptItemPrice(
            itemId = observationId,
            finalPriceMinor = newPrice.amountMinor,
            listPriceMinor = null,
            quantityValue = newQuantity.value,
            quantityUnit = newQuantity.unit.name
        )

        if (!newStoreName.isNullOrBlank()) {
            val item = receiptDao.getReceiptItemById(observationId) ?: return
            val store = storeDao.getStoreByName(newStoreName.trim()) ?: run {
                val createdStore = StoreEntity(
                    id = UUID.randomUUID().toString(),
                    name = newStoreName.trim(),
                    address = null,
                    detectedTaxId = null
                )
                storeDao.insertStore(createdStore)
                createdStore
            }
            receiptDao.updateReceiptStore(item.receiptId, store.id)
        }
    }

    suspend fun deleteObservation(observationId: String) {
        receiptDao.deleteReceiptItem(observationId)
    }

    private fun String?.isNullExcludingBlank(): Boolean = this == null || this.isBlank()
}

