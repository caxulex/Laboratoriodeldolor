package com.example.laboratoriodeldolor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import android.content.Context
import android.graphics.Canvas
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PainChartScreen(
    painDao: PainPointDao,
    onBack: () -> Unit,
    viewModel: PainChartViewModel = viewModel(factory = PainChartViewModelFactory(painDao))
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // String resources
    val titleText = stringResource(id = R.string.pain_intensity_chart_title)
    val noDataText = stringResource(id = R.string.no_pain_data_message)
    val chartDesc = stringResource(id = R.string.pain_chart_description)
    val last7DaysText = stringResource(id = R.string.last_7_days)
    val last30DaysText = stringResource(id = R.string.last_30_days)
    val last90DaysText = stringResource(id = R.string.last_90_days)
    val intensityLabelText = stringResource(id = R.string.pain_intensity_label)
    val dateAxisText = stringResource(id = R.string.date_axis_label)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        // Header
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time range selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val timeRangeOptions = listOf(
                TimeRange.LAST_7_DAYS to last7DaysText,
                TimeRange.LAST_30_DAYS to last30DaysText,
                TimeRange.LAST_90_DAYS to last90DaysText
            )
            
            timeRangeOptions.forEach { (range, label) ->
                FilterChip(
                    onClick = { viewModel.selectTimeRange(range) },
                    label = { Text(label) },
                    selected = uiState.selectedTimeRange == range
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Chart Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .semantics { contentDescription = chartDesc },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = uiState.error ?: "Unknown error",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    uiState.entries.isEmpty() || uiState.entries.all { it.maxIntensity == 0 } -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = noDataText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(id = R.string.pain_chart_suggestion),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    else -> {
                        // MPAndroidChart LineChart rendering via AndroidView
                        AndroidView(factory = { ctx ->
                            LineChart(ctx).apply {
                                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                                setNoDataText("No hay datos")
                                setTouchEnabled(true)
                                setPinchZoom(true)
                                axisRight.isEnabled = false
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                legend.isEnabled = false
                                description = Description().apply { text = "" }
                            }
                        }, update = { chart ->
                            val entriesList = uiState.entries.mapIndexed { idx, e -> Entry(idx.toFloat(), e.maxIntensity.toFloat()) }
                            val dataSet = LineDataSet(entriesList, "Intensidad").apply {
                                color = android.graphics.Color.RED
                                setDrawCircles(true)
                                setDrawValues(false)
                                lineWidth = 2f
                                circleRadius = 4f
                                mode = LineDataSet.Mode.CUBIC_BEZIER // smooth curve
                            }
                            val lineData = LineData(dataSet)
                            chart.data = lineData

                            // X axis labels: use viewModel.formatDate()
                            val labels = uiState.entries.map { viewModel.formatDate(it.date) }
                            chart.xAxis.valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    val i = value.toInt()
                                    return labels.getOrNull(i) ?: ""
                                }
                            }
                            chart.xAxis.labelRotationAngle = -45f

                            // Attach a lightweight Marker to show date + intensity when touching points
                            val labelsForMarker = uiState.entries.map { viewModel.formatDate(it.date) }
                            chart.marker = ChartMarker(chart.context, labelsForMarker)

                            chart.invalidate()
                        })
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legend
        PainIntensityLegend(viewModel = viewModel)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Summary Statistics
        if (uiState.entries.isNotEmpty()) {
            PainSummaryCard(entries = uiState.entries)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Back button
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(id = R.string.back_button))
        }
    }
}

/**
 * Simple IMarker implementation that draws a small rounded tooltip with the date and intensity.
 * Avoids XML resources so it works inline.
 */
private class ChartMarker(private val context: Context, private val labels: List<String>) : IMarker {
    private var text: String = ""
    private val textPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = 36f
        isAntiAlias = true
    }
    private val bgPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.argb(190, 0, 0, 0)
        isAntiAlias = true
    }
    private val padding = 12f
    private var measuredWidth = 0f
    private var measuredHeight = 0f

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) return
        val idx = e.x.toInt()
        val label = labels.getOrNull(idx) ?: ""
        val intensity = e.y.toInt()
        text = "$label: $intensity"
        measuredWidth = textPaint.measureText(text) + padding * 2
        measuredHeight = textPaint.textSize + padding * 2
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(measuredWidth / 2f), -measuredHeight - 10f)
    }
    // Newer versions of MPAndroidChart require getOffsetForDrawingAtPoint
    override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
        return getOffset()
    }

    override fun draw(canvas: Canvas, posX: Float, posY: Float) {
        val offset = getOffsetForDrawingAtPoint(posX, posY)
        val left = posX + offset.x
        val top = posY + offset.y
        val right = left + measuredWidth
        val bottom = top + measuredHeight
        val rectF = android.graphics.RectF(left, top, right, bottom)
        canvas.drawRoundRect(rectF, 8f, 8f, bgPaint)
        // draw text baseline
        val textX = left + padding
        val textY = top + padding + textPaint.textSize * 0.8f
        canvas.drawText(text, textX, textY, textPaint)
    }
}

// The previous Canvas-based chart implementation was replaced by MPAndroidChart AndroidView.

@Composable
    fun PainIntensityLegend(viewModel: PainChartViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.intensity_legend_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val intensityLevels = listOf(
                0 to stringResource(id = R.string.no_pain),
                1 to stringResource(id = R.string.intensity_moderate),
                2 to stringResource(id = R.string.intensity_high),
                3 to stringResource(id = R.string.intensity_severe)
            )
            
            intensityLevels.forEach { (level, label) ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(
                                color = viewModel.getIntensityColor(level),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
    fun PainSummaryCard(entries: List<PainChartEntry>) {
    val totalDaysWithPain = entries.count { it.maxIntensity > 0 }
    val avgIntensity = entries.filter { it.maxIntensity > 0 }
        .map { it.maxIntensity }
        .takeIf { it.isNotEmpty() }
        ?.average() ?: 0.0
    val totalPainPoints = entries.sumOf { it.pointCount }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(id = R.string.pain_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = totalDaysWithPain.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(id = R.string.days_with_pain),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = String.format("%.1f", avgIntensity),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = stringResource(id = R.string.avg_intensity),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = totalPainPoints.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = stringResource(id = R.string.total_points),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
