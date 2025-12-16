package com.example.batabung.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk tabel tabungan.
 * Setiap tabungan adalah wadah untuk mencatat transaksi keuangan.
 */
@Entity(tableName = "tabungan")
data class Tabungan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val nama: String,
    
    // Target tabungan dalam Rupiah (opsional)
    val target: Long? = null,
    
    // Timestamp kapan tabungan dibuat
    val createdAt: Long = System.currentTimeMillis()
)
