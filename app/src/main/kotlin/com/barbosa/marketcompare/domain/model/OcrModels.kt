package com.barbosa.marketcompare.domain.model

import java.util.UUID

/**
 * Categorías semánticas reconocidas para las cajas delimitadoras de OCR en recibos.
 * Mapeadas visualmente en los colores de la compuerta Gate 10:
 * - STORE_HEADER: 🔵 Azul (Tienda, NIT, dirección, encabezado fiscal)
 * - PRODUCT_NAME: 🟢 Verde (Descripción del producto)
 * - PRICE: 🟡 Dorado (Precios unitarios, importes de línea y totales)
 * - QUANTITY: 🟣 Morado (Cantidades, unidades y multiplicadores)
 * - DISCOUNT: 🔴 Rojo (Descuentos, promociones y ahorros)
 * - OTHER: Gris / Neutro (Metadatos adicionales o no clasificados)
 */
enum class OcrSemanticCategory {
    STORE_HEADER,
    PRODUCT_NAME,
    PRICE,
    QUANTITY,
    DISCOUNT,
    OTHER
}

/**
 * Coordenadas delimitadoras normalizadas (0.0f .. 1.0f) respecto a la imagen original,
 * junto con las coordenadas de píxeles enteras originales.
 */
data class BoundingBoxRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val pixelLeft: Int = 0,
    val pixelTop: Int = 0,
    val pixelRight: Int = 0,
    val pixelBottom: Int = 0
) {
    val width: Float get() = (right - left).coerceAtLeast(0f)
    val height: Float get() = (bottom - top).coerceAtLeast(0f)

    fun containsNormalizedPoint(x: Float, y: Float): Boolean {
        return x in left..right && y in top..bottom
    }
}

/**
 * Representa una caja individual detectada con su semántica asignada y metadatos.
 */
data class OcrElementBox(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val category: OcrSemanticCategory,
    val boundingBox: BoundingBoxRect,
    val confidence: Float = 1.0f,
    val associatedItemId: String? = null,
    val lineNumber: Int = 0
)

/**
 * Elemento atómico de texto extraído por el motor OCR con coordenadas en píxeles.
 */
data class RawOcrElement(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val confidence: Float = 1.0f
)

/**
 * Línea de texto completa detectada por OCR con sus elementos atómicos individuales.
 */
data class RawOcrLine(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
    val elements: List<RawOcrElement> = emptyList(),
    val confidence: Float = 1.0f
)

/**
 * Carga de datos completa extraída por el motor OCR de un documento o foto.
 */
data class RawOcrPayload(
    val fullText: String,
    val imageWidth: Int,
    val imageHeight: Int,
    val lines: List<RawOcrLine> = emptyList()
)
