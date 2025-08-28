package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

/**
 * MoodChartScreen - displays a weekly or monthly mood score line chart.
 * Note: This implementation uses generic Compose drawing so it compiles without
 * adding an external chart dependency. Replace the charting block with a
 * third-party library (e.g., Vico) for production-grade charts if desired.
 */
@Composable
fun MoodChartScreen(moodDao: MoodDao, vm: MoodChartViewModel = viewModel(factory = MoodChartViewModelFactory(moodDao))) {
    val entries by vm.entries.collectAsState()
    val tf by vm.timeFrame.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top) {
    androidx.compose.material3.Text(text = stringResource(id = R.string.progress_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val weeklyLabel = stringResource(id = R.string.progress_tab_weekly)
            val monthlyLabel = stringResource(id = R.string.progress_tab_monthly)

            Button(onClick = { vm.setTimeFrame(MoodChartViewModel.TimeFrame.WEEK) }, colors = ButtonDefaults.buttonColors(containerColor = if (tf == MoodChartViewModel.TimeFrame.WEEK) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                androidx.compose.material3.Text(text = weeklyLabel)
            }
            Button(onClick = { vm.setTimeFrame(MoodChartViewModel.TimeFrame.MONTH) }, colors = ButtonDefaults.buttonColors(containerColor = if (tf == MoodChartViewModel.TimeFrame.MONTH) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                androidx.compose.material3.Text(text = monthlyLabel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            if (entries.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        androidx.compose.material3.Text(text = stringResource(id = R.string.no_graph_data_message))
                    }
            } else {
                // Simple lightweight line chart drawing: map entries to points and draw a polyline
                // This is intentionally minimal and robust; for advanced visuals replace with a
                // dedicated chart library (Vico, MPAndroidChart with Compose interop, etc.)
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val paddingX = 24f
                    val paddingY = 24f
                    val w = size.width - paddingX * 2
                    val h = size.height - paddingY * 2

                    // X positions evenly spaced across entries
                    val xs = entries.indices.map { i -> paddingX + (w * i / maxOf(1, entries.size - 1)) }
                    val scores = entries.map { it.moodScore.coerceIn(1,5) }
                    val minScore = 1f
                    val maxScore = 5f
                    val ys = scores.map { s -> paddingY + h * (1f - (s - minScore) / (maxScore - minScore)) }

                    // draw axes
                    drawLine(color = Color.LightGray, start = androidx.compose.ui.geometry.Offset(paddingX, paddingY + h), end = androidx.compose.ui.geometry.Offset(paddingX + w, paddingY + h), strokeWidth = 2f)

                    // draw polyline
                    for (i in 0 until xs.size - 1) {
                        drawLine(color = Color(0xFF4CAF50), start = androidx.compose.ui.geometry.Offset(xs[i], ys[i]), end = androidx.compose.ui.geometry.Offset(xs[i+1], ys[i+1]), strokeWidth = 6f)
                    }

                    // draw points
                    for (i in xs.indices) {
                        drawCircle(color = Color(0xFF388E3C), radius = 8f, center = androidx.compose.ui.geometry.Offset(xs[i], ys[i]))
                    }
                }
            }
        }
    }
}
