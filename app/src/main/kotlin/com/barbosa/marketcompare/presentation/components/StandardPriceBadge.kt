package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.domain.normalizer.StandardPriceMeasurement

/**
 * Badge visual de precio normalizado por unidad de medida estándar (MC-052).
 * Presenta el precio por kilo, litro o unidad para comparaciones homogéneas.
 */
@Composable
fun StandardPriceBadge(
    measurement: StandardPriceMeasurement,
    showSecondary: Boolean = false,
    modifier: Modifier = Modifier
) {
    val displayText = if (showSecondary && measurement.secondaryPrice != null) {
        "${measurement.formatPrimary()} (${measurement.formatSecondary()})"
    } else {
        measurement.formatPrimary()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

