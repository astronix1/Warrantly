package com.example.warrantly.domain.repository

import com.example.warrantly.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeForProduct(productId: Long): Flow<List<Document>>
    fun observeAll(): Flow<List<Document>>
    suspend fun getById(id: Long): Document?
    suspend fun insert(document: Document): Long
    suspend fun delete(document: Document)
    suspend fun deleteForProduct(productId: Long)
    suspend fun deleteAll()
}
