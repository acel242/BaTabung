package com.example.batabung.data.repository

import android.util.Log
import com.example.batabung.data.local.dao.BankDao
import com.example.batabung.data.local.dao.TransaksiDao
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.SyncStatus
import com.example.batabung.data.local.entity.Transaksi
import com.example.batabung.data.remote.SupabaseDataSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sealed class untuk status sinkronisasi.
 */
sealed class SyncState {
    data object Idle : SyncState()
    data object Syncing : SyncState()
    data class Success(val message: String) : SyncState()
    data class Error(val message: String) : SyncState()
}

/**
 * Repository untuk sinkronisasi data antara Room dan Supabase.
 * Menggunakan strategi offline-first dengan conflict resolution berbasis timestamp.
 * 
 * Full Account Sync: account-centric, bukan device-centric
 */
@Singleton
class SyncRepository @Inject constructor(
    private val bankDao: BankDao,
    private val transaksiDao: TransaksiDao,
    private val remoteDataSource: SupabaseDataSource,
    private val authRepository: AuthRepository
) {
    
    companion object {
        private const val TAG = "SyncRepository"
    }
    
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()
    
    /**
     * Mendapatkan user ID saat ini dari AuthRepository.
     * Throws exception jika user belum terautentikasi.
     */
    private val currentUserId: String
        get() = authRepository.currentUserId 
            ?: throw IllegalStateException("User not authenticated")
    
    /**
     * Cek apakah ada data lokal untuk user ini.
     * Digunakan untuk menentukan apakah perlu initial download atau regular sync.
     */
    suspend fun hasLocalData(userId: String): Boolean {
        val banks = bankDao.getBanksByUserIdOnce(userId)
        return banks.isNotEmpty()
    }
    
    /**
     * Perform initial download untuk fresh device login.
     * PENTING: Ini akan menghapus data lokal lalu menarik SEMUA data dari cloud.
     * 
     * Gunakan ini ketika:
     * - User login di device baru
     * - Device tidak memiliki data lokal untuk user ini
     */
    suspend fun performInitialDownload(): Result<Unit> {
        val userId = currentUserId
        
        _syncState.value = SyncState.Syncing
        
        return try {
            Log.d(TAG, "Starting initial download for user: $userId")
            
            // 1. Clear ANY existing local data (untuk isolasi akun)
            clearAllLocalData()
            
            // 2. Pull ALL data from cloud
            pullRemoteChanges(userId)
            
            _syncState.value = SyncState.Success("Sinkronisasi awal berhasil")
            Log.d(TAG, "Initial download completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Initial download failed: ${e.message}", e)
            _syncState.value = SyncState.Error(e.message ?: "Sinkronisasi awal gagal")
            Result.failure(e)
        }
    }
    
    /**
     * Clear semua data lokal.
     * Digunakan sebelum initial download untuk fresh device scenario.
     */
    private suspend fun clearAllLocalData() {
        Log.d(TAG, "Clearing all local data...")
        bankDao.deleteAll()
        transaksiDao.deleteAll()
        // NOTE: Chat AI tidak dihapus karena bersifat lokal per device
        Log.d(TAG, "All local data cleared")
    }
    
    /**
     * Melakukan full sync: push local changes lalu pull remote changes.
     * Gunakan ini untuk sinkronisasi reguler (bukan fresh device).
     */
    suspend fun fullSync(): Result<Unit> {
        val userId = currentUserId
        
        _syncState.value = SyncState.Syncing
        
        return try {
            // 1. Push pending local changes to remote
            pushLocalChanges(userId)
            
            // 2. Pull remote changes to local
            pullRemoteChanges(userId)
            
            _syncState.value = SyncState.Success("Sinkronisasi berhasil")
            Log.d(TAG, "Full sync completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            _syncState.value = SyncState.Error(e.message ?: "Sinkronisasi gagal")
            Result.failure(e)
        }
    }
    
    /**
     * Push local pending changes ke Supabase.
     */
    private suspend fun pushLocalChanges(userId: String) {
        // Push pending banks
        val pendingBanks = bankDao.getBanksBySyncStatus(SyncStatus.PENDING)
        pendingBanks.forEach { bank ->
            remoteDataSource.upsertBank(bank).onSuccess {
                bankDao.updateSyncStatus(bank.id, SyncStatus.SYNCED)
            }
        }
        
        // Push pending transaksi
        val pendingTransaksi = transaksiDao.getTransaksiBySyncStatus(SyncStatus.PENDING)
        pendingTransaksi.forEach { transaksi ->
            remoteDataSource.upsertTransaksi(transaksi).onSuccess {
                transaksiDao.updateSyncStatus(transaksi.id, SyncStatus.SYNCED)
            }
        }
        
        Log.d(TAG, "Pushed ${pendingBanks.size} banks, ${pendingTransaksi.size} transaksi")
    }
    
    /**
     * Pull remote changes ke local database.
     * Menggunakan timestamp-based conflict resolution (latest wins).
     */
    private suspend fun pullRemoteChanges(userId: String) {
        Log.d(TAG, "========================================")
        Log.d(TAG, "=== PULL REMOTE CHANGES ===")
        Log.d(TAG, "========================================")
        Log.d(TAG, "User ID: $userId")
        
        // Pull banks
        val remoteBanks = remoteDataSource.fetchBanks(userId)
        Log.d(TAG, "Fetched ${remoteBanks.size} banks from Supabase for user: $userId")
        
        if (remoteBanks.isEmpty()) {
            Log.w(TAG, "WARNING: No banks found in Supabase for user $userId")
            Log.w(TAG, "This could mean: 1) No data exists 2) RLS blocking 3) Wrong user_id")
        }
        
        var insertedBanks = 0
        remoteBanks.forEach { dto ->
            Log.d(TAG, "Processing bank: ${dto.nama} (id: ${dto.id})")
            val local = bankDao.getBankByIdOnce(dto.id)
            if (local == null || dto.updatedAt > local.updatedAt) {
                // Remote is newer or doesn't exist locally
                bankDao.insert(dto.toEntity().copy(syncStatus = SyncStatus.SYNCED))
                Log.d(TAG, "Inserted/Updated bank: ${dto.nama} (id: ${dto.id})")
                insertedBanks++
            } else {
                Log.d(TAG, "Skipped bank (local is newer): ${dto.nama}")
            }
        }
        
        // Pull transaksi
        val remoteTransaksi = remoteDataSource.fetchTransaksi(userId)
        Log.d(TAG, "Fetched ${remoteTransaksi.size} transaksi from Supabase")
        
        var insertedTransaksi = 0
        remoteTransaksi.forEach { dto ->
            val local = transaksiDao.getTransaksiById(dto.id)
            if (local == null || dto.updatedAt > local.updatedAt) {
                transaksiDao.insert(dto.toEntity().copy(syncStatus = SyncStatus.SYNCED))
                insertedTransaksi++
            }
        }
        
        Log.d(TAG, "========================================")
        Log.d(TAG, "SYNC COMPLETE: Inserted $insertedBanks banks, $insertedTransaksi transaksi")
        Log.d(TAG, "========================================")
    }

    
    /**
     * Sync hanya satu entity (untuk immediate sync setelah create/update).
     */
    suspend fun syncBank(bank: Bank): Result<Unit> {
        return try {
            remoteDataSource.upsertBank(bank).onSuccess {
                bankDao.updateSyncStatus(bank.id, SyncStatus.SYNCED)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncTransaksi(transaksi: Transaksi): Result<Unit> {
        return try {
            remoteDataSource.upsertTransaksi(transaksi).onSuccess {
                transaksiDao.updateSyncStatus(transaksi.id, SyncStatus.SYNCED)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Reset sync state ke idle.
     */
    fun resetSyncState() {
        _syncState.value = SyncState.Idle
    }
}
