package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.platform.ocr.ReceiptImageLoader

/**
 * Visor interactivo de foto de factura con gestos multitáctiles de zoom y paneo (MC-055, MC-056).
 */
@Composable
fun ReceiptPhotoViewer(
    imageUri: String?,
    modifier: Modifier = Modifier,
    ocrBlocks: List<OcrElementBox> = emptyList(),
    showOverlays: Boolean = true,
    visibleCategories: Set<OcrSemanticCategory> = OcrSemanticCategory.entries.toSet(),
    selectedBlockId: String? = null,
    onBlockClick: ((OcrElementBox) -> Unit)? = null,
    zoomPanState: ZoomPanState = rememberZoomPanState(),
    showControls: Boolean = true,
    overlayContent: @Composable (BoxScope.(zoomPanState: ZoomPanState, imageWidth: Float, imageHeight: Float) -> Unit)? = null
) {
    val context = LocalContext.current
    var bitmap by remember(imageUri) { mutableStateOf<ImageBitmap?>(null) }
    var isLoading by remember(imageUri) { mutableStateOf(true) }

    LaunchedEffect(imageUri) {
        isLoading = true
        bitmap = ReceiptImageLoader.loadBitmap(context, imageUri)
        isLoading = false
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val containerW = constraints.maxWidth.toFloat()
        val containerH = constraints.maxHeight.toFloat()

        LaunchedEffect(containerW, containerH, bitmap) {
            zoomPanState.containerSize = Size(containerW, containerH)
            bitmap?.let {
                zoomPanState.contentSize = Size(it.width.toFloat(), it.height.toFloat())
            }
        }

        when {
            isLoading -> {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
            bitmap != null -> {
                val currentBitmap = bitmap!!
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(zoomPanState) {
                            detectTapGestures(
                                onDoubleTap = { tapOffset -> zoomPanState.onDoubleTap(tapOffset) }
                            )
                        }
                        .pointerInput(zoomPanState) {
                            detectTransformGestures(panZoomLock = true) { centroid, pan, zoom, _ ->
                                zoomPanState.onTransform(panChange = pan, zoomChange = zoom, centroid = centroid)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.graphicsLayer {
                            scaleX = zoomPanState.scale
                            scaleY = zoomPanState.scale
                            translationX = zoomPanState.offset.x
                            translationY = zoomPanState.offset.y
                        }
                    ) {
                        Image(
                            bitmap = currentBitmap,
                            contentDescription = stringResource(R.string.review_photo_viewer_title)
                        )
                        if (showOverlays && ocrBlocks.isNotEmpty()) {
                            OcrOverlayCanvas(
                                ocrBlocks = ocrBlocks,
                                visibleCategories = visibleCategories,
                                selectedBlockId = selectedBlockId,
                                onBlockClick = onBlockClick,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        overlayContent?.invoke(this, zoomPanState, currentBitmap.width.toFloat(), currentBitmap.height.toFloat())
                    }
                }

                if (showControls) {
                    PhotoViewerControlsRow(
                        zoomPanState = zoomPanState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    )
                }
            }
            else -> {
                Text(
                    text = stringResource(R.string.review_photo_error),
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun PhotoViewerControlsRow(
    zoomPanState: ZoomPanState,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(32.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = { zoomPanState.zoomOut() },
                enabled = zoomPanState.scale > zoomPanState.minScale
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.review_photo_zoom_out)
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { zoomPanState.reset() }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.review_photo_zoom_percent, (zoomPanState.scale * 100).toInt()),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = { zoomPanState.zoomIn() },
                enabled = zoomPanState.scale < zoomPanState.maxScale
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.review_photo_zoom_in)
                )
            }

            IconButton(
                onClick = { zoomPanState.reset() }
            ) {
                Icon(
                    imageVector = Icons.Default.FitScreen,
                    contentDescription = stringResource(R.string.review_photo_zoom_reset)
                )
            }
        }
    }
}
