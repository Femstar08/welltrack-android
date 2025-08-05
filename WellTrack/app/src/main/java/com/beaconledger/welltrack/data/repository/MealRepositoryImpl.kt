package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.MealDao
import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.data.model.MealStatus
import com.beaconledger.welltrack.domain.repository.MealRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealRepositoryImpl @Inject constructor(
    private val mealDao: MealDao
) : MealRepository {

    override suspend fun logMeal(meal: Meal): Result<String> {
        return try {
            mealDao.insertMeal(meal)
            Result.success(meal.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMeal(meal: Meal): Result<Unit> {
        return try {
            mealDao.updateMeal(meal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteMeal(mealId: String): Result<Unit> {
        return try {
            // Note: We need userId for security, but for now we'll get the meal first
            // In a real implementation, you'd pass userId as parameter
            val meal = mealDao.getMealById(mealId, "") // This needs to be fixed with proper userId
            meal?.let { mealDao.deleteMeal(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMealById(mealId: String): Result<Meal?> {
        return try {
            // Note: This needs userId parameter for security
            val meal = mealDao.getMealById(mealId, "") // This needs to be fixed with proper userId
            Result.success(meal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getMealsByUser(userId: String): Flow<List<Meal>> {
        return mealDao.getMealsByUser(userId)
    }

    override fun getMealsForDate(userId: String, date: String): Flow<List<Meal>> {
        return mealDao.getMealsByDate(userId, date)
    }

    override fun getMealsByType(userId: String, mealType: MealType): Flow<List<Meal>> {
        return mealDao.getMealsByType(userId, mealType)
    }

    override fun getMealsByStatus(userId: String, status: MealStatus): Flow<List<Meal>> {
        return mealDao.getMealsByStatus(userId, status)
    }

    override fun getMealsInDateRange(userId: String, startDate: String, endDate: String): Flow<List<Meal>> {
        return mealDao.getMealsInDateRange(userId, startDate, endDate)
    }

    override suspend fun updateMealStatus(mealId: String, status: MealStatus): Result<Unit> {
        return try {
            // Note: This needs userId parameter for security
            mealDao.updateMealStatus(mealId, "", status) // This needs to be fixed with proper userId
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMealRating(mealId: String, userId: String, rating: Float?): Result<Unit> {
        return try {
            mealDao.updateMealRating(mealId, userId, rating)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateMealFavorite(mealId: String, userId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            mealDao.updateMealFavorite(mealId, userId, isFavorite)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getFavoriteMeals(userId: String): Flow<List<Meal>> {
        return mealDao.getFavoriteMeals(userId)
    }

    override fun getRatedMeals(userId: String): Flow<List<Meal>> {
        return mealDao.getRatedMeals(userId)
    }

    override suspend fun getAverageRating(userId: String): Float? {
        return try {
            mealDao.getAverageRating(userId)
        } catch (e: Exception) {
            null
        }
    }
}