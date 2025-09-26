package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoutinesListViewModelFactory(private val routineRepository: com.example.laboratoriodeldolor.repository.RoutineRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutinesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutinesListViewModel(routineRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
