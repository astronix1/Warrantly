package com.example.warrantly.domain.model

import com.example.warrantly.data.model.ProductCategory

data class Product(
    val id: Long,
    val name: String,
    val brand: String,
    val category: ProductCategory,
    val modelNumber: String,
    val serialNumber: String,
    val purchaseDate: Long,
    val purchasePriceCents: Long?,
    val currencyCode: String,
    val warrantyStartDate: Long?,
    val warrantyEndDate: Long?,
    val warrantyDurationMonths: Int?,
    val seller: String,
    val imageUri: String,
    val notes: String,
    val reminderEnabled: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
