package com.example.batabung.ai.model

/**
 * Sealed class untuk merepresentasikan pesan dalam chat.
 */
sealed class ChatMessage {
    data class UserMessage(
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : ChatMessage()
    
    data class AIMessage(
        val text: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : ChatMessage()
    
    data object Loading : ChatMessage()
    
    data class Error(
        val message: String,
        val timestamp: Long = System.currentTimeMillis()
    ) : ChatMessage()
}
