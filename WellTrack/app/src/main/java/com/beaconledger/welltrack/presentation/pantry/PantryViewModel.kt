package com.beaconledger.welltrack.presentation.pantry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.ExpiringItemWithSuggestions
import com.beaconledger.welltrack.domain.usecase.PantryUseCase
import com.beaconledger.welltrack.domain.usecase.PantryOverviewData
import com.beaconledger.welltrack.domain.usecase.ProfileContextUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val pantryUseCase: PantryUseCase,
    private val profileContextUseCase: ProfileContextUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState: StateFlow<PantryUiState> = _uiState.asStateFlow()
    
    private val currentUserId = profileContextUseCase.currentUserId
    
    init {
        loadPantryData()
    }
    
    private fun loadPantryData() {
        viewModelScope.launch {
            currentUserId.collect { userId ->
                if (userId != null) {
                    loadPantryItems(userId)
                    loadPantryOverview(userId)
                    loadPantryAlerts(userId)
                    loadExpiringItemsWithSuggestions(userId)
                }
            }
        }
    }
    
    private fun loadPantryItems(userId: String) {
        viewModelScope.launch {
            pantryUseCase.getPantryItemsForUser(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { items ->
                    _uiState.update { 
                        it.copy(
                            pantryItems = items,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    private fun loadPantryOverview(userId: String) {
        viewModelScope.launch {
            pantryUseCase.getPantryOverview(userId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message) }
                }
                .collect { overview ->
                    _uiState.update { it.copy(overview = overview) }
                }
        }
    }
    
    private fun loadPantryAlerts(userId: String) {
        viewModelScope.launch {
            try {
                val alerts = pantryUseCase.getPantryAlertsDetailed(userId)
                _uiState.update { it.copy(alerts = alerts) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    private fun loadExpiringItemsWithSuggestions(userId: String) {
        viewModelScope.launch {
            try {
                val expiringItems = pantryUseCase.getExpiringItemsWithRecipeSuggestions(userId)
                _uiState.update { it.copy(expiringItemsWithSuggestions = expiringItems) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    fun addPantryItem(request: PantryItemRequest) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                _uiState.update { it.copy(isLoading = true) }
                
                pantryUseCase.addPantryItem(userId, request)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false, error = null) }
                    }
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Failed to add item: ${e.message}"
                            )
                        }
                    }
            }
        }
    }
    
    fun addPantryItemByBarcode(barcode: String) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                _uiState.update { it.copy(isLoading = true) }
                
                pantryUseCase.addPantryItemByBarcode(userId, barcode)
                    .onSuccess { item ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = null,
                                message = "Added ${item.ingredientName} to pantry"
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false, 
                                error = "Failed to scan barcode: ${e.message}"
                            )
                        }
                    }
            }
        }
    }
    
    fun updateQuantity(ingredientName: String, newQuantity: Double) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                pantryUseCase.updateQuantity(userId, ingredientName, newQuantity)
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(error = "Failed to update quantity: ${e.message}")
                        }
                    }
            }
        }
    }
    
    fun editPantryItem(item: PantryItem) {
        _uiState.update { 
            it.copy(editingItem = item, showEditDialog = true)
        }
    }
    
    fun updatePantryItem(ingredientName: String, request: PantryUpdateRequest) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                pantryUseCase.updatePantryItem(userId, ingredientName, request)
                    .onSuccess {
                        _uiState.update { 
                            it.copy(
                                showEditDialog = false,
                                editingItem = null,
                                error = null
                            )
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(error = "Failed to update item: ${e.message}")
                        }
                    }
            }
        }
    }
    
    fun removePantryItem(ingredientName: String) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                pantryUseCase.deletePantryItem(userId, ingredientName)
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(error = "Failed to remove item: ${e.message}")
                        }
                    }
            }
        }
    }
    
    fun recordIngredientUsage(
        ingredientName: String,
        quantityUsed: Double,
        unit: String,
        recipeId: String? = null,
        mealId: String? = null,
        usageType: UsageType = UsageType.RECIPE
    ) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                pantryUseCase.recordIngredientUsage(
                    userId = userId,
                    ingredientName = ingredientName,
                    quantityUsed = quantityUsed,
                    unit = unit,
                    recipeId = recipeId,
                    mealId = mealId,
                    usageType = usageType
                ).onFailure { e ->
                    _uiState.update { 
                        it.copy(error = "Failed to record usage: ${e.message}")
                    }
                }
            }
        }
    }
    
    fun dismissAlert(alertId: String) {
        _uiState.update { state ->
            state.copy(
                alerts = state.alerts.filter { it.id != alertId }
            )
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    fun searchPantryItems(query: String) {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                try {
                    val results = pantryUseCase.searchPantryItems(userId, query)
                    _uiState.update { it.copy(searchResults = results) }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message) }
                }
            }
        }
    }
    
    fun cleanupExpiredItems() {
        viewModelScope.launch {
            currentUserId.value?.let { userId ->
                pantryUseCase.cleanupExpiredItems(userId)
                    .onSuccess { count ->
                        _uiState.update { 
                            it.copy(message = "Removed $count expired items")
                        }
                    }
                    .onFailure { e ->
                        _uiState.update { 
                            it.copy(error = "Failed to cleanup: ${e.message}")
                        }
                    }
            }
        }
    }
}

data class PantryUiState(
    val pantryItems: List<PantryItem> = emptyList(),
    val overview: PantryOverviewData? = null,
    val alerts: List<PantryAlert> = emptyList(),
    val expiringItemsWithSuggestions: List<ExpiringItemWithSuggestions> = emptyList(),
    val searchResults: List<PantryItem> = emptyList(),
    val editingItem: PantryItem? = null,
    val showEditDialog: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null
)