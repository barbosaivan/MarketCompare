package com.barbosa.marketcompare.presentation.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.UnitType

@Composable
fun EditObservationDialog(
    observation: ProductPriceObservationItem,
    onDismiss: () -> Unit,
    onConfirm: (newPrice: Money, newQuantity: ProductQuantity, newStoreName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var storeName by remember { mutableStateOf(observation.storeName) }
    var priceText by remember { mutableStateOf(observation.unitPrice.amountMinor.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.product_detail_edit_observation),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text(stringResource(R.string.product_detail_store_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = {
                        priceText = it.filter { char -> char.isDigit() }
                        isError = priceText.toLongOrNull()?.let { p -> p <= 0 } ?: true
                    },
                    label = { Text(stringResource(R.string.product_detail_price_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val priceMinor = priceText.toLongOrNull() ?: 0L
                    if (priceMinor > 0L && storeName.isNotBlank()) {
                        onConfirm(
                            Money(priceMinor, "COP"),
                            ProductQuantity(1L, UnitType.UNIT),
                            storeName.trim()
                        )
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.product_detail_save_action))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(stringResource(R.string.product_detail_cancel_action))
            }
        },
        modifier = modifier
    )
}

