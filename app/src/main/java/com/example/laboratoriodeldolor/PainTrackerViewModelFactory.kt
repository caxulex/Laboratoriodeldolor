package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PainTrackerViewModelFactory(
    private val painPointRepository: com.example.laboratoriodeldolor.repository.PainPointRepository,
    private val painLogRepository: com.example.laboratoriodeldolor.repository.PainLogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PainTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PainTrackerViewModel(painPointRepository, painLogRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
