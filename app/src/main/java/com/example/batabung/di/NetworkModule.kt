package com.example.batabung.di

import android.content.Context
import com.example.batabung.data.remote.SupabaseDataSource
import com.example.batabung.worker.SyncManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt Module untuk network dan sync dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideSupabaseDataSource(): SupabaseDataSource {
        return SupabaseDataSource()
    }
    
    @Provides
    @Singleton
    fun provideSyncManager(
        @ApplicationContext context: Context
    ): SyncManager {
        return SyncManager(context)
    }
}
