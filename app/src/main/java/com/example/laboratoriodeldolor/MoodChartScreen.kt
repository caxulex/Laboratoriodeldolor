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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.Instant
import java.time.LocalDate

/**
 * MoodChartScreen - displays a weekly or monthly mood score line chart.
 * Note: This implementation uses generic Compose drawing so it compiles without
 * adding an external chart dependency. Replace the charting block with a
 * third-party library (e.g., Vico) for production-grade charts if desired.
 */
@Composable
fun MoodProgressScreen(moodDao: MoodDao) {
    val allEntries by moodDao.getAllEntries().collectAsState(initial = emptyList())
    // true = weekly, false = monthly
    val (isWeekly, setIsWeekly) = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }

    // Filter entries according to selected timeframe
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val cutoff = if (isWeekly) today.minusDays(6) else today.minusDays(29)

    val entries = allEntries.filter { e ->
        val d = Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDate()
        !d.isBefore(cutoff)
    }.sortedBy { it.timestamp }

    val chartDesc = stringResource(id = R.string.progress_chart_desc)
    val dateFormatPattern = stringResource(id = R.string.date_format_short)

    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top) {
    androidx.compose.material3.Text(text = stringResource(id = R.string.progress_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val weeklyLabel = stringResource(id = R.string.progress_tab_weekly)
            val monthlyLabel = stringResource(id = R.string.progress_tab_monthly)

            Button(onClick = { setIsWeekly(true) }, colors = ButtonDefaults.buttonColors(containerColor = if (isWeekly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                androidx.compose.material3.Text(text = weeklyLabel)
            }
            Button(onClick = { setIsWeekly(false) }, colors = ButtonDefaults.buttonColors(containerColor = if (!isWeekly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                androidx.compose.material3.Text(text = monthlyLabel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

    // Add accessibility description for the chart area
    Card(modifier = Modifier.fillMaxWidth().height(320.dp).semantics { contentDescription = chartDesc }) {
            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Text(text = stringResource(id = R.string.no_graph_data_message))
                }
            } else {
                // Simple lightweight line chart drawing: map entries to points and draw a polyline
                // This is intentionally minimal and robust; for advanced visuals replace the charting
                // block with a dedicated chart library in a separate, controlled migration.
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val paddingX = 24f
                    val paddingY = 24f
                    val w = size.width - paddingX * 2
                    val h = size.height - paddingY * 2

                    // Preprocess entries: dedupe per LocalDate (keep last entry of the day) and compute daily scores
                    val zone = ZoneId.systemDefault()
                    val byDay = entries
                        .map { e -> Pair(Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDate(), e.moodScore.coerceIn(1,5)) }
                        .groupBy({ it.first }, { it.second })
                        .toSortedMap()
                        .mapValues { (_, scoresForDay) -> scoresForDay.last() }

                    val dates = byDay.keys.toList()
                    val scores = byDay.values.toList()

                    // Apply simple moving average smoothing (window = 3)
                    fun smooth(input: List<Int>): List<Float> {
                        if (input.size <= 2) return input.map { it.toFloat() }
                        return input.indices.map { i ->
                            val window = (i-1..i+1).mapNotNull { idx -> input.getOrNull(idx) }
                            window.average().toFloat()
                        }
                    }

                    val smoothScores = smooth(scores)
                    // X positions evenly spaced across days
                    val xs = smoothScores.indices.map { i -> paddingX + (w * i / maxOf(1, smoothScores.size - 1)) }
                    val minScore = 1f
                    val maxScore = 5f
                    val ys = smoothScores.map { s -> paddingY + h * (1f - (s - minScore) / (maxScore - minScore)) }

                    // draw axes using theme onSurface with low alpha
                    drawLine(color = colorScheme.onSurface.copy(alpha = 0.12f), start = androidx.compose.ui.geometry.Offset(paddingX, paddingY + h), end = androidx.compose.ui.geometry.Offset(paddingX + w, paddingY + h), strokeWidth = 2f)

                    // draw polyline using theme primary color
                    for (i in 0 until xs.size - 1) {
                        drawLine(color = colorScheme.primary, start = androidx.compose.ui.geometry.Offset(xs[i], ys[i]), end = androidx.compose.ui.geometry.Offset(xs[i+1], ys[i+1]), strokeWidth = 6f)
                    }

                    // draw points using theme secondary color
                    for (i in xs.indices) {
                        drawCircle(color = colorScheme.secondary, radius = 8f, center = androidx.compose.ui.geometry.Offset(xs[i], ys[i]))
                    }

                    // draw x-axis labels (dates)
                    val formatter = DateTimeFormatter.ofPattern(dateFormatPattern)
                    for (i in dates.indices) {
                        val label = dates.getOrNull(i)?.format(formatter) ?: ""
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(label, xs.getOrNull(i) ?: 0f, paddingY + h + 18f, android.graphics.Paint().apply { textSize = 24f; color = colorScheme.onSurface.copy(alpha = 0.7f).toArgb() })
                        }
                    }
                }
            }
        }
    }
}
