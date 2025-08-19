package com.beaconledger.welltrack.presentation.mealprep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.*
import com.beaconledger.welltrack.domain.repository.LeftoverWasteAnalytics
import com.beaconledger.welltrack.domain.repository.MealPrepSchedule
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class MealPrepViewModel @Inject constructor(
    private val getMealPrepGuidanceUseCase: GetMealPrepGuidanceUseCase,
    private val createLeftoverUseCase: CreateLeftoverUseCase,
    private val getActiveLeftoversUseCase: GetActiveLeftoversUseCase,
    private val getLeftoverSuggestionsUseCase: GetLeftoverSuggestionsUseCase,
    private val markLeftoverConsumedUseCase: MarkLeftoverConsumedUseCase,
    private val getExpiringLeftoversUseCase: GetExpiringLeftoversUseCase,
    private val cleanupExpiredLeftoversUseCase: CleanupExpiredLeftoversUseCase,
    private val getStorageRecommendationsUseCase: GetStorageRecommendationsUseCase,
    private val getReheatingInstructionsUseCase: GetReheatingInstructionsUseCase,
    private val createLeftoverCombinationUseCase: CreateLeftoverCombinationUseCase,
    private val getLeftoverWasteAnalyticsUseCase: GetLeftoverWasteAnalyticsUseCase,
    private val getOptimalMealPrepScheduleUseCase: GetOptimalMealPrepScheduleUseCase,
    private val updateLeftoverUseCase: UpdateLeftoverUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MealPrepUiState())
    val uiState: StateFlow<MealPrepUiState> = _uiState.asStateFlow()

    private val _mealPrepGuidance = MutableStateFlow<MealPrepGuidanceResponse?>(null)
    val mealPrepGuidance: StateFlow<MealPrepGuidanceResponse?> = _mealPrepGuidance.asStateFlow()

    private val _leftoverSuggestions = MutableStateFlow<LeftoverSuggestionResponse?>(null)
    val leftoverSuggestions: StateFlow<LeftoverSuggestionResponse?> = _leftoverSuggestions.asStateFlow()

    private val _wasteAnalytics = MutableStateFlow<LeftoverWasteAnalytics?>(null)
    val wasteAnalytics: StateFlow<LeftoverWasteAnalytics?> = _wasteAnalytics.asStateFlow()

    private val _mealPrepSchedule = MutableStateFlow<MealPrepSchedule?>(null)
    val mealPrepSchedule: StateFlow<MealPrepSchedule?> = _mealPrepSchedule.asStateFlow()

    fun loadMealPrepGuidance(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getMealPrepGuidanceUseCase(recipeId)
                .onSuccess { guidance ->
                    _mealPrepGuidance.value = guidance
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun loadActiveLeftovers(userId: String) {
        viewModelScope.launch {
            getActiveLeftoversUseCase(userId).collect { leftovers ->
                _uiState.value = _uiState.value.copy(activeLeftovers = leftovers)
            }
        }
    }

    fun createLeftover(
        userId: String,
        mealId: String,
        recipeId: String?,
        name: String,
        quantity: Double,
        unit: String,
        storageLocation: StorageLocation,
        containerType: String,
        nutritionInfo: NutritionInfo,
        shelfLifeDays: Int = 3,
        notes: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            createLeftoverUseCase(
                userId, mealId, recipeId, name, quantity, unit,
                storageLocation, containerType, nutritionInfo, shelfLifeDays, notes
            )
                .onSuccess { leftoverId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Leftover saved successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun getLeftoverSuggestions(
        leftoverIds: List<String>,
        additionalIngredients: List<String> = emptyList(),
        maxPrepTime: Int = 30
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getLeftoverSuggestionsUseCase(leftoverIds, additionalIngredients, maxPrepTime)
                .onSuccess { suggestions ->
                    _leftoverSuggestions.value = suggestions
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun markLeftoverAsConsumed(leftoverId: String) {
        viewModelScope.launch {
            markLeftoverConsumedUseCase(leftoverId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Leftover marked as consumed"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = error.message
                    )
                }
        }
    }

    fun loadExpiringLeftovers(userId: String, days: Int = 2) {
        viewModelScope.launch {
            getExpiringLeftoversUseCase(userId, days)
                .onSuccess { expiring ->
                    _uiState.value = _uiState.value.copy(expiringLeftovers = expiring)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun cleanupExpiredLeftovers(userId: String) {
        viewModelScope.launch {
            cleanupExpiredLeftoversUseCase(userId)
                .onSuccess { count ->
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Cleaned up $count expired leftovers"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun loadStorageRecommendations(recipeId: String) {
        viewModelScope.launch {
            getStorageRecommendationsUseCase(recipeId)
                .onSuccess { recommendations ->
                    _uiState.value = _uiState.value.copy(storageRecommendations = recommendations)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun loadReheatingInstructions(leftoverId: String) {
        viewModelScope.launch {
            getReheatingInstructionsUseCase(leftoverId)
                .onSuccess { instructions ->
                    _uiState.value = _uiState.value.copy(reheatingInstructions = instructions)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun createLeftoverCombination(
        name: String,
        description: String,
        leftoverIds: List<String>,
        reheatingInstructions: List<ReheatingInstruction>,
        additionalIngredients: List<Ingredient> = emptyList(),
        prepTime: Int,
        servings: Int,
        nutritionInfo: NutritionInfo
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            createLeftoverCombinationUseCase(
                name, description, leftoverIds, reheatingInstructions,
                additionalIngredients, prepTime, servings, nutritionInfo
            )
                .onSuccess { combinationId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Leftover combination saved successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun loadWasteAnalytics(userId: String, days: Int = 30) {
        viewModelScope.launch {
            getLeftoverWasteAnalyticsUseCase(userId, days)
                .onSuccess { analytics ->
                    _wasteAnalytics.value = analytics
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun loadOptimalMealPrepSchedule(recipeIds: List<String>, targetDate: LocalDateTime = LocalDateTime.now().plusDays(1)) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getOptimalMealPrepScheduleUseCase(recipeIds, targetDate)
                .onSuccess { schedule ->
                    _mealPrepSchedule.value = schedule
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    fun updateLeftover(leftover: Leftover) {
        viewModelScope.launch {
            updateLeftoverUseCase(leftover)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Leftover updated successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(errorMessage = error.message)
                }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }
}

data class MealPrepUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val activeLeftovers: List<Leftover> = emptyList(),
    val expiringLeftovers: List<Leftover> = emptyList(),
    val storageRecommendations: List<ContainerType> = emptyList(),
    val reheatingInstructions: List<ReheatingInstruction> = emptyList()
)