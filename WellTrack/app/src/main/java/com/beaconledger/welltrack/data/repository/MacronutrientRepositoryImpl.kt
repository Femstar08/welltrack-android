package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.MacronutrientDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MacronutrientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MacronutrientRepositoryImpl @Inject constructor(
    private val macronutrientDao: MacronutrientDao
) : MacronutrientRepository {

    override suspend fun setDailyTargets(
        userId: String,
        date: LocalDate,
        targets: MacronutrientTarget
    ): Result<String> {
        return try {
            macronutrientDao.insertTarget(targets)
            Result.success(targets.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getDailyTargets(userId: String, date: LocalDate): MacronutrientTarget? {
        return macronutrientDao.getTargetForDate(userId, date)
    }

    override fun getTargetsForDateRange(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<MacronutrientTarget>> {
        return macronutrientDao.getTargetsForDateRange(userId, startDate, endDate)
    }

    override suspend fun logNutrientIntake(
        userId: String,
        intake: MacronutrientIntake
    ): Result<String> {
        return try {
            macronutrientDao.insertIntake(intake)
            Result.success(intake.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logMealNutrients(
        userId: String,
        mealId: String,
        nutrients: MacronutrientIntake
    ): Result<String> {
        return try {
            val mealIntake = nutrients.copy(
                userId = userId,
                mealId = mealId,
                source = NutrientSource.MEAL
            )
            macronutrientDao.insertIntake(mealIntake)
            Result.success(mealIntake.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logSupplementNutrients(
        userId: String,
        supplementId: String,
        nutrients: MacronutrientIntake
    ): Result<String> {
        return try {
            val supplementIntake = nutrients.copy(
                userId = userId,
                supplementId = supplementId,
                source = NutrientSource.SUPPLEMENT
            )
            macronutrientDao.insertIntake(supplementIntake)
            Result.success(supplementIntake.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDailySummary(userId: String, date: LocalDate): Flow<MacronutrientSummary> {
        return combine(
            macronutrientDao.getIntakeForDate(userId, date),
            macronutrientDao.getTargetsForDateRange(userId, date, date)
        ) { intakeList, targetList ->
            val target = targetList.firstOrNull()
            val totalCalories = intakeList.sumOf { it.calories }
            val totalProtein = intakeList.sumOf { it.proteinGrams }
            val totalCarbs = intakeList.sumOf { it.carbsGrams }
            val totalFat = intakeList.sumOf { it.fatGrams }
            val totalFiber = intakeList.sumOf { it.fiberGrams }
            val totalWater = intakeList.sumOf { it.waterMl }

            // Aggregate custom nutrients
            val customNutrientTotals = mutableMapOf<String, Double>()
            intakeList.forEach { intake ->
                intake.customNutrients.forEach { (nutrient, value) ->
                    customNutrientTotals[nutrient] = customNutrientTotals.getOrDefault(nutrient, 0.0) + value
                }
            }

            // Calculate progress percentages
            val caloriesProgress = if (target?.caloriesTarget != null && target.caloriesTarget > 0) {
                (totalCalories.toFloat() / target.caloriesTarget.toFloat()).coerceAtMost(1.0f)
            } else 0f

            val proteinProgress = if (target?.proteinGrams != null && target.proteinGrams > 0) {
                (totalProtein.toFloat() / target.proteinGrams.toFloat()).coerceAtMost(1.0f)
            } else 0f

            val carbsProgress = if (target?.carbsGrams != null && target.carbsGrams > 0) {
                (totalCarbs.toFloat() / target.carbsGrams.toFloat()).coerceAtMost(1.0f)
            } else 0f

            val fatProgress = if (target?.fatGrams != null && target.fatGrams > 0) {
                (totalFat.toFloat() / target.fatGrams.toFloat()).coerceAtMost(1.0f)
            } else 0f

            val fiberProgress = if (target?.fiberGrams != null && target.fiberGrams > 0) {
                (totalFiber.toFloat() / target.fiberGrams.toFloat()).coerceAtMost(1.0f)
            } else 0f

            val waterProgress = if (target?.waterMl != null && target.waterMl > 0) {
                (totalWater.toFloat() / target.waterMl.toFloat()).coerceAtMost(1.0f)
            } else 0f

            // Calculate custom nutrient progress
            val customNutrientProgress = mutableMapOf<String, Float>()
            target?.customNutrients?.forEach { (nutrient, targetValue) ->
                val actualValue = customNutrientTotals[nutrient] ?: 0.0
                if (targetValue > 0) {
                    customNutrientProgress[nutrient] = (actualValue.toFloat() / targetValue.toFloat()).coerceAtMost(1.0f)
                }
            }

            MacronutrientSummary(
                userId = userId,
                date = date,
                targets = target,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat,
                totalFiber = totalFiber,
                totalWater = totalWater,
                customNutrientTotals = customNutrientTotals,
                caloriesProgress = caloriesProgress,
                proteinProgress = proteinProgress,
                carbsProgress = carbsProgress,
                fatProgress = fatProgress,
                fiberProgress = fiberProgress,
                waterProgress = waterProgress,
                customNutrientProgress = customNutrientProgress
            )
        }
    }

    override fun getWeeklySummary(userId: String, weekStartDate: LocalDate): Flow<List<MacronutrientSummary>> {
        val weekEndDate = weekStartDate.plusDays(6)
        return combine(
            macronutrientDao.getIntakeForDateRange(userId, weekStartDate, weekEndDate),
            macronutrientDao.getTargetsForDateRange(userId, weekStartDate, weekEndDate)
        ) { intakeList, targetList ->
            val summaries = mutableListOf<MacronutrientSummary>()
            
            for (i in 0..6) {
                val currentDate = weekStartDate.plusDays(i.toLong())
                val dayIntake = intakeList.filter { it.date == currentDate }
                val dayTarget = targetList.find { it.date == currentDate }
                
                val totalCalories = dayIntake.sumOf { it.calories }
                val totalProtein = dayIntake.sumOf { it.proteinGrams }
                val totalCarbs = dayIntake.sumOf { it.carbsGrams }
                val totalFat = dayIntake.sumOf { it.fatGrams }
                val totalFiber = dayIntake.sumOf { it.fiberGrams }
                val totalWater = dayIntake.sumOf { it.waterMl }

                val customNutrientTotals = mutableMapOf<String, Double>()
                dayIntake.forEach { intake ->
                    intake.customNutrients.forEach { (nutrient, value) ->
                        customNutrientTotals[nutrient] = customNutrientTotals.getOrDefault(nutrient, 0.0) + value
                    }
                }

                summaries.add(
                    MacronutrientSummary(
                        userId = userId,
                        date = currentDate,
                        targets = dayTarget,
                        totalCalories = totalCalories,
                        totalProtein = totalProtein,
                        totalCarbs = totalCarbs,
                        totalFat = totalFat,
                        totalFiber = totalFiber,
                        totalWater = totalWater,
                        customNutrientTotals = customNutrientTotals,
                        caloriesProgress = calculateProgress(totalCalories.toDouble(), dayTarget?.caloriesTarget?.toDouble()),
                        proteinProgress = calculateProgress(totalProtein, dayTarget?.proteinGrams),
                        carbsProgress = calculateProgress(totalCarbs, dayTarget?.carbsGrams),
                        fatProgress = calculateProgress(totalFat, dayTarget?.fatGrams),
                        fiberProgress = calculateProgress(totalFiber, dayTarget?.fiberGrams),
                        waterProgress = calculateProgress(totalWater.toDouble(), dayTarget?.waterMl?.toDouble()),
                        customNutrientProgress = emptyMap()
                    )
                )
            }
            summaries
        }
    }

    override fun getMonthlySummary(userId: String, month: Int, year: Int): Flow<List<MacronutrientSummary>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        
        return combine(
            macronutrientDao.getIntakeForDateRange(userId, startDate, endDate),
            macronutrientDao.getTargetsForDateRange(userId, startDate, endDate)
        ) { intakeList, targetList ->
            val summaries = mutableListOf<MacronutrientSummary>()
            var currentDate = startDate
            
            while (!currentDate.isAfter(endDate)) {
                val dayIntake = intakeList.filter { it.date == currentDate }
                val dayTarget = targetList.find { it.date == currentDate }
                
                val totalCalories = dayIntake.sumOf { it.calories }
                val totalProtein = dayIntake.sumOf { it.proteinGrams }
                val totalCarbs = dayIntake.sumOf { it.carbsGrams }
                val totalFat = dayIntake.sumOf { it.fatGrams }
                val totalFiber = dayIntake.sumOf { it.fiberGrams }
                val totalWater = dayIntake.sumOf { it.waterMl }

                summaries.add(
                    MacronutrientSummary(
                        userId = userId,
                        date = currentDate,
                        targets = dayTarget,
                        totalCalories = totalCalories,
                        totalProtein = totalProtein,
                        totalCarbs = totalCarbs,
                        totalFat = totalFat,
                        totalFiber = totalFiber,
                        totalWater = totalWater,
                        customNutrientTotals = emptyMap(),
                        caloriesProgress = calculateProgress(totalCalories.toDouble(), dayTarget?.caloriesTarget?.toDouble()),
                        proteinProgress = calculateProgress(totalProtein, dayTarget?.proteinGrams),
                        carbsProgress = calculateProgress(totalCarbs, dayTarget?.carbsGrams),
                        fatProgress = calculateProgress(totalFat, dayTarget?.fatGrams),
                        fiberProgress = calculateProgress(totalFiber, dayTarget?.fiberGrams),
                        waterProgress = calculateProgress(totalWater.toDouble(), dayTarget?.waterMl?.toDouble()),
                        customNutrientProgress = emptyMap()
                    )
                )
                currentDate = currentDate.plusDays(1)
            }
            summaries
        }
    }

    override suspend fun getTotalProteinForDate(userId: String, date: LocalDate): Double {
        return macronutrientDao.getTotalProteinForDate(userId, date) ?: 0.0
    }

    override suspend fun getTotalFiberForDate(userId: String, date: LocalDate): Double {
        return macronutrientDao.getTotalFiberForDate(userId, date) ?: 0.0
    }

    override suspend fun getTotalWaterForDate(userId: String, date: LocalDate): Int {
        return macronutrientDao.getTotalWaterForDate(userId, date) ?: 0
    }

    override suspend fun getTotalCaloriesForDate(userId: String, date: LocalDate): Int {
        return macronutrientDao.getTotalCaloriesForDate(userId, date) ?: 0
    }

    override fun getProteinProgress(userId: String, date: LocalDate): Flow<Float> {
        return getDailySummary(userId, date).map { it.proteinProgress }
    }

    override fun getFiberProgress(userId: String, date: LocalDate): Flow<Float> {
        return getDailySummary(userId, date).map { it.fiberProgress }
    }

    override fun getWaterProgress(userId: String, date: LocalDate): Flow<Float> {
        return getDailySummary(userId, date).map { it.waterProgress }
    }

    override fun getCalorieProgress(userId: String, date: LocalDate): Flow<Float> {
        return getDailySummary(userId, date).map { it.caloriesProgress }
    }

    override suspend fun calculateProteinTarget(
        userId: String,
        bodyWeight: Double,
        activityLevel: ActivityLevel,
        goal: FitnessGoal
    ): ProteinTarget {
        val gramsPerKg = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2
            ActivityLevel.LIGHT -> 1.4
            ActivityLevel.MODERATE -> 1.6
            ActivityLevel.MODERATELY_ACTIVE -> 1.6
            ActivityLevel.ACTIVE -> 1.8
            ActivityLevel.VERY_ACTIVE -> 2.0
        }
        
        // Adjust based on fitness goal
        val adjustedGramsPerKg = when (goal) {
            FitnessGoal.MUSCLE_GAIN -> gramsPerKg * 1.2
            FitnessGoal.WEIGHT_LOSS -> gramsPerKg * 1.1
            FitnessGoal.MAINTENANCE -> gramsPerKg
            FitnessGoal.ENDURANCE -> gramsPerKg * 1.1
            FitnessGoal.STRENGTH -> gramsPerKg * 1.15
            FitnessGoal.VO2_MAX_IMPROVEMENT -> gramsPerKg * 1.1
            FitnessGoal.STRENGTH -> gramsPerKg * 1.15
        }
        
        return ProteinTarget(
            userId = userId,
            bodyWeightKg = bodyWeight,
            activityLevel = activityLevel,
            goal = goal,
            recommendedGramsPerKg = adjustedGramsPerKg,
            totalTargetGrams = bodyWeight * adjustedGramsPerKg
        )
    }

    override suspend fun calculateFiberTarget(userId: String, age: Int, gender: Gender): FiberTarget {
        val recommendedGrams = when {
            age < 50 && gender == Gender.MALE -> 38.0
            age < 50 && gender == Gender.FEMALE -> 25.0
            age >= 50 && gender == Gender.MALE -> 30.0
            age >= 50 && gender == Gender.FEMALE -> 21.0
            else -> 25.0 // Default for other genders
        }
        
        return FiberTarget(
            userId = userId,
            age = age,
            gender = gender,
            recommendedGrams = recommendedGrams
        )
    }

    override suspend fun calculateWaterTarget(
        userId: String,
        bodyWeight: Double,
        activityLevel: ActivityLevel
    ): Int {
        // Base water intake: 35ml per kg of body weight
        val baseWaterMl = (bodyWeight * 35).toInt()
        
        // Adjust for activity level
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

    override suspend fun addCustomNutrient(userId: String, nutrient: CustomNutrient): Result<String> {
        return try {
            macronutrientDao.insertCustomNutrient(nutrient)
            Result.success(nutrient.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCustomNutrient(nutrient: CustomNutrient): Result<Unit> {
        return try {
            macronutrientDao.updateCustomNutrient(nutrient)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeCustomNutrient(nutrientId: String): Result<Unit> {
        return try {
            macronutrientDao.deactivateCustomNutrient(nutrientId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveCustomNutrients(userId: String): Flow<List<CustomNutrient>> {
        return macronutrientDao.getActiveCustomNutrients(userId)
    }

    override fun getNutrientTrends(userId: String, nutrientName: String, days: Int): Flow<NutrientTrend> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        return combine(
            macronutrientDao.getIntakeForDateRange(userId, startDate, endDate),
            macronutrientDao.getTargetsForDateRange(userId, startDate, endDate)
        ) { intakeList, targetList ->
            val dates = mutableListOf<LocalDate>()
            val values = mutableListOf<Double>()
            val targets = mutableListOf<Double>()
            
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                dates.add(currentDate)
                
                val dayIntake = intakeList.filter { it.date == currentDate }
                val dayTarget = targetList.find { it.date == currentDate }
                
                val value = when (nutrientName.lowercase()) {
                    "protein" -> dayIntake.sumOf { it.proteinGrams }
                    "carbs", "carbohydrates" -> dayIntake.sumOf { it.carbsGrams }
                    "fat", "fats" -> dayIntake.sumOf { it.fatGrams }
                    "fiber" -> dayIntake.sumOf { it.fiberGrams }
                    "water" -> dayIntake.sumOf { it.waterMl }.toDouble()
                    "calories" -> dayIntake.sumOf { it.calories }.toDouble()
                    else -> {
                        // Custom nutrient
                        dayIntake.sumOf { intake ->
                            intake.customNutrients[nutrientName] ?: 0.0
                        }
                    }
                }
                
                val target = when (nutrientName.lowercase()) {
                    "protein" -> dayTarget?.proteinGrams ?: 0.0
                    "carbs", "carbohydrates" -> dayTarget?.carbsGrams ?: 0.0
                    "fat", "fats" -> dayTarget?.fatGrams ?: 0.0
                    "fiber" -> dayTarget?.fiberGrams ?: 0.0
                    "water" -> dayTarget?.waterMl?.toDouble() ?: 0.0
                    "calories" -> dayTarget?.caloriesTarget?.toDouble() ?: 0.0
                    else -> dayTarget?.customNutrients?.get(nutrientName) ?: 0.0
                }
                
                values.add(value)
                targets.add(target)
                
                currentDate = currentDate.plusDays(1)
            }
            
            val averageIntake = if (values.isNotEmpty()) values.average() else 0.0
            val averageTarget = if (targets.isNotEmpty()) targets.average() else 0.0
            val adherenceRate = if (averageTarget > 0) (averageIntake / averageTarget).toFloat().coerceAtMost(1.0f) else 0f
            
            // Calculate trend direction
            val trend = if (values.size >= 2) {
                val firstHalf = values.take(values.size / 2).average()
                val secondHalf = values.drop(values.size / 2).average()
                when {
                    secondHalf > firstHalf * 1.05 -> TrendDirection.INCREASING
                    secondHalf < firstHalf * 0.95 -> TrendDirection.DECREASING
                    else -> TrendDirection.STABLE
                }
            } else TrendDirection.STABLE
            
            NutrientTrend(
                nutrientName = nutrientName,
                dates = dates,
                values = values,
                targets = targets,
                averageIntake = averageIntake,
                averageTarget = averageTarget,
                adherenceRate = adherenceRate,
                trend = trend
            )
        }
    }

    override fun getMacronutrientBalance(userId: String, date: LocalDate): Flow<MacronutrientBalance> {
        return macronutrientDao.getIntakeForDate(userId, date).map { intakeList ->
            val totalCalories = intakeList.sumOf { it.calories }
            val totalProtein = intakeList.sumOf { it.proteinGrams }
            val totalCarbs = intakeList.sumOf { it.carbsGrams }
            val totalFat = intakeList.sumOf { it.fatGrams }
            
            val caloriesFromProtein = (totalProtein * 4).toInt()
            val caloriesFromCarbs = (totalCarbs * 4).toInt()
            val caloriesFromFat = (totalFat * 9).toInt()
            
            val proteinPercentage = if (totalCalories > 0) (caloriesFromProtein.toFloat() / totalCalories) * 100 else 0f
            val carbsPercentage = if (totalCalories > 0) (caloriesFromCarbs.toFloat() / totalCalories) * 100 else 0f
            val fatPercentage = if (totalCalories > 0) (caloriesFromFat.toFloat() / totalCalories) * 100 else 0f
            
            // Check if macronutrient distribution is balanced (rough guidelines)
            val isBalanced = proteinPercentage in 10f..35f && 
                           carbsPercentage in 45f..65f && 
                           fatPercentage in 20f..35f
            
            val recommendations = mutableListOf<String>()
            if (proteinPercentage < 10f) recommendations.add("Consider increasing protein intake")
            if (proteinPercentage > 35f) recommendations.add("Consider reducing protein intake")
            if (carbsPercentage < 45f) recommendations.add("Consider increasing carbohydrate intake")
            if (carbsPercentage > 65f) recommendations.add("Consider reducing carbohydrate intake")
            if (fatPercentage < 20f) recommendations.add("Consider increasing healthy fat intake")
            if (fatPercentage > 35f) recommendations.add("Consider reducing fat intake")
            
            MacronutrientBalance(
                date = date,
                caloriesFromProtein = caloriesFromProtein,
                caloriesFromCarbs = caloriesFromCarbs,
                caloriesFromFat = caloriesFromFat,
                proteinPercentage = proteinPercentage,
                carbsPercentage = carbsPercentage,
                fatPercentage = fatPercentage,
                isBalanced = isBalanced,
                recommendations = recommendations
            )
        }
    }

    override suspend fun importNutrientsFromMeal(userId: String, mealId: String, date: LocalDate): Result<Unit> {
        // This would integrate with the meal repository to extract nutrients from a meal
        // For now, return success as this would be implemented when integrating with meal data
        return Result.success(Unit)
    }

    override suspend fun importNutrientsFromRecipe(
        userId: String,
        recipeId: String,
        servings: Float,
        date: LocalDate
    ): Result<Unit> {
        // This would integrate with the recipe repository to extract nutrients from a recipe
        // For now, return success as this would be implemented when integrating with recipe data
        return Result.success(Unit)
    }

    override suspend fun clearIntakeForDate(userId: String, date: LocalDate): Result<Unit> {
        return try {
            macronutrientDao.deleteIntakeForDate(userId, date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun calculateProgress(actual: Double?, target: Double?): Float {
        return if (target != null && target > 0 && actual != null) {
            (actual.toFloat() / target.toFloat()).coerceAtMost(1.0f)
        } else 0f
    }
}

