package com.example.laboratoriodeldolor

/**
 * Navigation helper utilities for the smart recommendation system.
 * Provides user-friendly messages and descriptions for navigation actions.
 */

/**
 * Get a user-friendly navigation message based on the target route.
 * Returns a string resource ID for localized messages.
 */
fun getNavigationMessage(route: String): Int {
    return when (route) {
        "front_upper_body" -> R.string.navigating_to_front_upper
        "back_upper_body" -> R.string.navigating_to_back_upper
        "front_middle_body" -> R.string.navigating_to_front_middle
        "back_middle_body" -> R.string.navigating_to_back_middle
        "front_lower_body" -> R.string.navigating_to_front_lower
        "back_lower_body" -> R.string.navigating_to_back_lower
        "exercises" -> R.string.navigating_to_exercises
        else -> R.string.navigating_to_techniques
    }
}

/**
 * Get the screen title for a given route.
 * Useful for displaying navigation context to users.
 */
fun getScreenTitleForRoute(route: String): Int {
    return when (route) {
        "front_upper_body" -> R.string.front_upper_body_title
        "back_upper_body" -> R.string.back_upper_body_title
        "front_middle_body" -> R.string.front_middle_body_title
        "back_middle_body" -> R.string.back_middle_body_title
        "front_lower_body" -> R.string.front_lower_body_title
        "back_lower_body" -> R.string.back_lower_body_title
        "exercises" -> R.string.exercises_label
        else -> R.string.techniques_label
    }
}
