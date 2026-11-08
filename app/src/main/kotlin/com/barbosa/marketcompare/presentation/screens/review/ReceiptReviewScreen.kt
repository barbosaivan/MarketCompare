package com.barbosa.marketcompare.presentation.screens.review

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.domain.model.ReceiptItem
import com.barbosa.marketcompare.domain.model.Store
import com.barbosa.marketcompare.presentation.components.EditableReceiptItemCard
import com.barbosa.marketcompare.presentation.components.ReceiptReviewBottomBar
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
            ReceiptReviewBottomBar(
                computedTotal = computedTotal,
                totalSavings = totalSavings,
                canSave = includedItems.isNotEmpty() && storeName.isNotBlank(),
                onSaveClick = {
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
                }
            )
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
