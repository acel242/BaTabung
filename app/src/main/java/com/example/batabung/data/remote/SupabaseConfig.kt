package com.example.batabung.data.remote

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import com.example.batabung.BuildConfig

/**
 * Singleton object untuk Supabase client configuration.
 * URL dan Key diambil dari BuildConfig.
 * 
 * Modules:
 * - Auth: Email/Password authentication via Supabase GoTrue
 * - Postgrest: Database operations with RLS
 * - Realtime: Real-time subscriptions (optional)
 */
object SupabaseConfig {
    
    private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
    private const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
    
    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            install(Auth)
            install(Postgrest)
            install(Realtime)
        }
    }
}
