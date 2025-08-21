package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource

@Composable
fun PainCheckInScreen(painViewModel: PainTrackerViewModel, onFinish: () -> Unit, onSkip: () -> Unit) {
    val scope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
            Text(text = stringResource(id = R.string.checkin_pain_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            // Reuse the existing PainTrackerCanvas behavior: show the canvas and allow toggling
            // For simplicity, we instruct users to tap the body map to add points (PainTrackerScreen has this logic).
            Text(text = stringResource(id = R.string.checkin_pain_instruction))
            Spacer(modifier = Modifier.height(8.dp))

            // Show a miniature preview of the body map by reusing the PainTrackerScreen composable but without navigation
            Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                PainTrackerScreen(viewModel = painViewModel)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(onClick = {
                    scope.launch { painViewModel.savePainPoints() }
                    onFinish()
                }, modifier = Modifier.height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text(text = stringResource(id = R.string.checkin_finish), color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = { onSkip() }, modifier = Modifier.height(48.dp)) {
                    Text(text = stringResource(id = R.string.checkin_skip))
                }
            }
        }
    }
}
