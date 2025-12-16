package com.example.batabung.data.repository

import com.example.batabung.data.local.dao.KategoriTotal
import com.example.batabung.data.local.dao.TabunganDao
import com.example.batabung.data.local.dao.TransaksiDao
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.Tabungan
import com.example.batabung.data.local.entity.Transaksi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk abstraksi akses data tabungan dan transaksi.
 */
@Singleton
class TabunganRepository @Inject constructor(
    private val tabunganDao: TabunganDao,
    private val transaksiDao: TransaksiDao
) {
    // === TABUNGAN ===
    
    fun getAllTabungan(): Flow<List<Tabungan>> = tabunganDao.getAllTabungan()
    
    fun getTabunganById(id: Long): Flow<Tabungan?> = tabunganDao.getTabunganById(id)
    
    suspend fun getTabunganByIdOnce(id: Long): Tabungan? = tabunganDao.getTabunganByIdOnce(id)
    
    suspend fun insertTabungan(tabungan: Tabungan): Long = tabunganDao.insert(tabungan)
    
    suspend fun updateTabungan(tabungan: Tabungan) = tabunganDao.update(tabungan)
    
    suspend fun deleteTabungan(tabungan: Tabungan) = tabunganDao.delete(tabungan)
    
    suspend fun getTabunganCount(): Int = tabunganDao.getCount()
    
    // === TRANSAKSI ===
    
    fun getTransaksiByTabungan(tabunganId: Long): Flow<List<Transaksi>> = 
        transaksiDao.getTransaksiByTabungan(tabunganId)
    
    fun getAllTransaksi(): Flow<List<Transaksi>> = transaksiDao.getAllTransaksi()
    
    suspend fun insertTransaksi(transaksi: Transaksi): Long {
        // Validasi: jumlah harus > 0
        require(transaksi.jumlah > 0) { "Jumlah transaksi harus lebih dari 0" }
        return transaksiDao.insert(transaksi)
    }
    
    suspend fun deleteTransaksi(transaksi: Transaksi) = transaksiDao.delete(transaksi)
    
    suspend fun deleteTransaksiById(id: Long) = transaksiDao.deleteById(id)
    
    // === SALDO ===
    
    fun getSaldo(tabunganId: Long): Flow<Long> = transaksiDao.getSaldo(tabunganId)
    
    suspend fun getSaldoOnce(tabunganId: Long): Long = transaksiDao.getSaldoOnce(tabunganId)
    
    suspend fun getTotalPemasukan(tabunganId: Long): Long = 
        transaksiDao.getTotalByJenis(tabunganId, JenisTransaksi.MASUK)
    
    suspend fun getTotalPengeluaran(tabunganId: Long): Long = 
        transaksiDao.getTotalByJenis(tabunganId, JenisTransaksi.KELUAR)
    
    // === RINGKASAN BULANAN ===
    
    fun getTransaksiInRange(
        tabunganId: Long, 
        startTime: Long, 
        endTime: Long
    ): Flow<List<Transaksi>> = transaksiDao.getTransaksiInRange(tabunganId, startTime, endTime)
    
    suspend fun getTotalPemasukanBulanIni(
        tabunganId: Long, 
        startOfMonth: Long, 
        endOfMonth: Long
    ): Long = transaksiDao.getTotalByJenisInRange(
        tabunganId, 
        JenisTransaksi.MASUK, 
        startOfMonth, 
        endOfMonth
    )
    
    suspend fun getTotalPengeluaranBulanIni(
        tabunganId: Long, 
        startOfMonth: Long, 
        endOfMonth: Long
    ): Long = transaksiDao.getTotalByJenisInRange(
        tabunganId, 
        JenisTransaksi.KELUAR, 
        startOfMonth, 
        endOfMonth
    )
    
    // === KATEGORI ===
    
    suspend fun getTotalByKategori(
        tabunganId: Long, 
        jenis: JenisTransaksi
    ): List<KategoriTotal> = transaksiDao.getTotalByKategori(tabunganId, jenis)
}
