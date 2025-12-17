package com.example.batabung.worker

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manager untuk scheduling sync work.
 */
@Singleton
class SyncManager @Inject constructor(
    private val context: Context
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Schedule periodic sync setiap 15 menit (minimum interval WorkManager).
     * Hanya berjalan saat ada koneksi internet.
     */
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
    
    /**
     * Trigger sync sekali (one-time) segera.
     * Digunakan untuk manual refresh atau setelah login.
     */
    fun triggerImmediateSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        
        workManager.enqueue(syncRequest)
    }
    
    /**
     * Cancel semua sync work.
     * Digunakan saat logout.
     */
    fun cancelAllSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
    }
}
