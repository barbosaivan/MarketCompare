package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.normalizer.ProductNormalizer
import com.barbosa.marketcompare.domain.parser.DiscountClassifier

@Composable
fun EditableReceiptItemCard(
    item: ReceiptItemDraft,
    catalogProducts: List<Product>,
    onItemChange: (ReceiptItemDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDiscountDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
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

                if (item.confidenceScore < 0.8f) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.review_confidence_low),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (item.discount != null && item.discount.amountMinor > 0) {
                    ReceiptDiscountRow(
                        item = item,
                        onOpenDiscountDialog = { showDiscountDialog = true }
                    )
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

@Composable
private fun ReceiptDiscountRow(
    item: ReceiptItemDraft,
    onOpenDiscountDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = Modifier.height(4.dp))
    Row(
        modifier = modifier.fillMaxWidth(),
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
                text = stringResource(R.string.review_item_discount_tag, item.discount?.formatDisplay() ?: ""),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.width(6.dp))
            SuggestionChip(
                onClick = onOpenDiscountDialog,
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
            onClick = onOpenDiscountDialog,
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

