package com.beaconledger.welltrack.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.beaconledger.welltrack.data.meal.MealRecognitionResult
import com.beaconledger.welltrack.data.meal.MealRecognitionService
import com.beaconledger.welltrack.data.meal.MealScoringService
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.nutrition.NutritionCalculator
import com.beaconledger.welltrack.domain.repository.MealRepository
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealLoggingUseCase @Inject constructor(
    private val mealRepository: MealRepository,
    private val recipeRepository: RecipeRepository,
    private val mealRecognitionService: MealRecognitionService,
    private val mealScoringService: MealScoringService,
    private val nutritionCalculator: NutritionCalculator,
    private val gson: Gson
) {
    
    /**
     * Log a meal manually with ingredients
     */
    suspend fun logManualMeal(
        userId: String,
        mealName: String,
        ingredients: List<Ingredient>,
        mealType: MealType,
        portions: Float = 1.0f,
        notes: String? = null
    ): Result<String> {
        return try {
            val nutritionInfo = nutritionCalculator.calculateNutritionInfo(ingredients)
            val scaledNutrition = nutritionCalculator.scaleNutrition(nutritionInfo, portions.toDouble())
            val mealScore = mealScoringService.calculateMealScore(scaledNutrition)
            
            val meal = Meal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                recipeId = null,
                timestamp = LocalDateTime.now().toString(),
                mealType = mealType,
                portions = portions,
                nutritionInfo = gson.toJson(scaledNutrition),
                score = mealScore,
                status = MealStatus.EATEN,
                notes = notes
            )
            
            mealRepository.logMeal(meal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Log a meal from a saved recipe
     */
    suspend fun logMealFromRecipe(
        userId: String,
        recipeId: String,
        mealType: MealType,
        portions: Float = 1.0f,
        notes: String? = null
    ): Result<String> {
        return try {
            val recipeResult = recipeRepository.getRecipeById(recipeId)
            if (recipeResult.isFailure) {
                return Result.failure(recipeResult.exceptionOrNull() ?: Exception("Recipe not found"))
            }
            
            val recipe = recipeResult.getOrNull() ?: return Result.failure(Exception("Recipe not found"))
            val nutritionInfo = gson.fromJson(recipe.nutritionInfo, NutritionInfo::class.java)
            val scaledNutrition = nutritionCalculator.scaleNutrition(nutritionInfo, portions.toDouble())
            val mealScore = mealScoringService.calculateMealScore(scaledNutrition)
            
            val meal = Meal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                recipeId = recipeId,
                timestamp = LocalDateTime.now().toString(),
                mealType = mealType,
                portions = portions,
                nutritionInfo = gson.toJson(scaledNutrition),
                score = mealScore,
                status = MealStatus.EATEN,
                notes = notes
            )
            
            mealRepository.logMeal(meal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Log a meal from camera recognition
     */
    suspend fun logMealFromCamera(
        context: Context,
        userId: String,
        imageUri: Uri,
        mealType: MealType,
        portions: Float = 1.0f,
        notes: String? = null
    ): Result<MealFromCameraResult> {
        return try {
            val recognitionResult = mealRecognitionService.recognizeMealFromImage(context, imageUri)
            if (recognitionResult.isFailure) {
                return Result.failure(recognitionResult.exceptionOrNull() ?: Exception("Recognition failed"))
            }
            
            val recognition = recognitionResult.getOrNull() ?: return Result.failure(Exception("Recognition failed"))
            val scaledNutrition = nutritionCalculator.scaleNutrition(recognition.nutritionInfo, portions.toDouble())
            val mealScore = mealScoringService.calculateMealScore(scaledNutrition)
            
            val meal = Meal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                recipeId = null,
                timestamp = LocalDateTime.now().toString(),
                mealType = mealType,
                portions = portions,
                nutritionInfo = gson.toJson(scaledNutrition),
                score = mealScore,
                status = MealStatus.EATEN,
                notes = notes
            )
            
            val logResult = mealRepository.logMeal(meal)
            if (logResult.isFailure) {
                return Result.failure(logResult.exceptionOrNull() ?: Exception("Failed to log meal"))
            }
            
            Result.success(
                MealFromCameraResult(
                    mealId = meal.id,
                    recognitionResult = recognition,
                    meal = meal
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Log a meal from bitmap
     */
    suspend fun logMealFromBitmap(
        userId: String,
        bitmap: Bitmap,
        mealType: MealType,
        portions: Float = 1.0f,
        notes: String? = null
    ): Result<MealFromCameraResult> {
        return try {
            val recognitionResult = mealRecognitionService.recognizeMealFromBitmap(bitmap)
            if (recognitionResult.isFailure) {
                return Result.failure(recognitionResult.exceptionOrNull() ?: Exception("Recognition failed"))
            }
            
            val recognition = recognitionResult.getOrNull() ?: return Result.failure(Exception("Recognition failed"))
            val scaledNutrition = nutritionCalculator.scaleNutrition(recognition.nutritionInfo, portions.toDouble())
            val mealScore = mealScoringService.calculateMealScore(scaledNutrition)
            
            val meal = Meal(
                id = UUID.randomUUID().toString(),
                userId = userId,
                recipeId = null,
                timestamp = LocalDateTime.now().toString(),
                mealType = mealType,
                portions = portions,
                nutritionInfo = gson.toJson(scaledNutrition),
                score = mealScore,
                status = MealStatus.EATEN,
                notes = notes
            )
            
            val logResult = mealRepository.logMeal(meal)
            if (logResult.isFailure) {
                return Result.failure(logResult.exceptionOrNull() ?: Exception("Failed to log meal"))
            }
            
            Result.success(
                MealFromCameraResult(
                    mealId = meal.id,
                    recognitionResult = recognition,
                    meal = meal
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update meal status (eaten/skipped)
     */
    suspend fun updateMealStatus(mealId: String, status: MealStatus): Result<Unit> {
        return mealRepository.updateMealStatus(mealId, status)
    }
    
    /**
     * Update meal rating (1-5 stars)
     */
    suspend fun updateMealRating(mealId: String, userId: String, rating: Float?): Result<Unit> {
        return mealRepository.updateMealRating(mealId, userId, rating)
    }
    
    /**
     * Toggle meal favorite status
     */
    suspend fun updateMealFavorite(mealId: String, userId: String, isFavorite: Boolean): Result<Unit> {
        return mealRepository.updateMealFavorite(mealId, userId, isFavorite)
    }
    
    /**
     * Get favorite meals for user
     */
    fun getFavoriteMeals(userId: String): Flow<List<Meal>> {
        return mealRepository.getFavoriteMeals(userId)
    }
    
    /**
     * Get rated meals for user
     */
    fun getRatedMeals(userId: String): Flow<List<Meal>> {
        return mealRepository.getRatedMeals(userId)
    }
    
    /**
     * Get average rating for user's meals
     */
    suspend fun getAverageRating(userId: String): Float? {
        return mealRepository.getAverageRating(userId)
    }
    
    /**
     * Get meals for a specific date
     */
    fun getMealsForDate(userId: String, date: String): Flow<List<Meal>> {
        return mealRepository.getMealsForDate(userId, date)
    }
    
    /**
     * Get meals by type
     */
    fun getMealsByType(userId: String, mealType: MealType): Flow<List<Meal>> {
        return mealRepository.getMealsByType(userId, mealType)
    }
    
    /**
     * Get all meals for user
     */
    fun getAllMeals(userId: String): Flow<List<Meal>> {
        return mealRepository.getMealsByUser(userId)
    }
}

data class MealFromCameraResult(
    val mealId: String,
    val recognitionResult: MealRecognitionResult,
    val meal: Meal
)