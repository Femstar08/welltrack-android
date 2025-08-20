package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DietaryRestrictionsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DietaryRestrictionsUseCase @Inject constructor(
    private val dietaryRestrictionsRepository: DietaryRestrictionsRepository
) {
    
    // User Dietary Profile Management
    suspend fun getUserDietaryProfile(userId: String): Flow<DietaryProfile> {
        return dietaryRestrictionsRepository.getUserDietaryProfile(userId)
    }
    
    suspend fun updateDietaryProfile(
        userId: String,
        request: DietaryRestrictionsUpdateRequest
    ): Result<Unit> {
        return dietaryRestrictionsRepository.updateDietaryProfile(userId, request)
    }
    
    // Dietary Restrictions Management
    suspend fun getUserDietaryRestrictions(userId: String): Flow<List<UserDietaryRestriction>> {
        return dietaryRestrictionsRepository.getUserDietaryRestrictions(userId)
    }
    
    suspend fun addDietaryRestriction(
        userId: String,
        restrictionType: DietaryRestrictionType,
        severity: RestrictionSeverity = RestrictionSeverity.MODERATE,
        notes: String? = null
    ): Result<String> {
        val request = DietaryRestrictionRequest(
            restrictionType = restrictionType,
            severity = severity,
            notes = notes
        )
        return dietaryRestrictionsRepository.addDietaryRestriction(userId, request)
    }
    
    suspend fun removeDietaryRestriction(restrictionId: String): Result<Unit> {
        return dietaryRestrictionsRepository.removeDietaryRestriction(restrictionId)
    }
    
    // Allergy Management
    suspend fun getUserAllergies(userId: String): Flow<List<UserAllergy>> {
        return dietaryRestrictionsRepository.getUserAllergies(userId)
    }
    
    suspend fun addAllergy(
        userId: String,
        allergen: String,
        severity: AllergySeverity,
        symptoms: String? = null,
        notes: String? = null
    ): Result<String> {
        val request = AllergyRequest(
            allergen = allergen,
            severity = severity,
            symptoms = symptoms,
            notes = notes
        )
        return dietaryRestrictionsRepository.addAllergy(userId, request)
    }
    
    suspend fun removeAllergy(allergyId: String): Result<Unit> {
        return dietaryRestrictionsRepository.removeAllergy(allergyId)
    }
    
    // Food Preferences Management
    suspend fun getUserFoodPreferences(userId: String): Flow<List<UserFoodPreference>> {
        return dietaryRestrictionsRepository.getUserFoodPreferences(userId)
    }
    
    suspend fun getUserPreferencesByType(
        userId: String,
        type: FoodPreferenceType
    ): Flow<List<UserFoodPreference>> {
        return dietaryRestrictionsRepository.getUserPreferencesByType(userId, type)
    }
    
    suspend fun addFoodPreference(
        userId: String,
        preferenceType: FoodPreferenceType,
        item: String,
        preference: PreferenceLevel,
        notes: String? = null
    ): Result<String> {
        val request = FoodPreferenceRequest(
            preferenceType = preferenceType,
            item = item,
            preference = preference,
            notes = notes
        )
        return dietaryRestrictionsRepository.addFoodPreference(userId, request)
    }
    
    suspend fun removeFoodPreference(preferenceId: String): Result<Unit> {
        return dietaryRestrictionsRepository.removeFoodPreference(preferenceId)
    }
    
    // Automatic Tagging
    suspend fun tagMealWithDietaryInfo(mealId: String, meal: Meal): Result<Unit> {
        return dietaryRestrictionsRepository.tagMealWithDietaryInfo(mealId, meal)
    }
    
    suspend fun tagRecipeWithDietaryInfo(recipeId: String, recipe: Recipe): Result<Unit> {
        return dietaryRestrictionsRepository.tagRecipeWithDietaryInfo(recipeId, recipe)
    }
    
    // Compatibility Checking
    suspend fun checkRecipeCompatibility(userId: String, recipeId: String): Result<DietaryCompatibility> {
        return dietaryRestrictionsRepository.checkRecipeCompatibility(userId, recipeId)
    }
    
    suspend fun checkMealCompatibility(userId: String, mealId: String): Result<DietaryCompatibility> {
        return dietaryRestrictionsRepository.checkMealCompatibility(userId, mealId)
    }
    
    suspend fun getCompatibleRecipes(
        userId: String,
        minSeverity: RestrictionSeverity = RestrictionSeverity.MODERATE
    ): Result<List<Recipe>> {
        return dietaryRestrictionsRepository.getCompatibleRecipes(userId, minSeverity)
    }
    
    // Filtering and Analysis
    suspend fun filterRecipesByDietaryProfile(userId: String, recipes: List<Recipe>): Result<List<Recipe>> {
        return dietaryRestrictionsRepository.filterRecipesByDietaryProfile(userId, recipes)
    }
    
    suspend fun suggestIngredientSubstitutions(
        userId: String,
        ingredients: List<String>
    ): Result<Map<String, List<String>>> {
        return dietaryRestrictionsRepository.suggestIngredientSubstitutions(userId, ingredients)
    }
    
    suspend fun analyzeNutritionalAlignment(
        userId: String,
        nutritionInfo: NutritionInfo
    ): Result<List<DietaryWarning>> {
        return dietaryRestrictionsRepository.analyzeNutritionalAlignment(userId, nutritionInfo)
    }
    
    // Convenience methods for common operations
    suspend fun setupBasicDietaryProfile(
        userId: String,
        restrictions: List<DietaryRestrictionType>,
        allergies: List<String>,
        likedIngredients: List<String> = emptyList(),
        dislikedIngredients: List<String> = emptyList(),
        preferredCuisines: List<String> = emptyList()
    ): Result<Unit> {
        val restrictionRequests = restrictions.map { restriction ->
            DietaryRestrictionRequest(
                restrictionType = restriction,
                severity = RestrictionSeverity.MODERATE,
                notes = null
            )
        }
        
        val allergyRequests = allergies.map { allergen ->
            AllergyRequest(
                allergen = allergen,
                severity = AllergySeverity.MODERATE,
                symptoms = null,
                notes = null
            )
        }
        
        val preferenceRequests = mutableListOf<FoodPreferenceRequest>()
        
        likedIngredients.forEach { ingredient ->
            preferenceRequests.add(
                FoodPreferenceRequest(
                    preferenceType = FoodPreferenceType.INGREDIENT,
                    item = ingredient,
                    preference = PreferenceLevel.LIKE,
                    notes = null
                )
            )
        }
        
        dislikedIngredients.forEach { ingredient ->
            preferenceRequests.add(
                FoodPreferenceRequest(
                    preferenceType = FoodPreferenceType.INGREDIENT,
                    item = ingredient,
                    preference = PreferenceLevel.DISLIKE,
                    notes = null
                )
            )
        }
        
        preferredCuisines.forEach { cuisine ->
            preferenceRequests.add(
                FoodPreferenceRequest(
                    preferenceType = FoodPreferenceType.CUISINE,
                    item = cuisine,
                    preference = PreferenceLevel.LIKE,
                    notes = null
                )
            )
        }
        
        val updateRequest = DietaryRestrictionsUpdateRequest(
            restrictions = restrictionRequests,
            allergies = allergyRequests,
            preferences = preferenceRequests
        )
        
        return dietaryRestrictionsRepository.updateDietaryProfile(userId, updateRequest)
    }
    
    suspend fun getDietaryRestrictionsByCategory(userId: String): Result<Map<RestrictionCategory, List<UserDietaryRestriction>>> {
        return try {
            val restrictions = mutableListOf<UserDietaryRestriction>()
            dietaryRestrictionsRepository.getUserDietaryRestrictions(userId).collect { list ->
                restrictions.clear()
                restrictions.addAll(list)
            }
            
            val groupedRestrictions = restrictions.groupBy { it.restrictionType.category }
            Result.success(groupedRestrictions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHighSeverityRestrictions(userId: String): Result<List<UserDietaryRestriction>> {
        return try {
            val restrictions = mutableListOf<UserDietaryRestriction>()
            dietaryRestrictionsRepository.getUserDietaryRestrictions(userId).collect { list ->
                restrictions.clear()
                restrictions.addAll(list.filter { it.severity.level >= RestrictionSeverity.STRICT.level })
            }
            Result.success(restrictions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCriticalAllergies(userId: String): Result<List<UserAllergy>> {
        return try {
            val allergies = mutableListOf<UserAllergy>()
            dietaryRestrictionsRepository.getUserAllergies(userId).collect { list ->
                allergies.clear()
                allergies.addAll(list.filter { it.severity.level >= AllergySeverity.SEVERE.level })
            }
            Result.success(allergies)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}