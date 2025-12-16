package com.example.batabung.data.local.dao

import androidx.room.*
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.Transaksi
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database pada tabel Transaksi.
 */
@Dao
interface TransaksiDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaksi: Transaksi): Long
    
    @Update
    suspend fun update(transaksi: Transaksi)
    
    @Delete
    suspend fun delete(transaksi: Transaksi)
    
    @Query("DELETE FROM transaksi WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT * FROM transaksi WHERE tabunganId = :tabunganId ORDER BY tanggal DESC")
    fun getTransaksiByTabungan(tabunganId: Long): Flow<List<Transaksi>>
    
    @Query("SELECT * FROM transaksi ORDER BY tanggal DESC")
    fun getAllTransaksi(): Flow<List<Transaksi>>
    
    @Query("SELECT * FROM transaksi WHERE id = :id")
    suspend fun getTransaksiById(id: Long): Transaksi?
    
    // Hitung total berdasarkan jenis (MASUK atau KELUAR)
    @Query("""
        SELECT COALESCE(SUM(jumlah), 0) FROM transaksi 
        WHERE tabunganId = :tabunganId AND jenis = :jenis
    """)
    suspend fun getTotalByJenis(tabunganId: Long, jenis: JenisTransaksi): Long
    
    // Hitung saldo (total MASUK - total KELUAR)
    @Query("""
        SELECT COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE tabunganId = :tabunganId AND jenis = 'MASUK'), 0
        ) - COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE tabunganId = :tabunganId AND jenis = 'KELUAR'), 0
        )
    """)
    fun getSaldo(tabunganId: Long): Flow<Long>
    
    // Hitung saldo sekali (non-flow)
    @Query("""
        SELECT COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE tabunganId = :tabunganId AND jenis = 'MASUK'), 0
        ) - COALESCE(
            (SELECT SUM(jumlah) FROM transaksi WHERE tabunganId = :tabunganId AND jenis = 'KELUAR'), 0
        )
    """)
    suspend fun getSaldoOnce(tabunganId: Long): Long
    
    // Transaksi dalam rentang waktu (untuk ringkasan bulanan)
    @Query("""
        SELECT * FROM transaksi 
        WHERE tabunganId = :tabunganId 
        AND tanggal >= :startTime AND tanggal <= :endTime
        ORDER BY tanggal DESC
    """)
    fun getTransaksiInRange(
        tabunganId: Long, 
        startTime: Long, 
        endTime: Long
    ): Flow<List<Transaksi>>
    
    // Total berdasarkan jenis dalam rentang waktu
    @Query("""
        SELECT COALESCE(SUM(jumlah), 0) FROM transaksi 
        WHERE tabunganId = :tabunganId 
        AND jenis = :jenis
        AND tanggal >= :startTime AND tanggal <= :endTime
    """)
    suspend fun getTotalByJenisInRange(
        tabunganId: Long, 
        jenis: JenisTransaksi, 
        startTime: Long, 
        endTime: Long
    ): Long
    
    // Grup transaksi berdasarkan kategori
    @Query("""
        SELECT kategori, SUM(jumlah) as total FROM transaksi 
        WHERE tabunganId = :tabunganId AND jenis = :jenis
        GROUP BY kategori
        ORDER BY total DESC
    """)
    suspend fun getTotalByKategori(
        tabunganId: Long, 
        jenis: JenisTransaksi
    ): List<KategoriTotal>
}

/**
 * Data class untuk hasil query grup kategori.
 */
data class KategoriTotal(
    val kategori: String,
    val total: Long
)
