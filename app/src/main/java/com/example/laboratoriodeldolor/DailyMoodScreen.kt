package com.example.laboratoriodeldolor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Icon
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/** Redesigned DailyMoodScreen per request (clean, fixed braces) */
@Composable
fun DailyMoodScreen(
    viewModel: MoodViewModel,
    onNavigateToPainTracker: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToBreath: () -> Unit = {},
    onNavigateToDiary: () -> Unit = {},
    onOpenPainChart: () -> Unit = {},
    onOpenTechniquesLibrary: () -> Unit = {},
    onOpenExerciseHub: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Gradient header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    // Show title and streak text at left, fire indicator at right when in a streak
                    val streakCount by viewModel.streak.collectAsState()
                    val streakText = LocalContext.current.resources.getQuantityString(R.plurals.streak_display, streakCount, streakCount)
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(text = stringResource(id = R.string.appbar_title), color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(text = streakText, color = Color.White.copy(alpha = 0.95f), style = MaterialTheme.typography.bodyMedium)
                        }

                        // Fire indicator sizing rules (assumption: show when streakCount > 0)
                        if (streakCount > 0) {
                            val fireSize = when {
                                streakCount == 1 -> 16.dp
                                streakCount == 2 -> 24.dp
                                streakCount == 3 -> 36.dp
                                else -> 56.dp
                            }
                            Icon(
                                imageVector = Icons.Filled.Whatshot,
                                contentDescription = stringResource(id = R.string.streak_subtitle),
                                tint = Color(0xFFFF5722),
                                modifier = Modifier.size(fireSize)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                Card(modifier = Modifier.fillMaxWidth().height(140.dp), elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)) {
                    Column(modifier = Modifier.padding(12.dp).fillMaxWidth()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            val loggedToday by viewModel.loggedToday.collectAsState()
                            val streakCount by viewModel.streak.collectAsState()
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                com.example.laboratoriodeldolor.ui.components.SecondaryButton(
                                    text = if (loggedToday) stringResource(id = R.string.streak_done_button) else stringResource(id = R.string.streak_mark_button),
                                    onClick = { coroutineScope.launch { viewModel.logExerciseCompleted() } }
                                )

                                // small gap between button and fire icon
                                Spacer(modifier = Modifier.width(8.dp))

                                if (streakCount > 0) {
                                    val fireSize = when {
                                        streakCount == 1 -> 12.dp
                                        streakCount == 2 -> 18.dp
                                        streakCount == 3 -> 28.dp
                                        else -> 40.dp
                                    }
                                    Icon(imageVector = Icons.Filled.Whatshot, contentDescription = stringResource(id = R.string.streak_subtitle), tint = Color(0xFFFF7043), modifier = Modifier.size(fireSize))
                                }
                            }
                        }

                        Text(text = stringResource(id = R.string.streak_subtitle), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }

            item {
                Text(text = stringResource(id = R.string.actions_label), style = MaterialTheme.typography.titleSmall)
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Direct action button for Techniques Library (Biblioteca de Maniobras)
                    com.example.laboratoriodeldolor.ui.components.PrimaryButton(
                        text = stringResource(id = R.string.techniques_library_title),
                        onClick = { onOpenTechniquesLibrary() }
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenPainChart() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(text = stringResource(id = R.string.view_pain_chart_button), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToDiary() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(text = stringResource(id = R.string.open_diary_button), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onNavigateToBreath() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(text = stringResource(id = R.string.breath_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenTechniquesLibrary() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(text = stringResource(id = R.string.techniques_library_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onOpenExerciseHub() },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(text = stringResource(id = R.string.exercise_hub_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(64.dp)) }
        }
    }
}



