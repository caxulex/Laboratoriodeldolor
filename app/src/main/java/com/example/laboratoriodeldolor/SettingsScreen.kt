package com.example.laboratoriodeldolor

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.material3.Switch
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

// small helper to obtain pluralized month labels
@Composable
private fun resourcesPlural(count: Int): String {
    val ctx = LocalContext.current
    return ctx.resources.getQuantityString(R.plurals.settings_retention_months, if (count == 1) 1 else 2, count)
}
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
    val gender by viewModel.gender.collectAsState()
    val retentionMonths by viewModel.retentionMonths.collectAsState()

    com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)
            .verticalScroll(rememberScrollState())) {
                Text(text = stringResource(id = R.string.settings_title), style = MaterialTheme.typography.headlineSmall, modifier = Modifier.testTag("screen_title"))

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

                Spacer(modifier = Modifier.height(16.dp))

                // Gender selector
                Text(text = stringResource(id = R.string.gender_label), modifier = Modifier.padding(top = 8.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { viewModel.setGenderMale(true) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (gender) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                    ) {
                        Text(text = stringResource(id = R.string.gender_male))
                    }
                    OutlinedButton(
                        onClick = { viewModel.setGenderMale(false) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = if (!gender) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                    ) {
                        Text(text = stringResource(id = R.string.gender_female))
                    }
                }

                // Dark mode toggle
                Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text(text = stringResource(id = R.string.dark_mode_label), modifier = Modifier.weight(1f))
                    Switch(checked = isDarkMode, onCheckedChange = { enabled -> scope.launch { viewModel.setDarkModeEnabled(enabled) } })
                }

                // Retention period for diary & pain records
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(id = R.string.settings_retention_title), modifier = Modifier.padding(top = 8.dp))
                Text(text = stringResource(id = R.string.settings_retention_summary, retentionMonths), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                Spacer(modifier = Modifier.height(8.dp))
                // Dropdown selector for retention months using stable DropdownMenu
                val options = listOf(1, 3, 6, 12)
                var expanded by remember { mutableStateOf(false) }
                val selectedLabel = if (retentionMonths >= 12) stringResource(id = R.string.settings_retention_option_year) else resourcesPlural(retentionMonths)

                val density = LocalDensity.current
                var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

                // Anchor the text field and menu inside a Box so the Popup positions relative to it
                Box(modifier = Modifier.fillMaxWidth().onGloballyPositioned { coords -> textFieldSize = coords.size }) {
                    androidx.compose.material3.OutlinedTextField(
                        value = selectedLabel,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = !expanded }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(with(density) { textFieldSize.width.toDp() }),
                        offset = DpOffset(x = 0.dp, y = with(density) { textFieldSize.height.toDp() })
                    ) {
                        options.forEach { months ->
                            DropdownMenuItem(text = { Text(text = if (months >= 12) stringResource(id = R.string.settings_retention_option_year) else resourcesPlural(months)) }, onClick = {
                                scope.launch { viewModel.setRetentionMonths(months) }
                                expanded = false
                            })
                        }
                    }
                }

                com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.about_title), onClick = { onNavigateToAbout() }, modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(48.dp))
            }
    }
}
