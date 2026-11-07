package com.barbosa.marketcompare.domain.model

import java.util.UUID

data class ReceiptDraft(
    val id: String = UUID.randomUUID().toString(),
    val detectedStoreName: String?,
    val detectedDateTimestamp: Long,
    val detectedTaxId: String?,
    val detectedTotal: Money?,
    val rawText: String,
    val items: List<ReceiptItemDraft>,
    val rawDocumentUri: String? = null
)

data class ReceiptItemDraft(
    val id: String = UUID.randomUUID().toString(),
    val rawDescription: String,
    val quantity: ProductQuantity,
    val unitListPrice: Money?,
    val discount: Money?,
    val finalPrice: Money,
    val discountType: DiscountType = DiscountType.NONE,
    val discountEvidence: String? = null,
    val confidenceScore: Float = 1.0f,
    val isIncluded: Boolean = true
)

