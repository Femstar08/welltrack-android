package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.CustomHabit
import com.beaconledger.welltrack.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
@Dao
interface HealthMetricDao {
    @Query("SELECT * FROM health_metrics WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHealthMetricsByUser(userId: String): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE userId = :userId AND type = :type ORDER BY timestamp DESC")
    fun getHealthMetricsByType(userId: String, type: HealthMetricType): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE userId = :userId AND timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getHealthMetricsInDateRange(userId: String, startDate: String, endDate: String): Flow<List<HealthMetric>>

    @Query("SELECT * FROM health_metrics WHERE userId = :userId AND source = :source ORDER BY timestamp DESC")
    fun getHealthMetricsBySource(userId: String, source: DataSource): Flow<List<HealthMetric>>

    @Query("SELECT COUNT(*) FROM health_metrics WHERE userId = :userId")
    suspend fun getHealthMetricCountByUser(userId: String): Int

    @Query("DELETE FROM health_metrics WHERE userId = :userId")
    suspend fun deleteAllHealthMetricsByUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetric(healthMetric: HealthMetric)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHealthMetrics(healthMetrics: List<HealthMetric>)

    @Update
    suspend fun updateHealthMetric(healthMetric: HealthMetric)

    @Delete
    suspend fun deleteHealthMetric(healthMetric: HealthMetric)

    // Custom Habits
    @Query("SELECT * FROM custom_habits WHERE userId = :userId AND isActive = 1")
    fun getActiveCustomHabits(userId: String): Flow<List<CustomHabit>>

    @Query("SELECT * FROM custom_habits WHERE userId = :userId")
    fun getAllCustomHabits(userId: String): Flow<List<CustomHabit>>

    @Query("SELECT COUNT(*) FROM custom_habits WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveHabitCountByUser(userId: String): Int

    @Query("DELETE FROM custom_habits WHERE userId = :userId")
    suspend fun deleteAllCustomHabitsByUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomHabit(habit: CustomHabit)

    @Update
    suspend fun updateCustomHabit(habit: CustomHabit)

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND userId = :userId AND DATE(completedAt) = DATE(:date)")
    suspend fun getHabitCompletionsForDate(habitId: String, userId: String, date: String): List<HabitCompletion>

    @Query("SELECT * FROM habit_completions WHERE userId = :userId AND DATE(completedAt) = DATE(:date)")
    suspend fun getAllHabitCompletionsForDate(userId: String, date: String): List<HabitCompletion>

    @Query("DELETE FROM habit_completions WHERE userId = :userId")
    suspend fun deleteAllHabitCompletionsByUser(userId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabitCompletion(completion: HabitCompletion)
}