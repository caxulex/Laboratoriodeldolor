package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch

class MoodHistoryViewModel(private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    val moodEntries: StateFlow<List<MoodEntry>> = moodRepository.getAllMoodEntries()
        .catch { e ->
            // capture error and emit empty list to UI
            _error.value = e.message ?: "db_error"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearError() {
        _error.value = null
    }
}
