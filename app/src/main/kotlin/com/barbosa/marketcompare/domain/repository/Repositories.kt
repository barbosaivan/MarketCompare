package com.barbosa.marketcompare.domain.repository

import com.barbosa.marketcompare.domain.model.BasketEstimate
import com.barbosa.marketcompare.domain.model.ExternalPriceQuote
import com.barbosa.marketcompare.domain.model.HistoricalPriceObservation
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.StorePriceStatistics
import com.barbosa.marketcompare.domain.model.StoreSummary
import com.barbosa.marketcompare.domain.model.ShoppingListItem
import com.barbosa.marketcompare.domain.model.Store
import com.barbosa.marketcompare.domain.model.UnitType
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    fun getAllReceipts(): Flow<List<Receipt>>
    fun getReceiptById(receiptId: String): Flow<Receipt?>
    suspend fun saveConfirmedPurchase(store: Store, receipt: Receipt)
    fun getAllProducts(): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getProductById(productId: String): Product?
    suspend fun resolveProductFromDescription(rawDescription: String, storeId: String? = null): com.barbosa.marketcompare.domain.model.ProductResolution
    suspend fun registerProductWithAlias(product: Product, aliasText: String, storeId: String? = null): Product
    suspend fun linkItemToProduct(receiptItemId: String, productId: String, aliasText: String? = null, storeId: String? = null)

    // Explorador de precios e inteligencia de góndola (MC-044, MC-045, MC-046, MC-047)
    fun getAllPriceObservations(): Flow<List<ProductPriceObservationItem>>
    fun getAllStoreNames(): Flow<List<String>>
    fun getStoreSummaries(): Flow<List<StoreSummary>>
    fun getProductsForStore(storeName: String): Flow<List<ProductPriceObservationItem>>
    fun getPriceTimeline(productName: String): Flow<List<ProductPriceObservationItem>>
    fun getPriceObservationsBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<ProductPriceObservationItem>>
    suspend fun getStoreStatistics(storeName: String): StorePriceStatistics?
    suspend fun getCrossStoreComparison(productName: String): ProductComparisonSummary?

    // Lista de compras y canasta estimada (MC-029)
    fun getShoppingListWithEstimates(): Flow<List<ShoppingListItem>>
    fun getBasketEstimate(): Flow<BasketEstimate>
    suspend fun addShoppingListItem(name: String, productId: String? = null, quantity: Long = 1L, unit: UnitType = UnitType.UNIT)
    suspend fun toggleItemChecked(itemId: String, isChecked: Boolean)
    suspend fun removeShoppingListItem(itemId: String)
    suspend fun clearCheckedShoppingListItems()
    suspend fun getLatestPriceObservation(productId: String?, productName: String): HistoricalPriceObservation?
}

/**
 * Proveedor de precios externo. Consulta efímera en memoria (ADR 0001).
 * Implementación desacoplada y sin persistencia en disco.
 */
interface ExternalPriceProvider {
    suspend fun searchReferencePrice(
        productName: String,
        brand: String?
    ): Result<List<ExternalPriceQuote>>
}

