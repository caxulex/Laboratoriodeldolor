package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PainChartViewModelFactory(
    private val painPointRepository: com.example.laboratoriodeldolor.repository.PainPointRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PainChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PainChartViewModel(painPointRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
