package com.barbosa.marketcompare.domain.comparison

import com.barbosa.marketcompare.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CrossStoreComparisonCalculatorTest {

    @Test
    fun `calculate returns null when product name or records are empty`() {
        assertNull(CrossStoreComparisonCalculator.calculate("", emptyList()))
        assertNull(CrossStoreComparisonCalculator.calculate("   ", emptyList()))
        assertNull(CrossStoreComparisonCalculator.calculate("Arroz", emptyList()))
    }

    @Test
    fun `calculate correctly identifies lowest price and computes savings across multiple stores`() {
        val now = System.currentTimeMillis()
        val records = listOf(
            PriceComparisonRecord(storeName = "Éxito", unitPriceMinor = 4500L, timestamp = now),
            PriceComparisonRecord(storeName = "D1", unitPriceMinor = 3800L, timestamp = now),
            PriceComparisonRecord(storeName = "Ara", unitPriceMinor = 4000L, timestamp = now)
        )

        val summary = CrossStoreComparisonCalculator.calculate("Leche Entera 1L", records)

        assertNotNull(summary)
        summary?.let {
            assertEquals("Leche Entera 1L", it.productName)
            assertEquals("D1", it.cheapestStore)
            assertEquals(Money(3800L, "COP"), it.lowestPrice)
            assertEquals(Money(4500L, "COP"), it.highestPrice)
            assertEquals(Money(700L, "COP"), it.maxSavings)
            assertEquals(3, it.storeEntries.size)

            // Ranked ascending by price: D1 (3800), Ara (4000), Éxito (4500)
            assertEquals("D1", it.storeEntries[0].storeName)
            assertTrue(it.storeEntries[0].isLowestPrice)
            assertEquals(Money(0L, "COP"), it.storeEntries[0].priceDifferenceWithLowest)

            assertEquals("Ara", it.storeEntries[1].storeName)
            assertEquals(Money(200L, "COP"), it.storeEntries[1].priceDifferenceWithLowest)

            assertEquals("Éxito", it.storeEntries[2].storeName)
            assertEquals(Money(700L, "COP"), it.storeEntries[2].priceDifferenceWithLowest)
        }
    }

    @Test
    fun `calculate retains only latest observation per store when duplicates exist`() {
        val oldTimestamp = 1000L
        val newTimestamp = 5000L
        val records = listOf(
            PriceComparisonRecord(storeName = "D1", unitPriceMinor = 3500L, timestamp = oldTimestamp),
            PriceComparisonRecord(storeName = "D1", unitPriceMinor = 3800L, timestamp = newTimestamp)
        )

        val summary = CrossStoreComparisonCalculator.calculate("Arroz", records)

        assertNotNull(summary)
        assertEquals(1, summary?.storeEntries?.size)
        assertEquals(Money(3800L, "COP"), summary?.lowestPrice)
        assertEquals(newTimestamp, summary?.storeEntries?.get(0)?.observedAtTimestamp)
    }

    @Test
    fun `calculate handles single store correctly with zero savings`() {
        val records = listOf(
            PriceComparisonRecord(storeName = "Jumbo", unitPriceMinor = 5200L, timestamp = 1000L)
        )

        val summary = CrossStoreComparisonCalculator.calculate("Aceite", records)

        assertNotNull(summary)
        assertEquals(Money(0L, "COP"), summary?.maxSavings)
        assertEquals(0.0, summary?.maxSavingsPercentage ?: -1.0, 0.001)
        assertEquals("Jumbo", summary?.cheapestStore)
    }
}

