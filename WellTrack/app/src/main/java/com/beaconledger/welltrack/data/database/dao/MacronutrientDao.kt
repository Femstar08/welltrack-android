package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MacronutrientDao {
    
    // Targets
    @Query("SELECT * FROM macronutrient_targets WHERE userId = :userId AND date = :date AND isActive = 1")
    suspend fun getTargetForDate(userId: String, date: LocalDate): MacronutrientTarget?
    
    @Query("SELECT * FROM macronutrient_targets WHERE userId = :userId AND date BETWEEN :startDate AND :endDate AND isActive = 1 ORDER BY date DESC")
    fun getTargetsForDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<MacronutrientTarget>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTarget(target: MacronutrientTarget)
    
    @Update
    suspend fun updateTarget(target: MacronutrientTarget)
    
    @Query("UPDATE macronutrient_targets SET isActive = 0 WHERE userId = :userId AND date = :date")
    suspend fun deactivateTargetForDate(userId: String, date: LocalDate)
    
    // Intake
    @Query("SELECT * FROM macronutrient_intake WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getIntakeForDate(userId: String, date: LocalDate): Flow<List<MacronutrientIntake>>
    
    @Query("SELECT * FROM macronutrient_intake WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, timestamp DESC")
    fun getIntakeForDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<MacronutrientIntake>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntake(intake: MacronutrientIntake)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntakeEntries(intakes: List<MacronutrientIntake>)
    
    @Update
    suspend fun updateIntake(intake: MacronutrientIntake)
    
    @Delete
    suspend fun deleteIntake(intake: MacronutrientIntake)
    
    @Query("DELETE FROM macronutrient_intake WHERE userId = :userId AND date = :date")
    suspend fun deleteIntakeForDate(userId: String, date: LocalDate)
    
    // Summary queries
    @Query("""
        SELECT 
            SUM(calories) as totalCalories,
            SUM(proteinGrams) as totalProtein,
            SUM(carbsGrams) as totalCarbs,
            SUM(fatGrams) as totalFat,
            SUM(fiberGrams) as totalFiber,
            SUM(waterMl) as totalWater
        FROM macronutrient_intake 
        WHERE userId = :userId AND date = :date
    """)
    suspend fun getDailySummary(userId: String, date: LocalDate): DailyNutrientSummary?
    
    @Query("""
        SELECT 
            date,
            SUM(calories) as totalCalories,
            SUM(proteinGrams) as totalProtein,
            SUM(carbsGrams) as totalCarbs,
            SUM(fatGrams) as totalFat,
            SUM(fiberGrams) as totalFiber,
            SUM(waterMl) as totalWater
        FROM macronutrient_intake 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY date
        ORDER BY date DESC
    """)
    fun getWeeklySummary(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<WeeklyNutrientSummary>>
    
    // Specific nutrient queries
    @Query("SELECT SUM(proteinGrams) FROM macronutrient_intake WHERE userId = :userId AND date = :date")
    suspend fun getTotalProteinForDate(userId: String, date: LocalDate): Double?
    
    @Query("SELECT SUM(fiberGrams) FROM macronutrient_intake WHERE userId = :userId AND date = :date")
    suspend fun getTotalFiberForDate(userId: String, date: LocalDate): Double?
    
    @Query("SELECT SUM(waterMl) FROM macronutrient_intake WHERE userId = :userId AND date = :date")
    suspend fun getTotalWaterForDate(userId: String, date: LocalDate): Int?
    
    @Query("SELECT SUM(calories) FROM macronutrient_intake WHERE userId = :userId AND date = :date")
    suspend fun getTotalCaloriesForDate(userId: String, date: LocalDate): Int?
    
    // Source-specific queries
    @Query("SELECT * FROM macronutrient_intake WHERE userId = :userId AND date = :date AND source = :source")
    fun getIntakeBySource(userId: String, date: LocalDate, source: NutrientSource): Flow<List<MacronutrientIntake>>
    
    @Query("SELECT SUM(calories) FROM macronutrient_intake WHERE userId = :userId AND date = :date AND source = 'MEAL'")
    suspend fun getCaloriesFromMeals(userId: String, date: LocalDate): Int?
    
    @Query("SELECT SUM(calories) FROM macronutrient_intake WHERE userId = :userId AND date = :date AND source = 'SUPPLEMENT'")
    suspend fun getCaloriesFromSupplements(userId: String, date: LocalDate): Int?
    
    // Custom nutrients
    @Query("SELECT * FROM custom_nutrients WHERE userId = :userId AND isActive = 1")
    fun getActiveCustomNutrients(userId: String): Flow<List<CustomNutrient>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomNutrient(nutrient: CustomNutrient)
    
    @Update
    suspend fun updateCustomNutrient(nutrient: CustomNutrient)
    
    @Query("UPDATE custom_nutrients SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCustomNutrient(id: String)
}

data class DailyNutrientSummary(
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double,
    val totalWater: Int
)

data class WeeklyNutrientSummary(
    val date: LocalDate,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double,
    val totalWater: Int
)