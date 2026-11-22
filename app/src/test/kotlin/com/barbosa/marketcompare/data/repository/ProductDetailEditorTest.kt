package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.dao.PriceObservationItemEntity
import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.ShoppingListDao
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductComparisonSummary
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.StorePriceEntry
import com.barbosa.marketcompare.domain.model.PriceFreshness
import com.barbosa.marketcompare.domain.model.UnitType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class ProductDetailEditorTest {

    private val productDao = mockk<ProductDao>(relaxed = true)
    private val receiptDao = mockk<ReceiptDao>(relaxed = true)
    private val storeDao = mockk<StoreDao>(relaxed = true)
    private val shoppingListDao = mockk<ShoppingListDao>(relaxed = true)

    private lateinit var editor: ProductDetailEditor

    @Before
    fun setUp() {
        editor = ProductDetailEditor(productDao, receiptDao, storeDao, shoppingListDao)
    }

    @Test
    fun `getProductDetailData returns complete data for product id`() = runTest {
        val entity = ProductEntity(
            id = "prod-1",
            canonicalName = "Arroz Diana 1kg",
            brand = "Diana",
            category = "Abarrotes",
            baseQuantity = 1000L,
            unit = "GRAM",
            presentation = "Bolsa 1kg"
        )
        coEvery { productDao.getProductById("prod-1") } returns entity
        coEvery { receiptDao.getPriceObservationsForProduct("Arroz Diana 1kg") } returns listOf(
            PriceObservationItemEntity("obs-1", "prod-1", "Arroz Diana 1kg", 4200L, 100000L, "Éxito"),
            PriceObservationItemEntity("obs-2", "prod-1", "Arroz Diana 1kg", 3900L, 100000L, "D1")
        )

        val comparison = ProductComparisonSummary(
            productName = "Arroz Diana 1kg",
            lowestPrice = Money(3900L, "COP"),
            highestPrice = Money(4200L, "COP"),
            maxSavings = Money(300L, "COP"),
            maxSavingsPercentage = 7.14,
            cheapestStore = "D1",
            storeEntries = emptyList()
        )

        val result = editor.getProductDetailData("prod-1") { comparison }

        assertNotNull(result)
        assertEquals("Arroz Diana 1kg", result?.product?.canonicalName)
        assertEquals("Diana", result?.product?.brand)
        assertEquals(2, result?.observations?.size)
        assertEquals(true, result?.observations?.firstOrNull { it.storeName == "D1" }?.isLowestPrice)
        assertEquals("D1", result?.comparisonSummary?.cheapestStore)
    }

    @Test
    fun `updateProduct persists entity and cascades description rename`() = runTest {
        val updatedProduct = Product(
            id = "prod-1",
            canonicalName = "Arroz Diana Premium 1kg",
            brand = "Diana",
            category = "Granos",
            baseQuantity = 1000L,
            unit = UnitType.GRAM,
            presentation = "Bolsa"
        )

        val productSlot = slot<ProductEntity>()
        coEvery { productDao.insertProduct(capture(productSlot)) } returns Unit

        editor.updateProduct(updatedProduct, previousName = "Arroz Diana 1kg")

        assertEquals("Arroz Diana Premium 1kg", productSlot.captured.canonicalName)
        assertEquals("Granos", productSlot.captured.category)
        coVerify {
            receiptDao.updateItemDescriptions("prod-1", "Arroz Diana 1kg", "Arroz Diana Premium 1kg")
            shoppingListDao.updateItemNames("prod-1", "Arroz Diana 1kg", "Arroz Diana Premium 1kg")
        }
    }

    @Test
    fun `updateObservation updates price and creates or switches store`() = runTest {
        val fakeItem = ReceiptItemEntity(
            id = "item-123",
            receiptId = "rec-456",
            productId = "prod-1",
            rawDescription = "Leche Alquería",
            quantityValue = 1L,
            quantityUnit = "UNIT",
            listPriceMinor = null,
            discountMinor = null,
            finalPriceMinor = 4800L,
            discountType = "NONE",
            discountEvidence = null,
            confidenceScore = 1f,
            userConfirmed = true
        )
        coEvery { receiptDao.getReceiptItemById("item-123") } returns fakeItem
        coEvery { storeDao.getStoreByName("Carulla") } returns null
        val storeSlot = slot<StoreEntity>()
        coEvery { storeDao.insertStore(capture(storeSlot)) } returns Unit

        editor.updateObservation(
            observationId = "item-123",
            newPrice = Money(5200L, "COP"),
            newQuantity = ProductQuantity(2L, UnitType.UNIT),
            newStoreName = "Carulla"
        )

        coVerify {
            receiptDao.updateReceiptItemPrice("item-123", 5200L, null, 2L, "UNIT")
            receiptDao.updateReceiptStore("rec-456", any())
        }
        assertEquals("Carulla", storeSlot.captured.name)
    }

    @Test
    fun `deleteObservation invokes receiptDao delete`() = runTest {
        editor.deleteObservation("item-to-delete")
        coVerify { receiptDao.deleteReceiptItem("item-to-delete") }
    }
}

