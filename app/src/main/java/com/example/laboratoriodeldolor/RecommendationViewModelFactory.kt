package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecommendationViewModelFactory(
    private val moodDao: MoodDao,
    private val painDao: PainPointDao,
    private val routineDao: RoutineDao,
    private val routineStepDao: RoutineStepDao,
    private val techniqueDao: TechniqueDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendationViewModel(moodDao, painDao, routineDao, routineStepDao, techniqueDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass}")
    }
}
