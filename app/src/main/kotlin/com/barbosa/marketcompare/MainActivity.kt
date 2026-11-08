package com.barbosa.marketcompare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.barbosa.marketcompare.domain.model.BasketEstimate
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.ReceiptDraft
import com.barbosa.marketcompare.presentation.navigation.Screen
import com.barbosa.marketcompare.presentation.screens.history.HistoryScreen
import com.barbosa.marketcompare.presentation.screens.review.ReceiptReviewScreen
import com.barbosa.marketcompare.presentation.screens.scan.ReceiptCaptureScreen
import com.barbosa.marketcompare.presentation.screens.shopping.ShoppingListScreen
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
                    var currentScreen by remember { mutableStateOf<String>(Screen.History.route) }
                    var activeDraft by remember { mutableStateOf<ReceiptDraft?>(null) }

                    val receipts by repository.getAllReceipts().collectAsState(initial = emptyList())
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

                    val showBottomNav = currentScreen == Screen.History.route || currentScreen == Screen.ShoppingList.route

                    Scaffold(
                        bottomBar = {
                            if (showBottomNav) {
                                NavigationBar {
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.History.route,
                                        onClick = { currentScreen = Screen.History.route },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(R.string.nav_history)) }
                                    )
                                    NavigationBarItem(
                                        selected = false,
                                        onClick = { currentScreen = Screen.Scan.route },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(R.string.nav_scan)) }
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == Screen.ShoppingList.route,
                                        onClick = { currentScreen = Screen.ShoppingList.route },
                                        icon = {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(R.string.nav_shopping_list)) }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = innerPadding.calculateBottomPadding())
                        ) {
                            when (currentScreen) {
                                Screen.History.route -> {
                                    HistoryScreen(
                                        receipts = receipts,
                                        onScanClick = {
                                            currentScreen = Screen.Scan.route
                                        },
                                        onReceiptClick = { receiptId ->
                                            // Detalle de recibo
                                        }
                                    )
                                }
                                Screen.ShoppingList.route -> {
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
                                Screen.Scan.route -> {
                                    ReceiptCaptureScreen(
                                        onBackClick = {
                                            currentScreen = Screen.History.route
                                        },
                                        onDraftReady = { draft ->
                                            activeDraft = draft
                                            currentScreen = Screen.Review.route
                                        }
                                    )
                                }
                                Screen.Review.route -> {
                                    val draft = activeDraft
                                    if (draft != null) {
                                        ReceiptReviewScreen(
                                            draft = draft,
                                            catalogProducts = products,
                                            onBackClick = {
                                                currentScreen = Screen.Scan.route
                                            },
                                            onConfirmSave = { store, receipt ->
                                                scope.launch {
                                                    repository.saveConfirmedPurchase(store, receipt)
                                                    activeDraft = null
                                                    currentScreen = Screen.History.route
                                                }
                                            }
                                        )
                                    } else {
                                        currentScreen = Screen.History.route
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
