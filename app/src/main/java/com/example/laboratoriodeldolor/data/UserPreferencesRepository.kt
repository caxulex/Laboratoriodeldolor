package com.example.laboratoriodeldolor.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

// Map DashboardPriority enum ordinals to ints. Kept as a top-level enum so callers can reference it
enum class StoredPriority(val value: Int) {
    DIARY(0), PAIN(1), BREATH(2);

    companion object {
        fun fromInt(value: Int) = values().getOrNull(value) ?: DIARY
    }
}

class UserPreferencesRepository(private val context: Context) {

    companion object {
        private val DASHBOARD_PRIORITY_KEY = intPreferencesKey("dashboard_priority")
    }

    val dashboardPriorityFlow: Flow<StoredPriority> = context.dataStore.data
        .map { prefs: Preferences ->
            val raw = prefs[DASHBOARD_PRIORITY_KEY] ?: StoredPriority.DIARY.value
            StoredPriority.fromInt(raw)
        }

    suspend fun setDashboardPriority(priority: StoredPriority) {
        context.dataStore.edit { prefs ->
            prefs[DASHBOARD_PRIORITY_KEY] = priority.value
        }
    }
}
