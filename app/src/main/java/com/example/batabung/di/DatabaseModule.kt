package com.example.batabung.di

import android.content.Context
import androidx.room.Room
import com.example.batabung.data.local.BaTabungDatabase
import com.example.batabung.data.local.dao.BankDao
import com.example.batabung.data.local.dao.ChatMessageDao
import com.example.batabung.data.local.dao.TransaksiDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module untuk menyediakan Database dan DAOs.
 * Full Account Sync dengan chat lokal
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BaTabungDatabase {
        return Room.databaseBuilder(
            context,
            BaTabungDatabase::class.java,
            BaTabungDatabase.DATABASE_NAME
        )
            .addMigrations(
                BaTabungDatabase.MIGRATION_1_2,
                BaTabungDatabase.MIGRATION_2_3,
                BaTabungDatabase.MIGRATION_3_4
            )
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBankDao(database: BaTabungDatabase): BankDao {
        return database.bankDao()
    }
    
    @Provides
    @Singleton
    fun provideTransaksiDao(database: BaTabungDatabase): TransaksiDao {
        return database.transaksiDao()
    }
    
    @Provides
    @Singleton
    fun provideChatMessageDao(database: BaTabungDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }
}
