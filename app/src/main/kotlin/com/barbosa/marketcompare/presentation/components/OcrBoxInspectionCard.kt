package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.OcrElementBox
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.presentation.theme.semanticColors

/**
 * Panel de inspección y reasignación táctil para cajas de texto OCR seleccionadas (MC-057).
 */
@Composable
fun OcrBoxInspectionCard(
    box: OcrElementBox,
    items: List<ReceiptItemDraft>,
    onAssignStore: (String) -> Unit,
    onAssignDescription: (ReceiptItemDraft, String) -> Unit,
    onAssignPrice: (ReceiptItemDraft, String) -> Unit,
    onAssignQuantity: (ReceiptItemDraft, String) -> Unit,
    onAssignDiscount: (ReceiptItemDraft, String) -> Unit,
    onCreateNewItem: (OcrElementBox) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val initialIndex = remember(box.id, items) {
        val matched = items.indexOfFirst { it.id == box.associatedItemId }
        if (matched >= 0) matched else 0
    }
    var selectedItemIndex by remember(box.id, items) { mutableIntStateOf(initialIndex) }
    val currentTargetItem = items.getOrNull(selectedItemIndex)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OcrBoxHeaderRow(box = box, onDismiss = onDismiss)

            OcrBoxTextSurface(text = box.text)

            if (items.isNotEmpty()) {
                OcrItemSelectorRow(
                    items = items,
                    selectedIndex = selectedItemIndex,
                    onSelectIndex = { selectedItemIndex = it }
                )
            }

            OcrBoxActionChipsRow(
                box = box,
                targetItem = currentTargetItem,
                onAssignStore = onAssignStore,
                onAssignDescription = onAssignDescription,
                onAssignPrice = onAssignPrice,
                onAssignQuantity = onAssignQuantity,
                onAssignDiscount = onAssignDiscount,
                onCreateNewItem = onCreateNewItem
            )
        }
    }
}

@Composable
private fun OcrBoxHeaderRow(
    box: OcrElementBox,
    onDismiss: () -> Unit
) {
    val semanticColors = MaterialTheme.semanticColors
    val categoryColor = OcrCategoryColors.getColor(box.category, semanticColors)
    val categoryLabel = stringResource(OcrCategoryColors.getLabelResId(box.category))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = categoryColor.copy(alpha = 0.2f)
        ) {
            Text(
                text = categoryLabel,
                color = categoryColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.ocr_assign_dismiss),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OcrBoxTextSurface(text: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun OcrItemSelectorRow(
    items: List<ReceiptItemDraft>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.ocr_assign_item_selector),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        items.forEachIndexed { index, item ->
            val label = "#${index + 1} ${item.rawDescription.take(14)}"
            FilterChip(
                selected = index == selectedIndex,
                onClick = { onSelectIndex(index) },
                label = { Text(text = label, maxLines = 1) }
            )
        }
    }
}

@Composable
private fun OcrBoxActionChipsRow(
    box: OcrElementBox,
    targetItem: ReceiptItemDraft?,
    onAssignStore: (String) -> Unit,
    onAssignDescription: (ReceiptItemDraft, String) -> Unit,
    onAssignPrice: (ReceiptItemDraft, String) -> Unit,
    onAssignQuantity: (ReceiptItemDraft, String) -> Unit,
    onAssignDiscount: (ReceiptItemDraft, String) -> Unit,
    onCreateNewItem: (OcrElementBox) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssistChip(
            onClick = { onAssignStore(box.text) },
            label = { Text(stringResource(R.string.ocr_assign_to_store)) },
            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        )

        targetItem?.let { item ->
            AssistChip(
                onClick = { onAssignDescription(item, box.text) },
                label = { Text(stringResource(R.string.ocr_assign_desc)) },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )

            AssistChip(
                onClick = { onAssignPrice(item, box.text) },
                label = { Text(stringResource(R.string.ocr_assign_price)) },
                leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) }
            )

            AssistChip(
                onClick = { onAssignQuantity(item, box.text) },
                label = { Text(stringResource(R.string.ocr_assign_qty)) },
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null) }
            )

            AssistChip(
                onClick = { onAssignDiscount(item, box.text) },
                label = { Text(stringResource(R.string.ocr_assign_discount)) },
                leadingIcon = { Icon(Icons.Default.Discount, contentDescription = null) }
            )
        }

        AssistChip(
            onClick = { onCreateNewItem(box) },
            label = { Text(stringResource(R.string.ocr_assign_new_item)) },
            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
        )
    }
}
