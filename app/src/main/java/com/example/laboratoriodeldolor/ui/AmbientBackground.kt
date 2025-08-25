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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

/**
 * Subtle ambient background: slow-moving, time-aware multi-color gradient.
 * Low opacity so it remains calm and non-distracting.
 */
@Composable
fun AmbientBackground(modifier: Modifier = Modifier) {
    // compute palette based on hour
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    // read theme colors in composable scope (safe for DrawScope use later)
    val morningA = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
    val morningB = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)

    val afternoonA = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.12f)
    val afternoonB = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)

    val eveningA = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.12f)
    val eveningB = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)

    val nightA = MaterialTheme.colorScheme.background.copy(alpha = 0.14f)
    val nightB = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f)

    val baseColors = when (hour) {
        in 6..11 -> listOf(morningA, morningB)
        in 12..17 -> listOf(afternoonA, afternoonB)
        in 18..20 -> listOf(eveningA, eveningB)
        else -> listOf(nightA, nightB)
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
