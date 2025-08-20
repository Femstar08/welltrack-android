package com.beaconledger.welltrack.presentation.macronutrients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.MacronutrientUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class MacronutrientViewModel @Inject constructor(
    private val macronutrientUseCase: MacronutrientUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MacronutrientUiState())
    val uiState: StateFlow<MacronutrientUiState> = _uiState.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentUserId = MutableStateFlow("") // This would come from user session
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    init {
        observeDailySummary()
        observeCustomNutrients()
    }

    fun setUserId(userId: String) {
        _currentUserId.value = userId
        observeDailySummary()
        observeCustomNutrients()
    }

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
        observeDailySummary()
    }

    private fun observeDailySummary() {
        viewModelScope.launch {
            combine(
                _currentUserId,
                _selectedDate
            ) { userId, date ->
                if (userId.isNotEmpty()) {
                    macronutrientUseCase.getDailySummary(userId, date)
                } else {
                    flowOf(null)
                }
            }.flatMapLatest { it ?: flowOf(null) }
                .collect { summary ->
                    _uiState.value = _uiState.value.copy(
                        dailySummary = summary,
                        isLoading = false
                    )
                }
        }
    }

    private fun observeCustomNutrients() {
        viewModelScope.launch {
            _currentUserId.flatMapLatest { userId ->
                if (userId.isNotEmpty()) {
                    macronutrientUseCase.getActiveCustomNutrients(userId)
                } else {
                    flowOf(emptyList())
                }
            }.collect { nutrients ->
                _uiState.value = _uiState.value.copy(customNutrients = nutrients)
            }
        }
    }

    fun logWaterIntake(waterMl: Int) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val date = _selectedDate.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.logWaterIntake(userId, date, waterMl)
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            message = "Water intake logged successfully"
                        )
                    }
                    .onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            error = error.message ?: "Failed to log water intake"
                        )
                    }
            }
        }
    }

    fun logManualNutrients(
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        water: Int,
        customNutrients: Map<String, Double> = emptyMap()
    ) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val date = _selectedDate.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.logManualNutrientIntake(
                    userId, date, calories, protein, carbs, fat, fiber, water, customNutrients
                ).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Nutrients logged successfully"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to log nutrients"
                    )
                }
            }
        }
    }

    fun setDailyTargets(
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        water: Int,
        customNutrients: Map<String, Double> = emptyMap()
    ) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val date = _selectedDate.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.setDailyTargets(
                    userId, date, calories, protein, carbs, fat, fiber, water, customNutrients
                ).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Daily targets updated successfully"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update targets"
                    )
                }
            }
        }
    }

    fun calculateProteinTarget(bodyWeight: Double, activityLevel: ActivityLevel, goal: FitnessGoal) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val date = _selectedDate.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.calculateAndSetProteinTarget(
                    userId, date, bodyWeight, activityLevel, goal
                ).onSuccess { proteinTarget ->
                    _uiState.value = _uiState.value.copy(
                        proteinTarget = proteinTarget,
                        message = "Protein target calculated: ${proteinTarget.totalTargetGrams.toInt()}g"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to calculate protein target"
                    )
                }
            }
        }
    }

    fun calculateFiberTarget(age: Int, gender: Gender) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val date = _selectedDate.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.calculateAndSetFiberTarget(
                    userId, date, age, gender
                ).onSuccess { fiberTarget ->
                    _uiState.value = _uiState.value.copy(
                        fiberTarget = fiberTarget,
                        message = "Fiber target calculated: ${fiberTarget.recommendedGrams.toInt()}g"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to calculate fiber target"
                    )
                }
            }
        }
    }

    fun calculateWaterTarget(bodyWeight: Double, activityLevel: ActivityLevel) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val date = _selectedDate.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.calculateAndSetWaterTarget(
                    userId, date, bodyWeight, activityLevel
                ).onSuccess { waterTarget ->
                    _uiState.value = _uiState.value.copy(
                        waterTarget = waterTarget,
                        message = "Water target calculated: ${waterTarget}ml"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to calculate water target"
                    )
                }
            }
        }
    }

    fun addCustomNutrient(
        name: String,
        unit: String,
        targetValue: Double?,
        category: NutrientCategory,
        priority: NutrientPriority = NutrientPriority.OPTIONAL
    ) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.addCustomNutrient(
                    userId, name, unit, targetValue, category, priority
                ).onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Custom nutrient '$name' added successfully"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to add custom nutrient"
                    )
                }
            }
        }
    }

    fun removeCustomNutrient(nutrientId: String) {
        viewModelScope.launch {
            macronutrientUseCase.removeCustomNutrient(nutrientId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Custom nutrient removed successfully"
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to remove custom nutrient"
                    )
                }
        }
    }

    fun loadNutrientTrends(nutrientName: String, days: Int = 7) {
        viewModelScope.launch {
            val userId = _currentUserId.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.getNutrientTrends(userId, nutrientName, days)
                    .collect { trend ->
                        _uiState.value = _uiState.value.copy(
                            nutrientTrends = _uiState.value.nutrientTrends + (nutrientName to trend)
                        )
                    }
            }
        }
    }

    fun loadMacronutrientBalance() {
        viewModelScope.launch {
            val userId = _currentUserId.value
            val date = _selectedDate.value
            
            if (userId.isNotEmpty()) {
                macronutrientUseCase.getMacronutrientBalance(userId, date)
                    .collect { balance ->
                        _uiState.value = _uiState.value.copy(macronutrientBalance = balance)
                    }
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun showTargetDialog() {
        _uiState.value = _uiState.value.copy(showTargetDialog = true)
    }

    fun hideTargetDialog() {
        _uiState.value = _uiState.value.copy(showTargetDialog = false)
    }

    fun showManualEntryDialog() {
        _uiState.value = _uiState.value.copy(showManualEntryDialog = true)
    }

    fun hideManualEntryDialog() {
        _uiState.value = _uiState.value.copy(showManualEntryDialog = false)
    }

    fun showCustomNutrientDialog() {
        _uiState.value = _uiState.value.copy(showCustomNutrientDialog = true)
    }

    fun hideCustomNutrientDialog() {
        _uiState.value = _uiState.value.copy(showCustomNutrientDialog = false)
    }
}

data class MacronutrientUiState(
    val dailySummary: MacronutrientSummary? = null,
    val customNutrients: List<CustomNutrient> = emptyList(),
    val proteinTarget: ProteinTarget? = null,
    val fiberTarget: FiberTarget? = null,
    val waterTarget: Int? = null,
    val nutrientTrends: Map<String, NutrientTrend> = emptyMap(),
    val macronutrientBalance: MacronutrientBalance? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null,
    val showTargetDialog: Boolean = false,
    val showManualEntryDialog: Boolean = false,
    val showCustomNutrientDialog: Boolean = false
)