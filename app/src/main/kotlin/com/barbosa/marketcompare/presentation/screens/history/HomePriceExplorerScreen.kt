package com.barbosa.marketcompare.presentation.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.presentation.components.PriceKpiSummaryCard
import com.barbosa.marketcompare.presentation.components.PriceSearchBar
import com.barbosa.marketcompare.presentation.components.ProductCrossStoreComparisonSheet
import com.barbosa.marketcompare.presentation.components.ProductPriceObservationCard
import com.barbosa.marketcompare.presentation.components.RecentReceiptsCollapsibleSection
import com.barbosa.marketcompare.presentation.components.StoreFilterChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePriceExplorerScreen(
    priceObservations: List<ProductPriceObservationItem>,
    stores: List<String>,
    receipts: List<Receipt>,
    onScanClick: () -> Unit,
    onReceiptClick: (String) -> Unit,
    onProductClick: (String) -> Unit = {},
    comparisonSummary: ProductComparisonSummary? = null,
    onDismissComparison: () -> Unit = {},
    onAddToList: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStore by rememberSaveable { mutableStateOf<String?>(null) }
    var isReceiptsExpanded by rememberSaveable { mutableStateOf(false) }

    val filteredObservations = remember(priceObservations, searchQuery, selectedStore) {
        priceObservations.filter { item ->
            val matchesStore = selectedStore == null || item.storeName.equals(selectedStore, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || item.productName.contains(searchQuery.trim(), ignoreCase = true)
            matchesStore && matchesQuery
        }
    }

    val monitoredCount = remember(priceObservations) {
        priceObservations.map { it.productName.trim().lowercase() }.distinct().size
    }
    val storesCount = remember(stores, priceObservations) {
        (stores + priceObservations.map { it.storeName }).filter { it.isNotBlank() }.distinct().size
    }
    val bestDealPrice = remember(filteredObservations) {
        filteredObservations.minByOrNull { it.unitPrice.amountMinor }?.unitPrice?.formatDisplay()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_price_explorer_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScanClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_fab_scan)
                )
            }
        }
    ) { innerPadding ->
        if (priceObservations.isEmpty() && receipts.isEmpty()) {
            PriceExplorerEmptyView(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PriceSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }
                if (stores.isNotEmpty()) {
                    item {
                        StoreFilterChips(
                            stores = stores,
                            selectedStore = selectedStore,
                            onStoreSelected = { selectedStore = it }
                        )
                    }
                }
                item {
                    PriceKpiSummaryCard(
                        monitoredCount = monitoredCount,
                        storesCount = storesCount,
                        bestDealPrice = bestDealPrice
                    )
                }
                if (filteredObservations.isEmpty()) {
                    item {
                        SearchEmptyView(query = searchQuery)
                    }
                } else {
                    items(filteredObservations, key = { it.id }) { item ->
                        ProductPriceObservationCard(
                            observation = item,
                            onClick = { onProductClick(item.productName) }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    RecentReceiptsCollapsibleSection(
                        receipts = receipts,
                        isExpanded = isReceiptsExpanded,
                        onToggleExpanded = { isReceiptsExpanded = !isReceiptsExpanded },
                        onReceiptClick = onReceiptClick
                    )
                }
            }
        }

        if (comparisonSummary != null) {
            ProductCrossStoreComparisonSheet(
                summary = comparisonSummary,
                onDismiss = onDismissComparison,
                onAddToList = onAddToList
            )
        }
    }
}

@Composable
private fun PriceExplorerEmptyView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.height(72.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_empty_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SearchEmptyView(
    query: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.home_empty_search, query),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

