package com.barbosa.marketcompare.domain.normalizer

import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.UnitType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductNormalizerTest {

    @Test
    fun `normalize extracts brand presentation and net quantity correctly`() {
        val candidate = ProductNormalizer.normalize("YOGURT ALPINA FRESA 150G VASO")

        assertEquals("Alpina", candidate.brand)
        assertEquals("Vaso", candidate.presentation)
        assertEquals(150L, candidate.baseQuantity)
        assertEquals(UnitType.GRAM, candidate.unit)
        assertEquals("Lácteos", candidate.category)
        assertTrue(candidate.canonicalName.contains("Alpina"))
        assertTrue(candidate.canonicalName.contains("Vaso"))
        assertTrue(candidate.canonicalName.contains("150g"))
    }

    @Test
    fun `normalize extracts grain product details`() {
        val candidate = ProductNormalizer.normalize("ARROZ FLORHUILA 1000G BOLSA")

        assertEquals("Florhuila", candidate.brand)
        assertEquals("Bolsa", candidate.presentation)
        assertEquals(1000L, candidate.baseQuantity)
        assertEquals(UnitType.GRAM, candidate.unit)
        assertEquals("Despensa", candidate.category)
    }

    @Test
    fun `normalize extracts beverage and volume units`() {
        val candidate1 = ProductNormalizer.normalize("ACEITE PREMIER 1000ML BOTELLA")
        assertEquals("Premier", candidate1.brand)
        assertEquals("Botella", candidate1.presentation)
        assertEquals(1000L, candidate1.baseQuantity)
        assertEquals(UnitType.MILLILITER, candidate1.unit)

        val candidate2 = ProductNormalizer.normalize("LECHE ALQUERIA ENTERA 1L")
        assertEquals("Alquería", candidate2.brand)
        assertEquals(UnitType.LITER, candidate2.unit)
    }

    @Test
    fun `normalize extracts egg tray with presentation and units`() {
        val candidate = ProductNormalizer.normalize("HUEVOS SANTA REYES 30UND BANDEJA")

        assertEquals("Santa Reyes", candidate.brand)
        assertEquals("Bandeja", candidate.presentation)
        assertEquals(30L, candidate.baseQuantity)
        assertEquals(UnitType.UNIT, candidate.unit)
        assertEquals("Huevos y Aves", candidate.category)
    }

    @Test
    fun `areVariantsCompatible strictly distinguishes variants by size and presentation`() {
        val yogurtSmallVaso = Product(
            id = "1",
            canonicalName = "Alpina Yogurt Fresa 150g Vaso",
            brand = "Alpina",
            category = "Lácteos",
            baseQuantity = 150L,
            unit = UnitType.GRAM,
            presentation = "Vaso"
        )

        val yogurtLargeBolsa = Product(
            id = "2",
            canonicalName = "Alpina Yogurt Fresa 1000g Bolsa",
            brand = "Alpina",
            category = "Lácteos",
            baseQuantity = 1000L,
            unit = UnitType.GRAM,
            presentation = "Bolsa"
        )

        val yogurtSmallBolsa = Product(
            id = "3",
            canonicalName = "Alpina Yogurt Fresa 150g Bolsa",
            brand = "Alpina",
            category = "Lácteos",
            baseQuantity = 150L,
            unit = UnitType.GRAM,
            presentation = "Bolsa"
        )

        val yogurtSmallVasoClone = yogurtSmallVaso.copy(id = "99")

        // Diferente tamaño y empaque: NO compatibles (no deben fusionarse)
        assertFalse(ProductNormalizer.areVariantsCompatible(yogurtSmallVaso, yogurtLargeBolsa))

        // Mismo tamaño pero diferente empaque (Vaso vs Bolsa): NO compatibles
        assertFalse(ProductNormalizer.areVariantsCompatible(yogurtSmallVaso, yogurtSmallBolsa))

        // Mismos atributos esenciales de variante: COMPATIBLES
        assertTrue(ProductNormalizer.areVariantsCompatible(yogurtSmallVaso, yogurtSmallVasoClone))
    }

    @Test
    fun `variantFingerprint generates distinct keys for packaging variants`() {
        val c1 = ProductNormalizer.normalize("YOGURT ALPINA FRESA 150G VASO")
        val c2 = ProductNormalizer.normalize("YOGURT ALPINA FRESA 1000G BOLSA")
        val c3 = ProductNormalizer.normalize("YOGURT ALPINA FRESA 150G BOLSA")

        assertNotEquals(c1.variantFingerprint(), c2.variantFingerprint())
        assertNotEquals(c1.variantFingerprint(), c3.variantFingerprint())
        assertNotEquals(c2.variantFingerprint(), c3.variantFingerprint())
    }

    @Test
    fun `normalizeAlias removes accents special characters and extra spaces`() {
        val raw1 = "  ¡Café Sello Rojo, 500g!  "
        assertEquals("CAFE SELLO ROJO 500G", ProductNormalizer.normalizeAlias(raw1))

        val raw2 = "Yogurt Melocotón / Alpina - 150g"
        assertEquals("YOGURT MELOCOTON ALPINA 150G", ProductNormalizer.normalizeAlias(raw2))
    }
}
