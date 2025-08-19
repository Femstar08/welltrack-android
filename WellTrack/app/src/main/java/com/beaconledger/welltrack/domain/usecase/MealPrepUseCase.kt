package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MealPrepRepository
import com.beaconledger.welltrack.domain.repository.LeftoverWasteAnalytics
import com.beaconledger.welltrack.domain.repository.MealPrepSchedule
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class GetMealPrepGuidanceUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(recipeId: String): Result<MealPrepGuidanceResponse> {
        return repository.getMealPrepGuidance(recipeId)
    }
}

class CreateLeftoverUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(
        userId: String,
        mealId: String,
        recipeId: String?,
        name: String,
        quantity: Double,
        unit: String,
        storageLocation: StorageLocation,
        containerType: String,
        nutritionInfo: NutritionInfo,
        shelfLifeDays: Int = 3,
        notes: String? = null
    ): Result<String> {
        val now = LocalDateTime.now()
        val leftover = Leftover(
            id = UUID.randomUUID().toString(),
            userId = userId,
            mealId = mealId,
            recipeId = recipeId,
            name = name,
            quantity = quantity,
            unit = unit,
            storageDate = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            expiryDate = now.plusDays(shelfLifeDays.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            storageLocation = storageLocation,
            containerType = containerType,
            nutritionInfo = com.google.gson.Gson().toJson(nutritionInfo),
            notes = notes
        )
        
        return repository.createLeftover(leftover)
    }
}

class GetActiveLeftoversUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    operator fun invoke(userId: String): Flow<List<Leftover>> {
        return repository.getActiveLeftovers(userId)
    }
}

class GetLeftoverSuggestionsUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(
        leftoverIds: List<String>,
        additionalIngredients: List<String> = emptyList(),
        maxPrepTime: Int = 30
    ): Result<LeftoverSuggestionResponse> {
        val request = LeftoverSuggestionRequest(
            leftoverIds = leftoverIds,
            additionalIngredients = additionalIngredients,
            maxPrepTime = maxPrepTime
        )
        return repository.getLeftoverSuggestions(request)
    }
}

class MarkLeftoverConsumedUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(leftoverId: String): Result<Unit> {
        return repository.markLeftoverAsConsumed(leftoverId)
    }
}

class GetExpiringLeftoversUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(userId: String, days: Int = 2): Result<List<Leftover>> {
        return repository.getLeftoversExpiringWithin(userId, days)
    }
}

class CleanupExpiredLeftoversUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(userId: String): Result<Int> {
        return repository.cleanupExpiredLeftovers(userId)
    }
}

class GetStorageRecommendationsUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(recipeId: String): Result<List<ContainerType>> {
        return repository.getStorageRecommendations(recipeId)
    }
}

class GetReheatingInstructionsUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(leftoverId: String): Result<List<ReheatingInstruction>> {
        return repository.getReheatingInstructions(leftoverId)
    }
}

class CreateLeftoverCombinationUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        leftoverIds: List<String>,
        reheatingInstructions: List<ReheatingInstruction>,
        additionalIngredients: List<Ingredient> = emptyList(),
        prepTime: Int,
        servings: Int,
        nutritionInfo: NutritionInfo
    ): Result<String> {
        val combination = LeftoverCombination(
            id = UUID.randomUUID().toString(),
            name = name,
            description = description,
            leftoverIds = com.google.gson.Gson().toJson(leftoverIds),
            reheatingInstructions = com.google.gson.Gson().toJson(reheatingInstructions),
            additionalIngredients = com.google.gson.Gson().toJson(additionalIngredients),
            prepTime = prepTime,
            servings = servings,
            nutritionInfo = com.google.gson.Gson().toJson(nutritionInfo)
        )
        
        return repository.saveLeftoverCombination(combination)
    }
}

class GetLeftoverWasteAnalyticsUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(userId: String, days: Int = 30): Result<LeftoverWasteAnalytics> {
        return repository.getLeftoverWasteAnalytics(userId, days)
    }
}

class GetOptimalMealPrepScheduleUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(
        recipeIds: List<String>,
        targetDate: LocalDateTime = LocalDateTime.now().plusDays(1)
    ): Result<MealPrepSchedule> {
        return repository.getOptimalMealPrepSchedule(recipeIds, targetDate)
    }
}

class UpdateLeftoverUseCase @Inject constructor(
    private val repository: MealPrepRepository
) {
    suspend operator fun invoke(leftover: Leftover): Result<Unit> {
        val updatedLeftover = leftover.copy(
            updatedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        )
        return repository.updateLeftover(updatedLeftover)
    }
}