package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.LocalDate

class MoodChartViewModel(private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository) : ViewModel() {
    enum class TimeFrame { WEEK, MONTH }

    private val _timeFrame = MutableStateFlow(TimeFrame.WEEK)
    val timeFrame: StateFlow<TimeFrame> = _timeFrame

    // Expose filtered entries as a StateFlow so UI can collectAsState
    val entries = combine(moodRepository.getAllMoodEntries(), _timeFrame) { list, tf ->
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val cutoff = when (tf) {
            TimeFrame.WEEK -> today.minusDays(6) // last 7 days inclusive
            TimeFrame.MONTH -> today.minusDays(29) // last 30 days
        }

        list.filter { e ->
            val d = Instant.ofEpochMilli(e.timestamp).atZone(zone).toLocalDate()
            !d.isBefore(cutoff)
        }.sortedBy { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTimeFrame(tf: TimeFrame) { _timeFrame.value = tf }
}
