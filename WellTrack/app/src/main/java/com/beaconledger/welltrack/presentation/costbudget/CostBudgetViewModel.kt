package com.beaconledger.welltrack.presentation.costbudget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.domain.usecase.CostBudgetUseCase
import com.beaconledger.welltrack.domain.usecase.ProfileContextUseCase
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class CostBudgetViewModel @Inject constructor(
    private val costBudgetUseCase: CostBudgetUseCase,
    private val profileContextUseCase: ProfileContextUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CostBudgetUiState())
    val uiState: StateFlow<CostBudgetUiState> = _uiState.asStateFlow()

    private val currentUserId = profileContextUseCase.currentUserId

    init {
        loadBudgetData()
    }

    private fun loadBudgetData() {
        viewModelScope.launch {
            currentUserId.collect { userId ->
                if (userId != null) {
                    loadBudgetStatus(userId)
                    loadCostAnalysis(userId)
                    loadBudgetAlerts(userId)
                    loadOptimizationSuggestions(userId)
                }
            }
        }
    }

    private suspend fun loadBudgetStatus(userId: String) {
        try {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val budgetStatus = costBudgetUseCase.getCurrentBudgetStatus(userId)
            
            _uiState.value = _uiState.value.copy(
                budgetStatus = budgetStatus,
                isLoading = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Failed to load budget status: ${e.message}"
            )
        }
    }

    private suspend fun loadCostAnalysis(userId: String) {
        try {
            val costAnalysis = costBudgetUseCase.getCostAnalysis(userId, 30)
            
            _uiState.value = _uiState.value.copy(
                costAnalysis = costAnalysis
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                error = "Failed to load cost analysis: ${e.message}"
            )
        }
    }

    private suspend fun loadBudgetAlerts(userId: String) {
        try {
            val alerts = costBudgetUseCase.checkBudgetAlerts(userId)
            
            _uiState.value = _uiState.value.copy(
                budgetAlerts = alerts
            )
        } catch (e: Exception) {
            // Don't show error for alerts, just log
        }
    }

    private suspend fun loadOptimizationSuggestions(userId: String) {
        try {
            val suggestions = costBudgetUseCase.getCostOptimizationSuggestions(userId)
            
            _uiState.value = _uiState.value.copy(
                optimizationSuggestions = suggestions
            )
        } catch (e: Exception) {
            // Don't show error for suggestions, just log
        }
    }

    fun setBudgetSettings(
        weeklyBudget: Double?,
        monthlyBudget: Double?,
        alertThreshold: Double = 0.8,
        enableAlerts: Boolean = true,
        currency: String = "USD"
    ) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    
                    val existingSettings = costBudgetUseCase.getBudgetSettings(userId)
                    
                    val result = if (existingSettings != null) {
                        costBudgetUseCase.updateBudgetSettings(
                            userId = userId,
                            weeklyBudget = weeklyBudget,
                            monthlyBudget = monthlyBudget,
                            alertThreshold = alertThreshold,
                            enableAlerts = enableAlerts,
                            currency = currency
                        )
                    } else {
                        costBudgetUseCase.createBudgetSettings(
                            userId = userId,
                            weeklyBudget = weeklyBudget,
                            monthlyBudget = monthlyBudget,
                            alertThreshold = alertThreshold,
                            enableAlerts = enableAlerts,
                            currency = currency
                        )
                    }
                    
                    if (result.isSuccess) {
                        loadBudgetStatus(userId)
                        _uiState.value = _uiState.value.copy(
                            showBudgetSettingsDialog = false,
                            successMessage = "Budget settings saved successfully"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to save budget settings: ${result.exceptionOrNull()?.message}"
                        )
                    }
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to save budget settings: ${e.message}"
                    )
                }
            }
        }
    }

    fun loadMealCostsForPeriod(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                costBudgetUseCase.getMealCostsForPeriod(userId, startDate, endDate)
                    .collect { mealCosts ->
                        _uiState.value = _uiState.value.copy(
                            mealCosts = mealCosts
                        )
                    }
            }
        }
    }

    fun compareRecipeCosts(recipeIds: List<String>) {
        viewModelScope.launch {
            try {
                val comparisons = costBudgetUseCase.compareRecipeCosts(recipeIds)
                
                _uiState.value = _uiState.value.copy(
                    recipeCostComparisons = comparisons
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to compare recipe costs: ${e.message}"
                )
            }
        }
    }

    fun estimateShoppingListCost(shoppingList: List<ShoppingListItem>) {
        viewModelScope.launch {
            try {
                val result = costBudgetUseCase.estimateShoppingListCost(shoppingList)
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        shoppingListCostEstimate = result.getOrNull()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to estimate shopping list cost: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to estimate shopping list cost: ${e.message}"
                )
            }
        }
    }

    fun updateIngredientPrice(
        ingredientName: String,
        price: Double,
        unit: String,
        storeId: String? = null,
        storeName: String? = null
    ) {
        viewModelScope.launch {
            try {
                val result = costBudgetUseCase.updateIngredientPrice(
                    ingredientName = ingredientName,
                    price = price,
                    unit = unit,
                    storeId = storeId,
                    storeName = storeName
                )
                
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        showPriceUpdateDialog = false,
                        successMessage = "Ingredient price updated successfully"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to update ingredient price: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to update ingredient price: ${e.message}"
                )
            }
        }
    }

    fun loadBudgetTrackingHistory(period: BudgetPeriod) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                costBudgetUseCase.getBudgetTrackingHistory(userId, period)
                    .collect { history ->
                        _uiState.value = _uiState.value.copy(
                            budgetTrackingHistory = history
                        )
                    }
            }
        }
    }

    fun showBudgetSettingsDialog() {
        _uiState.value = _uiState.value.copy(showBudgetSettingsDialog = true)
    }

    fun hideBudgetSettingsDialog() {
        _uiState.value = _uiState.value.copy(showBudgetSettingsDialog = false)
    }

    fun showPriceUpdateDialog() {
        _uiState.value = _uiState.value.copy(showPriceUpdateDialog = true)
    }

    fun hidePriceUpdateDialog() {
        _uiState.value = _uiState.value.copy(showPriceUpdateDialog = false)
    }

    fun dismissAlert(alertId: String) {
        _uiState.value = _uiState.value.copy(
            budgetAlerts = _uiState.value.budgetAlerts.filter { it.id != alertId }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun refreshData() {
        currentUserId.value?.let { userId ->
            viewModelScope.launch {
                loadBudgetStatus(userId)
                loadCostAnalysis(userId)
                loadBudgetAlerts(userId)
                loadOptimizationSuggestions(userId)
            }
        }
    }
}

data class CostBudgetUiState(
    val isLoading: Boolean = false,
    val budgetStatus: BudgetStatus? = null,
    val costAnalysis: CostAnalysis? = null,
    val budgetAlerts: List<BudgetAlert> = emptyList(),
    val optimizationSuggestions: List<CostOptimizationSuggestion> = emptyList(),
    val mealCosts: List<MealCost> = emptyList(),
    val recipeCostComparisons: List<RecipeCostComparison> = emptyList(),
    val shoppingListCostEstimate: ShoppingListCostEstimate? = null,
    val budgetTrackingHistory: List<BudgetTracking> = emptyList(),
    val showBudgetSettingsDialog: Boolean = false,
    val showPriceUpdateDialog: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)