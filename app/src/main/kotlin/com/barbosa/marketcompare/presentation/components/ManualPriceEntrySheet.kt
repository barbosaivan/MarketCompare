package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.UnitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualPriceEntrySheet(
    initialStoreName: String = "",
    knownStores: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSavePrice: (
        storeName: String,
        productName: String,
        price: Money,
        quantity: ProductQuantity,
        brand: String?,
        presentation: String?,
        discount: Money?,
        discountType: DiscountType
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var storeName by remember { mutableStateOf(initialStoreName) }
    var productName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("1") }
    var selectedUnit by remember { mutableStateOf(UnitType.UNIT) }
    var presentation by remember { mutableStateOf("") }
    var isDiscounted by remember { mutableStateOf(false) }
    var regularPriceText by remember { mutableStateOf("") }

    val parsedPrice = priceText.toLongOrNull() ?: 0L
    val isFormValid = storeName.isNotBlank() && productName.isNotBlank() && parsedPrice > 0L

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ManualEntryHeader(onDismiss = onDismiss)
            StoreSelectionSection(
                storeName = storeName,
                knownStores = knownStores,
                onStoreNameChange = { storeName = it }
            )
            ProductDetailsSection(
                productName = productName,
                brand = brand,
                onProductNameChange = { productName = it },
                onBrandChange = { brand = it }
            )
            PriceAndQuantitySection(
                priceText = priceText,
                quantityText = quantityText,
                selectedUnit = selectedUnit,
                onPriceChange = { priceText = it },
                onQuantityChange = { quantityText = it },
                onUnitSelect = { selectedUnit = it }
            )
            PresentationAndDiscountSection(
                presentation = presentation,
                isDiscounted = isDiscounted,
                regularPriceText = regularPriceText,
                onPresentationChange = { presentation = it },
                onDiscountToggle = { isDiscounted = it },
                onRegularPriceChange = { regularPriceText = it }
            )
            ManualEntryActions(
                isFormValid = isFormValid,
                onSave = {
                    val finalPrice = Money(parsedPrice, "COP")
                    val regPrice = regularPriceText.toLongOrNull()
                    val discount = if (isDiscounted && regPrice != null && regPrice > parsedPrice) {
                        Money(regPrice - parsedPrice, "COP")
                    } else null
                    val discountType = if (isDiscounted) DiscountType.PROMOTION_2X1 else DiscountType.NONE
                    val qtyValue = quantityText.toLongOrNull()?.coerceAtLeast(1L) ?: 1L
                    val qty = ProductQuantity(qtyValue, selectedUnit)

                    onSavePrice(
                        storeName,
                        productName,
                        finalPrice,
                        qty,
                        brand.ifBlank { null },
                        presentation.ifBlank { null },
                        discount,
                        discountType
                    )
                },
                onCancel = onDismiss
            )
        }
    }
}

@Composable
private fun ManualEntryHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.manual_entry_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.manual_entry_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.action_cancel),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
