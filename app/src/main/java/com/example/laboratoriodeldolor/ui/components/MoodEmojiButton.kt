package com.example.laboratoriodeldolor.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment

@Composable
fun MoodEmojiButton(
    emoji: String,
    selected: Boolean,
    size: Dp = 64.dp,
    contentDesc: String? = null,
    onClick: () -> Unit
) {
    var pressedScale by remember { mutableStateOf(1f) }
    val scaleAnim by animateFloatAsState(targetValue = pressedScale)

    // Ensure minimum tappable area (>= 48dp). Use 56dp as comfortable target.
    val touchSize = if (size < 48.dp) 56.dp else size
    val modifier = Modifier
        .size(touchSize)
        .scale(scaleAnim)

    if (selected) {
        Box(
            modifier = modifier
                .semantics { if (!contentDesc.isNullOrEmpty()) this.contentDescription = contentDesc },
            contentAlignment = Alignment.Center
        ) {
            // Use themed primary colors for selected state
            PrimaryButton(
                text = emoji,
                onClick = {
                    pressedScale = 1.12f
                    onClick()
                    pressedScale = 1f
                },
                modifier = Modifier
                    .size(touchSize)
                    .shadow(elevation = 6.dp, shape = CircleShape),
                height = touchSize,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                textStyle = MaterialTheme.typography.labelLarge
            )
        }
    } else {
        Box(
            modifier = modifier
                .semantics { if (!contentDesc.isNullOrEmpty()) this.contentDescription = contentDesc },
            contentAlignment = Alignment.Center
        ) {
            // Use secondary/neutral styling for unselected state
            SecondaryButton(
                text = emoji,
                onClick = {
                    pressedScale = 1.08f
                    onClick()
                    pressedScale = 1f
                },
                modifier = Modifier.size(touchSize),
                height = touchSize,
                textStyle = MaterialTheme.typography.labelLarge
            )
        }
    }
}
