package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.model.UnitType
import java.util.Locale
import java.util.regex.Pattern

/**
 * Asistente puro de dominio para reasignación de texto detectado por OCR a campos del recibo (MC-057).
 * Garantiza cumplimiento de ADR 0001 (sin persistencia externa) y ADR 0003 (aritmética exacta con Money).
 */
object OcrFieldAssignmentHelper {

    private val PRICE_PATTERN: Pattern = Pattern.compile("""(\d+(?:[.,]\d+)?)""")
    private val QUANTITY_PATTERN: Pattern = Pattern.compile(
        """(\d+(?:[.,]\d+)?)\s*(KG|KILOS?|G|GR|GRS|ML|CC|L|LTS?|UND|UN)?""",
        Pattern.CASE_INSENSITIVE
    )

    fun parsePrice(text: String): Money? {
        val sanitized = text.replace("$", "").replace("COP", "", ignoreCase = true).trim()
        val digitsOnly = sanitized.replace(".", "").replace(",", "").trim()
        val longVal = digitsOnly.toLongOrNull() ?: return null
        return Money(longVal, "COP")
    }

    fun parseQuantity(text: String): ProductQuantity? {
        val matcher = QUANTITY_PATTERN.matcher(text.trim())
        if (!matcher.find()) return null

        val numStr = matcher.group(1)?.replace(",", ".") ?: return null
        val numVal = numStr.toDoubleOrNull() ?: return null
        val unitStr = matcher.group(2)?.uppercase(Locale.ROOT) ?: ""

        return when {
            unitStr.startsWith("KG") || unitStr.startsWith("KILO") -> {
                ProductQuantity((numVal * 1000).toLong().coerceAtLeast(1L), UnitType.KILOGRAM)
            }
            unitStr.startsWith("G") || unitStr.startsWith("GR") -> {
                ProductQuantity(numVal.toLong().coerceAtLeast(1L), UnitType.GRAM)
            }
            unitStr.startsWith("ML") || unitStr.contains("CC") -> {
                ProductQuantity(numVal.toLong().coerceAtLeast(1L), UnitType.MILLILITER)
            }
            unitStr.startsWith("L") || unitStr.startsWith("LT") -> {
                ProductQuantity((numVal * 1000).toLong().coerceAtLeast(1L), UnitType.LITER)
            }
            else -> {
                ProductQuantity(numVal.toLong().coerceAtLeast(1L), UnitType.UNIT)
            }
        }
    }

    fun assignStoreName(text: String): String {
        return text
            .replace(Regex("""(?i)\b(bienvenidos?|a|factura|ticket|de|venta)\b"""), "")
            .trim(' ', '-', ':', '*', '#')
            .ifBlank { text.trim() }
    }

    fun assignItemDescription(item: ReceiptItemDraft, text: String): ReceiptItemDraft {
        return item.copy(rawDescription = text.trim())
    }

    fun assignItemPrice(item: ReceiptItemDraft, text: String): ReceiptItemDraft {
        val parsed = parsePrice(text) ?: return item
        val discount = item.discount
        val finalPrice = if (discount != null && parsed.amountMinor >= discount.amountMinor) {
            parsed - discount
        } else {
            parsed
        }
        return item.copy(
            unitListPrice = parsed,
            finalPrice = finalPrice
        )
    }

    fun assignItemQuantity(item: ReceiptItemDraft, text: String): ReceiptItemDraft {
        val parsed = parseQuantity(text) ?: return item
        return item.copy(quantity = parsed)
    }

    fun assignItemDiscount(item: ReceiptItemDraft, text: String): ReceiptItemDraft {
        val parsed = parsePrice(text) ?: return item
        val basePrice = item.unitListPrice ?: item.finalPrice
        val finalPrice = if (basePrice.amountMinor >= parsed.amountMinor) {
            basePrice - parsed
        } else {
            item.finalPrice
        }
        return item.copy(
            discount = parsed,
            unitListPrice = basePrice,
            finalPrice = finalPrice,
            discountType = DiscountType.EXPLICIT_LINE,
            discountEvidence = text.trim()
        )
    }

    fun createNewItem(box: OcrElementBox): ReceiptItemDraft {
        return when (box.category) {
            OcrSemanticCategory.PRICE -> {
                val price = parsePrice(box.text) ?: Money.ZERO
                ReceiptItemDraft(
                    rawDescription = "Nuevo producto",
                    quantity = ProductQuantity(1L, UnitType.UNIT),
                    unitListPrice = price,
                    discount = null,
                    finalPrice = price,
                    isIncluded = true
                )
            }
            OcrSemanticCategory.QUANTITY -> {
                val qty = parseQuantity(box.text) ?: ProductQuantity(1L, UnitType.UNIT)
                ReceiptItemDraft(
                    rawDescription = "Nuevo producto",
                    quantity = qty,
                    unitListPrice = Money.ZERO,
                    discount = null,
                    finalPrice = Money.ZERO,
                    isIncluded = true
                )
            }
            else -> {
                ReceiptItemDraft(
                    rawDescription = box.text.trim(),
                    quantity = ProductQuantity(1L, UnitType.UNIT),
                    unitListPrice = Money.ZERO,
                    discount = null,
                    finalPrice = Money.ZERO,
                    isIncluded = true
                )
            }
        }
    }
}
