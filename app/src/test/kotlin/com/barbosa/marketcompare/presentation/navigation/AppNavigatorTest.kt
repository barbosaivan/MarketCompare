package com.barbosa.marketcompare.presentation.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AppNavigatorTest {

    private lateinit var navigator: AppNavigator

    @Before
    fun setUp() {
        navigator = AppNavigator(Screen.History.route)
    }

    @Test
    fun initialState_isHistory_andCannotGoBack() {
        assertEquals(Screen.History.route, navigator.currentRoute)
        assertFalse(navigator.canGoBack)
        assertEquals(1, navigator.backStack.size)
    }

    @Test
    fun navigateToTab_shoppingList_canGoBackToHistory() {
        navigator.navigateToTab(Screen.ShoppingList.route)

        assertEquals(Screen.ShoppingList.route, navigator.currentRoute)
        assertTrue(navigator.canGoBack)
        assertEquals(2, navigator.backStack.size)

        val popped = navigator.popBack()
        assertTrue(popped)
        assertEquals(Screen.History.route, navigator.currentRoute)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun navigateToTab_history_clearsToHistory() {
        navigator.navigateToTab(Screen.Scan.route)
        navigator.pushScreen(Screen.Review.route)
        assertEquals(3, navigator.backStack.size)

        navigator.navigateToTab(Screen.History.route)
        assertEquals(Screen.History.route, navigator.currentRoute)
        assertEquals(1, navigator.backStack.size)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun pushScreen_scanToReview_backGoesToScanThenHistory() {
        navigator.navigateToTab(Screen.Scan.route)
        navigator.pushScreen(Screen.Review.route)

        assertEquals(Screen.Review.route, navigator.currentRoute)
        assertEquals(3, navigator.backStack.size)

        // First pop goes to Scan
        assertTrue(navigator.popBack())
        assertEquals(Screen.Scan.route, navigator.currentRoute)

        // Second pop goes to History
        assertTrue(navigator.popBack())
        assertEquals(Screen.History.route, navigator.currentRoute)

        // Third pop is rejected (at root)
        assertFalse(navigator.popBack())
        assertEquals(Screen.History.route, navigator.currentRoute)
    }

    @Test
    fun navigateToTab_fromReview_switchesTabAndResetsHierarchy() {
        navigator.navigateToTab(Screen.Scan.route)
        navigator.pushScreen(Screen.Review.route)

        navigator.navigateToTab(Screen.ShoppingList.route)
        assertEquals(Screen.ShoppingList.route, navigator.currentRoute)
        assertEquals(listOf(Screen.History.route, Screen.ShoppingList.route), navigator.backStack.toList())

        assertTrue(navigator.popBack())
        assertEquals(Screen.History.route, navigator.currentRoute)
    }

    @Test
    fun resetToRoot_clearsAllScreens() {
        navigator.navigateToTab(Screen.Scan.route)
        navigator.pushScreen(Screen.Review.route)

        navigator.resetToRoot()
        assertEquals(Screen.History.route, navigator.currentRoute)
        assertEquals(1, navigator.backStack.size)
        assertFalse(navigator.canGoBack)
    }
}

