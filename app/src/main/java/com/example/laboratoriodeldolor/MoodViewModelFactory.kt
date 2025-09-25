package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoodViewModelFactory(
    private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository,
    private val exerciseRepository: com.example.laboratoriodeldolor.repository.ExerciseRepository,
    private val preferencesRepository: com.example.laboratoriodeldolor.data.UserPreferencesRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodViewModel(moodRepository, exerciseRepository, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}