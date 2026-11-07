package com.barbosa.marketcompare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.barbosa.marketcompare.presentation.screens.history.HistoryScreen
import com.barbosa.marketcompare.presentation.theme.MarketCompareTheme

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
                    val receipts by repository.getAllReceipts().collectAsState(initial = emptyList())

                    HistoryScreen(
                        receipts = receipts,
                        onScanClick = {
                            // Flujo de navegación hacia captura en MC-014
                        },
                        onReceiptClick = { receiptId ->
                            // Flujo hacia detalle
                        }
                    )
                }
            }
        }
    }
}

