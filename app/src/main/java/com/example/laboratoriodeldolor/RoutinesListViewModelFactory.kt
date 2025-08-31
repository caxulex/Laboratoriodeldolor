package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class RoutinesListViewModelFactory(private val routineDao: RoutineDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RoutinesListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoutinesListViewModel(routineDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
