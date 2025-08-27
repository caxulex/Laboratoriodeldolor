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
import androidx.compose.material3.MaterialTheme
import com.example.laboratoriodeldolor.ui.theme.GradientStart
import com.example.laboratoriodeldolor.ui.theme.GradientEnd

/**
 * Reusable immersive gradient background based on the app's central palette.
 * This is the primary full-screen background used across main screens.
 */
@Composable
fun GradientBackground(modifier: Modifier = Modifier) {
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
                brush = Brush.linearGradient(colors = listOf(GradientStart, GradientEnd), start = Offset(startX, 0f), end = Offset(endX, size.height)),
                size = size
            )
        }
    }
}

// Backwards-compatible ambient background wrapper that uses the main GradientBackground
@Composable
fun AmbientBackground(modifier: Modifier = Modifier) {
    GradientBackground(modifier = modifier)
}
