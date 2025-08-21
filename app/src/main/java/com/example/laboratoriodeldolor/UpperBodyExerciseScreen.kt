package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun UpperBodyExerciseScreen(onBack: () -> Unit = {}) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
            Text(text = stringResource(id = R.string.upper_body_routine_title), style = MaterialTheme.typography.headlineSmall)
            Text(text = stringResource(id = R.string.upper_body_routine_subtitle), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))

            val steps = listOf(
                Triple(R.string.upper_ex1_title, R.string.upper_ex1_desc, R.string.reps_10),
                Triple(R.string.upper_ex2_title, R.string.upper_ex2_desc, R.string.reps_10),
                Triple(R.string.upper_ex3_title, R.string.upper_ex3_desc, R.string.reps_30s)
            )

            LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
                itemsIndexed(steps) { index, item ->
                    ExerciseStepCard(stepNumber = index + 1, titleRes = item.first, descRes = item.second, repsTextRes = item.third)
                }
            }

            androidx.compose.material3.OutlinedButton(onClick = onBack, modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally).height(48.dp)) {
                Text(text = stringResource(id = R.string.back_button))
            }
        }
    }
}
