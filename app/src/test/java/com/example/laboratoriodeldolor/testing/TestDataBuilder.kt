package com.example.laboratoriodeldolor.testing

import com.example.laboratoriodeldolor.*

/**
 * Builder class for creating test data objects.
 * Provides consistent test data for all tests.
 */
object TestDataBuilder {

    /**
     * Creates a single MoodEntry for testing
     */
    fun createMoodEntry(
        id: Int = 1,
        emoji: String = "😊",
        note: String = "Test mood entry",
        timestamp: Long = System.currentTimeMillis(),
        moodScore: Int = 3
    ) = MoodEntry(
        id = id,
        emoji = emoji,
        note = note,
        timestamp = timestamp,
        moodScore = moodScore
    )

    /**
     * Creates a list of MoodEntry objects for testing
     */
    fun createMoodEntries(count: Int = 3): List<MoodEntry> {
        val emojis = listOf("😊", "😐", "😢", "😡", "😰")
        return (1..count).map { index ->
            createMoodEntry(
                id = index,
                emoji = emojis[(index - 1) % emojis.size],
                note = "Test mood entry $index",
                timestamp = System.currentTimeMillis() - (index * 1000L * 60 * 60 * 24), // Days ago
                moodScore = (index % 5) + 1
            )
        }
    }

    /**
     * Creates a single PainPoint for testing
     */
    fun createPainPoint(
        id: Long = 1L,
        x: Float = 0.5f,
        y: Float = 0.5f,
        view: String = "front",
        logId: Long = 0L,
        intensity: Int = 2,
        timestamp: Long = System.currentTimeMillis()
    ) = PainPoint(
        id = id,
        x = x,
        y = y,
        view = view,
        logId = logId,
        intensity = intensity,
        timestamp = timestamp
    )

    /**
     * Creates a list of PainPoint objects for testing
     */
    fun createPainPoints(count: Int = 3): List<PainPoint> {
        val views = listOf("front", "back")
        return (1..count).map { index ->
            createPainPoint(
                id = index.toLong(),
                x = (0.2f + (index * 0.2f)).coerceAtMost(0.8f),
                y = (0.3f + (index * 0.1f)).coerceAtMost(0.7f),
                view = views[(index - 1) % views.size],
                logId = index.toLong(),
                intensity = (index % 3) + 1,
                timestamp = System.currentTimeMillis() - (index * 1000L * 60 * 60)
            )
        }
    }

    /**
     * Creates a single ExerciseLog for testing
     */
    fun createExerciseLog(
        id: Long = 1L,
        timestamp: Long = System.currentTimeMillis()
    ) = ExerciseLog(
        id = id,
        timestamp = timestamp
    )

    /**
     * Creates a list of ExerciseLog objects for testing
     */
    fun createExerciseLogs(count: Int = 3): List<ExerciseLog> {
        return (1..count).map { index ->
            createExerciseLog(
                id = index.toLong(),
                timestamp = System.currentTimeMillis() - (index * 1000L * 60 * 60 * 24)
            )
        }
    }

    /**
     * Creates test data for pain log
     */
    fun createPainLog(
        id: Long = 1L,
        timestamp: Long = System.currentTimeMillis()
    ) = PainLog(
        id = id,
        timestamp = timestamp
    )

    /**
     * Creates mood data for charts and analytics  
     */
    fun createMoodChartData(days: Int = 7): List<Pair<Long, Int>> {
        return (0 until days).map { dayOffset ->
            (System.currentTimeMillis() - (dayOffset * 24 * 60 * 60 * 1000L)) to (3 + (dayOffset % 3))
        }.reversed()
    }

    /**
     * Creates pain data for charts and analytics
     */
    fun createPainChartData(days: Int = 7): List<Pair<Long, Float>> {
        return (0 until days).map { dayOffset ->
            (System.currentTimeMillis() - (dayOffset * 24 * 60 * 60 * 1000L)) to (1.0f + (dayOffset % 3))
        }.reversed()
    }
}