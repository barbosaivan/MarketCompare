package com.barbosa.marketcompare.presentation.components

import androidx.compose.ui.graphics.Color
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.presentation.theme.MarketCompareSemanticColors

/**
 * Mapeo de colores y etiquetas para las categorías semánticas de OCR (MC-056).
 */
object OcrCategoryColors {

    fun getColor(category: OcrSemanticCategory, semanticColors: MarketCompareSemanticColors): Color {
        return when (category) {
            OcrSemanticCategory.STORE_HEADER -> semanticColors.ocrStoreBlue
            OcrSemanticCategory.PRODUCT_NAME -> semanticColors.ocrProductGreen
            OcrSemanticCategory.PRICE -> semanticColors.ocrPriceGold
            OcrSemanticCategory.QUANTITY -> semanticColors.ocrQuantityPurple
            OcrSemanticCategory.DISCOUNT -> semanticColors.ocrDiscountRed
            OcrSemanticCategory.OTHER -> Color(0xFF9E9E9E)
        }
    }

    fun getLabelResId(category: OcrSemanticCategory): Int {
        return when (category) {
            OcrSemanticCategory.STORE_HEADER -> R.string.ocr_category_store
            OcrSemanticCategory.PRODUCT_NAME -> R.string.ocr_category_product
            OcrSemanticCategory.PRICE -> R.string.ocr_category_price
            OcrSemanticCategory.QUANTITY -> R.string.ocr_category_quantity
            OcrSemanticCategory.DISCOUNT -> R.string.ocr_category_discount
            OcrSemanticCategory.OTHER -> R.string.ocr_category_other
        }
    }
}
