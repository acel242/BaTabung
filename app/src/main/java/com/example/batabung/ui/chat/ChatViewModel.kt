package com.example.batabung.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.batabung.ai.AIController
import com.example.batabung.ai.PromptBuilder
import com.example.batabung.ai.model.ChatMessage
import com.example.batabung.data.ApiKeyManager
import com.example.batabung.data.local.entity.Tabungan
import com.example.batabung.data.repository.TabunganRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State untuk Chat Screen.
 */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isAIReady: Boolean = false,
    val currentTabungan: Tabungan? = null,
    val error: String? = null,
    val savedApiKey: String? = null  // API key yang tersimpan
)

/**
 * ViewModel untuk Chat Screen.
 * Mengelola state chat dan komunikasi dengan AI.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    private val aiController: AIController,
    private val promptBuilder: PromptBuilder,
    private val repository: TabunganRepository,
    private val apiKeyManager: ApiKeyManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    private var currentTabunganId: Long? = null
    
    init {
        loadCurrentTabungan()
        addWelcomeMessage()
        loadSavedApiKey()
    }
    
    /**
     * Load API key yang tersimpan dari DataStore.
     */
    private fun loadSavedApiKey() {
        viewModelScope.launch {
            val savedKey = apiKeyManager.getApiKey()
            if (!savedKey.isNullOrBlank()) {
                _uiState.update { it.copy(savedApiKey = savedKey) }
                initializeAI(savedKey)
            }
        }
    }
    
    /**
     * Inisialisasi AI dengan API key dan simpan ke DataStore.
     */
    fun initializeAI(apiKey: String) {
        viewModelScope.launch {
            aiController.initialize(apiKey)
            apiKeyManager.saveApiKey(apiKey)
            _uiState.update { 
                it.copy(
                    isAIReady = aiController.isInitialized(),
                    savedApiKey = apiKey
                ) 
            }
        }
    }
    
    private fun loadCurrentTabungan() {
        viewModelScope.launch {
            repository.getAllTabungan().collect { tabunganList ->
                if (tabunganList.isNotEmpty()) {
                    val tabungan = tabunganList.first()
                    currentTabunganId = tabungan.id
                    _uiState.update { it.copy(currentTabungan = tabungan) }
                }
            }
        }
    }
    
    private fun addWelcomeMessage() {
        val welcomeMessage = ChatMessage.AIMessage(
            text = "Halo! 👋 Saya BaTabung AI, asisten tabungan pribadimu.\n\n" +
                    "Kamu bisa bertanya tentang:\n" +
                    "• \"Saldo saya berapa?\"\n" +
                    "• \"Total pengeluaran bulan ini\"\n" +
                    "• \"Berapa persen target tercapai?\"\n" +
                    "• \"Apakah saya boros bulan ini?\"\n\n" +
                    "Silakan mulai bertanya! 💬"
        )
        _uiState.update { it.copy(messages = listOf(welcomeMessage)) }
    }
    
    /**
     * Kirim pesan dari user ke AI.
     */
    fun sendMessage(userInput: String) {
        if (userInput.isBlank()) return
        
        val tabunganId = currentTabunganId
        if (tabunganId == null) {
            addErrorMessage("Belum ada tabungan. Silakan buat tabungan terlebih dahulu.")
            return
        }
        
        if (!aiController.isInitialized()) {
            addErrorMessage("AI belum siap. Silakan set API key terlebih dahulu.")
            return
        }
        
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage.UserMessage(text = userInput)
            _uiState.update { 
                it.copy(
                    messages = it.messages + userMessage + ChatMessage.Loading,
                    isLoading = true,
                    error = null
                ) 
            }
            
            try {
                // Get financial context
                val context = promptBuilder.getFinancialContext(tabunganId)
                
                if (context == null) {
                    removeLoadingAndAddError("Tidak bisa mengambil data tabungan.")
                    return@launch
                }
                
                // Build prompt with context
                val prompt = promptBuilder.buildPrompt(userInput, context)
                
                // Send to AI
                val result = aiController.sendMessage(prompt)
                
                result.fold(
                    onSuccess = { response ->
                        val aiMessage = ChatMessage.AIMessage(text = response)
                        _uiState.update { 
                            it.copy(
                                messages = it.messages.filterNot { msg -> msg is ChatMessage.Loading } + aiMessage,
                                isLoading = false
                            ) 
                        }
                    },
                    onFailure = { error ->
                        removeLoadingAndAddError(
                            "Maaf, terjadi kesalahan: ${error.localizedMessage ?: "Unknown error"}"
                        )
                    }
                )
            } catch (e: Exception) {
                removeLoadingAndAddError("Terjadi kesalahan: ${e.localizedMessage}")
            }
        }
    }
    
    private fun addErrorMessage(message: String) {
        val errorMessage = ChatMessage.Error(message = message)
        _uiState.update { 
            it.copy(
                messages = it.messages + errorMessage,
                error = message
            ) 
        }
    }
    
    private fun removeLoadingAndAddError(message: String) {
        val errorMessage = ChatMessage.Error(message = message)
        _uiState.update { 
            it.copy(
                messages = it.messages.filterNot { msg -> msg is ChatMessage.Loading } + errorMessage,
                isLoading = false,
                error = message
            ) 
        }
    }
    
    /**
     * Clear chat history.
     */
    fun clearChat() {
        addWelcomeMessage()
        _uiState.update { it.copy(error = null) }
    }
}
