package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun PriceKpiSummaryCard(
    monitoredCount: Int,
    storesCount: Int,
    bestDealPrice: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KpiItemCard(
            title = stringResource(R.string.home_kpi_monitored_products),
            value = monitoredCount.toString(),
            modifier = Modifier.weight(1f)
        )
        KpiItemCard(
            title = stringResource(R.string.home_kpi_stores_tracked),
            value = storesCount.toString(),
            modifier = Modifier.weight(1f)
        )
        KpiItemCard(
            title = stringResource(R.string.home_kpi_best_deal),
            value = bestDealPrice ?: stringResource(R.string.home_kpi_best_deal_none),
            isPrimaryHighlight = bestDealPrice != null,
            modifier = Modifier.weight(1.2f)
        )
    }
}

@Composable
private fun KpiItemCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    isPrimaryHighlight: Boolean = false
) {
    val containerColor = if (isPrimaryHighlight) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (isPrimaryHighlight) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor.copy(alpha = 0.75f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}

