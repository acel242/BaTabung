package com.example.batabung.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Enum untuk jenis pesan chat.
 */
enum class ChatRole {
    USER,
    AI,
    ERROR
}

/**
 * Entity untuk tabel chat_messages.
 * Menyimpan riwayat chat AI untuk sinkronisasi antar device.
 */
@Entity(
    tableName = "chat_messages",
    indices = [Index("userId"), Index("timestamp")]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Supabase user UUID
    val userId: String,
    
    // Jenis pesan: USER, AI, atau ERROR
    val role: ChatRole,
    
    // Isi pesan
    val content: String,
    
    // Timestamp pesan
    val timestamp: Long = System.currentTimeMillis(),
    
    // Timestamp dibuat (untuk sync)
    val createdAt: Long = System.currentTimeMillis(),
    
    // Status sinkronisasi dengan cloud
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
