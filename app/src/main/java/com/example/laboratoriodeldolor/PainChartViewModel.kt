package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.LocalDate
import java.time.Instant
import java.time.format.DateTimeFormatter

data class PainChartEntry(
    val date: LocalDate,
    val maxIntensity: Int,
    val pointCount: Int
)

data class PainChartUiState(
    val entries: List<PainChartEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTimeRange: TimeRange = TimeRange.LAST_7_DAYS
)

enum class TimeRange(val days: Long, val labelKey: String) {
    LAST_7_DAYS(7, "last_7_days"),
    LAST_30_DAYS(30, "last_30_days"),
    LAST_90_DAYS(90, "last_90_days")
}

class PainChartViewModel(private val painPointRepository: com.example.laboratoriodeldolor.repository.PainPointRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PainChartUiState())
    val uiState: StateFlow<PainChartUiState> = _uiState

    init {
        loadPainData()
    }

    fun selectTimeRange(timeRange: TimeRange) {
        _uiState.value = _uiState.value.copy(selectedTimeRange = timeRange)
        loadPainData()
    }

    private fun loadPainData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val zone = ZoneId.systemDefault()
                val now = Instant.now()
                val cutoffDays = _uiState.value.selectedTimeRange.days
                val cutoffTime = now.minusSeconds(cutoffDays * 24 * 60 * 60).toEpochMilli()
                
                painPointRepository.getAllPainPoints().collect { allPoints ->
                    // Filter points within the selected time range
                    val filteredPoints = allPoints.filter { it.timestamp >= cutoffTime }
                    
                    // Group by date and calculate max intensity and count per day
                    val entriesByDay = filteredPoints
                        .groupBy { point ->
                            Instant.ofEpochMilli(point.timestamp).atZone(zone).toLocalDate()
                        }
                        .map { (date, points) ->
                            PainChartEntry(
                                date = date,
                                maxIntensity = points.maxOfOrNull { it.intensity } ?: 1,
                                pointCount = points.size
                            )
                        }
                        .sortedBy { it.date }
                    
                    // Fill in missing days with zero intensity
                    val filledEntries = fillMissingDays(entriesByDay, cutoffDays, zone)
                    
                    _uiState.value = _uiState.value.copy(
                        entries = filledEntries,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Error loading pain data"
                )
            }
        }
    }

    private fun fillMissingDays(entries: List<PainChartEntry>, days: Long, zone: ZoneId): List<PainChartEntry> {
        val today = LocalDate.now(zone)
        val startDate = today.minusDays(days - 1)
        
        val entryMap = entries.associateBy { it.date }
        
        return (0 until days).map { dayOffset ->
            val date = startDate.plusDays(dayOffset)
            entryMap[date] ?: PainChartEntry(
                date = date,
                maxIntensity = 0,
                pointCount = 0
            )
        }
    }

    fun formatDate(date: LocalDate): String {
        return date.format(DateTimeFormatter.ofPattern("dd/MM"))
    }

    fun getIntensityColor(intensity: Int): androidx.compose.ui.graphics.Color {
        return when (intensity) {
            0 -> androidx.compose.ui.graphics.Color(0xFFE0E0E0) // Gray for no pain
            1 -> androidx.compose.ui.graphics.Color(0xFFFFF176) // Yellow for moderate
            2 -> androidx.compose.ui.graphics.Color(0xFFFFA726) // Orange for high
            3 -> androidx.compose.ui.graphics.Color(0xFFF44336) // Red for severe
            else -> androidx.compose.ui.graphics.Color(0xFFE0E0E0)
        }
    }
}
