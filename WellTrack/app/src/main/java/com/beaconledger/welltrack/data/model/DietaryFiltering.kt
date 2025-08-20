package com.beaconledger.welltrack.data.model

/**
 * Data models for dietary filtering and suggestions functionality
 */

// Meal Plan Filtering Models

data class FilteredMealPlan(
    val originalMealPlan: WeeklyMealPlan,
    val compatibleMeals: List<PlannedMeal>,
    val incompatibleMeals: List<IncompatibleMeal>,
    val warnings: List<String>,
    val overallCompatibilityScore: Float
)

data class IncompatibleMeal(
    val plannedMeal: PlannedMeal,
    val recipe: Recipe,
    val compatibility: DietaryCompatibility,
    val suggestedAlternatives: List<Recipe>
)

// Recipe Filtering Models

data class RecipeFilterCriteria(
    val minCompatibilityScore: Float = 0.7f,
    val minPreferenceScore: Float = 0.0f,
    val strictMode: Boolean = true, // If true, exclude any recipes with violations
    val includeWarnings: Boolean = true,
    val maxCookTime: Int? = null,
    val maxPrepTime: Int? = null,
    val preferredDifficulty: List<RecipeDifficulty> = emptyList(),
    val preferredCuisines: List<String> = emptyList()
)

data class FilteredRecipes(
    val compatibleRecipes: List<Recipe>,
    val incompatibleRecipes: List<IncompatibleRecipe>,
    val totalFiltered: Int,
    val compatibilityStats: CompatibilityStats
)

data class IncompatibleRecipe(
    val recipe: Recipe,
    val compatibility: DietaryCompatibility,
    val failedCriteria: List<String>
)

data class CompatibilityStats(
    val totalRecipes: Int,
    val compatibleCount: Int,
    val incompatibleCount: Int,
    val compatibilityRate: Float
)

// Recipe Import Validation Models

data class RecipeImportValidation(
    val recipe: Recipe,
    val compatibility: DietaryCompatibility,
    val canImport: Boolean,
    val suggestedSubstitutions: Map<String, List<IngredientSubstitution>>,
    val modifiedRecipe: Recipe? = null
)

data class IngredientSubstitution(
    val originalIngredient: String,
    val substituteIngredient: String,
    val substitutionRatio: String, // e.g., "1:1", "2:1", "1/2 cup per egg"
    val nutritionalImpact: NutritionalImpact,
    val confidenceLevel: Float, // 0.0 to 1.0
    val notes: String
)

data class NutritionalImpact(
    val calorieChange: Double, // Positive = increase, negative = decrease
    val proteinChange: Double,
    val carbChange: Double,
    val fatChange: Double,
    val fiberChange: Double = 0.0,
    val sodiumChange: Double = 0.0
)

// Shopping List Highlighting Models

data class HighlightedShoppingList(
    val originalList: ShoppingListWithItems,
    val highlightedItems: List<HighlightedShoppingItem>,
    val restrictedItemsCount: Int,
    val warningItemsCount: Int
)

data class HighlightedShoppingItem(
    val item: ShoppingListItem,
    val restrictions: List<DietaryViolation>,
    val warnings: List<DietaryWarning>,
    val highlightLevel: HighlightLevel,
    val suggestedAlternatives: List<String>
)

enum class HighlightLevel(val displayName: String, val colorCode: String) {
    NONE("No Issues", "#000000"),
    LOW("Minor Warning", "#FFA500"), // Orange
    MEDIUM("Caution", "#FF6B35"), // Red-Orange
    HIGH("Restriction Violation", "#FF0000"), // Red
    CRITICAL("Medical Alert", "#8B0000") // Dark Red
}

// Ingredient Substitution System Models

data class IngredientSubstitutionRequest(
    val userId: String,
    val ingredients: List<String>,
    val context: SubstitutionContext = SubstitutionContext.GENERAL
)

data class IngredientSubstitutionResponse(
    val substitutions: Map<String, List<IngredientSubstitution>>,
    val unavailableSubstitutions: List<String>, // Ingredients with no suitable substitutes
    val warnings: List<String>
)

enum class SubstitutionContext {
    GENERAL,
    BAKING,
    COOKING,
    SAUCE,
    DESSERT,
    PROTEIN,
    DAIRY_ALTERNATIVE
}

data class SubstitutionRule(
    val originalIngredient: String,
    val substitutes: List<SubstituteOption>,
    val context: SubstitutionContext,
    val restrictions: List<DietaryRestrictionType>,
    val allergens: List<String>
)

data class SubstituteOption(
    val ingredient: String,
    val ratio: String,
    val confidenceLevel: Float,
    val nutritionalImpact: NutritionalImpact,
    val notes: String,
    val worksBestFor: List<SubstitutionContext>
)

// Meal Plan Generation with Filtering

data class FilteredMealPlanRequest(
    val userId: String,
    val weekStartDate: String, // ISO 8601 date
    val preferences: MealPlanPreferences,
    val filterCriteria: RecipeFilterCriteria,
    val allowSubstitutions: Boolean = true,
    val maxSubstitutionsPerRecipe: Int = 3
)

data class FilteredMealPlanResponse(
    val success: Boolean,
    val mealPlan: FilteredMealPlan? = null,
    val appliedSubstitutions: Map<String, List<IngredientSubstitution>> = emptyMap(),
    val skippedMeals: List<PlannedMeal> = emptyList(), // Meals that couldn't be made compatible
    val warnings: List<String> = emptyList(),
    val error: String? = null
)

// Dietary Analysis Results

data class DietaryAnalysisResult(
    val compatibility: DietaryCompatibility,
    val nutritionalAlignment: NutritionalAlignment,
    val preferenceMatch: PreferenceMatch,
    val overallScore: Float
)

data class NutritionalAlignment(
    val meetsCalorieGoals: Boolean,
    val meetsMacroGoals: Boolean,
    val meetsRestrictionRequirements: Boolean,
    val nutritionalWarnings: List<String>,
    val suggestions: List<String>
)

data class PreferenceMatch(
    val score: Float, // 0.0 to 1.0
    val matchedPreferences: List<String>,
    val conflictingPreferences: List<String>,
    val neutralIngredients: List<String>
)

// Batch Processing Models

data class BatchFilterRequest(
    val userId: String,
    val recipes: List<Recipe>,
    val filterCriteria: RecipeFilterCriteria,
    val includeSubstitutions: Boolean = false
)

data class BatchFilterResponse(
    val processedRecipes: List<ProcessedRecipe>,
    val summary: BatchFilterSummary
)

data class ProcessedRecipe(
    val recipe: Recipe,
    val compatibility: DietaryCompatibility,
    val suggestedSubstitutions: Map<String, List<IngredientSubstitution>> = emptyMap(),
    val modifiedRecipe: Recipe? = null,
    val processingStatus: ProcessingStatus
)

enum class ProcessingStatus {
    COMPATIBLE,
    COMPATIBLE_WITH_SUBSTITUTIONS,
    INCOMPATIBLE_MINOR,
    INCOMPATIBLE_MAJOR,
    PROCESSING_ERROR
}

data class BatchFilterSummary(
    val totalRecipes: Int,
    val compatibleRecipes: Int,
    val compatibleWithSubstitutions: Int,
    val incompatibleRecipes: Int,
    val processingErrors: Int,
    val mostCommonViolations: List<String>,
    val mostCommonSubstitutions: List<String>
)

// User Preference Learning Models

data class PreferenceLearningData(
    val userId: String,
    val recipeInteractions: List<RecipeInteraction>,
    val ingredientFeedback: List<IngredientFeedback>,
    val mealRatings: List<MealRating>
)

data class RecipeInteraction(
    val recipeId: String,
    val interactionType: InteractionType,
    val timestamp: String,
    val context: String? = null
)

enum class InteractionType {
    VIEWED,
    SAVED,
    COOKED,
    RATED,
    SHARED,
    SKIPPED,
    SUBSTITUTED
}

data class IngredientFeedback(
    val ingredientName: String,
    val feedbackType: FeedbackType,
    val context: String,
    val timestamp: String
)

enum class FeedbackType {
    LIKED,
    DISLIKED,
    ALLERGIC_REACTION,
    SUBSTITUTED,
    AVOIDED
}

data class MealRating(
    val mealId: String,
    val recipeId: String?,
    val rating: Float, // 1.0 to 5.0
    val tags: List<String>, // e.g., "too salty", "perfect", "needs more spice"
    val timestamp: String
)