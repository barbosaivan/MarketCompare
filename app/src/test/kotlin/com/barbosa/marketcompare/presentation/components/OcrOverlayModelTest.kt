package com.barbosa.marketcompare.presentation.components

import com.barbosa.marketcompare.domain.model.BoundingBoxRect
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.presentation.theme.SemanticColorsLight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrOverlayModelTest {

    private val colors = SemanticColorsLight

    @Test
    fun `getColor maps all semantic categories to their distinct theme colors`() {
        assertEquals(colors.ocrStoreBlue, OcrCategoryColors.getColor(OcrSemanticCategory.STORE_HEADER, colors))
        assertEquals(colors.ocrProductGreen, OcrCategoryColors.getColor(OcrSemanticCategory.PRODUCT_NAME, colors))
        assertEquals(colors.ocrPriceGold, OcrCategoryColors.getColor(OcrSemanticCategory.PRICE, colors))
        assertEquals(colors.ocrQuantityPurple, OcrCategoryColors.getColor(OcrSemanticCategory.QUANTITY, colors))
        assertEquals(colors.ocrDiscountRed, OcrCategoryColors.getColor(OcrSemanticCategory.DISCOUNT, colors))
    }

    @Test
    fun `getLabelResId returns non-zero string resource for every category`() {
        OcrSemanticCategory.entries.forEach { category ->
            val resId = OcrCategoryColors.getLabelResId(category)
            assertTrue(resId != 0)
        }
    }

    @Test
    fun `filtering by category correctly selects only visible blocks`() {
        val blocks = listOf(
            OcrElementBox(id = "1", text = "EXITO", category = OcrSemanticCategory.STORE_HEADER, boundingBox = BoundingBoxRect(0.1f, 0.1f, 0.5f, 0.2f)),
            OcrElementBox(id = "2", text = "ARROZ", category = OcrSemanticCategory.PRODUCT_NAME, boundingBox = BoundingBoxRect(0.1f, 0.3f, 0.5f, 0.4f)),
            OcrElementBox(id = "3", text = "4500", category = OcrSemanticCategory.PRICE, boundingBox = BoundingBoxRect(0.6f, 0.3f, 0.9f, 0.4f)),
            OcrElementBox(id = "4", text = "-1000", category = OcrSemanticCategory.DISCOUNT, boundingBox = BoundingBoxRect(0.6f, 0.5f, 0.9f, 0.6f))
        )

        val visibleCategories = setOf(OcrSemanticCategory.PRODUCT_NAME, OcrSemanticCategory.PRICE)
        val filtered = blocks.filter { it.category in visibleCategories }

        assertEquals(2, filtered.size)
        assertTrue(filtered.any { it.id == "2" })
        assertTrue(filtered.any { it.id == "3" })
        assertFalse(filtered.any { it.id == "1" })
        assertFalse(filtered.any { it.id == "4" })
    }

    @Test
    fun `hit testing resolves touched block and ignores filtered-out categories`() {
        val storeBox = OcrElementBox(
            id = "store-1",
            text = "D1",
            category = OcrSemanticCategory.STORE_HEADER,
            boundingBox = BoundingBoxRect(0.1f, 0.1f, 0.4f, 0.2f)
        )
        val priceBox = OcrElementBox(
            id = "price-1",
            text = "3800",
            category = OcrSemanticCategory.PRICE,
            boundingBox = BoundingBoxRect(0.6f, 0.4f, 0.9f, 0.5f)
        )
        val blocks = listOf(storeBox, priceBox)

        // Point inside storeBox
        val hitStore = blocks.lastOrNull { it.boundingBox.containsNormalizedPoint(0.2f, 0.15f) }
        assertNotNull(hitStore)
        assertEquals("store-1", hitStore?.id)

        // Point outside all boxes
        val hitNone = blocks.lastOrNull { it.boundingBox.containsNormalizedPoint(0.05f, 0.05f) }
        assertNull(hitNone)

        // Point inside storeBox but STORE_HEADER is filtered out
        val visibleCategories = setOf(OcrSemanticCategory.PRICE)
        val hitFiltered = blocks.lastOrNull {
            it.category in visibleCategories && it.boundingBox.containsNormalizedPoint(0.2f, 0.15f)
        }
        assertNull(hitFiltered)

        // Point inside priceBox with PRICE visible
        val hitPrice = blocks.lastOrNull {
            it.category in visibleCategories && it.boundingBox.containsNormalizedPoint(0.7f, 0.45f)
        }
        assertNotNull(hitPrice)
        assertEquals("price-1", hitPrice?.id)
    }
}
