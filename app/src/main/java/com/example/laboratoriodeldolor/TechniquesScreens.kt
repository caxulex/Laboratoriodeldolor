package com.example.laboratoriodeldolor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.Switch
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text as MText
import androidx.compose.material3.Button

@Composable
fun TechniquesLibraryScreen(techniqueDao: TechniqueDao, onTechniqueSelected: (Long) -> Unit) {
    val techniques by techniqueDao.getAll().collectAsState(initial = emptyList())

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        LazyColumn(modifier = Modifier.padding(12.dp)) {
            items(techniques) { t ->
                androidx.compose.material3.Card(modifier = Modifier.padding(vertical = 6.dp).clickable { onTechniqueSelected(t.id) }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = t.title, style = MaterialTheme.typography.titleMedium)
                        Text(text = t.description, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun TechniqueDetailScreen(techniqueId: Long, techniqueDao: TechniqueDao, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val techniqueState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Technique?>(null) }
    androidx.compose.runtime.LaunchedEffect(techniqueId) {
        scope.launch { techniqueState.value = techniqueDao.getById(techniqueId) }
    }

    val technique = techniqueState.value
    val context = LocalContext.current

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        // Use a scrollable column for long descriptions
        Column(modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())) {
            if (technique == null) {
                Text(text = stringResource(id = R.string.error_load_data))
            } else {
                Text(text = technique.title, style = MaterialTheme.typography.titleLarge)

                // Ensure description is readable and wrapped; allow multiple paragraphs if needed
                Text(text = technique.description, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 12.dp))

                // If technique has a video URL, show a clear button to open it
                val video = technique.videoUrl
                if (!video.isNullOrBlank()) {
                    Card(modifier = Modifier.padding(top = 16.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = stringResource(id = R.string.technique_play_video), style = MaterialTheme.typography.titleMedium)
                            Text(text = video, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
                            androidx.compose.material3.Button(onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video))
                                    // Use FLAG_ACTIVITY_NEW_TASK when launching from non-activity context
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (t: Throwable) {
                                    t.printStackTrace()
                                }
                            }, modifier = Modifier.padding(top = 8.dp)) {
                                Text(text = "Ver Video")
                            }
                        }
                    }
                }
            }

            com.example.laboratoriodeldolor.ui.components.SecondaryButton(text = stringResource(id = R.string.back_button), onClick = onBack, modifier = Modifier.padding(top = 20.dp))
        }
    }
}

@Composable
fun PainRegionScreen(routineId: Long, routineDao: RoutineDao, techniqueDao: TechniqueDao, onNavigateToTechnique: (Long) -> Unit) {
    val routineWithSteps by routineDao.getWithSteps(routineId).collectAsState(initial = null)

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        Column(modifier = Modifier.padding(16.dp)) {
            if (routineWithSteps == null) {
                Text(text = stringResource(id = R.string.error_load_data))
            } else {
                // routineWithSteps is non-null in this branch; capture to local non-null val
                // to avoid using the unsafe !! operator.
                val rws = routineWithSteps ?: return@Column
                Text(text = rws.routine.title, style = MaterialTheme.typography.titleLarge)
                val routineSummary = rws.routine.summary
                if (!routineSummary.isNullOrEmpty()) {
                    Text(text = routineSummary, style = MaterialTheme.typography.bodyMedium)
                }

                // Show mirror technique UI for neck/shoulders region
                if (rws.routine.bodyRegion == "neck_shoulders") {
                    var mirrorEnabled by remember { mutableStateOf(false) }
                    val mirrorLabel = stringResource(id = R.string.mirror_technique_label)
                    val mirrorSwitchDesc = stringResource(id = R.string.mirror_technique_switch_desc)
                    val mirrorInstruction = stringResource(id = R.string.mirror_technique_instruction)
                    Card(modifier = Modifier.padding(vertical = 8.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(text = mirrorLabel, style = MaterialTheme.typography.titleMedium)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            // Info icon opens a dialog explaining the mirror technique
                                            var showMirrorInfo by remember { mutableStateOf(false) }
                                            IconButton(onClick = { showMirrorInfo = true }) {
                                                Icon(imageVector = Icons.Default.Info, contentDescription = stringResource(id = R.string.mirror_technique_label))
                                            }
                                            if (showMirrorInfo) {
                                                AlertDialog(onDismissRequest = { showMirrorInfo = false }, confirmButton = {
                                                    Button(onClick = { showMirrorInfo = false }) { MText(text = stringResource(id = R.string.back_button)) }
                                                }, text = {
                                                    MText(text = stringResource(id = R.string.mirror_technique_instruction))
                                                })
                                            }
                                        }

                                        Switch(
                                            checked = mirrorEnabled,
                                            onCheckedChange = { mirrorEnabled = it },
                                            modifier = Modifier.semantics { contentDescription = mirrorSwitchDesc }
                                        )
                                    }

                            AnimatedVisibility(visible = mirrorEnabled) {
                                Text(text = mirrorInstruction, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
                            }
                        }
                    }
                }

                for (step in rws.steps) {
                    Card(modifier = Modifier.padding(vertical = 6.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Numbered step label
                            Text(text = "${stringResource(id = R.string.step_label)} ${step.stepOrder}", style = MaterialTheme.typography.titleMedium)
                            Text(text = step.description, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))

                            // If this step references a technique, load its title and render as a clickable link
                            val techniqueIdLocal = step.techniqueId
                            if (techniqueIdLocal != null) {
                                val scope = rememberCoroutineScope()
                                val techniqueTitle = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
                                androidx.compose.runtime.LaunchedEffect(techniqueIdLocal) {
                                    scope.launch {
                                        val t = techniqueDao.getById(techniqueIdLocal)
                                        techniqueTitle.value = t?.title
                                    }
                                }

                                val label = techniqueTitle.value ?: stringResource(id = R.string.technique_detail_title)
                                Text(text = label, modifier = Modifier.padding(top = 8.dp).clickable { onNavigateToTechnique(techniqueIdLocal) }, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutinesListScreen(routineDao: RoutineDao, onRoutineSelected: (Long) -> Unit) {
    val routines by routineDao.getAll().collectAsState(initial = emptyList())

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        LazyColumn(modifier = Modifier.padding(12.dp)) {
            items(routines) { r ->
                androidx.compose.material3.Card(modifier = Modifier.padding(vertical = 6.dp).clickable { onRoutineSelected(r.id) }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = r.title, style = MaterialTheme.typography.titleMedium)
                        val summary = r.summary
                        if (!summary.isNullOrEmpty()) {
                            Text(text = summary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutinesListScreen(viewModel: RoutinesListViewModel, onRoutineSelected: (Long) -> Unit) {
    val routines by viewModel.routines.collectAsState()

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        LazyColumn(modifier = Modifier.padding(12.dp)) {
            items(routines) { r ->
                androidx.compose.material3.Card(modifier = Modifier.padding(vertical = 6.dp).clickable { onRoutineSelected(r.id) }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = r.title, style = MaterialTheme.typography.titleMedium)
                        val summary2 = r.summary
                        if (!summary2.isNullOrEmpty()) {
                            Text(text = summary2, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
