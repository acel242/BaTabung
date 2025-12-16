package com.example.batabung.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "batabung_prefs")

/**
 * Manager untuk menyimpan dan mengambil API Key dari DataStore.
 * API Key disimpan secara persistent sehingga user tidak perlu memasukkan ulang.
 */
@Singleton
class ApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val API_KEY = stringPreferencesKey("gemini_api_key")
    }
    
    /**
     * Flow untuk mengamati perubahan API key.
     */
    val apiKeyFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[API_KEY]
    }
    
    /**
     * Simpan API key ke DataStore.
     */
    suspend fun saveApiKey(apiKey: String) {
        context.dataStore.edit { preferences ->
            preferences[API_KEY] = apiKey
        }
    }
    
    /**
     * Ambil API key secara langsung (blocking).
     */
    suspend fun getApiKey(): String? {
        return context.dataStore.data.first()[API_KEY]
    }
    
    /**
     * Hapus API key dari DataStore.
     */
    suspend fun clearApiKey() {
        context.dataStore.edit { preferences ->
            preferences.remove(API_KEY)
        }
    }
    
    /**
     * Cek apakah API key sudah disimpan.
     */
    suspend fun hasApiKey(): Boolean {
        return !getApiKey().isNullOrBlank()
    }
}
