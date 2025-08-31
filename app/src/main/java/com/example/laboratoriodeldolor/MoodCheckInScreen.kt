package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.laboratoriodeldolor.ui.components.MoodEmojiButton
import com.example.laboratoriodeldolor.ui.components.LottieSaveButton
import com.example.laboratoriodeldolor.ui.theme.Dimens
import androidx.compose.ui.res.stringResource

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun MoodCheckInScreen(moodViewModel: MoodViewModel, onNext: () -> Unit, onSkip: () -> Unit, onSaved: (String) -> Unit = {}) {
    val emojiState = remember { mutableStateOf(moodViewModel.selectedEmoji) }
    var note by remember { mutableStateOf(moodViewModel.noteText) }
    val haptic = LocalHapticFeedback.current
    var visible by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(260)), exit = fadeOut(animationSpec = tween(260))) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
                Text(text = stringResource(id = R.string.checkin_mood_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("screen_title"))
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            // Five-level emoji selector: � 😟 😐 🙂 😄 (very bad -> very good)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                val options = MoodOptions.FIVE_LEVEL
                options.forEachIndexed { idx, e ->
                    val selected = emojiState.value == e
                    val desc = when (idx) {
                        0 -> stringResource(id = R.string.emoji_desc_very_bad)
                        1 -> stringResource(id = R.string.emoji_desc_bad)
                        2 -> stringResource(id = R.string.emoji_desc_neutral)
                        3 -> stringResource(id = R.string.emoji_desc_good)
                        else -> stringResource(id = R.string.emoji_desc_very_good)
                    }
                    MoodEmojiButton(emoji = e, selected = selected, size = 64.dp, contentDesc = desc, modifier = Modifier.testTag("moodEmoji_$idx")) {
                        // give lightweight haptic feedback and update selection
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        emojiState.value = e
                    }

                    if (idx < options.size - 1) Spacer(modifier = Modifier.width(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = note, onValueChange = { note = it }, modifier = Modifier.fillMaxWidth().height(200.dp), placeholder = { Text(text = stringResource(id = R.string.checkin_mood_hint)) })

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                LottieSaveButton(enabled = true, onSave = {
                    // save and animate transition before moving to next screen
                    moodViewModel.selectedEmoji = emojiState.value
                    moodViewModel.noteText = note
                    moodViewModel.onMoodSave()
                    // notify caller of saved emoji so it can adjust dashboard priority
                    onSaved(emojiState.value)
                    // trigger exit animation then navigate
                    visible = false
                    // wait for the exit animation to complete before calling onNext
                    coroutineScope.launch {
                        // slightly longer than fade duration to allow Lottie to play a bit
                        delay(360)
                        onNext()
                    }
                })
                Spacer(modifier = Modifier.width(Dimens.spaceSmall))
                com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.checkin_skip), onClick = {
                    visible = false
                    coroutineScope.launch {
                        delay(260)
                        onSkip()
                    }
                }, modifier = Modifier.height(Dimens.buttonHeight).testTag("checkin_skip"))
            }
        }
    }
    }
}
