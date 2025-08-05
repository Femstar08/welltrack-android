package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.CustomHabit
import com.beaconledger.welltrack.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow
data class DateRange(
    val startDate: String,
    val endDate: String
)

interface HealthDataRepository {
    suspend fun syncHealthConnectData(userId: String): Result<Unit>
    suspend fun syncGarminData(userId: String): Result<Unit>
    suspend fun syncSamsungHealthData(userId: String): Result<Unit>
    
    fun getHealthMetrics(userId: String, type: HealthMetricType, dateRange: DateRange): Flow<List<HealthMetric>>
    fun getAllHealthMetrics(userId: String): Flow<List<HealthMetric>>
    suspend fun saveHealthMetric(healthMetric: HealthMetric): Result<Unit>
    suspend fun saveHealthMetrics(healthMetrics: List<HealthMetric>): Result<Unit>
    
    // Custom Habits
    suspend fun saveCustomHabit(habit: CustomHabit): Result<String>
    suspend fun updateCustomHabit(habit: CustomHabit): Result<Unit>
    fun getActiveCustomHabits(userId: String): Flow<List<CustomHabit>>
    suspend fun completeHabit(completion: HabitCompletion): Result<Unit>
    suspend fun getHabitCompletionsForDate(habitId: String, date: String): Result<List<HabitCompletion>>
}