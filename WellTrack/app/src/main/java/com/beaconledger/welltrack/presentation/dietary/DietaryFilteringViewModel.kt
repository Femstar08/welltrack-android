package com.beaconledger.welltrack.presentation.dietary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.DietaryFilteringUseCase
import com.beaconledger.welltrack.domain.usecase.DietaryRestrictionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DietaryFilteringViewModel @Inject constructor(
    private val dietaryFilteringUseCase: DietaryFilteringUseCase,
    private val dietaryRestrictionsUseCase: DietaryRestrictionsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DietaryFilteringUiState())
    val uiState: StateFlow<DietaryFilteringUiState> = _uiState.asStateFlow()
    
    private val _filteredRecipes = MutableStateFlow<FilteredRecipes?>(null)
    val filteredRecipes: StateFlow<FilteredRecipes?> = _filteredRecipes.asStateFlow()
    
    private val _filteredMealPlan = MutableStateFlow<FilteredMealPlan?>(null)
    val filteredMealPlan: StateFlow<FilteredMealPlan?> = _filteredMealPlan.asStateFlow()
    
    private val _highlightedShoppingList = MutableStateFlow<HighlightedShoppingList?>(null)
    val highlightedShoppingList: StateFlow<HighlightedShoppingList?> = _highlightedShoppingList.asStateFlow()
    
    private val _recipeImportValidation = MutableStateFlow<RecipeImportValidation?>(null)
    val recipeImportValidation: StateFlow<RecipeImportValidation?> = _recipeImportValidation.asStateFlow()
    
    private val _ingredientSubstitutions = MutableStateFlow<Map<String, List<IngredientSubstitution>>>(emptyMap())
    val ingredientSubstitutions: StateFlow<Map<String, List<IngredientSubstitution>>> = _ingredientSubstitutions.asStateFlow()
    
    fun filterRecipes(
        userId: String,
        recipeIds: List<String>? = null,
        filterCriteria: RecipeFilterCriteria = RecipeFilterCriteria()
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dietaryFilteringUseCase.filterRecipes(userId, recipeIds, filterCriteria)
                .fold(
                    onSuccess = { filtered ->
                        _filteredRecipes.value = filtered
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastOperation = "Recipe filtering completed"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to filter recipes"
                        )
                    }
                )
        }
    }
    
    fun filterMealPlan(
        userId: String,
        mealPlanId: String,
        minCompatibilityScore: Float = 0.7f
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dietaryFilteringUseCase.filterMealPlan(userId, mealPlanId, minCompatibilityScore)
                .fold(
                    onSuccess = { filtered ->
                        _filteredMealPlan.value = filtered
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastOperation = "Meal plan filtering completed"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to filter meal plan"
                        )
                    }
                )
        }
    }
    
    fun validateRecipeImport(userId: String, recipe: Recipe) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dietaryFilteringUseCase.validateRecipeImport(userId, recipe)
                .fold(
                    onSuccess = { validation ->
                        _recipeImportValidation.value = validation
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastOperation = "Recipe import validation completed"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to validate recipe import"
                        )
                    }
                )
        }
    }
    
    fun highlightShoppingListRestrictions(userId: String, shoppingListId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dietaryFilteringUseCase.highlightShoppingListRestrictions(userId, shoppingListId)
                .fold(
                    onSuccess = { highlighted ->
                        _highlightedShoppingList.value = highlighted
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastOperation = "Shopping list highlighting completed"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to highlight shopping list"
                        )
                    }
                )
        }
    }
    
    fun generateIngredientSubstitutions(userId: String, recipe: Recipe) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dietaryFilteringUseCase.generateIngredientSubstitutions(userId, recipe)
                .fold(
                    onSuccess = { substitutions ->
                        _ingredientSubstitutions.value = substitutions
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            lastOperation = "Ingredient substitutions generated"
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to generate substitutions"
                        )
                    }
                )
        }
    }
    
    fun updateFilterCriteria(criteria: RecipeFilterCriteria) {
        _uiState.value = _uiState.value.copy(filterCriteria = criteria)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearResults() {
        _filteredRecipes.value = null
        _filteredMealPlan.value = null
        _highlightedShoppingList.value = null
        _recipeImportValidation.value = null
        _ingredientSubstitutions.value = emptyMap()
        _uiState.value = _uiState.value.copy(lastOperation = null)
    }
}

data class DietaryFilteringUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastOperation: String? = null,
    val filterCriteria: RecipeFilterCriteria = RecipeFilterCriteria()
)