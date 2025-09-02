package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PainChartViewModelFactory(
    private val painPointDao: PainPointDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PainChartViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PainChartViewModel(painPointDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
