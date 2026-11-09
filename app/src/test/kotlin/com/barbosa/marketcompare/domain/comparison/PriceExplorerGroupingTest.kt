package com.barbosa.marketcompare.domain.comparison

import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.PriceFreshness
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PriceExplorerGroupingTest {

    @Test
    fun `grouping observations detects cheapest store correctly`() {
        val observations = listOf(
            ProductPriceObservationItem(
                id = "1",
                productId = "prod-1",
                productName = "Arroz Diana 1kg",
                storeName = "Éxito",
                unitPrice = Money(4500L, "COP"),
                observedAtTimestamp = 1000L,
                freshness = PriceFreshness.RECENT
            ),
            ProductPriceObservationItem(
                id = "2",
                productId = "prod-1",
                productName = "Arroz Diana 1kg",
                storeName = "D1",
                unitPrice = Money(3800L, "COP"),
                observedAtTimestamp = 2000L,
                freshness = PriceFreshness.RECENT
            ),
            ProductPriceObservationItem(
                id = "3",
                productId = "prod-1",
                productName = "Arroz Diana 1kg",
                storeName = "Ara",
                unitPrice = Money(4000L, "COP"),
                observedAtTimestamp = 3000L,
                freshness = PriceFreshness.RECENT
            )
        )

        val minPrice = observations.minOf { it.unitPrice.amountMinor }
        assertEquals(3800L, minPrice)

        val withLowestPriceBadge = observations.map { item ->
            item.copy(isLowestPrice = item.unitPrice.amountMinor == minPrice)
        }

        assertFalse(withLowestPriceBadge.first { it.storeName == "Éxito" }.isLowestPrice)
        assertTrue(withLowestPriceBadge.first { it.storeName == "D1" }.isLowestPrice)
        assertFalse(withLowestPriceBadge.first { it.storeName == "Ara" }.isLowestPrice)
    }

    @Test
    fun `filtering observations by store and query works with case insensitivity`() {
        val observations = listOf(
            ProductPriceObservationItem(
                id = "1",
                productId = null,
                productName = "LECHE ENTERA ALQUERIA",
                storeName = "Éxito Calle 80",
                unitPrice = Money(4600L, "COP"),
                observedAtTimestamp = 1000L
            ),
            ProductPriceObservationItem(
                id = "2",
                productId = null,
                productName = "leche descremada d1",
                storeName = "D1 Normandía",
                unitPrice = Money(3700L, "COP"),
                observedAtTimestamp = 2000L
            ),
            ProductPriceObservationItem(
                id = "3",
                productId = null,
                productName = "HUEVOS ORO AA",
                storeName = "Éxito Calle 80",
                unitPrice = Money(18000L, "COP"),
                observedAtTimestamp = 3000L
            )
        )

        // Filter by query "leche"
        val queryMatches = observations.filter { it.productName.contains("leche", ignoreCase = true) }
        assertEquals(2, queryMatches.size)

        // Filter by store "Éxito Calle 80"
        val storeMatches = observations.filter { it.storeName == "Éxito Calle 80" }
        assertEquals(2, storeMatches.size)

        // Filter by both
        val combined = observations.filter {
            it.storeName == "Éxito Calle 80" && it.productName.contains("leche", ignoreCase = true)
        }
        assertEquals(1, combined.size)
        assertEquals("LECHE ENTERA ALQUERIA", combined[0].productName)
    }

    @Test
    fun `distinct monitored products and stores count matches unique names`() {
        val observations = listOf(
            ProductPriceObservationItem(id = "1", productId = null, productName = "Pan", storeName = "D1", unitPrice = Money(2500L, "COP"), observedAtTimestamp = 1L),
            ProductPriceObservationItem(id = "2", productId = null, productName = "pan", storeName = "Ara", unitPrice = Money(2700L, "COP"), observedAtTimestamp = 2L),
            ProductPriceObservationItem(id = "3", productId = null, productName = "Leche", storeName = "D1", unitPrice = Money(3800L, "COP"), observedAtTimestamp = 3L)
        )

        val uniqueProducts = observations.map { it.productName.trim().lowercase() }.distinct().size
        val uniqueStores = observations.map { it.storeName.trim().lowercase() }.distinct().size

        assertEquals(2, uniqueProducts) // "pan" and "leche"
        assertEquals(2, uniqueStores) // "d1" and "ara"
    }

    @Test
    fun `best deal price selects lowest active observation`() {
        val emptyObservations = emptyList<ProductPriceObservationItem>()
        val emptyBestDeal = emptyObservations.minByOrNull { it.unitPrice.amountMinor }?.unitPrice
        assertNull(emptyBestDeal)

        val observations = listOf(
            ProductPriceObservationItem(id = "1", productId = null, productName = "Aceite", storeName = "Éxito", unitPrice = Money(12000L, "COP"), observedAtTimestamp = 1L),
            ProductPriceObservationItem(id = "2", productId = null, productName = "Sal", storeName = "D1", unitPrice = Money(1500L, "COP"), observedAtTimestamp = 2L)
        )
        val bestDeal = observations.minByOrNull { it.unitPrice.amountMinor }?.unitPrice
        assertEquals(Money(1500L, "COP"), bestDeal)
    }
}
