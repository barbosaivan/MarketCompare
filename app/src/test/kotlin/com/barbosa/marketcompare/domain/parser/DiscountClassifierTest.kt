package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DiscountClassifierTest {

    @Test
    fun `classify identifies loyalty member discounts with evidence`() {
        assertEquals(DiscountType.LOYALTY_MEMBER, DiscountClassifier.classify("DCTO PUNTOS COLOMBIA -5.000"))
        assertEquals(DiscountType.LOYALTY_MEMBER, DiscountClassifier.classify("REBAJA CLIENTE FIEL -2.000"))
        assertEquals(DiscountType.LOYALTY_MEMBER, DiscountClassifier.classify("TARJETA EXITO -3.500"))
        assertEquals(DiscountType.LOYALTY_MEMBER, DiscountClassifier.classify("CLUB AFILIADOS -1.500"))
    }

    @Test
    fun `classify identifies multi-item promotions and percentage discounts`() {
        assertEquals(DiscountType.PROMOTION_2X1, DiscountClassifier.classify("PROMO 2X1 LLEVE 2 -4.000"))
        assertEquals(DiscountType.PROMOTION_2X1, DiscountClassifier.classify("3X2 ESPECIAL -6.000"))
        assertEquals(DiscountType.PROMOTION_2X1, DiscountClassifier.classify("2DO AL 50% DCTO"))
        assertEquals(DiscountType.PROMOTION_2X1, DiscountClassifier.classify("SEGUNDA UNIDAD -3.000"))
    }

    @Test
    fun `classify identifies standard explicit line discounts`() {
        assertEquals(DiscountType.EXPLICIT_LINE, DiscountClassifier.classify("DESCUENTO DIRECTO -2.000"))
        assertEquals(DiscountType.EXPLICIT_LINE, DiscountClassifier.classify("DCTO TEMPORADA -1.000"))
        assertEquals(DiscountType.EXPLICIT_LINE, DiscountClassifier.classify("AHORRO ESPECIAL -1.500"))
    }

    @Test
    fun `classify marks unknown discounts when evidence is generic or numeric only`() {
        assertEquals(DiscountType.UNKNOWN, DiscountClassifier.classify("-2.500"))
        assertEquals(DiscountType.UNKNOWN, DiscountClassifier.classify("- 4.000 VAL 992"))
    }

    @Test
    fun `inferDerivedDiscount calculates positive difference`() {
        val listPrice = Money(10000L, "COP")
        val finalPrice = Money(8000L, "COP")

        val derived = DiscountClassifier.inferDerivedDiscount(listPrice, finalPrice)
        assertEquals(Money(2000L, "COP"), derived)

        val samePrice = DiscountClassifier.inferDerivedDiscount(finalPrice, finalPrice)
        assertNull(samePrice)

        val nullPrice = DiscountClassifier.inferDerivedDiscount(null, finalPrice)
        assertNull(nullPrice)
    }
}
