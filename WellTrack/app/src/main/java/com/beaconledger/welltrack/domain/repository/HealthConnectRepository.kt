package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface HealthConnectRepository {
    
    /**
     * Check if Health Connect is available on this device
     */
    suspend fun isHealthConnectAvailable(): Boolean
    
    /**
     * Check if all required permissions are granted
     */
    suspend fun hasAllPermissions(): Boolean
    
    /**
     * Get the set of required permissions for Health Connect
     */
    fun getRequiredPermissions(): Set<String>
    
    /**
     * Sync health data from Health Connect for the specified number of days
     */
    suspend fun syncHealthData(userId: String, days: Int = 7): Result<Unit>
    
    /**
     * Get health metrics by type and date range
     */
    suspend fun getHealthMetrics(
        userId: String,
        type: HealthMetricType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HealthMetric>>
    
    /**
     * Get the latest health metric of a specific type
     */
    suspend fun getLatestHealthMetric(
        userId: String,
        type: HealthMetricType
    ): HealthMetric?
    
    /**
     * Get all health metrics for a date range
     */
    suspend fun getAllHealthMetrics(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HealthMetric>>
    
    /**
     * Get a summary of health metrics for a specific date
     */
    suspend fun getHealthMetricsSummary(
        userId: String,
        date: LocalDate
    ): Flow<Map<HealthMetricType, HealthMetric>>
    
    /**
     * Delete a health metric
     */
    suspend fun deleteHealthMetric(metricId: String): Result<Unit>
    
    /**
     * Insert a health metric manually
     */
    suspend fun insertHealthMetric(healthMetric: HealthMetric): Result<String>
    
    // Convenience methods for common metrics
    
    /**
     * Get total steps for a specific date
     */
    suspend fun getStepsForDate(userId: String, date: LocalDate): Int
    
    /**
     * Get average heart rate for a date range
     */
    suspend fun getAverageHeartRate(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double?
    
    /**
     * Get the latest weight measurement
     */
    suspend fun getLatestWeight(userId: String): Double?
    
    /**
     * Get total calories burned for a specific date
     */
    suspend fun getTotalCaloriesBurned(userId: String, date: LocalDate): Double
    
    /**
     * Get sleep duration for a specific date
     */
    suspend fun getSleepDuration(userId: String, date: LocalDate): Double?
    
    /**
     * Get total hydration for a specific date
     */
    suspend fun getHydrationTotal(userId: String, date: LocalDate): Double
}