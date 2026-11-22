package com.barbosa.marketcompare.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Gestor de estado para zoom y paneo táctil en el visor de fotos de recibos (MC-055).
 */
class ZoomPanState(
    val minScale: Float = 1.0f,
    val maxScale: Float = 5.0f
) {
    var scale by mutableFloatStateOf(minScale)
    var offset by mutableStateOf(Offset.Zero)
    var contentSize by mutableStateOf(Size.Zero)
    var containerSize by mutableStateOf(Size.Zero)

    fun onTransform(panChange: Offset, zoomChange: Float, centroid: Offset = Offset.Zero) {
        val newScale = (scale * zoomChange).coerceIn(minScale, maxScale)
        val scaleFactor = newScale / scale
        val updatedOffset = (offset + panChange) * scaleFactor - (centroid * (scaleFactor - 1f))

        scale = newScale
        offset = clampOffset(updatedOffset, newScale)
    }

    fun onDoubleTap(tapOffset: Offset = Offset.Zero) {
        if (scale > 1.05f) {
            reset()
        } else {
            val targetScale = 2.5f.coerceAtMost(maxScale)
            val center = if (containerSize != Size.Zero) {
                Offset(containerSize.width / 2f, containerSize.height / 2f)
            } else tapOffset
            val newOffset = (center - tapOffset) * (targetScale - 1f)
            scale = targetScale
            offset = clampOffset(newOffset, targetScale)
        }
    }

    fun zoomIn() {
        val newScale = (scale * 1.5f).coerceAtMost(maxScale)
        scale = newScale
        offset = clampOffset(offset, newScale)
    }

    fun zoomOut() {
        val newScale = (scale / 1.5f).coerceAtLeast(minScale)
        scale = newScale
        if (newScale <= minScale) {
            offset = Offset.Zero
        } else {
            offset = clampOffset(offset, newScale)
        }
    }

    fun reset() {
        scale = minScale
        offset = Offset.Zero
    }

    fun clampOffset(proposedOffset: Offset, currentScale: Float = scale): Offset {
        if (contentSize == Size.Zero || containerSize == Size.Zero || currentScale <= minScale) {
            return Offset.Zero
        }
        val scaledWidth = contentSize.width * currentScale
        val scaledHeight = contentSize.height * currentScale

        val maxOffsetX = ((scaledWidth - containerSize.width) / 2f).coerceAtLeast(0f)
        val maxOffsetY = ((scaledHeight - containerSize.height) / 2f).coerceAtLeast(0f)

        return Offset(
            x = proposedOffset.x.coerceIn(-maxOffsetX, maxOffsetX),
            y = proposedOffset.y.coerceIn(-maxOffsetY, maxOffsetY)
        )
    }
}

@Composable
fun rememberZoomPanState(
    minScale: Float = 1.0f,
    maxScale: Float = 5.0f
): ZoomPanState {
    return remember(minScale, maxScale) {
        ZoomPanState(minScale = minScale, maxScale = maxScale)
    }
}
