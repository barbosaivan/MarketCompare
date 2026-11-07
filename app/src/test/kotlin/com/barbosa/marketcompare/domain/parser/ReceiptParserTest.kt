package com.barbosa.marketcompare.domain.parser

import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiptParserTest {

    @Test
    fun `parse standard supermarket receipt extracts store items and total`() {
        val rawReceipt = """
            ALMACENES EXITO S.A.
            NIT: 890.900.608-9
            FECHA: 15/06/2026 14:30
            CARRERA 48 # 32-102 MEDELLIN
            --------------------------------
            ARROZ FLORHUILA 1000G      4.200
            LECHE ALQUERIA ENTERA 1L   4.800
            ACEITE PREMIER 1000ML     12.500
            HUEVOS SANTA REYES 30UND  19.900
            --------------------------------
            SUBTOTAL                  41.400
            IVA 19%                    2.375
            TOTAL A PAGAR             41.400
            EFECTIVO                  50.000
            CAMBIO                     8.600
        """.trimIndent()

        val draft = ReceiptParser.parse(rawReceipt)

        assertEquals("Éxito", draft.detectedStoreName)
        assertEquals("890.900.608-9", draft.detectedTaxId)
        assertEquals(Money(41400L, "COP"), draft.detectedTotal)
        assertEquals(4, draft.items.size)

        assertEquals("ARROZ FLORHUILA 1000G", draft.items[0].rawDescription)
        assertEquals(Money(4200L, "COP"), draft.items[0].finalPrice)

        assertEquals("HUEVOS SANTA REYES 30UND", draft.items[3].rawDescription)
        assertEquals(Money(19900L, "COP"), draft.items[3].finalPrice)
    }

    @Test
    fun `parse receipt with promotional discount line links discount to preceding item`() {
        val rawReceipt = """
            TIENDAS D1
            KOBA COLOMBIA S.A.S.
            NIT 900.276.962-1
            10/05/2026
            --------------------------------
            DETERGENTE EN POLVO 2KG   15.900
            DCTO PUNTOS CLIENTE       -3.000
            PAPEL HIGIENICO 12 ROLLOS 14.500
            TOTAL                     27.400
        """.trimIndent()

        val draft = ReceiptParser.parse(rawReceipt)

        assertEquals("D1", draft.detectedStoreName)
        assertEquals(2, draft.items.size)

        val discountedItem = draft.items[0]
        assertEquals("DETERGENTE EN POLVO 2KG", discountedItem.rawDescription)
        assertEquals(Money(3000L, "COP"), discountedItem.discount)
        assertEquals(DiscountType.EXPLICIT_LINE, discountedItem.discountType)
        assertEquals(Money(12900L, "COP"), discountedItem.finalPrice)
        assertNotNull(discountedItem.discountEvidence)
    }

    @Test
    fun `parse receipt ignores payment and fiscal footer lines`() {
        val rawReceipt = """
            SUPERMERCADOS OLIMPICA
            02/04/2026
            PAN TAJADO BIMBO 500G      5.500
            CAFE SELLO ROJO 500G      14.000
            TOTAL                     19.500
            PAGO TARJETA DEBITO       19.500
            AUTORIZACION 123456
            RESOLUCION DIAN 1876000001
            CUFE: abc123def456
        """.trimIndent()

        val draft = ReceiptParser.parse(rawReceipt)

        assertEquals("Olímpica", draft.detectedStoreName)
        assertEquals(2, draft.items.size)
        assertTrue(draft.items.none { it.rawDescription.contains("AUTORIZACION") })
        assertTrue(draft.items.none { it.rawDescription.contains("CUFE") })
    }
}

