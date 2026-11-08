package com.barbosa.marketcompare.domain.model

import java.util.UUID

enum class PriceFreshness {
    RECENT,
    STALE,
    UNKNOWN
}

data class HistoricalPriceObservation(
    val unitPrice: Money,
    val observedAtTimestamp: Long,
    val storeName: String,
    val freshness: PriceFreshness
) {
    fun getAgeInDays(now: Long = System.currentTimeMillis()): Long {
        val diff = now - observedAtTimestamp
        return if (diff > 0) diff / (1000L * 60 * 60 * 24) else 0L
    }
}

data class ShoppingListItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val productId: String? = null,
    val quantity: Long = 1L,
    val unit: UnitType = UnitType.UNIT,
    val isChecked: Boolean = false,
    val estimatedPrice: HistoricalPriceObservation? = null,
    val createdAtTimestamp: Long = System.currentTimeMillis()
) {
    val estimatedTotal: Money?
        get() = estimatedPrice?.unitPrice?.let { it * quantity }
}

data class BasketEstimate(
    val items: List<ShoppingListItem>,
    val totalEstimated: Money,
    val totalItemsCount: Int,
    val pricedItemsCount: Int,
    val staleItemsCount: Int,
    val unknownItemsCount: Int
)

