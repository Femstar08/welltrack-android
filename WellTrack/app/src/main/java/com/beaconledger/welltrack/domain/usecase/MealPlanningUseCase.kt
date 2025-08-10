package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanningUseCase @Inject constructor(
    private val mealPlanRepository: MealPlanRepository
) {

    fun getMealPlansByUser(userId: String): Flow<List<MealPlan>> {
        return mealPlanRepository.getMealPlansByUser(userId)
    }

    suspend fun getWeeklyMealPlan(userId: String, date: LocalDate): Result<WeeklyMealPlan?> {
        val weekStartDate = getWeekStartDate(date)
        return mealPlanRepository.getWeeklyMealPlan(userId, weekStartDate)
    }

    suspend fun getDailyMealPlan(userId: String, date: LocalDate): Result<DailyMealPlan> {
        return mealPlanRepository.getDailyMealPlan(userId, date)
    }

    suspend fun generateWeeklyMealPlan(
        userId: String,
        weekStartDate: LocalDate,
        preferences: MealPlanPreferences? = null
    ): Result<MealPlanGenerationResult> {
        val finalPreferences = preferences ?: run {
            // Get user's saved preferences or use defaults
            mealPlanRepository.getMealPlanPreferences(userId).getOrNull() ?: MealPlanPreferences()
        }

        val request = MealPlanGenerationRequest(
            userId = userId,
            weekStartDate = weekStartDate,
            preferences = finalPreferences
        )

        return mealPlanRepository.generateWeeklyMealPlan(request)
    }

    suspend fun regenerateMealPlan(
        mealPlanId: String,
        preferences: MealPlanPreferences
    ): Result<MealPlanGenerationResult> {
        return mealPlanRepository.regenerateMealPlan(mealPlanId, preferences)
    }

    suspend fun updatePlannedMeal(
        plannedMealId: String,
        newRecipeId: String? = null,
        customMealName: String? = null,
        servings: Int? = null,
        notes: String? = null
    ): Result<Unit> {
        return try {
            val existingMeal = mealPlanRepository.getPlannedMealById(plannedMealId).getOrNull()
                ?: return Result.failure(Exception("Planned meal not found"))

            val updatedMeal = existingMeal.copy(
                recipeId = newRecipeId ?: existingMeal.recipeId,
                customMealName = customMealName ?: existingMeal.customMealName,
                servings = servings ?: existingMeal.servings,
                notes = notes ?: existingMeal.notes
            )

            mealPlanRepository.updatePlannedMeal(updatedMeal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun replacePlannedMeal(
        plannedMealId: String,
        newRecipeId: String
    ): Result<Unit> {
        return updatePlannedMeal(plannedMealId, newRecipeId = newRecipeId)
    }

    suspend fun addCustomMeal(
        userId: String,
        date: LocalDate,
        mealType: MealType,
        customMealName: String,
        servings: Int = 1,
        notes: String? = null
    ): Result<String> {
        return try {
            // Get or create meal plan for the week
            val weekStartDate = getWeekStartDate(date)
            val existingPlan = mealPlanRepository.getWeeklyMealPlan(userId, weekStartDate).getOrNull()
            
            val mealPlanId = if (existingPlan != null) {
                existingPlan.mealPlan.id
            } else {
                // Create a new meal plan for this week
                val newMealPlan = MealPlan(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    weekStartDate = weekStartDate.toString(),
                    weekEndDate = weekStartDate.plusDays(6).toString(),
                    isActive = true
                )
                mealPlanRepository.createMealPlan(newMealPlan).getOrThrow()
            }

            val plannedMeal = PlannedMeal(
                id = UUID.randomUUID().toString(),
                mealPlanId = mealPlanId,
                userId = userId,
                date = date.toString(),
                mealType = mealType,
                customMealName = customMealName,
                servings = servings,
                notes = notes
            )

            mealPlanRepository.createPlannedMeal(plannedMeal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markMealAsCompleted(plannedMealId: String): Result<Unit> {
        return mealPlanRepository.markMealAsCompleted(plannedMealId)
    }

    suspend fun markMealAsSkipped(plannedMealId: String): Result<Unit> {
        return mealPlanRepository.markMealAsSkipped(plannedMealId)
    }

    suspend fun deletePlannedMeal(plannedMealId: String): Result<Unit> {
        return mealPlanRepository.deletePlannedMeal(plannedMealId)
    }

    suspend fun getMealPlanAdherence(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Double> {
        return mealPlanRepository.getMealPlanAdherence(userId, startDate, endDate)
    }

    suspend fun getWeeklyNutritionSummary(
        userId: String,
        weekStartDate: LocalDate
    ): Result<NutritionInfo> {
        return mealPlanRepository.getWeeklyNutritionSummary(userId, weekStartDate)
    }

    suspend fun saveMealPlanPreferences(
        userId: String,
        preferences: MealPlanPreferences
    ): Result<Unit> {
        return mealPlanRepository.saveMealPlanPreferences(userId, preferences)
    }

    suspend fun getMealPlanPreferences(userId: String): Result<MealPlanPreferences?> {
        return mealPlanRepository.getMealPlanPreferences(userId)
    }

    // Meal prep scheduling functions
    suspend fun optimizeMealPrepSchedule(
        weeklyMealPlan: WeeklyMealPlan,
        mealPrepDays: List<String>
    ): Result<MealPrepSchedule> {
        return try {
            val schedule = generateMealPrepSchedule(weeklyMealPlan, mealPrepDays)
            Result.success(schedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateMealPrepSchedule(
        weeklyMealPlan: WeeklyMealPlan,
        mealPrepDays: List<String>
    ): MealPrepSchedule {
        // Group recipes by prep requirements and cooking methods
        val recipeGroups = weeklyMealPlan.recipes.groupBy { recipe ->
            // Simple grouping by cooking method - can be enhanced
            val recipeTags = recipe.tags.split(",").map { it.trim() }
            when {
                recipeTags.any { it.contains("bake", ignoreCase = true) } -> "Baking"
                recipeTags.any { it.contains("grill", ignoreCase = true) } -> "Grilling"
                recipeTags.any { it.contains("slow", ignoreCase = true) } -> "Slow Cooking"
                else -> "Stovetop"
            }
        }

        val prepTasks = mutableListOf<MealPrepTask>()
        
        // Create prep tasks for each group
        recipeGroups.forEach { (method, recipes) ->
            val totalPrepTime = recipes.sumOf { it.prepTime }
            val totalCookTime = recipes.sumOf { it.cookTime }
            
            prepTasks.add(
                MealPrepTask(
                    id = UUID.randomUUID().toString(),
                    name = "$method Prep Session",
                    recipes = recipes,
                    estimatedTime = totalPrepTime + totalCookTime,
                    cookingMethod = method,
                    suggestedDay = mealPrepDays.firstOrNull() ?: "Sunday",
                    priority = when (method) {
                        "Slow Cooking" -> MealPrepPriority.HIGH
                        "Baking" -> MealPrepPriority.MEDIUM
                        else -> MealPrepPriority.LOW
                    }
                )
            )
        }

        return MealPrepSchedule(
            weekStartDate = LocalDate.parse(weeklyMealPlan.mealPlan.weekStartDate),
            prepTasks = prepTasks.sortedBy { it.priority.ordinal },
            totalEstimatedTime = prepTasks.sumOf { it.estimatedTime },
            recommendations = generateMealPrepRecommendations(prepTasks)
        )
    }

    private fun generateMealPrepRecommendations(prepTasks: List<MealPrepTask>): List<String> {
        val recommendations = mutableListOf<String>()
        
        val totalTime = prepTasks.sumOf { it.estimatedTime }
        if (totalTime > 180) { // More than 3 hours
            recommendations.add("Consider splitting meal prep across multiple days")
        }
        
        val bakingTasks = prepTasks.filter { it.cookingMethod == "Baking" }
        if (bakingTasks.size > 1) {
            recommendations.add("Batch baking items together to save energy")
        }
        
        val slowCookingTasks = prepTasks.filter { it.cookingMethod == "Slow Cooking" }
        if (slowCookingTasks.isNotEmpty()) {
            recommendations.add("Start slow cooking items early in the day")
        }
        
        return recommendations
    }

    private fun getWeekStartDate(date: LocalDate): LocalDate {
        val weekFields = WeekFields.of(Locale.getDefault())
        return date.with(weekFields.dayOfWeek(), 1)
    }
}

// Additional data classes for meal prep scheduling
data class MealPrepSchedule(
    val weekStartDate: LocalDate,
    val prepTasks: List<MealPrepTask>,
    val totalEstimatedTime: Int, // in minutes
    val recommendations: List<String>
)

data class MealPrepTask(
    val id: String,
    val name: String,
    val recipes: List<Recipe>,
    val estimatedTime: Int, // in minutes
    val cookingMethod: String,
    val suggestedDay: String,
    val priority: MealPrepPriority,
    val isCompleted: Boolean = false
)

enum class MealPrepPriority {
    LOW, MEDIUM, HIGH
}