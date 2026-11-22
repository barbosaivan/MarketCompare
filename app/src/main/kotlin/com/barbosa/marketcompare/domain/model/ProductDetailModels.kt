package com.barbosa.marketcompare.domain.model

/**
 * Datos consolidados y editables de un producto y su historial de precios (MC-050).
 */
data class ProductDetailData(
    val product: Product,
    val observations: List<ProductPriceObservationItem>,
    val comparisonSummary: ProductComparisonSummary? = null
)

