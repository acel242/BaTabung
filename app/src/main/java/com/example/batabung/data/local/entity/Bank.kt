package com.example.batabung.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity untuk tabel bank.
 * Menyimpan informasi bank atau e-wallet yang dimiliki user.
 */
@Entity(
    tableName = "bank",
    indices = [Index("userId")]
)
data class Bank(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Supabase user UUID
    val userId: String,
    
    // Nama bank atau e-wallet (BCA, Mandiri, DANA, GoPay, dll)
    val nama: String,
    
    // Alias custom dari user (contoh: "Rekening Gaji")
    val alias: String = "",
    
    // Jenis: BANK atau EWALLET
    val jenis: JenisBank,
    
    // Status aktif untuk filter notifikasi
    val isAktif: Boolean = true,
    
    // Package name aplikasi untuk parser notifikasi (opsional)
    val packageName: String? = null,
    
    // Timestamp pembuatan
    val createdAt: Long = System.currentTimeMillis(),
    
    // Timestamp update terakhir (untuk conflict resolution)
    val updatedAt: Long = System.currentTimeMillis(),
    
    // Status sinkronisasi dengan cloud
    val syncStatus: SyncStatus = SyncStatus.PENDING
) {
    /**
     * Mendapatkan nama display (alias jika ada, atau nama bank)
     */
    val displayName: String
        get() = if (alias.isNotBlank()) "$nama - $alias" else nama
}
