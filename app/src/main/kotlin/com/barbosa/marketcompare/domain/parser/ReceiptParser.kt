package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.model.UnitType
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.regex.Pattern

object ReceiptParser {

    private val KNOWN_STORES = listOf(
        "ALMACENES EXITO" to "Éxito",
        "EXITO" to "Éxito",
        "CARULLA" to "Carulla",
        "OLIMPICA" to "Olímpica",
        "TIENDAS D1" to "D1",
        "D1" to "D1",
        "KOBA COLOMBIA" to "D1",
        "TIENDAS ARA" to "Ara",
        "JERONIMO MARTINS" to "Ara",
        "ARA" to "Ara",
        "JUMBO" to "Jumbo",
        "CENCOSUD" to "Jumbo",
        "METRO" to "Metro",
        "SUPER INTER" to "Súper Ínter",
        "SURTIMAX" to "Surtimax",
        "MAKRO" to "Makro",
        "PRICE SMART" to "PriceSmart",
        "PRICESMART" to "PriceSmart",
        "EURO SUPERMERCADO" to "Euro",
        "ISIMO" to "Ísimo"
    )

    private val METADATA_KEYWORDS = listOf(
        // Fiscales y Dian
        "NIT", "N.I.T.", "RUT", "CUFE", "RESOLUCION", "AUTORIZACION", "DIAN", "REGIMEN",
        // Metadata / Cabecera / Ubicacion
        "FECHA", "HORA", "CARRERA", "CALLE", "AVENIDA", "TRANSVERSAL", "DIAGONAL",
        "TELEFONO", "TEL:", "CEL:", "DIRECCION",
        "TIQUETE", "FACTURA", "COMPROBANTE", "CAJERO", "TERMINAL", "CAJA",
        // Subtotales, impuestos y formas de pago
        "SUBTOTAL", "IMPOCONSUMO", "BASE GRAVABLE", "BASE IVA", "EXENTO", "EXCLUIDO",
        "EFECTIVO", "CAMBIO", "VUELTOS", "TARJETA", "DEBITO", "CREDITO",
        "PROPINA", "REDONDEO", "CHEQUE", "BONO", "CLIENTE", "PUNTOS COLOMBIA"
    )

    private val DISCOUNT_KEYWORDS = listOf(
        "DCTO", "DESCUENTO", "AHORRO", "PROMO", "REBAJA"
    )

    private val DATE_PATTERNS = listOf(
        SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CO")),
        SimpleDateFormat("dd-MM-yyyy", Locale.forLanguageTag("es-CO")),
        SimpleDateFormat("yyyy/MM/dd", Locale.forLanguageTag("es-CO")),
        SimpleDateFormat("yyyy-MM-dd", Locale.forLanguageTag("es-CO")),
        SimpleDateFormat("dd/MM/yy", Locale.forLanguageTag("es-CO"))
    )

    // Regex para detectar precios colombianos al final de línea
    private val PRICE_REGEX = Pattern.compile("""(?:\s|^)(?:[-–]\s*)?(?:\$\s*)?(\d{1,3}(?:[.,]\d{3})*(?:[.,]\d{2})?|\d{3,7})\s*$""")
    private val NIT_REGEX = Pattern.compile("""(?:NIT|N\.I\.T\.)\s*[:.]?\s*(\d{3}[\d.-]+)""", Pattern.CASE_INSENSITIVE)
    private val DATE_REGEX = Pattern.compile("""\b(\d{1,4}[/-]\d{1,2}[/-]\d{2,4})\b""")
    private val DATE_ONLY_REGEX = Regex("""^(\d{1,4}[/-]\d{1,2}[/-]\d{2,4})(\s+\d{1,2}:\d{2}(:\d{2})?)?$""")
    private val IVA_REGEX = Regex("""\bIVA\b""")
    private val QTY_X_PRICE_REGEX = Pattern.compile("""(\d+(?:[.,]\d+)?)\s*(?:X|x|\*)\s*(\d{1,3}(?:[.,]\d{3})*)""")

    fun parse(rawText: String, documentUri: String? = null): ReceiptDraft {
        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        val detectedStore = detectStore(lines)
        val detectedDate = detectDate(rawText) ?: System.currentTimeMillis()
        val detectedTaxId = detectTaxId(rawText)

        var detectedTotal: Money? = null
        val items = mutableListOf<ReceiptItemDraft>()

        for (i in lines.indices) {
            val line = lines[i]
            val upperLine = line.uppercase(Locale.ROOT)

            // 0. Omitir separadores visuales
            if (line.all { it == '-' || it == '=' || it == '*' || it == '_' || it.isWhitespace() }) {
                continue
            }

            // 1. Detectar si es línea de total
            if (upperLine.startsWith("TOTAL") || upperLine.contains("TOTAL A PAGAR") || upperLine.contains("TOTAL PAGAR") || upperLine.contains("VALOR TOTAL")) {
                val totalAmount = extractPrice(line)
                if (totalAmount != null && (detectedTotal == null || totalAmount > detectedTotal)) {
                    detectedTotal = totalAmount
                }
                continue
            }

            // 2. Detectar si es línea de descuento atribuible al artículo anterior
            val isDiscountLine = DISCOUNT_KEYWORDS.any { upperLine.contains(it) } ||
                    (line.contains("-") && extractPrice(line) != null && !upperLine.contains("NIT"))

            if (isDiscountLine) {
                val discountAmount = extractPrice(line)
                if (discountAmount != null && items.isNotEmpty()) {
                    val lastItem = items.removeAt(items.size - 1)
                    val newFinalPrice = if (lastItem.finalPrice.amountMinor > discountAmount.amountMinor) {
                        lastItem.finalPrice - discountAmount
                    } else {
                        lastItem.finalPrice
                    }
                    val detectedType = DiscountClassifier.classify(line)
                    items.add(
                        lastItem.copy(
                            discount = discountAmount,
                            discountType = detectedType,
                            discountEvidence = line.trim(),
                            finalPrice = newFinalPrice
                        )
                    )
                }
                continue
            }

            // 3. Omitir líneas que son únicamente fechas o empiezan por fecha/hora
            if (DATE_ONLY_REGEX.matches(line) || upperLine.startsWith("FECHA")) {
                continue
            }

            // 4. Omitir líneas de metadatos, pie de página, impuestos o formas de pago
            if (METADATA_KEYWORDS.any { upperLine.contains(it) } || upperLine.startsWith("PAGO") || IVA_REGEX.containsMatchIn(upperLine)) {
                if (!isPotentialItem(upperLine)) {
                    continue
                }
            }

            // 5. Intentar parsear como artículo
            val itemDraft = parseItemLine(line)
            if (itemDraft != null) {
                items.add(itemDraft)
            }
        }

        // Si no se detectó total explícito pero hay artículos, sugerir la suma de los mismos
        val computedTotal = if (items.isNotEmpty()) {
            Money(items.sumOf { it.finalPrice.amountMinor }, "COP")
        } else null

        val finalTotal = detectedTotal ?: computedTotal

        return ReceiptDraft(
            detectedStoreName = detectedStore,
            detectedDateTimestamp = detectedDate,
            detectedTaxId = detectedTaxId,
            detectedTotal = finalTotal,
            rawText = rawText,
            items = items,
            rawDocumentUri = documentUri
        )
    }

    private fun detectStore(lines: List<String>): String? {
        val topLines = lines.take(12)
        for (line in topLines) {
            val upper = line.uppercase(Locale.ROOT)
            for ((key, name) in KNOWN_STORES) {
                if (upper.contains(key)) {
                    return name
                }
            }
        }
        return topLines.firstOrNull { it.length in 3..30 && !it.any { c -> c.isDigit() } }
    }

    private fun detectTaxId(text: String): String? {
        val matcher = NIT_REGEX.matcher(text)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun detectDate(text: String): Long? {
        val matcher = DATE_REGEX.matcher(text)
        while (matcher.find()) {
            val dateStr = matcher.group(1) ?: continue
            for (format in DATE_PATTERNS) {
                try {
                    val parsed = format.parse(dateStr)
                    if (parsed != null && parsed.time > 946684800000L && parsed.time <= System.currentTimeMillis() + 86400000L) {
                        return parsed.time
                    }
                } catch (_: Exception) {}
            }
        }
        return null
    }

    private fun parseItemLine(line: String): ReceiptItemDraft? {
        val trimmed = line.trim()
        if (trimmed.length < 4) return null

        val priceMatcher = PRICE_REGEX.matcher(trimmed)
        if (!priceMatcher.find()) return null

        val priceStr = priceMatcher.group(1) ?: return null
        val finalPrice = parseCleanPrice(priceStr) ?: return null
        // En Colombia productos de supermercado son al menos 50 COP
        if (finalPrice.amountMinor < 50) return null

        val descriptionPart = trimmed.substring(0, priceMatcher.start()).trim()
        if (descriptionPart.length < 3) return null
        if (descriptionPart.count { it.isLetter() } < 2) return null
        if (descriptionPart.endsWith(":") || descriptionPart.endsWith("/") || descriptionPart.endsWith("-")) {
            return null
        }

        // Detectar si hay patrón de cantidad: "2 X 4.000"
        var quantity = ProductQuantity(1L, UnitType.UNIT)
        var unitPrice: Money? = null
        var cleanDescription = descriptionPart

        val qtyMatcher = QTY_X_PRICE_REGEX.matcher(descriptionPart)
        if (qtyMatcher.find()) {
            val qtyVal = qtyMatcher.group(1)?.replace(",", ".")?.toDoubleOrNull()
            val unitPriceVal = qtyMatcher.group(2)?.let { parseCleanPrice(it) }
            if (qtyVal != null && qtyVal > 0) {
                quantity = ProductQuantity(qtyVal.toLong().coerceAtLeast(1L), UnitType.UNIT)
                unitPrice = unitPriceVal
                cleanDescription = descriptionPart.substring(0, qtyMatcher.start()).trim()
            }
        }

        // Si la descripción contiene indicadores de peso / unidad
        val upperDesc = cleanDescription.uppercase(Locale.ROOT)
        if (upperDesc.contains("KG") || upperDesc.contains("KILO")) {
            quantity = ProductQuantity(quantity.value, UnitType.KILOGRAM)
        } else if (upperDesc.contains(" G ") || upperDesc.endsWith("G") || upperDesc.endsWith("GR")) {
            quantity = ProductQuantity(quantity.value, UnitType.GRAM)
        } else if (upperDesc.contains("ML") || upperDesc.contains("C.C.")) {
            quantity = ProductQuantity(quantity.value, UnitType.MILLILITER)
        } else if (upperDesc.contains(" L ") || upperDesc.endsWith("L") || upperDesc.contains("LITRO")) {
            quantity = ProductQuantity(quantity.value, UnitType.LITER)
        }

        val confidence = when {
            cleanDescription.length < 3 -> 0.6f
            cleanDescription.any { it.isDigit() } && cleanDescription.any { it.isLetter() } -> 0.95f
            else -> 0.9f
        }

        return ReceiptItemDraft(
            rawDescription = cleanDescription,
            quantity = quantity,
            unitListPrice = unitPrice ?: finalPrice,
            discount = null,
            finalPrice = finalPrice,
            confidenceScore = confidence
        )
    }

    private fun extractPrice(line: String): Money? {
        val matcher = PRICE_REGEX.matcher(line)
        return if (matcher.find()) {
            matcher.group(1)?.let { parseCleanPrice(it) }
        } else null
    }

    private fun parseCleanPrice(raw: String): Money? {
        val cleaned = raw.replace("$", "").replace(" ", "").trim()
        val digitsOnly = cleaned.replace(".", "").replace(",", "")
        val longVal = digitsOnly.toLongOrNull() ?: return null
        return Money(longVal, "COP")
    }

    private fun isPotentialItem(line: String): Boolean {
        return line.contains("ARROZ") || line.contains("LECHE") || line.contains("PAN") || line.contains("HUEVOS") || line.contains("ACEITE")
    }
}
