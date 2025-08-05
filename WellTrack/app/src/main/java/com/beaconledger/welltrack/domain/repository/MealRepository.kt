package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.data.model.MealStatus
import kotlinx.coroutines.flow.Flow
interface MealRepository {
    suspend fun logMeal(meal: Meal): Result<String>
    suspend fun updateMeal(meal: Meal): Result<Unit>
    suspend fun deleteMeal(mealId: String): Result<Unit>
    suspend fun getMealById(mealId: String): Result<Meal?>
    fun getMealsByUser(userId: String): Flow<List<Meal>>
    fun getMealsForDate(userId: String, date: String): Flow<List<Meal>>
    fun getMealsByType(userId: String, mealType: MealType): Flow<List<Meal>>
    fun getMealsByStatus(userId: String, status: MealStatus): Flow<List<Meal>>
    fun getMealsInDateRange(userId: String, startDate: String, endDate: String): Flow<List<Meal>>
    suspend fun updateMealStatus(mealId: String, status: MealStatus): Result<Unit>
    suspend fun updateMealRating(mealId: String, userId: String, rating: Float?): Result<Unit>
    suspend fun updateMealFavorite(mealId: String, userId: String, isFavorite: Boolean): Result<Unit>
    fun getFavoriteMeals(userId: String): Flow<List<Meal>>
    fun getRatedMeals(userId: String): Flow<List<Meal>>
    suspend fun getAverageRating(userId: String): Float?
}