package com.example.batabung.ui.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.JenisBank
import com.example.batabung.data.repository.BankOption
import com.example.batabung.data.repository.BankRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State untuk form tambah bank.
 */
data class AddBankUiState(
    val selectedJenis: JenisBank = JenisBank.BANK,
    val selectedBankOption: BankOption? = null,
    val alias: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

/**
 * Filter type untuk unified bank list.
 */
enum class FilterType {
    ALL,
    BANK,
    EWALLET
}

/**
 * ViewModel untuk mengelola bank.
 */
@HiltViewModel
class BankViewModel @Inject constructor(
    private val bankRepository: BankRepository
) : ViewModel() {
    
    /**
     * Daftar bank milik user.
     */
    val banks: StateFlow<List<Bank>> = bankRepository.getBanks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _addBankState = MutableStateFlow(AddBankUiState())
    val addBankState: StateFlow<AddBankUiState> = _addBankState.asStateFlow()
    
    /**
     * Opsi bank berdasarkan jenis yang dipilih.
     */
    val availableOptions: List<BankOption>
        get() = when (_addBankState.value.selectedJenis) {
            JenisBank.BANK -> BankRepository.AVAILABLE_BANKS
            JenisBank.EWALLET -> BankRepository.AVAILABLE_EWALLETS
        }
    
    private val _banksFiltered = MutableStateFlow<List<Bank>>(emptyList())
    val banksFiltered: StateFlow<List<Bank>> = _banksFiltered.asStateFlow()
    
    // Filter state for unified bank list
    private val _currentFilter = MutableStateFlow(FilterType.ALL)
    val currentFilter: StateFlow<FilterType> = _currentFilter.asStateFlow()
    
    init {
        // Load all banks on init and apply filter
        loadAllBanks()
    }
    
    /**
     * Load all banks and apply current filter.
     */
    fun loadAllBanks() {
        viewModelScope.launch {
            bankRepository.getBanks().collect { allBanks ->
                applyFilter(allBanks, _currentFilter.value)
            }
        }
    }
    
    /**
     * Set filter and apply to banks list.
     */
    fun setFilter(filter: FilterType) {
        _currentFilter.value = filter
        viewModelScope.launch {
            bankRepository.getBanks().collect { allBanks ->
                applyFilter(allBanks, filter)
            }
        }
    }
    
    private fun applyFilter(banks: List<Bank>, filter: FilterType) {
        _banksFiltered.value = when (filter) {
            FilterType.ALL -> banks
            FilterType.BANK -> banks.filter { it.jenis == JenisBank.BANK }
            FilterType.EWALLET -> banks.filter { it.jenis == JenisBank.EWALLET }
        }
    }
    
    /**
     * Load banks filtered by jenis (BANK atau EWALLET).
     */
    fun loadBanksByJenis(jenis: String) {
        viewModelScope.launch {
            bankRepository.getBanksByJenis(jenis).collect { bankList ->
                _banksFiltered.value = bankList
            }
        }
    }
    
    fun setSelectedJenis(jenis: JenisBank) {
        _addBankState.value = _addBankState.value.copy(
            selectedJenis = jenis,
            selectedBankOption = null,
            errorMessage = null
        )
    }
    
    fun setSelectedBankOption(option: BankOption) {
        _addBankState.value = _addBankState.value.copy(
            selectedBankOption = option,
            errorMessage = null
        )
    }
    
    fun setAlias(alias: String) {
        _addBankState.value = _addBankState.value.copy(
            alias = alias,
            errorMessage = null
        )
    }
    
    fun addBank() {
        val state = _addBankState.value
        val bankOption = state.selectedBankOption
        
        if (bankOption == null) {
            _addBankState.value = state.copy(errorMessage = "Pilih bank atau e-wallet")
            return
        }
        
        viewModelScope.launch {
            _addBankState.value = _addBankState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                bankRepository.addBank(
                    nama = bankOption.nama,
                    alias = state.alias,
                    jenis = state.selectedJenis,
                    packageName = bankOption.packageName
                )
                _addBankState.value = _addBankState.value.copy(
                    isLoading = false,
                    isSuccess = true
                )
            } catch (e: Exception) {
                _addBankState.value = _addBankState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Gagal menambahkan bank"
                )
            }
        }
    }
    
    fun toggleBankActive(bank: Bank) {
        viewModelScope.launch {
            try {
                bankRepository.toggleBankActive(bank)
            } catch (e: Exception) {
                // Handle error silently or show snackbar
            }
        }
    }
    
    fun deleteBank(bank: Bank) {
        viewModelScope.launch {
            try {
                bankRepository.deleteBank(bank)
            } catch (e: Exception) {
                // Handle error silently or show snackbar
            }
        }
    }
    
    fun resetAddBankState() {
        _addBankState.value = AddBankUiState()
    }
}
