package com.barbosa.marketcompare.data.repository

import com.barbosa.marketcompare.data.local.dao.ProductDao
import com.barbosa.marketcompare.data.local.dao.ReceiptDao
import com.barbosa.marketcompare.data.local.entity.ProductAliasEntity
import com.barbosa.marketcompare.data.local.entity.ProductEntity
import com.barbosa.marketcompare.data.local.entity.toDomain
import com.barbosa.marketcompare.domain.model.Product
import com.barbosa.marketcompare.domain.model.ProductResolution
import com.barbosa.marketcompare.domain.normalizer.ProductNormalizer
import java.util.UUID

internal class ProductCatalogResolver(
    private val productDao: ProductDao,
    private val receiptDao: ReceiptDao
) {

    suspend fun resolveProductFromDescription(
        rawDescription: String,
        storeId: String?
    ): ProductResolution {
        val cleanAlias = ProductNormalizer.normalizeAlias(rawDescription)

        val existingAlias = productDao.findByAlias(cleanAlias)
            ?: productDao.findByAlias(rawDescription.trim().uppercase())

        if (existingAlias != null) {
            val matchedProduct = productDao.getProductById(existingAlias.productId)?.toDomain()
            if (matchedProduct != null) {
                return ProductResolution.ExactAliasMatch(matchedProduct, existingAlias.aliasText)
            }
        }

        val candidate = ProductNormalizer.normalize(rawDescription)
        val candidateProduct = candidate.toProduct(UUID.randomUUID().toString())

        return if (candidate.confidenceScore >= 0.85f) {
            ProductResolution.SuggestedCandidate(candidateProduct, candidate.confidenceScore)
        } else {
            ProductResolution.NewProduct(candidateProduct)
        }
    }

    suspend fun registerProductWithAlias(
        product: Product,
        aliasText: String,
        storeId: String?
    ): Product {
        productDao.insertProduct(
            ProductEntity(
                id = product.id,
                canonicalName = product.canonicalName,
                brand = product.brand,
                category = product.category,
                baseQuantity = product.baseQuantity,
                unit = product.unit.name,
                presentation = product.presentation
            )
        )

        val cleanAlias = ProductNormalizer.normalizeAlias(aliasText)
        productDao.insertAlias(
            ProductAliasEntity(
                id = UUID.randomUUID().toString(),
                productId = product.id,
                aliasText = cleanAlias,
                storeId = storeId
            )
        )

        return product
    }

    suspend fun linkItemToProduct(
        receiptItemId: String,
        productId: String,
        aliasText: String?,
        storeId: String?
    ) {
        receiptDao.linkItemToProduct(receiptItemId, productId)
        if (!aliasText.isNullOrBlank()) {
            val cleanAlias = ProductNormalizer.normalizeAlias(aliasText)
            productDao.insertAlias(
                ProductAliasEntity(
                    id = UUID.randomUUID().toString(),
                    productId = productId,
                    aliasText = cleanAlias,
                    storeId = storeId
                )
            )
        }
    }
}

