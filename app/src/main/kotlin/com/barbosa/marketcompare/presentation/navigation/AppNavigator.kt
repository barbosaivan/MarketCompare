package com.barbosa.marketcompare.presentation.navigation

import androidx.compose.runtime.mutableStateListOf

class AppNavigator(initialRoute: String = Screen.History.route) {

    val backStack = mutableStateListOf(initialRoute)

    val currentRoute: String
        get() = backStack.lastOrNull() ?: Screen.History.route

    val canGoBack: Boolean
        get() = backStack.size > 1

    fun navigateToTab(tabRoute: String) {
        if (currentRoute == tabRoute) return

        if (tabRoute == Screen.History.route) {
            backStack.clear()
            backStack.add(Screen.History.route)
        } else {
            backStack.removeAll { it != Screen.History.route }
            backStack.add(tabRoute)
        }
    }

    fun pushScreen(route: String) {
        if (currentRoute == route) return
        backStack.add(route)
    }

    fun popBack(): Boolean {
        return if (canGoBack) {
            backStack.removeAt(backStack.lastIndex)
            true
        } else {
            false
        }
    }

    fun resetToRoot() {
        backStack.clear()
        backStack.add(Screen.History.route)
    }
}

