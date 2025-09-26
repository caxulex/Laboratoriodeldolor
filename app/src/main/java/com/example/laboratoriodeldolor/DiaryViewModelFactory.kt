package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DiaryViewModelFactory(private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository? = null) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
            val repository = moodRepository ?: (MoodApplication.instance?.moodRepository ?: throw IllegalStateException("No MoodRepository available"))
            @Suppress("UNCHECKED_CAST")
            return DiaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
