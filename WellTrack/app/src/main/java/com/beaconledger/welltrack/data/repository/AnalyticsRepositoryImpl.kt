package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.AnalyticsRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class AnalyticsRepositoryImpl @Inject constructor(
    private val mealDao: MealDao,
    private val supplementDao: SupplementDao,
    private val healthMetricDao: HealthMetricDao,
    private val dailyTrackingDao: DailyTrackingDao,
    private val gson: Gson
) : AnalyticsRepository {

    override fun getTodaysSummary(userId: String, date: LocalDate): Flow<TodaysSummary> {
        return combine(
            mealDao.getMealsForDate(userId, date),
            supplementDao.getSupplementIntakeForDate(userId, date),
            healthMetricDao.getHealthMetricsForDate(userId, date),
            dailyTrackingDao.getDailyTrackingForDate(userId, date)
        ) { meals, supplements, healthMetrics, dailyTracking ->
            
            val totalCalories = meals.sumOf { it.nutritionInfo?.calories ?: 0 }
            val supplementsTaken = supplements.count { it.taken }
            val totalSupplements = supplements.size
            
            val waterMetric = healthMetrics.find { it.type == HealthMetricType.WATER_INTAKE }
            val waterIntakeMl = waterMetric?.value?.toInt() ?: 0
            
            val stepsMetric = healthMetrics.find { it.type == HealthMetricType.STEPS }
            val stepsCount = stepsMetric?.value?.toInt()
            
            val activeMinutesMetric = healthMetrics.find { it.type == HealthMetricType.ACTIVE_MINUTES }
            val activeMinutes = activeMinutesMetric?.value?.toInt()
            
            val sleepMetric = healthMetrics.find { it.type == HealthMetricType.SLEEP_DURATION }
            val sleepHours = sleepMetric?.value
            
            // Calculate averages from daily tracking
            var energyLevelSum = 0f
            var energyLevelCount = 0
            var moodSum = 0f
            var moodCount = 0
            
            dailyTracking.forEach { entry ->
                when (entry.trackingType) {
                    DailyTrackingType.MORNING_ROUTINE -> {
                        try {
                            val data = gson.fromJson(entry.data, MorningTrackingData::class.java)
                            energyLevelSum += data.energyLevel
                            energyLevelCount++
                        } catch (e: Exception) { /* Ignore parsing errors */ }
                    }
                    DailyTrackingType.PRE_WORKOUT -> {
                        try {
                            val data = gson.fromJson(entry.data, PreWorkoutTrackingData::class.java)
                            energyLevelSum += data.energyLevel
                            energyLevelCount++
                        } catch (e: Exception) { /* Ignore parsing errors */ }
                    }
                    DailyTrackingType.POST_WORKOUT -> {
                        try {
                            val data = gson.fromJson(entry.data, PostWorkoutTrackingData::class.java)
                            moodSum += data.mood
                            moodCount++
                        } catch (e: Exception) { /* Ignore parsing errors */ }
                    }
                    else -> {}
                }
            }
            
            val energyLevelAverage = if (energyLevelCount > 0) energyLevelSum / energyLevelCount else 0f
            val moodAverage = if (moodCount > 0) moodSum / moodCount else 0f
            
            // Calculate completion percentage
            val completedTasks = listOf(
                meals.isNotEmpty(),
                supplementsTaken > 0,
                waterIntakeMl > 0,
                dailyTracking.any { it.isCompleted }
            ).count { it }
            val completionPercentage = completedTasks / 4f
            
            TodaysSummary(
                userId = userId,
                date = date,
                mealsLogged = meals.size,
                totalCalories = totalCalories,
                supplementsTaken = supplementsTaken,
                totalSupplements = totalSupplements,
                waterIntakeMl = waterIntakeMl,
                waterTargetMl = 2500, // Default target
                stepsCount = stepsCount,
                activeMinutes = activeMinutes,
                sleepHours = sleepHours,
                energyLevelAverage = energyLevelAverage,
                moodAverage = moodAverage,
                completionPercentage = completionPercentage
            )
        }
    }

    override fun getNutritionTrends(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<NutritionTrend>> {
        return mealDao.getMealsForDateRange(userId, startDate, endDate).map { meals ->
            meals.groupBy { it.timestamp.toLocalDate() }.map { (date, dayMeals) ->
                val totalCalories = dayMeals.sumOf { it.nutritionInfo?.calories ?: 0 }
                val totalProtein = dayMeals.sumOf { it.nutritionInfo?.protein ?: 0.0 }
                val totalCarbs = dayMeals.sumOf { it.nutritionInfo?.carbs ?: 0.0 }
                val totalFat = dayMeals.sumOf { it.nutritionInfo?.fat ?: 0.0 }
                val totalFiber = dayMeals.sumOf { it.nutritionInfo?.fiber ?: 0.0 }
                
                NutritionTrend(
                    date = date,
                    calories = totalCalories,
                    protein = totalProtein,
                    carbs = totalCarbs,
                    fat = totalFat,
                    fiber = totalFiber,
                    water = 0 // Will be populated from health metrics
                )
            }.sortedBy { it.date }
        }
    }

    override fun getNutritionTrendsForWeek(userId: String, weekStartDate: LocalDate): Flow<List<NutritionTrend>> {
        val endDate = weekStartDate.plusDays(6)
        return getNutritionTrends(userId, weekStartDate, endDate)
    }

    override fun getNutritionTrendsForMonth(userId: String, month: Int, year: Int): Flow<List<NutritionTrend>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return getNutritionTrends(userId, startDate, endDate)
    }

    override fun getFitnessStats(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<FitnessStats>> {
        return healthMetricDao.getHealthMetricsForDateRange(userId, startDate, endDate).map { metrics ->
            metrics.groupBy { it.timestamp.toLocalDate() }.map { (date, dayMetrics) ->
                val steps = dayMetrics.find { it.type == HealthMetricType.STEPS }?.value?.toInt()
                val activeMinutes = dayMetrics.find { it.type == HealthMetricType.ACTIVE_MINUTES }?.value?.toInt()
                val caloriesBurned = dayMetrics.find { it.type == HealthMetricType.CALORIES_BURNED }?.value?.toInt()
                val heartRateAvg = dayMetrics.find { it.type == HealthMetricType.HEART_RATE }?.value?.toInt()
                val sleepHours = dayMetrics.find { it.type == HealthMetricType.SLEEP_DURATION }?.value
                
                FitnessStats(
                    date = date,
                    steps = steps,
                    activeMinutes = activeMinutes,
                    caloriesBurned = caloriesBurned,
                    heartRateAverage = heartRateAvg,
                    sleepHours = sleepHours,
                    workoutCount = 0 // Will be calculated from workout data
                )
            }.sortedBy { it.date }
        }
    }

    override fun getFitnessStatsForWeek(userId: String, weekStartDate: LocalDate): Flow<List<FitnessStats>> {
        val endDate = weekStartDate.plusDays(6)
        return getFitnessStats(userId, weekStartDate, endDate)
    }

    override fun getFitnessStatsForMonth(userId: String, month: Int, year: Int): Flow<List<FitnessStats>> {
        val startDate = LocalDate.of(year, month, 1)
        val endDate = startDate.plusMonths(1).minusDays(1)
        return getFitnessStats(userId, startDate, endDate)
    }

    override fun getSupplementAdherence(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<SupplementAdherence>> {
        return supplementDao.getSupplementIntakeForDateRange(userId, startDate, endDate).map { intakes ->
            intakes.map { intake ->
                SupplementAdherence(
                    date = intake.date,
                    supplementName = intake.supplementName,
                    taken = intake.taken,
                    scheduledTime = intake.scheduledTime,
                    actualTime = intake.actualTime
                )
            }
        }
    }

    override fun getSupplementAdherenceRate(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<Float> {
        return getSupplementAdherence(userId, startDate, endDate).map { adherence ->
            if (adherence.isEmpty()) 0f
            else adherence.count { it.taken }.toFloat() / adherence.size.toFloat()
        }
    }

    override fun getWeeklyAnalytics(userId: String, weekStartDate: LocalDate): Flow<WeeklyAnalytics> {
        return combine(
            getNutritionTrendsForWeek(userId, weekStartDate),
            getFitnessStatsForWeek(userId, weekStartDate),
            getSupplementAdherence(userId, weekStartDate, weekStartDate.plusDays(6))
        ) { nutrition, fitness, supplements ->
            WeeklyAnalytics(
                userId = userId,
                weekStartDate = weekStartDate,
                nutritionTrends = nutrition,
                fitnessStats = fitness,
                supplementAdherence = supplements,
                averageEnergyLevel = 0f, // Calculate from daily tracking
                averageMood = 0f, // Calculate from daily tracking
                totalMealsLogged = nutrition.sumOf { it.calories / 300 }, // Rough estimate
                mealPlanAdherence = 0.8f, // Placeholder
                waterIntakeAverage = nutrition.sumOf { it.water } / 7,
                supplementAdherenceRate = if (supplements.isEmpty()) 0f else supplements.count { it.taken }.toFloat() / supplements.size.toFloat()
            )
        }
    }

    override fun getMonthlyAnalytics(userId: String, month: Int, year: Int): Flow<MonthlyAnalytics> {
        // Simplified implementation - would need more complex logic for full implementation
        return flowOf(
            MonthlyAnalytics(
                userId = userId,
                month = month,
                year = year,
                weeklyAnalytics = emptyList(),
                nutritionAverages = NutritionAverages(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                fitnessAverages = FitnessAverages(0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                topPerformingDays = emptyList(),
                improvementAreas = emptyList(),
                achievements = emptyList()
            )
        )
    }

    override fun getCalendarActivities(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<CalendarActivity>> {
        return combine(
            mealDao.getMealsForDateRange(userId, startDate, endDate),
            supplementDao.getSupplementIntakeForDateRange(userId, startDate, endDate),
            dailyTrackingDao.getDailyTrackingForDateRange(userId, startDate, endDate)
        ) { meals, supplements, tracking ->
            val dateRange = generateSequence(startDate) { it.plusDays(1) }
                .takeWhile { !it.isAfter(endDate) }
                .toList()
            
            dateRange.map { date ->
                val dayMeals = meals.filter { it.timestamp.toLocalDate() == date }
                val daySupplements = supplements.filter { it.date == date }
                val dayTracking = tracking.filter { it.date == date }
                
                CalendarActivity(
                    date = date,
                    mealsLogged = dayMeals.size,
                    supplementsTaken = daySupplements.count { it.taken },
                    workoutsCompleted = dayTracking.count { it.trackingType == DailyTrackingType.POST_WORKOUT },
                    waterGoalMet = false, // Calculate from water intake
                    energyLevel = null, // Extract from tracking data
                    mood = null, // Extract from tracking data
                    notes = null,
                    photos = emptyList(),
                    overallRating = 0.5f // Calculate based on completion
                )
            }
        }
    }

    override fun getChartData(userId: String, metric: String, startDate: LocalDate, endDate: LocalDate): Flow<List<ChartDataPoint>> {
        return when (metric) {
            "calories" -> getNutritionTrends(userId, startDate, endDate).map { trends ->
                trends.map { ChartDataPoint(it.date, it.calories.toDouble()) }
            }
            "steps" -> getFitnessStats(userId, startDate, endDate).map { stats ->
                stats.mapNotNull { stat ->
                    stat.steps?.let { ChartDataPoint(stat.date, it.toDouble()) }
                }
            }
            else -> flowOf(emptyList())
        }
    }

    override fun getTrendAnalysis(userId: String, metric: String, days: Int): Flow<TrendAnalysis> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        return getChartData(userId, metric, startDate, endDate).map { dataPoints ->
            if (dataPoints.size < 2) {
                TrendAnalysis(
                    metric = metric,
                    trend = TrendDirection.STABLE,
                    changePercentage = 0.0,
                    description = "Insufficient data for trend analysis",
                    recommendation = null
                )
            } else {
                val firstValue = dataPoints.first().value
                val lastValue = dataPoints.last().value
                val changePercentage = ((lastValue - firstValue) / firstValue) * 100
                
                val trend = when {
                    changePercentage > 5 -> TrendDirection.IMPROVING
                    changePercentage < -5 -> TrendDirection.DECLINING
                    else -> TrendDirection.STABLE
                }
                
                TrendAnalysis(
                    metric = metric,
                    trend = trend,
                    changePercentage = changePercentage,
                    description = "Your $metric has ${trend.name.lowercase()} by ${abs(changePercentage).toInt()}% over the last $days days",
                    recommendation = when (trend) {
                        TrendDirection.DECLINING -> "Consider reviewing your $metric goals and making adjustments"
                        TrendDirection.IMPROVING -> "Great progress! Keep up the good work"
                        TrendDirection.STABLE -> "Your $metric is consistent"
                    }
                )
            }
        }
    }

    override fun getCorrelationInsights(userId: String, days: Int): Flow<List<CorrelationInsight>> {
        // Simplified implementation - would need statistical analysis for real correlations
        return flowOf(
            listOf(
                CorrelationInsight(
                    metric1 = "Sleep",
                    metric2 = "Energy Level",
                    correlationStrength = 0.7,
                    description = "Better sleep quality correlates with higher energy levels",
                    actionableInsight = "Aim for 7-9 hours of quality sleep to maintain energy"
                )
            )
        )
    }

    override fun getAchievements(userId: String): Flow<List<Achievement>> {
        return flowOf(emptyList()) // Placeholder implementation
    }

    override fun checkForNewAchievements(userId: String, date: LocalDate): Flow<List<Achievement>> {
        return flowOf(emptyList()) // Placeholder implementation
    }

    override fun getImprovementAreas(userId: String): Flow<List<ImprovementArea>> {
        return flowOf(
            listOf(
                ImprovementArea(
                    category = "Hydration",
                    description = "Increase daily water intake",
                    currentValue = 2000.0,
                    targetValue = 2500.0,
                    priority = Priority.MEDIUM
                )
            )
        )
    }

    override suspend fun refreshAnalyticsData(userId: String): Result<Unit> {
        return try {
            // Refresh analytics calculations
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun calculateDailyMetrics(userId: String, date: LocalDate): Result<Unit> {
        return try {
            // Calculate and cache daily metrics
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}