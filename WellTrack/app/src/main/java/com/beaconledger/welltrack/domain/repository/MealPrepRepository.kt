package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface MealPrepRepository {
    
    // Meal Prep Instructions
    suspend fun getMealPrepGuidance(recipeId: String): Result<MealPrepGuidanceResponse>
    suspend fun saveMealPrepInstructions(recipeId: String, instructions: MealPrepInstruction): Result<Unit>
    suspend fun saveStorageRecommendations(recipeId: String, recommendations: StorageRecommendation): Result<Unit>
    
    // Leftover Management
    suspend fun createLeftover(leftover: Leftover): Result<String>
    fun getActiveLeftovers(userId: String): Flow<List<Leftover>>
    suspend fun getLeftoverById(id: String): Result<Leftover>
    suspend fun updateLeftover(leftover: Leftover): Result<Unit>
    suspend fun markLeftoverAsConsumed(id: String): Result<Unit>
    suspend fun deleteLeftover(id: String): Result<Unit>
    
    // Leftover Expiry Management
    suspend fun getExpiredLeftovers(userId: String): Result<List<Leftover>>
    suspend fun getLeftoversExpiringWithin(userId: String, days: Int): Result<List<Leftover>>
    suspend fun cleanupExpiredLeftovers(userId: String): Result<Int>
    
    // Leftover Combinations and Suggestions
    suspend fun getLeftoverSuggestions(request: LeftoverSuggestionRequest): Result<LeftoverSuggestionResponse>
    suspend fun saveLeftoverCombination(combination: LeftoverCombination): Result<String>
    suspend fun getLeftoverCombinationsFor(leftoverIds: List<String>): Result<List<LeftoverCombination>>
    
    // Storage and Reheating Guidance
    suspend fun getStorageRecommendations(recipeId: String): Result<List<ContainerType>>
    suspend fun getReheatingInstructions(leftoverId: String): Result<List<ReheatingInstruction>>
    
    // Analytics and Insights
    suspend fun getLeftoverWasteAnalytics(userId: String, days: Int): Result<LeftoverWasteAnalytics>
    suspend fun getOptimalMealPrepSchedule(recipeIds: List<String>, targetDate: LocalDateTime): Result<MealPrepSchedule>
}

data class LeftoverWasteAnalytics(
    val totalLeftovers: Int,
    val consumedLeftovers: Int,
    val expiredLeftovers: Int,
    val wastePercentage: Double,
    val mostWastedIngredients: List<String>,
    val suggestions: List<String>
)

data class MealPrepSchedule(
    val totalTime: Int, // minutes
    val steps: List<MealPrepScheduleStep>,
    val tips: List<String>
)

data class MealPrepScheduleStep(
    val recipeId: String,
    val recipeName: String,
    val startTime: LocalDateTime,
    val duration: Int, // minutes
    val description: String,
    val dependencies: List<String> = emptyList()
)