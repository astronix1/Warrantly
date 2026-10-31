package com.example.warrantly.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ProductEntity): Long

    @Update
    suspend fun update(product: ProductEntity)

    @Delete
    suspend fun delete(product: ProductEntity)

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): ProductEntity?

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ProductEntity>>

    @Query(
        """
        SELECT * FROM products
        WHERE name LIKE '%' || :query || '%'
           OR brand LIKE '%' || :query || '%'
           OR modelNumber LIKE '%' || :query || '%'
           OR serialNumber LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
        """
    )
    fun search(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE warrantyEndDate IS NOT NULL ORDER BY warrantyEndDate ASC")
    fun observeWithWarranty(): Flow<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun countAll(): Flow<Int>

    @Query("DELETE FROM products")
    suspend fun deleteAll()
}
