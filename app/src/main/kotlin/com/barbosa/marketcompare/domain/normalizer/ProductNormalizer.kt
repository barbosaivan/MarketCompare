package com.barbosa.marketcompare.domain.normalizer

import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.UnitType
import java.text.Normalizer
import java.util.Locale
import java.util.regex.Pattern

data class NormalizedProductCandidate(
    val canonicalName: String,
    val brand: String?,
    val cleanBaseName: String,
    val category: String?,
    val baseQuantity: Long,
    val unit: UnitType,
    val presentation: String?,
    val confidenceScore: Float
) {
    fun toProduct(id: String): Product {
        return Product(
            id = id,
            canonicalName = canonicalName,
            brand = brand,
            category = category,
            baseQuantity = baseQuantity,
            unit = unit,
            presentation = presentation
        )
    }

    /**
     * Clave única de variante: si dos productos difieren en cantidad, unidad o presentación,
     * generan llaves distintas y se impide su fusión accidental.
     */
    fun variantFingerprint(): String {
        val b = brand?.uppercase(Locale.ROOT) ?: "GENERIC"
        val p = presentation?.uppercase(Locale.ROOT) ?: "STANDARD"
        val n = cleanBaseName.uppercase(Locale.ROOT)
        return "$b|$n|$baseQuantity|${unit.name}|$p"
    }
}

object ProductNormalizer {

    private val KNOWN_BRANDS = listOf(
        "FLORHUILA" to "Florhuila",
        "ALQUERIA" to "Alquería",
        "ALPINA" to "Alpina",
        "COLANTA" to "Colanta",
        "ROA" to "Roa",
        "DIANA" to "Diana",
        "BIMBO" to "Bimbo",
        "SELLO ROJO" to "Sello Rojo",
        "COLCAFE" to "Colcafé",
        "CORONA" to "Corona",
        "ZENU" to "Zenú",
        "PIETRAN" to "Pietrán",
        "RICA" to "Rica",
        "SANTA REYES" to "Santa Reyes",
        "KIKER" to "Kiker",
        "PREMIER" to "Premier",
        "GOURMET" to "Gourmet",
        "OLEOCALI" to "Oleocali",
        "FAMILIA" to "Familia",
        "ELITE" to "Elite",
        "SUAVE" to "Suave",
        "COLGATE" to "Colgate",
        "ORAL-B" to "Oral-B",
        "PROTEX" to "Protex",
        "PALMOLIVE" to "Palmolive",
        "SAVITAL" to "Savital",
        "DOVE" to "Dove",
        "ARIEL" to "Ariel",
        "FAB" to "Fab",
        "DERSA" to "Dersa",
        "AXION" to "Axion",
        "AJAX" to "Ajax",
        "FABULOSO" to "Fabuloso",
        "CLOROX" to "Clorox",
        "DORIA" to "Doria",
        "MONTICELLO" to "Monticello",
        "COCA COLA" to "Coca-Cola",
        "POSTOBON" to "Postobón",
        "HIT" to "Hit",
        "DEL VALLE" to "Del Valle",
        "NESCAFE" to "Nescafé",
        "MILO" to "Milo",
        "CHOCOLISTO" to "Chocolisto",
        "RAMO" to "Ramo",
        "NOEL" to "Noel",
        "SALTIN" to "Saltín Noel",
        "DUCALES" to "Ducales"
    )

    private val PRESENTATIONS = listOf(
        "TETRA PAK" to "Tetra Pak",
        "TETRAPAK" to "Tetra Pak",
        "TETRA" to "Tetra Pak",
        "DOYPACK" to "Doypack",
        "DOY PACK" to "Doypack",
        "BANDEJA" to "Bandeja",
        "BOTELLA" to "Botella",
        "BOLSA" to "Bolsa",
        "VASO" to "Vaso",
        "LATA" to "Lata",
        "CAJA" to "Caja",
        "PAQUETE" to "Paquete",
        "FRASCO" to "Frasco",
        "BARRA" to "Barra",
        "TUBO" to "Tubo",
        "SOBRE" to "Sobre",
        "SACHET" to "Sobre",
        "POTE" to "Pote",
        "ROLLO" to "Rollo",
        "ROLLOS" to "Rollos"
    )

    // Regex para detectar medidas: ej. 1000G, 1.5L, 500ML, 2KG, 30UND, 12 ROLLOS
    private val QUANTITY_REGEX = Pattern.compile(
        """\b(\d+(?:[.,]\d+)?)\s*(KG|KILOS?|G|GR|GRAMOS?|L|LT|LITROS?|ML|CC|C\.C\.|UND|UNID|UNIDADES?|ROLLOS?|PASTILLAS?|SOBRES?)\b""",
        Pattern.CASE_INSENSITIVE
    )

    fun normalize(rawDescription: String): NormalizedProductCandidate {
        val upper = rawDescription.uppercase(Locale.ROOT)
        var workingText = cleanRawInput(rawDescription)

        // 1. Detectar marca conocida
        var detectedBrand: String? = null
        for ((key, canonicalBrand) in KNOWN_BRANDS) {
            val brandRegex = Regex("""\b${Regex.escape(key)}\b""", RegexOption.IGNORE_CASE)
            if (brandRegex.containsMatchIn(workingText)) {
                detectedBrand = canonicalBrand
                workingText = brandRegex.replace(workingText, " ").trim()
                break
            }
        }

        // 2. Detectar presentación del empaque
        var detectedPresentation: String? = null
        for ((key, canonicalPres) in PRESENTATIONS) {
            val presRegex = Regex("""\b${Regex.escape(key)}\b""", RegexOption.IGNORE_CASE)
            if (presRegex.containsMatchIn(workingText)) {
                detectedPresentation = canonicalPres
                workingText = presRegex.replace(workingText, " ").trim()
                break
            }
        }

        // 3. Detectar cantidad y unidad neta
        var detectedQuantity = 1L
        var detectedUnit = UnitType.UNIT

        val qtyMatcher = QUANTITY_REGEX.matcher(workingText)
        if (qtyMatcher.find()) {
            val numStr = qtyMatcher.group(1)?.replace(",", ".")
            val unitStr = qtyMatcher.group(2)?.uppercase(Locale.ROOT) ?: ""
            val numVal = numStr?.toDoubleOrNull() ?: 1.0

            when {
                unitStr.startsWith("KG") || unitStr.startsWith("KILO") -> {
                    detectedQuantity = (numVal * 1000).toLong().coerceAtLeast(1L)
                    detectedUnit = UnitType.KILOGRAM
                }
                unitStr.startsWith("G") || unitStr.startsWith("GR") -> {
                    detectedQuantity = numVal.toLong().coerceAtLeast(1L)
                    detectedUnit = UnitType.GRAM
                }
                unitStr.startsWith("ML") || unitStr.contains("CC") -> {
                    detectedQuantity = numVal.toLong().coerceAtLeast(1L)
                    detectedUnit = UnitType.MILLILITER
                }
                unitStr.startsWith("L") || unitStr.startsWith("LT") -> {
                    detectedQuantity = (numVal * 1000).toLong().coerceAtLeast(1L)
                    detectedUnit = UnitType.LITER
                }
                else -> {
                    detectedQuantity = numVal.toLong().coerceAtLeast(1L)
                    detectedUnit = UnitType.UNIT
                }
            }

            workingText = workingText.removeRange(qtyMatcher.start(), qtyMatcher.end()).trim()
        }

        // 4. Limpieza del nombre base (eliminar espacios múltiples, números sueltos o símbolos)
        val cleanBase = workingText
            .replace(Regex("""[#*_\-–]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .split(" ")
            .filter { it.length > 1 || it.all { c -> c.isLetter() } }
            .joinToString(" ") { word ->
                word.lowercase(Locale.ROOT).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
            }

        val baseName = if (cleanBase.isNotBlank()) cleanBase else "Producto General"

        // 5. Construcción del Nombre Canónico Estructurado:
        // Formato: [Marca] [Nombre Base] [Cantidad/Unidad] [Presentación]
        val quantityDisplay = ProductQuantity(detectedQuantity, detectedUnit).formatDisplay()
        val canonicalParts = mutableListOf<String>()
        detectedBrand?.let { canonicalParts.add(it) }
        canonicalParts.add(baseName)
        if (detectedUnit != UnitType.UNIT || detectedQuantity > 1) {
            canonicalParts.add(quantityDisplay)
        }
        detectedPresentation?.let { canonicalParts.add(it) }

        val canonicalName = canonicalParts.joinToString(" ").trim()

        val confidence = when {
            detectedBrand != null && detectedPresentation != null -> 0.95f
            detectedBrand != null -> 0.90f
            detectedUnit != UnitType.UNIT -> 0.85f
            else -> 0.75f
        }

        return NormalizedProductCandidate(
            canonicalName = canonicalName,
            brand = detectedBrand,
            cleanBaseName = baseName,
            category = inferCategory(upper),
            baseQuantity = detectedQuantity,
            unit = detectedUnit,
            presentation = detectedPresentation,
            confidenceScore = confidence
        )
    }

    /**
     * Normaliza el texto de alias crudo eliminando acentos, puntuación y espacios redundantes
     * para facilitar la búsqueda en `product_aliases`.
     */
    fun normalizeAlias(text: String): String {
        val nfd = Normalizer.normalize(text, Normalizer.Form.NFD)
        val withoutAccents = nfd.replace(Regex("""\p{InCombiningDiacriticalMarks}+"""), "")
        return withoutAccents
            .uppercase(Locale.ROOT)
            .replace(Regex("""[^A-Z0-9\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    /**
     * Regla estricta de variantes: Dos productos son compatibles solo si comparten
     * marca, cantidad neta, unidad y presentación.
     * Si difieren en tamaño (ej. 150g vs 1000g) o envase (Vaso vs Bolsa), se retorna false.
     */
    fun areVariantsCompatible(p1: Product, p2: Product): Boolean {
        val sameBrand = (p1.brand ?: "").equals(p2.brand ?: "", ignoreCase = true)
        val sameQuantity = p1.baseQuantity == p2.baseQuantity
        val sameUnit = p1.unit == p2.unit
        val samePresentation = (p1.presentation ?: "").equals(p2.presentation ?: "", ignoreCase = true)

        return sameBrand && sameQuantity && sameUnit && samePresentation
    }

    private fun cleanRawInput(raw: String): String {
        return raw.replace(Regex("""\s*X\s*\d+.*$"""), "") // Remueve multiplicadores de precio ej "2 X 4000"
            .replace(Regex("""[$,.:;]+"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    private fun inferCategory(upper: String): String? {
        return when {
            upper.contains("LECHE") || upper.contains("YOG") || upper.contains("QUESO") || upper.contains("MANTEQUILLA") -> "Lácteos"
            upper.contains("ARROZ") || upper.contains("PASTA") || upper.contains("ACEITE") || upper.contains("GRANO") || upper.contains("HARINA") -> "Despensa"
            upper.contains("CARNE") || upper.contains("POLLO") || upper.contains("PESCADO") || upper.contains("SALCHICHA") || upper.contains("JAMON") -> "Carnes y Embutidos"
            upper.contains("JABON") || upper.contains("DETERGENTE") || upper.contains("LAVAPLATOS") || upper.contains("CLORO") || upper.contains("SUAVIZANTE") -> "Aseo Hogar"
            upper.contains("SHAMPOO") || upper.contains("CREMA") || upper.contains("DESODORANTE") || upper.contains("COLGATE") || upper.contains("PAPEL") -> "Cuidado Personal"
            upper.contains("GASEOSA") || upper.contains("JUGO") || upper.contains("AGUA") || upper.contains("CERVEZA") || upper.contains("CAFE") -> "Bebidas"
            upper.contains("PAN") || upper.contains("GALLETA") || upper.contains("TOSTADA") || upper.contains("PONQUE") -> "Panadería"
            upper.contains("HUEVO") || upper.contains("HUEVOS") -> "Huevos y Aves"
            else -> null
        }
    }
}
