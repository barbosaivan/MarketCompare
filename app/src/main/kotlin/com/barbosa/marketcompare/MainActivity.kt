package com.barbosa.marketcompare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.barbosa.marketcompare.domain.model.BasketEstimate
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.presentation.components.AppBottomNavigationBar
import com.barbosa.marketcompare.presentation.navigation.AppNavigator
import com.barbosa.marketcompare.presentation.navigation.Screen
import com.barbosa.marketcompare.presentation.screens.history.HomePriceExplorerScreen
import com.barbosa.marketcompare.presentation.screens.review.ReceiptReviewScreen
import com.barbosa.marketcompare.presentation.screens.scan.ReceiptCaptureScreen
import com.barbosa.marketcompare.presentation.screens.shopping.ShoppingListScreen
import com.barbosa.marketcompare.presentation.screens.store.StoreCatalogScreen
import com.barbosa.marketcompare.presentation.theme.MarketCompareTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MarketCompareApplication
        val repository = app.purchaseRepository

        setContent {
            MarketCompareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()
                    val navigator = remember { AppNavigator() }
                    var activeDraft by remember { mutableStateOf<ReceiptDraft?>(null) }
                    var activeComparison by remember { mutableStateOf<ProductComparisonSummary?>(null) }

                    BackHandler(enabled = navigator.canGoBack || activeComparison != null) {
                        if (activeComparison != null) {
                            activeComparison = null
                        } else {
                            if (navigator.currentRoute == Screen.Review.route) {
                                activeDraft = null
                            }
                            navigator.popBack()
                        }
                    }

                    val receipts by repository.getAllReceipts().collectAsState(initial = emptyList())
                    val priceObservations by repository.getAllPriceObservations().collectAsState(initial = emptyList())
                    val storeNames by repository.getAllStoreNames().collectAsState(initial = emptyList())
                    val products by repository.getAllProducts().collectAsState(initial = emptyList())
                    val basketEstimate by repository.getBasketEstimate().collectAsState(
                        initial = BasketEstimate(
                            items = emptyList(),
                            totalEstimated = Money(0L, "COP"),
                            totalItemsCount = 0,
                            pricedItemsCount = 0,
                            staleItemsCount = 0,
                            unknownItemsCount = 0
                        )
                    )

                    Scaffold(
                        bottomBar = {
                            AppBottomNavigationBar(
                                currentRoute = navigator.currentRoute,
                                onNavigateToHistory = {
                                    navigator.navigateToTab(Screen.History.route)
                                },
                                onNavigateToScan = {
                                    navigator.navigateToTab(Screen.Scan.route)
                                },
                                onNavigateToShoppingList = {
                                    navigator.navigateToTab(Screen.ShoppingList.route)
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            val route = navigator.currentRoute
                            when {
                                route == Screen.History.route -> {
                                    HomePriceExplorerScreen(
                                        priceObservations = priceObservations,
                                        stores = storeNames,
                                        receipts = receipts,
                                        onScanClick = {
                                            navigator.navigateToTab(Screen.Scan.route)
                                        },
                                        onReceiptClick = { receiptId ->
                                            // Detalle de recibo
                                        },
                                        onProductClick = { productName ->
                                            scope.launch {
                                                activeComparison = repository.getCrossStoreComparison(productName)
                                            }
                                        },
                                        comparisonSummary = activeComparison,
                                        onDismissComparison = {
                                            activeComparison = null
                                        },
                                        onAddToList = { productName ->
                                            scope.launch {
                                                repository.addShoppingListItem(name = productName)
                                                activeComparison = null
                                            }
                                        },
                                        onNavigateToStoreCatalog = { storeName ->
                                            navigator.pushScreen(Screen.StoreCatalog.createRoute(storeName))
                                        },
                                        onSaveManualPrice = { store, prod, price, qty, brand, pres, disc, discType ->
                                            scope.launch {
                                                repository.recordManualPriceObservation(
                                                    storeName = store,
                                                    productName = prod,
                                                    price = price,
                                                    quantity = qty,
                                                    brand = brand,
                                                    presentation = pres,
                                                    discount = disc,
                                                    discountType = discType
                                                )
                                            }
                                        }
                                    )
                                }
                                route.startsWith("store_catalog/") -> {
                                    val storeName = route.removePrefix("store_catalog/")
                                    val storeProducts by repository.getProductsForStore(storeName).collectAsState(initial = emptyList())
                                    StoreCatalogScreen(
                                        storeName = storeName,
                                        products = storeProducts,
                                        onBackClick = { navigator.popBack() },
                                        onProductClick = { productName ->
                                            scope.launch {
                                                activeComparison = repository.getCrossStoreComparison(productName)
                                            }
                                        },
                                        comparisonSummary = activeComparison,
                                        onDismissComparison = { activeComparison = null },
                                        onAddToList = { productName ->
                                            scope.launch {
                                                repository.addShoppingListItem(name = productName)
                                                activeComparison = null
                                            }
                                        },
                                        onSaveManualPrice = { store, prod, price, qty, brand, pres, disc, discType ->
                                            scope.launch {
                                                repository.recordManualPriceObservation(
                                                    storeName = store,
                                                    productName = prod,
                                                    price = price,
                                                    quantity = qty,
                                                    brand = brand,
                                                    presentation = pres,
                                                    discount = disc,
                                                    discountType = discType
                                                )
                                            }
                                        }
                                    )
                                }
                                route == Screen.ShoppingList.route -> {
                                    ShoppingListScreen(
                                        basketEstimate = basketEstimate,
                                        catalogProducts = products,
                                        onAddItem = { name, productId, qty ->
                                            scope.launch {
                                                repository.addShoppingListItem(name, productId, qty)
                                            }
                                        },
                                        onToggleChecked = { itemId, isChecked ->
                                            scope.launch {
                                                repository.toggleItemChecked(itemId, isChecked)
                                            }
                                        },
                                        onDeleteItem = { itemId ->
                                            scope.launch {
                                                repository.removeShoppingListItem(itemId)
                                            }
                                        },
                                        onClearChecked = {
                                            scope.launch {
                                                repository.clearCheckedShoppingListItems()
                                            }
                                        }
                                    )
                                }
                                route == Screen.Scan.route -> {
                                    ReceiptCaptureScreen(
                                        onBackClick = {
                                            navigator.popBack()
                                        },
                                        onDraftReady = { draft ->
                                            activeDraft = draft
                                            navigator.pushScreen(Screen.Review.route)
                                        }
                                    )
                                }
                                route == Screen.Review.route -> {
                                    val draft = activeDraft
                                    if (draft != null) {
                                        ReceiptReviewScreen(
                                            draft = draft,
                                            catalogProducts = products,
                                            onBackClick = {
                                                activeDraft = null
                                                navigator.popBack()
                                            },
                                            onConfirmSave = { store, receipt ->
                                                scope.launch {
                                                    repository.saveConfirmedPurchase(store, receipt)
                                                    activeDraft = null
                                                    navigator.resetToRoot()
                                                }
                                            }
                                        )
                                    } else {
                                        navigator.popBack()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
