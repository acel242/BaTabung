package com.example.batabung.data.repository

import android.util.Log
import com.example.batabung.data.remote.SupabaseConfig
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sealed class untuk representasi status autentikasi.
 */
sealed class AuthState {
    data object Loading : AuthState()
    data object Unauthenticated : AuthState()
    data class Authenticated(val userId: String, val email: String?) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * Repository untuk mengelola autentikasi menggunakan Supabase Auth.
 * Menggunakan email/password authentication via Supabase GoTrue.
 */
@Singleton
class AuthRepository @Inject constructor() {
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    private val supabase = SupabaseConfig.client
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    /**
     * Mendapatkan user ID saat ini dari Supabase session.
     * Returns null jika belum terautentikasi.
     */
    val currentUserId: String?
        get() = supabase.auth.currentUserOrNull()?.id
    
    /**
     * Mendapatkan email user saat ini.
     */
    val currentUserEmail: String?
        get() = supabase.auth.currentUserOrNull()?.email
    
    /**
     * Cek apakah user sudah terautentikasi.
     */
    val isAuthenticated: Boolean
        get() = supabase.auth.currentUserOrNull() != null
    
    /**
     * Inisialisasi auth state - memeriksa session yang tersimpan.
     */
    suspend fun initialize() {
        try {
            _authState.value = AuthState.Loading
            Log.d(TAG, "Initializing auth state...")
            
            // Supabase SDK otomatis memuat session dari storage
            val currentUser = supabase.auth.currentUserOrNull()
            
            if (currentUser != null) {
                Log.d(TAG, "Session found for user: ${currentUser.id}")
                _authState.value = AuthState.Authenticated(
                    userId = currentUser.id,
                    email = currentUser.email
                )
            } else {
                Log.d(TAG, "No session found, user is unauthenticated")
                _authState.value = AuthState.Unauthenticated
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing auth: ${e.message}", e)
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Sign in dengan email dan password.
     */
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            _authState.value = AuthState.Loading
            Log.d(TAG, "Signing in user: $email")
            
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                Log.d(TAG, "Sign in successful for user: ${user.id}")
                _authState.value = AuthState.Authenticated(
                    userId = user.id,
                    email = user.email
                )
                Result.success(user.id)
            } else {
                throw Exception("Login gagal: tidak dapat mengambil data user")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed: ${e.message}", e)
            val errorMessage = parseAuthError(e)
            _authState.value = AuthState.Error(errorMessage)
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Sign up dengan email dan password.
     * Setelah registrasi berhasil, user otomatis ter-login.
     */
    suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            _authState.value = AuthState.Loading
            Log.d(TAG, "Signing up new user: $email")
            
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = supabase.auth.currentUserOrNull()
            if (user != null) {
                Log.d(TAG, "Sign up successful for user: ${user.id}")
                _authState.value = AuthState.Authenticated(
                    userId = user.id,
                    email = user.email
                )
                Result.success(user.id)
            } else {
                // Beberapa konfigurasi Supabase memerlukan email confirmation
                Log.d(TAG, "Sign up successful, awaiting email confirmation")
                _authState.value = AuthState.Unauthenticated
                Result.success("")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign up failed: ${e.message}", e)
            val errorMessage = parseAuthError(e)
            _authState.value = AuthState.Error(errorMessage)
            Result.failure(Exception(errorMessage))
        }
    }
    
    /**
     * Sign out - menghapus session.
     */
    suspend fun signOut(): Result<Unit> {
        return try {
            Log.d(TAG, "Signing out user...")
            supabase.auth.signOut()
            _authState.value = AuthState.Unauthenticated
            Log.d(TAG, "Sign out successful")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed: ${e.message}", e)
            // Tetap set ke unauthenticated meskipun ada error
            _authState.value = AuthState.Unauthenticated
            Result.failure(e)
        }
    }
    
    /**
     * Reset error state ke unauthenticated.
     */
    fun clearError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }
    
    /**
     * Parse error message dari Supabase untuk tampilan user-friendly.
     */
    private fun parseAuthError(e: Exception): String {
        val message = e.message ?: "Terjadi kesalahan"
        return when {
            message.contains("Invalid login credentials", ignoreCase = true) -> 
                "Email atau password salah"
            message.contains("User already registered", ignoreCase = true) -> 
                "Email sudah terdaftar"
            message.contains("Password should be at least", ignoreCase = true) -> 
                "Password minimal 6 karakter"
            message.contains("Unable to validate email", ignoreCase = true) -> 
                "Format email tidak valid"
            message.contains("network", ignoreCase = true) -> 
                "Tidak dapat terhubung ke server"
            else -> message
        }
    }
}
