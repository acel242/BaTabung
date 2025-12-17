package com.example.batabung.data.local.dao

import androidx.room.*
import com.example.batabung.data.local.entity.ChatMessageEntity
import com.example.batabung.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi chat messages.
 */
@Dao
interface ChatMessageDao {
    
    /**
     * Insert atau replace chat message.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)
    
    /**
     * Insert multiple messages.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)
    
    /**
     * Get all messages for user ordered by timestamp.
     */
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>
    
    /**
     * Get all messages for user (non-flow, for sync).
     */
    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getMessagesForUserOnce(userId: String): List<ChatMessageEntity>
    
    /**
     * Get messages by sync status (for pushing to cloud).
     */
    @Query("SELECT * FROM chat_messages WHERE syncStatus = :status")
    suspend fun getMessagesBySyncStatus(status: SyncStatus): List<ChatMessageEntity>
    
    /**
     * Update sync status of a message.
     */
    @Query("UPDATE chat_messages SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus)
    
    /**
     * Delete all messages for user.
     */
    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun deleteAllForUser(userId: String)
    
    /**
     * Delete a specific message.
     */
    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteById(id: String)
    
    /**
     * Get message count for user.
     */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE userId = :userId")
    suspend fun getMessageCount(userId: String): Int
}
