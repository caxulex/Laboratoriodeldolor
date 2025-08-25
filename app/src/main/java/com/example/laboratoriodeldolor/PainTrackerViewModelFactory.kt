package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class PainTrackerViewModelFactory(
    private val painPointDao: PainPointDao,
    private val painLogDao: PainLogDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PainTrackerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PainTrackerViewModel(painPointDao, painLogDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
