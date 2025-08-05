package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.profile.UserContextManager
import com.beaconledger.welltrack.domain.usecase.ProfileDataSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that handles profile-specific data operations and ensures data isolation
 * between different user profiles
 */
@Singleton
class ProfileDataRepository @Inject constructor(
    private val userContextManager: UserContextManager,
    private val mealDao: MealDao,
    private val healthMetricDao: HealthMetricDao,
    private val recipeDao: RecipeDao,
    private val profileDao: ProfileDao
) {
    
    // Meal operations with profile context
    fun getCurrentUserMeals(): Flow<List<Meal>> {
        val userId = userContextManager.requireCurrentUserId()
        return mealDao.getMealsByUser(userId)
    }
    
    fun getCurrentUserMealsByDate(date: String): Flow<List<Meal>> {
        val userId = userContextManager.requireCurrentUserId()
        return mealDao.getMealsByDate(userId, date)
    }
    
    fun getCurrentUserMealsByType(mealType: MealType): Flow<List<Meal>> {
        val userId = userContextManager.requireCurrentUserId()
        return mealDao.getMealsByType(userId, mealType)
    }
    
    suspend fun insertMealForCurrentUser(meal: Meal): Result<Unit> {
        return try {
            val userId = userContextManager.requireCurrentUserId()
            val mealWithUserId = meal.copy(userId = userId)
            mealDao.insertMeal(mealWithUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateMealStatusForCurrentUser(mealId: String, status: MealStatus): Result<Unit> {
        return try {
            val userId = userContextManager.requireCurrentUserId()
            mealDao.updateMealStatus(mealId, userId, status)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMealByIdForCurrentUser(mealId: String): Meal? {
        val userId = userContextManager.requireCurrentUserId()
        return mealDao.getMealById(mealId, userId)
    }
    
    // Health metrics operations with profile context
    fun getCurrentUserHealthMetrics(): Flow<List<HealthMetric>> {
        val userId = userContextManager.requireCurrentUserId()
        return healthMetricDao.getHealthMetricsByUser(userId)
    }
    
    fun getCurrentUserHealthMetricsByType(type: HealthMetricType): Flow<List<HealthMetric>> {
        val userId = userContextManager.requireCurrentUserId()
        return healthMetricDao.getHealthMetricsByType(userId, type)
    }
    
    fun getCurrentUserHealthMetricsInDateRange(startDate: String, endDate: String): Flow<List<HealthMetric>> {
        val userId = userContextManager.requireCurrentUserId()
        return healthMetricDao.getHealthMetricsInDateRange(userId, startDate, endDate)
    }
    
    suspend fun insertHealthMetricForCurrentUser(healthMetric: HealthMetric): Result<Unit> {
        return try {
            val userId = userContextManager.requireCurrentUserId()
            val metricWithUserId = healthMetric.copy(userId = userId)
            healthMetricDao.insertHealthMetric(metricWithUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Custom habits operations with profile context
    fun getCurrentUserActiveCustomHabits(): Flow<List<CustomHabit>> {
        val userId = userContextManager.requireCurrentUserId()
        return healthMetricDao.getActiveCustomHabits(userId)
    }
    
    suspend fun insertCustomHabitForCurrentUser(habit: CustomHabit): Result<Unit> {
        return try {
            val userId = userContextManager.requireCurrentUserId()
            val habitWithUserId = habit.copy(userId = userId)
            healthMetricDao.insertCustomHabit(habitWithUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getHabitCompletionsForCurrentUser(habitId: String, date: String): List<HabitCompletion> {
        val userId = userContextManager.requireCurrentUserId()
        return healthMetricDao.getHabitCompletionsForDate(habitId, userId, date)
    }
    
    suspend fun insertHabitCompletionForCurrentUser(completion: HabitCompletion): Result<Unit> {
        return try {
            val userId = userContextManager.requireCurrentUserId()
            val completionWithUserId = completion.copy(userId = userId)
            healthMetricDao.insertHabitCompletion(completionWithUserId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Profile switching operations
    suspend fun switchToProfile(profileId: String): Result<Unit> {
        return try {
            val success = userContextManager.switchContext(profileId)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to switch to profile: $profileId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Data cleanup operations for profile deletion
    suspend fun deleteAllDataForProfile(profileId: String): Result<Unit> {
        return try {
            // Delete all meals for this profile
            mealDao.deleteAllMealsByUser(profileId)
            
            // Delete all health metrics for this profile
            healthMetricDao.deleteAllHealthMetricsByUser(profileId)
            
            // Delete all custom habits for this profile
            healthMetricDao.deleteAllCustomHabitsByUser(profileId)
            
            // Delete all habit completions for this profile
            healthMetricDao.deleteAllHabitCompletionsByUser(profileId)
            
            // Delete the profile itself
            profileDao.deleteProfileByUserId(profileId)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Profile statistics
    suspend fun getProfileDataSummary(profileId: String): ProfileDataSummary {
        return try {
            val mealCount = mealDao.getMealCountByUser(profileId)
            val healthMetricCount = healthMetricDao.getHealthMetricCountByUser(profileId)
            val activeHabitCount = healthMetricDao.getActiveHabitCountByUser(profileId)
            
            ProfileDataSummary(
                profileId = profileId,
                totalMeals = mealCount,
                totalHealthMetrics = healthMetricCount,
                activeHabits = activeHabitCount
            )
        } catch (e: Exception) {
            ProfileDataSummary(
                profileId = profileId,
                totalMeals = 0,
                totalHealthMetrics = 0,
                activeHabits = 0
            )
        }
    }
    
    // Context validation
    fun validateCurrentContext(): Boolean {
        return userContextManager.hasActiveContext()
    }
    
    fun getCurrentUserId(): String? {
        return try {
            userContextManager.requireCurrentUserId()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getCurrentProfile(): UserProfile? {
        return try {
            userContextManager.requireCurrentProfile()
        } catch (e: Exception) {
            null
        }
    }
}

