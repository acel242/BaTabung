package com.example.batabung.data.repository

import android.util.Log
import com.example.batabung.data.local.dao.BankDao
import com.example.batabung.data.local.dao.KategoriTotal
import com.example.batabung.data.local.dao.TransaksiDao
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.JenisBank
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.SyncStatus
import com.example.batabung.data.local.entity.Transaksi
import com.example.batabung.data.remote.SupabaseDataSource
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk mengelola data bank/e-wallet dan transaksi.
 * Struktur baru: 1 Bank = 1 Tabungan (bank adalah tabungan itu sendiri)
 * 
 * Data disimpan ke local Room DB DAN di-sync ke Supabase cloud.
 */
@Singleton
class BankRepository @Inject constructor(
    private val bankDao: BankDao,
    private val transaksiDao: TransaksiDao,
    private val authRepository: AuthRepository,
    private val remoteDataSource: SupabaseDataSource
) {
    
    /**
     * Mendapatkan user ID saat ini dari AuthRepository.
     * Throws exception jika user belum terautentikasi.
     */
    private val currentUserId: String
        get() = authRepository.currentUserId
            ?: throw IllegalStateException("User not authenticated")
    
    // === BANK / TABUNGAN ===
    
    /**
     * Mendapatkan semua bank milik user yang sedang login.
     */
    fun getBanks(): Flow<List<Bank>> {
        return bankDao.getBanksByUser(currentUserId)
    }
    
    /**
     * Mendapatkan bank yang aktif saja.
     */
    fun getActiveBanks(): Flow<List<Bank>> {
        return bankDao.getActiveBanksByUser(currentUserId)
    }
    
    /**
     * Mendapatkan bank berdasarkan jenis (BANK atau EWALLET).
     */
    fun getBanksByJenis(jenis: String): Flow<List<Bank>> {
        return bankDao.getBanksByJenis(currentUserId, jenis)
    }
    
    /**
     * Mendapatkan bank berdasarkan ID.
     */
    fun getBankById(id: String): Flow<Bank?> {
        return bankDao.getBankById(id)
    }
    
    /**
     * Mendapatkan bank berdasarkan ID (once).
     */
    suspend fun getBankByIdOnce(id: String): Bank? {
        return bankDao.getBankByIdOnce(id)
    }
    
    /**
     * Mendapatkan bank berdasarkan package name notifikasi.
     */
    suspend fun getBankByPackageName(packageName: String): Bank? {
        return bankDao.getBankByPackageName(currentUserId, packageName)
    }
    
    /**
     * Menambahkan bank baru.
     * Data disimpan ke local Room DB dan langsung sync ke Supabase.
     */
    suspend fun addBank(
        nama: String,
        alias: String = "",
        jenis: JenisBank,
        packageName: String? = null
    ) {
        val bank = Bank(
            userId = currentUserId,
            nama = nama,
            alias = alias,
            jenis = jenis,
            packageName = packageName,
            syncStatus = SyncStatus.PENDING
        )
        
        // 1. Insert ke local Room DB
        bankDao.insert(bank)
        Log.d(TAG, "Inserted bank to local DB: ${bank.nama} (id: ${bank.id})")
        
        // 2. Sync ke Supabase cloud (NonCancellable - tidak dibatalkan saat navigasi)
        withContext(NonCancellable) {
            remoteDataSource.upsertBank(bank).fold(
                onSuccess = {
                    bankDao.updateSyncStatus(bank.id, SyncStatus.SYNCED)
                    Log.d(TAG, "Bank synced to Supabase: ${bank.nama}")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync bank to Supabase: ${error.message}", error)
                    // Data tetap tersimpan lokal dengan status PENDING
                    // Akan di-retry pada periodic sync
                }
            )
        }
    }
    
    /**
     * Update bank.
     * Data di-update di local Room DB dan langsung sync ke Supabase.
     */
    suspend fun updateBank(bank: Bank) {
        val updatedBank = bank.copy(
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        
        // 1. Update di local Room DB
        bankDao.update(updatedBank)
        
        // 2. Sync ke Supabase cloud
        remoteDataSource.upsertBank(updatedBank).fold(
            onSuccess = {
                bankDao.updateSyncStatus(bank.id, SyncStatus.SYNCED)
                Log.d(TAG, "Bank updated in Supabase: ${bank.nama}")
            },
            onFailure = { error ->
                Log.e(TAG, "Failed to update bank in Supabase: ${error.message}", error)
            }
        )
    }
    
    /**
     * Toggle status aktif bank.
     */
    suspend fun toggleBankActive(bank: Bank) {
        bankDao.updateActiveStatus(
            id = bank.id,
            isAktif = !bank.isAktif
        )
    }
    
    /**
     * Hapus bank.
     * Data dihapus dari local Room DB dan langsung sync ke Supabase.
     */
    suspend fun deleteBank(bank: Bank) {
        // 1. Delete dari local Room DB
        bankDao.delete(bank)
        Log.d(TAG, "Deleted bank from local DB: ${bank.nama} (id: ${bank.id})")
        
        // 2. Delete dari Supabase cloud (NonCancellable)
        withContext(NonCancellable) {
            // Hapus transaksi terkait dulu untuk menghindari foreign key violation
            remoteDataSource.deleteTransactionsByBank(bank.id).fold(
                onSuccess = {
                    Log.d(TAG, "Transactions for bank ${bank.nama} deleted from Supabase")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to delete transactions for bank ${bank.nama}: ${error.message}", error)
                }
            )

            remoteDataSource.deleteBank(bank.id).fold(
                onSuccess = {
                    Log.d(TAG, "Bank deleted from Supabase: ${bank.nama}")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to delete bank from Supabase: ${error.message}", error)
                    // Data sudah terhapus lokal, akan di-sync ulang jika ada masalah
                }
            )
        }
    }
    
    /**
     * Mendapatkan jumlah bank.
     */
    suspend fun getBankCount(): Int {
        return bankDao.getCount(currentUserId)
    }
    
    // === TRANSAKSI ===
    
    /**
     * Mendapatkan semua transaksi user.
     */
    fun getTransaksiByUser(): Flow<List<Transaksi>> {
        return transaksiDao.getTransaksiByUser(currentUserId)
    }
    
    /**
     * Mendapatkan transaksi berdasarkan bank.
     */
    fun getTransaksiByBank(bankId: String): Flow<List<Transaksi>> {
        return transaksiDao.getTransaksiByBank(bankId)
    }
    
    /**
     * Mendapatkan transaksi terbaru berdasarkan bank.
     */
    fun getRecentTransaksiByBank(bankId: String, limit: Int = 10): Flow<List<Transaksi>> {
        return transaksiDao.getRecentTransaksiByBank(bankId, limit)
    }
    
    /**
     * Mendapatkan semua transaksi.
     */
    fun getAllTransaksi(): Flow<List<Transaksi>> {
        return transaksiDao.getAllTransaksi()
    }
    
    /**
     * Menambahkan transaksi baru.
     * Data disimpan ke local Room DB dan langsung sync ke Supabase.
     */
    suspend fun insertTransaksi(
        bankId: String,
        jenis: JenisTransaksi,
        jumlah: Long,
        kategori: String = "",
        catatan: String? = null,
        tanggal: Long = System.currentTimeMillis()
    ) {
        require(jumlah > 0) { "Jumlah transaksi harus lebih dari 0" }
        
        val transaksi = Transaksi(
            userId = currentUserId,
            bankId = bankId,
            tanggal = tanggal,
            jenis = jenis,
            jumlah = jumlah,
            kategori = kategori,
            catatan = catatan,
            syncStatus = SyncStatus.PENDING
        )
        
        // 1. Insert ke local Room DB
        transaksiDao.insert(transaksi)
        Log.d(TAG, "Inserted transaksi to local DB: $jenis Rp$jumlah")
        
        // 2. Sync ke Supabase cloud (NonCancellable - tidak dibatalkan saat navigasi)
        withContext(NonCancellable) {
            remoteDataSource.upsertTransaksi(transaksi).fold(
                onSuccess = {
                    transaksiDao.updateSyncStatus(transaksi.id, SyncStatus.SYNCED)
                    Log.d(TAG, "Transaksi synced to Supabase")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to sync transaksi to Supabase: ${error.message}", error)
                }
            )
        }
    }
    
    /**
     * Hapus transaksi.
     * Data dihapus dari local Room DB dan langsung sync ke Supabase.
     */
    suspend fun deleteTransaksi(transaksi: Transaksi) {
        // 1. Delete dari local Room DB
        transaksiDao.delete(transaksi)
        Log.d(TAG, "Deleted transaksi from local DB: id=${transaksi.id}")
        
        // 2. Delete dari Supabase cloud (NonCancellable)
        withContext(NonCancellable) {
            remoteDataSource.deleteTransaksi(transaksi.id).fold(
                onSuccess = {
                    Log.d(TAG, "Transaksi deleted from Supabase")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to delete transaksi from Supabase: ${error.message}", error)
                    // Data sudah terhapus lokal
                }
            )
        }
    }
    
    /**
     * Hapus transaksi berdasarkan ID.
     * Data dihapus dari local Room DB dan langsung sync ke Supabase.
     */
    suspend fun deleteTransaksiById(id: String) {
        // 1. Delete dari local Room DB
        transaksiDao.deleteById(id)
        Log.d(TAG, "Deleted transaksi by ID from local DB: id=$id")
        
        // 2. Delete dari Supabase cloud (NonCancellable)
        withContext(NonCancellable) {
            remoteDataSource.deleteTransaksi(id).fold(
                onSuccess = {
                    Log.d(TAG, "Transaksi deleted from Supabase by ID")
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to delete transaksi from Supabase: ${error.message}", error)
                }
            )
        }
    }
    
    // === SALDO ===
    
    /**
     * Mendapatkan saldo bank (Flow).
     */
    fun getSaldo(bankId: String): Flow<Long> {
        return transaksiDao.getSaldo(bankId)
    }
    
    /**
     * Mendapatkan saldo bank (once).
     */
    suspend fun getSaldoOnce(bankId: String): Long {
        return transaksiDao.getSaldoOnce(bankId)
    }
    
    /**
     * Mendapatkan total saldo semua bank user.
     */
    suspend fun getTotalSaldo(): Long {
        return transaksiDao.getTotalSaldoByUser(currentUserId)
    }
    
    /**
     * Mendapatkan total pemasukan bank.
     */
    suspend fun getTotalPemasukan(bankId: String): Long {
        return transaksiDao.getTotalByJenis(bankId, JenisTransaksi.MASUK)
    }
    
    /**
     * Mendapatkan total pengeluaran bank.
     */
    suspend fun getTotalPengeluaran(bankId: String): Long {
        return transaksiDao.getTotalByJenis(bankId, JenisTransaksi.KELUAR)
    }
    
    // === RINGKASAN BULANAN ===
    
    /**
     * Mendapatkan transaksi dalam rentang waktu.
     */
    fun getTransaksiInRange(
        bankId: String,
        startTime: Long,
        endTime: Long
    ): Flow<List<Transaksi>> {
        return transaksiDao.getTransaksiInRange(bankId, startTime, endTime)
    }
    
    /**
     * Mendapatkan total pemasukan dalam rentang waktu.
     */
    suspend fun getTotalPemasukanInRange(
        bankId: String,
        startTime: Long,
        endTime: Long
    ): Long {
        return transaksiDao.getTotalByJenisInRange(bankId, JenisTransaksi.MASUK, startTime, endTime)
    }
    
    /**
     * Mendapatkan total pengeluaran dalam rentang waktu.
     */
    suspend fun getTotalPengeluaranInRange(
        bankId: String,
        startTime: Long,
        endTime: Long
    ): Long {
        return transaksiDao.getTotalByJenisInRange(bankId, JenisTransaksi.KELUAR, startTime, endTime)
    }
    
    // === KATEGORI ===
    
    /**
     * Mendapatkan total transaksi per kategori.
     */
    suspend fun getTotalByKategori(
        bankId: String,
        jenis: JenisTransaksi
    ): List<KategoriTotal> {
        return transaksiDao.getTotalByKategori(bankId, jenis)
    }
    
    companion object {
        private const val TAG = "BankRepository"
        
        /**
         * Daftar bank yang tersedia di Indonesia.
         */
        val AVAILABLE_BANKS = listOf(
            BankOption("BCA", "com.bca.android", JenisBank.BANK),
            BankOption("BRI", "com.bri.mobile", JenisBank.BANK),
            BankOption("BNI", "com.bni.android", JenisBank.BANK),
            BankOption("Mandiri", "com.android.mandiri", JenisBank.BANK),
            BankOption("CIMB Niaga", "com.cimbniaga.octo", JenisBank.BANK),
            BankOption("BTN", "com.btn.mobilebanking", JenisBank.BANK),
            BankOption("Bank Jago", "com.jago.app", JenisBank.BANK),
            BankOption("SeaBank", "com.seabank.app", JenisBank.BANK),
            BankOption("Bank Lainnya", null, JenisBank.BANK)
        )
        
        /**
         * Daftar e-wallet yang tersedia di Indonesia.
         */
        val AVAILABLE_EWALLETS = listOf(
            BankOption("DANA", "id.dana", JenisBank.EWALLET),
            BankOption("GoPay", "com.gojek.app", JenisBank.EWALLET),
            BankOption("OVO", "com.ovo.android", JenisBank.EWALLET),
            BankOption("ShopeePay", "com.shopee.id", JenisBank.EWALLET),
            BankOption("LinkAja", "com.telkom.mwallet", JenisBank.EWALLET),
            BankOption("E-Wallet Lainnya", null, JenisBank.EWALLET)
        )
    }
}

/**
 * Data class untuk opsi bank/e-wallet.
 */
data class BankOption(
    val nama: String,
    val packageName: String?,
    val jenis: JenisBank
)
