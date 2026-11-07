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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.domain.model.ReceiptItem
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.model.Store
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptReviewScreen(
    draft: ReceiptDraft,
    onBackClick: () -> Unit,
    onConfirmSave: (Store, Receipt) -> Unit
) {
    val defaultStore = stringResource(R.string.review_default_store)
    var storeName by remember { mutableStateOf(draft.detectedStoreName ?: defaultStore) }
    var items by remember { mutableStateOf(draft.items) }

    val includedItems = items.filter { it.isIncluded }
    val computedTotal = Money(includedItems.sumOf { it.finalPrice.amountMinor }, "COP")

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
    onItemChange: (ReceiptItemDraft) -> Unit
) {
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isIncluded,
                onCheckedChange = { onItemChange(item.copy(isIncluded = it)) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                OutlinedTextField(
                    value = item.rawDescription,
                    onValueChange = { onItemChange(item.copy(rawDescription = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = item.isIncluded,
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium
                )
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.height(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.review_item_discount_tag, item.discount.formatDisplay()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
