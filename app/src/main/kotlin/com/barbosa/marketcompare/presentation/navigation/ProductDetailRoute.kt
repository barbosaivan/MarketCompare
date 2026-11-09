package com.barbosa.marketcompare.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.barbosa.marketcompare.domain.model.ProductDetailData
import com.barbosa.marketcompare.domain.repository.PurchaseRepository
import com.barbosa.marketcompare.presentation.screens.detail.ProductDetailScreen
import kotlinx.coroutines.launch

@Composable
fun ProductDetailRoute(
    productIdOrName: String,
    repository: PurchaseRepository,
    onBackClick: () -> Unit,
    onAddObservation: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var detailData by remember(productIdOrName) { mutableStateOf<ProductDetailData?>(null) }

    LaunchedEffect(productIdOrName) {
        detailData = repository.getProductDetailData(productIdOrName)
    }

    ProductDetailScreen(
        detailData = detailData,
        onBackClick = onBackClick,
        onSaveProduct = { updatedProduct, previousName ->
            scope.launch {
                repository.updateProduct(updatedProduct, previousName)
                detailData = repository.getProductDetailData(updatedProduct.canonicalName)
            }
        },
        onUpdateObservation = { obsId, newPrice, newQuantity, newStore ->
            scope.launch {
                repository.updateObservation(obsId, newPrice, newQuantity, newStore)
                detailData = repository.getProductDetailData(productIdOrName)
            }
        },
        onDeleteObservation = { obsId ->
            scope.launch {
                repository.deleteObservation(obsId)
                detailData = repository.getProductDetailData(productIdOrName)
            }
        },
        onAddObservationClick = {
            detailData?.product?.canonicalName?.let(onAddObservation)
        },
        onAddToList = { productName ->
            scope.launch {
                repository.addShoppingListItem(name = productName)
            }
        },
        modifier = modifier
    )
}

