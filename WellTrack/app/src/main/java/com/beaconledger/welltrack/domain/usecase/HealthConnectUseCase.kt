package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.domain.repository.HealthConnectRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class HealthConnectUseCase @Inject constructor(
    private val healthConnectRepository: HealthConnectRepository
) {
    
    suspend fun isHealthConnectAvailable(): Boolean {
        return healthConnectRepository.isHealthConnectAvailable()
    }
    
    suspend fun hasAllPermissions(): Boolean {
        return healthConnectRepository.hasAllPermissions()
    }
    
    fun getRequiredPermissions(): Set<String> {
        return healthConnectRepository.getRequiredPermissions()
    }
    
    suspend fun syncHealthData(userId: String, days: Int = 7): Result<Unit> {
        return healthConnectRepository.syncHealthData(userId, days)
    }
    
    suspend fun getHealthMetrics(
        userId: String,
        type: HealthMetricType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HealthMetric>> {
        return healthConnectRepository.getHealthMetrics(userId, type, startDate, endDate)
    }
    
    suspend fun getLatestHealthMetric(
        userId: String,
        type: HealthMetricType
    ): HealthMetric? {
        return healthConnectRepository.getLatestHealthMetric(userId, type)
    }
    
    suspend fun getAllHealthMetrics(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HealthMetric>> {
        return healthConnectRepository.getAllHealthMetrics(userId, startDate, endDate)
    }
    
    suspend fun getHealthMetricsSummary(
        userId: String,
        date: LocalDate
    ): Flow<Map<HealthMetricType, HealthMetric>> {
        return healthConnectRepository.getHealthMetricsSummary(userId, date)
    }
    
    suspend fun deleteHealthMetric(metricId: String): Result<Unit> {
        return healthConnectRepository.deleteHealthMetric(metricId)
    }
    
    suspend fun insertHealthMetric(healthMetric: HealthMetric): Result<String> {
        return healthConnectRepository.insertHealthMetric(healthMetric)
    }
    
    // Convenience methods for common metrics
    
    suspend fun getStepsForDate(userId: String, date: LocalDate): Int {
        return healthConnectRepository.getStepsForDate(userId, date)
    }
    
    suspend fun getAverageHeartRate(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double? {
        return healthConnectRepository.getAverageHeartRate(userId, startDate, endDate)
    }
    
    suspend fun getLatestWeight(userId: String): Double? {
        return healthConnectRepository.getLatestWeight(userId)
    }
    
    suspend fun getTotalCaloriesBurned(userId: String, date: LocalDate): Double {
        return healthConnectRepository.getTotalCaloriesBurned(userId, date)
    }
    
    suspend fun getSleepDuration(userId: String, date: LocalDate): Double? {
        return healthConnectRepository.getSleepDuration(userId, date)
    }
    
    suspend fun getHydrationTotal(userId: String, date: LocalDate): Double {
        return healthConnectRepository.getHydrationTotal(userId, date)
    }
    
    // Analysis methods
    
    suspend fun getWeeklyStepsAverage(userId: String): Int {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(7)
        
        var totalSteps = 0
        var daysWithData = 0
        
        for (i in 0..6) {
            val date = startDate.plusDays(i.toLong())
            val steps = getStepsForDate(userId, date)
            if (steps > 0) {
                totalSteps += steps
                daysWithData++
            }
        }
        
        return if (daysWithData > 0) totalSteps / daysWithData else 0
    }
    
    suspend fun getWeeklyCaloriesAverage(userId: String): Double {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(7)
        
        var totalCalories = 0.0
        var daysWithData = 0
        
        for (i in 0..6) {
            val date = startDate.plusDays(i.toLong())
            val calories = getTotalCaloriesBurned(userId, date)
            if (calories > 0) {
                totalCalories += calories
                daysWithData++
            }
        }
        
        return if (daysWithData > 0) totalCalories / daysWithData else 0.0
    }
    
    suspend fun getWeeklySleepAverage(userId: String): Double {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(7)
        
        var totalSleep = 0.0
        var daysWithData = 0
        
        for (i in 0..6) {
            val date = startDate.plusDays(i.toLong())
            val sleep = getSleepDuration(userId, date)
            if (sleep != null && sleep > 0) {
                totalSleep += sleep
                daysWithData++
            }
        }
        
        return if (daysWithData > 0) totalSleep / daysWithData else 0.0
    }
    
    suspend fun getWeeklyHydrationAverage(userId: String): Double {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(7)
        
        var totalHydration = 0.0
        var daysWithData = 0
        
        for (i in 0..6) {
            val date = startDate.plusDays(i.toLong())
            val hydration = getHydrationTotal(userId, date)
            if (hydration > 0) {
                totalHydration += hydration
                daysWithData++
            }
        }
        
        return if (daysWithData > 0) totalHydration / daysWithData else 0.0
    }
    
    suspend fun getHealthSummaryForToday(userId: String): HealthSummary {
        val today = LocalDate.now()
        
        return HealthSummary(
            date = today,
            steps = getStepsForDate(userId, today),
            caloriesBurned = getTotalCaloriesBurned(userId, today),
            sleepHours = getSleepDuration(userId, today),
            hydrationLiters = getHydrationTotal(userId, today),
            averageHeartRate = getAverageHeartRate(userId, today, today),
            weight = getLatestWeight(userId)
        )
    }
    
    suspend fun getHealthTrends(userId: String, days: Int = 30): HealthTrends {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        val stepsData = mutableListOf<Pair<LocalDate, Int>>()
        val caloriesData = mutableListOf<Pair<LocalDate, Double>>()
        val sleepData = mutableListOf<Pair<LocalDate, Double>>()
        val hydrationData = mutableListOf<Pair<LocalDate, Double>>()
        
        for (i in 0 until days) {
            val date = startDate.plusDays(i.toLong())
            
            val steps = getStepsForDate(userId, date)
            if (steps > 0) stepsData.add(date to steps)
            
            val calories = getTotalCaloriesBurned(userId, date)
            if (calories > 0) caloriesData.add(date to calories)
            
            val sleep = getSleepDuration(userId, date)
            if (sleep != null && sleep > 0) sleepData.add(date to sleep)
            
            val hydration = getHydrationTotal(userId, date)
            if (hydration > 0) hydrationData.add(date to hydration)
        }
        
        return HealthTrends(
            stepsData = stepsData,
            caloriesData = caloriesData,
            sleepData = sleepData,
            hydrationData = hydrationData
        )
    }
}

data class HealthSummary(
    val date: LocalDate,
    val steps: Int,
    val caloriesBurned: Double,
    val sleepHours: Double?,
    val hydrationLiters: Double,
    val averageHeartRate: Double?,
    val weight: Double?
)

data class HealthTrends(
    val stepsData: List<Pair<LocalDate, Int>>,
    val caloriesData: List<Pair<LocalDate, Double>>,
    val sleepData: List<Pair<LocalDate, Double>>,
    val hydrationData: List<Pair<LocalDate, Double>>
)