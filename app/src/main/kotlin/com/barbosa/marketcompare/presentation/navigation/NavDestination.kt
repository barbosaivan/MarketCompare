package com.barbosa.marketcompare.presentation.navigation

sealed class Screen(val route: String) {
    object History : Screen("history")
    object Scan : Screen("scan")
    object Review : Screen("review/{draftId}") {
        fun createRoute(draftId: String) = "review/$draftId"
    }
    object StoreCatalog : Screen("store_catalog/{storeName}") {
        fun createRoute(storeName: String) = "store_catalog/$storeName"
    }
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object ShoppingList : Screen("shopping_list")
}

