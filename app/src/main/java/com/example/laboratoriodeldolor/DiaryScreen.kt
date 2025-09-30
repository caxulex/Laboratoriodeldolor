package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.laboratoriodeldolor.ui.components.MoodEmojiButton
import com.example.laboratoriodeldolor.ui.components.LottieSaveButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * DiaryScreen - A detailed journaling interface for mood tracking and note-taking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: DiaryViewModel,
    onBack: () -> Unit = {}
) {
    val entries by viewModel.entries.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedEmoji by remember { mutableStateOf(MoodOptions.FIVE_LEVEL[2]) } // neutral
    var noteText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.diary_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add entry")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { scaffoldPadding ->
            
            if (entries.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.diary_empty_state),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        FilledTonalButton(
                            onClick = { showAddDialog = true }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add First Entry")
                        }
                    }
                }
            } else {
                // Show diary entries
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(scaffoldPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(entries) { entry ->
                        DiaryEntryCard(entry = entry)
                    }
                }
            }

            // Add new entry dialog
            if (showAddDialog) {
                AlertDialog(
                    onDismissRequest = { showAddDialog = false },
                    title = { Text(stringResource(R.string.diary_mood_prompt)) },
                    text = {
                        Column {
                            // Mood selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MoodOptions.FIVE_LEVEL.forEach { emoji ->
                                    MoodEmojiButton(
                                        emoji = emoji,
                                        selected = selectedEmoji == emoji,
                                        onClick = { selectedEmoji = emoji }
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Note input
                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Journal Entry") },
                                placeholder = { Text(stringResource(R.string.diary_entry_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 3,
                                maxLines = 6
                            )
                        }
                    },
                    confirmButton = {
                        LottieSaveButton(
                            enabled = noteText.isNotBlank(),
                            onSave = {
                                scope.launch {
                                    viewModel.saveEntry(selectedEmoji, noteText)
                                    showAddDialog = false
                                    noteText = ""
                                    selectedEmoji = MoodOptions.FIVE_LEVEL[2]
                                }
                            }
                        )
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DiaryEntryCard(entry: MoodEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.emoji,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date(entry.timestamp)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = SimpleDateFormat("HH:mm", Locale.getDefault())
                                .format(Date(entry.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            if (entry.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = entry.note,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}