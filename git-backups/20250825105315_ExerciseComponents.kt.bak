package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable card representing a single exercise step.
 * - titleRes and descRes are string resource ids
 * - repsTextRes is optional string resource id for repetitions/timer (e.g. "10 reps" or "30 s")
 */
@Composable
fun ExerciseStepCard(
    stepNumber: Int,
    titleRes: Int,
    descRes: Int,
    repsTextRes: Int? = null
) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 6.dp)) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {

            // Placeholder visual: simple emoji; replace with Image(painterResource(...)) when you add drawables.
            Text(text = "🏋️", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.size(64.dp))

            Spacer(modifier = Modifier.padding(horizontal = 8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${stringResource(id = R.string.step_label)} $stepNumber: ${stringResource(id = titleRes)}",
                    style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = stringResource(id = descRes), style = MaterialTheme.typography.bodyMedium)
            }

            // Optional reps / timer column
            repsTextRes?.let { res ->
                Text(text = stringResource(id = res), style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}
