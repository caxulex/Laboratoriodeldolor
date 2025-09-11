package com.example.laboratoriodeldolor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun ExerciseHubScreen(
    onNavigateToFrontUpper: () -> Unit = {},
    onNavigateToBackUpper: () -> Unit = {},
    onNavigateToFrontMiddle: () -> Unit = {},
    onNavigateToBackMiddle: () -> Unit = {},
    onNavigateToFrontLower: () -> Unit = {},
    onNavigateToBackLower: () -> Unit = {},
    _onBack: () -> Unit = {}
) {
    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.exercise_hub_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToFrontUpper() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.front_upper_body_routine_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToBackUpper() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.back_upper_body_routine_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToFrontMiddle() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.front_middle_body_routine_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToBackMiddle() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.back_middle_body_routine_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToFrontLower() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.front_lower_body_routine_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToBackLower() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.back_lower_body_routine_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
