package com.example.laboratoriodeldolor

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Calendar
import android.os.Build

/**
 * ViewModel that stores the user's reminder hour/minute in SharedPreferences and
 * schedules an exact Alarm via AlarmManager. Uses SharedPreferences for synchronous
 * reads from the BroadcastReceiver.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val PREF_HOUR = "reminder_hour"
    private val PREF_MINUTE = "reminder_minute"
    private val PREF_RECURRING = "reminder_recurring"

    private val _reminderTime = MutableStateFlow<Pair<Int, Int>>(getStoredTime())
    val reminderTime: StateFlow<Pair<Int, Int>> = _reminderTime
    private val _recurring = MutableStateFlow(getRecurringEnabled())
    val recurring: StateFlow<Boolean> = _recurring

    init {
    // Do not schedule alarms from the ViewModel constructor - scheduling can throw
    // on some platforms when exact alarms are not permitted. Call `scheduleAlarm`
    // explicitly (for example from an Activity or when the user confirms a time).
    }

    private fun getStoredTime(): Pair<Int, Int> {
        val h = prefs.getInt(PREF_HOUR, -1)
        val m = prefs.getInt(PREF_MINUTE, -1)
        return if (h >= 0 && m >= 0) Pair(h, m) else Pair(9, 0)
    }

    fun setReminderTime(hour: Int, minute: Int) {
        prefs.edit().putInt(PREF_HOUR, hour).putInt(PREF_MINUTE, minute).apply()
        _reminderTime.value = Pair(hour, minute)
        // Schedule immediately when user sets a time. If recurring is disabled, this
        // schedules a single alarm; enabling recurring will cause automatic rescheduling.
        scheduleAlarm(getApplication())
    }

    fun setRecurringEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(PREF_RECURRING, enabled).apply()
        _recurring.value = enabled
        if (enabled) {
            // If enabling recurring, ensure next alarm exists for stored time
            scheduleAlarm(getApplication())
        } else {
            // Cancel any scheduled alarm
            cancelAlarm(getApplication())
        }
    }

    private fun scheduleAlarmIfNeeded(context: Context) {
        // If no explicit preference saved, schedule default 9:00
        val stored = getStoredTime()
        // Only schedule if recurring enabled by user
        if (getRecurringEnabled()) scheduleAlarm(context)
    }

    private fun getRecurringEnabled(): Boolean {
        return prefs.getBoolean(PREF_RECURRING, true)
    }

    fun scheduleAlarm(context: Context) {
        val (hour, minute) = getStoredTime()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val triggerTime = computeNextTriggerMillis(hour, minute)

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMIND
        }
        val pendingFlags = if (android.os.Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT else FLAG_UPDATE_CURRENT
        val pending = PendingIntent.getBroadcast(context, 0, intent, pendingFlags)

        // Use exact alarm if the app is allowed to schedule exact alarms; otherwise fall back
        // to a non-exact alarm to avoid SecurityException on newer Android versions.
        try {
            val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // On API 31+, apps need SCHEDULE_EXACT_ALARM granted by user or by manifest+whitelisting.
                alarmManager.canScheduleExactAlarms()
            } else {
                true
            }

            if (canExact) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pending)
            } else {
                // Fall back to a non-exact set() which will not throw the exact-alarm permission exception.
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pending)
            }
        } catch (ise: SecurityException) {
            // As a last resort, schedule a non-exact alarm so the app doesn't crash.
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pending)
        }
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply { action = ReminderReceiver.ACTION_REMIND }
        val pendingFlags = if (android.os.Build.VERSION.SDK_INT >= 23) FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT else FLAG_UPDATE_CURRENT
        val pending = PendingIntent.getBroadcast(context, 0, intent, pendingFlags)
        alarmManager.cancel(pending)
    }

    private fun computeNextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val then = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (then.timeInMillis <= now.timeInMillis) {
            then.add(Calendar.DAY_OF_YEAR, 1)
        }
        return then.timeInMillis
    }
}
