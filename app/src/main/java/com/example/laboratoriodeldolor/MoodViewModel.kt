package com.example.laboratoriodeldolor

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.laboratoriodeldolor.data.StoredPriority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class MoodViewModel(
    private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository,
    private val exerciseRepository: com.example.laboratoriodeldolor.repository.ExerciseRepository,
    private val preferencesRepository: com.example.laboratoriodeldolor.data.UserPreferencesRepository? = null
) : ViewModel() {
    // State is still here
    private val tag = "MoodApp"
    // Use explicit MutableState backing fields instead of delegated properties to avoid compiler delegation issues in ViewModel
    private val _selectedEmoji = mutableStateOf("😊")
    var selectedEmoji: String
        get() = _selectedEmoji.value
        set(value) { _selectedEmoji.value = value }

    private val _noteText = mutableStateOf("")
    var noteText: String
        get() = _noteText.value
        set(value) { _noteText.value = value }

    val moodEntries: StateFlow<List<MoodEntry>> = moodRepository.getAllMoodEntries()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expose the user's current exercise streak (consecutive calendar days with >=1 log)
    private val _streak = MutableStateFlow(0)
    val streak = _streak.asStateFlow()
    private val _loggedToday = MutableStateFlow(false)
    val loggedToday = _loggedToday.asStateFlow()

    init {
        // Recompute streak whenever exercise logs change
        viewModelScope.launch {
            exerciseRepository.getAllExerciseLogs().collectLatest { logs ->
                val timestamps = logs.map { it.timestamp }
                _streak.value = computeStreakFromLogs(timestamps)

                // determine whether there's any log for today
                val zone = ZoneId.systemDefault()
                val today = java.time.LocalDate.now(zone)
                val hasToday = timestamps.map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }.any { it.isEqual(today) }
                _loggedToday.value = hasToday
            }
        }
    }

    // Dashboard prioritization logic for the main screen
    enum class DashboardPriority { DIARY, PAIN, BREATH }

    private val _dashboardPriority = MutableStateFlow(DashboardPriority.DIARY)
    val dashboardPriority = _dashboardPriority.asStateFlow()

    fun setDashboardPriority(priority: DashboardPriority) {
        _dashboardPriority.value = priority

        // Persist the user's choice if a preferences repository is available
        preferencesRepository?.let { repo ->
            viewModelScope.launch {
                try {
                    val stored = when (priority) {
                        DashboardPriority.DIARY -> StoredPriority.DIARY
                        DashboardPriority.PAIN -> StoredPriority.PAIN
                        DashboardPriority.BREATH -> StoredPriority.BREATH
                    }
                    repo.setDashboardPriority(stored)
                } catch (e: Exception) {
                    // Swallow and log if persistence fails
                }
            }
        }
    }

    // The save function is now updated
    fun onMoodSave() {
        // Launch a coroutine to do the database work in the background
        viewModelScope.launch {
            try {
                Log.d(tag, "Saving mood: emoji=${selectedEmoji} note=${noteText}")
                val newEntry = MoodEntry(
                    emoji = selectedEmoji,
                    note = noteText,
                    timestamp = System.currentTimeMillis(),
                    moodScore = MoodMapping.emojiToScore(selectedEmoji)
                )
                withContext(Dispatchers.IO) { moodRepository.insertMoodEntry(newEntry) }
                Log.d(tag, "Saved mood entry at ${'$'}{newEntry.timestamp}")
            } catch (e: Exception) {
                // Log exception so we can debug insert failures (Room, DB locked, etc.)
                Log.e(tag, "onMoodSave failed", e)
            }
        }
    }

    // Use MoodMapping.emojiToScore for centralized mapping

    /**
     * Record a timestamped exercise completion.
     */
    fun logExerciseCompleted() {
        viewModelScope.launch {
                try {
                val now = System.currentTimeMillis()
                withContext(Dispatchers.IO) { exerciseRepository.insertExerciseLog(ExerciseLog(timestamp = now)) }
                Log.d(tag, "Logged exercise at ${'$'}now")
            } catch (e: Exception) {
                Log.e(tag, "logExerciseCompleted failed", e)
            }
        }
    }

    /**
     * Undo: delete the most recent exercise log.
     */
    fun undoLastExercise() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { exerciseRepository.deleteMostRecentExerciseLog() }
                Log.d(tag, "Deleted most recent exercise log (undo)")
            } catch (e: Exception) {
                Log.e(tag, "undoLastExercise failed", e)
            }
        }
    }

    /**
     * Compute consecutive-day streak ending today from a list of timestamps (milliseconds UTC).
     * Handles multiple logs per day, timezone differences, and gaps.
     */
    private fun computeStreakFromLogs(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        // Convert to LocalDate in the device default zone, deduplicate by date
        val zone = ZoneId.systemDefault()
        val days = timestamps
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .distinct()
            .sortedDescending()

        val today = LocalDate.now(zone)
        var streakCount = 0
        var expectedDate = today

        for (d in days) {
            if (d.isEqual(expectedDate)) {
                streakCount++
                expectedDate = expectedDate.minusDays(1)
            } else if (d.isBefore(expectedDate)) {
                // Gap: streak ends
                break
            } else {
                // d is after expectedDate (future-dated log) - skip
            }
        }

        return streakCount
    }
}
