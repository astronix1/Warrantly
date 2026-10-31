package com.example.warrantly.data.repository

import com.example.warrantly.data.local.DocumentDao
import com.example.warrantly.data.mapper.toDomain
import com.example.warrantly.data.mapper.toEntity
import com.example.warrantly.domain.model.Document
import com.example.warrantly.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DocumentRepositoryImpl(
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun observeForProduct(productId: Long): Flow<List<Document>> =
        documentDao.observeForProduct(productId).map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Document>> =
        documentDao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Document? =
        documentDao.getById(id)?.toDomain()

    override suspend fun insert(document: Document): Long =
        documentDao.insert(document.toEntity())

    override suspend fun delete(document: Document) =
        documentDao.delete(document.toEntity())

    override suspend fun deleteForProduct(productId: Long) =
        documentDao.deleteForProduct(productId)

    override suspend fun deleteAll() =
        documentDao.deleteAll()
}
