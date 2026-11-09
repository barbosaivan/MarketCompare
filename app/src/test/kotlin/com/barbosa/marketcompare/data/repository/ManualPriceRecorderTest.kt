package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.ProductResolution
import com.barbosa.marketcompare.domain.model.UnitType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ManualPriceRecorderTest {

    private val storeDao = mockk<StoreDao>(relaxed = true)
    private val productDao = mockk<ProductDao>(relaxed = true)
    private val receiptDao = mockk<ReceiptDao>(relaxed = true)
    private val catalogResolver = mockk<ProductCatalogResolver>()

    private lateinit var recorder: ManualPriceRecorder

    @Before
    fun setUp() {
        recorder = ManualPriceRecorder(storeDao, productDao, receiptDao, catalogResolver)
    }

    @Test
    fun `record creates new store and product when they do not exist`() = runTest {
        coEvery { storeDao.getStoreByName("D1 Normandía") } returns null
        coEvery { productDao.getProductById(any()) } returns null
        val generatedProduct = Product("prod-new", "Leche Entera 1L", "Latti", null, 1L, UnitType.LITER, "Bolsa 1L")
        coEvery {
            catalogResolver.resolveProductFromDescription("Leche Entera 1L", any())
        } returns ProductResolution.NewProduct(generatedProduct)

        val insertedStoreSlot = slot<StoreEntity>()
        val insertedProductSlot = slot<ProductEntity>()
        val insertedReceiptSlot = slot<ReceiptEntity>()
        val insertedItemsSlot = slot<List<ReceiptItemEntity>>()

        coEvery { storeDao.insertStore(capture(insertedStoreSlot)) } returns Unit
        coEvery { productDao.insertProduct(capture(insertedProductSlot)) } returns Unit
        coEvery { receiptDao.insertReceipt(capture(insertedReceiptSlot)) } returns Unit
        coEvery { receiptDao.insertItems(capture(insertedItemsSlot)) } returns Unit

        recorder.record(
            storeName = "D1 Normandía",
            productName = "Leche Entera 1L",
            price = Money(3800L, "COP"),
            quantity = ProductQuantity(1L, UnitType.LITER),
            brand = "Latti",
            presentation = "Bolsa 1L",
            discount = null,
            discountType = DiscountType.NONE
        )

        coVerify { storeDao.insertStore(any()) }
        coVerify { productDao.insertProduct(any()) }
        coVerify { receiptDao.insertReceipt(any()) }
        coVerify { receiptDao.insertItems(any()) }

        assertEquals("D1 Normandía", insertedStoreSlot.captured.name)
        assertEquals("Leche Entera 1L", insertedProductSlot.captured.canonicalName)
        assertEquals("Latti", insertedProductSlot.captured.brand)
        assertEquals(3800L, insertedReceiptSlot.captured.totalAmountMinor)

        val items = insertedItemsSlot.captured
        assertEquals(1, items.size)
        assertEquals("Leche Entera 1L", items[0].rawDescription)
        assertEquals(3800L, items[0].finalPriceMinor)
        assertEquals(UnitType.LITER.name, items[0].quantityUnit)
        assertTrue(items[0].userConfirmed)
    }

    @Test
    fun `record reuses existing store and product when available`() = runTest {
        val existingStore = StoreEntity("store-123", "Éxito Calle 80", null, null)
        val existingProduct = Product("prod-456", "Arroz Diana 1kg", "Diana", "Granos", 1L, UnitType.KILOGRAM, "Bolsa")
        val existingProductEntity = ProductEntity("prod-456", "Arroz Diana 1kg", "Diana", "Granos", 1L, "KILOGRAM", "Bolsa")

        coEvery { storeDao.getStoreByName("Éxito Calle 80") } returns existingStore
        coEvery { productDao.getProductById("prod-456") } returns existingProductEntity
        coEvery {
            catalogResolver.resolveProductFromDescription("Arroz Diana 1kg", "store-123")
        } returns ProductResolution.ExactAliasMatch(
            product = existingProduct,
            matchedAlias = "Arroz Diana"
        )

        val insertedReceiptSlot = slot<ReceiptEntity>()
        val insertedItemsSlot = slot<List<ReceiptItemEntity>>()
        coEvery { receiptDao.insertReceipt(capture(insertedReceiptSlot)) } returns Unit
        coEvery { receiptDao.insertItems(capture(insertedItemsSlot)) } returns Unit

        recorder.record(
            storeName = "Éxito Calle 80",
            productName = "Arroz Diana 1kg",
            price = Money(4500L, "COP"),
            quantity = ProductQuantity(1L, UnitType.KILOGRAM),
            brand = "Diana",
            presentation = "Bolsa",
            discount = null,
            discountType = DiscountType.NONE
        )

        coVerify(exactly = 0) { storeDao.insertStore(any()) }
        coVerify(exactly = 0) { productDao.insertProduct(any()) }

        assertEquals("store-123", insertedReceiptSlot.captured.storeId)
        assertEquals("prod-456", insertedItemsSlot.captured[0].productId)
    }

    @Test
    fun `record calculates list price and discount correctly when offer present`() = runTest {
        coEvery { storeDao.getStoreByName("Carulla") } returns StoreEntity("store-c", "Carulla", null, null)
        coEvery { productDao.getProductById(any()) } returns null
        val generatedProduct = Product("prod-cafe", "Café Sello Rojo 500g", "Sello Rojo", null, 500L, UnitType.GRAM, "Empaque")
        coEvery {
            catalogResolver.resolveProductFromDescription("Café Sello Rojo 500g", any())
        } returns ProductResolution.NewProduct(generatedProduct)

        val insertedItemsSlot = slot<List<ReceiptItemEntity>>()
        coEvery { receiptDao.insertItems(capture(insertedItemsSlot)) } returns Unit

        recorder.record(
            storeName = "Carulla",
            productName = "Café Sello Rojo 500g",
            price = Money(15000L, "COP"),
            quantity = ProductQuantity(500L, UnitType.GRAM),
            brand = "Sello Rojo",
            presentation = "Empaque al vacío",
            discount = Money(3000L, "COP"),
            discountType = DiscountType.PROMOTION_2X1
        )

        val item = insertedItemsSlot.captured[0]
        assertEquals(15000L, item.finalPriceMinor)
        assertEquals(18000L, item.listPriceMinor)
        assertEquals(3000L, item.discountMinor)
        assertEquals(DiscountType.PROMOTION_2X1.name, item.discountType)
    }

    @Test
    fun `record ignores invocations with blank store or product name`() = runTest {
        recorder.record(
            storeName = "   ",
            productName = "Pan Tajado",
            price = Money(3000L, "COP"),
            quantity = ProductQuantity(1L, UnitType.UNIT),
            brand = null,
            presentation = null,
            discount = null,
            discountType = DiscountType.NONE
        )

        recorder.record(
            storeName = "Ara",
            productName = "  ",
            price = Money(3000L, "COP"),
            quantity = ProductQuantity(1L, UnitType.UNIT),
            brand = null,
            presentation = null,
            discount = null,
            discountType = DiscountType.NONE
        )

        coVerify(exactly = 0) { receiptDao.insertReceipt(any()) }
        coVerify(exactly = 0) { receiptDao.insertItems(any()) }
    }
}
