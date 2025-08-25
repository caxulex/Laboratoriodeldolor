package com.example.laboratoriodeldolor.ui.theme

import androidx.compose.ui.graphics.Color

// ----- Light theme palette (calming, high-contrast, accessible) -----
val LightBackground = Color(0xFFF4F7FB)    // very light cool background
val LightSurface = Color(0xFFFFFFFF)       // cards / surfaces
val LightOnBackground = Color(0xFF0B1A2B)  // deep navy text on light bg
val LightOnSurface = Color(0xFF0B1A2B)

val LightPrimary = Color(0xFF1E88E5)       // calm blue (accessible)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDCEEFB)

val LightSecondary = Color(0xFF00796B)     // teal for secondary accents
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFB2DFDB)

val LightTertiary = Color(0xFF8E24AA)
val LightOnTertiary = Color(0xFFFFFFFF)

val LightError = Color(0xFFB00020)


// ----- Dark theme palette (existing, refined for accessibility) -----
val DarkBackground = Color(0xFF071028)    // deep navy background
val DarkSurface = Color(0xFF0B1630)       // slightly lighter card surface
val DarkOnBackground = Color(0xFFDEEAF6)  // light text for contrast
val DarkOnSurface = Color(0xFFDEEAF6)

val DarkPrimary = Color(0xFFFFB74D)       // warm amber CTA
val DarkOnPrimary = Color(0xFF000000)
val DarkPrimaryContainer = Color(0xFFFBE6C7)

val DarkSecondary = Color(0xFF94A7BF)     // muted steel for secondary
val DarkOnSecondary = DarkBackground
val DarkSecondaryContainer = Color(0xFF0F2438)

val DarkTertiary = Color(0xFFE1BEE7)
val DarkOnTertiary = DarkBackground

val DarkError = Color(0xFFCF6679)


// ----- No direct top-level alias: Theme.kt will pick the appropriate palette -----
