package com.barbosa.marketcompare.domain.basket

import com.barbosa.marketcompare.domain.model.HistoricalPriceObservation
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.PriceFreshness
import com.barbosa.marketcompare.domain.model.ShoppingListItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BasketEstimatorTest {

    private val now = 1770000000000L // Referencia fija en ms
    private val oneDayMs = 24L * 60 * 60 * 1000

    @Test
    fun `evaluateFreshness marks prices within 30 days as RECENT`() {
        val tenDaysAgo = now - (10 * oneDayMs)
        val freshness = BasketEstimator.evaluateFreshness(tenDaysAgo, now)
        assertEquals(PriceFreshness.RECENT, freshness)
    }

    @Test
    fun `evaluateFreshness marks prices exactly 30 days old as RECENT`() {
        val thirtyDaysAgo = now - (30 * oneDayMs)
        val freshness = BasketEstimator.evaluateFreshness(thirtyDaysAgo, now)
        assertEquals(PriceFreshness.RECENT, freshness)
    }

    @Test
    fun `evaluateFreshness marks prices older than 30 days as STALE`() {
        val thirtyOneDaysAgo = now - (31 * oneDayMs)
        val freshness = BasketEstimator.evaluateFreshness(thirtyOneDaysAgo, now)
        assertEquals(PriceFreshness.STALE, freshness)

        val sixtyDaysAgo = now - (60 * oneDayMs)
        assertEquals(PriceFreshness.STALE, BasketEstimator.evaluateFreshness(sixtyDaysAgo, now))
    }

    @Test
    fun `createObservation sets correct Money and freshness`() {
        val fiveDaysAgo = now - (5 * oneDayMs)
        val obs = BasketEstimator.createObservation(
            unitPriceMinor = 4500L,
            observedTimestamp = fiveDaysAgo,
            storeName = "D1",
            currentTimestamp = now
        )

        assertEquals(Money(4500L, "COP"), obs.unitPrice)
        assertEquals("D1", obs.storeName)
        assertEquals(PriceFreshness.RECENT, obs.freshness)
        assertEquals(5L, obs.getAgeInDays(now))
    }

    @Test
    fun `estimateBasket computes totals and differentiates stale and unknown prices`() {
        val items = listOf(
            ShoppingListItem(
                name = "Leche Entera",
                quantity = 2L,
                estimatedPrice = HistoricalPriceObservation(
                    unitPrice = Money(4200L, "COP"),
                    observedAtTimestamp = now - (5 * oneDayMs),
                    storeName = "Éxito",
                    freshness = PriceFreshness.RECENT
                )
            ),
            ShoppingListItem(
                name = "Arroz 1kg",
                quantity = 3L,
                estimatedPrice = HistoricalPriceObservation(
                    unitPrice = Money(3800L, "COP"),
                    observedAtTimestamp = now - (45 * oneDayMs),
                    storeName = "D1",
                    freshness = PriceFreshness.STALE
                )
            ),
            ShoppingListItem(
                name = "Fruta exótica sin historial",
                quantity = 1L,
                estimatedPrice = null
            )
        )

        val estimate = BasketEstimator.estimateBasket(items)

        // Leche: 2 * 4200 = 8400
        // Arroz: 3 * 3800 = 11400
        // Total = 19800
        assertEquals(Money(19800L, "COP"), estimate.totalEstimated)
        assertEquals(3, estimate.totalItemsCount)
        assertEquals(2, estimate.pricedItemsCount)
        assertEquals(1, estimate.staleItemsCount)
        assertEquals(1, estimate.unknownItemsCount)
    }

    @Test
    fun `estimateBasket ignores checked items in totals and active counts`() {
        val items = listOf(
            ShoppingListItem(
                name = "Pan tajado",
                quantity = 1L,
                isChecked = true,
                estimatedPrice = HistoricalPriceObservation(
                    unitPrice = Money(5000L, "COP"),
                    observedAtTimestamp = now - (2 * oneDayMs),
                    storeName = "Ara",
                    freshness = PriceFreshness.RECENT
                )
            ),
            ShoppingListItem(
                name = "Huevos AA x30",
                quantity = 1L,
                isChecked = false,
                estimatedPrice = HistoricalPriceObservation(
                    unitPrice = Money(18000L, "COP"),
                    observedAtTimestamp = now - (10 * oneDayMs),
                    storeName = "Olímpica",
                    freshness = PriceFreshness.RECENT
                )
            )
        )

        val estimate = BasketEstimator.estimateBasket(items)

        // Solo Huevos AA debe ser sumado (18000)
        assertEquals(Money(18000L, "COP"), estimate.totalEstimated)
        assertEquals(1, estimate.totalItemsCount)
        assertEquals(1, estimate.pricedItemsCount)
        assertEquals(0, estimate.staleItemsCount)
        assertEquals(0, estimate.unknownItemsCount)
    }

    @Test
    fun `estimateBasket handles empty list`() {
        val estimate = BasketEstimator.estimateBasket(emptyList())

        assertEquals(Money(0L, "COP"), estimate.totalEstimated)
        assertEquals(0, estimate.totalItemsCount)
        assertEquals(0, estimate.pricedItemsCount)
        assertEquals(0, estimate.staleItemsCount)
        assertEquals(0, estimate.unknownItemsCount)
    }
}

