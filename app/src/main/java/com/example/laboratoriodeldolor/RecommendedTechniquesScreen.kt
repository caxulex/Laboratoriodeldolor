package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

/**
 * Screen that displays recommended technique routines after pain logging.
 * Receives a list of routine names (localized strings) and lets the user tap to navigate.
 */
@Composable
fun RecommendedTechniquesScreen(
    routines: List<String>,
    onSelectRoutine: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = stringResource(id = R.string.recommendation_title), style = MaterialTheme.typography.titleLarge)
        LazyColumn(modifier = Modifier.padding(top = 12.dp)) {
            items(routines) { r ->
                Card(modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                ) {
                    Button(onClick = { onSelectRoutine(r) }, modifier = Modifier.fillMaxWidth()) {
                        Text(text = r)
                    }
                }
            }
        }
        // Back button
        Button(onClick = onBack, modifier = Modifier.padding(top = 12.dp)) {
            Text(text = stringResource(id = R.string.back_button))
        }
    }
}
