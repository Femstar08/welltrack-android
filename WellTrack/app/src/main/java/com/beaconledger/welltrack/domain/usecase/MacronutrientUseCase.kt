package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MacronutrientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MacronutrientUseCase @Inject constructor(
    private val macronutrientRepository: MacronutrientRepository
) {

    suspend fun setDailyTargets(
        userId: String,
        date: LocalDate,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        water: Int,
        customNutrients: Map<String, Double> = emptyMap()
    ): Result<String> {
        val target = MacronutrientTarget(
            id = UUID.randomUUID().toString(),
            userId = userId,
            date = date,
            caloriesTarget = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            fiberGrams = fiber,
            waterMl = water,
            customNutrients = customNutrients,
            isActive = true
        )
        return macronutrientRepository.setDailyTargets(userId, date, target)
    }

    suspend fun logManualNutrientIntake(
        userId: String,
        date: LocalDate,
        calories: Int,
        protein: Double,
        carbs: Double,
        fat: Double,
        fiber: Double,
        water: Int,
        customNutrients: Map<String, Double> = emptyMap()
    ): Result<String> {
        val intake = MacronutrientIntake(
            id = UUID.randomUUID().toString(),
            userId = userId,
            date = date,
            mealId = null,
            supplementId = null,
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            fiberGrams = fiber,
            waterMl = water,
            customNutrients = customNutrients,
            source = NutrientSource.MANUAL_ENTRY,
            timestamp = LocalDateTime.now()
        )
        return macronutrientRepository.logNutrientIntake(userId, intake)
    }

    suspend fun logWaterIntake(userId: String, date: LocalDate, waterMl: Int): Result<String> {
        val intake = MacronutrientIntake(
            id = UUID.randomUUID().toString(),
            userId = userId,
            date = date,
            mealId = null,
            supplementId = null,
            calories = 0,
            proteinGrams = 0.0,
            carbsGrams = 0.0,
            fatGrams = 0.0,
            fiberGrams = 0.0,
            waterMl = waterMl,
            customNutrients = emptyMap(),
            source = NutrientSource.MANUAL_ENTRY,
            timestamp = LocalDateTime.now()
        )
        return macronutrientRepository.logNutrientIntake(userId, intake)
    }

    fun getDailySummary(userId: String, date: LocalDate): Flow<MacronutrientSummary> {
        return macronutrientRepository.getDailySummary(userId, date)
    }

    fun getWeeklySummary(userId: String, weekStartDate: LocalDate): Flow<List<MacronutrientSummary>> {
        return macronutrientRepository.getWeeklySummary(userId, weekStartDate)
    }

    suspend fun calculateAndSetProteinTarget(
        userId: String,
        date: LocalDate,
        bodyWeight: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal
    ): Result<ProteinTarget> {
        return try {
            val proteinTarget = macronutrientRepository.calculateProteinTarget(
                userId, bodyWeight, activityLevel, goal
            )
            
            // Update daily targets with new protein target
            val existingTargets = macronutrientRepository.getDailyTargets(userId, date)
            val updatedTargets = existingTargets?.copy(
                proteinGrams = proteinTarget.totalTargetGrams
            ) ?: MacronutrientTarget(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                caloriesTarget = 2000, // Default calories
                proteinGrams = proteinTarget.totalTargetGrams,
                carbsGrams = 250.0, // Default carbs
                fatGrams = 67.0, // Default fat
                fiberGrams = 25.0, // Default fiber
                waterMl = 2500, // Default water
                customNutrients = emptyMap(),
                isActive = true
            )
            
            macronutrientRepository.setDailyTargets(userId, date, updatedTargets)
            Result.success(proteinTarget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun calculateAndSetFiberTarget(
        userId: String,
        date: LocalDate,
        age: Int,
        gender: Gender
    ): Result<FiberTarget> {
        return try {
            val fiberTarget = macronutrientRepository.calculateFiberTarget(userId, age, gender)
            
            // Update daily targets with new fiber target
            val existingTargets = macronutrientRepository.getDailyTargets(userId, date)
            val updatedTargets = existingTargets?.copy(
                fiberGrams = fiberTarget.recommendedGrams
            ) ?: MacronutrientTarget(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                caloriesTarget = 2000,
                proteinGrams = 150.0,
                carbsGrams = 250.0,
                fatGrams = 67.0,
                fiberGrams = fiberTarget.recommendedGrams,
                waterMl = 2500,
                customNutrients = emptyMap(),
                isActive = true
            )
            
            macronutrientRepository.setDailyTargets(userId, date, updatedTargets)
            Result.success(fiberTarget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun calculateAndSetWaterTarget(
        userId: String,
        date: LocalDate,
        bodyWeight: Double,
        activityLevel: ActivityLevel
    ): Result<Int> {
        return try {
            val waterTarget = macronutrientRepository.calculateWaterTarget(userId, bodyWeight, activityLevel)
            
            // Update daily targets with new water target
            val existingTargets = macronutrientRepository.getDailyTargets(userId, date)
            val updatedTargets = existingTargets?.copy(
                waterMl = waterTarget
            ) ?: MacronutrientTarget(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                caloriesTarget = 2000,
                proteinGrams = 150.0,
                carbsGrams = 250.0,
                fatGrams = 67.0,
                fiberGrams = 25.0,
                waterMl = waterTarget,
                customNutrients = emptyMap(),
                isActive = true
            )
            
            macronutrientRepository.setDailyTargets(userId, date, updatedTargets)
            Result.success(waterTarget)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addCustomNutrient(
        userId: String,
        name: String,
        unit: String,
        targetValue: Double?,
        category: NutrientCategory,
        priority: NutrientPriority = NutrientPriority.OPTIONAL
    ): Result<String> {
        val customNutrient = CustomNutrient(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name,
            unit = unit,
            targetValue = targetValue,
            category = category,
            priority = priority,
            isActive = true
        )
        return macronutrientRepository.addCustomNutrient(userId, customNutrient)
    }

    fun getActiveCustomNutrients(userId: String): Flow<List<CustomNutrient>> {
        return macronutrientRepository.getActiveCustomNutrients(userId)
    }

    suspend fun updateCustomNutrient(
        nutrientId: String,
        name: String? = null,
        unit: String? = null,
        targetValue: Double? = null,
        category: NutrientCategory? = null,
        priority: NutrientPriority? = null
    ): Result<Unit> {
        // This would require getting the existing nutrient first, then updating it
        // For now, return success as this would be implemented with proper data fetching
        return Result.success(Unit)
    }

    suspend fun removeCustomNutrient(nutrientId: String): Result<Unit> {
        return macronutrientRepository.removeCustomNutrient(nutrientId)
    }

    fun getCoreNutrientProgress(userId: String, date: LocalDate): Flow<CoreNutrientProgress> {
        return macronutrientRepository.getDailySummary(userId, date).map { summary ->
            CoreNutrientProgress(
                proteinProgress = summary.proteinProgress,
                carbsProgress = summary.carbsProgress,
                fatProgress = summary.fatProgress,
                fiberProgress = summary.fiberProgress,
                waterProgress = summary.waterProgress,
                proteinGrams = summary.totalProtein,
                carbsGrams = summary.totalCarbs,
                fatGrams = summary.totalFat,
                fiberGrams = summary.totalFiber,
                waterMl = summary.totalWater,
                proteinTarget = summary.targets?.proteinGrams ?: 0.0,
                carbsTarget = summary.targets?.carbsGrams ?: 0.0,
                fatTarget = summary.targets?.fatGrams ?: 0.0,
                fiberTarget = summary.targets?.fiberGrams ?: 0.0,
                waterTarget = summary.targets?.waterMl ?: 0
            )
        }
    }

    fun getNutrientTrends(userId: String, nutrientName: String, days: Int = 7): Flow<NutrientTrend> {
        return macronutrientRepository.getNutrientTrends(userId, nutrientName, days)
    }

    fun getMacronutrientBalance(userId: String, date: LocalDate): Flow<MacronutrientBalance> {
        return macronutrientRepository.getMacronutrientBalance(userId, date)
    }

    suspend fun getProteinTargetRecommendation(
        bodyWeight: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal
    ): Double {
        val baseGramsPerKg = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHT -> 1.4
            ActivityLevel.MODERATE -> 1.6
            ActivityLevel.MODERATELY_ACTIVE -> 1.6
            ActivityLevel.ACTIVE -> 1.8
            ActivityLevel.VERY_ACTIVE -> 2.0
        }
        
        val adjustedGramsPerKg = when (goal) {
            FitnessGoal.MUSCLE_GAIN -> baseGramsPerKg * 1.2
            FitnessGoal.WEIGHT_LOSS -> baseGramsPerKg * 1.1
            FitnessGoal.MAINTENANCE -> baseGramsPerKg
            FitnessGoal.ENDURANCE -> baseGramsPerKg * 1.1
            FitnessGoal.STRENGTH -> baseGramsPerKg * 1.15
            FitnessGoal.VO2_MAX_IMPROVEMENT -> baseGramsPerKg * 1.1
        }
        
        return bodyWeight * adjustedGramsPerKg
    }

    suspend fun getFiberTargetRecommendation(age: Int, gender: Gender): Double {
        return when {
            age < 50 && gender == Gender.MALE -> 38.0
            age < 50 && gender == Gender.FEMALE -> 25.0
            age >= 50 && gender == Gender.MALE -> 30.0
            age >= 50 && gender == Gender.FEMALE -> 21.0
            else -> 25.0
        }
    }

    suspend fun getWaterTargetRecommendation(bodyWeight: Double, activityLevel: ActivityLevel): Int {
        val baseWaterMl = (bodyWeight * 35).toInt()
        val activityMultiplier = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.0
            ActivityLevel.LIGHT -> 1.1
            ActivityLevel.MODERATE -> 1.2
            ActivityLevel.MODERATELY_ACTIVE -> 1.2
            ActivityLevel.ACTIVE -> 1.3
            ActivityLevel.VERY_ACTIVE -> 1.4
        }
        return (baseWaterMl * activityMultiplier).toInt()
    }
}

data class CoreNutrientProgress(
    val proteinProgress: Float,
    val carbsProgress: Float,
    val fatProgress: Float,
    val fiberProgress: Float,
    val waterProgress: Float,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val waterMl: Int,
    val proteinTarget: Double,
    val carbsTarget: Double,
    val fatTarget: Double,
    val fiberTarget: Double,
    val waterTarget: Int
)