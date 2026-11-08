package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ReceiptItemDraft
import com.barbosa.marketcompare.domain.parser.DiscountClassifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscountAuditDialog(
    item: ReceiptItemDraft,
    onDismiss: () -> Unit,
    onSaveDiscount: (Money?, DiscountType) -> Unit,
    onRemoveDiscount: () -> Unit,
    modifier: Modifier = Modifier
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
        modifier = modifier,
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
