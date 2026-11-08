package com.barbosa.marketcompare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey val id: String,
    val name: String,
    val address: String?,
    val detectedTaxId: String?
)

@Entity(
    tableName = "receipts",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["id"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["storeId"])]
)
data class ReceiptEntity(
    @PrimaryKey val id: String,
    val storeId: String,
    val purchaseTimestamp: Long,
    val totalAmountMinor: Long,
    val rawDocumentUri: String?
)

@Entity(
    tableName = "products",
    indices = [Index(value = ["canonicalName"])]
)
data class ProductEntity(
    @PrimaryKey val id: String,
    val canonicalName: String,
    val brand: String?,
    val category: String?,
    val baseQuantity: Long,
    val unit: String,
    val presentation: String?
)

@Entity(
    tableName = "receipt_items",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receiptId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["receiptId"]),
        Index(value = ["productId"])
    ]
)
data class ReceiptItemEntity(
    @PrimaryKey val id: String,
    val receiptId: String,
    val productId: String?,
    val rawDescription: String,
    val quantityValue: Long,
    val quantityUnit: String,
    val listPriceMinor: Long?,
    val discountMinor: Long?,
    val finalPriceMinor: Long,
    val discountType: String,
    val discountEvidence: String?,
    val confidenceScore: Float,
    val userConfirmed: Boolean
)

@Entity(
    tableName = "product_aliases",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["productId"]), Index(value = ["aliasText"])]
)
data class ProductAliasEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val aliasText: String,
    val storeId: String?
)

@Entity(
    tableName = "shopping_list_items",
    indices = [Index(value = ["createdAtTimestamp"])]
)
data class ShoppingListItemEntity(
    @PrimaryKey val id: String,
    val name: String,
    val productId: String?,
    val quantity: Long,
    val unit: String,
    val isChecked: Boolean,
    val createdAtTimestamp: Long
)


