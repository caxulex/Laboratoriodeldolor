package com.example.laboratoriodeldolor.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.laboratoriodeldolor.ui.accessibility.AccessibilityUtils
import com.example.laboratoriodeldolor.ui.accessibility.accessibleClickable

/**
 * Accessible screen header with proper navigation and title semantics
 */
@Composable
fun AccessibleScreenHeader(
    title: String,
    onBackClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button with accessibility
        if (onBackClick != null) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = AccessibilityUtils.ContentDescriptions.BACK_BUTTON
                    }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null, // Handled by parent
                    modifier = Modifier.clearAndSetSemantics { }
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
        
        // Screen title as heading
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
                .weight(1f)
                .semantics {
                    heading()
                    contentDescription = "Screen title: $title"
                }
        )
        
        // Settings button with accessibility
        if (onSettingsClick != null) {
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = AccessibilityUtils.ContentDescriptions.SETTINGS_BUTTON
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null, // Handled by parent
                    modifier = Modifier.clearAndSetSemantics { }
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

/**
 * Accessible exercise completion indicator
 */
@Composable
fun AccessibleExerciseIndicator(
    exerciseName: String,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .accessibleClickable(
                contentDescription = "Exercise: $exerciseName. ${if (isCompleted) "Completed" else "Not completed"}. Tap to ${if (isCompleted) "mark as incomplete" else "mark as complete"}",
                onClick = onToggleComplete
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Exercise status indicator
        val statusIcon = if (isCompleted) "✓" else "○"
        val statusColor = if (isCompleted) 
            MaterialTheme.colorScheme.primary 
        else 
            MaterialTheme.colorScheme.onSurfaceVariant
            
        Text(
            text = statusIcon,
            style = MaterialTheme.typography.headlineSmall,
            color = statusColor,
            modifier = Modifier.clearAndSetSemantics { }
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .clearAndSetSemantics { }
        ) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = if (isCompleted) "Completed" else "Not completed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Accessible mood history item for lists
 */
@Composable
fun AccessibleMoodHistoryItem(
    emoji: String,
    moodScore: Int,
    note: String?,
    date: String,
    itemIndex: Int,
    totalItems: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .semantics {
                val noteText = if (note.isNullOrBlank()) "No note" else "Note: $note"
                contentDescription = "Mood entry $itemIndex of $totalItems. ${getMoodDescription(emoji, moodScore)} on $date. $noteText"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mood emoji
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.clearAndSetSemantics { }
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .clearAndSetSemantics { }
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.bodyLarge
            )
            if (!note.isNullOrBlank()) {
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Import the getMoodDescription function
private fun getMoodDescription(emoji: String, score: Int): String {
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