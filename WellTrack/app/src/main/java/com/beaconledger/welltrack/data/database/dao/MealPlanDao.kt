package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPlanDao {
    
    // Meal Plan operations
    @Query("SELECT * FROM meal_plans WHERE userId = :userId ORDER BY weekStartDate DESC")
    fun getMealPlansByUser(userId: String): Flow<List<MealPlan>>
    
    @Query("SELECT * FROM meal_plans WHERE id = :mealPlanId")
    suspend fun getMealPlanById(mealPlanId: String): MealPlan?
    
    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND isActive = 1 ORDER BY weekStartDate DESC LIMIT 1")
    suspend fun getActiveMealPlan(userId: String): MealPlan?
    
    @Query("SELECT * FROM meal_plans WHERE userId = :userId AND weekStartDate <= :date AND weekEndDate >= :date")
    suspend fun getMealPlanForDate(userId: String, date: String): MealPlan?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPlan(mealPlan: MealPlan)
    
    @Update
    suspend fun updateMealPlan(mealPlan: MealPlan)
    
    @Query("DELETE FROM meal_plans WHERE id = :mealPlanId")
    suspend fun deleteMealPlan(mealPlanId: String)
    
    @Query("SELECT * FROM meal_plans WHERE userId = :userId")
    suspend fun getMealPlansForUser(userId: String): List<MealPlan>
    
    @Query("UPDATE meal_plans SET isActive = 0 WHERE userId = :userId")
    suspend fun deactivateAllMealPlans(userId: String)
    
    // Planned Meal operations
    @Query("SELECT * FROM planned_meals WHERE mealPlanId = :mealPlanId ORDER BY date ASC, mealType ASC")
    suspend fun getPlannedMealsByPlan(mealPlanId: String): List<PlannedMeal>
    
    @Query("SELECT * FROM planned_meals WHERE userId = :userId AND date = :date ORDER BY mealType ASC")
    suspend fun getPlannedMealsForDate(userId: String, date: String): List<PlannedMeal>
    
    @Query("SELECT * FROM planned_meals WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC, mealType ASC")
    suspend fun getPlannedMealsForDateRange(userId: String, startDate: String, endDate: String): List<PlannedMeal>
    
    @Query("SELECT * FROM planned_meals WHERE id = :plannedMealId")
    suspend fun getPlannedMealById(plannedMealId: String): PlannedMeal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedMeal(plannedMeal: PlannedMeal)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedMeals(plannedMeals: List<PlannedMeal>)
    
    @Update
    suspend fun updatePlannedMeal(plannedMeal: PlannedMeal)
    
    @Query("DELETE FROM planned_meals WHERE id = :plannedMealId")
    suspend fun deletePlannedMeal(plannedMealId: String)
    
    @Query("DELETE FROM planned_meals WHERE mealPlanId = :mealPlanId")
    suspend fun deletePlannedMealsByPlan(mealPlanId: String)
    
    @Query("UPDATE planned_meals SET status = :status, completedAt = :completedAt WHERE id = :plannedMealId")
    suspend fun updatePlannedMealStatus(plannedMealId: String, status: PlannedMealStatus, completedAt: String?)
    
    // Planned Supplement operations
    @Query("SELECT * FROM planned_supplements WHERE mealPlanId = :mealPlanId ORDER BY date ASC, timing ASC")
    suspend fun getPlannedSupplementsByPlan(mealPlanId: String): List<PlannedSupplement>
    
    @Query("SELECT * FROM planned_supplements WHERE userId = :userId AND date = :date ORDER BY timing ASC")
    suspend fun getPlannedSupplementsForDate(userId: String, date: String): List<PlannedSupplement>
    
    @Query("SELECT * FROM planned_supplements WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC, timing ASC")
    suspend fun getPlannedSupplementsForDateRange(userId: String, startDate: String, endDate: String): List<PlannedSupplement>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedSupplement(plannedSupplement: PlannedSupplement)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlannedSupplements(plannedSupplements: List<PlannedSupplement>)
    
    @Update
    suspend fun updatePlannedSupplement(plannedSupplement: PlannedSupplement)
    
    @Query("DELETE FROM planned_supplements WHERE id = :plannedSupplementId")
    suspend fun deletePlannedSupplement(plannedSupplementId: String)
    
    @Query("DELETE FROM planned_supplements WHERE mealPlanId = :mealPlanId")
    suspend fun deletePlannedSupplementsByPlan(mealPlanId: String)
    
    @Query("UPDATE planned_supplements SET isCompleted = :isCompleted, completedAt = :completedAt WHERE id = :plannedSupplementId")
    suspend fun updatePlannedSupplementCompletion(plannedSupplementId: String, isCompleted: Boolean, completedAt: String?)
    
    // Complex queries for meal planning
    @Query("""
        SELECT pm.* FROM planned_meals pm
        INNER JOIN meal_plans mp ON pm.mealPlanId = mp.id
        WHERE pm.userId = :userId 
        AND mp.isActive = 1
        AND pm.date BETWEEN :startDate AND :endDate
        ORDER BY pm.date ASC, pm.mealType ASC
    """)
    suspend fun getActivePlannedMealsForDateRange(userId: String, startDate: String, endDate: String): List<PlannedMeal>
    
    @Query("""
        SELECT COUNT(*) FROM planned_meals pm
        INNER JOIN meal_plans mp ON pm.mealPlanId = mp.id
        WHERE pm.userId = :userId 
        AND mp.isActive = 1
        AND pm.status = 'COMPLETED'
        AND pm.date BETWEEN :startDate AND :endDate
    """)
    suspend fun getCompletedMealsCount(userId: String, startDate: String, endDate: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM planned_meals pm
        INNER JOIN meal_plans mp ON pm.mealPlanId = mp.id
        WHERE pm.userId = :userId 
        AND mp.isActive = 1
        AND pm.date BETWEEN :startDate AND :endDate
    """)
    suspend fun getTotalPlannedMealsCount(userId: String, startDate: String, endDate: String): Int
    
    // Transaction methods for atomic operations
    @Transaction
    suspend fun createMealPlanWithMeals(
        mealPlan: MealPlan,
        plannedMeals: List<PlannedMeal>,
        plannedSupplements: List<PlannedSupplement>
    ) {
        insertMealPlan(mealPlan)
        insertPlannedMeals(plannedMeals)
        insertPlannedSupplements(plannedSupplements)
    }
    
    @Transaction
    suspend fun replaceMealPlan(
        mealPlan: MealPlan,
        plannedMeals: List<PlannedMeal>,
        plannedSupplements: List<PlannedSupplement>
    ) {
        // Delete existing planned meals and supplements
        deletePlannedMealsByPlan(mealPlan.id)
        deletePlannedSupplementsByPlan(mealPlan.id)
        
        // Insert new data
        insertMealPlan(mealPlan)
        insertPlannedMeals(plannedMeals)
        insertPlannedSupplements(plannedSupplements)
    }

    @Query("DELETE FROM meal_plans WHERE userId = :userId")
    suspend fun deleteAllMealPlansForUser(userId: String)
}