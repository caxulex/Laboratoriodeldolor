package com.example.laboratoriodeldolor

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// Pure algorithm unit tests for computeStreakFromLogs logic (copied from MoodViewModel).
class MoodViewModelStreakTest {

    private fun computeStreakFromLogs(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

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
                break
            }
        }

        return streakCount
    }

    @Test
    fun streak_noEntries_returnsZero() {
        assertEquals(0, computeStreakFromLogs(emptyList()))
    }

    @Test
    fun streak_multipleLogsSameDay_countsOnce() {
        val now = System.currentTimeMillis()
        val a = now
        val b = now - 1000L
        assertEquals(1, computeStreakFromLogs(listOf(a, b)))
    }

    @Test
    fun streak_consecutiveDays_countsCorrectly() {
        val today = System.currentTimeMillis()
        val yesterday = today - 24L * 60 * 60 * 1000
        val twoDaysAgo = yesterday - 24L * 60 * 60 * 1000
        assertEquals(3, computeStreakFromLogs(listOf(today, yesterday, twoDaysAgo)))
    }

    @Test
    fun streak_gapStopsCount() {
        val today = System.currentTimeMillis()
        val threeDaysAgo = today - 3L * 24 * 60 * 60 * 1000
        assertEquals(1, computeStreakFromLogs(listOf(today, threeDaysAgo)))
    }
}
