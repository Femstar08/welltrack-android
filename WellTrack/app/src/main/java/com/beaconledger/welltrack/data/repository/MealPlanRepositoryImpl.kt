package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.MealPlanDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPlanRepositoryImpl @Inject constructor(
    private val mealPlanDao: MealPlanDao,
    private val recipeDao: RecipeDao
) : MealPlanRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    override fun getMealPlansByUser(userId: String): Flow<List<MealPlan>> {
        return mealPlanDao.getMealPlansByUser(userId)
    }

    override suspend fun getMealPlanById(mealPlanId: String): Result<MealPlan?> {
        return try {
            val mealPlan = mealPlanDao.getMealPlanById(mealPlanId)
            Result.success(mealPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getActiveMealPlan(userId: String): Result<MealPlan?> {
        return try {
            val mealPlan = mealPlanDao.getActiveMealPlan(userId)
            Result.success(mealPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMealPlanForDate(userId: String, date: LocalDate): Result<MealPlan?> {
        return try {
            val dateString = date.format(dateFormatter)
            val mealPlan = mealPlanDao.getMealPlanForDate(userId, dateString)
            Result.success(mealPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createMealPlan(mealPlan: MealPlan): Result<String> {
        return try {
            mealPlanDao.insertMealPlan(mealPlan)
            Result.success(mealPlan.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMealPlan(mealPlan: MealPlan): Result<Unit> {
        return try {
            val updatedMealPlan = mealPlan.copy(
                lastModified = LocalDateTime.now().format(dateTimeFormatter)
            )
            mealPlanDao.updateMealPlan(updatedMealPlan)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMealPlan(mealPlanId: String): Result<Unit> {
        return try {
            mealPlanDao.deleteMealPlan(mealPlanId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun activateMealPlan(mealPlanId: String, userId: String): Result<Unit> {
        return try {
            // Deactivate all existing meal plans for the user
            mealPlanDao.deactivateAllMealPlans(userId)
            
            // Activate the specified meal plan
            val mealPlan = mealPlanDao.getMealPlanById(mealPlanId)
            if (mealPlan != null) {
                val activatedPlan = mealPlan.copy(
                    isActive = true,
                    lastModified = LocalDateTime.now().format(dateTimeFormatter)
                )
                mealPlanDao.updateMealPlan(activatedPlan)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Meal plan not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    } 
   // Planned Meal operations
    override suspend fun getPlannedMealsByPlan(mealPlanId: String): Result<List<PlannedMeal>> {
        return try {
            val plannedMeals = mealPlanDao.getPlannedMealsByPlan(mealPlanId)
            Result.success(plannedMeals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlannedMealsForDate(userId: String, date: LocalDate): Result<List<PlannedMeal>> {
        return try {
            val dateString = date.format(dateFormatter)
            val plannedMeals = mealPlanDao.getPlannedMealsForDate(userId, dateString)
            Result.success(plannedMeals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlannedMealsForWeek(userId: String, weekStartDate: LocalDate): Result<List<PlannedMeal>> {
        return try {
            val startDate = weekStartDate.format(dateFormatter)
            val endDate = weekStartDate.plusDays(6).format(dateFormatter)
            val plannedMeals = mealPlanDao.getPlannedMealsForDateRange(userId, startDate, endDate)
            Result.success(plannedMeals)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlannedMealById(plannedMealId: String): Result<PlannedMeal?> {
        return try {
            val plannedMeal = mealPlanDao.getPlannedMealById(plannedMealId)
            Result.success(plannedMeal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPlannedMeal(plannedMeal: PlannedMeal): Result<String> {
        return try {
            mealPlanDao.insertPlannedMeal(plannedMeal)
            Result.success(plannedMeal.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePlannedMeal(plannedMeal: PlannedMeal): Result<Unit> {
        return try {
            val updatedMeal = plannedMeal.copy(
                updatedAt = LocalDateTime.now().format(dateTimeFormatter)
            )
            mealPlanDao.updatePlannedMeal(updatedMeal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePlannedMeal(plannedMealId: String): Result<Unit> {
        return try {
            mealPlanDao.deletePlannedMeal(plannedMealId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMealAsCompleted(plannedMealId: String): Result<Unit> {
        return try {
            val completedAt = LocalDateTime.now().format(dateTimeFormatter)
            mealPlanDao.updatePlannedMealStatus(plannedMealId, PlannedMealStatus.COMPLETED, completedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markMealAsSkipped(plannedMealId: String): Result<Unit> {
        return try {
            mealPlanDao.updatePlannedMealStatus(plannedMealId, PlannedMealStatus.SKIPPED, null)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Planned Supplement operations
    override suspend fun getPlannedSupplementsByPlan(mealPlanId: String): Result<List<PlannedSupplement>> {
        return try {
            val plannedSupplements = mealPlanDao.getPlannedSupplementsByPlan(mealPlanId)
            Result.success(plannedSupplements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPlannedSupplementsForDate(userId: String, date: LocalDate): Result<List<PlannedSupplement>> {
        return try {
            val dateString = date.format(dateFormatter)
            val plannedSupplements = mealPlanDao.getPlannedSupplementsForDate(userId, dateString)
            Result.success(plannedSupplements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPlannedSupplement(plannedSupplement: PlannedSupplement): Result<String> {
        return try {
            mealPlanDao.insertPlannedSupplement(plannedSupplement)
            Result.success(plannedSupplement.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updatePlannedSupplement(plannedSupplement: PlannedSupplement): Result<Unit> {
        return try {
            mealPlanDao.updatePlannedSupplement(plannedSupplement)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePlannedSupplement(plannedSupplementId: String): Result<Unit> {
        return try {
            mealPlanDao.deletePlannedSupplement(plannedSupplementId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markSupplementAsCompleted(plannedSupplementId: String): Result<Unit> {
        return try {
            val completedAt = LocalDateTime.now().format(dateTimeFormatter)
            mealPlanDao.updatePlannedSupplementCompletion(plannedSupplementId, true, completedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Meal Plan Generation
    override suspend fun generateWeeklyMealPlan(request: MealPlanGenerationRequest): Result<MealPlanGenerationResult> {
        return try {
            // Get available recipes for the user
            val availableRecipes = recipeDao.getAllRecipes().first()
            
            if (availableRecipes.isEmpty()) {
                return Result.success(
                    MealPlanGenerationResult(
                        success = false,
                        error = "No recipes available for meal plan generation"
                    )
                )
            }

            // Filter recipes based on preferences
            val filteredRecipes = filterRecipesByPreferences(availableRecipes, request.preferences)
            
            if (filteredRecipes.isEmpty()) {
                return Result.success(
                    MealPlanGenerationResult(
                        success = false,
                        error = "No recipes match your dietary preferences"
                    )
                )
            }

            // Generate meal plan
            val mealPlan = createMealPlanFromRequest(request)
            val plannedMeals = generatePlannedMeals(request, filteredRecipes)
            val plannedSupplements = generatePlannedSupplements(request)

            val weeklyMealPlan = WeeklyMealPlan(
                mealPlan = mealPlan,
                plannedMeals = plannedMeals,
                plannedSupplements = plannedSupplements,
                recipes = filteredRecipes
            )

            // Save to database
            mealPlanDao.createMealPlanWithMeals(mealPlan, plannedMeals, plannedSupplements)

            Result.success(
                MealPlanGenerationResult(
                    success = true,
                    mealPlan = weeklyMealPlan
                )
            )
        } catch (e: Exception) {
            Result.success(
                MealPlanGenerationResult(
                    success = false,
                    error = e.message ?: "Failed to generate meal plan"
                )
            )
        }
    }

    override suspend fun regenerateMealPlan(mealPlanId: String, preferences: MealPlanPreferences): Result<MealPlanGenerationResult> {
        return try {
            val existingPlan = mealPlanDao.getMealPlanById(mealPlanId)
                ?: return Result.success(
                    MealPlanGenerationResult(
                        success = false,
                        error = "Meal plan not found"
                    )
                )

            val weekStartDate = LocalDate.parse(existingPlan.weekStartDate, dateFormatter)
            val request = MealPlanGenerationRequest(
                userId = existingPlan.userId,
                weekStartDate = weekStartDate,
                preferences = preferences
            )

            generateWeeklyMealPlan(request)
        } catch (e: Exception) {
            Result.success(
                MealPlanGenerationResult(
                    success = false,
                    error = e.message ?: "Failed to regenerate meal plan"
                )
            )
        }
    }

    private fun createMealPlanFromRequest(request: MealPlanGenerationRequest): MealPlan {
        return MealPlan(
            id = UUID.randomUUID().toString(),
            userId = request.userId,
            weekStartDate = request.weekStartDate.format(dateFormatter),
            weekEndDate = request.weekStartDate.plusDays(6).format(dateFormatter),
            isActive = true,
            preferences = serializePreferences(request.preferences)
        )
    }

    private fun generatePlannedMeals(
        request: MealPlanGenerationRequest,
        recipes: List<Recipe>
    ): List<PlannedMeal> {
        val plannedMeals = mutableListOf<PlannedMeal>()
        val mealTypes = listOf(MealType.BREAKFAST, MealType.LUNCH, MealType.DINNER)
        
        // Generate meals for each day of the week
        for (dayOffset in 0..6) {
            val date = request.weekStartDate.plusDays(dayOffset.toLong())
            
            for (mealType in mealTypes) {
                val selectedRecipe = selectRecipeForMeal(recipes, mealType, request.preferences)
                if (selectedRecipe != null) {
                    plannedMeals.add(
                        PlannedMeal(
                            id = UUID.randomUUID().toString(),
                            mealPlanId = "", // Will be set when meal plan is created
                            userId = request.userId,
                            date = date.format(dateFormatter),
                            mealType = mealType,
                            recipeId = selectedRecipe.id,
                            servings = 1
                        )
                    )
                }
            }
        }
        
        return plannedMeals
    }

    private fun generatePlannedSupplements(request: MealPlanGenerationRequest): List<PlannedSupplement> {
        // For now, return empty list - supplements will be handled in a future task
        return emptyList()
    }

    private fun selectRecipeForMeal(
        recipes: List<Recipe>,
        mealType: MealType,
        preferences: MealPlanPreferences
    ): Recipe? {
        // Simple selection logic - can be enhanced with AI/ML later
        val suitableRecipes = recipes.filter { recipe ->
            val recipeTags = recipe.tags.split(",").map { it.trim() }
            when (mealType) {
                MealType.BREAKFAST -> recipeTags.any { it.contains("breakfast", ignoreCase = true) }
                MealType.LUNCH -> recipeTags.any { it.contains("lunch", ignoreCase = true) } || 
                                 recipe.prepTime + recipe.cookTime <= 45
                MealType.DINNER -> recipeTags.any { it.contains("dinner", ignoreCase = true) }
                MealType.SNACK -> recipeTags.any { it.contains("snack", ignoreCase = true) }
                MealType.SUPPLEMENT -> false // Supplements are not recipes
            }
        }
        
        return if (suitableRecipes.isNotEmpty()) {
            suitableRecipes.random()
        } else {
            recipes.randomOrNull()
        }
    }

    private fun filterRecipesByPreferences(
        recipes: List<Recipe>,
        preferences: MealPlanPreferences
    ): List<Recipe> {
        return recipes.filter { recipe ->
            // For now, we'll use a simplified filtering approach
            // In a real implementation, we would need to get recipe ingredients from a separate table
            
            // Filter by cooking time preference
            val totalTime = recipe.prepTime + recipe.cookTime
            when (preferences.cookingTimePreference) {
                CookingTimePreference.QUICK -> totalTime <= 30
                CookingTimePreference.MODERATE -> totalTime <= 60
                CookingTimePreference.EXTENDED -> true
            }
        }
    }

    private fun serializePreferences(preferences: MealPlanPreferences): String {
        // Simple JSON serialization - in a real app, use a proper JSON library
        return "{}"
    }

    // Weekly and Daily Meal Plan operations
    override suspend fun getWeeklyMealPlan(mealPlanId: String): Flow<WeeklyMealPlan?> = flow {
        try {
            val mealPlan = mealPlanDao.getMealPlanById(mealPlanId)
            if (mealPlan == null) {
                emit(null)
                return@flow
            }
            
            val plannedMeals = mealPlanDao.getPlannedMealsByPlan(mealPlan.id)
            val plannedSupplements = mealPlanDao.getPlannedSupplementsByPlan(mealPlan.id)
            
            // Get recipes for the planned meals
            val recipeIds = plannedMeals.mapNotNull { it.recipeId }.distinct()
            val recipes = recipeIds.mapNotNull { recipeDao.getRecipeById(it) }
            
            val weeklyMealPlan = WeeklyMealPlan(
                mealPlan = mealPlan,
                plannedMeals = plannedMeals,
                plannedSupplements = plannedSupplements,
                recipes = recipes
            )
            
            emit(weeklyMealPlan)
        } catch (e: Exception) {
            emit(null)
        }
    }

    override suspend fun getWeeklyMealPlan(userId: String, weekStartDate: LocalDate): Result<WeeklyMealPlan?> {
        return try {
            val startDate = weekStartDate.format(dateFormatter)
            val endDate = weekStartDate.plusDays(6).format(dateFormatter)
            
            val mealPlan = mealPlanDao.getMealPlanForDate(userId, startDate)
            if (mealPlan == null) {
                return Result.success(null)
            }
            
            val plannedMeals = mealPlanDao.getPlannedMealsByPlan(mealPlan.id)
            val plannedSupplements = mealPlanDao.getPlannedSupplementsByPlan(mealPlan.id)
            
            // Get recipes for the planned meals
            val recipeIds = plannedMeals.mapNotNull { it.recipeId }.distinct()
            val recipes = recipeIds.mapNotNull { recipeDao.getRecipeById(it) }
            
            val weeklyMealPlan = WeeklyMealPlan(
                mealPlan = mealPlan,
                plannedMeals = plannedMeals,
                plannedSupplements = plannedSupplements,
                recipes = recipes
            )
            
            Result.success(weeklyMealPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createWeeklyMealPlan(weeklyMealPlan: WeeklyMealPlan): Result<String> {
        return try {
            // Update planned meals with the meal plan ID
            val updatedPlannedMeals = weeklyMealPlan.plannedMeals.map { meal ->
                meal.copy(mealPlanId = weeklyMealPlan.mealPlan.id)
            }
            
            val updatedPlannedSupplements = weeklyMealPlan.plannedSupplements.map { supplement ->
                supplement.copy(mealPlanId = weeklyMealPlan.mealPlan.id)
            }
            
            mealPlanDao.createMealPlanWithMeals(
                weeklyMealPlan.mealPlan,
                updatedPlannedMeals,
                updatedPlannedSupplements
            )
            
            Result.success(weeklyMealPlan.mealPlan.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateWeeklyMealPlan(weeklyMealPlan: WeeklyMealPlan): Result<Unit> {
        return try {
            val updatedPlannedMeals = weeklyMealPlan.plannedMeals.map { meal ->
                meal.copy(mealPlanId = weeklyMealPlan.mealPlan.id)
            }
            
            val updatedPlannedSupplements = weeklyMealPlan.plannedSupplements.map { supplement ->
                supplement.copy(mealPlanId = weeklyMealPlan.mealPlan.id)
            }
            
            mealPlanDao.replaceMealPlan(
                weeklyMealPlan.mealPlan,
                updatedPlannedMeals,
                updatedPlannedSupplements
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyMealPlan(userId: String, date: LocalDate): Result<DailyMealPlan> {
        return try {
            val dateString = date.format(dateFormatter)
            val plannedMeals = mealPlanDao.getPlannedMealsForDate(userId, dateString)
            val plannedSupplements = mealPlanDao.getPlannedSupplementsForDate(userId, dateString)
            
            val breakfast = plannedMeals.find { it.mealType == MealType.BREAKFAST }
            val lunch = plannedMeals.find { it.mealType == MealType.LUNCH }
            val dinner = plannedMeals.find { it.mealType == MealType.DINNER }
            val snacks = plannedMeals.filter { it.mealType == MealType.SNACK }
            
            // Calculate total nutrition (simplified - would need proper calculation)
            val totalNutrition = calculateDailyNutrition(plannedMeals)
            
            val dailyMealPlan = DailyMealPlan(
                date = date,
                breakfast = breakfast,
                lunch = lunch,
                dinner = dinner,
                snacks = snacks,
                supplements = plannedSupplements,
                totalNutrition = totalNutrition
            )
            
            Result.success(dailyMealPlan)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun calculateDailyNutrition(plannedMeals: List<PlannedMeal>): NutritionInfo? {
        // Simplified calculation - in a real app, this would aggregate nutrition from recipes
        return null
    }

    // Statistics and Analytics
    override suspend fun getMealPlanAdherence(userId: String, startDate: LocalDate, endDate: LocalDate): Result<Double> {
        return try {
            val startDateString = startDate.format(dateFormatter)
            val endDateString = endDate.format(dateFormatter)
            
            val totalMeals = mealPlanDao.getTotalPlannedMealsCount(userId, startDateString, endDateString)
            val completedMeals = mealPlanDao.getCompletedMealsCount(userId, startDateString, endDateString)
            
            val adherence = if (totalMeals > 0) {
                completedMeals.toDouble() / totalMeals.toDouble()
            } else {
                0.0
            }
            
            Result.success(adherence)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWeeklyNutritionSummary(userId: String, weekStartDate: LocalDate): Result<NutritionInfo> {
        return try {
            // Simplified implementation - would aggregate nutrition from all meals in the week
            val defaultNutrition = NutritionInfo(
                calories = 0.0,
                carbohydrates = 0.0,
                proteins = 0.0,
                fats = 0.0,
                fiber = 0.0,
                sodium = 0.0,
                potassium = 0.0
            )
            Result.success(defaultNutrition)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Meal Plan Preferences
    override suspend fun saveMealPlanPreferences(userId: String, preferences: MealPlanPreferences): Result<Unit> {
        return try {
            // In a real implementation, this would save to a preferences table
            // For now, we'll just return success
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMealPlanPreferences(userId: String): Result<MealPlanPreferences?> {
        return try {
            // In a real implementation, this would load from a preferences table
            // For now, return default preferences
            val defaultPreferences = MealPlanPreferences()
            Result.success(defaultPreferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}