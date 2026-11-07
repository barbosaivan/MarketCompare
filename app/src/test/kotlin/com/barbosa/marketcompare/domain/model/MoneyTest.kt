package com.barbosa.marketcompare.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.RoundingMode

class MoneyTest {

    @Test
    fun `addition of two money instances returns exact sum`() {
        val m1 = Money(15000L, "COP")
        val m2 = Money(32500L, "COP")
        val result = m1 + m2
        assertEquals(47500L, result.amountMinor)
        assertEquals("COP", result.currency)
    }

    @Test
    fun `subtraction of money instances returns exact difference`() {
        val m1 = Money(50000L, "COP")
        val m2 = Money(12500L, "COP")
        val result = m1 - m2
        assertEquals(37500L, result.amountMinor)
    }

    @Test
    fun `multiplication by integer returns exact total`() {
        val unitPrice = Money(4500L, "COP")
        val total = unitPrice * 3L
        assertEquals(13500L, total.amountMinor)
    }

    @Test
    fun `division with bank rounding divides exactly without double precision errors`() {
        val total = Money(10000L, "COP")
        val divided = total.divide(3L, RoundingMode.HALF_EVEN)
        assertEquals(3333L, divided.amountMinor)
    }

    @Test
    fun `comparison between money amounts works as expected`() {
        val low = Money(1000L, "COP")
        val high = Money(5000L, "COP")
        assertTrue(low < high)
    }
}

