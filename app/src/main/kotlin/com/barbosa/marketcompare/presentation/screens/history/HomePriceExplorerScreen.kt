package com.barbosa.marketcompare.presentation.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.presentation.components.DeleteProductConfirmationDialog
import com.barbosa.marketcompare.presentation.components.DeleteStoreConfirmationDialog
import com.barbosa.marketcompare.presentation.components.HomeStoreSectionHeader
import com.barbosa.marketcompare.presentation.components.ManualPriceEntrySheet
import com.barbosa.marketcompare.presentation.components.PriceExplorerEmptyView
import com.barbosa.marketcompare.presentation.components.PriceSearchBar
import com.barbosa.marketcompare.presentation.components.ProductCrossStoreComparisonSheet
import com.barbosa.marketcompare.presentation.components.ProductPriceObservationCard
import com.barbosa.marketcompare.presentation.components.RecentReceiptsCollapsibleSection
import com.barbosa.marketcompare.presentation.components.SearchEmptyView
import com.barbosa.marketcompare.presentation.components.StoreCatalogBanner
import com.barbosa.marketcompare.presentation.components.StoreFilterChips

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePriceExplorerScreen(
    priceObservations: List<ProductPriceObservationItem>,
    stores: List<String>,
    receipts: List<Receipt>,
    onScanClick: () -> Unit,
    onReceiptClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onProductClick: (String) -> Unit = {},
    comparisonSummary: ProductComparisonSummary? = null,
    onDismissComparison: () -> Unit = {},
    onAddToList: (String) -> Unit = {},
    onEditProductClick: (String) -> Unit = {},
    onNavigateToStoreCatalog: (String) -> Unit = {},
    onDeleteObservation: (String) -> Unit = {},
    onDeleteStore: (String) -> Unit = {},
    onSaveManualPrice: (
        storeName: String,
        productName: String,
        price: Money,
        quantity: ProductQuantity,
        brand: String?,
        presentation: String?,
        discount: Money?,
        discountType: DiscountType
    ) -> Unit = { _, _, _, _, _, _, _, _ -> }
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStore by rememberSaveable { mutableStateOf<String?>(null) }
    var isReceiptsExpanded by rememberSaveable { mutableStateOf(false) }
    var showManualEntrySheet by rememberSaveable { mutableStateOf(false) }
    var observationToDelete by remember { mutableStateOf<ProductPriceObservationItem?>(null) }
    var storeToDelete by remember { mutableStateOf<String?>(null) }

    val filteredObservations = remember(priceObservations, searchQuery, selectedStore) {
        priceObservations.filter { item ->
            val matchesStore = selectedStore == null || item.storeName.equals(selectedStore, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || item.productName.contains(searchQuery.trim(), ignoreCase = true)
            matchesStore && matchesQuery
        }
    }

    val groupedByStore = remember(filteredObservations) {
        filteredObservations.groupBy { it.storeName }
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
                onClick = { showManualEntrySheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.home_fab_manual_entry)
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            onStoreSelected = { selectedStore = it },
                            onStoreLongClick = { storeToDelete = it }
                        )
                    }
                }

                if (filteredObservations.isEmpty()) {
                    item {
                        SearchEmptyView(query = searchQuery)
                    }
                } else if (selectedStore != null) {
                    val currentStore = selectedStore!!
                    item(key = "banner_$currentStore") {
                        StoreCatalogBanner(
                            storeName = currentStore,
                            onClick = { onNavigateToStoreCatalog(currentStore) }
                        )
                    }
                    items(filteredObservations, key = { it.id }) { item ->
                        ProductPriceObservationCard(
                            observation = item,
                            onClick = { onProductClick(item.productName) },
                            onLongClick = { observationToDelete = item }
                        )
                    }
                } else {
                    groupedByStore.forEach { (storeName, obsList) ->
                        item(key = "store_header_$storeName") {
                            HomeStoreSectionHeader(
                                storeName = storeName,
                                productCount = obsList.size,
                                onClick = { onNavigateToStoreCatalog(storeName) },
                                onLongClick = { storeToDelete = storeName }
                            )
                        }
                        items(obsList, key = { it.id }) { item ->
                            ProductPriceObservationCard(
                                observation = item,
                                onClick = { onProductClick(item.productName) },
                                onLongClick = { observationToDelete = item }
                            )
                        }
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
                onAddToList = onAddToList,
                onEditProductClick = onEditProductClick
            )
        }

        if (showManualEntrySheet) {
            ManualPriceEntrySheet(
                knownStores = stores,
                onDismiss = { showManualEntrySheet = false },
                onSavePrice = { store, prod, price, qty, brand, pres, disc, discType ->
                    showManualEntrySheet = false
                    onSaveManualPrice(store, prod, price, qty, brand, pres, disc, discType)
                }
            )
        }

        observationToDelete?.let { obs ->
            DeleteProductConfirmationDialog(
                observation = obs,
                onConfirm = {
                    onDeleteObservation(obs.id)
                    observationToDelete = null
                },
                onDismiss = { observationToDelete = null }
            )
        }

        storeToDelete?.let { storeName ->
            DeleteStoreConfirmationDialog(
                storeName = storeName,
                onConfirm = {
                    onDeleteStore(storeName)
                    if (selectedStore.equals(storeName, ignoreCase = true)) {
                        selectedStore = null
                    }
                    storeToDelete = null
                },
                onDismiss = { storeToDelete = null }
            )
        }
    }
}
