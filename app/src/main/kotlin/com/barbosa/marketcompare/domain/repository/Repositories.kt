package com.barbosa.marketcompare.domain.repository

import com.barbosa.marketcompare.domain.model.ExternalPriceQuote
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.Store
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

