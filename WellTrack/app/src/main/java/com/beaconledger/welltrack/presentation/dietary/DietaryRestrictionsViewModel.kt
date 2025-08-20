package com.beaconledger.welltrack.presentation.dietary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.DietaryRestrictionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DietaryRestrictionsViewModel @Inject constructor(
    private val dietaryRestrictionsUseCase: DietaryRestrictionsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DietaryRestrictionsUiState())
    val uiState: StateFlow<DietaryRestrictionsUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    fun setUserId(userId: String) {
        _currentUserId.value = userId
        loadDietaryProfile(userId)
    }
    
    private fun loadDietaryProfile(userId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                dietaryRestrictionsUseCase.getUserDietaryProfile(userId).collect { profile ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            dietaryProfile = profile,
                            selectedRestrictions = profile.restrictions.map { it.restrictionType }.toSet(),
                            selectedAllergies = profile.allergies.map { it.allergen }.toSet(),
                            ingredientPreferences = profile.preferences
                                .filter { it.preferenceType == FoodPreferenceType.INGREDIENT }
                                .associate { it.item to it.preference },
                            cuisinePreferences = profile.preferences
                                .filter { it.preferenceType == FoodPreferenceType.CUISINE }
                                .associate { it.item to it.preference },
                            cookingMethodPreferences = profile.preferences
                                .filter { it.preferenceType == FoodPreferenceType.COOKING_METHOD }
                                .associate { it.item to it.preference }
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun toggleDietaryRestriction(restriction: DietaryRestrictionType) {
        val currentRestrictions = _uiState.value.selectedRestrictions
        val newRestrictions = if (currentRestrictions.contains(restriction)) {
            currentRestrictions - restriction
        } else {
            currentRestrictions + restriction
        }
        
        _uiState.update { it.copy(selectedRestrictions = newRestrictions) }
    }
    
    fun addAllergy(allergen: String, severity: AllergySeverity) {
        val currentAllergies = _uiState.value.selectedAllergies
        _uiState.update { 
            it.copy(
                selectedAllergies = currentAllergies + allergen,
                allergySeverities = it.allergySeverities + (allergen to severity)
            )
        }
    }
    
    fun removeAllergy(allergen: String) {
        val currentAllergies = _uiState.value.selectedAllergies
        val currentSeverities = _uiState.value.allergySeverities
        _uiState.update { 
            it.copy(
                selectedAllergies = currentAllergies - allergen,
                allergySeverities = currentSeverities - allergen
            )
        }
    }
    
    fun updateIngredientPreference(ingredient: String, preference: PreferenceLevel) {
        val currentPreferences = _uiState.value.ingredientPreferences
        _uiState.update { 
            it.copy(ingredientPreferences = currentPreferences + (ingredient to preference))
        }
    }
    
    fun removeIngredientPreference(ingredient: String) {
        val currentPreferences = _uiState.value.ingredientPreferences
        _uiState.update { 
            it.copy(ingredientPreferences = currentPreferences - ingredient)
        }
    }
    
    fun updateCuisinePreference(cuisine: String, preference: PreferenceLevel) {
        val currentPreferences = _uiState.value.cuisinePreferences
        _uiState.update { 
            it.copy(cuisinePreferences = currentPreferences + (cuisine to preference))
        }
    }
    
    fun removeCuisinePreference(cuisine: String) {
        val currentPreferences = _uiState.value.cuisinePreferences
        _uiState.update { 
            it.copy(cuisinePreferences = currentPreferences - cuisine)
        }
    }
    
    fun updateCookingMethodPreference(method: String, preference: PreferenceLevel) {
        val currentPreferences = _uiState.value.cookingMethodPreferences
        _uiState.update { 
            it.copy(cookingMethodPreferences = currentPreferences + (method to preference))
        }
    }
    
    fun removeCookingMethodPreference(method: String) {
        val currentPreferences = _uiState.value.cookingMethodPreferences
        _uiState.update { 
            it.copy(cookingMethodPreferences = currentPreferences - method)
        }
    }
    
    fun saveDietaryProfile() {
        val userId = _currentUserId.value ?: return
        val currentState = _uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            try {
                val restrictionRequests = currentState.selectedRestrictions.map { restriction ->
                    DietaryRestrictionRequest(
                        restrictionType = restriction,
                        severity = currentState.restrictionSeverities[restriction] ?: RestrictionSeverity.MODERATE,
                        notes = null
                    )
                }
                
                val allergyRequests = currentState.selectedAllergies.map { allergen ->
                    AllergyRequest(
                        allergen = allergen,
                        severity = currentState.allergySeverities[allergen] ?: AllergySeverity.MODERATE,
                        symptoms = null,
                        notes = null
                    )
                }
                
                val preferenceRequests = mutableListOf<FoodPreferenceRequest>()
                
                currentState.ingredientPreferences.forEach { (ingredient, preference) ->
                    preferenceRequests.add(
                        FoodPreferenceRequest(
                            preferenceType = FoodPreferenceType.INGREDIENT,
                            item = ingredient,
                            preference = preference,
                            notes = null
                        )
                    )
                }
                
                currentState.cuisinePreferences.forEach { (cuisine, preference) ->
                    preferenceRequests.add(
                        FoodPreferenceRequest(
                            preferenceType = FoodPreferenceType.CUISINE,
                            item = cuisine,
                            preference = preference,
                            notes = null
                        )
                    )
                }
                
                currentState.cookingMethodPreferences.forEach { (method, preference) ->
                    preferenceRequests.add(
                        FoodPreferenceRequest(
                            preferenceType = FoodPreferenceType.COOKING_METHOD,
                            item = method,
                            preference = preference,
                            notes = null
                        )
                    )
                }
                
                val updateRequest = DietaryRestrictionsUpdateRequest(
                    restrictions = restrictionRequests,
                    allergies = allergyRequests,
                    preferences = preferenceRequests
                )
                
                val result = dietaryRestrictionsUseCase.updateDietaryProfile(userId, updateRequest)
                
                if (result.isSuccess) {
                    _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                } else {
                    _uiState.update { 
                        it.copy(
                            isSaving = false, 
                            error = result.exceptionOrNull()?.message ?: "Failed to save dietary profile"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
    
    fun clearSaveSuccess() {
        _uiState.update { it.copy(saveSuccess = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun setRestrictionSeverity(restriction: DietaryRestrictionType, severity: RestrictionSeverity) {
        val currentSeverities = _uiState.value.restrictionSeverities
        _uiState.update { 
            it.copy(restrictionSeverities = currentSeverities + (restriction to severity))
        }
    }
    
    fun setAllergySeverity(allergen: String, severity: AllergySeverity) {
        val currentSeverities = _uiState.value.allergySeverities
        _uiState.update { 
            it.copy(allergySeverities = currentSeverities + (allergen to severity))
        }
    }
    
    fun getRestrictionsByCategory(): Map<RestrictionCategory, List<DietaryRestrictionType>> {
        return DietaryRestrictionType.values().groupBy { it.category }
    }
    
    fun checkRecipeCompatibility(recipeId: String) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            try {
                val result = dietaryRestrictionsUseCase.checkRecipeCompatibility(userId, recipeId)
                if (result.isSuccess) {
                    val compatibility = result.getOrNull()
                    _uiState.update { it.copy(lastCompatibilityCheck = compatibility) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

data class DietaryRestrictionsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val error: String? = null,
    val dietaryProfile: DietaryProfile? = null,
    val selectedRestrictions: Set<DietaryRestrictionType> = emptySet(),
    val restrictionSeverities: Map<DietaryRestrictionType, RestrictionSeverity> = emptyMap(),
    val selectedAllergies: Set<String> = emptySet(),
    val allergySeverities: Map<String, AllergySeverity> = emptyMap(),
    val ingredientPreferences: Map<String, PreferenceLevel> = emptyMap(),
    val cuisinePreferences: Map<String, PreferenceLevel> = emptyMap(),
    val cookingMethodPreferences: Map<String, PreferenceLevel> = emptyMap(),
    val lastCompatibilityCheck: DietaryCompatibility? = null
)