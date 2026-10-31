package com.example.warrantly.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.warrantly.data.model.ProductCategory

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val brand: String = "",
    val category: String = ProductCategory.OTHER.name,
    val modelNumber: String = "",
    val serialNumber: String = "",
    val purchaseDate: Long,
    val purchasePriceCents: Long? = null,
    val currencyCode: String = "USD",
    val warrantyStartDate: Long? = null,
    val warrantyEndDate: Long? = null,
    val warrantyDurationMonths: Int? = null,
    val seller: String = "",
    val imageUri: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
