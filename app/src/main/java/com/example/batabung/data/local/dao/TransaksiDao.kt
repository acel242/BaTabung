package com.example.batabung.data.local.dao

import androidx.room.*
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.SyncStatus
import com.example.batabung.data.local.entity.Transaksi
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database pada tabel Transaksi.
 * Struktur baru: Transaksi langsung terhubung ke Bank (1 Bank = 1 Tabungan)
 */
@Dao
interface TransaksiDao {
    
    // === QUERY ===
    
    @Query("SELECT * FROM transaksi WHERE userId = :userId ORDER BY tanggal DESC")
    fun getTransaksiByUser(userId: String): Flow<List<Transaksi>>
    
    @Query("SELECT * FROM transaksi WHERE bankId = :bankId ORDER BY tanggal DESC")
    fun getTransaksiByBank(bankId: String): Flow<List<Transaksi>>
    
    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getAllTransaksi(): Flow<List<Transaksi>>
    
    @Query("SELECT * FROM transaksi WHERE id = :id")
    suspend fun getTransaksiById(id: String): Transaksi?
    
    @Query("SELECT * FROM transaksi WHERE bankId = :bankId ORDER BY tanggal DESC LIMIT :limit")
    fun getRecentTransaksiByBank(bankId: String, limit: Int = 10): Flow<List<Transaksi>>
    
    // === SALDO ===
    
    // Hitung total berdasarkan jenis (MASUK atau KELUAR) untuk bank tertentu
    @Query("""
        SELECT COALESCE(SUM(jumlah), 0) FROM transaksi 
        WHERE bankId = :bankId AND jenis = :jenis
    """)
    suspend fun getTotalByJenis(bankId: String, jenis: JenisTransaksi): Long
    
    // Hitung saldo per bank (total MASUK - total KELUAR)
    @Query("""
        SELECT COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE bankId = :bankId AND jenis = 'MASUK'), 0
        ) - COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE bankId = :bankId AND jenis = 'KELUAR'), 0
        )
    """)
    fun getSaldo(bankId: String): Flow<Long>
    
    @Query("""
        SELECT COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE bankId = :bankId AND jenis = 'MASUK'), 0
        ) - COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE bankId = :bankId AND jenis = 'KELUAR'), 0
        )
    """)
    suspend fun getSaldoOnce(bankId: String): Long
    
    // Total saldo user (semua bank)
    @Query("""
        SELECT COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE userId = :userId AND jenis = 'MASUK'), 0
        ) - COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE userId = :userId AND jenis = 'KELUAR'), 0
        )
    """)
    suspend fun getTotalSaldoByUser(userId: String): Long
    
    // === RENTANG WAKTU ===
    
    @Query("""
        SELECT * FROM transaksi 
        WHERE bankId = :bankId 
        AND tanggal >= :startTime AND tanggal <= :endTime
        ORDER BY tanggal DESC
    """)
    fun getTransaksiInRange(
        bankId: String, 
        startTime: Long, 
        endTime: Long
    ): Flow<List<Transaksi>>
    
    @Query("""
        SELECT COALESCE(SUM(jumlah), 0) FROM transaksi 
        WHERE bankId = :bankId 
        AND jenis = :jenis
        AND tanggal >= :startTime AND tanggal <= :endTime
    """)
    suspend fun getTotalByJenisInRange(
        bankId: String, 
        jenis: JenisTransaksi, 
        startTime: Long, 
        endTime: Long
    ): Long
    
    // === KATEGORI ===
    
    @Query("""
        SELECT kategori, SUM(jumlah) as total FROM transaksi 
        WHERE bankId = :bankId AND jenis = :jenis
        GROUP BY kategori
        ORDER BY total DESC
    """)
    suspend fun getTotalByKategori(
        bankId: String, 
        jenis: JenisTransaksi
    ): List<KategoriTotal>
    
    // === SYNC ===
    
    @Query("SELECT * FROM transaksi WHERE syncStatus = :status")
    suspend fun getTransaksiBySyncStatus(status: SyncStatus): List<Transaksi>
    
    @Query("UPDATE transaksi SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
    
    // === INSERT ===
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaksi: Transaksi)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transaksis: List<Transaksi>)
    
    // === UPDATE ===
    
    @Update
    suspend fun update(transaksi: Transaksi)
    
    // === DELETE ===
    
    @Delete
    suspend fun delete(transaksi: Transaksi)
    
    @Query("DELETE FROM transaksi WHERE id = :id")
    suspend fun deleteById(id: String)
    
    @Query("DELETE FROM transaksi WHERE userId = :userId")
    suspend fun deleteAllByUser(userId: String)
    
    @Query("DELETE FROM transaksi WHERE bankId = :bankId")
    suspend fun deleteAllByBank(bankId: String)
    
    @Query("DELETE FROM transaksi")
    suspend fun deleteAll()
}

/**
 * Data class untuk hasil query grup kategori.
 */
data class KategoriTotal(
    val kategori: String,
    val total: Long
)
