package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.UnitType

@Composable
fun StoreSelectionSection(
    storeName: String,
    knownStores: List<String>,
    onStoreNameChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = storeName,
            onValueChange = onStoreNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.manual_entry_store_label)) },
            placeholder = { Text(stringResource(R.string.manual_entry_store_hint)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true
        )
        val defaultQuickStores = listOf("Éxito", "D1", "Ara", "Carulla", "Jumbo", "Olímpica")
        val chipsList = (knownStores + defaultQuickStores).distinct().take(6)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chipsList.forEach { store ->
                FilterChip(
                    selected = storeName.equals(store, ignoreCase = true),
                    onClick = { onStoreNameChange(store) },
                    label = { Text(store, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }
    }
}

@Composable
fun ProductDetailsSection(
    productName: String,
    brand: String,
    onProductNameChange: (String) -> Unit,
    onBrandChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = productName,
            onValueChange = onProductNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.manual_entry_product_label)) },
            placeholder = { Text(stringResource(R.string.manual_entry_product_hint)) },
            singleLine = true
        )
        OutlinedTextField(
            value = brand,
            onValueChange = onBrandChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.manual_entry_brand_label)) },
            placeholder = { Text(stringResource(R.string.manual_entry_brand_hint)) },
            singleLine = true
        )
    }
}

@Composable
fun PriceAndQuantitySection(
    priceText: String,
    quantityText: String,
    selectedUnit: UnitType,
    onPriceChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitSelect: (UnitType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = priceText,
            onValueChange = { input -> onPriceChange(input.filter { it.isDigit() }) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.manual_entry_price_label)) },
            placeholder = { Text(stringResource(R.string.manual_entry_price_hint)) },
            prefix = { Text("$ ") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = quantityText,
                onValueChange = { input -> onQuantityChange(input.filter { it.isDigit() }) },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.manual_entry_quantity_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            UnitSelectorRow(
                modifier = Modifier.weight(2f),
                selectedUnit = selectedUnit,
                onUnitSelect = onUnitSelect
            )
        }
    }
}

@Composable
fun UnitSelectorRow(
    modifier: Modifier = Modifier,
    selectedUnit: UnitType,
    onUnitSelect: (UnitType) -> Unit
) {
    val units = listOf(
        UnitType.UNIT to "und",
        UnitType.GRAM to "g",
        UnitType.KILOGRAM to "kg",
        UnitType.MILLILITER to "ml",
        UnitType.LITER to "L"
    )
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        units.forEach { (unitType, label) ->
            FilterChip(
                selected = selectedUnit == unitType,
                onClick = { onUnitSelect(unitType) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
fun PresentationAndDiscountSection(
    presentation: String,
    isDiscounted: Boolean,
    regularPriceText: String,
    onPresentationChange: (String) -> Unit,
    onDiscountToggle: (Boolean) -> Unit,
    onRegularPriceChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = presentation,
            onValueChange = onPresentationChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.manual_entry_presentation_label)) },
            placeholder = { Text(stringResource(R.string.manual_entry_presentation_hint)) },
            singleLine = true
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.manual_entry_discount_switch),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = isDiscounted,
                onCheckedChange = onDiscountToggle
            )
        }
        if (isDiscounted) {
            OutlinedTextField(
                value = regularPriceText,
                onValueChange = { input -> onRegularPriceChange(input.filter { it.isDigit() }) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.manual_entry_regular_price_label)) },
                prefix = { Text("$ ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }
    }
}

@Composable
fun ManualEntryActions(
    isFormValid: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(onClick = onCancel) {
            Text(stringResource(R.string.action_cancel))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onSave,
            enabled = isFormValid
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.padding(end = 6.dp)
            )
            Text(stringResource(R.string.manual_entry_save_action))
        }
    }
}

