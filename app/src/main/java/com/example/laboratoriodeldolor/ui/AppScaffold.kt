package com.example.laboratoriodeldolor.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * AppScaffold
 * Provides a single place to render the immersive gradient background and a
 * themed Surface for screen content. Use this at the top of screen composables
 * to ensure consistent look & feel across the app.
 */
@Composable
fun AppScaffold(content: @Composable (innerPadding: PaddingValues) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Animated immersive background
        GradientBackground()

        // Use a Scaffold with transparent container so the gradient remains visible.
        Scaffold(containerColor = Color.Transparent) { innerPadding ->
            // Draw content edge-to-edge; screens that need safe area can opt-in with statusBarsPadding/navigationBarsPadding
            Surface(modifier = Modifier.fillMaxSize(), color = Color.Transparent) {
                content(innerPadding)
            }
        }
    }
}
