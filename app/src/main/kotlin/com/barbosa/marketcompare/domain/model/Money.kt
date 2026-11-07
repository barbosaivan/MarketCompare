package com.barbosa.marketcompare.domain.model

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * Representación inmutable y matemáticamente exacta del dinero.
 * Prohíbe el uso de Double/Float para importes monetarios (ADR 0003).
 *
 * @param amountMinor Importe en unidades menores (para COP: centavos o pesos base enteros según escala fija).
 * @param currency Código de moneda ISO 4217 (por defecto COP).
 */
data class Money(
    val amountMinor: Long,
    val currency: String = "COP"
) : Comparable<Money> {

    operator fun plus(other: Money): Money {
        require(currency == other.currency) { "No se pueden sumar monedas distintas: $currency y ${other.currency}" }
        return Money(Math.addExact(amountMinor, other.amountMinor), currency)
    }

    operator fun minus(other: Money): Money {
        require(currency == other.currency) { "No se pueden restar monedas distintas: $currency y ${other.currency}" }
        return Money(Math.subtractExact(amountMinor, other.amountMinor), currency)
    }

    operator fun times(factor: Long): Money {
        return Money(Math.multiplyExact(amountMinor, factor), currency)
    }

    fun divide(divisor: Long, roundingMode: RoundingMode = RoundingMode.HALF_EVEN): Money {
        require(divisor != 0L) { "División monetaria por cero" }
        val bdAmount = amountMinor.toBigDecimal()
        val bdDivisor = divisor.toBigDecimal()
        val result = bdAmount.divide(bdDivisor, 0, roundingMode).toLong()
        return Money(result, currency)
    }

    fun formatDisplay(): String {
        val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"))
        format.maximumFractionDigits = 0
        return format.format(amountMinor)
    }

    override fun compareTo(other: Money): Int {
        require(currency == other.currency) { "No se pueden comparar monedas distintas: $currency y ${other.currency}" }
        return amountMinor.compareTo(other.amountMinor)
    }

    companion object {
        val ZERO = Money(0L, "COP")
        fun fromMajor(amountMajor: Long, currency: String = "COP"): Money = Money(amountMajor, currency)
    }
}

