package com.example.laboratoriodeldolor

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
/**
 * Settings screen that allows the user to pick a reminder time.
 */
@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onNavigateToAbout: () -> Unit = {}) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val reminder by viewModel.reminderTime.collectAsState()
    val recurring by viewModel.recurring.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Scaffold { innerPadding ->
        Card(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
                Text(text = stringResource(id = R.string.settings_title), style = MaterialTheme.typography.headlineSmall)

                // primary action button
                com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.reminder_time_label) + ": ${reminder.first.toString().padStart(2,'0')}:${reminder.second.toString().padStart(2,'0')}", onClick = {
                    // Show a TimePickerDialog
                    val tp = TimePickerDialog(ctx, { _, hourOfDay, minute ->
                        // Save selection
                        scope.launch { viewModel.setReminderTime(hourOfDay, minute) }
                    }, reminder.first, reminder.second, true)
                    tp.show()
                }, modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(48.dp))

                // Recurring toggle
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.recurring_label), modifier = Modifier.weight(1f))
                    Switch(checked = recurring, onCheckedChange = { enabled -> scope.launch { viewModel.setRecurringEnabled(enabled) } })
                }

                // Dark mode toggle
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.dark_mode_label), modifier = Modifier.weight(1f))
                    Switch(checked = isDarkMode, onCheckedChange = { enabled -> scope.launch { viewModel.setDarkModeEnabled(enabled) } })
                }

                com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.about_title), onClick = { onNavigateToAbout() }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(48.dp))
            }
        }
    }
}
