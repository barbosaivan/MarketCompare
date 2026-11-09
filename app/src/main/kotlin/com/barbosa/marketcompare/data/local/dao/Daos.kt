package com.barbosa.marketcompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import com.barbosa.marketcompare.data.local.entity.ShoppingListItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity
import kotlinx.coroutines.flow.Flow

data class ReceiptWithStoreAndItems(
    val id: String,
    val storeId: String,
    val storeName: String,
    val purchaseTimestamp: Long,
    val totalAmountMinor: Long,
    val rawDocumentUri: String?
)

data class StoreSummaryQueryResult(
    val storeId: String,
    val storeName: String,
    val address: String?,
    val productCount: Int,
    val lastObservationTimestamp: Long?
)

@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)

    @Query("SELECT * FROM stores WHERE id = :storeId")
    suspend fun getStoreById(storeId: String): StoreEntity?

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

data class StoreStatsQueryResult(
    val storeName: String,
    val distinctProductsCount: Int,
    val totalObservationsCount: Int,
    val lowestPriceMinor: Long?,
    val highestPriceMinor: Long?,
    val lastObservedTimestamp: Long?
)

data class PriceObservationItemEntity(
    val id: String,
    val productId: String?,
    val rawDescription: String,
    val unitPriceMinor: Long,
    val purchaseTimestamp: Long,
    val storeName: String
)

@Dao
interface ReceiptDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: ReceiptEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ReceiptItemEntity>)

    @Query("""
        SELECT r.id, r.storeId, s.name AS storeName, r.purchaseTimestamp, r.totalAmountMinor, r.rawDocumentUri
        FROM receipts r
        INNER JOIN stores s ON r.storeId = s.id
        ORDER BY r.purchaseTimestamp DESC
    """)
    fun getAllReceiptsWithStore(): Flow<List<ReceiptWithStoreAndItems>>

    @Query("""
        SELECT 
            ri.id,
            ri.productId,
            ri.rawDescription,
            CASE 
                WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue 
                ELSE ri.finalPriceMinor 
            END as unitPriceMinor,
            r.purchaseTimestamp,
            s.name as storeName
        FROM receipt_items ri
        INNER JOIN receipts r ON ri.receiptId = r.id
        INNER JOIN stores s ON r.storeId = s.id
        ORDER BY r.purchaseTimestamp DESC
    """)
    fun observeAllPriceObservations(): Flow<List<PriceObservationItemEntity>>

    @Query("""
        SELECT 
            ri.id,
            ri.productId,
            ri.rawDescription,
            CASE 
                WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue 
                ELSE ri.finalPriceMinor 
            END as unitPriceMinor,
            r.purchaseTimestamp,
            s.name as storeName
        FROM receipt_items ri
        INNER JOIN receipts r ON ri.receiptId = r.id
        INNER JOIN stores s ON r.storeId = s.id
        WHERE ri.rawDescription LIKE '%' || :queryName || '%'
        ORDER BY r.purchaseTimestamp DESC
    """)
    suspend fun getPriceObservationsForProduct(queryName: String): List<PriceObservationItemEntity>

    @Query("""
        SELECT 
            ri.id,
            ri.productId,
            ri.rawDescription,
            CASE 
                WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue 
                ELSE ri.finalPriceMinor 
            END as unitPriceMinor,
            r.purchaseTimestamp,
            s.name as storeName
        FROM receipt_items ri
        INNER JOIN receipts r ON ri.receiptId = r.id
        INNER JOIN stores s ON r.storeId = s.id
        WHERE s.name = :storeName
        ORDER BY r.purchaseTimestamp DESC
    """)
    fun observeProductsForStore(storeName: String): Flow<List<PriceObservationItemEntity>>

    @Query("""
        SELECT 
            ri.id,
            ri.productId,
            ri.rawDescription,
            CASE 
                WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue 
                ELSE ri.finalPriceMinor 
            END as unitPriceMinor,
            r.purchaseTimestamp,
            s.name as storeName
        FROM receipt_items ri
        INNER JOIN receipts r ON ri.receiptId = r.id
        INNER JOIN stores s ON r.storeId = s.id
        WHERE ri.rawDescription LIKE '%' || :productName || '%'
        ORDER BY r.purchaseTimestamp ASC
    """)
    fun observePriceTimeline(productName: String): Flow<List<PriceObservationItemEntity>>

    @Query("""
        SELECT 
            ri.id,
            ri.productId,
            ri.rawDescription,
            CASE 
                WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue 
                ELSE ri.finalPriceMinor 
            END as unitPriceMinor,
            r.purchaseTimestamp,
            s.name as storeName
        FROM receipt_items ri
        INNER JOIN receipts r ON ri.receiptId = r.id
        INNER JOIN stores s ON r.storeId = s.id
        WHERE r.purchaseTimestamp BETWEEN :startTimestamp AND :endTimestamp
        ORDER BY r.purchaseTimestamp DESC
    """)
    fun observePriceObservationsBetween(startTimestamp: Long, endTimestamp: Long): Flow<List<PriceObservationItemEntity>>

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    suspend fun getItemsForReceipt(receiptId: String): List<ReceiptItemEntity>

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    fun observeItemsForReceipt(receiptId: String): Flow<List<ReceiptItemEntity>>

    @Query("UPDATE receipt_items SET productId = :productId WHERE id = :itemId")
    suspend fun linkItemToProduct(itemId: String, productId: String)
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: ProductAliasEntity)

    @Query("SELECT * FROM products ORDER BY canonicalName ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :productId LIMIT 1")
    suspend fun getProductById(productId: String): ProductEntity?

    @Query("SELECT * FROM products WHERE canonicalName LIKE '%' || :query || '%' LIMIT 20")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product_aliases WHERE aliasText = :alias LIMIT 1")
    suspend fun findByAlias(alias: String): ProductAliasEntity?

    @Query("SELECT * FROM product_aliases WHERE productId = :productId")
    suspend fun getAliasesForProduct(productId: String): List<ProductAliasEntity>
}

data class PriceObservationQueryResult(
    val productId: String?,
    val rawDescription: String,
    val unitPriceMinor: Long,
    val purchaseTimestamp: Long,
    val storeName: String
)

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items ORDER BY createdAtTimestamp ASC")
    fun getAllShoppingListItems(): Flow<List<ShoppingListItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItemEntity)

    @Update
    suspend fun updateItem(item: ShoppingListItemEntity)

    @Query("UPDATE shopping_list_items SET isChecked = :isChecked WHERE id = :id")
    suspend fun updateCheckedStatus(id: String, isChecked: Boolean)

    @Query("DELETE FROM shopping_list_items WHERE id = :id")
    suspend fun deleteItemById(id: String)

    @Query("DELETE FROM shopping_list_items WHERE isChecked = 1")
    suspend fun deleteCheckedItems()

    @Query("""
        SELECT 
            ri.productId,
            ri.rawDescription,
            CASE 
                WHEN ri.quantityValue > 0 THEN ri.finalPriceMinor / ri.quantityValue 
                ELSE ri.finalPriceMinor 
            END as unitPriceMinor,
            r.purchaseTimestamp,
            s.name as storeName
        FROM receipt_items ri
        INNER JOIN receipts r ON ri.receiptId = r.id
        INNER JOIN stores s ON r.storeId = s.id
        WHERE (:productId IS NOT NULL AND ri.productId = :productId) 
           OR ri.rawDescription LIKE '%' || :queryName || '%'
        ORDER BY r.purchaseTimestamp DESC
        LIMIT 1
    """)
    suspend fun getLatestPriceObservation(productId: String?, queryName: String): PriceObservationQueryResult?
}


