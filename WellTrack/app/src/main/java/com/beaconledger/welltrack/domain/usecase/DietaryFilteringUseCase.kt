package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.analysis.DietaryFilteringService
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DietaryRestrictionsRepository
import com.beaconledger.welltrack.domain.repository.MealPlanRepository
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import com.beaconledger.welltrack.domain.repository.ShoppingListRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for dietary filtering and suggestions functionality
 */
@Singleton
class DietaryFilteringUseCase @Inject constructor(
    private val dietaryFilteringService: DietaryFilteringService,
    private val dietaryRestrictionsRepository: DietaryRestrictionsRepository,
    private val recipeRepository: RecipeRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val shoppingListRepository: ShoppingListRepository
) {
    
    /**
     * Filter meal plan based on user's dietary restrictions and preferences
     */
    suspend fun filterMealPlan(
        userId: String,
        mealPlanId: String,
        minCompatibilityScore: Float = 0.7f
    ): Result<FilteredMealPlan> {
        return try {
            val mealPlan = mealPlanRepository.getWeeklyMealPlan(mealPlanId).first()
                ?: return Result.failure(Exception("Meal plan not found"))
            
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            
            dietaryFilteringService.filterMealPlan(userId, mealPlan, dietaryProfile, minCompatibilityScore)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Filter recipes based on dietary criteria
     */
    suspend fun filterRecipes(
        userId: String,
        recipeIds: List<String>? = null,
        filterCriteria: RecipeFilterCriteria = RecipeFilterCriteria()
    ): Result<FilteredRecipes> {
        return try {
            val recipes = if (recipeIds != null) {
                recipeRepository.getRecipesByIds(recipeIds)
            } else {
                recipeRepository.getAllRecipes(userId).first()
            }
            
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            
            dietaryFilteringService.filterRecipes(recipes, dietaryProfile, filterCriteria)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate recipe import against dietary restrictions
     */
    suspend fun validateRecipeImport(
        userId: String,
        recipe: Recipe
    ): Result<RecipeImportValidation> {
        return try {
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            
            dietaryFilteringService.validateRecipeImport(recipe, dietaryProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Highlight restricted ingredients in shopping list
     */
    suspend fun highlightShoppingListRestrictions(
        userId: String,
        shoppingListId: String
    ): Result<HighlightedShoppingList> {
        return try {
            val shoppingList = shoppingListRepository.getShoppingListWithItems(shoppingListId)
                ?: return Result.failure(Exception("Shopping list not found"))
            
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            
            dietaryFilteringService.highlightRestrictedIngredients(shoppingList, dietaryProfile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate ingredient substitutions for a recipe
     */
    suspend fun generateIngredientSubstitutions(
        userId: String,
        recipe: Recipe
    ): Result<Map<String, List<IngredientSubstitution>>> {
        return try {
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            
            Result.success(dietaryFilteringService.generateIngredientSubstitutions(recipe, dietaryProfile))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get compatible recipes for meal planning
     */
    suspend fun getCompatibleRecipesForMealPlanning(
        userId: String,
        mealType: MealType,
        maxResults: Int = 20,
        filterCriteria: RecipeFilterCriteria = RecipeFilterCriteria()
    ): Result<List<Recipe>> {
        return try {
            val allRecipes = recipeRepository.getAllRecipes(userId).first()
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            
            val filteredResult = dietaryFilteringService.filterRecipes(allRecipes, dietaryProfile, filterCriteria)
            
            filteredResult.fold(
                onSuccess = { filtered ->
                    // Filter by meal type if needed and limit results
                    val compatibleRecipes = filtered.compatibleRecipes
                        .take(maxResults)
                    
                    Result.success(compatibleRecipes)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Analyze dietary compatibility for a batch of recipes
     */
    suspend fun batchAnalyzeRecipes(
        userId: String,
        recipes: List<Recipe>,
        includeSubstitutions: Boolean = false
    ): Result<BatchFilterResponse> {
        return try {
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            val processedRecipes = mutableListOf<ProcessedRecipe>()
            var compatibleCount = 0
            var compatibleWithSubstitutionsCount = 0
            var incompatibleCount = 0
            var errorCount = 0
            
            for (recipe in recipes) {
                try {
                    val compatibility = dietaryFilteringService.validateRecipeImport(recipe, dietaryProfile)
                    
                    compatibility.fold(
                        onSuccess = { validation ->
                            val status = when {
                                validation.compatibility.isCompatible -> {
                                    compatibleCount++
                                    ProcessingStatus.COMPATIBLE
                                }
                                validation.suggestedSubstitutions.isNotEmpty() -> {
                                    compatibleWithSubstitutionsCount++
                                    ProcessingStatus.COMPATIBLE_WITH_SUBSTITUTIONS
                                }
                                validation.compatibility.violations.any { it.severity == RestrictionSeverity.MEDICAL } -> {
                                    incompatibleCount++
                                    ProcessingStatus.INCOMPATIBLE_MAJOR
                                }
                                else -> {
                                    incompatibleCount++
                                    ProcessingStatus.INCOMPATIBLE_MINOR
                                }
                            }
                            
                            processedRecipes.add(
                                ProcessedRecipe(
                                    recipe = recipe,
                                    compatibility = validation.compatibility,
                                    suggestedSubstitutions = if (includeSubstitutions) validation.suggestedSubstitutions else emptyMap(),
                                    modifiedRecipe = validation.modifiedRecipe,
                                    processingStatus = status
                                )
                            )
                        },
                        onFailure = {
                            errorCount++
                            processedRecipes.add(
                                ProcessedRecipe(
                                    recipe = recipe,
                                    compatibility = DietaryCompatibility(false, emptyList(), emptyList(), 0f),
                                    processingStatus = ProcessingStatus.PROCESSING_ERROR
                                )
                            )
                        }
                    )
                } catch (e: Exception) {
                    errorCount++
                    processedRecipes.add(
                        ProcessedRecipe(
                            recipe = recipe,
                            compatibility = DietaryCompatibility(false, emptyList(), emptyList(), 0f),
                            processingStatus = ProcessingStatus.PROCESSING_ERROR
                        )
                    )
                }
            }
            
            val summary = BatchFilterSummary(
                totalRecipes = recipes.size,
                compatibleRecipes = compatibleCount,
                compatibleWithSubstitutions = compatibleWithSubstitutionsCount,
                incompatibleRecipes = incompatibleCount,
                processingErrors = errorCount,
                mostCommonViolations = extractCommonViolations(processedRecipes),
                mostCommonSubstitutions = extractCommonSubstitutions(processedRecipes)
            )
            
            Result.success(
                BatchFilterResponse(
                    processedRecipes = processedRecipes,
                    summary = summary
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get dietary filtering statistics for user
     */
    suspend fun getDietaryFilteringStats(userId: String): Result<DietaryFilteringStats> {
        return try {
            val dietaryProfile = dietaryRestrictionsRepository.getUserDietaryProfile(userId).first()
            val allRecipes = recipeRepository.getAllRecipes(userId).first()
            
            val filteredResult = dietaryFilteringService.filterRecipes(
                allRecipes, 
                dietaryProfile, 
                RecipeFilterCriteria()
            )
            
            filteredResult.fold(
                onSuccess = { filtered ->
                    Result.success(
                        DietaryFilteringStats(
                            totalRecipes = allRecipes.size,
                            compatibleRecipes = filtered.compatibleRecipes.size,
                            incompatibleRecipes = filtered.incompatibleRecipes.size,
                            compatibilityRate = filtered.compatibilityStats.compatibilityRate,
                            activeRestrictions = dietaryProfile.restrictions.count { it.isActive },
                            activeAllergies = dietaryProfile.allergies.count { it.isActive },
                            preferenceCount = dietaryProfile.preferences.size,
                            mostCommonViolations = filtered.incompatibleRecipes
                                .flatMap { it.compatibility.violations }
                                .groupBy { it.description }
                                .mapValues { it.value.size }
                                .toList()
                                .sortedByDescending { it.second }
                                .take(5)
                                .map { it.first }
                        )
                    )
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Private helper methods
    
    private fun extractCommonViolations(processedRecipes: List<ProcessedRecipe>): List<String> {
        return processedRecipes
            .flatMap { it.compatibility.violations }
            .groupBy { it.description }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
    }
    
    private fun extractCommonSubstitutions(processedRecipes: List<ProcessedRecipe>): List<String> {
        return processedRecipes
            .flatMap { it.suggestedSubstitutions.keys }
            .groupBy { it }
            .mapValues { it.value.size }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
    }
}

/**
 * Statistics for dietary filtering
 */
data class DietaryFilteringStats(
    val totalRecipes: Int,
    val compatibleRecipes: Int,
    val incompatibleRecipes: Int,
    val compatibilityRate: Float,
    val activeRestrictions: Int,
    val activeAllergies: Int,
    val preferenceCount: Int,
    val mostCommonViolations: List<String>
)