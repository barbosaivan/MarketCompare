package com.barbosa.marketcompare.domain.normalizer

import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StandardMeasurementNormalizerTest {

    @Test
    fun `normalizes solid weight from grams to standard per kg and per g`() {
        // 500g costing 5,000 COP -> 10,000 COP/kg and 10 COP/g
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(5000L, "COP"),
            quantity = ProductQuantity(500L, UnitType.GRAM)
        )

        assertEquals(MeasurementDimension.WEIGHT, measurement.dimension)
        assertEquals(Money(10000L, "COP"), measurement.primaryPrice)
        assertEquals("kg", measurement.primaryUnitLabel)
        assertEquals(Money(10L, "COP"), measurement.secondaryPrice)
        assertEquals("g", measurement.secondaryUnitLabel)
    }

    @Test
    fun `normalizes solid weight from kilograms with small value`() {
        // 1kg costing 4,500 COP -> 4,500 COP/kg
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(4500L, "COP"),
            quantity = ProductQuantity(1L, UnitType.KILOGRAM)
        )

        assertEquals(MeasurementDimension.WEIGHT, measurement.dimension)
        assertEquals(Money(4500L, "COP"), measurement.primaryPrice)
        assertEquals("kg", measurement.primaryUnitLabel)
        assertEquals(Money(4L, "COP"), measurement.secondaryPrice) // 4500 / 1000 = 4.5 -> 4 with HALF_EVEN
        assertEquals("g", measurement.secondaryUnitLabel)
    }

    @Test
    fun `normalizes solid weight from kilograms with gram representation`() {
        // 1500 (representing 1.5kg) costing 6,000 COP -> 4,000 COP/kg
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(6000L, "COP"),
            quantity = ProductQuantity(1500L, UnitType.KILOGRAM)
        )

        assertEquals(MeasurementDimension.WEIGHT, measurement.dimension)
        assertEquals(Money(4000L, "COP"), measurement.primaryPrice)
        assertEquals("kg", measurement.primaryUnitLabel)
        assertEquals(Money(4L, "COP"), measurement.secondaryPrice)
        assertEquals("g", measurement.secondaryUnitLabel)
    }

    @Test
    fun `normalizes liquid volume from milliliters to per L and per ml`() {
        // 900ml costing 3,600 COP -> 4,000 COP/L and 4 COP/ml
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(3600L, "COP"),
            quantity = ProductQuantity(900L, UnitType.MILLILITER)
        )

        assertEquals(MeasurementDimension.LIQUID, measurement.dimension)
        assertEquals(Money(4000L, "COP"), measurement.primaryPrice)
        assertEquals("L", measurement.primaryUnitLabel)
        assertEquals(Money(4L, "COP"), measurement.secondaryPrice)
        assertEquals("ml", measurement.secondaryUnitLabel)
    }

    @Test
    fun `normalizes liquid volume from liters with gram-scale representation`() {
        // 1100 (representing 1.1L) costing 4,400 COP -> 4,000 COP/L
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(4400L, "COP"),
            quantity = ProductQuantity(1100L, UnitType.LITER)
        )

        assertEquals(MeasurementDimension.LIQUID, measurement.dimension)
        assertEquals(Money(4000L, "COP"), measurement.primaryPrice)
        assertEquals("L", measurement.primaryUnitLabel)
        assertEquals(Money(4L, "COP"), measurement.secondaryPrice)
        assertEquals("ml", measurement.secondaryUnitLabel)
    }

    @Test
    fun `normalizes liquid volume from single liter`() {
        // 1L costing 4,200 COP -> 4,200 COP/L
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(4200L, "COP"),
            quantity = ProductQuantity(1L, UnitType.LITER)
        )

        assertEquals(MeasurementDimension.LIQUID, measurement.dimension)
        assertEquals(Money(4200L, "COP"), measurement.primaryPrice)
        assertEquals("L", measurement.primaryUnitLabel)
    }

    @Test
    fun `normalizes discrete count units`() {
        // 6 units costing 4,800 COP -> 800 COP/und
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(4800L, "COP"),
            quantity = ProductQuantity(6L, UnitType.UNIT)
        )

        assertEquals(MeasurementDimension.COUNT, measurement.dimension)
        assertEquals(Money(800L, "COP"), measurement.primaryPrice)
        assertEquals("und", measurement.primaryUnitLabel)
        assertNull(measurement.secondaryPrice)
        assertNull(measurement.secondaryUnitLabel)
    }

    @Test
    fun `handles zero or negative quantity safely without division by zero`() {
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(3000L, "COP"),
            quantity = ProductQuantity(0L, UnitType.UNIT)
        )

        assertEquals(Money(3000L, "COP"), measurement.primaryPrice)
        assertEquals("und", measurement.primaryUnitLabel)
    }

    @Test
    fun `formats display strings correctly with Colombian locale`() {
        val measurement = StandardMeasurementNormalizer.normalize(
            price = Money(5000L, "COP"),
            quantity = ProductQuantity(500L, UnitType.GRAM)
        )

        assertEquals("${Money(10000L, "COP").formatDisplay()}/kg", measurement.formatPrimary())
        assertEquals("${Money(10L, "COP").formatDisplay()}/g", measurement.formatSecondary())
    }
}

