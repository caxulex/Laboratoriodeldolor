package com.example.laboratoriodeldolor

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object StreakCalculator {
    /**
     * Compute consecutive-day streak ending at `today` (in `zone`).
     * timestamps are epoch millis (UTC). Returns 0 for empty input.
     */
    fun computeStreak(timestamps: List<Long>, zone: ZoneId = ZoneId.systemDefault(), today: LocalDate = LocalDate.now(zone)): Int {
        if (timestamps.isEmpty()) return 0

        val days = timestamps
            .map { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
            .distinct()
            .sortedDescending()

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
}
