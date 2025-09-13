package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun RecommendationScreen(viewModel: RecommendationViewModel, onOpenTechnique: (Long) -> Unit = {}, onOpenTechniquesLibrary: () -> Unit = {}) {
    val recState by viewModel.recommendation.collectAsState()
    val rec = recState
    val regionKeys by viewModel.regionKeys.collectAsState()
    val techniques by viewModel.techniques.collectAsState()

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
    LazyColumn(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {
            item {
                Text(text = stringResource(id = R.string.recommendation_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("screen_title"))
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (rec == null) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().height(180.dp), elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)) {
                        Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Text(text = stringResource(id = R.string.recommendation_no_data_title), style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = stringResource(id = R.string.recommendation_no_data_subtitle), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(4.dp), elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = stringResource(id = rec.titleResId), style = MaterialTheme.typography.titleLarge)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = stringResource(id = rec.descriptionResId), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (regionKeys.isNotEmpty()) {
                    item {
                        Text(text = stringResource(id = R.string.suggested_categories_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(regionKeys) { rk ->
                        androidx.compose.material3.AssistChip(onClick = { onOpenTechniquesLibrary() }, label = { Text(text = stringResource(id = regionKeyToStringRes(rk))) }, modifier = Modifier.padding(end = 8.dp, bottom = 8.dp))
                    }
                    item { Spacer(modifier = Modifier.height(12.dp)) }
                }

                if (techniques.isNotEmpty()) {
                    item {
                        Text(text = stringResource(id = R.string.suggested_techniques_title), style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    items(techniques) { t ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = t.title, style = MaterialTheme.typography.titleMedium)
                                if (t.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = t.description, style = MaterialTheme.typography.bodySmall)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                com.example.laboratoriodeldolor.ui.components.PrimaryButton(text = stringResource(id = R.string.view_technique_button), onClick = { onOpenTechnique(t.id) }, modifier = Modifier.height(44.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
