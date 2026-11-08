package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.StorePriceEntry
import com.barbosa.marketcompare.presentation.theme.semanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCrossStoreComparisonSheet(
    summary: ProductComparisonSummary,
    onDismiss: () -> Unit,
    onAddToList: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ComparisonHeader(
                productName = summary.productName,
                storesCount = summary.storeEntries.size,
                onDismiss = onDismiss
            )
            ComparisonHeroCard(summary = summary)
            ComparisonBreakdownList(entries = summary.storeEntries)
            Button(
                onClick = { onAddToList(summary.productName) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.compare_sheet_add_to_list))
            }
        }
    }
}

@Composable
private fun ComparisonHeader(
    productName: String,
    storesCount: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.compare_sheet_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = productName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.compare_sheet_stores_count, storesCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.compare_sheet_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ComparisonHeroCard(
    summary: ProductComparisonSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.compare_sheet_best_deal_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = summary.cheapestStore,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = summary.lowestPrice.formatDisplay(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (summary.storeEntries.size > 1 && summary.maxSavings.amountMinor > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.compare_sheet_savings,
                        summary.maxSavings.formatDisplay(),
                        summary.maxSavingsPercentage,
                        summary.cheapestStore
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.semanticColors.freshnessRecent
                )
            }
        }
    }
}

@Composable
private fun ComparisonBreakdownList(
    entries: List<StorePriceEntry>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        entries.forEachIndexed { index, entry ->
            StoreComparisonRow(rank = index + 1, entry = entry)
        }
    }
}

@Composable
private fun StoreComparisonRow(
    rank: Int,
    entry: StorePriceEntry,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CO"))
    val formattedDate = dateFormatter.format(Date(entry.observedAtTimestamp))

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isLowestPrice) {
                MaterialTheme.colorScheme.surfaceContainerHigh
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                RankBadge(rank = rank, isLowest = entry.isLowestPrice)
                Column {
                    Text(
                        text = entry.storeName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.home_price_observation_date, formattedDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.unitPrice.formatDisplay(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.isLowestPrice) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (!entry.isLowestPrice && entry.priceDifferenceWithLowest.amountMinor > 0) {
                    Text(
                        text = stringResource(
                            R.string.compare_sheet_price_delta,
                            entry.priceDifferenceWithLowest.formatDisplay(),
                            entry.percentageDifferenceWithLowest
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.semanticColors.freshnessStale
                    )
                }
            }
        }
    }
}

@Composable
private fun RankBadge(
    rank: Int,
    isLowest: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = if (isLowest) {
        MaterialTheme.semanticColors.freshnessRecent
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (isLowest) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .size(26.dp)
            .background(containerColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = rank.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

