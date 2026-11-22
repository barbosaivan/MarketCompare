package com.barbosa.marketcompare.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem

/**
 * Diálogos de confirmación de eliminación destructiva para productos y tiendas (MC-059).
 */
@Composable
fun DeleteProductConfirmationDialog(
    observation: ProductPriceObservationItem,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.home_dialog_delete_product_title))
        },
        text = {
            Text(
                text = stringResource(
                    R.string.home_dialog_delete_product_message,
                    observation.productName,
                    observation.storeName
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.action_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
fun DeleteStoreConfirmationDialog(
    storeName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.home_dialog_delete_store_title))
        },
        text = {
            Text(
                text = stringResource(
                    R.string.home_dialog_delete_store_message,
                    storeName
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.home_dialog_delete_store_confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_cancel))
            }
        }
    )
}
