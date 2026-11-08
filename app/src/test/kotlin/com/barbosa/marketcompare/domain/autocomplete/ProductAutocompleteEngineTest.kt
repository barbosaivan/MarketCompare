package com.barbosa.marketcompare.domain.autocomplete

import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.UnitType
import com.barbosa.marketcompare.domain.normalizer.ProductNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductAutocompleteEngineTest {

    private val catalog = listOf(
        Product(
            id = "p-yog-150-vaso",
            canonicalName = "Alpina Yogurt Fresa 150g Vaso",
            brand = "Alpina",
            category = "Lácteos",
            baseQuantity = 150L,
            unit = UnitType.GRAM,
            presentation = "Vaso"
        ),
        Product(
            id = "p-yog-1000-bolsa",
            canonicalName = "Alpina Yogurt Fresa 1000g Bolsa",
            brand = "Alpina",
            category = "Lácteos",
            baseQuantity = 1000L,
            unit = UnitType.GRAM,
            presentation = "Bolsa"
        ),
        Product(
            id = "p-leche-1l",
            canonicalName = "Alquería Leche Entera 1L Bolsa",
            brand = "Alquería",
            category = "Lácteos",
            baseQuantity = 1000L,
            unit = UnitType.LITER,
            presentation = "Bolsa"
        ),
        Product(
            id = "p-arroz-1kg",
            canonicalName = "Florhuila Arroz 1000g Bolsa",
            brand = "Florhuila",
            category = "Despensa",
            baseQuantity = 1000L,
            unit = UnitType.GRAM,
            presentation = "Bolsa"
        )
    )

    private val aliases = mapOf(
        "YOG ALP FR 150G" to catalog[0],
        "LECHE ALQUERIA ENT" to catalog[2]
    )

    @Test
    fun `rankSuggestions returns empty for query shorter than 2 characters`() {
        val results = ProductAutocompleteEngine.rankSuggestions(
            query = "A",
            catalogProducts = catalog
        )
        assertTrue(results.isEmpty())
    }

    @Test
    fun `rankSuggestions prioritizes exact alias match at the top`() {
        val results = ProductAutocompleteEngine.rankSuggestions(
            query = "YOG ALP FR 150G",
            catalogProducts = catalog,
            knownAliases = aliases
        )

        assertTrue(results.isNotEmpty())
        val top = results.first()
        assertEquals(SuggestionMatchType.EXACT_ALIAS, top.matchType)
        assertEquals("p-yog-150-vaso", top.product.id)
        assertTrue(top.rankScore >= 100)
    }

    @Test
    fun `rankSuggestions strictly marks variant compatibility against reference candidate`() {
        val referenceCandidate = ProductNormalizer.normalize("YOGURT ALPINA FRESA 150G VASO")

        val results = ProductAutocompleteEngine.rankSuggestions(
            query = "Yogurt Alpina",
            catalogProducts = catalog,
            referenceCandidate = referenceCandidate
        )

        // El producto de 150g Vaso debe ser compatible
        val compatibleItem = results.firstOrNull { it.product.id == "p-yog-150-vaso" }
        assertTrue("El producto con misma presentación debe ser compatible", compatibleItem?.isVariantCompatible == true)

        // El producto de 1000g Bolsa NO debe ser compatible (variante distinta)
        val incompatibleItem = results.firstOrNull { it.product.id == "p-yog-1000-bolsa" }
        assertFalse("El producto con diferente peso/empaque debe marcarse como variante incompatible", incompatibleItem?.isVariantCompatible == true)
    }

    @Test
    fun `rankSuggestions places compatible variants before incompatible ones`() {
        val referenceCandidate = ProductNormalizer.normalize("YOGURT ALPINA FRESA 150G VASO")

        val results = ProductAutocompleteEngine.rankSuggestions(
            query = "Alpina Fresa",
            catalogProducts = catalog,
            referenceCandidate = referenceCandidate
        )

        assertTrue(results.size >= 2)
        // El primer resultado debe ser compatible
        assertTrue(results.first().isVariantCompatible)
        assertEquals("p-yog-150-vaso", results.first().product.id)

        // El resultado incompatible debe estar después
        val incompatibleIndex = results.indexOfFirst { !it.isVariantCompatible }
        assertTrue(incompatibleIndex > 0)
    }

    @Test
    fun `rankSuggestions generates heuristic proposal when no catalog item fully matches`() {
        val results = ProductAutocompleteEngine.rankSuggestions(
            query = "CHOCOLISTO 500G TARRO",
            catalogProducts = catalog
        )

        assertTrue(results.isNotEmpty())
        val proposal = results.firstOrNull { it.matchType == SuggestionMatchType.HEURISTIC_PROPOSAL }
        assertTrue(proposal != null)
        assertTrue(proposal!!.product.canonicalName.contains("Chocolisto"))
    }
}
