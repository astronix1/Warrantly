package com.example.warrantly.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(doc: DocumentEntity): Long

    @Delete
    suspend fun delete(doc: DocumentEntity)

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getById(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE productId = :productId ORDER BY addedAt DESC")
    fun observeForProduct(productId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<DocumentEntity>>

    @Query("DELETE FROM documents WHERE productId = :productId")
    suspend fun deleteForProduct(productId: Long)

    @Query("DELETE FROM documents")
    suspend fun deleteAll()
}
