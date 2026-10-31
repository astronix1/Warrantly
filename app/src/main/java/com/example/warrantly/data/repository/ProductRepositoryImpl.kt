package com.example.warrantly.data.repository

import com.example.warrantly.data.local.ProductDao
import com.example.warrantly.data.mapper.toDomain
import com.example.warrantly.data.mapper.toEntity
import com.example.warrantly.domain.model.Product
import com.example.warrantly.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {

    override fun observeAll(): Flow<List<Product>> =
        productDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Product>> =
        productDao.search(query).map { list -> list.map { it.toDomain() } }

    override fun observeWithWarranty(): Flow<List<Product>> =
        productDao.observeWithWarranty().map { list -> list.map { it.toDomain() } }

    override fun countAll(): Flow<Int> = productDao.countAll()

    override suspend fun getById(id: Long): Product? =
        productDao.getById(id)?.toDomain()

    override suspend fun insert(product: Product): Long =
        productDao.insert(product.toEntity())

    override suspend fun update(product: Product) =
        productDao.update(product.toEntity())

    override suspend fun delete(product: Product) =
        productDao.delete(product.toEntity())

    override suspend fun deleteAll() =
        productDao.deleteAll()
}
