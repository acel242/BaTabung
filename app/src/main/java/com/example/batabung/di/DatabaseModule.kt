package com.example.batabung.di

import android.content.Context
import androidx.room.Room
import com.example.batabung.data.local.BaTabungDatabase
import com.example.batabung.data.local.dao.TabunganDao
import com.example.batabung.data.local.dao.TransaksiDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module untuk menyediakan Database dan DAOs.
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
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideTabunganDao(database: BaTabungDatabase): TabunganDao {
        return database.tabunganDao()
    }
    
    @Provides
    @Singleton
    fun provideTransaksiDao(database: BaTabungDatabase): TransaksiDao {
        return database.transaksiDao()
    }
}
