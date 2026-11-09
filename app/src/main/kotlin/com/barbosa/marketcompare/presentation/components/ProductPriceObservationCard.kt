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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.model.PriceFreshness
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.normalizer.StandardPriceMeasurement
import com.barbosa.marketcompare.presentation.theme.semanticColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProductPriceObservationCard(
    observation: ProductPriceObservationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            ObservationCardHeader(
                productName = observation.productName,
                priceDisplay = observation.unitPrice.formatDisplay()
            )
            Spacer(modifier = Modifier.height(6.dp))
            ObservationCardFooter(
                storeName = observation.storeName,
                timestamp = observation.observedAtTimestamp
            )
            Spacer(modifier = Modifier.height(8.dp))
            ObservationBadges(
                isLowestPrice = observation.isLowestPrice,
                freshness = observation.freshness,
                standardMeasurement = observation.standardMeasurement
            )
        }
    }
}

@Composable
private fun ObservationCardHeader(
    productName: String,
    priceDisplay: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = productName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        Text(
            text = priceDisplay,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun ObservationCardFooter(
    storeName: String,
    timestamp: Long,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CO"))
    val formattedDate = dateFormatter.format(Date(timestamp))

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = storeName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = stringResource(R.string.home_price_observation_date, formattedDate),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ObservationBadges(
    isLowestPrice: Boolean,
    freshness: PriceFreshness,
    standardMeasurement: StandardPriceMeasurement?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        standardMeasurement?.let {
            StandardPriceBadge(measurement = it)
        }
        if (isLowestPrice) {
            BadgeChip(
                text = stringResource(R.string.home_badge_best_price),
                containerColor = MaterialTheme.semanticColors.freshnessRecent.copy(alpha = 0.15f),
                contentColor = MaterialTheme.semanticColors.freshnessRecent
            )
        }
        val (freshnessLabel, freshnessColor) = when (freshness) {
            PriceFreshness.RECENT -> stringResource(R.string.basket_badge_recent) to MaterialTheme.semanticColors.freshnessRecent
            PriceFreshness.STALE -> stringResource(R.string.basket_badge_stale) to MaterialTheme.semanticColors.freshnessStale
            PriceFreshness.UNKNOWN -> stringResource(R.string.basket_badge_unknown) to MaterialTheme.semanticColors.freshnessUnknown
        }
        BadgeChip(
            text = freshnessLabel,
            containerColor = freshnessColor.copy(alpha = 0.12f),
            contentColor = freshnessColor
        )
    }
}

@Composable
private fun BadgeChip(
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(containerColor, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = contentColor
        )
    }
}

