package com.barbosa.marketcompare.presentation.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.UnitType
import com.barbosa.marketcompare.presentation.components.UnitSelectorRow

@Composable
fun ProductDetailEditForm(
    product: Product,
    onSave: (Product) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(product.canonicalName) }
    var brand by remember { mutableStateOf(product.brand.orEmpty()) }
    var category by remember { mutableStateOf(product.category.orEmpty()) }
    var baseQtyText by remember { mutableStateOf(product.baseQuantity.toString()) }
    var selectedUnit by remember { mutableStateOf(product.unit) }
    var presentation by remember { mutableStateOf(product.presentation.orEmpty()) }
    var isNameError by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.product_detail_edit_action),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isNameError = it.isBlank()
                },
                label = { Text(stringResource(R.string.product_detail_name_label)) },
                isError = isNameError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = brand,
                onValueChange = { brand = it },
                label = { Text(stringResource(R.string.product_detail_brand_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            ClassificationSelector(
                selectedCategory = category.ifBlank { null },
                onCategorySelected = { category = it }
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = baseQtyText,
                    onValueChange = { baseQtyText = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.product_detail_base_quantity_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = presentation,
                    onValueChange = { presentation = it },
                    label = { Text(stringResource(R.string.product_detail_presentation_label)) },
                    singleLine = true,
                    modifier = Modifier.weight(1.5f)
                )
            }
            UnitSelectorRow(
                selectedUnit = selectedUnit,
                onUnitSelect = { selectedUnit = it }
            )
            EditFormActions(
                onCancel = onCancel,
                onSave = {
                    if (name.isNotBlank()) {
                        onSave(
                            product.copy(
                                canonicalName = name.trim(),
                                brand = brand.trim().ifBlank { null },
                                category = category.trim().ifBlank { null },
                                baseQuantity = baseQtyText.toLongOrNull()?.coerceAtLeast(1L) ?: 1L,
                                unit = selectedUnit,
                                presentation = presentation.trim().ifBlank { null }
                            )
                        )
                    } else {
                        isNameError = true
                    }
                }
            )
        }
    }
}

@Composable
private fun EditFormActions(
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.product_detail_cancel_action))
        }
        Button(onClick = onSave, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.product_detail_save_action))
        }
    }
}

