package com.barbosa.marketcompare.presentation.screens.shopping

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.BasketEstimate
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.presentation.components.BasketSummaryCard
import com.barbosa.marketcompare.presentation.components.ShoppingListAddCard
import com.barbosa.marketcompare.presentation.components.ShoppingListItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    basketEstimate: BasketEstimate,
    catalogProducts: List<Product>,
    onAddItem: (name: String, productId: String?, quantity: Long) -> Unit,
    onToggleChecked: (itemId: String, isChecked: Boolean) -> Unit,
    onDeleteItem: (itemId: String) -> Unit,
    onClearChecked: () -> Unit
) {
    val hasCheckedItems = basketEstimate.items.any { it.isChecked }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.shopping_list_title),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
                    if (hasCheckedItems) {
                        TextButton(onClick = onClearChecked) {
                            Text(stringResource(R.string.shopping_list_clear_checked))
                        }
                    }
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
                BasketSummaryCard(estimate = basketEstimate)
            }

            item {
                ShoppingListAddCard(
                    catalogProducts = catalogProducts,
                    onAddItem = onAddItem
                )
            }

            if (basketEstimate.items.isEmpty()) {
                item {
                    ShoppingListEmptyState()
                }
            } else {
                items(basketEstimate.items, key = { it.id }) { item ->
                    ShoppingListItemRow(
                        item = item,
                        onToggleChecked = { onToggleChecked(item.id, it) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShoppingListEmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.shopping_list_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
