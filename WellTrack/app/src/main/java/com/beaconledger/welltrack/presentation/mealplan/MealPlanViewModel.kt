package com.beaconledger.welltrack.presentation.mealplan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.MealPlanningUseCase
import com.beaconledger.welltrack.domain.usecase.MealPrepSchedule
import com.beaconledger.welltrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val mealPlanningUseCase: MealPlanningUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPlanUiState())
    val uiState: StateFlow<MealPlanUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentWeekStart = MutableStateFlow(getWeekStartDate(LocalDate.now()))
    val currentWeekStart: StateFlow<LocalDate> = _currentWeekStart.asStateFlow()

    init {
        loadCurrentWeekMealPlan()
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        val weekStart = getWeekStartDate(date)
        if (weekStart != _currentWeekStart.value) {
            _currentWeekStart.value = weekStart
            loadWeekMealPlan(weekStart)
        }
        loadDailyMealPlan(date)
    }

    fun navigateToWeek(weekStartDate: LocalDate) {
        _currentWeekStart.value = weekStartDate
        _selectedDate.value = weekStartDate
        loadWeekMealPlan(weekStartDate)
        loadDailyMealPlan(weekStartDate)
    }

    fun generateMealPlan(preferences: MealPlanPreferences? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)
            
            val userId = getCurrentUserId() // This would come from auth state
            val result = mealPlanningUseCase.generateWeeklyMealPlan(
                userId = userId,
                weekStartDate = _currentWeekStart.value,
                preferences = preferences
            )
            
            result.fold(
                onSuccess = { generationResult ->
                    if (generationResult.success && generationResult.mealPlan != null) {
                        _uiState.value = _uiState.value.copy(
                            weeklyMealPlan = generationResult.mealPlan,
                            isGenerating = false,
                            error = null
                        )
                        loadDailyMealPlan(_selectedDate.value)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            error = generationResult.error ?: "Failed to generate meal plan"
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = exception.message ?: "Failed to generate meal plan"
                    )
                }
            )
        }
    }

    fun regenerateMealPlan(preferences: MealPlanPreferences) {
        viewModelScope.launch {
            val currentPlan = _uiState.value.weeklyMealPlan
            if (currentPlan == null) {
                generateMealPlan(preferences)
                return@launch
            }

            _uiState.value = _uiState.value.copy(isGenerating = true, error = null)
            
            val result = mealPlanningUseCase.regenerateMealPlan(
                mealPlanId = currentPlan.mealPlan.id,
                preferences = preferences
            )
            
            result.fold(
                onSuccess = { generationResult ->
                    if (generationResult.success && generationResult.mealPlan != null) {
                        _uiState.value = _uiState.value.copy(
                            weeklyMealPlan = generationResult.mealPlan,
                            isGenerating = false,
                            error = null
                        )
                        loadDailyMealPlan(_selectedDate.value)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isGenerating = false,
                            error = generationResult.error ?: "Failed to regenerate meal plan"
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isGenerating = false,
                        error = exception.message ?: "Failed to regenerate meal plan"
                    )
                }
            )
        }
    }

    fun updatePlannedMeal(
        plannedMealId: String,
        newRecipeId: String? = null,
        customMealName: String? = null,
        servings: Int? = null,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val result = mealPlanningUseCase.updatePlannedMeal(
                plannedMealId = plannedMealId,
                newRecipeId = newRecipeId,
                customMealName = customMealName,
                servings = servings,
                notes = notes
            )
            
            result.fold(
                onSuccess = {
                    // Reload the current week and day
                    loadWeekMealPlan(_currentWeekStart.value)
                    loadDailyMealPlan(_selectedDate.value)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to update meal"
                    )
                }
            )
        }
    }

    fun addCustomMeal(
        date: LocalDate,
        mealType: MealType,
        customMealName: String,
        servings: Int = 1,
        notes: String? = null
    ) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val result = mealPlanningUseCase.addCustomMeal(
                userId = userId,
                date = date,
                mealType = mealType,
                customMealName = customMealName,
                servings = servings,
                notes = notes
            )
            
            result.fold(
                onSuccess = {
                    // Reload the current week and day
                    loadWeekMealPlan(_currentWeekStart.value)
                    loadDailyMealPlan(_selectedDate.value)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to add custom meal"
                    )
                }
            )
        }
    }

    fun markMealAsCompleted(plannedMealId: String) {
        viewModelScope.launch {
            val result = mealPlanningUseCase.markMealAsCompleted(plannedMealId)
            result.fold(
                onSuccess = {
                    loadDailyMealPlan(_selectedDate.value)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to mark meal as completed"
                    )
                }
            )
        }
    }

    fun markMealAsSkipped(plannedMealId: String) {
        viewModelScope.launch {
            val result = mealPlanningUseCase.markMealAsSkipped(plannedMealId)
            result.fold(
                onSuccess = {
                    loadDailyMealPlan(_selectedDate.value)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to mark meal as skipped"
                    )
                }
            )
        }
    }

    fun deletePlannedMeal(plannedMealId: String) {
        viewModelScope.launch {
            val result = mealPlanningUseCase.deletePlannedMeal(plannedMealId)
            result.fold(
                onSuccess = {
                    loadWeekMealPlan(_currentWeekStart.value)
                    loadDailyMealPlan(_selectedDate.value)
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to delete meal"
                    )
                }
            )
        }
    }

    fun showMealPrepSchedule() {
        viewModelScope.launch {
            val weeklyPlan = _uiState.value.weeklyMealPlan
            if (weeklyPlan != null) {
                val preferences = getMealPlanPreferences()
                val result = mealPlanningUseCase.optimizeMealPrepSchedule(
                    weeklyMealPlan = weeklyPlan,
                    mealPrepDays = preferences?.mealPrepDays ?: listOf("Sunday")
                )
                
                result.fold(
                    onSuccess = { schedule ->
                        _uiState.value = _uiState.value.copy(
                            mealPrepSchedule = schedule,
                            showMealPrepDialog = true
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = exception.message ?: "Failed to generate meal prep schedule"
                        )
                    }
                )
            }
        }
    }

    fun hideMealPrepSchedule() {
        _uiState.value = _uiState.value.copy(
            showMealPrepDialog = false,
            mealPrepSchedule = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun loadCurrentWeekMealPlan() {
        loadWeekMealPlan(_currentWeekStart.value)
        loadDailyMealPlan(_selectedDate.value)
    }

    private fun loadWeekMealPlan(weekStartDate: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val userId = getCurrentUserId()
            val result = mealPlanningUseCase.getWeeklyMealPlan(userId, weekStartDate)
            
            result.fold(
                onSuccess = { weeklyPlan ->
                    _uiState.value = _uiState.value.copy(
                        weeklyMealPlan = weeklyPlan,
                        isLoading = false,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Failed to load meal plan"
                    )
                }
            )
        }
    }

    private fun loadDailyMealPlan(date: LocalDate) {
        viewModelScope.launch {
            val userId = getCurrentUserId()
            val result = mealPlanningUseCase.getDailyMealPlan(userId, date)
            
            result.fold(
                onSuccess = { dailyPlan ->
                    _uiState.value = _uiState.value.copy(
                        dailyMealPlan = dailyPlan,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Failed to load daily meal plan"
                    )
                }
            )
        }
    }

    private suspend fun getMealPlanPreferences(): MealPlanPreferences? {
        val userId = getCurrentUserId()
        return mealPlanningUseCase.getMealPlanPreferences(userId).getOrNull()
    }

    private fun getCurrentUserId(): String {
        return authRepository.currentUser.value?.id ?: throw IllegalStateException("User not authenticated")
    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        val weekFields = WeekFields.of(Locale.getDefault())
        return date.with(weekFields.dayOfWeek(), 1)
    }
}

data class MealPlanUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val weeklyMealPlan: WeeklyMealPlan? = null,
    val dailyMealPlan: DailyMealPlan? = null,
    val mealPrepSchedule: MealPrepSchedule? = null,
    val showMealPrepDialog: Boolean = false,
    val error: String? = null
)