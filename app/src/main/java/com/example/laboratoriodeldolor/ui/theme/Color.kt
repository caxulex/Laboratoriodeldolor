package com.example.laboratoriodeldolor.ui.theme

import androidx.compose.ui.graphics.Color

// Design audit color palette (exact values for dark theme)
val NavyBackground = Color(0xFF071028) // Background: #071028
val NavySurface = Color(0xFF0B1630)    // Cards/Surface: #0B1630

val AccentCTA = Color(0xFFFFB74D)      // Accent/CTA: #FFB74D
val AccentOnCTA = Color(0xFF000000)    // Black text on amber CTA

// Text colors
val PrimaryText = Color(0xFFF3F8FF)    // Primary Text: #F3F8FF
val SecondaryText = Color(0xFFB7C6D6)  // Secondary/Muted Text: #B7C6D6

// Map to the theme-friendly names used by Material3 color scheme
val Background = NavyBackground
val Surface = NavySurface
val OnBackground = PrimaryText
val OnSurface = PrimaryText

val Primary = AccentCTA
val OnPrimary = AccentOnCTA
val PrimaryContainer = Color(0xFFFBE6C7)

// A neutral container color for secondary surfaces (keeps a subtle contrast on dark theme)
val SecondaryContainer = Color(0xFF0F2438)

// Lighter/darker accents for gradients and states
val AccentLight = Color(0xFFFFD8A8)
val AccentDark = AccentCTA

val Secondary = SecondaryText
val OnSecondary = NavyBackground

// Keep an unobtrusive tertiary and error color
val Tertiary = Color(0xFFE1BEE7)
val OnTertiary = NavyBackground

val Error = Color(0xFFCF6679)