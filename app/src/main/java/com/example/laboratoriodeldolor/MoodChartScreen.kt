package com.example.laboratoriodeldolor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import android.view.ViewGroup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laboratoriodeldolor.PainPointDao
import com.example.laboratoriodeldolor.PainChartViewModel
import com.example.laboratoriodeldolor.PainChartViewModelFactory
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
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
fun MoodProgressScreen(moodDao: MoodDao, painDao: PainPointDao, onOpenPainChart: () -> Unit) {
    val allEntries by moodDao.getAllEntries().collectAsState(initial = emptyList())
    // true = weekly, false = monthly (kept for UI state)
    val (isWeekly, setIsWeekly) = remember { androidx.compose.runtime.mutableStateOf(true) }

    // Primary period selector (days): keeps the UI in sync (7/30/90)
    val (periodDays, setPeriodDays) = remember { androidx.compose.runtime.mutableStateOf(7) }

    // Filter entries according to selected timeframe (driven by periodDays)
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val cutoff = remember(periodDays) { today.minusDays((periodDays - 1).toLong()) }

    val entries = remember(allEntries, cutoff) {
        allEntries.filter { e ->
            val d = Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDate()
            !d.isBefore(cutoff)
        }.sortedBy { it.timestamp }
    }

    val chartDesc = stringResource(id = R.string.progress_chart_desc)
    val dateFormatPattern = stringResource(id = R.string.date_format_short)

    val colorScheme = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Top) {
        Text(text = stringResource(id = R.string.progress_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val weeklyLabel = stringResource(id = R.string.progress_tab_weekly)
            val monthlyLabel = stringResource(id = R.string.progress_tab_monthly)

            Button(onClick = { setIsWeekly(true); setPeriodDays(7) }, colors = ButtonDefaults.buttonColors(containerColor = if (isWeekly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                Text(text = weeklyLabel)
            }
            Button(onClick = { setIsWeekly(false); setPeriodDays(30) }, colors = ButtonDefaults.buttonColors(containerColor = if (!isWeekly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)) {
                Text(text = monthlyLabel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to open the Pain Chart (evolución de la intensidad del dolor)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(onClick = onOpenPainChart) {
                Text(text = stringResource(id = R.string.view_pain_chart_button))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        // Mood flow chart: selectable windows (7, 30, 90 days)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val activeColor = MaterialTheme.colorScheme.primary
            Button(onClick = { setPeriodDays(7) }, colors = ButtonDefaults.buttonColors(containerColor = if (periodDays == 7) activeColor else MaterialTheme.colorScheme.surface)) {
                Text(text = "7d")
            }
            Button(onClick = { setPeriodDays(30) }, colors = ButtonDefaults.buttonColors(containerColor = if (periodDays == 30) activeColor else MaterialTheme.colorScheme.surface)) {
                Text(text = "30d")
            }
            Button(onClick = { setPeriodDays(90) }, colors = ButtonDefaults.buttonColors(containerColor = if (periodDays == 90) activeColor else MaterialTheme.colorScheme.surface)) {
                Text(text = "90d")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Simple helper: color per score 1..5
        fun colorForScore(s: Int): androidx.compose.ui.graphics.Color = when (s) {
            1 -> androidx.compose.ui.graphics.Color(0xFFB00020)
            2 -> androidx.compose.ui.graphics.Color(0xFFFF7043)
            3 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
            4 -> androidx.compose.ui.graphics.Color(0xFF8BC34A)
            else -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        }

        // Mood flow chart: draw entries as a connected flow (like pain chart)
        Card(modifier = Modifier.fillMaxWidth().height(320.dp).semantics { contentDescription = chartDesc }) {
            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.no_graph_data_message))
                }
            } else {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val paddingX = 24f
                    val paddingY = 24f
                    val w = size.width - paddingX * 2
                    val h = size.height - paddingY * 2

                    val n = entries.size
                    if (n <= 0) return@Canvas

                    val xs = (0 until n).map { i -> paddingX + (w * (i.toFloat() / (n - 1).coerceAtLeast(1))) }
                    fun yForScore(s: Float): Float {
                        val minScore = 1f
                        val maxScore = 5f
                        val frac = 1f - (s - minScore) / (maxScore - minScore)
                        return paddingY + h * frac
                    }

                    // Draw baseline
                    drawLine(color = colorScheme.onSurface.copy(alpha = 0.12f), start = androidx.compose.ui.geometry.Offset(paddingX, paddingY + h), end = androidx.compose.ui.geometry.Offset(paddingX + w, paddingY + h), strokeWidth = 2f)

                    val scores = entries.map { it.moodScore.coerceIn(1,5).toFloat() }

                    // Draw connecting lines
                    for (i in 0 until n - 1) {
                        val s1 = scores[i]
                        val s2 = scores[i + 1]
                        drawLine(color = colorScheme.primary, start = androidx.compose.ui.geometry.Offset(xs[i], yForScore(s1)), end = androidx.compose.ui.geometry.Offset(xs[i + 1], yForScore(s2)), strokeWidth = 4f)
                    }

                    // Draw points
                    for (i in 0 until n) {
                        val s = scores[i]
                        val x = xs[i]
                        val rounded = s.toInt().coerceIn(1,5)
                        drawCircle(color = colorForScore(rounded), radius = 8f, center = androidx.compose.ui.geometry.Offset(x, yForScore(s)))
                    }

                    // X-axis labels: show a label every ~max(1, n/6) points
                    val step = (n / 6).coerceAtLeast(1)
                    val formatter = DateTimeFormatter.ofPattern(dateFormatPattern)
                    for (i in 0 until n step step) {
                        val label = Instant.ofEpochMilli(entries[i].timestamp).atZone(zone).toLocalDate().format(formatter)
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(label, xs[i], paddingY + h + 18f, android.graphics.Paint().apply { textSize = 20f; color = colorScheme.onSurface.copy(alpha = 0.7f).toArgb() })
                        }
                    }
                }
            }
        }

        // Add accessibility description for the chart area
        Card(modifier = Modifier.fillMaxWidth().height(320.dp).semantics { contentDescription = chartDesc }) {
            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.no_graph_data_message))
                }
            } else {
                // Simple lightweight line chart drawing: map entries to points and draw a polyline
                // Precompute derived lists (dates, scores, smoothed values and fractions) in the composable scope
                // so the Canvas lambda stays free of @Composable calls.
                val paddingX = 24f
                val paddingY = 24f

                val cachedZone = remember { ZoneId.systemDefault() }
                val byDay = remember(entries) {
                    entries
                        .asSequence()
                        .map { e -> Pair(Instant.ofEpochMilli(e.timestamp).atZone(cachedZone).toLocalDate(), e.moodScore.coerceIn(1, 5)) }
                        .groupBy({ it.first }, { it.second })
                        .toSortedMap()
                        .mapValues { (_, scoresForDay) -> scoresForDay.last() }
                }

                val dates = remember(byDay) { byDay.keys.toList() }
                val scores = remember(byDay) { byDay.values.toList() }

                val smoothScores = remember(scores) {
                    fun smooth(input: List<Int>): List<Float> {
                        if (input.size <= 2) return input.map { it.toFloat() }
                        return input.indices.map { i ->
                            val window = (i - 1..i + 1).mapNotNull { idx -> input.getOrNull(idx) }
                            window.average().toFloat()
                        }
                    }
                    smooth(scores)
                }

                val minScore = 1f
                val maxScore = 5f
                val xFractions = remember(smoothScores) { smoothScores.indices.map { i -> if (smoothScores.size <= 1) 0f else (i.toFloat() / (smoothScores.size - 1)) } }
                val yFractions = remember(smoothScores) { smoothScores.map { s -> 1f - (s - minScore) / (maxScore - minScore) } }

                val formatter = DateTimeFormatter.ofPattern(dateFormatPattern)

                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                    val w = size.width - paddingX * 2
                    val h = size.height - paddingY * 2

                    val xs = xFractions.map { fx -> paddingX + w * fx }
                    val ys = yFractions.map { fy -> paddingY + h * fy }

                    drawLine(color = colorScheme.onSurface.copy(alpha = 0.12f), start = androidx.compose.ui.geometry.Offset(paddingX, paddingY + h), end = androidx.compose.ui.geometry.Offset(paddingX + w, paddingY + h), strokeWidth = 2f)

                    for (i in 0 until xs.size - 1) {
                        drawLine(color = colorScheme.primary, start = androidx.compose.ui.geometry.Offset(xs[i], ys[i]), end = androidx.compose.ui.geometry.Offset(xs[i + 1], ys[i + 1]), strokeWidth = 6f)
                    }

                    for (i in xs.indices) {
                        drawCircle(color = colorScheme.secondary, radius = 8f, center = androidx.compose.ui.geometry.Offset(xs[i], ys[i]))
                    }

                    for (i in dates.indices) {
                        val label = dates.getOrNull(i)?.format(formatter) ?: ""
                        drawContext.canvas.nativeCanvas.apply {
                            drawText(label, xs.getOrNull(i) ?: 0f, paddingY + h + 18f, android.graphics.Paint().apply { textSize = 24f; color = colorScheme.onSurface.copy(alpha = 0.7f).toArgb() })
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Explanation card for the Mood chart + small legend
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(text = stringResource(id = R.string.mood_chart_explanation_title), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = stringResource(id = R.string.mood_chart_explanation_body), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(12.dp))

                // Compact legend for Mood chart (scores 1..5)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(color = when (i) {
                                1 -> androidx.compose.ui.graphics.Color(0xFFB00020) // low
                                2 -> androidx.compose.ui.graphics.Color(0xFFFF7043)
                                3 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
                                4 -> androidx.compose.ui.graphics.Color(0xFF8BC34A)
                                else -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                            }, shape = CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = i.toString(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Group pain preview + legend + summary inside a Card so layout doesn't overlap
    val painViewModel: PainChartViewModel = viewModel(factory = PainChartViewModelFactory(painDao))
    val painState by painViewModel.uiState.collectAsState()

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                Text(text = stringResource(id = R.string.pain_chart_description), style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    when {
                        painState.isLoading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                androidx.compose.material3.CircularProgressIndicator()
                            }
                        }
                        painState.error != null -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = painState.error ?: "Unknown error", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        painState.entries.isEmpty() -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(text = stringResource(id = R.string.no_pain_data_message))
                            }
                        }
                        else -> {
                            AndroidView(factory = { ctx ->
                                LineChart(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                    setNoDataText("No hay datos")
                                    setTouchEnabled(true)
                                    setPinchZoom(true)
                                    axisRight.isEnabled = false
                                    xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
                                    legend.isEnabled = false
                                    description = Description().apply { text = "" }
                                }
                            }, update = { chart ->
                                val entriesList = painState.entries.mapIndexed { idx, e -> Entry(idx.toFloat(), e.maxIntensity.toFloat()) }
                                val dataSet = LineDataSet(entriesList, "Intensidad").apply {
                                    color = android.graphics.Color.RED
                                    setDrawCircles(true)
                                    setDrawValues(false)
                                    lineWidth = 2f
                                    circleRadius = 4f
                                    mode = LineDataSet.Mode.CUBIC_BEZIER
                                }
                                val lineData = LineData(dataSet)
                                chart.data = lineData

                                val labels = painState.entries.map { painViewModel.formatDate(it.date) }
                                chart.xAxis.valueFormatter = object : ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        val i = value.toInt()
                                        return labels.getOrNull(i) ?: ""
                                    }
                                }
                                chart.xAxis.labelRotationAngle = -45f
                                chart.invalidate()
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                // Pain chart legend and period summary (reuse existing composables from PainChartScreen)
                PainIntensityLegend(viewModel = painViewModel)
                Spacer(modifier = Modifier.height(12.dp))
                if (painState.entries.isNotEmpty()) {
                    PainSummaryCard(entries = painState.entries)
                }
            }
        }
    }
}

