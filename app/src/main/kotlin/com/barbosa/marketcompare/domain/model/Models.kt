package com.barbosa.marketcompare.domain.model

enum class UnitType {
    UNIT,
    GRAM,
    KILOGRAM,
    MILLILITER,
    LITER
}

data class ProductQuantity(
    val value: Long,
    val unit: UnitType
) {
    fun formatDisplay(): String = when (unit) {
        UnitType.UNIT -> "$value und"
        UnitType.GRAM -> "${value}g"
        UnitType.KILOGRAM -> "${value / 1000.0}kg"
        UnitType.MILLILITER -> "${value}ml"
        UnitType.LITER -> "${value / 1000.0}L"
    }
}

enum class DiscountType {
    NONE,
    EXPLICIT_LINE,
    PROMOTION_2X1,
    LOYALTY_MEMBER,
    UNKNOWN
}

data class Store(
    val id: String,
    val name: String,
    val address: String? = null,
    val detectedTaxId: String? = null
)

data class Product(
    val id: String,
    val canonicalName: String,
    val brand: String? = null,
    val category: String? = null,
    val baseQuantity: Long = 1L,
    val unit: UnitType = UnitType.UNIT,
    val presentation: String? = null
)

data class ReceiptItem(
    val id: String,
    val receiptId: String,
    val productId: String? = null,
    val rawDescription: String,
    val quantity: ProductQuantity,
    val listPrice: Money?,
    val discount: Money?,
    val finalPrice: Money,
    val discountType: DiscountType = DiscountType.NONE,
    val discountEvidence: String? = null,
    val confidenceScore: Float = 1.0f,
    val userConfirmed: Boolean = true
)

data class Receipt(
    val id: String,
    val storeId: String,
    val storeName: String,
    val purchaseTimestamp: Long,
    val totalAmount: Money,
    val rawDocumentUri: String? = null,
    val items: List<ReceiptItem> = emptyList()
)

/**
 * Modelo de cotización externa efímero en memoria (ADR 0001).
 * PROHIBIDO persistir en Room, DataStore o disco.
 */
data class ExternalPriceQuote(
    val quoteId: String,
    val providerName: String,
    val productName: String,
    val brand: String?,
    val price: Money,
    val matchConfidence: Float,
    val quotedAtTimestamp: Long,
    val sourceUrl: String? = null
)

