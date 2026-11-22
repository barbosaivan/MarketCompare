package com.barbosa.marketcompare.domain.comparison

import com.barbosa.marketcompare.domain.basket.BasketEstimator
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.StorePriceEntry
import com.barbosa.marketcompare.domain.model.UnitType
import com.barbosa.marketcompare.domain.normalizer.ProductNormalizer
import com.barbosa.marketcompare.domain.normalizer.StandardMeasurementNormalizer

data class PriceComparisonRecord(
    val storeName: String,
    val unitPriceMinor: Long,
    val timestamp: Long,
    val quantityValue: Long = 1L,
    val quantityUnit: String = "UNIT"
)

object CrossStoreComparisonCalculator {

    fun calculate(
        productName: String,
        records: List<PriceComparisonRecord>
    ): ProductComparisonSummary? {
        val trimmed = productName.trim()
        if (trimmed.isEmpty() || records.isEmpty()) return null

        // Agrupar por tienda y conservar la observación más reciente de cada una
        val storeLatest = records.groupBy { it.storeName }
            .mapValues { (_, items) -> items.maxByOrNull { it.timestamp }!! }
            .values
            .sortedBy { it.unitPriceMinor }

        if (storeLatest.isEmpty()) return null

        val lowest = storeLatest.first()
        val highest = storeLatest.last()
        val lowestPrice = Money(lowest.unitPriceMinor, "COP")
        val highestPrice = Money(highest.unitPriceMinor, "COP")
        val maxSavings = highestPrice - lowestPrice
        val maxSavingsPercentage = if (highest.unitPriceMinor > 0) {
            ((highest.unitPriceMinor - lowest.unitPriceMinor).toDouble() / highest.unitPriceMinor.toDouble()) * 100.0
        } else 0.0

        val entries = storeLatest.map { item ->
            val diffMinor = item.unitPriceMinor - lowest.unitPriceMinor
            val diffPercentage = if (item.unitPriceMinor > 0 && lowest.unitPriceMinor > 0) {
                ((item.unitPriceMinor - lowest.unitPriceMinor).toDouble() / item.unitPriceMinor.toDouble()) * 100.0
            } else 0.0

            val unitType = runCatching { UnitType.valueOf(item.quantityUnit) }.getOrDefault(UnitType.UNIT)
            val measurement = resolveMeasurement(trimmed, item.unitPriceMinor, item.quantityValue, unitType)

            StorePriceEntry(
                storeName = item.storeName,
                unitPrice = Money(item.unitPriceMinor, "COP"),
                observedAtTimestamp = item.timestamp,
                freshness = BasketEstimator.evaluateFreshness(item.timestamp),
                priceDifferenceWithLowest = Money(diffMinor, "COP"),
                percentageDifferenceWithLowest = diffPercentage,
                isLowestPrice = item.unitPriceMinor == lowest.unitPriceMinor,
                standardMeasurement = measurement
            )
        }

        return ProductComparisonSummary(
            productName = trimmed,
            lowestPrice = lowestPrice,
            highestPrice = highestPrice,
            maxSavings = maxSavings,
            maxSavingsPercentage = maxSavingsPercentage,
            cheapestStore = lowest.storeName,
            storeEntries = entries
        )
    }

    private fun resolveMeasurement(
        productName: String,
        priceMinor: Long,
        qtyValue: Long,
        unitType: UnitType
    ) = if (unitType != UnitType.UNIT) {
        StandardMeasurementNormalizer.normalize(
            price = Money(priceMinor, "COP"),
            rawQuantityValue = qtyValue,
            unit = unitType
        )
    } else {
        val candidate = ProductNormalizer.normalize(productName)
        if (candidate.unit != UnitType.UNIT) {
            StandardMeasurementNormalizer.normalize(
                price = Money(priceMinor, "COP"),
                rawQuantityValue = candidate.baseQuantity,
                unit = candidate.unit
            )
        } else {
            StandardMeasurementNormalizer.normalize(
                price = Money(priceMinor, "COP"),
                rawQuantityValue = qtyValue,
                unit = UnitType.UNIT
            )
        }
    }
}
