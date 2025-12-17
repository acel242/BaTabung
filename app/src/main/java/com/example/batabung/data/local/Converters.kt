package com.example.batabung.data.local

import androidx.room.TypeConverter
import com.example.batabung.data.local.entity.ChatRole
import com.example.batabung.data.local.entity.JenisBank
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.SyncStatus

/**
 * Room Type Converters untuk enum types.
 */
class Converters {
    
    // === JenisTransaksi ===
    
    @TypeConverter
    fun fromJenisTransaksi(value: JenisTransaksi): String = value.name
    
    @TypeConverter
    fun toJenisTransaksi(value: String): JenisTransaksi = JenisTransaksi.valueOf(value)
    
    // === JenisBank ===
    
    @TypeConverter
    fun fromJenisBank(value: JenisBank): String = value.name
    
    @TypeConverter
    fun toJenisBank(value: String): JenisBank = JenisBank.valueOf(value)
    
    // === SyncStatus ===
    
    @TypeConverter
    fun fromSyncStatus(value: SyncStatus): String = value.name
    
    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
    
    // === ChatRole ===
    
    @TypeConverter
    fun fromChatRole(value: ChatRole): String = value.name
    
    @TypeConverter
    fun toChatRole(value: String): ChatRole = ChatRole.valueOf(value)
}
