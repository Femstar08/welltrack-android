package com.beaconledger.welltrack.presentation.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecipeUiState())
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()

    private val _selectedRecipe = MutableStateFlow<Recipe?>(null)
    val selectedRecipe: StateFlow<Recipe?> = _selectedRecipe.asStateFlow()

    private val _selectedRecipeIngredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val selectedRecipeIngredients: StateFlow<List<Ingredient>> = _selectedRecipeIngredients.asStateFlow()

    init {
        loadRecipes()
    }

    fun loadRecipes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                recipeRepository.getAllRecipes().collect { recipeList ->
                    _recipes.value = recipeList
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load recipes: ${e.message}"
                )
            }
        }
    }

    fun loadRecipeById(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = recipeRepository.getRecipeById(recipeId)
                result.fold(
                    onSuccess = { recipe ->
                        _selectedRecipe.value = recipe
                        if (recipe != null) {
                            loadRecipeIngredients(recipeId)
                        }
                        _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load recipe: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load recipe: ${e.message}"
                )
            }
        }
    }

    private fun loadRecipeIngredients(recipeId: String) {
        viewModelScope.launch {
            try {
                val result = recipeRepository.getRecipeIngredients(recipeId)
                result.fold(
                    onSuccess = { ingredients ->
                        _selectedRecipeIngredients.value = ingredients
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to load ingredients: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load ingredients: ${e.message}"
                )
            }
        }
    }

    fun createRecipe(request: RecipeCreateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = recipeRepository.createRecipe(request)
                result.fold(
                    onSuccess = { recipeId ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            isRecipeCreated = true
                        )
                        loadRecipes() // Refresh the list
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to create recipe: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to create recipe: ${e.message}"
                )
            }
        }
    }

    fun updateRecipe(recipeId: String, request: RecipeUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = recipeRepository.updateRecipe(recipeId, request)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            isRecipeUpdated = true
                        )
                        loadRecipeById(recipeId) // Refresh the recipe
                        loadRecipes() // Refresh the list
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to update recipe: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to update recipe: ${e.message}"
                )
            }
        }
    }

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val result = recipeRepository.deleteRecipe(recipeId)
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null,
                            isRecipeDeleted = true
                        )
                        loadRecipes() // Refresh the list
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to delete recipe: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to delete recipe: ${e.message}"
                )
            }
        }
    }

    fun searchRecipes(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                recipeRepository.searchRecipes(query).collect { searchResults ->
                    _recipes.value = searchResults
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to search recipes: ${e.message}"
                )
            }
        }
    }

    fun getRecipesByRating(minRating: Float) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                recipeRepository.getRecipesByRating(minRating).collect { filteredRecipes ->
                    _recipes.value = filteredRecipes
                    _uiState.value = _uiState.value.copy(isLoading = false, error = null)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to filter recipes: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearRecipeCreated() {
        _uiState.value = _uiState.value.copy(isRecipeCreated = false)
    }

    fun clearRecipeUpdated() {
        _uiState.value = _uiState.value.copy(isRecipeUpdated = false)
    }

    fun clearRecipeDeleted() {
        _uiState.value = _uiState.value.copy(isRecipeDeleted = false)
    }

    fun clearSelectedRecipe() {
        _selectedRecipe.value = null
        _selectedRecipeIngredients.value = emptyList()
    }
}

data class RecipeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRecipeCreated: Boolean = false,
    val isRecipeUpdated: Boolean = false,
    val isRecipeDeleted: Boolean = false
)