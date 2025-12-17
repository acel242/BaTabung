package com.example.batabung.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batabung.data.repository.AuthRepository
import com.example.batabung.data.repository.AuthState
import com.example.batabung.data.repository.SyncRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State untuk layar autentikasi.
 */
data class AuthUiState(
    val isLoading: Boolean = false,
    val isSignUp: Boolean = false, // true = mode register, false = mode login
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val syncComplete: Boolean = false,
    val logoutComplete: Boolean = false
)

/**
 * ViewModel untuk mengelola state autentikasi.
 * Handles login, register, dan sync setelah authentication.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    val authState: StateFlow<AuthState> = authRepository.authState
    
    init {
        viewModelScope.launch {
            authRepository.initialize()
        }
    }
    
    /**
     * Update email field.
     */
    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    
    /**
     * Update password field.
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    /**
     * Update confirm password field.
     */
    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword)
    }
    
    /**
     * Toggle antara mode login dan register.
     */
    fun toggleSignUpMode() {
        _uiState.value = _uiState.value.copy(
            isSignUp = !_uiState.value.isSignUp,
            errorMessage = null,
            confirmPassword = ""
        )
    }
    
    /**
     * Sign in dengan email dan password.
     * Setelah login berhasil, trigger sinkronisasi data.
     */
    fun signIn() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        
        // Validasi input
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email tidak boleh kosong")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password tidak boleh kosong")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            authRepository.signIn(email, password)
                .onSuccess { userId ->
                    // Login berhasil, trigger initial download
                    performInitialSync()
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }
    
    /**
     * Sign up dengan email dan password.
     * Setelah register berhasil, user langsung ter-login.
     */
    fun signUp() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        val confirmPassword = _uiState.value.confirmPassword
        
        // Validasi input
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Email tidak boleh kosong")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password tidak boleh kosong")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password minimal 6 karakter")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(errorMessage = "Password tidak cocok")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            authRepository.signUp(email, password)
                .onSuccess { userId ->
                    if (userId.isNotEmpty()) {
                        // User langsung ter-login setelah register
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            syncComplete = true
                        )
                    } else {
                        // Perlu konfirmasi email
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Silakan cek email untuk konfirmasi"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }
    
    /**
     * Sign out - logout user dan reset state.
     * Menggunakan callback untuk memastikan navigasi terjadi setelah logout selesai.
     */
    fun signOut(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            authRepository.signOut()
            _uiState.value = AuthUiState(logoutComplete = true) // Reset state dan tandai logout selesai
            onComplete?.invoke()
        }
    }
    
    /**
     * Reset logout complete flag.
     */
    fun resetLogoutComplete() {
        _uiState.value = _uiState.value.copy(logoutComplete = false)
    }
    
    /**
     * Perform initial sync setelah login.
     * Download semua data dari Supabase.
     */
    private suspend fun performInitialSync() {
        syncRepository.performInitialDownload()
            .onSuccess {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    syncComplete = true
                )
            }
            .onFailure { error ->
                // Sync gagal tapi tetap lanjut ke home dengan warning
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    syncComplete = true,
                    errorMessage = "Data sinkronisasi gagal: ${error.message}"
                )
            }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
        authRepository.clearError()
    }
    
    /**
     * Reset sync complete flag (untuk navigasi handling).
     */
    fun resetSyncComplete() {
        _uiState.value = _uiState.value.copy(syncComplete = false)
    }
}
