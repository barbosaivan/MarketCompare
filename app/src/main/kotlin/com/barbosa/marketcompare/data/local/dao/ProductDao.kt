package com.barbosa.marketcompare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

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

