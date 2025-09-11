package com.example.laboratoriodeldolor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

/**
 * ViewModel exposing the list of [Routine] items.
 * Collection strategy is test-friendly: a custom dispatcher and optional external collection scope
 * can be provided. Tests can also disable auto-collection and invoke [startCollecting] manually.
 */
class RoutinesListViewModel(
    private val routineDao: RoutineDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    // Retained for potential future use (previously part of a StateFlow.stateIn approach).
    @Suppress("unused") private val started: SharingStarted = SharingStarted.WhileSubscribed(5000),
    private val collectionScope: CoroutineScope? = null,
    private val autoCollect: Boolean = true
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _routines = MutableStateFlow<List<Routine>>(emptyList())
    val routines: StateFlow<List<Routine>> = _routines

    private suspend fun collectRoutines() {
        routineDao.getAll()
            .catch { e ->
                _error.value = e.message ?: "db_error"
                emit(emptyList())
            }
            .collect { list -> _routines.value = list }
    }

    init {
        if (autoCollect) {
            val scope = collectionScope ?: viewModelScope
            scope.launch(dispatcher) { collectRoutines() }
        }
    }

    /** Start collecting routines in the provided [scope] or [viewModelScope] if null. */
    fun startCollecting(scope: CoroutineScope? = null) =
        (scope ?: viewModelScope).launch(dispatcher) { collectRoutines() }

    fun clearError() { _error.value = null }
}
