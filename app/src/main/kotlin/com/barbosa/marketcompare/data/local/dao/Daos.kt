package com.barbosa.marketcompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
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

@Dao
interface StoreDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity)

    @Query("SELECT * FROM stores WHERE id = :storeId")
    suspend fun getStoreById(storeId: String): StoreEntity?

    @Query("SELECT * FROM stores WHERE name LIKE '%' || :query || '%' LIMIT 10")
    fun searchStores(query: String): Flow<List<StoreEntity>>
}

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

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    suspend fun getItemsForReceipt(receiptId: String): List<ReceiptItemEntity>

    @Query("SELECT * FROM receipt_items WHERE receiptId = :receiptId")
    fun observeItemsForReceipt(receiptId: String): Flow<List<ReceiptItemEntity>>
}

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlias(alias: ProductAliasEntity)

    @Query("SELECT * FROM products ORDER BY canonicalName ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE canonicalName LIKE '%' || :query || '%' LIMIT 20")
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product_aliases WHERE aliasText = :alias LIMIT 1")
    suspend fun findByAlias(alias: String): ProductAliasEntity?
}

