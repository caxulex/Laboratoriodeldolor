package com.example.laboratoriodeldolor

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
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
                        PainIntensityChart(
                            entries = uiState.entries,
                            viewModel = viewModel,
                            modifier = Modifier.fillMaxSize()
                        )
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

@Composable
private fun PainIntensityChart(
    entries: List<PainChartEntry>,
    viewModel: PainChartViewModel,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val paddingX = 60f
        val paddingY = 60f
        val chartWidth = size.width - paddingX * 2
        val chartHeight = size.height - paddingY * 2
        
        if (entries.isEmpty()) return@Canvas
        
        // Draw Y-axis (intensity scale)
        val maxIntensity = 3f
        val yAxisColor = Color.Gray.copy(alpha = 0.5f)
        
        // Y-axis line
        drawLine(
            color = yAxisColor,
            start = androidx.compose.ui.geometry.Offset(paddingX, paddingY),
            end = androidx.compose.ui.geometry.Offset(paddingX, paddingY + chartHeight),
            strokeWidth = 2f
        )
        
        // Y-axis labels
        for (i in 0..3) {
            val y = paddingY + chartHeight - (i / maxIntensity) * chartHeight
            val label = when (i) {
                0 -> "Sin dolor"
                1 -> "Moderado"
                2 -> "Alto"
                3 -> "Severo"
                else -> ""
            }
            
            // Grid line
            if (i > 0) {
                drawLine(
                    color = yAxisColor.copy(alpha = 0.3f),
                    start = androidx.compose.ui.geometry.Offset(paddingX, y),
                    end = androidx.compose.ui.geometry.Offset(paddingX + chartWidth, y),
                    strokeWidth = 1f
                )
            }
            
            // Label
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    label,
                    paddingX - 50f,
                    y + 5f,
                    android.graphics.Paint().apply {
                        color = yAxisColor.toArgb()
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.RIGHT
                    }
                )
            }
        }
        
        // Draw X-axis
        drawLine(
            color = yAxisColor,
            start = androidx.compose.ui.geometry.Offset(paddingX, paddingY + chartHeight),
            end = androidx.compose.ui.geometry.Offset(paddingX + chartWidth, paddingY + chartHeight),
            strokeWidth = 2f
        )
        
        // Draw bars
        val barWidth = chartWidth / entries.size * 0.8f
        val barSpacing = chartWidth / entries.size
        
        entries.forEachIndexed { index, entry ->
            val barHeight = if (entry.maxIntensity > 0) {
                (entry.maxIntensity / maxIntensity) * chartHeight
            } else {
                0f
            }
            
            val x = paddingX + index * barSpacing + (barSpacing - barWidth) / 2
            val y = paddingY + chartHeight - barHeight
            
            val barColor = viewModel.getIntensityColor(entry.maxIntensity)
            
            drawRect(
                color = barColor,
                topLeft = androidx.compose.ui.geometry.Offset(x, y),
                size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
            )
            
            // Draw date labels (every few days to avoid crowding)
            if (entries.size <= 7 || index % (entries.size / 7).coerceAtLeast(1) == 0) {
                val dateLabel = viewModel.formatDate(entry.date)
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        dateLabel,
                        x + barWidth / 2,
                        paddingY + chartHeight + 30f,
                        android.graphics.Paint().apply {
                            color = yAxisColor.toArgb()
                            textSize = 20f
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PainIntensityLegend(viewModel: PainChartViewModel) {
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
private fun PainSummaryCard(entries: List<PainChartEntry>) {
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
