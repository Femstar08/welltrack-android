package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.data.model.MealStatus
import kotlinx.coroutines.flow.Flow
@Dao
interface MealDao {
    @Query("SELECT * FROM meals WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMealsByUser(userId: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE id = :mealId AND userId = :userId")
    suspend fun getMealById(mealId: String, userId: String): Meal?

    @Query("SELECT * FROM meals WHERE userId = :userId AND DATE(timestamp) = DATE(:date)")
    fun getMealsByDate(userId: String, date: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND mealType = :mealType ORDER BY timestamp DESC")
    fun getMealsByType(userId: String, mealType: MealType): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND status = :status ORDER BY timestamp DESC")
    fun getMealsByStatus(userId: String, status: MealStatus): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getMealsInDateRange(userId: String, startDate: String, endDate: String): Flow<List<Meal>>

    @Query("SELECT COUNT(*) FROM meals WHERE userId = :userId")
    suspend fun getMealCountByUser(userId: String): Int
    
    @Query("SELECT * FROM meals WHERE userId = :userId")
    suspend fun getMealsForUser(userId: String): List<Meal>
    
    @Query("SELECT * FROM meals WHERE id = :mealId")
    suspend fun getMealById(mealId: String): Meal?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeal(meal: Meal)
    
    @Update
    suspend fun updateMeal(meal: Meal)
    
    @Delete
    suspend fun deleteMeal(meal: Meal)
    
    @Query("DELETE FROM meals WHERE id = :mealId")
    suspend fun deleteMealById(mealId: String)

    @Query("SELECT COUNT(*) FROM meals WHERE userId = :userId AND DATE(timestamp) = DATE(:date)")
    suspend fun getMealCountByDate(userId: String, date: String): Int

    @Query("DELETE FROM meals WHERE userId = :userId")
    suspend fun deleteAllMealsByUser(userId: String)

    @Query("UPDATE meals SET status = :status WHERE id = :mealId AND userId = :userId")
    suspend fun updateMealStatus(mealId: String, userId: String, status: MealStatus)

    @Query("UPDATE meals SET rating = :rating WHERE id = :mealId AND userId = :userId")
    suspend fun updateMealRating(mealId: String, userId: String, rating: Float?)

    @Query("UPDATE meals SET isFavorite = :isFavorite WHERE id = :mealId AND userId = :userId")
    suspend fun updateMealFavorite(mealId: String, userId: String, isFavorite: Boolean)

    @Query("SELECT * FROM meals WHERE userId = :userId AND isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteMeals(userId: String): Flow<List<Meal>>

    @Query("SELECT * FROM meals WHERE userId = :userId AND rating IS NOT NULL ORDER BY rating DESC, timestamp DESC")
    fun getRatedMeals(userId: String): Flow<List<Meal>>

    @Query("SELECT AVG(rating) FROM meals WHERE userId = :userId AND rating IS NOT NULL")
    suspend fun getAverageRating(userId: String): Float?
}