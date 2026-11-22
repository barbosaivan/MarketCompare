package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.model.UnitType
import java.util.Locale
import java.util.regex.Pattern

/**
 * Analizador especializado en facturas y tiquetes de supermercados colombianos (D1, Ara, Éxito, etc.).
 */
object ColombianReceiptMatcher {

    // Formato POS D1 / Ara: [Ítem] [Cantidad] [Unidad] [PrecioUnit] [CódigoBarras] [Descripción] [PrecioTotal] [Impuesto]
    // Ejemplo: "2 2 UN 4,990 7700304457908 TOMATE CHONTO 9,980 6"
    private val POS_BARCODE_LINE_PATTERN = Pattern.compile(
        """^(?:(\d{1,3})\s+)?(\d+(?:[.,]\d+)?)\s+(UN|UND|KG|GR|G|L|ML)\s+([\d.,]+)\s+(\d{7,14})\s+(.+?)\s+([\d.,]+)(?:\s+[A-Za-z0-9]{1,2})?\s*${'$'}""",
        Pattern.CASE_INSENSITIVE
    )

    // Formato Factura Electrónica: [Ítem] [CódigoBarras] [Descripción] ... [PrecioTotal]
    // Ejemplo: "1 7700304389865 PAPEL HIG 3H ULTRASU 7.100,00"
    private val ELECTRONIC_ITEM_PATTERN = Pattern.compile(
        """^(?:(\d{1,3})\s+)?(\d{7,14})\s+(.+?)\s+([\d.,]+)(?:\s+[A-Za-z0-9]{1,2})?\s*${'$'}""",
        Pattern.CASE_INSENSITIVE
    )

    // Regex para detectar precio con sufijo tributario opcional: "4.200 A" o "9,980 6"
    val PRICE_WITH_TAX_REGEX: Pattern = Pattern.compile(
        """(?:\s|^)(?:[-–]\s*)?(?:\$\s*)?(\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{2})?|\d{3,7})(?:\s+[A-Za-z0-9]{1,2})?\s*${'$'}"""
    )

    fun matchPosItem(line: String): ReceiptItemDraft? {
        val trimmed = line.trim()
        val posMatcher = POS_BARCODE_LINE_PATTERN.matcher(trimmed)
        if (posMatcher.find()) {
            val qtyRaw = posMatcher.group(2)?.replace(",", ".")?.toDoubleOrNull() ?: 1.0
            val unitStr = posMatcher.group(3)?.uppercase(Locale.ROOT) ?: "UN"
            val unitPrice = parseCleanPrice(posMatcher.group(4))
            val description = posMatcher.group(6)?.trim() ?: return null
            val totalPrice = parseCleanPrice(posMatcher.group(7)) ?: return null

            val unitType = when (unitStr) {
                "KG" -> UnitType.KILOGRAM
                "GR", "G" -> UnitType.GRAM
                "L" -> UnitType.LITER
                "ML" -> UnitType.MILLILITER
                else -> UnitType.UNIT
            }

            return ReceiptItemDraft(
                rawDescription = description,
                quantity = ProductQuantity(qtyRaw.toLong().coerceAtLeast(1L), unitType),
                unitListPrice = unitPrice ?: totalPrice,
                discount = null,
                finalPrice = totalPrice,
                confidenceScore = 0.98f
            )
        }

        val elecMatcher = ELECTRONIC_ITEM_PATTERN.matcher(trimmed)
        if (elecMatcher.find()) {
            val description = elecMatcher.group(3)?.trim() ?: return null
            val totalPrice = parseCleanPrice(elecMatcher.group(4)) ?: return null
            if (description.count { it.isLetter() } >= 2 && totalPrice.amountMinor >= 50) {
                return ReceiptItemDraft(
                    rawDescription = description,
                    quantity = ProductQuantity(1L, UnitType.UNIT),
                    unitListPrice = totalPrice,
                    discount = null,
                    finalPrice = totalPrice,
                    confidenceScore = 0.95f
                )
            }
        }

        return null
    }

    fun parseCleanPrice(raw: String?): Money? {
        if (raw.isNullOrBlank()) return null
        var cleaned = raw.replace("$", "").replace(" ", "").trim()

        // Si tiene 2 decimales fiscales al final (ej. ",00" o ".00" en facturas electrónicas)
        val decimalMatch = Regex("""[.,](\d{2})$""").find(cleaned)
        if (decimalMatch != null) {
            cleaned = cleaned.substring(0, decimalMatch.range.first)
        }

        val digitsOnly = cleaned.replace(".", "").replace(",", "").replace("-", "").trim()
        val longVal = digitsOnly.toLongOrNull() ?: return null
        return Money(longVal, "COP")
    }
}
