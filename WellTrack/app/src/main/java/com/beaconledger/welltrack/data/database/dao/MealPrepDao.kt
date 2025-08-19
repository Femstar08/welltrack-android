package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MealPrepDao {
    
    // Meal Prep Instructions
    @Query("SELECT * FROM meal_prep_instructions WHERE recipeId = :recipeId")
    suspend fun getMealPrepInstructionsByRecipeId(recipeId: String): MealPrepInstruction?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealPrepInstruction(instruction: MealPrepInstruction)
    
    @Update
    suspend fun updateMealPrepInstruction(instruction: MealPrepInstruction)
    
    @Delete
    suspend fun deleteMealPrepInstruction(instruction: MealPrepInstruction)
    
    // Storage Recommendations
    @Query("SELECT * FROM storage_recommendations WHERE recipeId = :recipeId")
    suspend fun getStorageRecommendationsByRecipeId(recipeId: String): StorageRecommendation?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStorageRecommendation(recommendation: StorageRecommendation)
    
    @Update
    suspend fun updateStorageRecommendation(recommendation: StorageRecommendation)
    
    @Delete
    suspend fun deleteStorageRecommendation(recommendation: StorageRecommendation)
    
    // Leftovers
    @Query("SELECT * FROM leftovers WHERE userId = :userId AND isConsumed = 0 ORDER BY expiryDate ASC")
    fun getActiveLeftovers(userId: String): Flow<List<Leftover>>
    
    @Query("SELECT * FROM leftovers WHERE userId = :userId AND expiryDate < :currentDate AND isConsumed = 0")
    suspend fun getExpiredLeftovers(userId: String, currentDate: String): List<Leftover>
    
    @Query("SELECT * FROM leftovers WHERE userId = :userId AND expiryDate BETWEEN :startDate AND :endDate AND isConsumed = 0")
    suspend fun getLeftoversExpiringBetween(userId: String, startDate: String, endDate: String): List<Leftover>
    
    @Query("SELECT * FROM leftovers WHERE id = :id")
    suspend fun getLeftoverById(id: String): Leftover?
    
    @Query("SELECT * FROM leftovers WHERE userId = :userId AND mealId = :mealId")
    suspend fun getLeftoversByMealId(userId: String, mealId: String): List<Leftover>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeftover(leftover: Leftover)
    
    @Update
    suspend fun updateLeftover(leftover: Leftover)
    
    @Query("UPDATE leftovers SET isConsumed = 1, updatedAt = :updatedAt WHERE id = :id")
    suspend fun markLeftoverAsConsumed(id: String, updatedAt: String)
    
    @Delete
    suspend fun deleteLeftover(leftover: Leftover)
    
    @Query("DELETE FROM leftovers WHERE userId = :userId AND expiryDate < :currentDate AND isConsumed = 0")
    suspend fun deleteExpiredLeftovers(userId: String, currentDate: String)
    
    // Leftover Combinations
    @Query("SELECT * FROM leftover_combinations ORDER BY createdAt DESC")
    suspend fun getAllLeftoverCombinations(): List<LeftoverCombination>
    
    @Query("SELECT * FROM leftover_combinations WHERE leftoverIds LIKE '%' || :leftoverId || '%'")
    suspend fun getLeftoverCombinationsContaining(leftoverId: String): List<LeftoverCombination>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeftoverCombination(combination: LeftoverCombination)
    
    @Update
    suspend fun updateLeftoverCombination(combination: LeftoverCombination)
    
    @Delete
    suspend fun deleteLeftoverCombination(combination: LeftoverCombination)
    
    // Complex queries for suggestions
    @Query("""
        SELECT * FROM leftovers 
        WHERE userId = :userId 
        AND isConsumed = 0 
        AND expiryDate > :currentDate
        AND storageLocation = :storageLocation
        ORDER BY expiryDate ASC
    """)
    suspend fun getLeftoversByStorageLocation(
        userId: String, 
        currentDate: String, 
        storageLocation: StorageLocation
    ): List<Leftover>
    
    @Query("""
        SELECT COUNT(*) FROM leftovers 
        WHERE userId = :userId 
        AND isConsumed = 0 
        AND expiryDate BETWEEN :startDate AND :endDate
    """)
    suspend fun getLeftoversExpiringCount(userId: String, startDate: String, endDate: String): Int
    
    @Query("""
        SELECT * FROM leftovers 
        WHERE userId = :userId 
        AND isConsumed = 0 
        AND recipeId IS NOT NULL
        GROUP BY recipeId
        HAVING COUNT(*) > 1
    """)
    suspend fun getLeftoversWithMultiplePortions(userId: String): List<Leftover>
}