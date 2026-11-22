package com.barbosa.marketcompare.domain.normalizer

import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.UnitType

enum class MeasurementDimension {
    WEIGHT,
    LIQUID,
    COUNT
}

/**
 * Representa el precio normalizado a unidades de medida estándar internacional y local (MC-051).
 * Garantiza equidad en la comparación de precios entre diferentes tamaños y presentaciones de empaque.
 */
data class StandardPriceMeasurement(
    val dimension: MeasurementDimension,
    val primaryPrice: Money,
    val primaryUnitLabel: String,
    val secondaryPrice: Money? = null,
    val secondaryUnitLabel: String? = null
) {
    fun formatPrimary(): String = "${primaryPrice.formatDisplay()}/$primaryUnitLabel"
    fun formatSecondary(): String? = secondaryPrice?.let { "${it.formatDisplay()}/$secondaryUnitLabel" }
}

/**
 * Motor puro de normalización de medidas y precios estándar (MC-051).
 * Aplica aritmética monetaria exacta (ADR 0003) sin usar tipos de punto flotante para importes.
 */
object StandardMeasurementNormalizer {

    fun normalize(price: Money, quantity: ProductQuantity): StandardPriceMeasurement {
        return normalize(price, quantity.value, quantity.unit)
    }

    fun normalize(price: Money, rawQuantityValue: Long, unit: UnitType): StandardPriceMeasurement {
        return when (unit) {
            UnitType.GRAM, UnitType.KILOGRAM -> normalizeWeight(price, rawQuantityValue, unit)
            UnitType.MILLILITER, UnitType.LITER -> normalizeLiquid(price, rawQuantityValue, unit)
            UnitType.UNIT -> normalizeCount(price, rawQuantityValue)
        }
    }

    private fun normalizeWeight(price: Money, rawQuantityValue: Long, unit: UnitType): StandardPriceMeasurement {
        val totalGrams = resolveTotalGrams(rawQuantityValue, unit)
        val pricePerKg = price.times(1000L).divide(totalGrams)
        val pricePerGram = price.divide(totalGrams)

        return StandardPriceMeasurement(
            dimension = MeasurementDimension.WEIGHT,
            primaryPrice = pricePerKg,
            primaryUnitLabel = "kg",
            secondaryPrice = pricePerGram,
            secondaryUnitLabel = "g"
        )
    }

    private fun normalizeLiquid(price: Money, rawQuantityValue: Long, unit: UnitType): StandardPriceMeasurement {
        val totalMl = resolveTotalMl(rawQuantityValue, unit)
        val pricePerLiter = price.times(1000L).divide(totalMl)
        val pricePerMl = price.divide(totalMl)

        return StandardPriceMeasurement(
            dimension = MeasurementDimension.LIQUID,
            primaryPrice = pricePerLiter,
            primaryUnitLabel = "L",
            secondaryPrice = pricePerMl,
            secondaryUnitLabel = "ml"
        )
    }

    private fun normalizeCount(price: Money, rawQuantityValue: Long): StandardPriceMeasurement {
        val count = maxOf(1L, rawQuantityValue)
        val pricePerUnit = price.divide(count)

        return StandardPriceMeasurement(
            dimension = MeasurementDimension.COUNT,
            primaryPrice = pricePerUnit,
            primaryUnitLabel = "und",
            secondaryPrice = null,
            secondaryUnitLabel = null
        )
    }

    private fun resolveTotalGrams(rawQuantityValue: Long, unit: UnitType): Long {
        val safeVal = maxOf(1L, rawQuantityValue)
        return when (unit) {
            UnitType.GRAM -> safeVal
            UnitType.KILOGRAM -> if (safeVal >= 100L) safeVal else safeVal * 1000L
            else -> safeVal
        }
    }

    private fun resolveTotalMl(rawQuantityValue: Long, unit: UnitType): Long {
        val safeVal = maxOf(1L, rawQuantityValue)
        return when (unit) {
            UnitType.MILLILITER -> safeVal
            UnitType.LITER -> if (safeVal >= 100L) safeVal else safeVal * 1000L
            else -> safeVal
        }
    }
}

