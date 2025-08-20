package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.health.HealthConnectManager
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.domain.repository.HealthConnectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthConnectRepositoryImpl @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val healthMetricDao: HealthMetricDao
) : HealthConnectRepository {
    
    override suspend fun isHealthConnectAvailable(): Boolean {
        return healthConnectManager.isAvailable()
    }
    
    override suspend fun hasAllPermissions(): Boolean {
        return healthConnectManager.hasAllPermissions()
    }
    
    override fun getRequiredPermissions(): Set<String> {
        return healthConnectManager.permissions.map { it.toString() }.toSet()
    }
    
    override suspend fun syncHealthData(userId: String, days: Int): Result<Unit> {
        return try {
            val endTime = Instant.now()
            val startTime = endTime.minusSeconds(days * 24 * 60 * 60L)
            
            val healthMetricsFlow = healthConnectManager.syncHealthData(userId, startTime, endTime)
            val healthMetrics = healthMetricsFlow.first()
            
            // Save to local database
            healthMetrics.forEach { metric ->
                healthMetricDao.insertHealthMetric(metric)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getHealthMetrics(
        userId: String,
        type: HealthMetricType,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HealthMetric>> {
        return healthMetricDao.getHealthMetricsByTypeAndDateRange(
            userId = userId,
            type = type,
            startDate = startDate.toString(),
            endDate = endDate.toString()
        )
    }
    
    override suspend fun getLatestHealthMetric(
        userId: String,
        type: HealthMetricType
    ): HealthMetric? {
        return healthMetricDao.getLatestHealthMetricByType(userId, type)
    }
    
    override suspend fun getAllHealthMetrics(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<HealthMetric>> {
        return healthMetricDao.getHealthMetricsByDateRange(
            userId = userId,
            startDate = startDate.toString(),
            endDate = endDate.toString()
        )
    }
    
    override suspend fun getHealthMetricsSummary(
        userId: String,
        date: LocalDate
    ): Flow<Map<HealthMetricType, HealthMetric>> = flow {
        val metrics = healthMetricDao.getHealthMetricsByDateRange(
            userId = userId,
            startDate = date.toString(),
            endDate = date.toString()
        ).first()
        
        // Get the latest metric for each type on the given date
        val summaryMap = metrics.groupBy { it.type }
            .mapValues { (_, metricsList) ->
                metricsList.maxByOrNull { it.timestamp }
            }
            .filterValues { it != null }
            .mapValues { it.value!! }
        
        emit(summaryMap)
    }
    
    override suspend fun deleteHealthMetric(metricId: String): Result<Unit> {
        return try {
            healthMetricDao.deleteHealthMetric(metricId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun insertHealthMetric(healthMetric: HealthMetric): Result<String> {
        return try {
            healthMetricDao.insertHealthMetric(healthMetric)
            Result.success(healthMetric.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getStepsForDate(userId: String, date: LocalDate): Int {
        val metrics = healthMetricDao.getHealthMetricsByTypeAndDateRange(
            userId = userId,
            type = HealthMetricType.STEPS,
            startDate = date.toString(),
            endDate = date.toString()
        ).first()
        
        return metrics.sumOf { it.value.toInt() }
    }
    
    override suspend fun getAverageHeartRate(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Double? {
        val metrics = healthMetricDao.getHealthMetricsByTypeAndDateRange(
            userId = userId,
            type = HealthMetricType.HEART_RATE,
            startDate = startDate.toString(),
            endDate = endDate.toString()
        ).first()
        
        return if (metrics.isNotEmpty()) {
            metrics.map { it.value }.average()
        } else null
    }
    
    override suspend fun getLatestWeight(userId: String): Double? {
        return healthMetricDao.getLatestHealthMetricByType(userId, HealthMetricType.WEIGHT)?.value
    }
    
    override suspend fun getTotalCaloriesBurned(
        userId: String,
        date: LocalDate
    ): Double {
        val metrics = healthMetricDao.getHealthMetricsByTypeAndDateRange(
            userId = userId,
            type = HealthMetricType.CALORIES_BURNED,
            startDate = date.toString(),
            endDate = date.toString()
        ).first()
        
        return metrics.sumOf { it.value }
    }
    
    override suspend fun getSleepDuration(
        userId: String,
        date: LocalDate
    ): Double? {
        val metrics = healthMetricDao.getHealthMetricsByTypeAndDateRange(
            userId = userId,
            type = HealthMetricType.SLEEP_DURATION,
            startDate = date.toString(),
            endDate = date.toString()
        ).first()
        
        return metrics.firstOrNull()?.value
    }
    
    override suspend fun getHydrationTotal(
        userId: String,
        date: LocalDate
    ): Double {
        val metrics = healthMetricDao.getHealthMetricsByTypeAndDateRange(
            userId = userId,
            type = HealthMetricType.HYDRATION,
            startDate = date.toString(),
            endDate = date.toString()
        ).first()
        
        return metrics.sumOf { it.value }
    }
}