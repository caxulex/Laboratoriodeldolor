package com.example.laboratoriodeldolor

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.components.MarkerView
import android.widget.TextView
import android.content.Context
import android.util.AttributeSet
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoodHistoryScreen(viewModel: MoodHistoryViewModel, onOpenMoodChart: () -> Unit = {}) {
    val ctx = LocalContext.current
    val entries by viewModel.moodEntries.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = SnackbarHostState()

    com.example.laboratoriodeldolor.ui.AppScaffold { innerPadding ->
        // show snackbar when an error occurs
        val snackbarMessage = stringResource(id = R.string.error_load_data)
        LaunchedEffect(error) {
            if (!error.isNullOrEmpty()) {
                snackbarHostState.showSnackbar(message = snackbarMessage)
                viewModel.clearError()
            }
        }
        Card(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {
            Column(modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 32.dp, bottom = 16.dp)) {
                Text(text = stringResource(id = R.string.mood_history_title), style = MaterialTheme.typography.headlineSmall)
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Button to access mood chart visualization
                com.example.laboratoriodeldolor.ui.components.PrimaryButton(
                    text = stringResource(id = R.string.mood_chart_title),
                    onClick = { onOpenMoodChart() },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                        if (entries.isEmpty()) {
                            // Polished empty state with gentle entrance animation
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { it / 4 }),
                                exit = fadeOut() + slideOutVertically()
                            ) {
                                EmptyMoodState()
                            }
                        } else {
                    // Use numeric moodScore directly and index-based X values to avoid huge timestamp floats
                    val mapped = entries.map { me -> Pair(me.timestamp, me.moodScore) }

                    if (mapped.isEmpty()) {
                        Text(text = stringResource(id = R.string.no_graph_data_message), modifier = Modifier.padding(top = 16.dp))
                        return@Column
                    }

                    // Initialize MPAndroidChart Utils (required for density conversions)
                    Utils.init(ctx)

                    // Create index-based entries and keep timestamps and emojis for axis labels and markers
                    val timestamps = mapped.map { it.first }
                    val emojis = entries.map { it.emoji }.filterIndexed { i, _ -> i < mapped.size }
                    val chartEntries = mapped.mapIndexed { index, pair -> Entry(index.toFloat(), pair.second.toFloat()) }
                    val dataSet = LineDataSet(chartEntries, "Mood")
                    dataSet.lineWidth = 2f
                    dataSet.setDrawCircles(true)
                    dataSet.setDrawValues(false)

                    val lineData = LineData(dataSet)

                    AndroidView(factory = { ctx2 ->
                        LineChart(ctx2).apply {
                            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                            data = lineData
                            // Attach a simple marker view that displays the emoji and date for the touched entry
                            marker = EmojiMarkerView(ctx2, timestamps, emojis)
                            description.isEnabled = false

                            xAxis.position = XAxis.XAxisPosition.BOTTOM
                            xAxis.valueFormatter = object : ValueFormatter() {
                                private val fmt = SimpleDateFormat("dd/MM", Locale.getDefault())
                                override fun getFormattedValue(value: Float): String {
                                    val idx = value.toInt()
                                    return if (idx >= 0 && idx < timestamps.size) {
                                        fmt.format(Date(timestamps[idx]))
                                    } else {
                                        ""
                                    }
                                }
                            }

                            axisRight.isEnabled = false
                            // Mood score is on a 1..5 scale
                            axisLeft.axisMinimum = 1f
                            axisLeft.axisMaximum = 5f

                            invalidate()
                        }
                    }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))

                    // Also present a list of entries as cards below the chart for quick scanning
                    LazyColumn(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                        val reversedEntries = entries.reversed()
                        items(items = reversedEntries) { entry: MoodEntry ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = entry.emoji, style = MaterialTheme.typography.headlineSmall)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = entry.note, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(java.util.Date(entry.timestamp)), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyMoodState() {
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        // Polished empty state with icon and friendly copy
        androidx.compose.material3.Icon(
            imageVector = androidx.compose.material.icons.Icons.Default.Home,
            contentDescription = stringResource(id = R.string.back_button),
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = stringResource(id = R.string.empty_mood_title), style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = stringResource(id = R.string.empty_mood_subtitle), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 24.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}

/**
 * Simple MarkerView that shows the emoji and formatted date for a selected chart entry.
 */
class EmojiMarkerView(context: Context, private val timestamps: List<Long>, private val emojis: List<String>) : MarkerView(context, android.R.layout.simple_list_item_1) {
    // Secondary constructors so tools and layout inflation can instantiate the view if needed
    constructor(context: Context) : this(context, emptyList(), emptyList())
    constructor(context: Context, _attrs: AttributeSet?) : this(context, emptyList(), emptyList())
    constructor(context: Context, _attrs: AttributeSet?, _defStyle: Int) : this(context, emptyList(), emptyList())
    private val tv: TextView = findViewById(android.R.id.text1)
    private val fmt = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())

    override fun refreshContent(e: Entry?, highlight: com.github.mikephil.charting.highlight.Highlight?) {
        if (e == null) return
        val idx = e.x.toInt()
        val emoji = if (idx >= 0 && idx < emojis.size) emojis[idx] else ""
        val date = if (idx >= 0 && idx < timestamps.size) fmt.format(Date(timestamps[idx])) else ""
        // Use a string resource with placeholders for safer localization
        tv.text = context.getString(R.string.marker_text, emoji, date)
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        // Center the marker horizontally and place it above the point
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}

private fun mapEmojiToValue(emoji: String): Int? {
    // Legacy helper removed — use MoodMapping.emojiToScore(emoji) when a numeric value is required.
    return null
}
