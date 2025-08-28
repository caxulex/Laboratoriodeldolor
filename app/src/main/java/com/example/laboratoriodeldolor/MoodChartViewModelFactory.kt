package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoodChartViewModelFactory(private val moodDao: MoodDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodChartViewModel(moodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass}")
    }
}
