package com.example.batabung.data.local.dao

import androidx.room.*
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database pada tabel bank.
 */
@Dao
interface BankDao {
    
    // === QUERY ===
    
    @Query("SELECT * FROM bank WHERE userId = :userId ORDER BY createdAt DESC")
    fun getBanksByUser(userId: String): Flow<List<Bank>>
    
    @Query("SELECT * FROM bank WHERE userId = :userId AND isAktif = 1 ORDER BY nama ASC")
    fun getActiveBanksByUser(userId: String): Flow<List<Bank>>
    
    @Query("SELECT * FROM bank WHERE id = :id")
    fun getBankById(id: String): Flow<Bank?>
    
    @Query("SELECT * FROM bank WHERE id = :id")
    suspend fun getBankByIdOnce(id: String): Bank?
    
    @Query("SELECT * FROM bank WHERE userId = :userId AND packageName = :packageName AND isAktif = 1 LIMIT 1")
    suspend fun getBankByPackageName(userId: String, packageName: String): Bank?
    
    @Query("SELECT COUNT(*) FROM bank WHERE userId = :userId")
    suspend fun getCount(userId: String): Int
    
    @Query("SELECT * FROM bank WHERE userId = :userId AND jenis = :jenis ORDER BY nama ASC")
    fun getBanksByJenis(userId: String, jenis: String): Flow<List<Bank>>
    
    // === SYNC ===
    
    @Query("SELECT * FROM bank WHERE syncStatus = :status")
    suspend fun getBanksBySyncStatus(status: SyncStatus): List<Bank>
    
    @Query("UPDATE bank SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
    
    // === INSERT ===
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bank: Bank)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(banks: List<Bank>)
    
    // === UPDATE ===
    
    @Update
    suspend fun update(bank: Bank)
    
    @Query("UPDATE bank SET isAktif = :isAktif, updatedAt = :updatedAt, syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateActiveStatus(
        id: String, 
        isAktif: Boolean, 
        updatedAt: Long = System.currentTimeMillis(),
        syncStatus: SyncStatus = SyncStatus.PENDING
    )
    
    // === DELETE ===
    
    @Delete
    suspend fun delete(bank: Bank)
    
    @Query("DELETE FROM bank WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM bank WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
    
    @Query("DELETE FROM bank")
    suspend fun deleteAll()
    
    // === SYNC HELPERS ===
    
    @Query("SELECT * FROM bank WHERE userId = :userId")
    suspend fun getBanksByUserIdOnce(userId: String): List<Bank>
}
