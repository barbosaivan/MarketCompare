package com.barbosa.marketcompare.presentation.components

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.barbosa.marketcompare.R
import com.barbosa.marketcompare.domain.autocomplete.ProductAutocompleteEngine
import com.barbosa.marketcompare.domain.autocomplete.ProductSuggestion
import com.barbosa.marketcompare.domain.autocomplete.SuggestionMatchType
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.normalizer.NormalizedProductCandidate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductAutocompleteField(
    query: String,
    onQueryChange: (String) -> Unit,
    catalogProducts: List<Product>,
    onProductSelected: (Product) -> Unit,
    modifier: Modifier = Modifier,
    knownAliases: Map<String, Product> = emptyMap(),
    referenceCandidate: NormalizedProductCandidate? = null,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    var pendingMismatchSuggestion by remember { mutableStateOf<ProductSuggestion?>(null) }

    val suggestions by remember(query, catalogProducts, knownAliases, referenceCandidate) {
        derivedStateOf {
            ProductAutocompleteEngine.rankSuggestions(
                query = query,
                catalogProducts = catalogProducts,
                knownAliases = knownAliases,
                referenceCandidate = referenceCandidate
            )
        }
    }

    // Diálogo de protección estricta contra fusión silenciosa de variantes incompatibles
    pendingMismatchSuggestion?.let { suggestion ->
        AlertDialog(
            onDismissRequest = { pendingMismatchSuggestion = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.dialog_variant_mismatch_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.dialog_variant_mismatch_message,
                        suggestion.product.canonicalName,
                        referenceCandidate?.canonicalName ?: query
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val chosen = suggestion.product
                        pendingMismatchSuggestion = null
                        expanded = false
                        onQueryChange(chosen.canonicalName)
                        onProductSelected(chosen)
                    }
                ) {
                    Text(
                        text = stringResource(R.string.dialog_variant_mismatch_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingMismatchSuggestion = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty() && enabled,
        onExpandedChange = { expanded = it && enabled },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                expanded = it.trim().length >= 2
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, enabled),
            enabled = enabled,
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (query.isNotEmpty() && enabled) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.action_cancel),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            label = { Text(stringResource(R.string.autocomplete_search_hint)) },
            textStyle = MaterialTheme.typography.bodyMedium
        )

        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty() && enabled,
            onDismissRequest = { expanded = false }
        ) {
            suggestions.take(5).forEach { suggestion ->
                DropdownMenuItem(
                    text = {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = suggestion.product.canonicalName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                val (badgeText, badgeColor) = when {
                                    !suggestion.isVariantCompatible ->
                                        stringResource(R.string.autocomplete_badge_variant_mismatch) to MaterialTheme.colorScheme.errorContainer
                                    suggestion.matchType == SuggestionMatchType.EXACT_ALIAS ->
                                        stringResource(R.string.autocomplete_badge_exact) to MaterialTheme.colorScheme.primaryContainer
                                    suggestion.matchType == SuggestionMatchType.CATALOG_PRODUCT ->
                                        stringResource(R.string.autocomplete_badge_catalog) to MaterialTheme.colorScheme.secondaryContainer
                                    else ->
                                        stringResource(R.string.autocomplete_badge_suggested) to MaterialTheme.colorScheme.tertiaryContainer
                                }

                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(badgeText, style = MaterialTheme.typography.labelSmall) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = badgeColor
                                    ),
                                    modifier = Modifier.height(26.dp)
                                )
                            }

                            if (suggestion.matchedLabel != suggestion.product.canonicalName) {
                                Text(
                                    text = suggestion.matchedLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        if (!suggestion.isVariantCompatible) {
                            // Se intercepta y se exige confirmación manual; no se fusionan variantes silenciosamente
                            pendingMismatchSuggestion = suggestion
                        } else {
                            expanded = false
                            onQueryChange(suggestion.product.canonicalName)
                            onProductSelected(suggestion.product)
                        }
                    }
                )
            }
        }
    }
}
