package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class BreathWorkViewModelFactory(private val moodDao: MoodDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BreathWorkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BreathWorkViewModel(moodDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
