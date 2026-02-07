package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.DailyTrackingEntry
import com.beaconledger.welltrack.data.model.DailyTrackingType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyTrackingDao {
    
    @Query("SELECT * FROM daily_tracking_entries WHERE userId = :userId AND date = :date ORDER BY timestamp DESC")
    fun getDailyTrackingForDate(userId: String, date: LocalDate): Flow<List<DailyTrackingEntry>>
    
    @Query("SELECT * FROM daily_tracking_entries WHERE userId = :userId AND date = :date AND trackingType = :type")
    suspend fun getDailyTrackingByType(userId: String, date: LocalDate, type: DailyTrackingType): DailyTrackingEntry?
    
    @Query("SELECT * FROM daily_tracking_entries WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC, timestamp DESC")
    fun getDailyTrackingForDateRange(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTrackingEntry>>
    
    @Query("SELECT * FROM daily_tracking_entries WHERE userId = :userId AND trackingType = :type AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getDailyTrackingByTypeAndDateRange(
        userId: String, 
        type: DailyTrackingType, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Flow<List<DailyTrackingEntry>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTracking(entry: DailyTrackingEntry)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyTrackingEntries(entries: List<DailyTrackingEntry>)
    
    @Update
    suspend fun updateDailyTracking(entry: DailyTrackingEntry)
    
    @Delete
    suspend fun deleteDailyTracking(entry: DailyTrackingEntry)
    
    @Query("DELETE FROM daily_tracking_entries WHERE userId = :userId AND date = :date")
    suspend fun deleteDailyTrackingForDate(userId: String, date: LocalDate)
    
    @Query("DELETE FROM daily_tracking_entries WHERE userId = :userId AND date = :date AND trackingType = :type")
    suspend fun deleteDailyTrackingByType(userId: String, date: LocalDate, type: DailyTrackingType)
    
    // Water intake specific queries
    @Query("SELECT * FROM daily_tracking_entries WHERE userId = :userId AND trackingType = 'WATER_INTAKE' AND date = :date ORDER BY timestamp DESC")
    fun getWaterIntakeForDate(userId: String, date: LocalDate): Flow<List<DailyTrackingEntry>>
    
    @Query("SELECT SUM(CAST(JSON_EXTRACT(data, '$.totalMl') AS INTEGER)) FROM daily_tracking_entries WHERE userId = :userId AND trackingType = 'WATER_INTAKE' AND date = :date")
    suspend fun getTotalWaterIntakeForDate(userId: String, date: LocalDate): Int?
    
    // Summary queries
    @Query("""
        SELECT 
            COUNT(CASE WHEN trackingType = 'MORNING_ROUTINE' AND isCompleted = 1 THEN 1 END) as morningCompleted,
            COUNT(CASE WHEN trackingType = 'PRE_WORKOUT' AND isCompleted = 1 THEN 1 END) as preWorkoutCompleted,
            COUNT(CASE WHEN trackingType = 'POST_WORKOUT' AND isCompleted = 1 THEN 1 END) as postWorkoutCompleted,
            COUNT(CASE WHEN trackingType = 'BEDTIME_ROUTINE' AND isCompleted = 1 THEN 1 END) as bedtimeCompleted
        FROM daily_tracking_entries 
        WHERE userId = :userId AND date = :date
    """)
    suspend fun getDailyCompletionSummary(userId: String, date: LocalDate): DailyCompletionSummary
    
    @Query("SELECT * FROM daily_tracking_entries WHERE id = :id")
    suspend fun getDailyTrackingById(id: String): DailyTrackingEntry?

    @Query("DELETE FROM daily_tracking_entries WHERE userId = :userId")
    suspend fun deleteAllDailyTrackingForUser(userId: String)
}

data class DailyCompletionSummary(
    val morningCompleted: Int,
    val preWorkoutCompleted: Int,
    val postWorkoutCompleted: Int,
    val bedtimeCompleted: Int
)