package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.domain.model.BoundingBoxRect
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrFieldAssignmentHelperTest {

    @Test
    fun parsePrice_variousFormats_correctMoney() {
        assertEquals(Money(12500L, "COP"), OcrFieldAssignmentHelper.parsePrice("$12.500"))
        assertEquals(Money(12500L, "COP"), OcrFieldAssignmentHelper.parsePrice("12500 COP"))
        assertEquals(Money(4900L, "COP"), OcrFieldAssignmentHelper.parsePrice("4,900"))
        assertEquals(Money(1250000L, "COP"), OcrFieldAssignmentHelper.parsePrice("$ 1.250.000"))
        assertNull(OcrFieldAssignmentHelper.parsePrice("NO_PRICE_HERE"))
    }

    @Test
    fun parseQuantity_unitsAndMeasures_correctProductQuantity() {
        val q1 = OcrFieldAssignmentHelper.parseQuantity("2")
        assertNotNull(q1)
        assertEquals(2L, q1?.value)
        assertEquals(UnitType.UNIT, q1?.unit)

        val q2 = OcrFieldAssignmentHelper.parseQuantity("500g")
        assertNotNull(q2)
        assertEquals(500L, q2?.value)
        assertEquals(UnitType.GRAM, q2?.unit)

        val q3 = OcrFieldAssignmentHelper.parseQuantity("1.5 kg")
        assertNotNull(q3)
        assertEquals(1500L, q3?.value)
        assertEquals(UnitType.KILOGRAM, q3?.unit)

        val q4 = OcrFieldAssignmentHelper.parseQuantity("1000ml")
        assertNotNull(q4)
        assertEquals(1000L, q4?.value)
        assertEquals(UnitType.MILLILITER, q4?.unit)

        val q5 = OcrFieldAssignmentHelper.parseQuantity("1 L")
        assertNotNull(q5)
        assertEquals(1000L, q5?.value)
        assertEquals(UnitType.LITER, q5?.unit)
    }

    @Test
    fun assignStoreName_stripsBoilerplate() {
        val result = OcrFieldAssignmentHelper.assignStoreName("BIENVENIDOS A ÉXITO EXPRESS -")
        assertEquals("ÉXITO EXPRESS", result)
    }

    @Test
    fun assignItemDescription_updatesRawDescription() {
        val item = createSampleItem()
        val updated = OcrFieldAssignmentHelper.assignItemDescription(item, "LECHE DESLACTOSADA 1.1L")
        assertEquals("LECHE DESLACTOSADA 1.1L", updated.rawDescription)
    }

    @Test
    fun assignItemPrice_withoutDiscount_updatesListAndFinalPrice() {
        val item = createSampleItem()
        val updated = OcrFieldAssignmentHelper.assignItemPrice(item, "$ 7.500")
        assertEquals(Money(7500L, "COP"), updated.unitListPrice)
        assertEquals(Money(7500L, "COP"), updated.finalPrice)
    }

    @Test
    fun assignItemPrice_withExistingDiscount_adjustsFinalPrice() {
        val item = createSampleItem().copy(discount = Money(1000L, "COP"))
        val updated = OcrFieldAssignmentHelper.assignItemPrice(item, "10.000")
        assertEquals(Money(10000L, "COP"), updated.unitListPrice)
        assertEquals(Money(9000L, "COP"), updated.finalPrice)
    }

    @Test
    fun assignItemQuantity_updatesQuantity() {
        val item = createSampleItem()
        val updated = OcrFieldAssignmentHelper.assignItemQuantity(item, "3 und")
        assertEquals(3L, updated.quantity.value)
        assertEquals(UnitType.UNIT, updated.quantity.unit)
    }

    @Test
    fun assignItemDiscount_updatesDiscountAndFinalPrice() {
        val item = createSampleItem().copy(
            unitListPrice = Money(10000L, "COP"),
            finalPrice = Money(10000L, "COP")
        )
        val updated = OcrFieldAssignmentHelper.assignItemDiscount(item, "$ 2.000")
        assertEquals(Money(2000L, "COP"), updated.discount)
        assertEquals(Money(8000L, "COP"), updated.finalPrice)
        assertEquals(DiscountType.EXPLICIT_LINE, updated.discountType)
        assertEquals("$ 2.000", updated.discountEvidence)
    }

    @Test
    fun createNewItem_fromPriceBox_setsPrice() {
        val box = OcrElementBox(
            text = "$ 15.000",
            category = OcrSemanticCategory.PRICE,
            boundingBox = BoundingBoxRect(0.1f, 0.1f, 0.4f, 0.2f)
        )
        val item = OcrFieldAssignmentHelper.createNewItem(box)
        assertEquals(Money(15000L, "COP"), item.finalPrice)
        assertTrue(item.isIncluded)
    }

    @Test
    fun createNewItem_fromProductBox_setsDescription() {
        val box = OcrElementBox(
            text = "ARROZ FLORHUILA 1000G",
            category = OcrSemanticCategory.PRODUCT_NAME,
            boundingBox = BoundingBoxRect(0.1f, 0.1f, 0.4f, 0.2f)
        )
        val item = OcrFieldAssignmentHelper.createNewItem(box)
        assertEquals("ARROZ FLORHUILA 1000G", item.rawDescription)
        assertEquals(Money.ZERO, item.finalPrice)
    }

    private fun createSampleItem(): ReceiptItemDraft {
        return ReceiptItemDraft(
            rawDescription = "PRODUCTO TEST",
            quantity = ProductQuantity(1L, UnitType.UNIT),
            unitListPrice = Money(5000L, "COP"),
            discount = null,
            finalPrice = Money(5000L, "COP")
        )
    }
}
