package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface DietaryRestrictionsRepository {
    
    // User Dietary Profile Management
    suspend fun getUserDietaryProfile(userId: String): Flow<DietaryProfile>
    suspend fun updateDietaryProfile(userId: String, request: DietaryRestrictionsUpdateRequest): Result<Unit>
    
    // Dietary Restrictions
    suspend fun getUserDietaryRestrictions(userId: String): Flow<List<UserDietaryRestriction>>
    suspend fun addDietaryRestriction(userId: String, request: DietaryRestrictionRequest): Result<String>
    suspend fun updateDietaryRestriction(restrictionId: String, request: DietaryRestrictionRequest): Result<Unit>
    suspend fun removeDietaryRestriction(restrictionId: String): Result<Unit>
    
    // Allergies
    suspend fun getUserAllergies(userId: String): Flow<List<UserAllergy>>
    suspend fun addAllergy(userId: String, request: AllergyRequest): Result<String>
    suspend fun updateAllergy(allergyId: String, request: AllergyRequest): Result<Unit>
    suspend fun removeAllergy(allergyId: String): Result<Unit>
    
    // Food Preferences
    suspend fun getUserFoodPreferences(userId: String): Flow<List<UserFoodPreference>>
    suspend fun getUserPreferencesByType(userId: String, type: FoodPreferenceType): Flow<List<UserFoodPreference>>
    suspend fun addFoodPreference(userId: String, request: FoodPreferenceRequest): Result<String>
    suspend fun updateFoodPreference(preferenceId: String, request: FoodPreferenceRequest): Result<Unit>
    suspend fun removeFoodPreference(preferenceId: String): Result<Unit>
    
    // Meal and Recipe Tagging
    suspend fun tagMealWithDietaryInfo(mealId: String, meal: Meal): Result<Unit>
    suspend fun tagRecipeWithDietaryInfo(recipeId: String, recipe: Recipe): Result<Unit>
    suspend fun getMealDietaryTags(mealId: String): Flow<List<MealDietaryTag>>
    suspend fun getRecipeDietaryTags(recipeId: String): Flow<List<RecipeDietaryTag>>
    
    // Compatibility Checking
    suspend fun checkRecipeCompatibility(userId: String, recipeId: String): Result<DietaryCompatibility>
    suspend fun checkMealCompatibility(userId: String, mealId: String): Result<DietaryCompatibility>
    suspend fun getCompatibleRecipes(userId: String, minSeverity: RestrictionSeverity = RestrictionSeverity.MODERATE): Result<List<Recipe>>
    
    // Filtering and Suggestions
    suspend fun filterRecipesByDietaryProfile(userId: String, recipes: List<Recipe>): Result<List<Recipe>>
    suspend fun filterRecipesWithCriteria(userId: String, recipes: List<Recipe>, criteria: RecipeFilterCriteria): Result<FilteredRecipes>
    suspend fun filterMealPlan(userId: String, mealPlan: WeeklyMealPlan, minCompatibilityScore: Float = 0.7f): Result<FilteredMealPlan>
    suspend fun validateRecipeImport(userId: String, recipe: Recipe): Result<RecipeImportValidation>
    suspend fun highlightShoppingListRestrictions(userId: String, shoppingList: ShoppingListWithItems): Result<HighlightedShoppingList>
    suspend fun suggestIngredientSubstitutions(userId: String, ingredients: List<String>): Result<Map<String, List<String>>>
    suspend fun generateIngredientSubstitutions(userId: String, recipe: Recipe): Result<Map<String, List<IngredientSubstitution>>>
    suspend fun analyzeNutritionalAlignment(userId: String, nutritionInfo: NutritionInfo): Result<List<DietaryWarning>>
}