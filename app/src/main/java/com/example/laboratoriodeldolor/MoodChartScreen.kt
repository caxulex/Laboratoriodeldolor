package com.example.laboratoriodeldolor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.laboratoriodeldolor.ui.AppScaffold
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import android.view.ViewGroup
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * MoodChartScreen - displays a weekly or monthly mood score line chart.
 * Note: This implementation uses generic Compose drawing so it compiles without
 * adding an external chart dependency. Replace the charting block with a
 * third-party library (e.g., Vico) for production-grade charts if desired.
 */
private enum class MoodPeriod { Weekly, Monthly, Quarterly }

@Composable
fun MoodProgressScreen(moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository, painPointRepository: com.example.laboratoriodeldolor.repository.PainPointRepository, onOpenPainChart: () -> Unit) {
    val allEntries by moodRepository.getAllMoodEntries().collectAsState(initial = emptyList())
    // Period selector: Weekly / Monthly / Quarterly
    val (period, setPeriod) = remember { androidx.compose.runtime.mutableStateOf(MoodPeriod.Weekly) }

    // Derived window per period
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val cutoff = remember(period, today) {
        when (period) {
            MoodPeriod.Weekly -> today.minusDays(6)
            MoodPeriod.Monthly -> today.minusDays(29)
            MoodPeriod.Quarterly -> today.minusDays(89)
        }
    }

    // Filter to window, sort ascending
    val windowEntries = remember(allEntries, cutoff) {
        allEntries.filter { e ->
            val d = Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDate()
            !d.isBefore(cutoff)
        }.sortedBy { it.timestamp }
    }

    // Build chart points per period with labels and emoji markers (nullable value => gap)
    data class ChartPoint(val date: LocalDate, val label: String, val value: Float?, val emoji: String?)

    val dateFormatPattern = stringResource(id = R.string.date_format_short)
    val weekFields = remember { WeekFields.of(Locale.getDefault()) }
    val chartPoints = remember(windowEntries, period) {
        when (period) {
            MoodPeriod.Weekly, MoodPeriod.Monthly -> {
                // Build contiguous day range from cutoff..today (inclusive)
                val allDays = generateSequence(cutoff) { prev ->
                    val next = prev.plusDays(1)
                    if (next.isAfter(today)) null else next
                }.toList()

                // Map last-of-day entry
                val byDay = windowEntries.asSequence()
                    .map { e ->
                        val date = Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDate()
                        Triple(date, e.moodScore.coerceIn(1, 5), e.emoji)
                    }
                    .groupBy({ it.first }, { it })
                    .mapValues { (_, v) -> v.last() }

                val formatter = DateTimeFormatter.ofPattern(dateFormatPattern)
                allDays.map { d ->
                    val last = byDay[d]
                    ChartPoint(
                        date = d,
                        label = d.format(formatter),
                        value = last?.second?.toFloat(),
                        emoji = last?.third
                    )
                }
            }
            MoodPeriod.Quarterly -> {
                // Build week range (start-of-week for labels), include gaps
                val weekStart = cutoff.with(weekFields.dayOfWeek(), 1)
                val weeks = generateSequence(weekStart) { prev ->
                    val next = prev.plusWeeks(1)
                    if (next.isAfter(today)) null else next
                }.toList()

                val groupedByWeek = windowEntries.asSequence()
                    .map { e ->
                        val date = Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDate()
                        val start = date.with(weekFields.dayOfWeek(), 1)
                        e to start
                    }
                    .groupBy({ it.second }, { it.first })

                val formatter = DateTimeFormatter.ofPattern(dateFormatPattern)
                weeks.map { ws ->
                    val vals = groupedByWeek[ws]
                    if (vals.isNullOrEmpty()) {
                        ChartPoint(date = ws, label = ws.format(formatter), value = null, emoji = null)
                    } else {
                        val avg = vals.map { it.moodScore.coerceIn(1, 5) }.average().toFloat().coerceIn(1f, 5f)
                        val idx = kotlin.math.round(avg).toInt().coerceIn(1, 5) - 1
                        val emoji = MoodOptions.FIVE_LEVEL[idx]
                        ChartPoint(date = ws, label = ws.format(formatter), value = avg, emoji = emoji)
                    }
                }
            }
        }
    }

    val chartDesc = stringResource(id = R.string.progress_chart_desc)
    val colorScheme = MaterialTheme.colorScheme

    AppScaffold { innerPadding ->
    Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp), verticalArrangement = Arrangement.Top) {
        Text(text = stringResource(id = R.string.progress_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        // Period tabs: Semanal / Mensual / Trimestral
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val weeklyLabel = stringResource(id = R.string.progress_tab_weekly)
            val monthlyLabel = stringResource(id = R.string.progress_tab_monthly)
            val quarterlyLabel = stringResource(id = R.string.progress_tab_quarterly)
            val options = listOf(weeklyLabel, monthlyLabel, quarterlyLabel)
            val selectedIdx = when (period) {
                MoodPeriod.Weekly -> 0
                MoodPeriod.Monthly -> 1
                MoodPeriod.Quarterly -> 2
            }
            com.example.laboratoriodeldolor.ui.components.NeumorphicSegmentedControl(
                options = options,
                selectedIndex = selectedIdx,
                onSelectedIndexChange = { idx ->
                    when (idx) {
                        0 -> setPeriod(MoodPeriod.Weekly)
                        1 -> setPeriod(MoodPeriod.Monthly)
                        2 -> setPeriod(MoodPeriod.Quarterly)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button to open the Pain Chart (evolución de la intensidad del dolor)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(
                text = stringResource(id = R.string.view_pain_chart_button),
                onClick = onOpenPainChart
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Suavizado on/off
        val (smooth, setSmooth) = remember { mutableStateOf(true) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            com.example.laboratoriodeldolor.ui.components.NeumorphicTextButton(
                text = stringResource(id = R.string.smoothing_label),
                onClick = { setSmooth(!smooth) },
                containerColor = if (smooth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (smooth) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }

        // Mood chart with emoji markers
        Card(modifier = Modifier.fillMaxWidth().height(320.dp).semantics { contentDescription = chartDesc }) {
            if (chartPoints.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = stringResource(id = R.string.no_graph_data_message))
                }
            } else {
                val paddingX = 24f
                val paddingY = 24f
                val minScore = 1f
                val maxScore = 5f

                // Precompute fractions for x and y
                val xFractions = remember(chartPoints) { chartPoints.indices.map { i -> if (chartPoints.size <= 1) 0f else (i.toFloat() / (chartPoints.size - 1)) } }
                val yFractions = remember(chartPoints) {
                    chartPoints.map { p ->
                        p.value?.let { v -> 1f - ((v - minScore) / (maxScore - minScore)) }
                    }
                }

                // Smoothing (for line only), keep gaps
                fun smoothList(values: List<Float?>): List<Float?> {
                    if (values.size <= 2) return values
                    val result = MutableList<Float?>(values.size) { null }
                    var start = 0
                    while (start < values.size) {
                        while (start < values.size && values[start] == null) start++
                        if (start >= values.size) break
                        var end = start
                        while (end + 1 < values.size && values[end + 1] != null) end++
                        // Smooth contiguous block [start..end]
                        for (i in start..end) {
                            val neighbors = arrayListOf<Float>()
                            values.getOrNull(i - 1)?.let { neighbors.add(it) }
                            values[i]?.let { neighbors.add(it) }
                            values.getOrNull(i + 1)?.let { neighbors.add(it) }
                            result[i] = if (neighbors.isNotEmpty()) neighbors.average().toFloat() else values[i]
                        }
                        start = end + 1
                    }
                    return result
                }

                val yFractionsLine = remember(yFractions, smooth) { if (smooth) smoothList(yFractions) else yFractions }

                // Utility: color by score 1..5
                fun colorForScore(score: Int): androidx.compose.ui.graphics.Color = when (score.coerceIn(1, 5)) {
                    1 -> androidx.compose.ui.graphics.Color(0xFFB00020)
                    2 -> androidx.compose.ui.graphics.Color(0xFFFF7043)
                    3 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
                    4 -> androidx.compose.ui.graphics.Color(0xFF8BC34A)
                    else -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
                }

                // Tooltip state
                val density = LocalDensity.current
                var canvasSize by remember { mutableStateOf(IntSize.Zero) }
                var selectedIndex by remember { mutableStateOf<Int?>(null) }
                val fullFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
                val touchRadius = with(density) { 20.dp.toPx() }

                Box(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .matchParentSize()
                            .padding(12.dp)
                            .pointerInput(chartPoints, canvasSize, smooth) {
                                detectTapGestures { tap ->
                                    val w = canvasSize.width.toFloat() - paddingX * 2
                                    val h = canvasSize.height.toFloat() - paddingY * 2
                                    if (w <= 0f || h <= 0f) return@detectTapGestures
                                    val xs = xFractions.map { fx -> paddingX + w * fx }
                                    val ys = yFractions.map { fy -> fy?.let { paddingY + h * it } }
                                    var bestIndex: Int? = null
                                    var bestDist = Float.MAX_VALUE
                                    for (i in xs.indices) {
                                        val yy = ys[i] ?: continue
                                        val dx = tap.x - xs[i]
                                        val dy = tap.y - yy
                                        val dist = sqrt(dx * dx + dy * dy)
                                        if (dist < bestDist && dist <= touchRadius) {
                                            bestDist = dist
                                            bestIndex = i
                                        }
                                    }
                                    selectedIndex = bestIndex
                                }
                            }
                            .onSizeChanged { canvasSize = it }
                    ) {
                        val w = size.width - paddingX * 2
                        val h = size.height - paddingY * 2

                        val xs = xFractions.map { fx -> paddingX + w * fx }
                        val ys = yFractions.map { fy -> fy?.let { paddingY + h * it } }
                        val ysLine = yFractionsLine.map { fy -> fy?.let { paddingY + h * it } }

                        // Grid lines for scores 1..5
                        for (level in 1..5) {
                            val frac = 1f - ((level - minScore) / (maxScore - minScore))
                            val y = paddingY + h * frac
                            drawLine(
                                color = colorScheme.onSurface.copy(alpha = if (level == 3) 0.2f else 0.08f),
                                start = androidx.compose.ui.geometry.Offset(paddingX, y),
                                end = androidx.compose.ui.geometry.Offset(paddingX + w, y),
                                strokeWidth = if (level == 3) 2f else 1f
                            )
                        }

                        // Connecting line segments (skip gaps) colored by average level
                        for (i in 0 until xs.size - 1) {
                            val y1 = ysLine[i]
                            val y2 = ysLine[i + 1]
                            val v1 = chartPoints[i].value
                            val v2 = chartPoints[i + 1].value
                            if (y1 != null && y2 != null && v1 != null && v2 != null) {
                                val avgLevel = round(((v1 + v2) / 2f)).toInt().coerceIn(1, 5)
                                val segColor = colorForScore(avgLevel)
                                drawLine(
                                    color = segColor,
                                    start = androidx.compose.ui.geometry.Offset(xs[i], y1),
                                    end = androidx.compose.ui.geometry.Offset(xs[i + 1], y2),
                                    strokeWidth = 5f
                                )
                            }
                        }

                        // Emoji markers at points
                        val paint = android.graphics.Paint().apply {
                            textSize = 18.dp.toPx()
                            isAntiAlias = true
                        }
                        for (i in xs.indices) {
                            val point = chartPoints[i]
                            val y = ys[i]
                            val emoji = point.emoji
                            if (y != null && emoji != null) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    emoji,
                                    xs[i] - (paint.measureText(emoji) / 2f),
                                    y - 8f,
                                    paint
                                )
                            }
                        }

                        // X-axis labels: reduce clutter by showing ~6 labels
                        val step = (chartPoints.size / 6).coerceAtLeast(1)
                        val labelPaint = android.graphics.Paint().apply {
                            textSize = 12.dp.toPx()
                            color = colorScheme.onSurface.copy(alpha = 0.7f).toArgb()
                            isAntiAlias = true
                        }
                        val labelIndices = mutableSetOf(0, chartPoints.size - 1)
                        var idx = 0
                        while (idx < chartPoints.size) {
                            labelIndices.add(idx)
                            idx += step
                        }
                        for (i in labelIndices.sorted()) {
                            val label = chartPoints[i].label
                            drawContext.canvas.nativeCanvas.drawText(
                                label,
                                xs[i] - (labelPaint.measureText(label) / 2f),
                                paddingY + h + 20f,
                                labelPaint
                            )
                        }
                    }

                    // Tooltip overlay
                    val idx = selectedIndex
                    if (idx != null) {
                        val w = canvasSize.width.toFloat() - paddingX * 2
                        val h = canvasSize.height.toFloat() - paddingY * 2
                        if (w > 0f && h > 0f) {
                            val xs = xFractions.map { fx -> paddingX + w * fx }
                            val ys = yFractions.map { fy -> fy?.let { paddingY + h * it } }
                            val x = xs.getOrNull(idx) ?: 0f
                            val y = ys.getOrNull(idx)
                            val point = chartPoints.getOrNull(idx)
                            val emoji = point?.emoji ?: ""
                            val dateStr = point?.date?.format(fullFormatter) ?: ""
                            if (y != null) {
                                val text = stringResource(id = R.string.marker_text, emoji, dateStr)
                                val bg = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                                val outline = MaterialTheme.colorScheme.outline
                                val xDp = with(density) { x.toDp() }
                                val yDp = with(density) { y.toDp() }
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .offset(x = xDp - 60.dp, y = yDp - 56.dp)
                                            .background(color = bg, shape = CircleShape)
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(text = text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
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
        val painViewModel: PainChartViewModel = viewModel(factory = PainChartViewModelFactory(painPointRepository))
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
}

