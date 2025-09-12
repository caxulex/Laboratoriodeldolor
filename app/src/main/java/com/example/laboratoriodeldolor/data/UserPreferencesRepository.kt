package com.example.laboratoriodeldolor.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneId

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

// Map DashboardPriority enum ordinals to ints. Kept as a top-level enum so callers can reference it
enum class StoredPriority(val value: Int) {
    DIARY(0), PAIN(1), BREATH(2);

    companion object {
        fun fromInt(value: Int) = values().getOrNull(value) ?: DIARY
    }
}

class UserPreferencesRepository(
    private val context: Context? = null,
    private val providedDataStore: DataStore<Preferences>? = null,
    // Optional scope used by callers/tests that need deterministic cancellation or control
    val externalScope: CoroutineScope? = null
) {

    companion object {
        private val DASHBOARD_PRIORITY_KEY = intPreferencesKey("dashboard_priority")
        private val HAS_SEEDED_CONTENT = booleanPreferencesKey("has_seeded_content")
    private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        private val EMOJI_PACK_KEY = intPreferencesKey("emoji_pack")
        private val LAST_CHECKIN_EPOCH_DAY = longPreferencesKey("last_checkin_epoch_day")
        private val BOX_PHASE_SECONDS = intPreferencesKey("box_phase_seconds")
    }

    private val activeDataStore: DataStore<Preferences> = providedDataStore ?: (context?.dataStore
        ?: throw IllegalArgumentException("Either context or providedDataStore must be provided"))

    val dashboardPriorityFlow: Flow<StoredPriority> = activeDataStore.data
        .map { prefs: Preferences ->
            val raw = prefs[DASHBOARD_PRIORITY_KEY] ?: StoredPriority.DIARY.value
            StoredPriority.fromInt(raw)
        }

    val hasSeededContentFlow: Flow<Boolean> = activeDataStore.data.map { prefs -> prefs[HAS_SEEDED_CONTENT] ?: false }

    val hasSeenOnboardingFlow: Flow<Boolean> = activeDataStore.data.map { prefs -> prefs[HAS_SEEN_ONBOARDING] ?: false }

    val emojiPackFlow: Flow<Int> = activeDataStore.data.map { prefs -> prefs[EMOJI_PACK_KEY] ?: 0 }

    // -1 means never checked in
    val lastCheckinEpochDayFlow: Flow<Long> = activeDataStore.data.map { prefs -> prefs[LAST_CHECKIN_EPOCH_DAY] ?: -1L }

    // Box breathing per-phase duration (in seconds). Defaults to 4.
    val boxPhaseSecondsFlow: Flow<Int> = activeDataStore.data.map { prefs -> prefs[BOX_PHASE_SECONDS] ?: 4 }

    suspend fun setDashboardPriority(priority: StoredPriority) {
        activeDataStore.edit { prefs ->
            prefs[DASHBOARD_PRIORITY_KEY] = priority.value
        }
    }

    suspend fun setHasSeededContent(value: Boolean) {
        activeDataStore.edit { prefs ->
            prefs[HAS_SEEDED_CONTENT] = value
        }
    }

    suspend fun setEmojiPack(index: Int) {
        activeDataStore.edit { prefs ->
            prefs[EMOJI_PACK_KEY] = index
        }
    }

    suspend fun setBoxPhaseSeconds(seconds: Int) {
        activeDataStore.edit { prefs ->
            prefs[BOX_PHASE_SECONDS] = seconds
        }
    }

    suspend fun setHasSeenOnboarding(value: Boolean) {
        activeDataStore.edit { prefs ->
            prefs[HAS_SEEN_ONBOARDING] = value
        }
    }

    suspend fun setLastCheckinEpochDay(epochDay: Long) {
        activeDataStore.edit { prefs ->
            prefs[LAST_CHECKIN_EPOCH_DAY] = epochDay
        }
    }

    suspend fun markCheckedInToday(zone: ZoneId = ZoneId.systemDefault()) {
        setLastCheckinEpochDay(LocalDate.now(zone).toEpochDay())
    }
}
