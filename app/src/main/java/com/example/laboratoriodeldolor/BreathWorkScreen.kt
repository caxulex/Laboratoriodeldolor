package com.example.laboratoriodeldolor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.Color

@Composable
fun BreathWorkScreen(viewModel: BreathWorkViewModel, onInstruction: (String) -> Unit = {}) {
    val recommended by viewModel.recommendedId.collectAsState()

    val exercises = listOf(
        "enamorado" to stringResource(id = R.string.breath_enamorado_title),
        "chilindrina" to stringResource(id = R.string.breath_chilindrina_title),
        "cuadrado" to stringResource(id = R.string.breath_cuadrado_title)
    )

    com.example.laboratoriodeldolor.ui.AppScaffold { _ ->
        Column(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp), verticalArrangement = Arrangement.Top) {
        Text(text = stringResource(id = R.string.breath_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.testTag("screen_title"))
        Spacer(modifier = Modifier.height(16.dp))

        for ((id, title) in exercises) {
            val isRecommended = id == recommended
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { onInstruction(id) },
                elevation = CardDefaults.cardElevation(defaultElevation = if (isRecommended) 12.dp else 6.dp),
                colors = CardDefaults.cardColors(containerColor = if (isRecommended) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, style = MaterialTheme.typography.titleMedium)
                        if (isRecommended) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = stringResource(id = R.string.breath_recommended_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = { onInstruction(id) }) {
                        Icon(imageVector = Icons.Filled.Home, contentDescription = stringResource(id = R.string.back_button), tint = if (isRecommended) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        }
        }
    }
}
