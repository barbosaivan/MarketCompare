package com.barbosa.marketcompare.presentation.screens.store

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.presentation.components.ManualPriceEntrySheet
import com.barbosa.marketcompare.presentation.components.PriceSearchBar
import com.barbosa.marketcompare.presentation.components.ProductCrossStoreComparisonSheet
import com.barbosa.marketcompare.presentation.components.ProductPriceObservationCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreCatalogScreen(
    storeName: String,
    products: List<ProductPriceObservationItem>,
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    comparisonSummary: ProductComparisonSummary? = null,
    onDismissComparison: () -> Unit = {},
    onAddToList: (String) -> Unit = {},
    onEditProductClick: (String) -> Unit = {},
    onSaveManualPrice: (
        storeName: String,
        productName: String,
        price: Money,
        quantity: ProductQuantity,
        brand: String?,
        presentation: String?,
        discount: Money?,
        discountType: DiscountType
    ) -> Unit = { _, _, _, _, _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showManualEntrySheet by rememberSaveable { mutableStateOf(false) }

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) {
            products
        } else {
            products.filter { it.productName.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = storeName,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showManualEntrySheet = true },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.store_catalog_add_price_button),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { innerPadding ->
        if (products.isEmpty()) {
            StoreCatalogEmptyView(
                message = stringResource(R.string.store_catalog_empty),
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    StoreHeaderInfoCard(
                        storeName = storeName,
                        totalProducts = products.size
                    )
                }
                item {
                    PriceSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                }
                if (filteredProducts.isEmpty()) {
                    item {
                        StoreCatalogEmptyView(
                            message = stringResource(R.string.store_catalog_empty_search, searchQuery)
                        )
                    }
                } else {
                    items(filteredProducts, key = { it.id }) { item ->
                        ProductPriceObservationCard(
                            observation = item,
                            onClick = { onProductClick(item.productName) }
                        )
                    }
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
                initialStoreName = storeName,
                onDismiss = { showManualEntrySheet = false },
                onSavePrice = { store, prod, price, qty, brand, pres, disc, discType ->
                    showManualEntrySheet = false
                    onSaveManualPrice(store, prod, price, qty, brand, pres, disc, discType)
                }
            )
        }
    }
}

@Composable
private fun StoreHeaderInfoCard(
    storeName: String,
    totalProducts: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = storeName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.store_catalog_products_count, totalProducts),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StoreCatalogEmptyView(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
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
                modifier = Modifier.height(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

