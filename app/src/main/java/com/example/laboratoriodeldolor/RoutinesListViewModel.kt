package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch

class RoutinesListViewModel(
    private val routineDao: RoutineDao,
    // dispatcher is injected for testability; default to IO to ensure DB collection doesn't block UI
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Collect Room Flow and expose as StateFlow; use viewModelScope with IO dispatcher to avoid blocking UI
    val routines: StateFlow<List<Routine>> = routineDao.getAll()
        .catch { e ->
            _error.value = e.message ?: "db_error"
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearError() { _error.value = null }
}
