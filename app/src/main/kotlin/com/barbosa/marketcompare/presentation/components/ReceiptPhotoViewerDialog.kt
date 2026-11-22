package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory

/**
 * Diálogo inmersivo de pantalla completa para inspección fotográfica y capas OCR (MC-055, MC-056).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptPhotoViewerDialog(
    imageUri: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    ocrBlocks: List<OcrElementBox> = emptyList(),
    selectedBlockId: String? = null,
    onBlockClick: ((OcrElementBox) -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    overlayContent: @Composable (BoxScope.(zoomPanState: ZoomPanState, imageWidth: Float, imageHeight: Float) -> Unit)? = null
) {
    var showOverlays by rememberSaveable { mutableStateOf(true) }
    var visibleCategories by remember { mutableStateOf(OcrSemanticCategory.entries.toSet()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = Color.Black,
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(R.string.review_photo_viewer_title),
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.action_back),
                                    tint = Color.White
                                )
                            }
                        },
                        actions = {
                            if (ocrBlocks.isNotEmpty()) {
                                IconButton(onClick = { showOverlays = !showOverlays }) {
                                    Icon(
                                        imageVector = if (showOverlays) Icons.Default.Layers else Icons.Default.LayersClear,
                                        contentDescription = stringResource(
                                            if (showOverlays) R.string.ocr_overlay_hide else R.string.ocr_overlay_show
                                        ),
                                        tint = if (showOverlays) Color(0xFF64B5F6) else Color.Gray
                                    )
                                }
                            }
                            actions()
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Black.copy(alpha = 0.85f),
                            titleContentColor = Color.White
                        )
                    )

                    if (showOverlays && ocrBlocks.isNotEmpty()) {
                        OcrCategoryFilterBar(
                            visibleCategories = visibleCategories,
                            onCategoryToggle = { toggled ->
                                visibleCategories = if (toggled in visibleCategories) {
                                    visibleCategories - toggled
                                } else {
                                    visibleCategories + toggled
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ReceiptPhotoViewer(
                    imageUri = imageUri,
                    ocrBlocks = ocrBlocks,
                    showOverlays = showOverlays,
                    visibleCategories = visibleCategories,
                    selectedBlockId = selectedBlockId,
                    onBlockClick = onBlockClick,
                    overlayContent = overlayContent
                )
            }
        }
    }
}
