package com.barbosa.marketcompare.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.presentation.navigation.Screen

@Composable
fun AppBottomNavigationBar(
    currentRoute: String,
    onNavigateToHistory: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToShoppingList: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScanSelected = currentRoute == Screen.Scan.route || currentRoute == Screen.Review.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            selected = currentRoute == Screen.History.route,
            onClick = onNavigateToHistory,
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.nav_prices)
                )
            },
            label = { Text(stringResource(R.string.nav_prices)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            selected = isScanSelected,
            onClick = onNavigateToScan,
            icon = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = stringResource(R.string.nav_scan)
                )
            },
            label = { Text(stringResource(R.string.nav_scan)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
        NavigationBarItem(
            selected = currentRoute == Screen.ShoppingList.route,
            onClick = onNavigateToShoppingList,
            icon = {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = stringResource(R.string.nav_shopping_list)
                )
            },
            label = { Text(stringResource(R.string.nav_shopping_list)) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

