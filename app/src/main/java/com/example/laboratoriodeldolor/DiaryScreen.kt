package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import com.example.laboratoriodeldolor.ui.GradientBackground
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.laboratoriodeldolor.ui.components.MoodEmojiButton
import com.example.laboratoriodeldolor.ui.components.LottieSaveButton
import com.example.laboratoriodeldolor.ui.theme.Dimens

@Suppress("UNUSED_PARAMETER")
@Composable
fun DiaryScreen(diaryViewModel: DiaryViewModel = viewModel(factory = DiaryViewModelFactory()), onBack: () -> Unit = {}) {
    var note by remember { mutableStateOf("") }
            var selectedEmoji by remember { mutableStateOf(MoodOptions.FIVE_LEVEL[2]) }

    val entries by diaryViewModel.entries.collectAsState(initial = emptyList())

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
            Text(text = stringResource(id = R.string.diary_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("screen_title"))
            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            // Five-level emoji selector
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    val options = MoodOptions.FIVE_LEVEL
                    options.forEachIndexed { idx, e ->
                    val isSelected = selectedEmoji == e
                    val desc = when (idx) {
                        0 -> stringResource(id = R.string.emoji_desc_very_bad)
                        1 -> stringResource(id = R.string.emoji_desc_bad)
                        2 -> stringResource(id = R.string.emoji_desc_neutral)
                        3 -> stringResource(id = R.string.emoji_desc_good)
                        else -> stringResource(id = R.string.emoji_desc_very_good)
                    }
                    MoodEmojiButton(emoji = e, selected = isSelected, size = 64.dp, contentDesc = desc) {
                        selectedEmoji = e
                    }
                    if (idx < options.size - 1) Spacer(modifier = Modifier.width(14.dp))
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spaceSmall))

        OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text(text = stringResource(id = R.string.diary_entry_hint)) },
                modifier = Modifier
                    .fillMaxWidth()
            .height(200.dp)
            )

            Spacer(modifier = Modifier.height(Dimens.spaceMedium))

            // Replace primary save Button with LottieSaveButton which plays success animation
            LottieSaveButton(enabled = note.isNotBlank(), onSave = {
                if (note.isNotBlank()) {
                    diaryViewModel.saveEntry(selectedEmoji, note)
                    note = ""
                    selectedEmoji = "😀"
                }
            })

            Spacer(modifier = Modifier.height(16.dp))

            // Past entries (showing MoodEntry records)
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(entries) { entry ->
                    DiaryEntryCard(entry = entry)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun DiaryEntryCard(entry: MoodEntry) {
    Card(modifier = Modifier.fillMaxWidth(), colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(12.dp)) {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            Text(text = sdf.format(Date(entry.timestamp)), style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = entry.emoji, style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = entry.note, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
