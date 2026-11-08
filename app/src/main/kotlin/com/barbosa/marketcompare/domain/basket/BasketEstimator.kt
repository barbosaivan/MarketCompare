package com.barbosa.marketcompare.domain.basket

import com.barbosa.marketcompare.domain.model.BasketEstimate
import com.barbosa.marketcompare.domain.model.HistoricalPriceObservation
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.PriceFreshness
import com.barbosa.marketcompare.domain.model.ShoppingListItem

object BasketEstimator {

    const val STALE_THRESHOLD_DAYS = 30L

    /**
     * Determina la frescura del precio en función de los días transcurridos desde la compra observada.
     */
    fun evaluateFreshness(observedTimestamp: Long, currentTimestamp: Long = System.currentTimeMillis()): PriceFreshness {
        val diffMs = currentTimestamp - observedTimestamp
        val days = if (diffMs > 0) diffMs / (1000L * 60 * 60 * 24) else 0L
        return if (days <= STALE_THRESHOLD_DAYS) {
            PriceFreshness.RECENT
        } else {
            PriceFreshness.STALE
        }
    }

    /**
     * Construye una observación histórica a partir de los datos sin procesar de la base de datos local.
     */
    fun createObservation(
        unitPriceMinor: Long,
        observedTimestamp: Long,
        storeName: String,
        currentTimestamp: Long = System.currentTimeMillis()
    ): HistoricalPriceObservation {
        val freshness = evaluateFreshness(observedTimestamp, currentTimestamp)
        return HistoricalPriceObservation(
            unitPrice = Money(unitPriceMinor, "COP"),
            observedAtTimestamp = observedTimestamp,
            storeName = storeName,
            freshness = freshness
        )
    }

    /**
     * Calcula la estimación total de la canasta a partir de los artículos de la lista de compras.
     * Solo toma en cuenta artículos activos no tachados (isChecked == false).
     */
    fun estimateBasket(items: List<ShoppingListItem>): BasketEstimate {
        val activeItems = items.filter { !it.isChecked }
        val pricedItems = activeItems.filter { it.estimatedPrice != null }

        val totalMinor = pricedItems.mapNotNull { it.estimatedTotal }.sumOf { it.amountMinor }
        val staleCount = pricedItems.count { it.estimatedPrice?.freshness == PriceFreshness.STALE }
        val unknownCount = activeItems.size - pricedItems.size

        return BasketEstimate(
            items = items,
            totalEstimated = Money(totalMinor, "COP"),
            totalItemsCount = activeItems.size,
            pricedItemsCount = pricedItems.size,
            staleItemsCount = staleCount,
            unknownItemsCount = unknownCount
        )
    }
}

