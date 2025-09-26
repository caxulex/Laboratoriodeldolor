package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RecommendationViewModelFactory(
    private val moodRepository: com.example.laboratoriodeldolor.repository.MoodRepository,
    private val painPointRepository: com.example.laboratoriodeldolor.repository.PainPointRepository,
    private val routineRepository: com.example.laboratoriodeldolor.repository.RoutineRepository,
    private val routineStepRepository: com.example.laboratoriodeldolor.repository.RoutineStepRepository,
    private val techniqueRepository: com.example.laboratoriodeldolor.repository.TechniqueRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RecommendationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RecommendationViewModel(moodRepository, painPointRepository, routineRepository, routineStepRepository, techniqueRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass}")
    }
}
