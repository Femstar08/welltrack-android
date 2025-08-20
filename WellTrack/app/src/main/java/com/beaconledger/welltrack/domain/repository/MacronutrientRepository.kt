package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MacronutrientRepository {
    
    // Target management
    suspend fun setDailyTargets(userId: String, date: LocalDate, targets: MacronutrientTarget): Result<String>
    suspend fun getDailyTargets(userId: String, date: LocalDate): MacronutrientTarget?
    fun getTargetsForDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<MacronutrientTarget>>
    
    // Intake tracking
    suspend fun logNutrientIntake(userId: String, intake: MacronutrientIntake): Result<String>
    suspend fun logMealNutrients(userId: String, mealId: String, nutrients: MacronutrientIntake): Result<String>
    suspend fun logSupplementNutrients(userId: String, supplementId: String, nutrients: MacronutrientIntake): Result<String>
    
    // Daily summaries
    fun getDailySummary(userId: String, date: LocalDate): Flow<MacronutrientSummary>
    fun getWeeklySummary(userId: String, weekStartDate: LocalDate): Flow<List<MacronutrientSummary>>
    fun getMonthlySummary(userId: String, month: Int, year: Int): Flow<List<MacronutrientSummary>>
    
    // Specific nutrient tracking
    suspend fun getTotalProteinForDate(userId: String, date: LocalDate): Double
    suspend fun getTotalFiberForDate(userId: String, date: LocalDate): Double
    suspend fun getTotalWaterForDate(userId: String, date: LocalDate): Int
    suspend fun getTotalCaloriesForDate(userId: String, date: LocalDate): Int
    
    // Progress tracking
    fun getProteinProgress(userId: String, date: LocalDate): Flow<Float>
    fun getFiberProgress(userId: String, date: LocalDate): Flow<Float>
    fun getWaterProgress(userId: String, date: LocalDate): Flow<Float>
    fun getCalorieProgress(userId: String, date: LocalDate): Flow<Float>
    
    // Target calculations
    suspend fun calculateProteinTarget(userId: String, bodyWeight: Double, activityLevel: ActivityLevel, goal: FitnessGoal): ProteinTarget
    suspend fun calculateFiberTarget(userId: String, age: Int, gender: Gender): FiberTarget
    suspend fun calculateWaterTarget(userId: String, bodyWeight: Double, activityLevel: ActivityLevel): Int
    
    // Custom nutrients
    suspend fun addCustomNutrient(userId: String, nutrient: CustomNutrient): Result<String>
    suspend fun updateCustomNutrient(nutrient: CustomNutrient): Result<Unit>
    suspend fun removeCustomNutrient(nutrientId: String): Result<Unit>
    fun getActiveCustomNutrients(userId: String): Flow<List<CustomNutrient>>
    
    // Trends and analysis
    fun getNutrientTrends(userId: String, nutrientName: String, days: Int): Flow<NutrientTrend>
    fun getMacronutrientBalance(userId: String, date: LocalDate): Flow<MacronutrientBalance>
    
    // Bulk operations
    suspend fun importNutrientsFromMeal(userId: String, mealId: String, date: LocalDate): Result<Unit>
    suspend fun importNutrientsFromRecipe(userId: String, recipeId: String, servings: Float, date: LocalDate): Result<Unit>
    suspend fun clearIntakeForDate(userId: String, date: LocalDate): Result<Unit>
}