package com.example.batabung.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.batabung.data.local.dao.BankDao
import com.example.batabung.data.local.dao.ChatMessageDao
import com.example.batabung.data.local.dao.TransaksiDao
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.ChatMessageEntity
import com.example.batabung.data.local.entity.Transaksi

/**
 * Room Database untuk aplikasi BaTabung.
 * Version 4: Added ChatMessageEntity for Full Account Sync
 */
@Database(
    entities = [Bank::class, Transaksi::class, ChatMessageEntity::class],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BaTabungDatabase : RoomDatabase() {
    
    abstract fun bankDao(): BankDao
    
    abstract fun transaksiDao(): TransaksiDao
    
    abstract fun chatMessageDao(): ChatMessageDao
    
    companion object {
        const val DATABASE_NAME = "batabung_db"
        
        /**
         * Migration dari version 1 ke 2.
         * - Membuat tabel bank baru
         * - Menambahkan kolom userId, bankId, syncStatus ke tabungan dan transaksi
         * - Mengubah id dari Long ke String (UUID)
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Karena perubahan yang signifikan (id type change), 
                // kita perlu membuat ulang tabel dengan data migration
                
                // 1. Buat tabel bank baru
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS bank (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        nama TEXT NOT NULL,
                        alias TEXT NOT NULL DEFAULT '',
                        jenis TEXT NOT NULL,
                        isAktif INTEGER NOT NULL DEFAULT 1,
                        packageName TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDING'
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_bank_userId ON bank(userId)")
                
                // 2. Buat tabel tabungan_new dengan schema baru
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS tabungan_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        bankId TEXT,
                        nama TEXT NOT NULL,
                        target INTEGER,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                        FOREIGN KEY (bankId) REFERENCES bank(id) ON DELETE SET NULL
                    )
                """)
                
                // 3. Migrate data dari tabungan lama (dengan default userId untuk guest)
                db.execSQL("""
                    INSERT INTO tabungan_new (id, userId, bankId, nama, target, createdAt, updatedAt, syncStatus)
                    SELECT 
                        printf('%08x-%04x-%04x-%04x-%012x', 
                            abs(random()) % 4294967296, 
                            abs(random()) % 65536, 
                            abs(random()) % 65536, 
                            abs(random()) % 65536, 
                            abs(random()) % 281474976710656),
                        'guest',
                        NULL,
                        nama,
                        target,
                        createdAt,
                        createdAt,
                        'PENDING'
                    FROM tabungan
                """)
                
                // 4. Drop tabel lama dan rename
                db.execSQL("DROP TABLE tabungan")
                db.execSQL("ALTER TABLE tabungan_new RENAME TO tabungan")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tabungan_bankId ON tabungan(bankId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_tabungan_userId ON tabungan(userId)")
                
                // 5. Buat tabel transaksi_new dengan schema baru
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transaksi_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        tabunganId TEXT NOT NULL,
                        bankId TEXT,
                        tanggal INTEGER NOT NULL,
                        jenis TEXT NOT NULL,
                        jumlah INTEGER NOT NULL,
                        kategori TEXT NOT NULL DEFAULT '',
                        catatan TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                        FOREIGN KEY (tabunganId) REFERENCES tabungan(id) ON DELETE CASCADE,
                        FOREIGN KEY (bankId) REFERENCES bank(id) ON DELETE SET NULL
                    )
                """)
                
                // 6. Migrate data transaksi (perlu mapping tabunganId lama ke baru)
                // Note: Ini simplified - dalam production, perlu mapping yang proper
                db.execSQL("""
                    INSERT INTO transaksi_new (id, userId, tabunganId, bankId, tanggal, jenis, jumlah, kategori, catatan, createdAt, updatedAt, syncStatus)
                    SELECT 
                        printf('%08x-%04x-%04x-%04x-%012x', 
                            abs(random()) % 4294967296, 
                            abs(random()) % 65536, 
                            abs(random()) % 65536, 
                            abs(random()) % 65536, 
                            abs(random()) % 281474976710656),
                        'guest',
                        (SELECT t2.id FROM tabungan t2 LIMIT 1),
                        NULL,
                        tanggal,
                        jenis,
                        jumlah,
                        kategori,
                        catatan,
                        tanggal,
                        tanggal,
                        'PENDING'
                    FROM transaksi
                """)
                
                // 7. Drop tabel lama dan rename
                db.execSQL("DROP TABLE transaksi")
                db.execSQL("ALTER TABLE transaksi_new RENAME TO transaksi")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_tabunganId ON transaksi(tabunganId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_bankId ON transaksi(bankId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_userId ON transaksi(userId)")
            }
        }
        
        /**
         * Migration dari version 2 ke 3.
         * - Menghapus tabel tabungan (1 Bank = 1 Tabungan konsep)
         * - Update transaksi: hapus tabunganId, set bankId required
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Buat tabel transaksi baru tanpa tabunganId
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS transaksi_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        bankId TEXT NOT NULL,
                        tanggal INTEGER NOT NULL,
                        jenis TEXT NOT NULL,
                        jumlah INTEGER NOT NULL,
                        kategori TEXT NOT NULL DEFAULT '',
                        catatan TEXT,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDING',
                        FOREIGN KEY (bankId) REFERENCES bank(id) ON DELETE CASCADE
                    )
                """)
                
                // 2. Migrate transaksi yang punya bankId valid
                db.execSQL("""
                    INSERT INTO transaksi_new (id, userId, bankId, tanggal, jenis, jumlah, kategori, catatan, createdAt, updatedAt, syncStatus)
                    SELECT id, userId, bankId, tanggal, jenis, jumlah, kategori, catatan, createdAt, updatedAt, syncStatus
                    FROM transaksi
                    WHERE bankId IS NOT NULL
                """)
                
                // 3. Drop tabel lama dan rename
                db.execSQL("DROP TABLE transaksi")
                db.execSQL("ALTER TABLE transaksi_new RENAME TO transaksi")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_bankId ON transaksi(bankId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_transaksi_userId ON transaksi(userId)")
                
                // 4. Hapus tabel tabungan
                db.execSQL("DROP TABLE IF EXISTS tabungan")
            }
        }
        
        /**
         * Migration dari version 3 ke 4.
         * - Menambahkan tabel chat_messages untuk Full Account Sync
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create chat_messages table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS chat_messages (
                        id TEXT NOT NULL PRIMARY KEY,
                        userId TEXT NOT NULL,
                        role TEXT NOT NULL,
                        content TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL,
                        syncStatus TEXT NOT NULL DEFAULT 'PENDING'
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_userId ON chat_messages(userId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_chat_messages_timestamp ON chat_messages(timestamp)")
            }
        }
    }
}
