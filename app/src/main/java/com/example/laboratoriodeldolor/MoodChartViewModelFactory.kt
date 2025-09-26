package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoodChartViewModelFactory(private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodChartViewModel(moodRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass}")
    }
}
