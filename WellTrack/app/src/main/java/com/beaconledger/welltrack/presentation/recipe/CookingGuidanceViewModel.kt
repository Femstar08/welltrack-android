package com.beaconledger.welltrack.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.CookingGuidanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CookingGuidanceViewModel @Inject constructor(
    private val cookingGuidanceUseCase: CookingGuidanceUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CookingGuidanceUiState())
    val uiState: StateFlow<CookingGuidanceUiState> = _uiState.asStateFlow()
    
    private var currentSessionId: String? = null
    
    fun startCookingSession(recipeId: String, targetServings: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // First scale the recipe
                val scaleResult = cookingGuidanceUseCase.scaleRecipe(recipeId, targetServings)
                if (scaleResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = scaleResult.exceptionOrNull()?.message ?: "Failed to scale recipe"
                    )
                    return@launch
                }
                
                val scaledRecipe = scaleResult.getOrThrow()
                
                // Start cooking session
                val sessionResult = cookingGuidanceUseCase.startCookingSession(
                    recipeId = recipeId,
                    userId = getCurrentUserId(), // You'll need to implement this
                    targetServings = targetServings
                )
                
                if (sessionResult.isFailure) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = sessionResult.exceptionOrNull()?.message ?: "Failed to start cooking session"
                    )
                    return@launch
                }
                
                val cookingSession = sessionResult.getOrThrow()
                currentSessionId = cookingSession.id
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    scaledRecipe = scaledRecipe,
                    cookingSession = cookingSession,
                    error = null
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }
    
    fun completeStep(stepIndex: Int) {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            val result = cookingGuidanceUseCase.completeStep(sessionId, stepIndex)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    cookingSession = result.getOrThrow()
                )
            }
        }
    }
    
    fun uncheckStep(stepIndex: Int) {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            val result = cookingGuidanceUseCase.uncheckStep(sessionId, stepIndex)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    cookingSession = result.getOrThrow()
                )
            }
        }
    }
    
    fun startTimer(stepNumber: Int, name: String, durationMinutes: Int) {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            val result = cookingGuidanceUseCase.startTimer(sessionId, stepNumber, name, durationMinutes)
            if (result.isSuccess) {
                val (updatedSession, timer) = result.getOrThrow()
                _uiState.value = _uiState.value.copy(
                    cookingSession = updatedSession,
                    activeTimers = _uiState.value.activeTimers + timer
                )
            }
        }
    }
    
    fun stopTimer(timerId: String) {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            val result = cookingGuidanceUseCase.stopTimer(sessionId, timerId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    cookingSession = result.getOrThrow(),
                    activeTimers = _uiState.value.activeTimers.filter { it.id != timerId }
                )
            }
        }
    }
    
    fun pauseSession() {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            val result = cookingGuidanceUseCase.pauseCookingSession(sessionId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    cookingSession = result.getOrThrow()
                )
            }
        }
    }
    
    fun resumeSession() {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            val result = cookingGuidanceUseCase.resumeCookingSession(sessionId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    cookingSession = result.getOrThrow()
                )
            }
        }
    }
    
    fun completeSession() {
        val sessionId = currentSessionId ?: return
        
        viewModelScope.launch {
            val result = cookingGuidanceUseCase.completeCookingSession(sessionId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    cookingSession = result.getOrThrow(),
                    activeTimers = emptyList()
                )
            }
        }
    }
    
    // TODO: Implement proper user session management
    private fun getCurrentUserId(): String {
        return "demo_user_id" // Placeholder - implement proper user session management
    }
}

data class CookingGuidanceUiState(
    val isLoading: Boolean = false,
    val scaledRecipe: ScaledRecipe? = null,
    val cookingSession: CookingSession? = null,
    val activeTimers: List<CookingTimer> = emptyList(),
    val error: String? = null
)