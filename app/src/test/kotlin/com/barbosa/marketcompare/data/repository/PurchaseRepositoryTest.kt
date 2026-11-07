package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.MarketCompareDatabase
import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.ReceiptWithStoreAndItems
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.Store
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class PurchaseRepositoryTest {

    private val database = mockk<MarketCompareDatabase>()
    private val receiptDao = mockk<ReceiptDao>()
    private val storeDao = mockk<StoreDao>()
    private val productDao = mockk<ProductDao>()

    private lateinit var repository: PurchaseRepositoryImpl

    @Before
    fun setUp() {
        every { database.receiptDao() } returns receiptDao
        every { database.storeDao() } returns storeDao
        every { database.productDao() } returns productDao
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
}

