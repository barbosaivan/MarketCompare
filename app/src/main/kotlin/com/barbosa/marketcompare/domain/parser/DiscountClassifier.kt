package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import java.util.Locale

object DiscountClassifier {

    /**
     * Clasifica la procedencia del descuento a partir de la evidencia textual extraída por el OCR.
     */
    fun classify(evidence: String): DiscountType {
        val upper = evidence.uppercase(Locale.ROOT)
        return when {
            upper.contains("PUNTOS") || upper.contains("CLIENTE") ||
                    upper.contains("TARJETA") || upper.contains("CLUB") ||
                    upper.contains("FIDELIDAD") || upper.contains("AFILIADO") -> DiscountType.LOYALTY_MEMBER

            upper.contains("2X1") || upper.contains("3X2") || upper.contains("2DO") ||
                    upper.contains("SEGUNDA UNIDAD") || upper.contains("PROMO") ||
                    upper.contains("%") -> DiscountType.PROMOTION_2X1

            upper.contains("DCTO") || upper.contains("DESCUENTO") ||
                    upper.contains("AHORRO") || upper.contains("REBAJA") -> DiscountType.EXPLICIT_LINE

            evidence.contains("-") -> DiscountType.UNKNOWN
            else -> DiscountType.UNKNOWN
        }
    }

    /**
     * Calcula un descuento derivado si el precio de lista supera al precio cobrado.
     */
    fun inferDerivedDiscount(listPrice: Money?, finalPrice: Money): Money? {
        if (listPrice == null) return null
        return if (listPrice.amountMinor > finalPrice.amountMinor) {
            listPrice - finalPrice
        } else null
    }

    /**
     * Retorna el identificador del recurso de texto asociado al tipo de descuento.
     */
    fun getLabelResId(type: DiscountType): Int {
        return when (type) {
            DiscountType.NONE -> R.string.discount_type_none
            DiscountType.EXPLICIT_LINE -> R.string.discount_type_explicit
            DiscountType.LOYALTY_MEMBER -> R.string.discount_type_loyalty
            DiscountType.PROMOTION_2X1 -> R.string.discount_type_promotion
            DiscountType.DERIVED -> R.string.discount_type_derived
            DiscountType.UNKNOWN -> R.string.discount_type_unknown
        }
    }
}

