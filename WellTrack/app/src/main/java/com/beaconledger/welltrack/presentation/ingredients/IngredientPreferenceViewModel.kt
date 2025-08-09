package com.beaconledger.welltrack.presentation.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.IngredientPreferenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IngredientPreferenceViewModel @Inject constructor(
    private val ingredientPreferenceUseCase: IngredientPreferenceUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IngredientPreferenceUiState())
    val uiState: StateFlow<IngredientPreferenceUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    fun setUserId(userId: String) {
        _currentUserId.value = userId
        loadData()
    }
    
    private fun loadData() {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // Load preferences
                ingredientPreferenceUseCase.getPreferencesForUser(userId).collect { preferences ->
                    _uiState.update { 
                        it.copy(
                            preferences = preferences,
                            preferredIngredients = preferences.filter { pref -> pref.preferenceType == PreferenceType.PREFERRED },
                            dislikedIngredients = preferences.filter { pref -> pref.preferenceType == PreferenceType.DISLIKED },
                            allergicIngredients = preferences.filter { pref -> pref.preferenceType == PreferenceType.ALLERGIC }
                        )
                    }
                }
                
                // Load pantry items
                ingredientPreferenceUseCase.getPantryItemsForUser(userId).collect { pantryItems ->
                    _uiState.update { it.copy(pantryItems = pantryItems) }
                }
                
                // Load suggestions
                val suggestions = ingredientPreferenceUseCase.getIngredientSuggestions(userId)
                val alerts = ingredientPreferenceUseCase.getPantryAlerts(userId)
                val mostUsed = ingredientPreferenceUseCase.getMostUsedIngredients(userId)
                
                _uiState.update { 
                    it.copy(
                        suggestions = suggestions,
                        pantryAlerts = alerts,
                        mostUsedIngredients = mostUsed,
                        isLoading = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message
                    )
                }
            }
        }
    }
    
    fun addPreferredIngredient(ingredientName: String, priority: Int = 5) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            ingredientPreferenceUseCase.addPreferredIngredient(userId, ingredientName, priority)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Added $ingredientName to preferred ingredients") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun addDislikedIngredient(ingredientName: String, notes: String? = null) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            ingredientPreferenceUseCase.addDislikedIngredient(userId, ingredientName, notes)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Added $ingredientName to disliked ingredients") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }  
  
    fun addAllergicIngredient(ingredientName: String, notes: String? = null) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            ingredientPreferenceUseCase.addAllergicIngredient(userId, ingredientName, notes)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Added $ingredientName to allergic ingredients") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun removePreference(ingredientName: String) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            ingredientPreferenceUseCase.removePreference(userId, ingredientName)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Removed $ingredientName preference") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun addPantryItem(
        ingredientName: String,
        quantity: Double,
        unit: String,
        category: IngredientCategory,
        expiryDate: String? = null,
        location: String? = null
    ) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            ingredientPreferenceUseCase.addPantryItem(
                userId, ingredientName, quantity, unit, category, expiryDate, location
            )
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Added $ingredientName to pantry") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun updatePantryItemQuantity(ingredientName: String, newQuantity: Double) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            ingredientPreferenceUseCase.updatePantryItemQuantity(userId, ingredientName, newQuantity)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Updated $ingredientName quantity") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun removePantryItem(ingredientName: String) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            ingredientPreferenceUseCase.removePantryItem(userId, ingredientName)
                .onSuccess {
                    _uiState.update { it.copy(successMessage = "Removed $ingredientName from pantry") }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
        }
    }
    
    fun searchIngredients(query: String) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            val results = ingredientPreferenceUseCase.searchIngredients(userId, query)
            _uiState.update { it.copy(searchResults = results) }
        }
    }
    
    fun clearMessages() {
        _uiState.update { 
            it.copy(
                errorMessage = null,
                successMessage = null
            )
        }
    }
    
    fun setSelectedTab(tab: IngredientTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    
    fun setShowAddDialog(show: Boolean) {
        _uiState.update { it.copy(showAddDialog = show) }
    }
    
    fun setShowPantryDialog(show: Boolean) {
        _uiState.update { it.copy(showPantryDialog = show) }
    }
}}


data class IngredientPreferenceUiState(
    val isLoading: Boolean = false,
    val preferences: List<IngredientPreference> = emptyList(),
    val preferredIngredients: List<IngredientPreference> = emptyList(),
    val dislikedIngredients: List<IngredientPreference> = emptyList(),
    val allergicIngredients: List<IngredientPreference> = emptyList(),
    val pantryItems: List<PantryItem> = emptyList(),
    val suggestions: List<IngredientSuggestion> = emptyList(),
    val pantryAlerts: List<PantryAlert> = emptyList(),
    val mostUsedIngredients: List<IngredientUsageStats> = emptyList(),
    val searchResults: List<String> = emptyList(),
    val selectedTab: IngredientTab = IngredientTab.PREFERENCES,
    val showAddDialog: Boolean = false,
    val showPantryDialog: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

enum class IngredientTab(val displayName: String) {
    PREFERENCES("Preferences"),
    PANTRY("Pantry"),
    SUGGESTIONS("Suggestions"),
    ANALYTICS("Analytics")
}