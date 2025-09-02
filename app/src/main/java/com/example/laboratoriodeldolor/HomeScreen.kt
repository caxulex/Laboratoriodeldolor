package com.example.laboratoriodeldolor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Menu

// Simple data class for region mapping
data class PainRegion(val id: Long, val key: String, val labelRes: Int)

val DEFAULT_REGIONS = listOf(
    PainRegion(0L, "face_head", R.string.region_face_head),
    PainRegion(1L, "neck_shoulders", R.string.region_neck_shoulders),
    PainRegion(2L, "upper_back", R.string.region_upper_back),
    PainRegion(3L, "abdomen_pelvis", R.string.region_mid_back_stomach),
    PainRegion(4L, "lower_back", R.string.region_lower_back),
    PainRegion(5L, "arms_hands", R.string.region_fingers_wrist_forearm),
    PainRegion(6L, "legs_feet", R.string.lower_body_routine_title),
    PainRegion(7L, "legs_feet", R.string.region_ankles_feet_toes)
)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(routineDao: RoutineDao, onOpenPainRegion: (Long) -> Unit, onOpenPainTracker: () -> Unit, onOpenTechniques: () -> Unit = {}, onOpenSettings: () -> Unit = {}) {
    // Show a grid of regions. The mapping from region -> routineId is simplified: use the first routine that matches bodyRegion if present.
    val routines by routineDao.getAll().collectAsState(initial = emptyList())

    var menuExpanded by remember { mutableStateOf(false) }

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(title = { Text(text = stringResource(id = R.string.home_title)) }, actions = {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Filled.MoreVert, contentDescription = stringResource(id = R.string.more_options))
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    // Techniques moved to a prominent card on the Home screen.
                    DropdownMenuItem(text = { Text(text = stringResource(id = R.string.settings_title)) }, onClick = { menuExpanded = false; onOpenSettings() })
                }
            })

            Column(modifier = Modifier.padding(12.dp)) {
                Spacer(modifier = Modifier.height(8.dp))

                com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.home_register_pain), onClick = { onOpenPainTracker() })

                Spacer(modifier = Modifier.height(12.dp))

                // Prominent Techniques card added to Home screen
                androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onOpenTechniques() }) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = stringResource(id = R.string.techniques_library_title), style = MaterialTheme.typography.titleMedium)
                        Text(text = stringResource(id = R.string.techniques_library_subtitle), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                    }
                }

                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxWidth()) {
                    items(DEFAULT_REGIONS) { region ->
                        Card(onClick = {
                            // Find routine matching this region key
                            val match = routines.firstOrNull { it.bodyRegion == region.key }
                            if (match != null) {
                                onOpenPainRegion(match.id)
                            }
                        }, modifier = Modifier.padding(8.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = stringResource(id = region.labelRes), style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
