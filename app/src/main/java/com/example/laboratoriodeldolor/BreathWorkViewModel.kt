package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * BreathWorkViewModel
 * - Fetches the most recent MoodEntry and suggests a breath-work exercise id.
 */
class BreathWorkViewModel(private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository) : ViewModel() {

    private val _recommendedId = MutableStateFlow<String?>(null)
    val recommendedId: StateFlow<String?> = _recommendedId

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val recent = withContext(Dispatchers.IO) { moodRepository.getRecentMoodEntries(1) }
            val latest = recent.firstOrNull()
            val id = when (latest?.emoji) {
                "😞" -> "enamorado"
                "😐" -> "chilindrina"
                "😀" -> "cuadrado"
                else -> null
            }
            _recommendedId.value = id
        }
    }
}
