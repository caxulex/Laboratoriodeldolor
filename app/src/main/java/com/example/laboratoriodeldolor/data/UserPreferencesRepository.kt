package com.example.laboratoriodeldolor.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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

// Enum for age ranges in initial configuration
enum class AgeRange(val value: String, val displayText: String) {
    TEEN("teen", "13-17 años"),
    YOUNG_ADULT("young_adult", "18-29 años"),
    ADULT("adult", "30-49 años"),
    MIDDLE_AGE("middle_age", "50-64 años"),
    SENIOR("senior", "65+ años");

    companion object {
        fun fromString(value: String) = values().find { it.value == value } ?: ADULT
    }
}

// Enum for primary conditions in initial configuration
enum class PrimaryCondition(val value: String, val displayText: String) {
    CHRONIC_PAIN("chronic_pain", "Dolor crónico"),
    ARTHRITIS("arthritis", "Artritis"),
    BACK_PAIN("back_pain", "Dolor de espalda"),
    JOINT_PAIN("joint_pain", "Dolor articular"),
    MIGRAINE("migraine", "Migrañas"),
    FIBROMYALGIA("fibromyalgia", "Fibromialgia"),
    STRESS_ANXIETY("stress_anxiety", "Estrés y ansiedad"),
    INSOMNIA("insomnia", "Insomnio"),
    OTHER("other", "Otro");

    companion object {
        fun fromString(value: String) = values().find { it.value == value } ?: CHRONIC_PAIN
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
        
        // Initial Configuration Keys
        private val HAS_COMPLETED_INITIAL_CONFIG = booleanPreferencesKey("has_completed_initial_config")
        private val USER_AGE_RANGE = stringPreferencesKey("user_age_range")
        private val USER_PRIMARY_CONDITION = stringPreferencesKey("user_primary_condition")
        private val CONFIG_STEP_COMPLETED = intPreferencesKey("config_step_completed")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val PREFERRED_REMINDER_TIME = stringPreferencesKey("preferred_reminder_time")
        
        // Breathing Enhancement Keys
        private val BREATHING_AUDIO_ENABLED = booleanPreferencesKey("breathing_audio_enabled")
        private val BREATHING_VIBRATION_ENABLED = booleanPreferencesKey("breathing_vibration_enabled")
        private val BREATHING_AUDIO_VOLUME = floatPreferencesKey("breathing_audio_volume")
        private val BREATHING_VIBRATION_INTENSITY = intPreferencesKey("breathing_vibration_intensity")
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

    // Initial Configuration Flows
    val hasCompletedInitialConfigFlow: Flow<Boolean> = activeDataStore.data.map { prefs -> 
        prefs[HAS_COMPLETED_INITIAL_CONFIG] ?: false 
    }
    
    val userAgeRangeFlow: Flow<AgeRange> = activeDataStore.data.map { prefs -> 
        val value = prefs[USER_AGE_RANGE] ?: AgeRange.ADULT.value
        AgeRange.fromString(value)
    }
    
    val userPrimaryConditionFlow: Flow<PrimaryCondition> = activeDataStore.data.map { prefs -> 
        val value = prefs[USER_PRIMARY_CONDITION] ?: PrimaryCondition.CHRONIC_PAIN.value
        PrimaryCondition.fromString(value)
    }
    
    val configStepCompletedFlow: Flow<Int> = activeDataStore.data.map { prefs -> 
        prefs[CONFIG_STEP_COMPLETED] ?: 0 
    }
    
    val userNameFlow: Flow<String> = activeDataStore.data.map { prefs -> 
        prefs[USER_NAME] ?: "" 
    }
    
    val notificationsEnabledFlow: Flow<Boolean> = activeDataStore.data.map { prefs -> 
        prefs[NOTIFICATIONS_ENABLED] ?: true 
    }
    
    val preferredReminderTimeFlow: Flow<String> = activeDataStore.data.map { prefs -> 
        prefs[PREFERRED_REMINDER_TIME] ?: "20:00" 
    }
    
    // Breathing Enhancement Flows
    val breathingAudioEnabledFlow: Flow<Boolean> = activeDataStore.data.map { prefs -> 
        prefs[BREATHING_AUDIO_ENABLED] ?: true 
    }
    
    val breathingVibrationEnabledFlow: Flow<Boolean> = activeDataStore.data.map { prefs -> 
        prefs[BREATHING_VIBRATION_ENABLED] ?: true 
    }
    
    val breathingAudioVolumeFlow: Flow<Float> = activeDataStore.data.map { prefs -> 
        prefs[BREATHING_AUDIO_VOLUME] ?: 0.7f 
    }
    
    val breathingVibrationIntensityFlow: Flow<Int> = activeDataStore.data.map { prefs -> 
        prefs[BREATHING_VIBRATION_INTENSITY] ?: 2 // 1=low, 2=medium, 3=high
    }

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
    
    // Initial Configuration Methods
    suspend fun setHasCompletedInitialConfig(completed: Boolean) {
        activeDataStore.edit { prefs ->
            prefs[HAS_COMPLETED_INITIAL_CONFIG] = completed
        }
    }
    
    suspend fun setUserAgeRange(ageRange: AgeRange) {
        activeDataStore.edit { prefs ->
            prefs[USER_AGE_RANGE] = ageRange.value
        }
    }
    
    suspend fun setUserPrimaryCondition(condition: PrimaryCondition) {
        activeDataStore.edit { prefs ->
            prefs[USER_PRIMARY_CONDITION] = condition.value
        }
    }
    
    suspend fun setConfigStepCompleted(step: Int) {
        activeDataStore.edit { prefs ->
            prefs[CONFIG_STEP_COMPLETED] = step
        }
    }
    
    suspend fun setUserName(name: String) {
        activeDataStore.edit { prefs ->
            prefs[USER_NAME] = name
        }
    }
    
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        activeDataStore.edit { prefs ->
            prefs[NOTIFICATIONS_ENABLED] = enabled
        }
    }
    
    suspend fun setPreferredReminderTime(time: String) {
        activeDataStore.edit { prefs ->
            prefs[PREFERRED_REMINDER_TIME] = time
        }
    }
    
    suspend fun completeInitialConfiguration() {
        activeDataStore.edit { prefs ->
            prefs[HAS_COMPLETED_INITIAL_CONFIG] = true
            prefs[CONFIG_STEP_COMPLETED] = 6 // Total steps in configuration
        }
    }
    
    // Breathing Enhancement Methods
    suspend fun setBreathingAudioEnabled(enabled: Boolean) {
        activeDataStore.edit { prefs ->
            prefs[BREATHING_AUDIO_ENABLED] = enabled
        }
    }
    
    suspend fun setBreathingVibrationEnabled(enabled: Boolean) {
        activeDataStore.edit { prefs ->
            prefs[BREATHING_VIBRATION_ENABLED] = enabled
        }
    }
    
    suspend fun setBreathingAudioVolume(volume: Float) {
        activeDataStore.edit { prefs ->
            prefs[BREATHING_AUDIO_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }
    
    suspend fun setBreathingVibrationIntensity(intensity: Int) {
        activeDataStore.edit { prefs ->
            prefs[BREATHING_VIBRATION_INTENSITY] = intensity.coerceIn(1, 3)
        }
    }
}
