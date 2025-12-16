package com.example.batabung.data.local.dao

import androidx.room.*
import com.example.batabung.data.local.entity.Tabungan
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database pada tabel Tabungan.
 */
@Dao
interface TabunganDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tabungan: Tabungan): Long
    
    @Update
    suspend fun update(tabungan: Tabungan)
    
    @Delete
    suspend fun delete(tabungan: Tabungan)
    
    @Query("SELECT * FROM tabungan ORDER BY createdAt DESC")
    fun getAllTabungan(): Flow<List<Tabungan>>
    
    @Query("SELECT * FROM tabungan WHERE id = :id")
    fun getTabunganById(id: Long): Flow<Tabungan?>
    
    @Query("SELECT * FROM tabungan WHERE id = :id")
    suspend fun getTabunganByIdOnce(id: Long): Tabungan?
    
    @Query("SELECT COUNT(*) FROM tabungan")
    suspend fun getCount(): Int
}
