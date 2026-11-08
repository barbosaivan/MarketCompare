package com.barbosa.marketcompare.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.dao.ShoppingListDao
import com.barbosa.marketcompare.data.local.dao.StoreDao
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptEntity
import com.barbosa.marketcompare.data.local.entity.ReceiptItemEntity
import com.barbosa.marketcompare.data.local.entity.ShoppingListItemEntity
import com.barbosa.marketcompare.data.local.entity.StoreEntity

@Database(
    entities = [
        StoreEntity::class,
        ReceiptEntity::class,
        ReceiptItemEntity::class,
        ProductEntity::class,
        ProductAliasEntity::class,
        ShoppingListItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MarketCompareDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    abstract fun receiptDao(): ReceiptDao
    abstract fun productDao(): ProductDao
    abstract fun shoppingListDao(): ShoppingListDao

    companion object {
        @Volatile
        private var INSTANCE: MarketCompareDatabase? = null

        fun getDatabase(context: Context): MarketCompareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MarketCompareDatabase::class.java,
                    "market_compare_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

