package com.example.batabung.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity untuk tabel transaksi.
 * Mencatat setiap pemasukan atau pengeluaran dalam tabungan.
 */
@Entity(
    tableName = "transaksi",
    foreignKeys = [
        ForeignKey(
            entity = Tabungan::class,
            parentColumns = ["id"],
            childColumns = ["tabunganId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tabunganId")]
)
data class Transaksi(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val tabunganId: Long,
    
    // Timestamp transaksi
    val tanggal: Long = System.currentTimeMillis(),
    
    // Jenis transaksi: MASUK atau KELUAR
    val jenis: JenisTransaksi,
    
    // Nominal dalam Rupiah (harus > 0)
    val jumlah: Long,
    
    // Kategori transaksi (contoh: "Gaji", "Makan", "Transport")
    val kategori: String = "",
    
    // Catatan tambahan (opsional)
    val catatan: String? = null
)
