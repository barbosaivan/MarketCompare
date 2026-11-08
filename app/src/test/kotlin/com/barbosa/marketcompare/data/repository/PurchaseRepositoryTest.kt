package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.MarketCompareDatabase
import com.barbosa.marketcompare.data.local.dao.PriceObservationItemEntity
import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.ReceiptWithStoreAndItems
import com.barbosa.marketcompare.data.local.dao.ShoppingListDao
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ShoppingListItemEntity
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductResolution
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.Store
import com.barbosa.marketcompare.domain.model.UnitType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PurchaseRepositoryTest {

    private val database = mockk<MarketCompareDatabase>()
    private val receiptDao = mockk<ReceiptDao>()
    private val storeDao = mockk<StoreDao>()
    private val productDao = mockk<ProductDao>()
    private val shoppingListDao = mockk<ShoppingListDao>()

    private lateinit var repository: PurchaseRepositoryImpl

    @Before
    fun setUp() {
        every { database.receiptDao() } returns receiptDao
        every { database.storeDao() } returns storeDao
        every { database.productDao() } returns productDao
        every { database.shoppingListDao() } returns shoppingListDao
        repository = PurchaseRepositoryImpl(database)
    }

    @Test
    fun `getAllReceipts maps database entities to domain receipts with exact money`() = runTest {
        val dbList = listOf(
            ReceiptWithStoreAndItems(
                id = "rcpt-1",
                storeId = "store-1",
                storeName = "Éxito Calle 80",
                purchaseTimestamp = 1718000000000L,
                totalAmountMinor = 45900L,
                rawDocumentUri = null
            )
        )
        every { receiptDao.getAllReceiptsWithStore() } returns flowOf(dbList)

        val receipts = repository.getAllReceipts().first()

        assertEquals(1, receipts.size)
        assertEquals("rcpt-1", receipts[0].id)
        assertEquals("Éxito Calle 80", receipts[0].storeName)
        assertEquals(Money(45900L, "COP"), receipts[0].totalAmount)
    }

    @Test
    fun `saveConfirmedPurchase calls store and receipt DAOs transactionally`() = runTest {
        val store = Store(id = "store-1", name = "Olímpica")
        val receipt = Receipt(
            id = "rcpt-2",
            storeId = "store-1",
            storeName = "Olímpica",
            purchaseTimestamp = 1718100000000L,
            totalAmount = Money(12000L, "COP"),
            items = emptyList()
        )

        coEvery { storeDao.insertStore(any()) } returns Unit
        coEvery { receiptDao.insertReceipt(any()) } returns Unit

        repository.saveConfirmedPurchase(store, receipt)

        coVerify(exactly = 1) { storeDao.insertStore(match { it.id == "store-1" && it.name == "Olímpica" }) }
        coVerify(exactly = 1) { receiptDao.insertReceipt(match { it.id == "rcpt-2" && it.totalAmountMinor == 12000L }) }
    }

    @Test
    fun `resolveProductFromDescription returns exact match when alias exists`() = runTest {
        val aliasEntity = ProductAliasEntity(
            id = "alias-1",
            productId = "prod-1",
            aliasText = "LECHE ALQUERIA ENT 1L",
            storeId = null
        )
        val productEntity = ProductEntity(
            id = "prod-1",
            canonicalName = "Alquería Leche Entera 1L Bolsa",
            brand = "Alquería",
            category = "Lácteos",
            baseQuantity = 1000L,
            unit = "LITER",
            presentation = "Bolsa"
        )

        coEvery { productDao.findByAlias("LECHE ALQUERIA ENT 1L") } returns aliasEntity
        coEvery { productDao.getProductById("prod-1") } returns productEntity

        val result = repository.resolveProductFromDescription("leche alquería ent 1L")

        assertTrue(result is ProductResolution.ExactAliasMatch)
        val match = result as ProductResolution.ExactAliasMatch
        assertEquals("prod-1", match.product.id)
        assertEquals("Alquería Leche Entera 1L Bolsa", match.product.canonicalName)
    }

    @Test
    fun `registerProductWithAlias persists product and clean alias`() = runTest {
        val product = Product(
            id = "prod-new",
            canonicalName = "Florhuila Arroz 1000g Bolsa",
            brand = "Florhuila",
            category = "Despensa",
            baseQuantity = 1000L,
            unit = UnitType.GRAM,
            presentation = "Bolsa"
        )

        coEvery { productDao.insertProduct(any()) } returns Unit
        coEvery { productDao.insertAlias(any()) } returns Unit

        val saved = repository.registerProductWithAlias(product, "  arrz florh 1kg  ")

        assertEquals("prod-new", saved.id)
        coVerify(exactly = 1) {
            productDao.insertProduct(match { it.id == "prod-new" && it.canonicalName == "Florhuila Arroz 1000g Bolsa" })
        }
        coVerify(exactly = 1) {
            productDao.insertAlias(match { it.productId == "prod-new" && it.aliasText == "ARRZ FLORH 1KG" })
        }
    }

    @Test
    fun `addShoppingListItem inserts entity with clean name and positive quantity`() = runTest {
        coEvery { shoppingListDao.insertItem(any()) } returns Unit

        repository.addShoppingListItem("  Café Juan Valdez  ", "prod-cafe", 2L, UnitType.UNIT)

        coVerify(exactly = 1) {
            shoppingListDao.insertItem(
                match {
                    it.name == "Café Juan Valdez" &&
                            it.productId == "prod-cafe" &&
                            it.quantity == 2L &&
                            !it.isChecked
                }
            )
        }
    }

    @Test
    fun `toggleItemChecked updates checked status in DAO`() = runTest {
        coEvery { shoppingListDao.updateCheckedStatus(any(), any()) } returns Unit

        repository.toggleItemChecked("item-123", true)

        coVerify(exactly = 1) {
            shoppingListDao.updateCheckedStatus("item-123", true)
        }
    }

    @Test
    fun `clearCheckedShoppingListItems delegates to DAO`() = runTest {
        coEvery { shoppingListDao.deleteCheckedItems() } returns Unit

        repository.clearCheckedShoppingListItems()

        coVerify(exactly = 1) {
            shoppingListDao.deleteCheckedItems()
        }
    }

    @Test
    fun `removeShoppingListItem deletes item from DAO`() = runTest {
        coEvery { shoppingListDao.deleteItemById(any()) } returns Unit

        repository.removeShoppingListItem("item-456")

        coVerify(exactly = 1) {
            shoppingListDao.deleteItemById("item-456")
        }
    }

    @Test
    fun `getLatestPriceObservation returns mapped domain observation when record exists`() = runTest {
        val queryResult = com.barbosa.marketcompare.data.local.dao.PriceObservationQueryResult(
            productId = "prod-arroz",
            rawDescription = "ARROZ FLORHUILA 1KG",
            unitPriceMinor = 4200L,
            purchaseTimestamp = 1718000000000L,
            storeName = "Éxito"
        )
        coEvery { shoppingListDao.getLatestPriceObservation("prod-arroz", "Arroz") } returns queryResult

        val obs = repository.getLatestPriceObservation("prod-arroz", "Arroz")

        org.junit.Assert.assertNotNull(obs)
        assertEquals(Money(4200L, "COP"), obs?.unitPrice)
        assertEquals("Éxito", obs?.storeName)
        assertEquals(1718000000000L, obs?.observedAtTimestamp)
    }

    @Test
    fun `getAllPriceObservations computes lowest price correctly for competing stores`() = runTest {
        val now = System.currentTimeMillis()
        val observations = listOf(
            PriceObservationItemEntity(
                id = "item-1",
                productId = "prod-1",
                rawDescription = "Leche Entera 1L",
                unitPriceMinor = 4500L,
                purchaseTimestamp = now,
                storeName = "Éxito"
            ),
            PriceObservationItemEntity(
                id = "item-2",
                productId = "prod-1",
                rawDescription = "Leche Entera 1L",
                unitPriceMinor = 3800L,
                purchaseTimestamp = now,
                storeName = "D1"
            )
        )
        every { receiptDao.observeAllPriceObservations() } returns flowOf(observations)

        val result = repository.getAllPriceObservations().first()

        assertEquals(2, result.size)
        val exitoItem = result.first { it.storeName == "Éxito" }
        val d1Item = result.first { it.storeName == "D1" }

        org.junit.Assert.assertFalse(exitoItem.isLowestPrice)
        assertTrue(d1Item.isLowestPrice)
        assertEquals(Money(3800L, "COP"), d1Item.unitPrice)
    }

    @Test
    fun `getAllStoreNames delegates to storeDao`() = runTest {
        val stores = listOf("Ara", "D1", "Éxito", "Jumbo")
        every { storeDao.getAllStoreNames() } returns flowOf(stores)

        val result = repository.getAllStoreNames().first()

        assertEquals(4, result.size)
        assertEquals("Ara", result[0])
        assertEquals("Jumbo", result[3])
    }

    @Test
    fun `getCrossStoreComparison fetches observations and calculates cross-store savings`() = runTest {
        val now = System.currentTimeMillis()
        val observations = listOf(
            PriceObservationItemEntity(
                id = "obs-1",
                productId = "prod-1",
                rawDescription = "Huevos AA x30",
                unitPriceMinor = 18000L,
                purchaseTimestamp = now,
                storeName = "Éxito"
            ),
            PriceObservationItemEntity(
                id = "obs-2",
                productId = "prod-1",
                rawDescription = "Huevos AA x30",
                unitPriceMinor = 15500L,
                purchaseTimestamp = now,
                storeName = "D1"
            )
        )
        coEvery { receiptDao.getPriceObservationsForProduct("Huevos AA x30") } returns observations

        val summary = repository.getCrossStoreComparison("Huevos AA x30")

        org.junit.Assert.assertNotNull(summary)
        assertEquals("D1", summary?.cheapestStore)
        assertEquals(Money(15500L, "COP"), summary?.lowestPrice)
        assertEquals(Money(2500L, "COP"), summary?.maxSavings)
    }
}

