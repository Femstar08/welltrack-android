package com.beaconledger.welltrack.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.GoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalUseCase: GoalUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GoalUiState())
    val uiState: StateFlow<GoalUiState> = _uiState.asStateFlow()

    private val _goalOverview = MutableStateFlow(GoalOverview(
        totalActiveGoals = 0,
        completedGoals = 0,
        overdueGoals = 0,
        averageCompletionRate = 0f,
        goalsByCategory = emptyMap(),
        recentProgress = emptyList(),
        upcomingMilestones = emptyList(),
        trends = emptyMap(),
        recommendations = emptyList()
    ))
    val goalOverview: StateFlow<GoalOverview> = _goalOverview.asStateFlow()

    // Current user ID - this would come from authentication
    private val currentUserId = "current_user_id" // TODO: Get from auth
    
    fun loadGoals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                val activeGoals = goalUseCase.getActiveGoalsWithProgress(currentUserId)
                _uiState.update { currentState ->
                    currentState.copy(
                        activeGoals = activeGoals,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load goals"
                    )
                }
            }
        }
    }

    fun loadGoalOverview() {
        viewModelScope.launch {
            try {
                val overview = goalUseCase.getGoalOverview(currentUserId)
                _goalOverview.value = overview
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to load goal overview")
                }
            }
        }
    }

    fun filterByCategory(category: GoalCategory) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                goalUseCase.getGoalsByCategory(currentUserId, category)
                    .collect { goals ->
                        // Convert goals to goals with progress
                        val goalsWithProgress = goals.mapNotNull { goal ->
                            goalUseCase.getGoalWithProgress(goal.id)
                        }

                        _uiState.update { currentState ->
                            currentState.copy(
                                activeGoals = goalsWithProgress,
                                selectedCategory = category,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to filter goals"
                    )
                }
            }
        }
    }
    
    fun selectGoal(goalId: String) {
        _selectedGoalId.value = goalId
        loadGoalDetails(goalId)
    }
    
    fun loadGoalDetails(goalId: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingDetails = true) }
                
                val goalWithProgress = goalUseCase.getGoalWithProgress(goalId)
                val statistics = goalUseCase.getGoalStatistics(goalId).getOrNull()
                val prediction = goalUseCase.getLatestPrediction(goalId)
                
                _uiState.update { 
                    it.copy(
                        selectedGoal = goalWithProgress,
                        selectedGoalStatistics = statistics,
                        selectedGoalPrediction = prediction,
                        isLoadingDetails = false
                    )
                }
                
                // Load progress and milestones
                loadProgressForGoal(goalId)
                loadMilestonesForGoal(goalId)
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoadingDetails = false,
                        error = "Failed to load goal details: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun loadProgressForGoal(goalId: String) {
        viewModelScope.launch {
            goalUseCase.getProgressForGoal(goalId).collect { progress ->
                _uiState.update { it.copy(selectedGoalProgress = progress) }
            }
        }
    }
    
    private fun loadMilestonesForGoal(goalId: String) {
        viewModelScope.launch {
            goalUseCase.getMilestonesForGoal(goalId).collect { milestones ->
                _uiState.update { it.copy(selectedGoalMilestones = milestones) }
            }
        }
    }
    
    fun createGoal(request: CreateGoalRequest) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isCreating = true, error = null) }
                
                val result = goalUseCase.createGoal(
                    userId = currentUserId,
                    type = request.type,
                    title = request.title,
                    description = request.description,
                    targetValue = request.targetValue,
                    unit = request.unit,
                    targetDate = request.targetDate,
                    priority = request.priority,
                    milestones = request.milestones
                )
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isCreating = false,
                            showCreateDialog = false
                        )
                    }
                    loadGoals()
                    loadGoalOverview()
                } else {
                    _uiState.update { 
                        it.copy(
                            isCreating = false,
                            error = "Failed to create goal: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isCreating = false,
                        error = "Failed to create goal: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun addProgress(goalId: String, value: Double, notes: String? = null) {
        viewModelScope.launch {
            try {
                val result = goalUseCase.addProgress(goalId, value, notes)
                
                if (result.isSuccess) {
                    // Refresh goal details and overview
                    loadGoalDetails(goalId)
                    loadGoalOverview()
                    
                    // Generate new prediction
                    goalUseCase.generatePrediction(goalId)
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to add progress: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to add progress: ${e.message}")
                }
            }
        }
    }
    
    fun addMilestone(goalId: String, request: CreateMilestoneRequest) {
        viewModelScope.launch {
            try {
                val result = goalUseCase.addMilestone(
                    goalId = goalId,
                    title = request.title,
                    description = request.description,
                    targetValue = request.targetValue,
                    targetDate = request.targetDate,
                    order = _uiState.value.selectedGoalMilestones.size + 1
                )
                
                if (result.isSuccess) {
                    loadMilestonesForGoal(goalId)
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to add milestone: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to add milestone: ${e.message}")
                }
            }
        }
    }
    
    fun completeMilestone(milestoneId: String) {
        viewModelScope.launch {
            try {
                val result = goalUseCase.completeMilestone(milestoneId)
                
                if (result.isSuccess) {
                    _selectedGoalId.value?.let { goalId ->
                        loadMilestonesForGoal(goalId)
                        loadGoalDetails(goalId)
                    }
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to complete milestone: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to complete milestone: ${e.message}")
                }
            }
        }
    }
    
    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            try {
                val result = goalUseCase.updateGoal(goal)
                
                if (result.isSuccess) {
                    loadGoals()
                    loadGoalDetails(goal.id)
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to update goal: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to update goal: ${e.message}")
                }
            }
        }
    }
    
    fun deactivateGoal(goalId: String) {
        viewModelScope.launch {
            try {
                val result = goalUseCase.deactivateGoal(goalId)
                
                if (result.isSuccess) {
                    loadGoals()
                    loadGoalOverview()
                    if (_selectedGoalId.value == goalId) {
                        _selectedGoalId.value = null
                        _uiState.update { 
                            it.copy(
                                selectedGoal = null,
                                selectedGoalStatistics = null,
                                selectedGoalPrediction = null,
                                selectedGoalProgress = emptyList(),
                                selectedGoalMilestones = emptyList()
                            )
                        }
                    }
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to deactivate goal: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to deactivate goal: ${e.message}")
                }
            }
        }
    }
    
    fun generatePrediction(goalId: String) {
        viewModelScope.launch {
            try {
                val result = goalUseCase.generatePrediction(goalId)
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(selectedGoalPrediction = result.getOrNull())
                    }
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to generate prediction: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to generate prediction: ${e.message}")
                }
            }
        }
    }
    
    fun loadGoalTemplates(category: GoalCategory) {
        viewModelScope.launch {
            try {
                val templates = goalUseCase.getGoalTemplates(category)
                _uiState.update { it.copy(goalTemplates = templates) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load templates: ${e.message}")
                }
            }
        }
    }
    
    fun filterGoalsByCategory(category: GoalCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }
    
    fun showCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show) }
    }
    
    fun showProgressDialog(show: Boolean) {
        _uiState.update { it.copy(showProgressDialog = show) }
    }
    
    fun showMilestoneDialog(show: Boolean) {
        _uiState.update { it.copy(showMilestoneDialog = show) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class GoalUiState(
    val activeGoals: List<GoalWithProgress> = emptyList(),
    val selectedCategory: GoalCategory? = null,
    val isLoading: Boolean = false,
    val isCreatingGoal: Boolean = false,
    val error: String? = null,
    val createdGoalId: String? = null
)

data class CreateGoalRequest(
    val type: GoalType,
    val title: String,
    val description: String?,
    val targetValue: Double,
    val unit: String,
    val targetDate: LocalDate,
    val priority: GoalPriority,
    val milestones: List<CreateMilestoneRequest>
)