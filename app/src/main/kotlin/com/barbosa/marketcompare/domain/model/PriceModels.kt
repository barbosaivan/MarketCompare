package com.barbosa.marketcompare.domain.model

/**
 * Representa una observación de precio de un producto en un comercio específico (MC-044).
 * Utilizado por el explorador de precios y el catálogo multicomercio.
 */
data class ProductPriceObservationItem(
    val id: String,
    val productId: String?,
    val productName: String,
    val storeName: String,
    val unitPrice: Money,
    val observedAtTimestamp: Long,
    val isLowestPrice: Boolean = false,
    val freshness: PriceFreshness = PriceFreshness.RECENT
)

