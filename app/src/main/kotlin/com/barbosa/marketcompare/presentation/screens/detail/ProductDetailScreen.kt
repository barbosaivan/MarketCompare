package com.barbosa.marketcompare.presentation.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductDetailData
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.presentation.components.StoreComparisonRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreen(
    detailData: ProductDetailData?,
    onBackClick: () -> Unit,
    onSaveProduct: (updatedProduct: Product, previousName: String) -> Unit,
    onUpdateObservation: (observationId: String, newPrice: Money, newQuantity: ProductQuantity, newStoreName: String) -> Unit,
    onDeleteObservation: (observationId: String) -> Unit,
    onAddObservationClick: () -> Unit,
    onAddToList: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var observationToEdit by remember { mutableStateOf<ProductPriceObservationItem?>(null) }
    var observationToDelete by remember { mutableStateOf<ProductPriceObservationItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_detail_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    if (detailData != null) {
                        IconButton(onClick = { isEditing = !isEditing }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = stringResource(R.string.product_detail_edit_action))
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        if (detailData == null) {
            ProductNotFoundView(modifier = Modifier.padding(innerPadding))
        } else {
            ProductDetailContent(
                detailData = detailData,
                isEditing = isEditing,
                onToggleEdit = { isEditing = it },
                onSaveProduct = { onSaveProduct(it, detailData.product.canonicalName) },
                onEditObservation = { observationToEdit = it },
                onDeleteObservation = { observationToDelete = it },
                onAddObservationClick = onAddObservationClick,
                onAddToList = onAddToList,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }

    observationToEdit?.let { obs ->
        EditObservationDialog(
            observation = obs,
            onDismiss = { observationToEdit = null },
            onConfirm = { newPrice, newQuantity, newStore ->
                onUpdateObservation(obs.id, newPrice, newQuantity, newStore)
                observationToEdit = null
            }
        )
    }

    observationToDelete?.let { obs ->
        DeleteObservationConfirmDialog(
            onDismiss = { observationToDelete = null },
            onConfirm = {
                onDeleteObservation(obs.id)
                observationToDelete = null
            }
        )
    }
}

@Composable
private fun ProductDetailContent(
    detailData: ProductDetailData,
    isEditing: Boolean,
    onToggleEdit: (Boolean) -> Unit,
    onSaveProduct: (Product) -> Unit,
    onEditObservation: (ProductPriceObservationItem) -> Unit,
    onDeleteObservation: (ProductPriceObservationItem) -> Unit,
    onAddObservationClick: () -> Unit,
    onAddToList: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ProductDetailOverviewCard(
                product = detailData.product,
                comparison = detailData.comparisonSummary
            )
        }
        if (isEditing) {
            item {
                ProductDetailEditForm(
                    product = detailData.product,
                    onSave = { updated ->
                        onSaveProduct(updated)
                        onToggleEdit(false)
                    },
                    onCancel = { onToggleEdit(false) }
                )
            }
        }
        item {
            ObservationSectionHeader(onAddObservationClick = onAddObservationClick)
        }
        if (detailData.observations.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.product_detail_empty_observations),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(detailData.observations, key = { it.id }) { obs ->
                ProductObservationItemRow(
                    observation = obs,
                    onEdit = { onEditObservation(obs) },
                    onDelete = { onDeleteObservation(obs) }
                )
            }
        }
        detailData.comparisonSummary?.let { comp ->
            if (comp.storeEntries.size > 1) {
                item {
                    Text(
                        text = stringResource(R.string.compare_sheet_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                items(comp.storeEntries) { entry ->
                    StoreComparisonRow(
                        rank = comp.storeEntries.indexOf(entry) + 1,
                        entry = entry
                    )
                }
            }
        }
        item {
            Button(
                onClick = { onAddToList(detailData.product.canonicalName) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.compare_sheet_add_to_list))
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ObservationSectionHeader(
    onAddObservationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.product_detail_observations_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedButton(onClick = onAddObservationClick, shape = RoundedCornerShape(8.dp)) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(stringResource(R.string.product_detail_add_observation))
        }
    }
}

@Composable
private fun ProductNotFoundView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.product_detail_not_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

