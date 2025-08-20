package com.beaconledger.welltrack.data.analysis

import com.beaconledger.welltrack.data.model.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for filtering meals, recipes, and shopping lists based on dietary restrictions and preferences
 */
@Singleton
class DietaryFilteringService @Inject constructor(
    private val dietaryAnalyzer: DietaryAnalyzer
) {
    
    /**
     * Filter meal plans based on user's dietary profile
     */
    suspend fun filterMealPlan(
        userId: String,
        mealPlan: WeeklyMealPlan,
        dietaryProfile: DietaryProfile,
        minCompatibilityScore: Float = 0.7f
    ): Result<FilteredMealPlan> {
        return try {
            val filteredMeals = mutableListOf<PlannedMeal>()
            val incompatibleMeals = mutableListOf<IncompatibleMeal>()
            val warnings = mutableListOf<String>()
            
            for (plannedMeal in mealPlan.plannedMeals) {
                val recipe = mealPlan.recipes.find { it.id == plannedMeal.recipeId }
                if (recipe != null) {
                    val compatibility = dietaryAnalyzer.analyzeRecipeCompatibility(recipe, dietaryProfile)
                    
                    if (compatibility.isCompatible && compatibility.score >= minCompatibilityScore) {
                        filteredMeals.add(plannedMeal)
                    } else {
                        incompatibleMeals.add(
                            IncompatibleMeal(
                                plannedMeal = plannedMeal,
                                recipe = recipe,
                                compatibility = compatibility,
                                suggestedAlternatives = findAlternativeRecipes(recipe, dietaryProfile)
                            )
                        )
                    }
                    
                    // Add warnings for minor compatibility issues
                    if (compatibility.warnings.isNotEmpty()) {
                        warnings.addAll(compatibility.warnings.map { "${recipe.name}: ${it.description}" })
                    }
                } else {
                    // Keep meals without recipes (custom meals)
                    filteredMeals.add(plannedMeal)
                }
            }
            
            Result.success(
                FilteredMealPlan(
                    originalMealPlan = mealPlan,
                    compatibleMeals = filteredMeals,
                    incompatibleMeals = incompatibleMeals,
                    warnings = warnings,
                    overallCompatibilityScore = calculateOverallCompatibility(filteredMeals.size, mealPlan.plannedMeals.size)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Filter recipes based on dietary restrictions and preferences
     */
    suspend fun filterRecipes(
        recipes: List<Recipe>,
        dietaryProfile: DietaryProfile,
        filterCriteria: RecipeFilterCriteria = RecipeFilterCriteria()
    ): Result<FilteredRecipes> {
        return try {
            val compatibleRecipes = mutableListOf<Recipe>()
            val incompatibleRecipes = mutableListOf<IncompatibleRecipe>()
            
            for (recipe in recipes) {
                val compatibility = dietaryAnalyzer.analyzeRecipeCompatibility(recipe, dietaryProfile)
                
                val meetsMinScore = compatibility.score >= filterCriteria.minCompatibilityScore
                val meetsRestrictions = filterCriteria.strictMode || compatibility.isCompatible
                val meetsPreferences = evaluatePreferenceMatch(recipe, dietaryProfile, filterCriteria)
                
                if (meetsMinScore && meetsRestrictions && meetsPreferences) {
                    compatibleRecipes.add(recipe)
                } else {
                    incompatibleRecipes.add(
                        IncompatibleRecipe(
                            recipe = recipe,
                            compatibility = compatibility,
                            failedCriteria = buildFailedCriteria(meetsMinScore, meetsRestrictions, meetsPreferences)
                        )
                    )
                }
            }
            
            // Sort compatible recipes by compatibility score and preference match
            val sortedRecipes = compatibleRecipes.sortedWith(
                compareByDescending<Recipe> { recipe ->
                    dietaryAnalyzer.analyzeRecipeCompatibility(recipe, dietaryProfile).score
                }.thenByDescending { recipe ->
                    calculatePreferenceScore(recipe, dietaryProfile)
                }
            )
            
            Result.success(
                FilteredRecipes(
                    compatibleRecipes = sortedRecipes,
                    incompatibleRecipes = incompatibleRecipes,
                    totalFiltered = recipes.size,
                    compatibilityStats = calculateCompatibilityStats(compatibleRecipes, incompatibleRecipes)
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate recipe import against dietary restrictions
     */
    suspend fun validateRecipeImport(
        recipe: Recipe,
        dietaryProfile: DietaryProfile
    ): Result<RecipeImportValidation> {
        return try {
            val compatibility = dietaryAnalyzer.analyzeRecipeCompatibility(recipe, dietaryProfile)
            val substitutions = if (!compatibility.isCompatible) {
                generateIngredientSubstitutions(recipe, dietaryProfile)
            } else {
                emptyMap()
            }
            
            Result.success(
                RecipeImportValidation(
                    recipe = recipe,
                    compatibility = compatibility,
                    canImport = compatibility.violations.none { it.severity == RestrictionSeverity.MEDICAL },
                    suggestedSubstitutions = substitutions,
                    modifiedRecipe = if (substitutions.isNotEmpty()) {
                        applySubstitutions(recipe, substitutions)
                    } else null
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Highlight restricted ingredients in shopping list
     */
    suspend fun highlightRestrictedIngredients(
        shoppingList: ShoppingListWithItems,
        dietaryProfile: DietaryProfile
    ): Result<HighlightedShoppingList> {
        return try {
            val highlightedItems = shoppingList.items.map { item ->
                val restrictions = findIngredientRestrictions(item.name, dietaryProfile)
                val warnings = findIngredientWarnings(item.name, dietaryProfile)
                
                HighlightedShoppingItem(
                    item = item,
                    restrictions = restrictions,
                    warnings = warnings,
                    highlightLevel = determineHighlightLevel(restrictions, warnings),
                    suggestedAlternatives = if (restrictions.isNotEmpty()) {
                        generateIngredientAlternatives(item.name, dietaryProfile)
                    } else emptyList()
                )
            }
            
            Result.success(
                HighlightedShoppingList(
                    originalList = shoppingList,
                    highlightedItems = highlightedItems,
                    restrictedItemsCount = highlightedItems.count { it.restrictions.isNotEmpty() },
                    warningItemsCount = highlightedItems.count { it.warnings.isNotEmpty() }
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Generate ingredient substitution suggestions
     */
    suspend fun generateIngredientSubstitutions(
        recipe: Recipe,
        dietaryProfile: DietaryProfile
    ): Map<String, List<IngredientSubstitution>> {
        val substitutions = mutableMapOf<String, List<IngredientSubstitution>>()
        
        try {
            val ingredients = parseIngredients(recipe.instructions) // Parse from JSON
            
            for (ingredient in ingredients) {
                val restrictions = findIngredientRestrictions(ingredient.name, dietaryProfile)
                if (restrictions.isNotEmpty()) {
                    val alternatives = generateIngredientAlternatives(ingredient.name, dietaryProfile)
                    if (alternatives.isNotEmpty()) {
                        substitutions[ingredient.name] = alternatives.map { alternative ->
                            IngredientSubstitution(
                                originalIngredient = ingredient.name,
                                substituteIngredient = alternative,
                                substitutionRatio = calculateSubstitutionRatio(ingredient.name, alternative),
                                nutritionalImpact = calculateNutritionalImpact(ingredient.name, alternative),
                                confidenceLevel = calculateSubstitutionConfidence(ingredient.name, alternative),
                                notes = generateSubstitutionNotes(ingredient.name, alternative)
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Log error but return empty map
        }
        
        return substitutions
    }
    
    // Private helper methods
    
    private suspend fun findAlternativeRecipes(
        originalRecipe: Recipe,
        dietaryProfile: DietaryProfile
    ): List<Recipe> {
        // This would typically query a recipe database for similar recipes
        // For now, return empty list - would be implemented with actual recipe repository
        return emptyList()
    }
    
    private fun calculateOverallCompatibility(compatibleCount: Int, totalCount: Int): Float {
        return if (totalCount > 0) compatibleCount.toFloat() / totalCount else 1.0f
    }
    
    private fun evaluatePreferenceMatch(
        recipe: Recipe,
        dietaryProfile: DietaryProfile,
        filterCriteria: RecipeFilterCriteria
    ): Boolean {
        val preferenceScore = calculatePreferenceScore(recipe, dietaryProfile)
        return preferenceScore >= filterCriteria.minPreferenceScore
    }
    
    private fun calculatePreferenceScore(recipe: Recipe, dietaryProfile: DietaryProfile): Float {
        var score = 0.0f
        var totalPreferences = 0
        
        val ingredients = parseIngredients(recipe.instructions)
        
        for (preference in dietaryProfile.preferences) {
            if (preference.preferenceType == FoodPreferenceType.INGREDIENT) {
                totalPreferences++
                val hasIngredient = ingredients.any { it.name.contains(preference.item, ignoreCase = true) }
                if (hasIngredient) {
                    score += preference.preference.score
                }
            }
        }
        
        return if (totalPreferences > 0) {
            (score / totalPreferences + 2) / 4 // Normalize to 0-1 range
        } else 0.5f
    }
    
    private fun buildFailedCriteria(
        meetsMinScore: Boolean,
        meetsRestrictions: Boolean,
        meetsPreferences: Boolean
    ): List<String> {
        val failed = mutableListOf<String>()
        if (!meetsMinScore) failed.add("Compatibility score too low")
        if (!meetsRestrictions) failed.add("Violates dietary restrictions")
        if (!meetsPreferences) failed.add("Poor preference match")
        return failed
    }
    
    private fun calculateCompatibilityStats(
        compatible: List<Recipe>,
        incompatible: List<IncompatibleRecipe>
    ): CompatibilityStats {
        val total = compatible.size + incompatible.size
        return CompatibilityStats(
            totalRecipes = total,
            compatibleCount = compatible.size,
            incompatibleCount = incompatible.size,
            compatibilityRate = if (total > 0) compatible.size.toFloat() / total else 0f
        )
    }
    
    private fun findIngredientRestrictions(
        ingredientName: String,
        dietaryProfile: DietaryProfile
    ): List<DietaryViolation> {
        val violations = mutableListOf<DietaryViolation>()
        
        // Check allergies
        for (allergy in dietaryProfile.allergies) {
            if (ingredientName.contains(allergy.allergen, ignoreCase = true)) {
                violations.add(
                    DietaryViolation(
                        type = ViolationType.ALLERGEN,
                        restriction = null,
                        allergen = allergy.allergen,
                        severity = when (allergy.severity) {
                            AllergySeverity.MILD -> RestrictionSeverity.MILD
                            AllergySeverity.MODERATE -> RestrictionSeverity.MODERATE
                            AllergySeverity.SEVERE -> RestrictionSeverity.STRICT
                            AllergySeverity.ANAPHYLAXIS -> RestrictionSeverity.MEDICAL
                        },
                        description = "Contains allergen: ${allergy.allergen}",
                        affectedIngredients = listOf(ingredientName)
                    )
                )
            }
        }
        
        // Check dietary restrictions
        for (restriction in dietaryProfile.restrictions) {
            if (violatesRestriction(ingredientName, restriction.restrictionType)) {
                violations.add(
                    DietaryViolation(
                        type = ViolationType.DIETARY_RESTRICTION,
                        restriction = restriction.restrictionType,
                        allergen = null,
                        severity = restriction.severity,
                        description = "Violates ${restriction.restrictionType.displayName} restriction",
                        affectedIngredients = listOf(ingredientName)
                    )
                )
            }
        }
        
        return violations
    }
    
    private fun findIngredientWarnings(
        ingredientName: String,
        dietaryProfile: DietaryProfile
    ): List<DietaryWarning> {
        val warnings = mutableListOf<DietaryWarning>()
        
        // Check strong dislikes
        for (preference in dietaryProfile.preferences) {
            if (preference.preferenceType == FoodPreferenceType.INGREDIENT &&
                preference.preference == PreferenceLevel.HATE &&
                ingredientName.contains(preference.item, ignoreCase = true)) {
                warnings.add(
                    DietaryWarning(
                        type = WarningType.PREFERENCE_MISMATCH,
                        description = "Contains strongly disliked ingredient: ${preference.item}",
                        suggestion = "Consider finding an alternative"
                    )
                )
            }
        }
        
        return warnings
    }
    
    private fun determineHighlightLevel(
        restrictions: List<DietaryViolation>,
        warnings: List<DietaryWarning>
    ): HighlightLevel {
        return when {
            restrictions.any { it.severity == RestrictionSeverity.MEDICAL } -> HighlightLevel.CRITICAL
            restrictions.any { it.severity == RestrictionSeverity.STRICT } -> HighlightLevel.HIGH
            restrictions.isNotEmpty() -> HighlightLevel.MEDIUM
            warnings.isNotEmpty() -> HighlightLevel.LOW
            else -> HighlightLevel.NONE
        }
    }
    
    private fun generateIngredientAlternatives(
        ingredientName: String,
        dietaryProfile: DietaryProfile
    ): List<String> {
        // This would typically use a comprehensive ingredient substitution database
        // For now, return basic substitutions based on common patterns
        return when {
            ingredientName.contains("milk", ignoreCase = true) -> 
                listOf("almond milk", "oat milk", "coconut milk", "soy milk")
            ingredientName.contains("butter", ignoreCase = true) -> 
                listOf("olive oil", "coconut oil", "vegan butter", "avocado oil")
            ingredientName.contains("egg", ignoreCase = true) -> 
                listOf("flax egg", "chia egg", "applesauce", "banana")
            ingredientName.contains("wheat flour", ignoreCase = true) -> 
                listOf("almond flour", "rice flour", "oat flour", "coconut flour")
            else -> emptyList()
        }
    }
    
    private fun violatesRestriction(ingredientName: String, restriction: DietaryRestrictionType): Boolean {
        return when (restriction) {
            DietaryRestrictionType.VEGETARIAN -> 
                ingredientName.contains("meat", ignoreCase = true) || 
                ingredientName.contains("chicken", ignoreCase = true) ||
                ingredientName.contains("beef", ignoreCase = true) ||
                ingredientName.contains("pork", ignoreCase = true) ||
                ingredientName.contains("fish", ignoreCase = true)
            DietaryRestrictionType.VEGAN -> 
                violatesRestriction(ingredientName, DietaryRestrictionType.VEGETARIAN) ||
                ingredientName.contains("milk", ignoreCase = true) ||
                ingredientName.contains("cheese", ignoreCase = true) ||
                ingredientName.contains("egg", ignoreCase = true) ||
                ingredientName.contains("honey", ignoreCase = true)
            DietaryRestrictionType.GLUTEN_FREE -> 
                ingredientName.contains("wheat", ignoreCase = true) ||
                ingredientName.contains("barley", ignoreCase = true) ||
                ingredientName.contains("rye", ignoreCase = true)
            DietaryRestrictionType.DAIRY_FREE -> 
                ingredientName.contains("milk", ignoreCase = true) ||
                ingredientName.contains("cheese", ignoreCase = true) ||
                ingredientName.contains("butter", ignoreCase = true) ||
                ingredientName.contains("cream", ignoreCase = true)
            else -> false
        }
    }
    
    private fun parseIngredients(instructionsJson: String): List<Ingredient> {
        // This would parse the JSON string to extract ingredients
        // For now, return empty list - would be implemented with actual JSON parsing
        return emptyList()
    }
    
    private fun calculateSubstitutionRatio(original: String, substitute: String): String {
        // Return 1:1 ratio by default - would be implemented with actual substitution ratios
        return "1:1"
    }
    
    private fun calculateNutritionalImpact(original: String, substitute: String): NutritionalImpact {
        // Would calculate actual nutritional differences
        return NutritionalImpact(
            calorieChange = 0.0,
            proteinChange = 0.0,
            carbChange = 0.0,
            fatChange = 0.0
        )
    }
    
    private fun calculateSubstitutionConfidence(original: String, substitute: String): Float {
        // Would calculate confidence based on substitution database
        return 0.8f
    }
    
    private fun generateSubstitutionNotes(original: String, substitute: String): String {
        return "Substitute $original with $substitute for dietary compatibility"
    }
    
    private fun applySubstitutions(
        recipe: Recipe,
        substitutions: Map<String, List<IngredientSubstitution>>
    ): Recipe {
        // Would create a modified recipe with substitutions applied
        // For now, return original recipe
        return recipe
    }
}