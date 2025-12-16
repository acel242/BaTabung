package com.example.batabung.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controller untuk berinteraksi dengan Gemini AI.
 * Menangani pembuatan model dan pengiriman pesan.
 */
@Singleton
class AIController @Inject constructor() {
    
    private var generativeModel: GenerativeModel? = null
    
    /**
     * Inisialisasi Gemini model dengan API key.
     * API key harus disimpan dengan aman (BuildConfig atau encrypted storage).
     */
    fun initialize(apiKey: String) {
        generativeModel = GenerativeModel(
            // Menggunakan gemini-2.0-flash - model yang tersedia dan sudah ditest
            modelName = "gemini-2.0-flash",
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
                topK = 40
                topP = 0.95f
                maxOutputTokens = 1024
            }
        )
    }
    
    /**
     * Kirim pesan ke AI dan dapatkan response.
     * @param prompt Prompt yang sudah termasuk konteks keuangan
     * @return Response dari AI, atau error message jika gagal
     */
    suspend fun sendMessage(prompt: String): Result<String> {
        val model = generativeModel ?: return Result.failure(
            IllegalStateException("AI belum diinisialisasi. Silakan set API key terlebih dahulu.")
        )
        
        return try {
            val response = model.generateContent(prompt)
            val text = response.text ?: "Maaf, saya tidak bisa menjawab saat ini."
            Result.success(text)
        } catch (e: Exception) {
            // Handle error dengan lebih baik
            val errorMessage = when {
                e.message?.contains("404") == true -> "Model tidak ditemukan. Silakan periksa konfigurasi."
                e.message?.contains("401") == true || e.message?.contains("403") == true -> 
                    "API key tidak valid. Silakan periksa API key Anda."
                e.message?.contains("429") == true -> "Terlalu banyak request. Coba lagi nanti."
                else -> e.localizedMessage ?: "Terjadi kesalahan saat menghubungi AI."
            }
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Cek apakah AI sudah diinisialisasi.
     */
    fun isInitialized(): Boolean = generativeModel != null
}
