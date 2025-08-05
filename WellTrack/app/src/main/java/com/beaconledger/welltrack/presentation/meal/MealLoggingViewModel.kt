package com.beaconledger.welltrack.presentation.meal

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.meal.MealRecognitionResult
import com.beaconledger.welltrack.data.meal.MealScoringService
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import com.beaconledger.welltrack.domain.usecase.MealLoggingUseCase
import com.beaconledger.welltrack.domain.usecase.MealFromCameraResult
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class MealLoggingViewModel @Inject constructor(
    private val mealLoggingUseCase: MealLoggingUseCase,
    private val recipeRepository: RecipeRepository,
    private val mealScoringService: MealScoringService,
    private val gson: Gson
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MealLoggingUiState())
    val uiState: StateFlow<MealLoggingUiState> = _uiState.asStateFlow()
    
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    
    private val _todaysMeals = MutableStateFlow<List<Meal>>(emptyList())
    val todaysMeals: StateFlow<List<Meal>> = _todaysMeals.asStateFlow()
    
    init {
        loadRecipes()
        loadTodaysMeals()
    }
    
    fun setCurrentUserId(userId: String) {
        _uiState.update { it.copy(currentUserId = userId) }
        loadTodaysMeals()
    }
    
    fun setMealType(mealType: MealType) {
        _uiState.update { it.copy(selectedMealType = mealType) }
    }
    
    fun setPortions(portions: Float) {
        _uiState.update { it.copy(portions = portions) }
    }
    
    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    fun addIngredient(ingredient: Ingredient) {
        val currentIngredients = _uiState.value.manualIngredients.toMutableList()
        currentIngredients.add(ingredient)
        _uiState.update { it.copy(manualIngredients = currentIngredients) }
    }
    
    fun removeIngredient(index: Int) {
        val currentIngredients = _uiState.value.manualIngredients.toMutableList()
        if (index in currentIngredients.indices) {
            currentIngredients.removeAt(index)
            _uiState.update { it.copy(manualIngredients = currentIngredients) }
        }
    }
    
    fun clearIngredients() {
        _uiState.update { it.copy(manualIngredients = emptyList()) }
    }
    
    fun logManualMeal(mealName: String) {
        val state = _uiState.value
        if (state.currentUserId.isEmpty() || state.manualIngredients.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = mealLoggingUseCase.logManualMeal(
                userId = state.currentUserId,
                mealName = mealName,
                ingredients = state.manualIngredients,
                mealType = state.selectedMealType,
                portions = state.portions,
                notes = state.notes.takeIf { it.isNotBlank() }
            )
            
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        manualIngredients = emptyList(),
                        notes = "",
                        portions = 1.0f
                    )
                }
                loadTodaysMeals()
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to log meal"
                    )
                }
            }
        }
    }
    
    fun logMealFromRecipe(recipeId: String) {
        val state = _uiState.value
        if (state.currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = mealLoggingUseCase.logMealFromRecipe(
                userId = state.currentUserId,
                recipeId = recipeId,
                mealType = state.selectedMealType,
                portions = state.portions,
                notes = state.notes.takeIf { it.isNotBlank() }
            )
            
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        notes = "",
                        portions = 1.0f
                    )
                }
                loadTodaysMeals()
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to log meal"
                    )
                }
            }
        }
    }
    
    fun logMealFromCamera(context: Context, imageUri: Uri) {
        val state = _uiState.value
        if (state.currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = mealLoggingUseCase.logMealFromCamera(
                context = context,
                userId = state.currentUserId,
                imageUri = imageUri,
                mealType = state.selectedMealType,
                portions = state.portions,
                notes = state.notes.takeIf { it.isNotBlank() }
            )
            
            if (result.isSuccess) {
                val cameraResult = result.getOrNull()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        cameraRecognitionResult = cameraResult,
                        notes = "",
                        portions = 1.0f
                    )
                }
                loadTodaysMeals()
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to recognize meal"
                    )
                }
            }
        }
    }
    
    fun logMealFromBitmap(bitmap: Bitmap) {
        val state = _uiState.value
        if (state.currentUserId.isEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val result = mealLoggingUseCase.logMealFromBitmap(
                userId = state.currentUserId,
                bitmap = bitmap,
                mealType = state.selectedMealType,
                portions = state.portions,
                notes = state.notes.takeIf { it.isNotBlank() }
            )
            
            if (result.isSuccess) {
                val cameraResult = result.getOrNull()
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        cameraRecognitionResult = cameraResult,
                        notes = "",
                        portions = 1.0f
                    )
                }
                loadTodaysMeals()
            } else {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = result.exceptionOrNull()?.message ?: "Failed to recognize meal"
                    )
                }
            }
        }
    }
    
    fun updateMealStatus(mealId: String, status: MealStatus) {
        viewModelScope.launch {
            val result = mealLoggingUseCase.updateMealStatus(mealId, status)
            if (result.isSuccess) {
                loadTodaysMeals()
            }
        }
    }
    
    fun updateMealRating(mealId: String, rating: Float?) {
        val userId = _uiState.value.currentUserId
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            val result = mealLoggingUseCase.updateMealRating(mealId, userId, rating)
            if (result.isSuccess) {
                loadTodaysMeals()
            }
        }
    }
    
    fun toggleMealFavorite(mealId: String, isFavorite: Boolean) {
        val userId = _uiState.value.currentUserId
        if (userId.isEmpty()) return
        
        viewModelScope.launch {
            val result = mealLoggingUseCase.updateMealFavorite(mealId, userId, isFavorite)
            if (result.isSuccess) {
                loadTodaysMeals()
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun clearCameraResult() {
        _uiState.update { it.copy(cameraRecognitionResult = null) }
    }
    
    private fun loadRecipes() {
        viewModelScope.launch {
            recipeRepository.getAllRecipes().collect { recipes ->
                _recipes.value = recipes
            }
        }
    }
    
    private fun loadTodaysMeals() {
        val userId = _uiState.value.currentUserId
        if (userId.isEmpty()) return
        
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        viewModelScope.launch {
            mealLoggingUseCase.getMealsForDate(userId, today).collect { meals ->
                _todaysMeals.value = meals
            }
        }
    }
    
    fun getMealNutritionInfo(meal: Meal): NutritionInfo? {
        return try {
            gson.fromJson(meal.nutritionInfo, NutritionInfo::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getMealScoreBreakdown(nutritionInfo: NutritionInfo) = 
        mealScoringService.getMealScoreBreakdown(nutritionInfo)
}

data class MealLoggingUiState(
    val currentUserId: String = "",
    val selectedMealType: MealType = MealType.BREAKFAST,
    val portions: Float = 1.0f,
    val notes: String = "",
    val manualIngredients: List<Ingredient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val cameraRecognitionResult: MealFromCameraResult? = null
)