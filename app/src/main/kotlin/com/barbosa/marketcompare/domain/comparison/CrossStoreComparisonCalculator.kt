package com.barbosa.marketcompare.domain.comparison

import com.barbosa.marketcompare.domain.basket.BasketEstimator
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.StorePriceEntry

data class PriceComparisonRecord(
    val storeName: String,
    val unitPriceMinor: Long,
    val timestamp: Long
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

            StorePriceEntry(
                storeName = item.storeName,
                unitPrice = Money(item.unitPriceMinor, "COP"),
                observedAtTimestamp = item.timestamp,
                freshness = BasketEstimator.evaluateFreshness(item.timestamp),
                priceDifferenceWithLowest = Money(diffMinor, "COP"),
                percentageDifferenceWithLowest = diffPercentage,
                isLowestPrice = item.unitPriceMinor == lowest.unitPriceMinor
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
}

