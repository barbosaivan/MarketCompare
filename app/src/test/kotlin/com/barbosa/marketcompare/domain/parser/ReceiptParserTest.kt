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
        assertEquals(DiscountType.LOYALTY_MEMBER, discountedItem.discountType)
        assertEquals(Money(12900L, "COP"), discountedItem.finalPrice)
        assertNotNull(discountedItem.discountEvidence)
    }

    @Test
    fun `parse receipt with direct discount line links as explicit line discount`() {
        val rawReceipt = """
            ALMACENES EXITO
            15/06/2026
            ACEITE PREMIER 1000ML     12.500
            DESCUENTO DIRECTO         -2.500
            TOTAL                     10.000
        """.trimIndent()

        val draft = ReceiptParser.parse(rawReceipt)

        assertEquals("Éxito", draft.detectedStoreName)
        assertEquals(1, draft.items.size)
        val item = draft.items[0]
        assertEquals(Money(2500L, "COP"), item.discount)
        assertEquals(DiscountType.EXPLICIT_LINE, item.discountType)
        assertEquals(Money(10000L, "COP"), item.finalPrice)
        assertEquals("DESCUENTO DIRECTO         -2.500", item.discountEvidence)
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

    @Test
    fun `parse real D1 physical receipt extracts all items barcode quantities promo and total`() {
        val rawReceipt = """
            TIENDAS D1
            NIT 900.276.962-1
            2 2 UN 4,990 7700304457908 TOMATE CHONTO 9,980 6
            3 1 UN 6,990 0000000000092 PINAS GOLDEN 6,990 6
            4 1 UN 3,500 7700304959693 CEBOLLA LARGA 3,500 6
            5 4 UN 4,990 0000000000084 AGUACATE 19,960 6
            Pague 1 lleve 2P Aguacate Var -9980
            6 1 UN 2,990 7702011161987 DULCES DUROS 2,990 A
            7 1 UN 1,990 7702026151744 PROTECTORES D 1,990 5
            8 1 UN 5,990 7700304601714 ARVEJA VERDE 5,990 A
            19 2 UN 2,400 0000077047779 NUCITA TOY 14 4,800 A
            TOTAL 106,730
            FORMA DE PAGO: CONTADO - VALOR PAGADO 106,730
            TARJ CRE/DEB $
        """.trimIndent()

        val draft = ReceiptParser.parse(rawReceipt)

        assertEquals("D1", draft.detectedStoreName)
        assertEquals("900.276.962-1", draft.detectedTaxId)
        assertEquals(Money(106730L, "COP"), draft.detectedTotal)
        assertEquals(8, draft.items.size)

        // Item 0: Tomate chonto
        val tomate = draft.items[0]
        assertEquals("TOMATE CHONTO", tomate.rawDescription)
        assertEquals(2L, tomate.quantity.value)
        assertEquals(Money(4990L, "COP"), tomate.unitListPrice)
        assertEquals(Money(9980L, "COP"), tomate.finalPrice)

        // Item 3: Aguacate con promo
        val aguacate = draft.items[3]
        assertEquals("AGUACATE", aguacate.rawDescription)
        assertEquals(4L, aguacate.quantity.value)
        assertEquals(Money(9980L, "COP"), aguacate.discount)
        assertEquals(DiscountType.PROMOTION_2X1, aguacate.discountType)
        assertEquals(Money(9980L, "COP"), aguacate.finalPrice)

        // Item 7: Nucita
        val nucita = draft.items[7]
        assertEquals("NUCITA TOY 14", nucita.rawDescription)
        assertEquals(2L, nucita.quantity.value)
        assertEquals(Money(2400L, "COP"), nucita.unitListPrice)
        assertEquals(Money(4800L, "COP"), nucita.finalPrice)
    }

    @Test
    fun `parse real D1 electronic invoice extracts store date and decimal prices correctly`() {
        val rawReceipt = """
            D 1 S A S
            TIENDA-6A100129
            FACTURA ELECTRÓNICA DE VENTA N: H8E8200379
            FECHA: 2026-11-06 20:45:07
            NUM. DOCUMENTO: 1007301911
            1 7700304389865 PAPEL HIG 3H ULTRASU 7.100,00
            2 7700304056583 TORTILLA BURRITO 8 U 7.250,00
            3 7700304349395 SALSA MAYONESA CON C 6.300,00
            15 7701101360279 HAMBURGUESA RANCHERA 19.990,00
            TOTAL 40.640,00
        """.trimIndent()

        val draft = ReceiptParser.parse(rawReceipt)

        assertEquals("D1", draft.detectedStoreName)
        assertEquals(4, draft.items.size)
        assertEquals(Money(40640L, "COP"), draft.detectedTotal)

        assertEquals("PAPEL HIG 3H ULTRASU", draft.items[0].rawDescription)
        assertEquals(Money(7100L, "COP"), draft.items[0].finalPrice)

        assertEquals("HAMBURGUESA RANCHERA", draft.items[3].rawDescription)
        assertEquals(Money(19990L, "COP"), draft.items[3].finalPrice)
    }
}

