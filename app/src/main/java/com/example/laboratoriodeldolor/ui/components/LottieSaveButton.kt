package com.example.laboratoriodeldolor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
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
import com.example.laboratoriodeldolor.R

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

    // Use a transparent Surface so the gradient shows; render a small rounded Box for the button's visual background
    Surface(
        modifier = Modifier.size(120.dp, 48.dp),
        color = Color.Transparent
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(120.dp, 48.dp)
                .clickable(enabled = enabled && state == SaveState.Idle) {
                    onSave()
                    state = SaveState.Playing
                }
                .background(color = bgColor, shape = RoundedCornerShape(8.dp))
        ) {
            when (state) {
                SaveState.Idle -> Text(text = stringResource(id = R.string.save_pain_button))
                SaveState.Playing -> LottieAnimation(composition = composition.value, progress = { progress })
                SaveState.Saved -> Text(text = stringResource(id = R.string.saved_label))
            }
        }
    }
}

private enum class SaveState { Idle, Playing, Saved }
