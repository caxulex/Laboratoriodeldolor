package com.example.laboratoriodeldolor.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.laboratoriodeldolor.data.AgeRange
import com.example.laboratoriodeldolor.data.PrimaryCondition
import com.example.laboratoriodeldolor.data.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InitialConfigurationState(
    val currentStep: Int = 1,
    val userName: String = "",
    val ageRange: AgeRange? = null,
    val primaryCondition: PrimaryCondition? = null,
    val notificationsEnabled: Boolean = true,
    val preferredReminderTime: String = "20:00",
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false
)

class InitialConfigurationViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InitialConfigurationState())
    val uiState: StateFlow<InitialConfigurationState> = _uiState.asStateFlow()

    fun nextStep() {
        _uiState.update { currentState ->
            val nextStep = (currentState.currentStep + 1).coerceAtMost(6)
            currentState.copy(currentStep = nextStep)
        }
        saveCurrentStep()
    }

    fun previousStep() {
        _uiState.update { currentState ->
            val previousStep = (currentState.currentStep - 1).coerceAtLeast(1)
            currentState.copy(currentStep = previousStep)
        }
        saveCurrentStep()
    }

    fun updateUserName(name: String) {
        _uiState.update { it.copy(userName = name) }
        viewModelScope.launch {
            userPreferencesRepository.setUserName(name)
        }
    }

    fun updateAgeRange(ageRange: AgeRange) {
        _uiState.update { it.copy(ageRange = ageRange) }
        viewModelScope.launch {
            userPreferencesRepository.setUserAgeRange(ageRange)
        }
    }

    fun updatePrimaryCondition(condition: PrimaryCondition) {
        _uiState.update { it.copy(primaryCondition = condition) }
        viewModelScope.launch {
            userPreferencesRepository.setUserPrimaryCondition(condition)
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
        viewModelScope.launch {
            userPreferencesRepository.setNotificationsEnabled(enabled)
        }
    }

    fun updatePreferredReminderTime(time: String) {
        _uiState.update { it.copy(preferredReminderTime = time) }
        viewModelScope.launch {
            userPreferencesRepository.setPreferredReminderTime(time)
        }
    }

    fun completeConfiguration() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                userPreferencesRepository.completeInitialConfiguration()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        isCompleted = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
                // Handle error if needed
            }
        }
    }

    private fun saveCurrentStep() {
        viewModelScope.launch {
            userPreferencesRepository.setConfigStepCompleted(_uiState.value.currentStep)
        }
    }

    // Factory para crear el ViewModel con dependencias
    class Factory(
        private val userPreferencesRepository: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InitialConfigurationViewModel::class.java)) {
                return InitialConfigurationViewModel(userPreferencesRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}