package com.barbosa.marketcompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.barbosa.marketcompare.data.local.entity.ShoppingListItemEntity
import kotlinx.coroutines.flow.Flow

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

    @Query("UPDATE shopping_list_items SET name = :newName WHERE productId = :productId OR LOWER(TRIM(name)) = LOWER(TRIM(:oldName))")
    suspend fun updateItemNames(productId: String?, oldName: String, newName: String)

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
