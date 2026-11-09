package com.barbosa.marketcompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import kotlinx.coroutines.flow.Flow

data class ReceiptWithStoreAndItems(
    val id: String,
    val storeId: String,
    val storeName: String,
    val purchaseTimestamp: Long,
    val totalAmountMinor: Long,
    val rawDocumentUri: String?
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
        SELECT 
            r.id, 
            r.storeId, 
            s.name as storeName, 
            r.purchaseTimestamp, 
            r.totalAmountMinor, 
            r.rawDocumentUri
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
        WHERE LOWER(TRIM(ri.rawDescription)) = LOWER(TRIM(:productName))
        ORDER BY r.purchaseTimestamp DESC
    """)
    suspend fun getPriceObservationsForProduct(productName: String): List<PriceObservationItemEntity>

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

    @Query("SELECT * FROM receipt_items WHERE id = :itemId LIMIT 1")
    suspend fun getReceiptItemById(itemId: String): ReceiptItemEntity?

    @Query("UPDATE receipt_items SET rawDescription = :newDescription WHERE productId = :productId OR LOWER(TRIM(rawDescription)) = LOWER(TRIM(:oldDescription))")
    suspend fun updateItemDescriptions(productId: String?, oldDescription: String, newDescription: String)

    @Query("UPDATE receipt_items SET finalPriceMinor = :finalPriceMinor, listPriceMinor = :listPriceMinor, quantityValue = :quantityValue, quantityUnit = :quantityUnit WHERE id = :itemId")
    suspend fun updateReceiptItemPrice(itemId: String, finalPriceMinor: Long, listPriceMinor: Long?, quantityValue: Long, quantityUnit: String)

    @Query("DELETE FROM receipt_items WHERE id = :itemId")
    suspend fun deleteReceiptItem(itemId: String)

    @Query("UPDATE receipts SET storeId = :storeId WHERE id = :receiptId")
    suspend fun updateReceiptStore(receiptId: String, storeId: String)
}

