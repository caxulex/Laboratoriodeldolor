package com.example.laboratoriodeldolor

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

class StreakCalculatorTest {

    private fun toMillis(year: Int, month: Int, day: Int, zone: ZoneId = ZoneId.systemDefault()): Long {
        val zdt = ZonedDateTime.of(year, month, day, 12, 0, 0, 0, zone)
        return zdt.toInstant().toEpochMilli()
    }

    @Test
    fun emptyLogs_zeroStreak() {
        assertEquals(0, StreakCalculator.computeStreak(emptyList()))
    }

    @Test
    fun singleToday_oneStreak() {
        val today = LocalDate.now()
        val millis = toMillis(today.year, today.monthValue, today.dayOfMonth)
        assertEquals(1, StreakCalculator.computeStreak(listOf(millis)))
    }

    @Test
    fun multipleSameDay_oneStreak() {
        val today = LocalDate.now()
        val millis1 = toMillis(today.year, today.monthValue, today.dayOfMonth)
        val millis2 = toMillis(today.year, today.monthValue, today.dayOfMonth)
        assertEquals(1, StreakCalculator.computeStreak(listOf(millis1, millis2)))
    }

    @Test
    fun consecutiveDays_threeStreak() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val t0 = toMillis(today.year, today.monthValue, today.dayOfMonth, zone)
        val t1 = toMillis(today.minusDays(1).year, today.minusDays(1).monthValue, today.minusDays(1).dayOfMonth, zone)
        val t2 = toMillis(today.minusDays(2).year, today.minusDays(2).monthValue, today.minusDays(2).dayOfMonth, zone)
        assertEquals(3, StreakCalculator.computeStreak(listOf(t0, t1, t2), zone, today))
    }

    @Test
    fun gapBreaksStreak() {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val t0 = toMillis(today.year, today.monthValue, today.dayOfMonth, zone)
        val t2 = toMillis(today.minusDays(2).year, today.minusDays(2).monthValue, today.minusDays(2).dayOfMonth, zone)
        assertEquals(1, StreakCalculator.computeStreak(listOf(t0, t2), zone, today))
    }
}
