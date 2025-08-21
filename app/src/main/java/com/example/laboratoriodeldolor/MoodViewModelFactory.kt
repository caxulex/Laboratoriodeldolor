package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MoodViewModelFactory(
    private val moodDao: MoodDao,
    private val exerciseDao: ExerciseDao,
    private val preferencesRepository: com.example.laboratoriodeldolor.data.UserPreferencesRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodViewModel(moodDao, exerciseDao, preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}