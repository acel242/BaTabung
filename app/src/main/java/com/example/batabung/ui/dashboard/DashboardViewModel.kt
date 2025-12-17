package com.example.batabung.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.Transaksi
import com.example.batabung.data.repository.BankRepository
import com.example.batabung.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class untuk UI state Dashboard.
 * Struktur baru: 1 Bank = 1 Tabungan
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val bank: Bank? = null,
    val bankList: List<Bank> = emptyList(),
    val saldo: Long = 0,
    val pemasukanBulanIni: Long = 0,
    val pengeluaranBulanIni: Long = 0,
    val recentTransaksi: List<Transaksi> = emptyList(),
    val hasBank: Boolean = false
)

/**
 * ViewModel untuk Dashboard Screen.
 * Struktur baru: 1 Bank = 1 Tabungan (bank adalah tabungan itu sendiri)
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: BankRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private var currentBankId: String? = null
    
    init {
        loadDashboard()
    }
    
    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Cek apakah sudah ada bank
            repository.getBanks().collect { bankList ->
                if (bankList.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            hasBank = false,
                            bank = null
                        ) 
                    }
                } else {
                    // Ambil bank pertama (untuk sekarang)
                    val bank = bankList.first()
                    currentBankId = bank.id
                    loadBankData(bank)
                }
            }
        }
    }
    
    /**
     * Load bank filtered by jenis (BANK atau EWALLET).
     */
    fun loadByJenisBank(jenisBank: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            repository.getBanksByJenis(jenisBank).collect { bankList ->
                if (bankList.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            hasBank = false,
                            bank = null
                        ) 
                    }
                } else {
                    // Ambil bank pertama dari jenis yang dipilih
                    val bank = bankList.first()
                    currentBankId = bank.id
                    loadBankData(bank)
                }
            }
        }
    }
    
    /**
     * Load data untuk bank tertentu berdasarkan ID.
     */
    fun loadBankById(bankId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            currentBankId = bankId
            
            repository.getBankById(bankId).collect { bank ->
                if (bank == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            hasBank = false,
                            bank = null
                        ) 
                    }
                } else {
                    loadBankData(bank)
                }
            }
        }
    }
    
    private fun loadBankData(bank: Bank) {
        viewModelScope.launch {
            val startOfMonth = FormatUtils.getStartOfMonth()
            val endOfMonth = FormatUtils.getEndOfMonth()
            
            // Observe saldo dan transaksi
            repository.getSaldo(bank.id).combine(
                repository.getTransaksiByBank(bank.id)
            ) { saldo, transaksiList ->
                val pemasukanBulanIni = repository.getTotalPemasukanInRange(
                    bank.id, startOfMonth, endOfMonth
                )
                val pengeluaranBulanIni = repository.getTotalPengeluaranInRange(
                    bank.id, startOfMonth, endOfMonth
                )
                
                DashboardUiState(
                    isLoading = false,
                    bank = bank,
                    saldo = saldo,
                    pemasukanBulanIni = pemasukanBulanIni,
                    pengeluaranBulanIni = pengeluaranBulanIni,
                    recentTransaksi = transaksiList.take(5),
                    hasBank = true
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun addTransaksi(jenis: JenisTransaksi, jumlah: Long, kategori: String, catatan: String? = null) {
        val bankId = currentBankId ?: return
        
        viewModelScope.launch {
            try {
                repository.insertTransaksi(
                    bankId = bankId,
                    jenis = jenis,
                    jumlah = jumlah,
                    kategori = kategori,
                    catatan = catatan
                )
            } catch (e: IllegalArgumentException) {
                // Handle error: jumlah <= 0
            }
        }
    }
    
    fun deleteTransaksi(transaksi: Transaksi) {
        viewModelScope.launch {
            repository.deleteTransaksi(transaksi)
        }
    }
}
