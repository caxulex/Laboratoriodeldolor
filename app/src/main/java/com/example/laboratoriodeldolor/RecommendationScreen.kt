package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource

@Composable
fun RecommendationScreen(viewModel: RecommendationViewModel) {
    val recState by viewModel.recommendation.collectAsState()
    val rec = recState

    Column(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp), verticalArrangement = Arrangement.Top) {
    Text(text = stringResource(id = R.string.recommendation_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("screen_title"))
        Spacer(modifier = Modifier.height(16.dp))

    if (rec == null) {
            Card(modifier = Modifier.fillMaxWidth().height(180.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Text(text = stringResource(id = R.string.recommendation_no_data_title), style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = stringResource(id = R.string.recommendation_no_data_subtitle), style = MaterialTheme.typography.bodyMedium)
                }
            }
    } else {
            Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = rec.title, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = rec.description, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.recommendation_action_button), onClick = { /* TODO: open exercise/workshop details */ }, modifier = Modifier.height(48.dp))
                }
            }
        
            // Push action button to bottom when content is sparse
            Spacer(modifier = Modifier.weight(1f))

        }
    }
}
