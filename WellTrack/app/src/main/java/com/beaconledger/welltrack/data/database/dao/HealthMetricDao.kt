package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthMetricDao {
    
    @Query("SELECT * FROM health_metrics WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllHealthMetrics(userId: String): Flow<List<HealthMetric>>
    
    @Query("""
        SELECT * FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        AND DATE(timestamp) BETWEEN :startDate AND :endDate 
        ORDER BY timestamp DESC
    """)
    fun getHealthMetricsByTypeAndDateRange(
        userId: String,
        type: HealthMetricType,
        startDate: String,
        endDate: String
    ): Flow<List<HealthMetric>>
    
    @Query("""
        SELECT * FROM health_metrics 
        WHERE userId = :userId 
        AND DATE(timestamp) BETWEEN :startDate AND :endDate 
        ORDER BY timestamp DESC
    """)
    fun getHealthMetricsByDateRange(
        userId: String,
        startDate: String,
        endDate: String
    ): Flow<List<HealthMetric>>
    
    @Query("""
        SELECT * FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        ORDER BY timestamp DESC 
        LIMIT 1
    """)
    suspend fun getLatestHealthMetricByType(
        userId: String,
        type: HealthMetricType
    ): HealthMetric?
    
    @Query("SELECT * FROM health_metrics WHERE id = :id")
    suspend fun getHealthMetricById(id: String): HealthMetric?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetric(healthMetric: HealthMetric)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetrics(healthMetrics: List<HealthMetric>)
    
    @Update
    suspend fun updateHealthMetric(healthMetric: HealthMetric)
    
    @Query("DELETE FROM health_metrics WHERE id = :id")
    suspend fun deleteHealthMetric(id: String)
    
    @Query("DELETE FROM health_metrics WHERE userId = :userId")
    suspend fun deleteAllHealthMetricsForUser(userId: String)
    
    @Query("""
        SELECT * FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        AND DATE(timestamp) = :date 
        ORDER BY timestamp DESC
    """)
    fun getHealthMetricsForDate(
        userId: String,
        type: HealthMetricType,
        date: String
    ): Flow<List<HealthMetric>>
    
    @Query("""
        SELECT AVG(value) FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        AND DATE(timestamp) BETWEEN :startDate AND :endDate
    """)
    suspend fun getAverageValueForDateRange(
        userId: String,
        type: HealthMetricType,
        startDate: String,
        endDate: String
    ): Double?
    
    @Query("""
        SELECT SUM(value) FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        AND DATE(timestamp) BETWEEN :startDate AND :endDate
    """)
    suspend fun getSumValueForDateRange(
        userId: String,
        type: HealthMetricType,
        startDate: String,
        endDate: String
    ): Double?
    
    @Query("""
        SELECT MAX(value) FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        AND DATE(timestamp) BETWEEN :startDate AND :endDate
    """)
    suspend fun getMaxValueForDateRange(
        userId: String,
        type: HealthMetricType,
        startDate: String,
        endDate: String
    ): Double?
    
    @Query("""
        SELECT MIN(value) FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        AND DATE(timestamp) BETWEEN :startDate AND :endDate
    """)
    suspend fun getMinValueForDateRange(
        userId: String,
        type: HealthMetricType,
        startDate: String,
        endDate: String
    ): Double?
    
    @Query("""
        SELECT COUNT(*) FROM health_metrics 
        WHERE userId = :userId 
        AND type = :type 
        AND DATE(timestamp) = :date
    """)
    suspend fun getMetricCountForDate(
        userId: String,
        type: HealthMetricType,
        date: String
    ): Int
    
    @Query("""
        SELECT DISTINCT type FROM health_metrics 
        WHERE userId = :userId 
        ORDER BY type
    """)
    suspend fun getAvailableMetricTypes(userId: String): List<HealthMetricType>
    
    @Query("""
        SELECT * FROM health_metrics 
        WHERE userId = :userId 
        AND source = :source 
        ORDER BY timestamp DESC
    """)
    fun getHealthMetricsBySource(
        userId: String,
        source: String
    ): Flow<List<HealthMetric>>
}