package com.example.batabung.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batabung.data.local.entity.JenisTransaksi
import com.example.batabung.data.local.entity.Tabungan
import com.example.batabung.data.local.entity.Transaksi
import com.example.batabung.data.repository.TabunganRepository
import com.example.batabung.util.FormatUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class untuk UI state Dashboard.
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val tabungan: Tabungan? = null,
    val saldo: Long = 0,
    val pemasukanBulanIni: Long = 0,
    val pengeluaranBulanIni: Long = 0,
    val recentTransaksi: List<Transaksi> = emptyList(),
    val progressPercentage: Float = 0f,
    val hasTabungan: Boolean = false
)

/**
 * ViewModel untuk Dashboard Screen.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: TabunganRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private var currentTabunganId: Long? = null
    
    init {
        loadDashboard()
    }
    
    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // Cek apakah sudah ada tabungan
            repository.getAllTabungan().collect { tabunganList ->
                if (tabunganList.isEmpty()) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            hasTabungan = false,
                            tabungan = null
                        ) 
                    }
                } else {
                    // Ambil tabungan pertama (untuk sekarang, single tabungan)
                    val tabungan = tabunganList.first()
                    currentTabunganId = tabungan.id
                    loadTabunganData(tabungan)
                }
            }
        }
    }
    
    private fun loadTabunganData(tabungan: Tabungan) {
        viewModelScope.launch {
            val startOfMonth = FormatUtils.getStartOfMonth()
            val endOfMonth = FormatUtils.getEndOfMonth()
            
            // Observe saldo
            repository.getSaldo(tabungan.id).combine(
                repository.getTransaksiByTabungan(tabungan.id)
            ) { saldo, transaksiList ->
                val pemasukanBulanIni = repository.getTotalPemasukanBulanIni(
                    tabungan.id, startOfMonth, endOfMonth
                )
                val pengeluaranBulanIni = repository.getTotalPengeluaranBulanIni(
                    tabungan.id, startOfMonth, endOfMonth
                )
                
                // Hitung progress jika ada target
                val progress = if (tabungan.target != null && tabungan.target > 0) {
                    (saldo.toFloat() / tabungan.target.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
                }
                
                DashboardUiState(
                    isLoading = false,
                    tabungan = tabungan,
                    saldo = saldo,
                    pemasukanBulanIni = pemasukanBulanIni,
                    pengeluaranBulanIni = pengeluaranBulanIni,
                    recentTransaksi = transaksiList.take(5),
                    progressPercentage = progress,
                    hasTabungan = true
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    fun createDefaultTabungan(nama: String = "Tabungan Utama", target: Long? = null) {
        viewModelScope.launch {
            val tabungan = Tabungan(nama = nama, target = target)
            repository.insertTabungan(tabungan)
            loadDashboard()
        }
    }
    
    fun addTransaksi(jenis: JenisTransaksi, jumlah: Long, kategori: String, catatan: String? = null) {
        val tabunganId = currentTabunganId ?: return
        
        viewModelScope.launch {
            try {
                val transaksi = Transaksi(
                    tabunganId = tabunganId,
                    jenis = jenis,
                    jumlah = jumlah,
                    kategori = kategori,
                    catatan = catatan
                )
                repository.insertTransaksi(transaksi)
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
