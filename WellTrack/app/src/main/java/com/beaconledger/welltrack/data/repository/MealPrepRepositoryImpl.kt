package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.MealPrepDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MealPrepRepository
import com.beaconledger.welltrack.domain.repository.LeftoverWasteAnalytics
import com.beaconledger.welltrack.domain.repository.MealPrepSchedule
import com.beaconledger.welltrack.domain.repository.MealPrepScheduleStep
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MealPrepRepositoryImpl @Inject constructor(
    private val mealPrepDao: MealPrepDao,
    private val gson: Gson
) : MealPrepRepository {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    override suspend fun getMealPrepGuidance(recipeId: String): Result<MealPrepGuidanceResponse> {
        return try {
            val instructions = mealPrepDao.getMealPrepInstructionsByRecipeId(recipeId)
            val recommendations = mealPrepDao.getStorageRecommendationsByRecipeId(recipeId)
            
            if (instructions != null && recommendations != null) {
                val prepSteps = gson.fromJson(instructions.prepSteps, Array<PrepStep>::class.java).toList()
                val cookingMethods = gson.fromJson(instructions.cookingMethods, Array<CookingMethod>::class.java).toList()
                val timingGuidance = gson.fromJson(instructions.timingGuidance, TimingGuidance::class.java)
                val containerTypes = gson.fromJson(recommendations.containerTypes, Array<ContainerType>::class.java).toList()
                val refrigerationGuideline = gson.fromJson(recommendations.refrigerationGuidelines, RefrigerationGuideline::class.java)
                val freezingInstructions = gson.fromJson(recommendations.freezingInstructions, FreezingInstruction::class.java)
                
                val response = MealPrepGuidanceResponse(
                    prepInstructions = prepSteps,
                    cookingMethods = cookingMethods,
                    timingGuidance = timingGuidance,
                    storageRecommendations = containerTypes,
                    refrigerationGuideline = refrigerationGuideline,
                    freezingInstructions = freezingInstructions
                )
                Result.success(response)
            } else {
                // Generate default guidance if not found
                Result.success(generateDefaultMealPrepGuidance())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveMealPrepInstructions(recipeId: String, instructions: MealPrepInstruction): Result<Unit> {
        return try {
            mealPrepDao.insertMealPrepInstruction(instructions)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveStorageRecommendations(recipeId: String, recommendations: StorageRecommendation): Result<Unit> {
        return try {
            mealPrepDao.insertStorageRecommendation(recommendations)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun createLeftover(leftover: Leftover): Result<String> {
        return try {
            mealPrepDao.insertLeftover(leftover)
            Result.success(leftover.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getActiveLeftovers(userId: String): Flow<List<Leftover>> {
        return mealPrepDao.getActiveLeftovers(userId)
    }
    
    override suspend fun getLeftoverById(id: String): Result<Leftover> {
        return try {
            val leftover = mealPrepDao.getLeftoverById(id)
            if (leftover != null) {
                Result.success(leftover)
            } else {
                Result.failure(Exception("Leftover not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateLeftover(leftover: Leftover): Result<Unit> {
        return try {
            mealPrepDao.updateLeftover(leftover)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markLeftoverAsConsumed(id: String): Result<Unit> {
        return try {
            val updatedAt = LocalDateTime.now().format(dateFormatter)
            mealPrepDao.markLeftoverAsConsumed(id, updatedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteLeftover(id: String): Result<Unit> {
        return try {
            val leftover = mealPrepDao.getLeftoverById(id)
            if (leftover != null) {
                mealPrepDao.deleteLeftover(leftover)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Leftover not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getExpiredLeftovers(userId: String): Result<List<Leftover>> {
        return try {
            val currentDate = LocalDateTime.now().format(dateFormatter)
            val expired = mealPrepDao.getExpiredLeftovers(userId, currentDate)
            Result.success(expired)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getLeftoversExpiringWithin(userId: String, days: Int): Result<List<Leftover>> {
        return try {
            val startDate = LocalDateTime.now().format(dateFormatter)
            val endDate = LocalDateTime.now().plusDays(days.toLong()).format(dateFormatter)
            val expiring = mealPrepDao.getLeftoversExpiringBetween(userId, startDate, endDate)
            Result.success(expiring)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cleanupExpiredLeftovers(userId: String): Result<Int> {
        return try {
            val currentDate = LocalDateTime.now().format(dateFormatter)
            val expiredCount = mealPrepDao.getExpiredLeftovers(userId, currentDate).size
            mealPrepDao.deleteExpiredLeftovers(userId, currentDate)
            Result.success(expiredCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getLeftoverSuggestions(request: LeftoverSuggestionRequest): Result<LeftoverSuggestionResponse> {
        return try {
            val combinations = mutableListOf<LeftoverCombination>()
            val quickIdeas = mutableListOf<String>()
            val safetyReminders = listOf(
                "Always reheat leftovers to 165°F (74°C) internal temperature",
                "Don't leave leftovers at room temperature for more than 2 hours",
                "When in doubt, throw it out - trust your senses",
                "Reheat only the portion you plan to eat immediately"
            )
            
            // Get existing combinations that match the leftovers
            for (leftoverId in request.leftoverIds) {
                val existingCombinations = mealPrepDao.getLeftoverCombinationsContaining(leftoverId)
                combinations.addAll(existingCombinations)
            }
            
            // Generate quick ideas based on leftover types
            quickIdeas.addAll(generateQuickLeftoverIdeas(request.leftoverIds))
            
            val response = LeftoverSuggestionResponse(
                combinations = combinations.distinctBy { it.id },
                quickIdeas = quickIdeas,
                safetyReminders = safetyReminders
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveLeftoverCombination(combination: LeftoverCombination): Result<String> {
        return try {
            mealPrepDao.insertLeftoverCombination(combination)
            Result.success(combination.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getLeftoverCombinationsFor(leftoverIds: List<String>): Result<List<LeftoverCombination>> {
        return try {
            val combinations = mutableListOf<LeftoverCombination>()
            for (leftoverId in leftoverIds) {
                val matching = mealPrepDao.getLeftoverCombinationsContaining(leftoverId)
                combinations.addAll(matching)
            }
            Result.success(combinations.distinctBy { it.id })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStorageRecommendations(recipeId: String): Result<List<ContainerType>> {
        return try {
            val recommendations = mealPrepDao.getStorageRecommendationsByRecipeId(recipeId)
            if (recommendations != null) {
                val containerTypes = gson.fromJson(recommendations.containerTypes, Array<ContainerType>::class.java).toList()
                Result.success(containerTypes)
            } else {
                Result.success(getDefaultStorageRecommendations())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getReheatingInstructions(leftoverId: String): Result<List<ReheatingInstruction>> {
        return try {
            // For now, return default reheating instructions
            // In a full implementation, this would be based on the leftover type
            val instructions = listOf(
                ReheatingInstruction(
                    method = ReheatingMethod.MICROWAVE,
                    temperature = "Medium power (50-70%)",
                    duration = "1-2 minutes per serving",
                    instructions = "Cover with microwave-safe lid or damp paper towel. Heat in 30-second intervals, stirring between.",
                    safetyNotes = listOf("Ensure internal temperature reaches 165°F (74°C)")
                ),
                ReheatingInstruction(
                    method = ReheatingMethod.OVEN,
                    temperature = "350°F (175°C)",
                    duration = "10-15 minutes",
                    instructions = "Cover with foil to prevent drying. Remove foil for last 2-3 minutes if crispness is desired.",
                    safetyNotes = listOf("Use oven-safe container", "Check internal temperature")
                )
            )
            Result.success(instructions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getLeftoverWasteAnalytics(userId: String, days: Int): Result<LeftoverWasteAnalytics> {
        return try {
            val startDate = LocalDateTime.now().minusDays(days.toLong()).format(dateFormatter)
            val endDate = LocalDateTime.now().format(dateFormatter)
            
            // This would require additional queries to get historical data
            // For now, return mock analytics
            val analytics = LeftoverWasteAnalytics(
                totalLeftovers = 10,
                consumedLeftovers = 7,
                expiredLeftovers = 3,
                wastePercentage = 30.0,
                mostWastedIngredients = listOf("Vegetables", "Bread", "Dairy"),
                suggestions = listOf(
                    "Consider smaller portion sizes",
                    "Plan leftover meals in advance",
                    "Use vegetables in soups or stir-fries"
                )
            )
            Result.success(analytics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getOptimalMealPrepSchedule(recipeIds: List<String>, targetDate: LocalDateTime): Result<MealPrepSchedule> {
        return try {
            // This would analyze recipes and create an optimal prep schedule
            // For now, return a basic schedule
            val steps = recipeIds.mapIndexed { index, recipeId ->
                MealPrepScheduleStep(
                    recipeId = recipeId,
                    recipeName = "Recipe $index",
                    startTime = targetDate.minusHours((recipeIds.size - index).toLong()),
                    duration = 30,
                    description = "Prep ingredients and cook recipe $index"
                )
            }
            
            val schedule = MealPrepSchedule(
                totalTime = steps.sumOf { it.duration },
                steps = steps,
                tips = listOf(
                    "Start with recipes that take longest to cook",
                    "Prep all vegetables first",
                    "Use multiple cooking methods simultaneously"
                )
            )
            Result.success(schedule)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun generateDefaultMealPrepGuidance(): MealPrepGuidanceResponse {
        return MealPrepGuidanceResponse(
            prepInstructions = listOf(
                PrepStep(1, "Gather all ingredients and equipment", 5, listOf("Cutting board", "Knives")),
                PrepStep(2, "Wash and prep vegetables", 10, listOf("Colander", "Paper towels")),
                PrepStep(3, "Measure dry ingredients", 5, listOf("Measuring cups"))
            ),
            cookingMethods = listOf(
                CookingMethod(
                    method = CookingMethodType.SAUTEING,
                    temperature = "Medium heat",
                    duration = 10,
                    instructions = "Heat oil in pan, add ingredients, stir frequently"
                )
            ),
            timingGuidance = TimingGuidance(
                totalPrepTime = 30,
                activeTime = 20,
                passiveTime = 10,
                optimalSchedule = listOf(
                    ScheduleStep(1, "Prep ingredients", 0, 15),
                    ScheduleStep(2, "Cook", 15, 15)
                )
            ),
            storageRecommendations = getDefaultStorageRecommendations(),
            refrigerationGuideline = RefrigerationGuideline(
                temperature = "32-40°F (0-4°C)",
                maxDuration = 3,
                storageInstructions = "Store in airtight container",
                qualityIndicators = listOf("Fresh smell", "No discoloration")
            ),
            freezingInstructions = FreezingInstruction(
                isFreezable = true,
                maxDuration = 30,
                freezingInstructions = "Cool completely before freezing",
                thawingInstructions = "Thaw in refrigerator overnight"
            )
        )
    }
    
    private fun getDefaultStorageRecommendations(): List<ContainerType> {
        return listOf(
            ContainerType(
                type = ContainerTypeEnum.GLASS_CONTAINER,
                size = "Medium (2-4 cups)",
                material = "Borosilicate glass",
                suitableFor = listOf("Hot foods", "Acidic foods", "Microwave reheating")
            ),
            ContainerType(
                type = ContainerTypeEnum.PLASTIC_CONTAINER,
                size = "Various sizes",
                material = "BPA-free plastic",
                suitableFor = listOf("Cold foods", "Dry ingredients", "Freezer storage")
            )
        )
    }
    
    private fun generateQuickLeftoverIdeas(leftoverIds: List<String>): List<String> {
        return listOf(
            "Transform into a stir-fry with fresh vegetables",
            "Add to a soup or broth for extra flavor",
            "Create a grain bowl with fresh toppings",
            "Make into a sandwich or wrap",
            "Use as pizza toppings",
            "Blend into a smoothie (for fruit leftovers)",
            "Add to scrambled eggs or omelet",
            "Create a salad with fresh greens"
        )
    }
}