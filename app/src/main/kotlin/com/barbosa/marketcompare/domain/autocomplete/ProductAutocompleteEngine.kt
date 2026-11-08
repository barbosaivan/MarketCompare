package com.barbosa.marketcompare.domain.autocomplete

import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.normalizer.NormalizedProductCandidate
import com.barbosa.marketcompare.domain.normalizer.ProductNormalizer
import java.util.Locale
import java.util.UUID

enum class SuggestionMatchType {
    EXACT_ALIAS,
    CATALOG_PRODUCT,
    HEURISTIC_PROPOSAL
}

data class ProductSuggestion(
    val product: Product,
    val matchType: SuggestionMatchType,
    val matchedLabel: String,
    val isVariantCompatible: Boolean,
    val rankScore: Int
)

object ProductAutocompleteEngine {

    /**
     * Genera una lista ordenada de sugerencias para un término de búsqueda,
     * evaluando la compatibilidad de variante frente a los datos detectados por el OCR.
     */
    fun rankSuggestions(
        query: String,
        catalogProducts: List<Product>,
        knownAliases: Map<String, Product> = emptyMap(),
        referenceCandidate: NormalizedProductCandidate? = null
    ): List<ProductSuggestion> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 2) return emptyList()

        val cleanQuery = ProductNormalizer.normalizeAlias(trimmedQuery)
        val queryTokens = cleanQuery.split(" ").filter { it.length > 1 }

        val suggestions = mutableListOf<ProductSuggestion>()
        val seenProductIds = mutableSetOf<String>()

        // 1. Evaluar coincidencia de alias registrados
        knownAliases.forEach { (aliasText, product) ->
            val cleanAlias = ProductNormalizer.normalizeAlias(aliasText)
            if (cleanAlias == cleanQuery || cleanAlias.contains(cleanQuery) || cleanQuery.contains(cleanAlias)) {
                val isCompatible = referenceCandidate == null ||
                        ProductNormalizer.areVariantsCompatible(referenceCandidate.toProduct("ref"), product)
                val score = if (cleanAlias == cleanQuery) 100 else 85
                suggestions.add(
                    ProductSuggestion(
                        product = product,
                        matchType = SuggestionMatchType.EXACT_ALIAS,
                        matchedLabel = aliasText,
                        isVariantCompatible = isCompatible,
                        rankScore = score
                    )
                )
                seenProductIds.add(product.id)
            }
        }

        // 2. Evaluar productos del catálogo local
        for (prod in catalogProducts) {
            if (seenProductIds.contains(prod.id)) continue

            val cleanCanonical = ProductNormalizer.normalizeAlias(prod.canonicalName)
            val cleanBrand = prod.brand?.let { ProductNormalizer.normalizeAlias(it) } ?: ""

            var score = 0
            if (cleanCanonical.startsWith(cleanQuery)) {
                score += 80
            } else if (cleanCanonical.contains(cleanQuery)) {
                score += 60
            }

            if (cleanBrand.isNotBlank() && cleanQuery.contains(cleanBrand)) {
                score += 20
            }

            // Bonificación por coincidencia de tokens
            val matchingTokensCount = queryTokens.count { cleanCanonical.contains(it) }
            score += (matchingTokensCount * 10)

            if (score > 20) {
                val isCompatible = referenceCandidate == null ||
                        ProductNormalizer.areVariantsCompatible(referenceCandidate.toProduct("ref"), prod)

                suggestions.add(
                    ProductSuggestion(
                        product = prod,
                        matchType = SuggestionMatchType.CATALOG_PRODUCT,
                        matchedLabel = prod.canonicalName,
                        isVariantCompatible = isCompatible,
                        rankScore = score
                    )
                )
                seenProductIds.add(prod.id)
            }
        }

        // 3. Propuesta heurística normalizada del OCR si no hay coincidencia exacta del 100%
        val heuristic = ProductNormalizer.normalize(trimmedQuery)
        val heuristicProduct = heuristic.toProduct(UUID.randomUUID().toString())
        if (suggestions.none { it.product.canonicalName.equals(heuristicProduct.canonicalName, ignoreCase = true) }) {
            suggestions.add(
                ProductSuggestion(
                    product = heuristicProduct,
                    matchType = SuggestionMatchType.HEURISTIC_PROPOSAL,
                    matchedLabel = heuristic.canonicalName,
                    isVariantCompatible = true,
                    rankScore = 40
                )
            )
        }

        // Ordenamiento: primero variantes compatibles, luego por score descendente
        return suggestions.sortedWith(
            compareByDescending<ProductSuggestion> { it.isVariantCompatible }
                .thenByDescending { it.rankScore }
        )
    }
}
