package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoodHistoryViewModelFactory(private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodHistoryViewModel(moodRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
