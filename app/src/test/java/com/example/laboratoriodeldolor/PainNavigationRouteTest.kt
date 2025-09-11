package com.example.laboratoriodeldolor

import org.junit.Assert.assertEquals
import org.junit.Test

class PainNavigationRouteTest {

    @Test
    fun analyze_front_upper_returns_front_upper_route() {
        val points = listOf(
            PainPoint(x = 0.5f, y = 0.1f, view = "front", intensity = 2)
        )
        val route = analyzePainPointsForNavigation(points)
        assertEquals("front_upper_body", route)
    }

    @Test
    fun analyze_back_lower_returns_back_lower_route() {
        val points = listOf(
            PainPoint(x = 0.5f, y = 0.85f, view = "back", intensity = 3)
        )
        val route = analyzePainPointsForNavigation(points)
        assertEquals("back_lower_body", route)
    }
}
