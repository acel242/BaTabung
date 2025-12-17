package com.example.batabung.data.local.entity

/**
 * Enum untuk status sinkronisasi data dengan cloud.
 */
enum class SyncStatus {
    SYNCED,     // Data sudah tersinkron dengan Supabase
    PENDING,    // Data lokal belum di-upload ke Supabase
    CONFLICT    // Ada konflik antara data lokal dan cloud
}
