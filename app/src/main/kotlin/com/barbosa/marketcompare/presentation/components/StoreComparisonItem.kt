package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.StorePriceEntry
import com.barbosa.marketcompare.presentation.theme.semanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StoreComparisonRow(
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
                entry.standardMeasurement?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    StandardPriceBadge(measurement = it)
                }
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
fun RankBadge(
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

