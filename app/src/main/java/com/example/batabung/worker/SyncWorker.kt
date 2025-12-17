package com.example.batabung.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.batabung.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * Worker untuk sinkronisasi data di background.
 * Menggunakan Hilt untuk dependency injection.
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {
    
    companion object {
        const val TAG = "SyncWorker"
        const val WORK_NAME = "sync_work"
    }
    
    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work...")
        
        return try {
            val result = syncRepository.fullSync()
            
            result.fold(
                onSuccess = {
                    Log.d(TAG, "Sync completed successfully")
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Sync failed: ${error.message}")
                    // Retry jika gagal, maksimal sesuai WorkRequest config
                    if (runAttemptCount < 3) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Sync worker exception: ${e.message}", e)
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
