package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.MarketCompareDatabase
import com.barbosa.marketcompare.data.local.dao.PriceObservationItemEntity
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.data.local.dao.StoreStatsQueryResult
import com.barbosa.marketcompare.data.local.dao.StoreSummaryQueryResult
import com.barbosa.marketcompare.domain.model.Money
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class StoreCatalogRepositoryTest {

    private val database = mockk<MarketCompareDatabase>()
    private val receiptDao = mockk<ReceiptDao>()
    private val storeDao = mockk<StoreDao>()

    private lateinit var repository: PurchaseRepositoryImpl

    @Before
    fun setUp() {
        every { database.receiptDao() } returns receiptDao
        every { database.storeDao() } returns storeDao
        every { database.productDao() } returns mockk()
        every { database.shoppingListDao() } returns mockk()
        repository = PurchaseRepositoryImpl(database)
    }

    @Test
    fun `getStoreSummaries maps query results to domain StoreSummary`() = runTest {
        val summaries = listOf(
            StoreSummaryQueryResult(
                storeId = "store-1",
                storeName = "Éxito Calle 80",
                address = "Calle 80 # 65-10",
                productCount = 14,
                lastObservationTimestamp = 1718000000000L
            ),
            StoreSummaryQueryResult(
                storeId = "store-2",
                storeName = "D1 Normandía",
                address = null,
                productCount = 8,
                lastObservationTimestamp = 1718100000000L
            )
        )
        every { storeDao.observeStoreSummaries() } returns flowOf(summaries)

        val result = repository.getStoreSummaries().first()

        assertEquals(2, result.size)
        assertEquals("Éxito Calle 80", result[0].storeName)
        assertEquals(14, result[0].productCount)
        assertEquals("Calle 80 # 65-10", result[0].address)
        assertEquals("D1 Normandía", result[1].storeName)
        assertEquals(8, result[1].productCount)
    }

    @Test
    fun `getProductsForStore returns mapped observations for given store`() = runTest {
        val observations = listOf(
            PriceObservationItemEntity(
                id = "item-1",
                productId = "prod-1",
                rawDescription = "Arroz Diana 1kg",
                unitPriceMinor = 4200L,
                purchaseTimestamp = 1718000000000L,
                storeName = "Éxito"
            ),
            PriceObservationItemEntity(
                id = "item-2",
                productId = "prod-2",
                rawDescription = "Aceite Premier 1L",
                unitPriceMinor = 9500L,
                purchaseTimestamp = 1718000000000L,
                storeName = "Éxito"
            )
        )
        every { receiptDao.observeProductsForStore("Éxito") } returns flowOf(observations)

        val result = repository.getProductsForStore("Éxito").first()

        assertEquals(2, result.size)
        assertEquals("Arroz Diana 1kg", result[0].productName)
        assertEquals(Money(4200L, "COP"), result[0].unitPrice)
        assertEquals("Aceite Premier 1L", result[1].productName)
        assertEquals(Money(9500L, "COP"), result[1].unitPrice)
    }

    @Test
    fun `getPriceTimeline returns observations chronologically ordered`() = runTest {
        val observations = listOf(
            PriceObservationItemEntity(
                id = "item-1",
                productId = "prod-1",
                rawDescription = "Leche Alqueria 1L",
                unitPriceMinor = 4100L,
                purchaseTimestamp = 1717000000000L,
                storeName = "D1"
            ),
            PriceObservationItemEntity(
                id = "item-2",
                productId = "prod-1",
                rawDescription = "Leche Alqueria 1L",
                unitPriceMinor = 4400L,
                purchaseTimestamp = 1718000000000L,
                storeName = "Éxito"
            )
        )
        every { receiptDao.observePriceTimeline("Leche Alqueria 1L") } returns flowOf(observations)

        val timeline = repository.getPriceTimeline("Leche Alqueria 1L").first()

        assertEquals(2, timeline.size)
        assertEquals(Money(4100L, "COP"), timeline[0].unitPrice)
        assertEquals(Money(4400L, "COP"), timeline[1].unitPrice)
        assertEquals("D1", timeline[0].storeName)
        assertEquals("Éxito", timeline[1].storeName)
    }

    @Test
    fun `getPriceObservationsBetween filters observations by timestamp window`() = runTest {
        val start = 1000L
        val end = 5000L
        val observations = listOf(
            PriceObservationItemEntity(
                id = "item-1",
                productId = "prod-1",
                rawDescription = "Pan Tajado",
                unitPriceMinor = 3200L,
                purchaseTimestamp = 3000L,
                storeName = "Ara"
            )
        )
        every { receiptDao.observePriceObservationsBetween(start, end) } returns flowOf(observations)

        val result = repository.getPriceObservationsBetween(start, end).first()

        assertEquals(1, result.size)
        assertEquals("Pan Tajado", result[0].productName)
        assertEquals(Money(3200L, "COP"), result[0].unitPrice)
    }

    @Test
    fun `getStoreStatistics maps query result to domain StorePriceStatistics`() = runTest {
        val statsResult = StoreStatsQueryResult(
            storeName = "D1",
            distinctProductsCount = 42,
            totalObservationsCount = 110,
            lowestPriceMinor = 1200L,
            highestPriceMinor = 32000L,
            lastObservedTimestamp = 1719000000000L
        )
        coEvery { storeDao.getStoreStatistics("D1") } returns statsResult

        val stats = repository.getStoreStatistics("D1")

        assertNotNull(stats)
        assertEquals("D1", stats?.storeName)
        assertEquals(42, stats?.distinctProductsCount)
        assertEquals(110, stats?.totalObservationsCount)
        assertEquals(Money(1200L, "COP"), stats?.lowestPriceRecorded)
        assertEquals(Money(32000L, "COP"), stats?.highestPriceRecorded)
        assertEquals(1719000000000L, stats?.lastObservedTimestamp)
    }
}

