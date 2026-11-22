package com.barbosa.marketcompare.domain.ocr

import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.RawOcrElement
import com.barbosa.marketcompare.domain.model.RawOcrLine
import com.barbosa.marketcompare.domain.model.RawOcrPayload
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.model.UnitType
import com.barbosa.marketcompare.domain.parser.OcrSemanticClassifier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OcrSemanticClassifierTest {

    @Test
    fun `store header line categorizes all tokens as STORE_HEADER`() {
        val payload = RawOcrPayload(
            fullText = "ALMACENES EXITO S.A.\nNIT 890.900.608-9",
            imageWidth = 1000,
            imageHeight = 2000,
            lines = listOf(
                RawOcrLine(
                    text = "ALMACENES EXITO S.A.",
                    left = 100, top = 50, right = 900, bottom = 100,
                    elements = listOf(
                        RawOcrElement("ALMACENES", 100, 50, 400, 100),
                        RawOcrElement("EXITO", 420, 50, 700, 100),
                        RawOcrElement("S.A.", 720, 50, 900, 100)
                    )
                ),
                RawOcrLine(
                    text = "NIT 890.900.608-9",
                    left = 200, top = 110, right = 800, bottom = 150,
                    elements = listOf(
                        RawOcrElement("NIT", 200, 110, 350, 150),
                        RawOcrElement("890.900.608-9", 370, 110, 800, 150)
                    )
                )
            )
        )

        val boxes = OcrSemanticClassifier.classifyPayload(payload)

        assertEquals(5, boxes.size)
        assertTrue(boxes.all { it.category == OcrSemanticCategory.STORE_HEADER })
        assertEquals(0.1f, boxes[0].boundingBox.left, 0.001f)
        assertEquals(0.025f, boxes[0].boundingBox.top, 0.001f)
    }

    @Test
    fun `item line separates product name, quantity and price with associated item id`() {
        val itemDraft = ReceiptItemDraft(
            id = "item-arroz-1",
            rawDescription = "ARROZ FLOR HUILA",
            quantity = ProductQuantity(1L, UnitType.KILOGRAM),
            unitListPrice = Money(8000L, "COP"),
            discount = null,
            finalPrice = Money(8000L, "COP")
        )

        val payload = RawOcrPayload(
            fullText = "ARROZ FLOR HUILA 1KG 8.000",
            imageWidth = 1000,
            imageHeight = 2000,
            lines = listOf(
                RawOcrLine(
                    text = "ARROZ FLOR HUILA 1KG 8.000",
                    left = 50, top = 500, right = 950, bottom = 550,
                    elements = listOf(
                        RawOcrElement("ARROZ", 50, 500, 200, 550),
                        RawOcrElement("FLOR", 220, 500, 350, 550),
                        RawOcrElement("HUILA", 370, 500, 500, 550),
                        RawOcrElement("1KG", 520, 500, 650, 550),
                        RawOcrElement("8.000", 750, 500, 950, 550)
                    )
                )
            )
        )

        val draft = ReceiptDraft(
            detectedStoreName = "Éxito",
            detectedDateTimestamp = 1700000000000L,
            detectedTaxId = "890900608",
            detectedTotal = Money(8000L, "COP"),
            rawText = payload.fullText,
            items = listOf(itemDraft)
        )

        val boxes = OcrSemanticClassifier.classifyPayload(payload, draft)

        assertEquals(5, boxes.size)
        assertEquals(OcrSemanticCategory.PRODUCT_NAME, boxes[0].category)
        assertEquals(OcrSemanticCategory.PRODUCT_NAME, boxes[1].category)
        assertEquals(OcrSemanticCategory.PRODUCT_NAME, boxes[2].category)
        assertEquals(OcrSemanticCategory.QUANTITY, boxes[3].category)
        assertEquals(OcrSemanticCategory.PRICE, boxes[4].category)

        assertTrue(boxes.all { it.associatedItemId == "item-arroz-1" })
    }

    @Test
    fun `multiplier quantity tokens are categorized as QUANTITY and unit prices as PRICE`() {
        val payload = RawOcrPayload(
            fullText = "PAN TAJADO 2 X 3.200 6.400",
            imageWidth = 1000,
            imageHeight = 2000,
            lines = listOf(
                RawOcrLine(
                    text = "PAN TAJADO 2 X 3.200 6.400",
                    left = 50, top = 600, right = 950, bottom = 650,
                    elements = listOf(
                        RawOcrElement("PAN", 50, 600, 150, 650),
                        RawOcrElement("TAJADO", 170, 600, 320, 650),
                        RawOcrElement("2", 340, 600, 380, 650),
                        RawOcrElement("X", 400, 600, 440, 650),
                        RawOcrElement("3.200", 500, 600, 650, 650),
                        RawOcrElement("6.400", 750, 600, 950, 650)
                    )
                )
            )
        )

        val boxes = OcrSemanticClassifier.classifyPayload(payload)

        assertEquals(6, boxes.size)
        assertEquals(OcrSemanticCategory.PRODUCT_NAME, boxes[0].category)
        assertEquals(OcrSemanticCategory.PRODUCT_NAME, boxes[1].category)
        assertEquals(OcrSemanticCategory.QUANTITY, boxes[3].category) // 'X'
        assertEquals(OcrSemanticCategory.PRICE, boxes[4].category) // 3.200
        assertEquals(OcrSemanticCategory.PRICE, boxes[5].category) // 6.400
    }

    @Test
    fun `discount line categorizes all elements as DISCOUNT`() {
        val payload = RawOcrPayload(
            fullText = "DCTO PUNTOS COLOMBIA -2.000",
            imageWidth = 1000,
            imageHeight = 2000,
            lines = listOf(
                RawOcrLine(
                    text = "DCTO PUNTOS COLOMBIA -2.000",
                    left = 80, top = 700, right = 920, bottom = 740,
                    elements = listOf(
                        RawOcrElement("DCTO", 80, 700, 200, 740),
                        RawOcrElement("PUNTOS", 220, 700, 400, 740),
                        RawOcrElement("COLOMBIA", 420, 700, 650, 740),
                        RawOcrElement("-2.000", 750, 700, 920, 740)
                    )
                )
            )
        )

        val boxes = OcrSemanticClassifier.classifyPayload(payload)

        assertEquals(4, boxes.size)
        assertTrue(boxes.all { it.category == OcrSemanticCategory.DISCOUNT })
    }

    @Test
    fun `total line separates label and monetary amount`() {
        val payload = RawOcrPayload(
            fullText = "TOTAL A PAGAR 45.000",
            imageWidth = 1000,
            imageHeight = 2000,
            lines = listOf(
                RawOcrLine(
                    text = "TOTAL A PAGAR 45.000",
                    left = 100, top = 1200, right = 900, bottom = 1260,
                    elements = listOf(
                        RawOcrElement("TOTAL", 100, 1200, 280, 1260),
                        RawOcrElement("A", 300, 1200, 340, 1260),
                        RawOcrElement("PAGAR", 360, 1200, 520, 1260),
                        RawOcrElement("45.000", 650, 1200, 900, 1260)
                    )
                )
            )
        )

        val boxes = OcrSemanticClassifier.classifyPayload(payload)

        assertEquals(4, boxes.size)
        assertEquals(OcrSemanticCategory.STORE_HEADER, boxes[0].category)
        assertEquals(OcrSemanticCategory.STORE_HEADER, boxes[1].category)
        assertEquals(OcrSemanticCategory.STORE_HEADER, boxes[2].category)
        assertEquals(OcrSemanticCategory.PRICE, boxes[3].category)
    }

    @Test
    fun `enrichDraft sets ocrBlocks and image dimensions on receipt draft`() {
        val payload = RawOcrPayload(
            fullText = "D1 SAS\nLECHE ENTERA 1L 3.800",
            imageWidth = 800,
            imageHeight = 1600,
            lines = listOf(
                RawOcrLine("D1 SAS", 50, 20, 300, 60),
                RawOcrLine("LECHE ENTERA 1L 3.800", 50, 100, 750, 140)
            )
        )

        val initialDraft = ReceiptDraft(
            detectedStoreName = "D1",
            detectedDateTimestamp = 1700000000000L,
            detectedTaxId = null,
            detectedTotal = Money(3800L, "COP"),
            rawText = payload.fullText,
            items = emptyList()
        )

        val enriched = OcrSemanticClassifier.enrichDraft(initialDraft, payload)

        assertEquals(800, enriched.imageWidth)
        assertEquals(1600, enriched.imageHeight)
        assertEquals(2, enriched.ocrBlocks.size)
        assertEquals(OcrSemanticCategory.STORE_HEADER, enriched.ocrBlocks[0].category)
    }

    @Test
    fun `boundingBox geometry methods perform accurate hit testing and size calculation`() {
        val box = com.barbosa.marketcompare.domain.model.BoundingBoxRect(
            left = 0.1f,
            top = 0.2f,
            right = 0.6f,
            bottom = 0.4f,
            pixelLeft = 100,
            pixelTop = 200,
            pixelRight = 600,
            pixelBottom = 400
        )

        assertEquals(0.5f, box.width, 0.001f)
        assertEquals(0.2f, box.height, 0.001f)
        assertTrue(box.containsNormalizedPoint(0.3f, 0.3f))
        assertFalse(box.containsNormalizedPoint(0.05f, 0.3f))
        assertFalse(box.containsNormalizedPoint(0.3f, 0.5f))
    }
}
