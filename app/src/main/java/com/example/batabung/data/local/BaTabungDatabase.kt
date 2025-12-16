package com.example.batabung.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.batabung.data.local.dao.TabunganDao
import com.example.batabung.data.local.dao.TransaksiDao
import com.example.batabung.data.local.entity.Tabungan
import com.example.batabung.data.local.entity.Transaksi

/**
 * Room Database untuk aplikasi BaTabung.
 */
@Database(
    entities = [Tabungan::class, Transaksi::class],
    version = 1,
    exportSchema = false
)
abstract class BaTabungDatabase : RoomDatabase() {
    
    abstract fun tabunganDao(): TabunganDao
    
    abstract fun transaksiDao(): TransaksiDao
    
    companion object {
        const val DATABASE_NAME = "batabung_db"
    }
}
