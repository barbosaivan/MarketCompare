package com.barbosa.marketcompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

data class StoreSummaryQueryResult(
    val storeId: String,
    val storeName: String,
    val address: String?,
    val productCount: Int,
    val lastObservationTimestamp: Long?
)

data class StoreStatsQueryResult(
    val storeName: String,
    val distinctProductsCount: Int,
    val totalObservationsCount: Int,
    val lowestPriceMinor: Long?,
    val highestPriceMinor: Long?,
    val lastObservedTimestamp: Long?
)

@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)

    @Query("SELECT * FROM stores WHERE id = :storeId")
    suspend fun getStoreById(storeId: String): StoreEntity?

    @Query("SELECT * FROM stores WHERE LOWER(TRIM(name)) = LOWER(TRIM(:name)) LIMIT 1")
    suspend fun getStoreByName(name: String): StoreEntity?

    @Query("SELECT * FROM stores WHERE name LIKE '%' || :query || '%' LIMIT 10")
    fun searchStores(query: String): Flow<List<StoreEntity>>

    @Query("SELECT DISTINCT name FROM stores ORDER BY name ASC")
    fun getAllStoreNames(): Flow<List<String>>

    @Query("""
        SELECT 
            s.id AS storeId,
            s.name AS storeName,
            s.address AS address,
            COUNT(DISTINCT ri.rawDescription) AS productCount,
            MAX(r.purchaseTimestamp) AS lastObservationTimestamp
        FROM stores s
        LEFT JOIN receipts r ON r.storeId = s.id
        LEFT JOIN receipt_items ri ON ri.receiptId = r.id
        GROUP BY s.id, s.name, s.address
        ORDER BY s.name ASC
    """)
    fun observeStoreSummaries(): Flow<List<StoreSummaryQueryResult>>

    @Query("""
        SELECT 
            s.name AS storeName,
            COUNT(DISTINCT ri.rawDescription) AS distinctProductsCount,
            COUNT(ri.id) AS totalObservationsCount,
            MIN(CASE WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue ELSE ri.finalPriceMinor END) AS lowestPriceMinor,
            MAX(CASE WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue ELSE ri.finalPriceMinor END) AS highestPriceMinor,
            MAX(r.purchaseTimestamp) AS lastObservedTimestamp
        FROM stores s
        LEFT JOIN receipts r ON r.storeId = s.id
        LEFT JOIN receipt_items ri ON ri.receiptId = r.id
        WHERE s.name = :storeName
        GROUP BY s.name
        LIMIT 1
    """)
    suspend fun getStoreStatistics(storeName: String): StoreStatsQueryResult?
}
