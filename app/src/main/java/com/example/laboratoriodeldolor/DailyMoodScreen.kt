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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.launch
import com.example.laboratoriodeldolor.ui.components.MoodEmojiButton
import com.example.laboratoriodeldolor.ui.theme.Dimens
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.ExperimentalMaterial3Api


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
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            // Place the title in the topBar slot so content doesn't overlap
            androidx.compose.material3.TopAppBar(
                title = { Text(text = stringResource(id = R.string.mood_question), style = MaterialTheme.typography.headlineSmall) }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background remains behind scaffold content
            com.example.laboratoriodeldolor.ui.GradientBackground()

            // Make the main area scrollable and ensure it respects scaffold padding
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top: prominent streak card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                val streakCount by viewModel.streak.collectAsState()
                                AnimatedContent(targetState = streakCount, transitionSpec = { scaleIn(tween(300)).togetherWith(scaleOut(tween(200))) }) { target ->
                                    Text(text = stringResource(id = R.string.streak_display, target) + " 🔥", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(4.dp))
                                }

                                // Keep action here to mark today's exercise (not navigation)
                                val loggedToday by viewModel.loggedToday.collectAsState()
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

                // Emoji selector — prominent
                item {
                    val localSelectedEmoji = remember { androidx.compose.runtime.mutableStateOf(viewModel.selectedEmoji) }
                    val haptic = LocalHapticFeedback.current
                    Card(shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = stringResource(id = R.string.checkin_mood_title), style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally)) {
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
                                }
                            }
                        }
                    }
                }

                // Secondary modules — presented compactly, cards act as single entry points (no duplicate navigation buttons inside)
                item {
                    Text(text = "Acciones", style = MaterialTheme.typography.titleSmall)
                }

                item {
                    // Compose a list of module entries with a single clickable card each
                    val priority by viewModel.dashboardPriority.collectAsState()
                    val modules = when (priority) {
                        MoodViewModel.DashboardPriority.PAIN -> listOf("pain", "breath", "diary")
                        MoodViewModel.DashboardPriority.BREATH -> listOf("breath", "diary", "pain")
                        else -> listOf("diary", "pain", "breath")
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (m in modules) {
                            when (m) {
                                "pain" -> Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToPainTracker() }
                                ) {
                                    Text(text = stringResource(id = R.string.pain_tracker_title), modifier = Modifier.padding(16.dp))
                                }
                                "breath" -> Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToBreath() }
                                ) {
                                    Text(text = stringResource(id = R.string.breath_title), modifier = Modifier.padding(16.dp))
                                }
                                "diary" -> Card(modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToDiary() }
                                ) {
                                    // This is the single content shortcut to Diary (uses same callback as bottom nav)
                                    Text(text = stringResource(id = R.string.diary_title), modifier = Modifier.padding(16.dp))
                                }
                            }
                        }
                    }
                }

                // Reserve space at bottom so content doesn't overlap with nav bars
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }
}


