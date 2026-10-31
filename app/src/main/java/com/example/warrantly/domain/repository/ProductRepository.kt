package com.example.warrantly.domain.repository

import com.example.warrantly.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeAll(): Flow<List<Product>>
    fun search(query: String): Flow<List<Product>>
    fun observeWithWarranty(): Flow<List<Product>>
    fun countAll(): Flow<Int>
    suspend fun getById(id: Long): Product?
    suspend fun insert(product: Product): Long
    suspend fun update(product: Product)
    suspend fun delete(product: Product)
    suspend fun deleteAll()
}
