package com.example.laboratoriodeldolor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode

import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.clickable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
// use colors from MaterialTheme.colorScheme instead of hard-coded palette tokens
import com.example.laboratoriodeldolor.ui.components.MoodEmojiButton
import com.example.laboratoriodeldolor.ui.theme.Dimens


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun DailyMoodScreen(
    viewModel: MoodViewModel,
    onNavigateToPainTracker: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBreath: () -> Unit = {},
    onNavigateToDiary: () -> Unit = {}
) {
    // remember a coroutine scope for UI actions (used for snackbars and async DB ops)
    val coroutineScope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { innerPadding ->
        // Animated gradient background that shifts slowly; palette varies with local hour
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val baseColors = when (hour) {
            in 6..11 -> listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)) // morning warm
            in 12..17 -> listOf(Color(0xFFE3F2FD), Color(0xFFBBDEFB)) // afternoon cool
            in 18..20 -> listOf(Color(0xFFF3E5F5), Color(0xFFE1BEE7)) // evening soft
            else -> listOf(Color(0xFF0B1630), Color(0xFF071028)) // night deep navy
        }

        val infinite = rememberInfiniteTransition()
        val shift by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(tween(durationMillis = 20000, easing = LinearEasing), RepeatMode.Restart)
        )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = shift % size.width
                val endX = (shift + size.width) % size.width
                drawRect(
                    brush = Brush.linearGradient(colors = baseColors, start = Offset(startX, 0f), end = Offset(endX, size.height)),
                    size = size
                )
            }

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(top = 32.dp, start = 16.dp, end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(stringResource(id = R.string.mood_question), style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                // Exercise streak display
                val streakCount by viewModel.streak.collectAsState()
                val loggedToday by viewModel.loggedToday.collectAsState()
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .padding(horizontal = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    val gradient = Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary))
                    Box(modifier = Modifier.background(gradient)) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                AnimatedContent(targetState = streakCount, transitionSpec = { scaleIn(tween(300)).togetherWith(scaleOut(tween(200))) }) { target ->
                                    Text(text = stringResource(id = R.string.streak_display, target) + " 🔥", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(4.dp))
                                }

                                // Allow the user to re-log/update today's exercise even if already logged
                                // Capture the strings once in a composable context so they can be used
                                // from the coroutine without invoking Compose APIs inside the onClick lambda.
                                val exerciseMarkedMsg = stringResource(id = R.string.exercise_marked_snackbar)
                                val undoLabel = stringResource(id = R.string.undo_action)

                                com.example.laboratoriodeldolor.ui.components.SecondaryButton(
                                    text = if (loggedToday) stringResource(id = R.string.streak_update_button) else stringResource(id = R.string.streak_mark_button),
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.logExerciseCompleted()
                                            val result = snackbarHostState.showSnackbar(
                                                message = exerciseMarkedMsg,
                                                actionLabel = undoLabel,
                                                duration = androidx.compose.material3.SnackbarDuration.Short
                                            )

                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoLastExercise()
                                            }
                                        }
                                    }
                                )
                            }

                            Text(text = stringResource(id = R.string.streak_subtitle), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                // Emoji selector (compact, evenly spaced)
                val localSelectedEmoji = remember { mutableStateOf(viewModel.selectedEmoji) }
                val haptic = LocalHapticFeedback.current
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.spaceMedium, vertical = Dimens.spaceMedium), horizontalArrangement = Arrangement.Center) {
                        val options = MoodOptions.FIVE_LEVEL
                        for ((idx, mood) in options.withIndex()) {
                            val selected = localSelectedEmoji.value == mood
                            val desc = when (idx) {
                                0 -> stringResource(id = R.string.emoji_desc_very_bad)
                                1 -> stringResource(id = R.string.emoji_desc_bad)
                                2 -> stringResource(id = R.string.emoji_desc_neutral)
                                3 -> stringResource(id = R.string.emoji_desc_good)
                                else -> stringResource(id = R.string.emoji_desc_very_good)
                            }
                            MoodEmojiButton(emoji = mood, selected = selected, size = 64.dp, contentDesc = desc) {
                                localSelectedEmoji.value = mood
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }

                            if (idx < options.size - 1) Spacer(modifier = Modifier.width(14.dp))
                        }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceMedium))

                // Prioritized module cards
                val priority by viewModel.dashboardPriority.collectAsState()
                val modules = when (priority) {
                    MoodViewModel.DashboardPriority.PAIN -> listOf("pain", "breath", "diary")
                    MoodViewModel.DashboardPriority.BREATH -> listOf("breath", "diary", "pain")
                    else -> listOf("diary", "pain", "breath")
                }


                for (m in modules) {
                    when (m) {
                        "pain" -> Card(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { onNavigateToPainTracker() }
                        ) { Text(text = stringResource(id = R.string.pain_tracker_title), modifier = Modifier.padding(16.dp)) }
                        "breath" -> Card(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { onNavigateToBreath() }
                        ) { Text(text = stringResource(id = R.string.breath_title), modifier = Modifier.padding(16.dp)) }
                        "diary" -> Card(modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .clickable { onNavigateToDiary() }
                        ) { Text(text = stringResource(id = R.string.diary_title), modifier = Modifier.padding(16.dp)) }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spaceLarge))

                // Action row
                // Action row: primary = Registrar Dolor (filled), secondary = Diary (outlined)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.pain_tracker_button), onClick = { onNavigateToPainTracker() }, modifier = Modifier.weight(1f).height(Dimens.buttonHeight))

                    com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.diary_title), onClick = { onNavigateToDiary() }, modifier = Modifier.weight(1f).height(Dimens.buttonHeight))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Respiration guided button (secondary filled if prioritized elsewhere)
                com.example.laboratoriodeldolor.ui.components.PrimaryButton(
                    text = stringResource(id = R.string.breath_nav_button),
                    onClick = onNavigateToBreath,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

// MoodEntryItem removed (unused). See DailyMoodScreen.kt.bak1 for the original implementation.

