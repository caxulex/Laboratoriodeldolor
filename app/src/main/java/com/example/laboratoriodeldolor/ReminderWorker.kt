package com.example.laboratoriodeldolor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.Manifest
import android.content.pm.PackageManager
import android.annotation.SuppressLint
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result as WorkResult

/**
 * Worker that shows a notification for the exercise reminder. This is lightweight and quick.
 */
class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): WorkResult {
        showNotification(applicationContext)
        return WorkResult.success()
    }

    @SuppressLint("NotificationPermission")
    private fun showNotification(context: Context) {
        val channelId = "exercise_reminder_channel"
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(channelId, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(ch)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    // minSdk >= M in this project, so we can use FLAG_IMMUTABLE directly.
    val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notif = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.exercise_reminder_title))
            .setContentText(context.getString(R.string.exercise_reminder_text))
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        // When targeting Android 13+ (API 33/TIRAMISU) posting notifications requires
        // the POST_NOTIFICATIONS runtime permission. A Worker can't request
        // permissions from the user, so check and bail out early if not granted.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted; skip posting to avoid lint/runtime issues.
            return
        }

        nm.notify(1001, notif)
    }
}
