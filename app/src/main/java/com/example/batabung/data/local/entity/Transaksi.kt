package com.example.batabung.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk tabel transaksi.
 * Mencatat setiap pemasukan atau pengeluaran dalam bank/tabungan.
 * 
 * Struktur baru: Transaksi langsung terhubung ke Bank (1 Bank = 1 Tabungan)
 */
@Entity(
    tableName = "transaksi",
    foreignKeys = [
        ForeignKey(
            entity = Bank::class,
            parentColumns = ["id"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("bankId"),
        Index("userId")
    ]
)
data class Transaksi(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Supabase user UUID
    val userId: String,
    
    // Foreign key ke bank (required, langsung ke bank/tabungan)
    val bankId: String,
    
    // Timestamp transaksi
    val tanggal: Long = System.currentTimeMillis(),
    
    // Jenis transaksi: MASUK atau KELUAR
    val jenis: JenisTransaksi,
    
    // Nominal dalam Rupiah (harus > 0)
    val jumlah: Long,
    
    // Kategori transaksi (contoh: "Gaji", "Makan", "Transport")
    val kategori: String = "",
    
    // Catatan tambahan (opsional)
    val catatan: String? = null,
    
    // Timestamp pembuatan
    val createdAt: Long = System.currentTimeMillis(),
    
    // Timestamp update terakhir (untuk conflict resolution)
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Status sinkronisasi dengan cloud
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
