package com.barbosa.marketcompare.data.local.entity

import com.barbosa.marketcompare.data.local.dao.PriceObservationItemEntity
import com.barbosa.marketcompare.data.local.dao.ReceiptWithStoreAndItems
import com.barbosa.marketcompare.domain.basket.BasketEstimator
import com.barbosa.marketcompare.domain.model.DiscountType
import com.barbosa.marketcompare.domain.model.HistoricalPriceObservation
import com.barbosa.marketcompare.domain.model.Money
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductPriceObservationItem
import com.barbosa.marketcompare.domain.model.ProductQuantity
import com.barbosa.marketcompare.domain.model.Receipt
import com.barbosa.marketcompare.domain.model.ReceiptItem
import com.barbosa.marketcompare.domain.model.ShoppingListItem
import com.barbosa.marketcompare.domain.model.UnitType

fun ProductEntity.toDomain(): Product = Product(
    id = id,
    canonicalName = canonicalName,
    brand = brand,
    category = category,
    baseQuantity = baseQuantity,
    unit = runCatching { UnitType.valueOf(unit) }.getOrDefault(UnitType.UNIT),
    presentation = presentation
)

fun ReceiptItemEntity.toDomain(): ReceiptItem = ReceiptItem(
    id = id,
    receiptId = receiptId,
    productId = productId,
    rawDescription = rawDescription,
    quantity = ProductQuantity(
        value = quantityValue,
        unit = runCatching { UnitType.valueOf(quantityUnit) }.getOrDefault(UnitType.UNIT)
    ),
    listPrice = listPriceMinor?.let { Money(it, "COP") },
    discount = discountMinor?.let { Money(it, "COP") },
    finalPrice = Money(finalPriceMinor, "COP"),
    discountType = runCatching { DiscountType.valueOf(discountType) }.getOrDefault(DiscountType.NONE),
    discountEvidence = discountEvidence,
    confidenceScore = confidenceScore,
    userConfirmed = userConfirmed
)

fun ReceiptWithStoreAndItems.toDomain(items: List<ReceiptItem> = emptyList()): Receipt = Receipt(
    id = id,
    storeId = storeId,
    storeName = storeName,
    purchaseTimestamp = purchaseTimestamp,
    totalAmount = Money(totalAmountMinor, "COP"),
    rawDocumentUri = rawDocumentUri,
    items = items
)

fun ReceiptItem.toEntity(): ReceiptItemEntity = ReceiptItemEntity(
    id = id,
    receiptId = receiptId,
    productId = productId,
    rawDescription = rawDescription,
    quantityValue = quantity.value,
    quantityUnit = quantity.unit.name,
    listPriceMinor = listPrice?.amountMinor,
    discountMinor = discount?.amountMinor,
    finalPriceMinor = finalPrice.amountMinor,
    discountType = discountType.name,
    discountEvidence = discountEvidence,
    confidenceScore = confidenceScore,
    userConfirmed = userConfirmed
)

fun ShoppingListItemEntity.toDomain(estimatedPrice: HistoricalPriceObservation? = null): ShoppingListItem = ShoppingListItem(
    id = id,
    name = name,
    productId = productId,
    quantity = quantity,
    unit = runCatching { UnitType.valueOf(unit) }.getOrDefault(UnitType.UNIT),
    isChecked = isChecked,
    estimatedPrice = estimatedPrice,
    createdAtTimestamp = createdAtTimestamp
)

fun PriceObservationItemEntity.toDomain(isLowestPrice: Boolean = false): ProductPriceObservationItem = ProductPriceObservationItem(
    id = id,
    productId = productId,
    productName = rawDescription,
    storeName = storeName,
    unitPrice = Money(unitPriceMinor, "COP"),
    observedAtTimestamp = purchaseTimestamp,
    isLowestPrice = isLowestPrice,
    freshness = BasketEstimator.evaluateFreshness(purchaseTimestamp)
)

