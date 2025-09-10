package com.example.laboratoriodeldolor

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Periodic worker that removes diary (mood) and pain records older than the configured
 * retention period defined in SharedPreferences (pref_retention_months). Default is 3 months.
 */
class DataRetentionWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val prefs = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
            val months = prefs.getInt("pref_retention_months", 3)

            // calculate cutoffMillis: keep entries newer than cutoff => delete <= cutoff
            val now = System.currentTimeMillis()
            val millisInMonth = 30L * 24L * 60L * 60L * 1000L
            val cutoff = now - months * millisInMonth

            val db = (applicationContext as? MoodApplication)?.database
            if (db != null) {
                // AppDatabase exposes moodDao() and painPointDao()
                db.moodDao().deleteOlderThan(cutoff)
                db.painPointDao().deleteOlderThan(cutoff)
            }

            Result.success()
        } catch (t: Throwable) {
            t.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "data_retention_cleanup"

        /** Schedule daily periodic cleanup (once per day). Call from Application.onCreate. */
        fun scheduleDaily(context: Context) {
            val req = PeriodicWorkRequestBuilder<DataRetentionWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.HOURS) // small delay after app start
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, req)
        }
    }
}
