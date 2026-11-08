package com.barbosa.marketcompare.presentation.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.domain.model.ReceiptItem
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.model.Store
import com.barbosa.marketcompare.domain.normalizer.ProductNormalizer
import com.barbosa.marketcompare.domain.parser.DiscountClassifier
import com.barbosa.marketcompare.presentation.components.ProductAutocompleteField
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptReviewScreen(
    draft: ReceiptDraft,
    catalogProducts: List<Product> = emptyList(),
    onBackClick: () -> Unit,
    onConfirmSave: (Store, Receipt) -> Unit
) {
    val defaultStore = stringResource(R.string.review_default_store)
    var storeName by remember { mutableStateOf(draft.detectedStoreName ?: defaultStore) }
    var items by remember { mutableStateOf(draft.items) }

    val includedItems = items.filter { it.isIncluded }
    val computedTotal = Money(includedItems.sumOf { it.finalPrice.amountMinor }, "COP")
    val totalSavings = Money(includedItems.sumOf { it.discount?.amountMinor ?: 0L }, "COP")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.review_title), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (totalSavings.amountMinor > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.review_total_savings_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = stringResource(R.string.review_total_savings_value, totalSavings.formatDisplay()),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.review_total_label),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = computedTotal.formatDisplay(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val storeId = UUID.randomUUID().toString()
                            val receiptId = UUID.randomUUID().toString()

                            val store = Store(
                                id = storeId,
                                name = storeName.trim(),
                                detectedTaxId = draft.detectedTaxId
                            )

                            val receiptItems = includedItems.map { draftItem ->
                                ReceiptItem(
                                    id = UUID.randomUUID().toString(),
                                    receiptId = receiptId,
                                    productId = draftItem.linkedProductId,
                                    rawDescription = draftItem.rawDescription.trim(),
                                    quantity = draftItem.quantity,
                                    listPrice = draftItem.unitListPrice,
                                    discount = draftItem.discount,
                                    finalPrice = draftItem.finalPrice,
                                    discountType = draftItem.discountType,
                                    discountEvidence = draftItem.discountEvidence,
                                    confidenceScore = draftItem.confidenceScore,
                                    userConfirmed = true
                                )
                            }

                            val receipt = Receipt(
                                id = receiptId,
                                storeId = storeId,
                                storeName = storeName.trim(),
                                purchaseTimestamp = draft.detectedDateTimestamp,
                                totalAmount = computedTotal,
                                rawDocumentUri = draft.rawDocumentUri,
                                items = receiptItems
                            )

                            onConfirmSave(store, receipt)
                        },
                        enabled = includedItems.isNotEmpty() && storeName.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.review_save_purchase))
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(stringResource(R.string.review_store_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = stringResource(R.string.review_items_detected, items.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                EditableReceiptItemCard(
                    item = item,
                    catalogProducts = catalogProducts,
                    onItemChange = { updated ->
                        items = items.toMutableList().also { it[index] = updated }
                    }
                )
            }
        }
    }
}

@Composable
fun EditableReceiptItemCard(
    item: ReceiptItemDraft,
    catalogProducts: List<Product>,
    onItemChange: (ReceiptItemDraft) -> Unit
) {
    var showDiscountDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isIncluded) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = item.isIncluded,
                onCheckedChange = { onItemChange(item.copy(isIncluded = it)) },
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Campo con autocompletado y validación estricta de variantes
                ProductAutocompleteField(
                    query = item.rawDescription,
                    onQueryChange = {
                        onItemChange(item.copy(rawDescription = it, linkedProductId = null))
                    },
                    catalogProducts = catalogProducts,
                    referenceCandidate = ProductNormalizer.normalize(item.rawDescription),
                    onProductSelected = { selected ->
                        onItemChange(
                            item.copy(
                                rawDescription = selected.canonicalName,
                                linkedProductId = selected.id,
                                linkedCanonicalName = selected.canonicalName
                            )
                        )
                    },
                    enabled = item.isIncluded,
                    modifier = Modifier.fillMaxWidth()
                )

                if (item.linkedProductId != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.autocomplete_badge_catalog),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.quantity.formatDisplay(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.finalPrice.formatDisplay(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (item.discount != null && item.discount.amountMinor > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalOffer,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.review_item_discount_tag, item.discount.formatDisplay()),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            SuggestionChip(
                                onClick = { showDiscountDialog = true },
                                label = {
                                    Text(
                                        text = stringResource(DiscountClassifier.getLabelResId(item.discountType)),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                ),
                                border = null,
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        TextButton(
                            onClick = { showDiscountDialog = true },
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.review_edit_discount),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    if (!item.discountEvidence.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.review_discount_evidence, item.discountEvidence),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }

    if (showDiscountDialog) {
        DiscountAuditDialog(
            item = item,
            onDismiss = { showDiscountDialog = false },
            onSaveDiscount = { newDiscount, newType ->
                val basePrice = item.unitListPrice ?: (item.finalPrice + (item.discount ?: Money(0L, "COP")))
                val newFinalPrice = if (newDiscount != null && basePrice.amountMinor > newDiscount.amountMinor) {
                    basePrice - newDiscount
                } else {
                    basePrice
                }
                onItemChange(
                    item.copy(
                        discount = newDiscount,
                        discountType = newType,
                        finalPrice = newFinalPrice
                    )
                )
                showDiscountDialog = false
            },
            onRemoveDiscount = {
                val basePrice = item.unitListPrice ?: (item.finalPrice + (item.discount ?: Money(0L, "COP")))
                onItemChange(
                    item.copy(
                        discount = null,
                        discountType = DiscountType.NONE,
                        discountEvidence = null,
                        finalPrice = basePrice
                    )
                )
                showDiscountDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountAuditDialog(
    item: ReceiptItemDraft,
    onDismiss: () -> Unit,
    onSaveDiscount: (Money?, DiscountType) -> Unit,
    onRemoveDiscount: () -> Unit
) {
    var discountText by remember {
        mutableStateOf(item.discount?.amountMinor?.toString() ?: "0")
    }
    var selectedType by remember {
        mutableStateOf(if (item.discountType == DiscountType.NONE) DiscountType.EXPLICIT_LINE else item.discountType)
    }
    var isTypeMenuExpanded by remember { mutableStateOf(false) }

    val selectableTypes = remember {
        listOf(
            DiscountType.EXPLICIT_LINE,
            DiscountType.LOYALTY_MEMBER,
            DiscountType.PROMOTION_2X1,
            DiscountType.DERIVED,
            DiscountType.UNKNOWN
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.review_dialog_discount_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!item.discountEvidence.isNullOrBlank()) {
                    Column {
                        Text(
                            text = stringResource(R.string.review_dialog_discount_evidence_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.discountEvidence,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }

                // Selector de tipo de descuento
                ExposedDropdownMenuBox(
                    expanded = isTypeMenuExpanded,
                    onExpandedChange = { isTypeMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = stringResource(DiscountClassifier.getLabelResId(selectedType)),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.review_dialog_discount_type)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTypeMenuExpanded) },
                        modifier = Modifier
                            .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isTypeMenuExpanded,
                        onDismissRequest = { isTypeMenuExpanded = false }
                    ) {
                        selectableTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(stringResource(DiscountClassifier.getLabelResId(type))) },
                                onClick = {
                                    selectedType = type
                                    isTypeMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Campo para valor del descuento
                OutlinedTextField(
                    value = discountText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() }) {
                            discountText = input
                        }
                    },
                    label = { Text(stringResource(R.string.review_dialog_discount_amount)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = discountText.toLongOrNull() ?: 0L
                    if (parsed > 0) {
                        onSaveDiscount(Money(parsed, "COP"), selectedType)
                    } else {
                        onRemoveDiscount()
                    }
                }
            ) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onRemoveDiscount) {
                    Text(
                        text = stringResource(R.string.review_dialog_discount_remove),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        }
    )
}
