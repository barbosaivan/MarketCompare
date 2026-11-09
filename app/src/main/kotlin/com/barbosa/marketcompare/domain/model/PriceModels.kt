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

/**
 * Entrada comparativa de precio de un producto en un establecimiento específico (MC-045).
 */
data class StorePriceEntry(
    val storeName: String,
    val unitPrice: Money,
    val observedAtTimestamp: Long,
    val freshness: PriceFreshness,
    val priceDifferenceWithLowest: Money,
    val percentageDifferenceWithLowest: Double,
    val isLowestPrice: Boolean
)

/**
 * Resumen de comparación de precios multicomercio para un producto (MC-045).
 */
data class ProductComparisonSummary(
    val productName: String,
    val lowestPrice: Money,
    val highestPrice: Money,
    val maxSavings: Money,
    val maxSavingsPercentage: Double,
    val cheapestStore: String,
    val storeEntries: List<StorePriceEntry>
)

/**
 * Resumen de un establecimiento en el directorio de tiendas (MC-046).
 */
data class StoreSummary(
    val storeId: String?,
    val storeName: String,
    val address: String?,
    val productCount: Int,
    val lastObservationTimestamp: Long
)

/**
 * Estadísticas consolidadas de precios de un comercio (MC-047).
 */
data class StorePriceStatistics(
    val storeName: String,
    val distinctProductsCount: Int,
    val totalObservationsCount: Int,
    val lowestPriceRecorded: Money?,
    val highestPriceRecorded: Money?,
    val lastObservedTimestamp: Long
)
