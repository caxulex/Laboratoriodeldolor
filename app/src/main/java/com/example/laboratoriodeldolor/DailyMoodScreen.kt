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
// Scaffold is provided by AppScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
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
    onNavigateToDiary: () -> Unit = {},
    onNavigateToPainChart: () -> Unit = {},
    onNavigateToTechniquesLibrary: () -> Unit = {},
    onNavigateToExerciseHub: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
        // Use the standard top app bar inside the scaffold surface
        androidx.compose.material3.TopAppBar(
            title = { Text(text = stringResource(id = R.string.app_title_cero_dolor), style = MaterialTheme.typography.headlineSmall) }
        )

        // Place content and a SnackbarHost in a Box so snackbars overlay the list and sit above the nav bar
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            // Make the main area scrollable and ensure it respects scaffold padding
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
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
                                    val streakText = LocalContext.current.resources.getQuantityString(R.plurals.streak_display, target, target)
                                    Text(text = "$streakText 🔥", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(4.dp))
                                }

                                // New CTA: "Ya los hice hoy" — updates streak immediately via ViewModel
                                val loggedToday by viewModel.loggedToday.collectAsState()
                                com.example.laboratoriodeldolor.ui.components.PrimaryButton(
                                    text = if (loggedToday) stringResource(id = R.string.streak_marked_today) else stringResource(id = R.string.streak_mark_button_today),
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.logExerciseCompleted()
                                        }
                                    }
                                )
                            }

                            Text(text = stringResource(id = R.string.streak_subtitle), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                // Quick mood register removed — mood is captured via the Check-in flow or the Diary screen

                // Secondary modules - presented compactly, cards act as single entry points (no duplicate navigation buttons inside)
                item {
                            Text(text = stringResource(id = R.string.actions_label), style = MaterialTheme.typography.titleSmall)
                }

                item {
                    // Static Actions: Pain Chart, Techniques Library, Exercise Hub
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToPainChart() }) {
                            Text(text = stringResource(id = R.string.view_pain_chart_button), modifier = Modifier.padding(16.dp))
                        }

                        Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToTechniquesLibrary() }) {
                            Text(text = stringResource(id = R.string.techniques_library_title), modifier = Modifier.padding(16.dp))
                        }

                        Card(modifier = Modifier.fillMaxWidth().clickable { onNavigateToExerciseHub() }) {
                            Text(text = stringResource(id = R.string.exercise_hub_title), modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                // Reserve space at bottom so content doesn't overlap with nav bars
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }

            // Snackbar host shown above content; keep it above navigation with a small bottom padding
            SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 72.dp))
        }

    }

}



