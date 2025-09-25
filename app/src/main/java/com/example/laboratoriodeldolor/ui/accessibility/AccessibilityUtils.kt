package com.example.laboratoriodeldolor.ui.accessibility

import androidx.compose.foundation.clickable
import androidx.compose.ui.semantics.Role
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * Accessibility utilities for the Laboratoriodeldolor app.
 * Provides standardized accessibility enhancements for UI components.
 */
object AccessibilityUtils {
    
    /**
     * Standard content descriptions for common UI elements
     */
    object ContentDescriptions {
        const val BACK_BUTTON = "Navigate back"
        const val MENU_BUTTON = "Open menu"
        const val CLOSE_BUTTON = "Close"
        const val SAVE_BUTTON = "Save changes"
        const val DELETE_BUTTON = "Delete item"
        const val ADD_BUTTON = "Add new item"
        const val SETTINGS_BUTTON = "Open settings"
        const val REFRESH_BUTTON = "Refresh content"
        
        // Mood-specific descriptions
        const val MOOD_SELECTOR = "Select your current mood"
        const val MOOD_NOTE_INPUT = "Add optional note about your mood"
        
        // Pain-specific descriptions  
        const val PAIN_TRACKER = "Pain tracking interface"
        const val PAIN_INTENSITY = "Select pain intensity level"
        const val BODY_VIEW_SELECTOR = "Choose body view - front or back"
        
        // Exercise descriptions
        const val EXERCISE_START = "Start exercise routine"
        const val EXERCISE_COMPLETE = "Mark exercise as completed"
    }
    
    /**
     * State descriptions for dynamic UI elements
     */
    object StateDescriptions {
        const val SELECTED = "Selected"
        const val NOT_SELECTED = "Not selected"
        const val EXPANDED = "Expanded"
        const val COLLAPSED = "Collapsed"
        const val ENABLED = "Enabled"
        const val DISABLED = "Disabled"
        const val LOADING = "Loading"
        const val ERROR = "Error occurred"
    }
}

/**
 * Enhanced modifier for clickable elements with proper accessibility support
 */
fun Modifier.accessibleClickable(
    contentDescription: String,
    role: Role = Role.Button,
    stateDescription: String? = null,
    onClick: () -> Unit
): Modifier = this
    .semantics {
        this.contentDescription = contentDescription
        this.role = role
        stateDescription?.let { this.stateDescription = it }
    }
    .clickable(role = role, onClick = onClick)

/**
 * Modifier for heading text elements
 */
fun Modifier.accessibleHeading(
    contentDescription: String? = null  
): Modifier = this.semantics {
    heading()
    contentDescription?.let { this.contentDescription = it }
}

/**
 * Modifier for selectable items (like mood emojis, pain intensity levels)
 */
fun Modifier.accessibleSelectable(
    contentDescription: String,
    isSelected: Boolean,
    onClick: () -> Unit
): Modifier = this
    .semantics {
        this.contentDescription = contentDescription
        this.role = Role.RadioButton
        this.selected = isSelected
        this.stateDescription = if (isSelected) 
            AccessibilityUtils.StateDescriptions.SELECTED 
        else 
            AccessibilityUtils.StateDescriptions.NOT_SELECTED
    }
    .clickable(role = Role.RadioButton, onClick = onClick)

/**
 * Enhanced pain intensity descriptions for better accessibility
 */
fun getPainIntensityDescription(intensity: Int): String {
    return when (intensity) {
        0 -> "No pain - intensity level 0 out of 3"
        1 -> "Mild pain - intensity level 1 out of 3"
        2 -> "Moderate pain - intensity level 2 out of 3"
        3 -> "Severe pain - intensity level 3 out of 3"
        else -> "Pain intensity level $intensity"
    }
}

/**
 * Enhanced mood descriptions for better accessibility  
 */
fun getMoodDescription(emoji: String, score: Int): String {
    val moodLevel = when (score) {
        1 -> "Very sad"
        2 -> "Sad" 
        3 -> "Neutral"
        4 -> "Happy"
        5 -> "Very happy"
        else -> "Unknown mood"
    }
    return "$moodLevel - mood level $score out of 5. Emoji: $emoji"
}

/**
 * Exercise completion state description
 */
fun getExerciseStateDescription(isCompleted: Boolean, exerciseName: String): String {
    return if (isCompleted) {
        "$exerciseName completed"
    } else {
        "$exerciseName not yet completed"
    }
}