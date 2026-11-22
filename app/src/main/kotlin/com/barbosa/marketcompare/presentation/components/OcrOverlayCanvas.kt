package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.presentation.theme.semanticColors

/**
 * Lienzo de alto rendimiento para renderizar capas semánticas de cajas OCR superpuestas (MC-056).
 */
@Composable
fun OcrOverlayCanvas(
    ocrBlocks: List<OcrElementBox>,
    modifier: Modifier = Modifier,
    visibleCategories: Set<OcrSemanticCategory> = OcrSemanticCategory.entries.toSet(),
    selectedBlockId: String? = null,
    onBlockClick: ((OcrElementBox) -> Unit)? = null
) {
    val semanticColors = MaterialTheme.semanticColors

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (onBlockClick != null) {
                    Modifier.pointerInput(ocrBlocks, visibleCategories) {
                        detectTapGestures { tapOffset ->
                            if (size.width > 0 && size.height > 0) {
                                val normX = (tapOffset.x / size.width).coerceIn(0f, 1f)
                                val normY = (tapOffset.y / size.height).coerceIn(0f, 1f)
                                val matched = ocrBlocks.lastOrNull { box ->
                                    box.category in visibleCategories && box.boundingBox.containsNormalizedPoint(normX, normY)
                                }
                                if (matched != null) {
                                    onBlockClick(matched)
                                }
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        for (block in ocrBlocks) {
            if (block.category !in visibleCategories) continue
            val isSelected = block.id == selectedBlockId
            val color = OcrCategoryColors.getColor(block.category, semanticColors)

            val left = block.boundingBox.left * canvasWidth
            val top = block.boundingBox.top * canvasHeight
            val width = block.boundingBox.width * canvasWidth
            val height = block.boundingBox.height * canvasHeight

            drawRect(
                color = color.copy(alpha = if (isSelected) 0.35f else 0.16f),
                topLeft = Offset(left, top),
                size = Size(width, height)
            )

            drawRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(width, height),
                style = Stroke(width = if (isSelected) 4f else 1.8f)
            )
        }
    }
}
