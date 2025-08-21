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
fun ExerciseHubScreen(onNavigateToUpper: () -> Unit = {}, onNavigateToMiddle: () -> Unit = {}, onNavigateToLower: () -> Unit = {}, onBack: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(id = R.string.exercise_hub_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToUpper() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.upper_body_exercises_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToMiddle() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.middle_body_exercises_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }

            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable { onNavigateToLower() }, elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Text(text = stringResource(id = R.string.lower_body_exercises_title), modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
