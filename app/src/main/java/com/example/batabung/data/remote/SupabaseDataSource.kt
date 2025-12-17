package com.example.batabung.data.remote

import android.util.Log
import com.example.batabung.data.local.entity.Bank
import com.example.batabung.data.local.entity.JenisBank
import com.example.batabung.data.local.entity.Transaksi
import com.example.batabung.data.local.entity.JenisTransaksi
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Remote data source untuk operasi Supabase Postgrest.
 * Struktur baru: Full Account Sync dengan chat history
 */
@Singleton
class SupabaseDataSource @Inject constructor() {
    
    companion object {
        private const val TAG = "SupabaseDataSource"
    }
    
    private val supabase = SupabaseConfig.client
    
    // === BANK OPERATIONS ===
    
    suspend fun fetchBanks(userId: String): List<BankDto> {
        return try {
            Log.d(TAG, "=== FETCH BANKS ===")
            Log.d(TAG, "Fetching banks for user_id: $userId")
            
            val result = supabase.postgrest["banks"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<BankDto>()
            
            Log.d(TAG, "Successfully fetched ${result.size} banks from Supabase")
            result.forEachIndexed { index, bank ->
                Log.d(TAG, "Bank[$index]: id=${bank.id}, nama=${bank.nama}, userId=${bank.userId}")
            }
            result
        } catch (e: Exception) {
            Log.e(TAG, "ERROR fetching banks: ${e.message}", e)
            Log.e(TAG, "Exception type: ${e::class.simpleName}")
            emptyList()
        }
    }
    
    suspend fun upsertBank(bank: Bank): Result<Unit> {
        return try {
            Log.d(TAG, "=== UPSERT BANK ===")
            Log.d(TAG, "Upserting bank: id=${bank.id}, nama=${bank.nama}, userId=${bank.userId}")
            supabase.postgrest["banks"]
                .upsert(bank.toDto())
            Log.d(TAG, "Successfully upserted bank: ${bank.nama}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "ERROR upserting bank: ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun deleteBank(id: String): Result<Unit> {
        return try {
            supabase.postgrest["banks"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // === TRANSAKSI OPERATIONS ===
    
    suspend fun fetchTransaksi(userId: String): List<TransaksiDto> {
        return try {
            Log.d(TAG, "=== FETCH TRANSAKSI ===")
            Log.d(TAG, "Fetching transaksi for user_id: $userId")
            
            val result = supabase.postgrest["transaksi"]
                .select {
                    filter { eq("user_id", userId) }
                }
                .decodeList<TransaksiDto>()
            
            Log.d(TAG, "Successfully fetched ${result.size} transaksi from Supabase")
            result
        } catch (e: Exception) {
            Log.e(TAG, "ERROR fetching transaksi: ${e.message}", e)
            emptyList()
        }
    }
    
    suspend fun upsertTransaksi(transaksi: Transaksi): Result<Unit> {
        return try {
            supabase.postgrest["transaksi"]
                .upsert(transaksi.toDto())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTransaksi(id: String): Result<Unit> {
        return try {
            supabase.postgrest["transaksi"]
                .delete { filter { eq("id", id) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTransactionsByBank(bankId: String): Result<Unit> {
        return try {
            supabase.postgrest["transaksi"]
                .delete { filter { eq("bank_id", bankId) } }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // NOTE: Chat AI adalah LOKAL SAJA - tidak disinkronkan ke cloud untuk privasi
    
    // === USER PROFILE OPERATIONS ===
    
    suspend fun fetchUserProfile(userId: String): UserProfileDto? {
        return try {
            supabase.postgrest["user_profiles"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<UserProfileDto>()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun upsertUserProfile(profile: UserProfileDto): Result<Unit> {
        return try {
            supabase.postgrest["user_profiles"]
                .upsert(profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// === DTOs for Supabase serialization ===

@Serializable
data class BankDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val nama: String,
    val alias: String = "",
    val jenis: String,
    @SerialName("is_aktif") val isAktif: Boolean = true,
    @SerialName("package_name") val packageName: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long
) {
    fun toEntity() = Bank(
        id = id,
        userId = userId,
        nama = nama,
        alias = alias,
        jenis = JenisBank.valueOf(jenis),
        isAktif = isAktif,
        packageName = packageName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

@Serializable
data class TransaksiDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("bank_id") val bankId: String,
    val tanggal: Long,
    val jenis: String,
    val jumlah: Long,
    val kategori: String = "",
    val catatan: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("updated_at") val updatedAt: Long
) {
    fun toEntity() = Transaksi(
        id = id,
        userId = userId,
        bankId = bankId,
        tanggal = tanggal,
        jenis = JenisTransaksi.valueOf(jenis),
        jumlah = jumlah,
        kategori = kategori,
        catatan = catatan,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// === Extension functions for Entity to DTO conversion ===

fun Bank.toDto() = BankDto(
    id = id,
    userId = userId,
    nama = nama,
    alias = alias,
    jenis = jenis.name,
    isAktif = isAktif,
    packageName = packageName,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Transaksi.toDto() = TransaksiDto(
    id = id,
    userId = userId,
    bankId = bankId,
    tanggal = tanggal,
    jenis = jenis.name,
    jumlah = jumlah,
    kategori = kategori,
    catatan = catatan,
    createdAt = createdAt,
    updatedAt = updatedAt
)

// NOTE: ChatMessageDto dihapus - Chat AI lokal saja, tidak disinkronkan ke cloud

// === User Profile DTO ===

@Serializable
data class UserProfileDto(
    val id: String,
    val email: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("created_at") val createdAt: Long = System.currentTimeMillis(),
    @SerialName("updated_at") val updatedAt: Long = System.currentTimeMillis()
)
