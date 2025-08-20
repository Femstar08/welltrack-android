package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailyTrackingRepository {
    
    // Core tracking operations
    suspend fun saveMorningTracking(userId: String, date: LocalDate, data: MorningTrackingData): Result<String>
    suspend fun savePreWorkoutTracking(userId: String, date: LocalDate, data: PreWorkoutTrackingData): Result<String>
    suspend fun savePostWorkoutTracking(userId: String, date: LocalDate, data: PostWorkoutTrackingData): Result<String>
    suspend fun saveBedtimeTracking(userId: String, date: LocalDate, data: BedtimeTrackingData): Result<String>
    suspend fun saveWaterIntake(userId: String, date: LocalDate, data: WaterIntakeData): Result<String>
    
    // Retrieval operations
    suspend fun getMorningTracking(userId: String, date: LocalDate): MorningTrackingData?
    suspend fun getPreWorkoutTracking(userId: String, date: LocalDate): PreWorkoutTrackingData?
    suspend fun getPostWorkoutTracking(userId: String, date: LocalDate): PostWorkoutTrackingData?
    suspend fun getBedtimeTracking(userId: String, date: LocalDate): BedtimeTrackingData?
    suspend fun getWaterIntake(userId: String, date: LocalDate): WaterIntakeData?
    
    // Summary and analytics
    fun getDailyTrackingSummary(userId: String, date: LocalDate): Flow<DailyTrackingSummary>
    fun getDailyTrackingHistory(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTrackingSummary>>
    
    // Water intake specific
    suspend fun addWaterEntry(userId: String, date: LocalDate, amountMl: Int, source: String): Result<String>
    suspend fun getTotalWaterIntakeForDate(userId: String, date: LocalDate): Int
    fun getWaterIntakeHistory(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<WaterIntakeData>>
    
    // Completion tracking
    suspend fun markTrackingCompleted(userId: String, date: LocalDate, type: DailyTrackingType): Result<Unit>
    suspend fun isTrackingCompleted(userId: String, date: LocalDate, type: DailyTrackingType): Boolean
    
    // Bulk operations
    suspend fun getDailyTrackingForDate(userId: String, date: LocalDate): Flow<List<DailyTrackingEntry>>
    suspend fun deleteDailyTrackingForDate(userId: String, date: LocalDate): Result<Unit>
}