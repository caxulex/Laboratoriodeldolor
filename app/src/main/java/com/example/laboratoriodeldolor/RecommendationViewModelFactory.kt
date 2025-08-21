package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecommendationViewModelFactory(
    private val moodDao: MoodDao,
    private val painDao: PainPointDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendationViewModel(moodDao, painDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
