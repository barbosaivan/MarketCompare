package com.barbosa.marketcompare.domain.normalizer

import com.barbosa.marketcompare.domain.comparison.CrossStoreComparisonCalculator
import com.barbosa.marketcompare.domain.comparison.PriceComparisonRecord
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StandardPriceComparisonIntegrationTest {

    @Test
    fun `cross-store comparison attaches standard measurement for liquid volumes`() {
        val records = listOf(
            PriceComparisonRecord(
                storeName = "D1",
                unitPriceMinor = 3600L,
                timestamp = 1000L,
                quantityValue = 900L,
                quantityUnit = "MILLILITER"
            ),
            PriceComparisonRecord(
                storeName = "Éxito",
                unitPriceMinor = 4400L,
                timestamp = 1000L,
                quantityValue = 1100L,
                quantityUnit = "LITER"
            ),
            PriceComparisonRecord(
                storeName = "Carulla",
                unitPriceMinor = 4200L,
                timestamp = 1000L,
                quantityValue = 1L,
                quantityUnit = "LITER"
            )
        )

        val summary = CrossStoreComparisonCalculator.calculate("Leche Entera", records)
        assertNotNull(summary)

        val d1Entry = summary?.storeEntries?.firstOrNull { it.storeName == "D1" }
        assertNotNull(d1Entry?.standardMeasurement)
        assertEquals(Money(4000L, "COP"), d1Entry?.standardMeasurement?.primaryPrice)
        assertEquals("L", d1Entry?.standardMeasurement?.primaryUnitLabel)

        val exitoEntry = summary?.storeEntries?.firstOrNull { it.storeName == "Éxito" }
        assertNotNull(exitoEntry?.standardMeasurement)
        assertEquals(Money(4000L, "COP"), exitoEntry?.standardMeasurement?.primaryPrice)
        assertEquals("L", exitoEntry?.standardMeasurement?.primaryUnitLabel)

        val carullaEntry = summary?.storeEntries?.firstOrNull { it.storeName == "Carulla" }
        assertNotNull(carullaEntry?.standardMeasurement)
        assertEquals(Money(4200L, "COP"), carullaEntry?.standardMeasurement?.primaryPrice)
        assertEquals("L", carullaEntry?.standardMeasurement?.primaryUnitLabel)
    }

    @Test
    fun `cross-store comparison attaches standard measurement for solid weights`() {
        val records = listOf(
            PriceComparisonRecord(
                storeName = "Ara",
                unitPriceMinor = 2500L,
                timestamp = 1000L,
                quantityValue = 500L,
                quantityUnit = "GRAM"
            ),
            PriceComparisonRecord(
                storeName = "Olímpica",
                unitPriceMinor = 4800L,
                timestamp = 1000L,
                quantityValue = 1L,
                quantityUnit = "KILOGRAM"
            )
        )

        val summary = CrossStoreComparisonCalculator.calculate("Arroz Blanco", records)
        assertNotNull(summary)

        val araEntry = summary?.storeEntries?.firstOrNull { it.storeName == "Ara" }
        assertEquals(Money(5000L, "COP"), araEntry?.standardMeasurement?.primaryPrice)
        assertEquals("kg", araEntry?.standardMeasurement?.primaryUnitLabel)
        assertEquals(Money(5L, "COP"), araEntry?.standardMeasurement?.secondaryPrice)
        assertEquals("g", araEntry?.standardMeasurement?.secondaryUnitLabel)

        val olimpicaEntry = summary?.storeEntries?.firstOrNull { it.storeName == "Olímpica" }
        assertEquals(Money(4800L, "COP"), olimpicaEntry?.standardMeasurement?.primaryPrice)
        assertEquals("kg", olimpicaEntry?.standardMeasurement?.primaryUnitLabel)
    }

    @Test
    fun `heuristic measurement resolution from product description when unit is generic`() {
        val records = listOf(
            PriceComparisonRecord(
                storeName = "Jumbo",
                unitPriceMinor = 8000L,
                timestamp = 1000L,
                quantityValue = 1L,
                quantityUnit = "UNIT"
            )
        )

        // Description contains "1000g"
        val summary = CrossStoreComparisonCalculator.calculate("Café Sello Rojo 1000g", records)
        assertNotNull(summary)

        val entry = summary?.storeEntries?.firstOrNull()
        assertNotNull(entry?.standardMeasurement)
        assertEquals(MeasurementDimension.WEIGHT, entry?.standardMeasurement?.dimension)
        assertEquals(Money(8000L, "COP"), entry?.standardMeasurement?.primaryPrice)
        assertEquals("kg", entry?.standardMeasurement?.primaryUnitLabel)
    }

    @Test
    fun `exact money arithmetic prevents floating point drift across multiple normalizations`() {
        // 333g at 3,333 COP
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(3333L, "COP"),
            quantity = ProductQuantity(333L, UnitType.GRAM)
        )

        // 3333 * 1000 = 3333000 / 333 = 10009.009... -> 10009 COP/kg exactly
        assertEquals(Money(10009L, "COP"), measurement.primaryPrice)
        assertEquals(Money(10L, "COP"), measurement.secondaryPrice)
        assertTrue(measurement.primaryPrice.currency == "COP")
    }

    @Test
    fun `boundary testing for large financial numbers and zero quantity fallback`() {
        val largePrice = Money(150_000_000L, "COP") // 150 million COP
        val measurement = StandardMeasurementNormalizer.normalize(
            price = largePrice,
            quantity = ProductQuantity(5000L, UnitType.GRAM) // 5kg
        )

        // 150,000,000 * 1000 / 5000 = 30,000,000 COP/kg
        assertEquals(Money(30_000_000L, "COP"), measurement.primaryPrice)
        assertEquals("kg", measurement.primaryUnitLabel)

        // Safe fallback for 0 quantity
        val zeroQtyMeasurement = StandardMeasurementNormalizer.normalize(
            price = Money(1000L, "COP"),
            rawQuantityValue = 0L,
            unit = UnitType.UNIT
        )
        assertEquals(Money(1000L, "COP"), zeroQtyMeasurement.primaryPrice)
    }
}

