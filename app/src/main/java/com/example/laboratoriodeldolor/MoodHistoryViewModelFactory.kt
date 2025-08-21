package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoodHistoryViewModelFactory(private val moodDao: MoodDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodHistoryViewModel(moodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
