package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.domain.model.OcrSemanticCategory
import com.barbosa.marketcompare.presentation.theme.semanticColors

/**
 * Barra horizontal de chips de filtrado para activar/desactivar capas semánticas individuales (MC-056).
 */
@Composable
fun OcrCategoryFilterBar(
    visibleCategories: Set<OcrSemanticCategory>,
    onCategoryToggle: (OcrSemanticCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    val semanticColors = MaterialTheme.semanticColors
    val scrollState = rememberScrollState()

    val filterableCategories = listOf(
        OcrSemanticCategory.STORE_HEADER,
        OcrSemanticCategory.PRODUCT_NAME,
        OcrSemanticCategory.PRICE,
        OcrSemanticCategory.QUANTITY,
        OcrSemanticCategory.DISCOUNT
    )

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filterableCategories.forEach { category ->
            val isSelected = category in visibleCategories
            val categoryColor = OcrCategoryColors.getColor(category, semanticColors)

            FilterChip(
                selected = isSelected,
                onClick = { onCategoryToggle(category) },
                label = {
                    Text(
                        text = stringResource(OcrCategoryColors.getLabelResId(category)),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(categoryColor)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = categoryColor.copy(alpha = 0.22f),
                    selectedLabelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}
