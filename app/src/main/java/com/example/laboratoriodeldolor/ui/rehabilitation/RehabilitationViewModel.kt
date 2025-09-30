package com.example.laboratoriodeldolor.ui.rehabilitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.laboratoriodeldolor.data.rehabilitation.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class RehabilitationViewModel(
    private val repository: RehabilitationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(RehabilitationUiState())
    val uiState: StateFlow<RehabilitationUiState> = _uiState.asStateFlow()
    
    val categories = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val categoryProgress = repository.getCategoryProgressOverview()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    val recentSessions = repository.getRecentSessionsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    val exercisesForCategory = selectedCategory
        .filterNotNull()
        .flatMapLatest { categoryId ->
            repository.getExercisesByCategory(categoryId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())
    
    private val _selectedExercise = MutableStateFlow<RehabilitationExercise?>(null)
    val selectedExercise: StateFlow<RehabilitationExercise?> = _selectedExercise.asStateFlow()
    
    private val _exerciseState = MutableStateFlow(ExerciseSessionState())
    val exerciseState: StateFlow<ExerciseSessionState> = _exerciseState.asStateFlow()
    
    init {
        initializeData()
        loadStats()
    }
    
    private fun initializeData() {
        viewModelScope.launch {
            try {
                repository.initializeDefaultCategories()
                repository.initializeDefaultExercises()
            } catch (e: Exception) {
                updateErrorState("Error initializing rehabilitation data: ${e.message}")
            }
        }
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            try {
                val stats = repository.getOverallStats()
                _uiState.update { it.copy(stats = stats) }
            } catch (e: Exception) {
                updateErrorState("Error loading stats: ${e.message}")
            }
        }
    }
    
    fun selectCategory(categoryId: String) {
        _selectedCategory.value = categoryId
    }
    
    fun selectExercise(exercise: RehabilitationExercise) {
        _selectedExercise.value = exercise
        _exerciseState.update { 
            ExerciseSessionState(
                exercise = exercise,
                currentStep = 1,
                totalSteps = exercise.repetitions,
                timeRemaining = exercise.durationSeconds,
                isRunning = false
            )
        }
    }
    
    fun startExercise() {
        _exerciseState.update { it.copy(isRunning = true, startTime = LocalDateTime.now()) }
    }
    
    fun pauseExercise() {
        _exerciseState.update { it.copy(isRunning = false) }
    }
    
    fun resumeExercise() {
        _exerciseState.update { it.copy(isRunning = true) }
    }
    
    fun nextStep() {
        val current = _exerciseState.value
        if (current.currentStep < current.totalSteps) {
            _exerciseState.update { 
                it.copy(
                    currentStep = it.currentStep + 1,
                    timeRemaining = current.exercise?.durationSeconds ?: 60
                )
            }
        } else {
            completeExercise()
        }
    }
    
    fun updateTimer(secondsRemaining: Int) {
        _exerciseState.update { it.copy(timeRemaining = secondsRemaining) }
        
        if (secondsRemaining <= 0) {
            nextStep()
        }
    }
    
    private fun completeExercise() {
        val state = _exerciseState.value
        val exercise = state.exercise ?: return
        
        _exerciseState.update { 
            it.copy(
                isRunning = false,
                isCompleted = true,
                endTime = LocalDateTime.now()
            )
        }
        
        // Show completion dialog
        _uiState.update { it.copy(showCompletionDialog = true) }
    }
    
    fun recordSession(painLevel: Int?, difficultyRating: Int?, notes: String?) {
        val state = _exerciseState.value
        val exercise = state.exercise ?: return
        val startTime = state.startTime ?: LocalDateTime.now()
        val endTime = state.endTime ?: LocalDateTime.now()
        
        viewModelScope.launch {
            try {
                val session = RehabilitationSession(
                    exerciseId = exercise.id,
                    completedAt = endTime,
                    durationSeconds = java.time.Duration.between(startTime, endTime).seconds.toInt(),
                    repetitionsCompleted = state.currentStep,
                    difficultyRating = difficultyRating,
                    painLevel = painLevel,
                    notes = notes
                )
                
                repository.recordSession(session)
                
                // Refresh stats
                loadStats()
                
                // Reset exercise state
                _exerciseState.value = ExerciseSessionState()
                _uiState.update { it.copy(showCompletionDialog = false) }
                
                updateSuccessState("Sesión registrada exitosamente")
                
            } catch (e: Exception) {
                updateErrorState("Error recording session: ${e.message}")
            }
        }
    }
    
    fun dismissCompletionDialog() {
        _uiState.update { it.copy(showCompletionDialog = false) }
        _exerciseState.value = ExerciseSessionState()
    }
    
    fun searchExercises(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }
        
        viewModelScope.launch {
            try {
                val results = repository.searchExercises(query)
                _uiState.update { it.copy(searchResults = results) }
            } catch (e: Exception) {
                updateErrorState("Error searching exercises: ${e.message}")
            }
        }
    }
    
    fun getRecommendedExercises() {
        viewModelScope.launch {
            try {
                val recommended = repository.getRecommendedExercises()
                _uiState.update { it.copy(recommendedExercises = recommended) }
            } catch (e: Exception) {
                updateErrorState("Error loading recommendations: ${e.message}")
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
    
    fun clearSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
    
    private fun updateErrorState(message: String) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }
    
    private fun updateSuccessState(message: String) {
        _uiState.update { it.copy(successMessage = message, isLoading = false) }
    }
    
    fun backToCategories() {
        _selectedCategory.value = null
        _selectedExercise.value = null
        _exerciseState.value = ExerciseSessionState()
    }
    
    fun backToExercises() {
        _selectedExercise.value = null
        _exerciseState.value = ExerciseSessionState()
    }
}

data class RehabilitationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showCompletionDialog: Boolean = false,
    val stats: RehabilitationStats? = null,
    val searchResults: List<RehabilitationExercise> = emptyList(),
    val recommendedExercises: List<RehabilitationExercise> = emptyList()
)

data class ExerciseSessionState(
    val exercise: RehabilitationExercise? = null,
    val currentStep: Int = 1,
    val totalSteps: Int = 1,
    val timeRemaining: Int = 0,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val startTime: LocalDateTime? = null,
    val endTime: LocalDateTime? = null
)

class RehabilitationViewModelFactory(
    private val repository: RehabilitationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RehabilitationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RehabilitationViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}