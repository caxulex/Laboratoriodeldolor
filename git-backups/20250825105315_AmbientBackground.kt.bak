package com.example.laboratoriodeldolor.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Subtle ambient background: slow-moving, time-aware multi-color gradient.
 * Low opacity so it remains calm and non-distracting.
 */
@Composable
fun AmbientBackground(modifier: Modifier = Modifier) {
    // compute palette based on hour
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val baseColors = when (hour) {
        in 6..11 -> listOf(Color(0xFFFFF3E0).copy(alpha = 0.18f), Color(0xFFFFE0B2).copy(alpha = 0.12f))
        in 12..17 -> listOf(Color(0xFFE3F2FD).copy(alpha = 0.12f), Color(0xFFBBDEFB).copy(alpha = 0.08f))
        in 18..20 -> listOf(Color(0xFFF3E5F5).copy(alpha = 0.12f), Color(0xFFE1BEE7).copy(alpha = 0.08f))
        else -> listOf(Color(0xFF071028).copy(alpha = 0.14f), Color(0xFF0B1630).copy(alpha = 0.18f))
    }

    val infinite = rememberInfiniteTransition()
    val shift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 40000, easing = LinearEasing))
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val startX = shift % size.width
            val endX = (shift + size.width) % size.width
            drawRect(
                brush = Brush.linearGradient(colors = baseColors, start = Offset(startX, 0f), end = Offset(endX, size.height)),
                size = size
            )
        }
    }
}
