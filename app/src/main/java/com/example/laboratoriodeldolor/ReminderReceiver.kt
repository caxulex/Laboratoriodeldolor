package com.example.laboratoriodeldolor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import java.util.Calendar
import android.os.Build

/**
 * BroadcastReceiver triggered by AlarmManager; enqueues a WorkManager job to show notification.
 */
class ReminderReceiver : BroadcastReceiver() {
    companion object {
        const val ACTION_REMIND = "com.example.laboratoriodeldolor.ACTION_REMIND"
        const val WORK_NAME = "exercise_reminder_work"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == ACTION_REMIND) {
            // Enqueue a short-lived work to show notification
            val work = OneTimeWorkRequestBuilder<ReminderWorker>().build()
            WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, work)

            // Auto-reschedule the next reminder for the following day using stored preferences
            try {
                val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                val recurring = prefs.getBoolean("reminder_recurring", true)
                if (!recurring) return
                val hour = prefs.getInt("reminder_hour", 9)
                val minute = prefs.getInt("reminder_minute", 0)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val nextTrigger = computeNextTriggerMillis(hour, minute)
                val nextIntent = Intent(context, ReminderReceiver::class.java).apply { action = ACTION_REMIND }
                val flags = FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
                val pending = PendingIntent.getBroadcast(context, 0, nextIntent, flags)

                try {
                    val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) alarmManager.canScheduleExactAlarms() else true
                    if (canExact) alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTrigger, pending)
                    else alarmManager.set(AlarmManager.RTC_WAKEUP, nextTrigger, pending)
                } catch (se: SecurityException) {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, nextTrigger, pending)
                }
            } catch (t: Throwable) {
                // Don't crash if scheduling fails
                t.printStackTrace()
            }
        }
    }

    private fun computeNextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        // ensure next occurrence (tomorrow if time already passed)
        if (then.timeInMillis <= now.timeInMillis) then.add(Calendar.DAY_OF_YEAR, 1)
        return then.timeInMillis
    }
}
