package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealPlanRepository {
    
    // Meal Plan operations
    fun getMealPlansByUser(userId: String): Flow<List<MealPlan>>
    suspend fun getMealPlanById(mealPlanId: String): Result<MealPlan?>
    suspend fun getActiveMealPlan(userId: String): Result<MealPlan?>
    suspend fun getMealPlanForDate(userId: String, date: LocalDate): Result<MealPlan?>
    suspend fun createMealPlan(mealPlan: MealPlan): Result<String>
    suspend fun updateMealPlan(mealPlan: MealPlan): Result<Unit>
    suspend fun deleteMealPlan(mealPlanId: String): Result<Unit>
    suspend fun activateMealPlan(mealPlanId: String, userId: String): Result<Unit>
    
    // Planned Meal operations
    suspend fun getPlannedMealsByPlan(mealPlanId: String): Result<List<PlannedMeal>>
    suspend fun getPlannedMealsForDate(userId: String, date: LocalDate): Result<List<PlannedMeal>>
    suspend fun getPlannedMealsForWeek(userId: String, weekStartDate: LocalDate): Result<List<PlannedMeal>>
    suspend fun getPlannedMealById(plannedMealId: String): Result<PlannedMeal?>
    suspend fun createPlannedMeal(plannedMeal: PlannedMeal): Result<String>
    suspend fun updatePlannedMeal(plannedMeal: PlannedMeal): Result<Unit>
    suspend fun deletePlannedMeal(plannedMealId: String): Result<Unit>
    suspend fun markMealAsCompleted(plannedMealId: String): Result<Unit>
    suspend fun markMealAsSkipped(plannedMealId: String): Result<Unit>
    
    // Planned Supplement operations
    suspend fun getPlannedSupplementsByPlan(mealPlanId: String): Result<List<PlannedSupplement>>
    suspend fun getPlannedSupplementsForDate(userId: String, date: LocalDate): Result<List<PlannedSupplement>>
    suspend fun createPlannedSupplement(plannedSupplement: PlannedSupplement): Result<String>
    suspend fun updatePlannedSupplement(plannedSupplement: PlannedSupplement): Result<Unit>
    suspend fun deletePlannedSupplement(plannedSupplementId: String): Result<Unit>
    suspend fun markSupplementAsCompleted(plannedSupplementId: String): Result<Unit>
    
    // Meal Plan Generation
    suspend fun generateWeeklyMealPlan(request: MealPlanGenerationRequest): Result<MealPlanGenerationResult>
    suspend fun regenerateMealPlan(mealPlanId: String, preferences: MealPlanPreferences): Result<MealPlanGenerationResult>
    
    // Weekly Meal Plan operations
    suspend fun getWeeklyMealPlan(userId: String, weekStartDate: LocalDate): Result<WeeklyMealPlan?>
    suspend fun createWeeklyMealPlan(weeklyMealPlan: WeeklyMealPlan): Result<String>
    suspend fun updateWeeklyMealPlan(weeklyMealPlan: WeeklyMealPlan): Result<Unit>
    
    // Daily Meal Plan operations
    suspend fun getDailyMealPlan(userId: String, date: LocalDate): Result<DailyMealPlan>
    
    // Statistics and Analytics
    suspend fun getMealPlanAdherence(userId: String, startDate: LocalDate, endDate: LocalDate): Result<Double>
    suspend fun getWeeklyNutritionSummary(userId: String, weekStartDate: LocalDate): Result<NutritionInfo>
    
    // Meal Plan Templates and Preferences
    suspend fun saveMealPlanPreferences(userId: String, preferences: MealPlanPreferences): Result<Unit>
    suspend fun getMealPlanPreferences(userId: String): Result<MealPlanPreferences?>
}