package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.domain.model.BoundingBoxRect
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.domain.model.RawOcrElement
import com.barbosa.marketcompare.domain.model.RawOcrLine
import com.barbosa.marketcompare.domain.model.RawOcrPayload
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import java.util.Locale
import java.util.regex.Pattern

/**
 * Clasificador semántico de elementos OCR.
 * Asigna a cada caja delimitadora una categoría según Gate 10:
 * - STORE_HEADER (Azul)
 * - PRODUCT_NAME (Verde)
 * - PRICE (Dorado)
 * - QUANTITY (Morado)
 * - DISCOUNT (Rojo)
 * - OTHER (Neutro)
 */
object OcrSemanticClassifier {

    private val STORE_KEYWORDS = listOf(
        "EXITO", "CARULLA", "OLIMPICA", "D1", "ARA", "JUMBO", "METRO",
        "SUPER INTER", "SURTIMAX", "MAKRO", "PRICE SMART", "PRICESMART",
        "EURO", "ISIMO", "NIT", "N.I.T.", "RUT", "CUFE", "DIAN", "FACTURA",
        "TIQUETE", "REGIMEN", "RESOLUCION", "AUTORIZACION", "CARRERA",
        "CALLE", "AVENIDA", "DIRECCION", "TEL:", "CEL:", "CAJA", "CAJERO"
    )

    private val DISCOUNT_KEYWORDS = listOf(
        "DCTO", "DESCUENTO", "AHORRO", "PROMO", "REBAJA"
    )

    private val TOTAL_KEYWORDS = listOf(
        "TOTAL", "TOTAL A PAGAR", "TOTAL PAGAR", "VALOR TOTAL", "SUBTOTAL"
    )

    private val PRICE_PATTERN = Pattern.compile("""(?:\$|\b)(\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{2})?|\d{3,7})(?:\b|$)""")
    private val QUANTITY_PATTERN = Pattern.compile("""^(?:\d+(?:[.,]\d+)?\s*(?:KG|G|GR|L|ML|UND|UNIDADES|C\.C\.)|(?:KG|G|GR|L|ML|UND|UNIDADES)|(?:\d+\s*[Xx*])|[Xx*])$""", Pattern.CASE_INSENSITIVE)

    fun enrichDraft(draft: ReceiptDraft, payload: RawOcrPayload): ReceiptDraft {
        val classifiedBoxes = classifyPayload(payload, draft)
        return draft.copy(
            ocrBlocks = classifiedBoxes,
            imageWidth = payload.imageWidth,
            imageHeight = payload.imageHeight
        )
    }

    fun classifyPayload(payload: RawOcrPayload, draft: ReceiptDraft? = null): List<OcrElementBox> {
        val results = mutableListOf<OcrElementBox>()
        val width = payload.imageWidth
        val height = payload.imageHeight

        payload.lines.forEachIndexed { lineIdx, line ->
            val matchedItem = findMatchingItem(line.text, draft?.items)
            val lineBoxes = classifyLine(line, lineIdx, width, height, matchedItem)
            results.addAll(lineBoxes)
        }

        return results
    }

    private fun classifyLine(
        line: RawOcrLine,
        lineIdx: Int,
        imageWidth: Int,
        imageHeight: Int,
        matchedItem: ReceiptItemDraft?
    ): List<OcrElementBox> {
        val upperText = line.text.uppercase(Locale.ROOT)

        return when {
            isDiscountLine(upperText) -> classifyUniformLine(line, lineIdx, imageWidth, imageHeight, OcrSemanticCategory.DISCOUNT, matchedItem)
            isStoreHeaderLine(upperText, lineIdx) -> classifyUniformLine(line, lineIdx, imageWidth, imageHeight, OcrSemanticCategory.STORE_HEADER, null)
            isTotalLine(upperText) -> classifyTotalLine(line, lineIdx, imageWidth, imageHeight)
            else -> classifyItemOrGeneralLine(line, lineIdx, imageWidth, imageHeight, matchedItem)
        }
    }

    private fun classifyUniformLine(
        line: RawOcrLine,
        lineIdx: Int,
        imageWidth: Int,
        imageHeight: Int,
        category: OcrSemanticCategory,
        matchedItem: ReceiptItemDraft?
    ): List<OcrElementBox> {
        if (line.elements.isEmpty()) {
            return listOf(
                OcrElementBox(
                    text = line.text,
                    category = category,
                    boundingBox = toBoundingBoxRect(line.left, line.top, line.right, line.bottom, imageWidth, imageHeight),
                    confidence = line.confidence,
                    associatedItemId = matchedItem?.id,
                    lineNumber = lineIdx
                )
            )
        }

        return line.elements.map { elem ->
            OcrElementBox(
                text = elem.text,
                category = category,
                boundingBox = toBoundingBoxRect(elem.left, elem.top, elem.right, elem.bottom, imageWidth, imageHeight),
                confidence = elem.confidence,
                associatedItemId = matchedItem?.id,
                lineNumber = lineIdx
            )
        }
    }

    private fun classifyTotalLine(
        line: RawOcrLine,
        lineIdx: Int,
        imageWidth: Int,
        imageHeight: Int
    ): List<OcrElementBox> {
        if (line.elements.isEmpty()) {
            return listOf(
                OcrElementBox(
                    text = line.text,
                    category = OcrSemanticCategory.PRICE,
                    boundingBox = toBoundingBoxRect(line.left, line.top, line.right, line.bottom, imageWidth, imageHeight),
                    confidence = line.confidence,
                    lineNumber = lineIdx
                )
            )
        }

        return line.elements.map { elem ->
            val cat = if (isPriceToken(elem.text)) OcrSemanticCategory.PRICE else OcrSemanticCategory.STORE_HEADER
            OcrElementBox(
                text = elem.text,
                category = cat,
                boundingBox = toBoundingBoxRect(elem.left, elem.top, elem.right, elem.bottom, imageWidth, imageHeight),
                confidence = elem.confidence,
                lineNumber = lineIdx
            )
        }
    }

    private fun classifyItemOrGeneralLine(
        line: RawOcrLine,
        lineIdx: Int,
        imageWidth: Int,
        imageHeight: Int,
        matchedItem: ReceiptItemDraft?
    ): List<OcrElementBox> {
        if (line.elements.isEmpty()) {
            val cat = if (matchedItem != null) OcrSemanticCategory.PRODUCT_NAME else OcrSemanticCategory.OTHER
            return listOf(
                OcrElementBox(
                    text = line.text,
                    category = cat,
                    boundingBox = toBoundingBoxRect(line.left, line.top, line.right, line.bottom, imageWidth, imageHeight),
                    confidence = line.confidence,
                    associatedItemId = matchedItem?.id,
                    lineNumber = lineIdx
                )
            )
        }

        return line.elements.map { elem ->
            val cat = resolveElementCategory(elem.text, matchedItem != null)
            OcrElementBox(
                text = elem.text,
                category = cat,
                boundingBox = toBoundingBoxRect(elem.left, elem.top, elem.right, elem.bottom, imageWidth, imageHeight),
                confidence = elem.confidence,
                associatedItemId = matchedItem?.id,
                lineNumber = lineIdx
            )
        }
    }

    private fun resolveElementCategory(token: String, isPartOfItem: Boolean): OcrSemanticCategory {
        return when {
            isPriceToken(token) -> OcrSemanticCategory.PRICE
            isQuantityToken(token) -> OcrSemanticCategory.QUANTITY
            isPartOfItem -> OcrSemanticCategory.PRODUCT_NAME
            token.length > 2 && token.any { it.isLetter() } -> OcrSemanticCategory.PRODUCT_NAME
            else -> OcrSemanticCategory.OTHER
        }
    }

    private fun isStoreHeaderLine(upperText: String, lineIdx: Int): Boolean {
        if (lineIdx < 15 && STORE_KEYWORDS.any { upperText.contains(it) }) return true
        return upperText.startsWith("NIT") || upperText.startsWith("N.I.T.") || upperText.startsWith("DIRECCION")
    }

    private fun isDiscountLine(upperText: String): Boolean {
        return DISCOUNT_KEYWORDS.any { upperText.contains(it) } || (upperText.contains("-") && upperText.contains("$"))
    }

    private fun isTotalLine(upperText: String): Boolean {
        return TOTAL_KEYWORDS.any { upperText.contains(it) }
    }

    private fun isPriceToken(token: String): Boolean {
        val clean = token.replace("$", "").trim()
        val matcher = PRICE_PATTERN.matcher(clean)
        if (!matcher.matches()) return false
        val digits = clean.replace(".", "").replace(",", "")
        val num = digits.toLongOrNull() ?: return false
        return num >= 50
    }

    private fun isQuantityToken(token: String): Boolean {
        val upper = token.trim().uppercase(Locale.ROOT)
        return QUANTITY_PATTERN.matcher(upper).matches()
    }

    private fun findMatchingItem(lineText: String, items: List<ReceiptItemDraft>?): ReceiptItemDraft? {
        if (items.isNullOrEmpty()) return null
        val upper = lineText.uppercase(Locale.ROOT)
        return items.firstOrNull { item ->
            val descUpper = item.rawDescription.uppercase(Locale.ROOT)
            val words = descUpper.split(" ").filter { it.length > 2 }
            val wordsMatch = words.count { upper.contains(it) } >= (words.size.coerceAtMost(2))
            val priceDigits = item.finalPrice.amountMinor.toString()
            val cleanLineDigits = upper.replace(".", "").replace(",", "")
            wordsMatch || cleanLineDigits.contains(priceDigits)
        }
    }

    private fun toBoundingBoxRect(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        imageWidth: Int,
        imageHeight: Int
    ): BoundingBoxRect {
        val w = imageWidth.toFloat().coerceAtLeast(1f)
        val h = imageHeight.toFloat().coerceAtLeast(1f)
        return BoundingBoxRect(
            left = (left / w).coerceIn(0f, 1f),
            top = (top / h).coerceIn(0f, 1f),
            right = (right / w).coerceIn(0f, 1f),
            bottom = (bottom / h).coerceIn(0f, 1f),
            pixelLeft = left,
            pixelTop = top,
            pixelRight = right,
            pixelBottom = bottom
        )
    }
}
