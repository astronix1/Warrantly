package com.example.warrantly.data.mapper

import com.example.warrantly.data.local.DocumentEntity
import com.example.warrantly.data.local.ProductEntity
import com.example.warrantly.data.model.ProductCategory
import com.example.warrantly.domain.model.Document
import com.example.warrantly.domain.model.Product

fun ProductEntity.toDomain(): Product {
    val resolvedCategory = try {
        ProductCategory.valueOf(category)
    } catch (e: IllegalArgumentException) {
        ProductCategory.OTHER
    }
    return Product(
        id = id,
        name = name,
        brand = brand,
        category = resolvedCategory,
        modelNumber = modelNumber,
        serialNumber = serialNumber,
        purchaseDate = purchaseDate,
        purchasePriceCents = purchasePriceCents,
        currencyCode = currencyCode,
        warrantyStartDate = warrantyStartDate,
        warrantyEndDate = warrantyEndDate,
        warrantyDurationMonths = warrantyDurationMonths,
        seller = seller,
        imageUri = imageUri,
        notes = notes,
        reminderEnabled = reminderEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = id,
        name = name,
        brand = brand,
        category = category.name,
        modelNumber = modelNumber,
        serialNumber = serialNumber,
        purchaseDate = purchaseDate,
        purchasePriceCents = purchasePriceCents,
        currencyCode = currencyCode,
        warrantyStartDate = warrantyStartDate,
        warrantyEndDate = warrantyEndDate,
        warrantyDurationMonths = warrantyDurationMonths,
        seller = seller,
        imageUri = imageUri,
        notes = notes,
        reminderEnabled = reminderEnabled,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun DocumentEntity.toDomain(): Document {
    return Document(
        id = id,
        productId = productId,
        displayName = displayName,
        mimeType = mimeType,
        relativePath = relativePath,
        fileSizeBytes = fileSizeBytes,
        addedAt = addedAt
    )
}

fun Document.toEntity(): DocumentEntity {
    return DocumentEntity(
        id = id,
        productId = productId,
        displayName = displayName,
        mimeType = mimeType,
        relativePath = relativePath,
        fileSizeBytes = fileSizeBytes,
        addedAt = addedAt
    )
}
