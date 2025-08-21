package com.example.laboratoriodeldolor

import androidx.compose.ui.geometry.Offset
import org.junit.Assert.assertEquals
import org.junit.Test

class PainTrackerViewModelAreaDetectionTest {

    @Test
    fun detect_no_points_returns_empty() {
        val points = emptyList<Offset>()
        val areas = detectBodyAreas(points)
        assertEquals(0, areas.size)
    }

    @Test
    fun detect_upper_middle_lower_working() {
        val points = listOf(
            Offset(0.5f, 0.05f), // upper
            Offset(0.5f, 0.35f), // middle
            Offset(0.5f, 0.9f)   // lower
        )
        val areas = detectBodyAreas(points)
        assertEquals(setOf(BodyArea.UPPER, BodyArea.MIDDLE, BodyArea.LOWER), areas)
    }

    @Test
    fun detect_edge_cases_on_boundaries() {
        // exact 1/3 should be considered middle, exact 2/3 should be considered lower
        val yOneThird = 1f / 3f
        val yTwoThird = 2f / 3f
        val points = listOf(Offset(0f, yOneThird), Offset(0f, yTwoThird))
        val areas = detectBodyAreas(points)
        assertEquals(setOf(BodyArea.MIDDLE, BodyArea.LOWER), areas)
    }
}
