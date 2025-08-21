package com.example.laboratoriodeldolor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

/**
 * Reusable save button that plays a Lottie success animation on click.
 * Expects a `save_success.json` file to exist in res/raw.
 */
@Composable
fun LottieSaveButton(
    enabled: Boolean = true,
    onSave: () -> Unit
) {
    var state by remember { mutableStateOf(SaveState.Idle) }

    val composition = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.save_success))
    val progress by animateLottieCompositionAsState(
        composition.value,
        isPlaying = state == SaveState.Playing,
        iterations = 1,
        speed = 1f,
        restartOnPlay = false
    )

    LaunchedEffect(progress, state) {
        if (state == SaveState.Playing && progress >= 0.99f) {
            state = SaveState.Saved
        }
    }

    val bgColor = when {
        state == SaveState.Saved -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        enabled -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Surface(
        modifier = Modifier.size(120.dp, 48.dp),
        color = bgColor
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.clickable(enabled = enabled && state == SaveState.Idle) {
            // trigger save action and play animation
            onSave()
            state = SaveState.Playing
        }) {
            when (state) {
                SaveState.Idle -> Text(text = stringResource(id = R.string.save_pain_button))
                SaveState.Playing -> LottieAnimation(composition = composition.value, progress = { progress })
                SaveState.Saved -> Text(text = stringResource(id = R.string.saved_label))
            }
        }
    }
}

private enum class SaveState { Idle, Playing, Saved }
