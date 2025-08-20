package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.DailyTrackingDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DailyTrackingRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyTrackingRepositoryImpl @Inject constructor(
    private val dailyTrackingDao: DailyTrackingDao,
    private val gson: Gson
) : DailyTrackingRepository {

    override suspend fun saveMorningTracking(userId: String, date: LocalDate, data: MorningTrackingData): Result<String> {
        return try {
            val entry = DailyTrackingEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                trackingType = DailyTrackingType.MORNING_ROUTINE,
                timestamp = LocalDateTime.now(),
                data = gson.toJson(data),
                isCompleted = true
            )
            dailyTrackingDao.insertDailyTracking(entry)
            Result.success(entry.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePreWorkoutTracking(userId: String, date: LocalDate, data: PreWorkoutTrackingData): Result<String> {
        return try {
            val entry = DailyTrackingEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                trackingType = DailyTrackingType.PRE_WORKOUT,
                timestamp = LocalDateTime.now(),
                data = gson.toJson(data),
                isCompleted = true
            )
            dailyTrackingDao.insertDailyTracking(entry)
            Result.success(entry.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePostWorkoutTracking(userId: String, date: LocalDate, data: PostWorkoutTrackingData): Result<String> {
        return try {
            val entry = DailyTrackingEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                trackingType = DailyTrackingType.POST_WORKOUT,
                timestamp = LocalDateTime.now(),
                data = gson.toJson(data),
                isCompleted = true
            )
            dailyTrackingDao.insertDailyTracking(entry)
            Result.success(entry.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveBedtimeTracking(userId: String, date: LocalDate, data: BedtimeTrackingData): Result<String> {
        return try {
            val entry = DailyTrackingEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                trackingType = DailyTrackingType.BEDTIME_ROUTINE,
                timestamp = LocalDateTime.now(),
                data = gson.toJson(data),
                isCompleted = true
            )
            dailyTrackingDao.insertDailyTracking(entry)
            Result.success(entry.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveWaterIntake(userId: String, date: LocalDate, data: WaterIntakeData): Result<String> {
        return try {
            val entry = DailyTrackingEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                date = date,
                trackingType = DailyTrackingType.WATER_INTAKE,
                timestamp = LocalDateTime.now(),
                data = gson.toJson(data),
                isCompleted = data.totalMl >= data.targetMl
            )
            dailyTrackingDao.insertDailyTracking(entry)
            Result.success(entry.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMorningTracking(userId: String, date: LocalDate): MorningTrackingData? {
        return try {
            val entry = dailyTrackingDao.getDailyTrackingByType(userId, date, DailyTrackingType.MORNING_ROUTINE)
            entry?.let { gson.fromJson(it.data, MorningTrackingData::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPreWorkoutTracking(userId: String, date: LocalDate): PreWorkoutTrackingData? {
        return try {
            val entry = dailyTrackingDao.getDailyTrackingByType(userId, date, DailyTrackingType.PRE_WORKOUT)
            entry?.let { gson.fromJson(it.data, PreWorkoutTrackingData::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getPostWorkoutTracking(userId: String, date: LocalDate): PostWorkoutTrackingData? {
        return try {
            val entry = dailyTrackingDao.getDailyTrackingByType(userId, date, DailyTrackingType.POST_WORKOUT)
            entry?.let { gson.fromJson(it.data, PostWorkoutTrackingData::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getBedtimeTracking(userId: String, date: LocalDate): BedtimeTrackingData? {
        return try {
            val entry = dailyTrackingDao.getDailyTrackingByType(userId, date, DailyTrackingType.BEDTIME_ROUTINE)
            entry?.let { gson.fromJson(it.data, BedtimeTrackingData::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun getWaterIntake(userId: String, date: LocalDate): WaterIntakeData? {
        return try {
            val entry = dailyTrackingDao.getDailyTrackingByType(userId, date, DailyTrackingType.WATER_INTAKE)
            entry?.let { gson.fromJson(it.data, WaterIntakeData::class.java) }
        } catch (e: Exception) {
            null
        }
    }

    override fun getDailyTrackingSummary(userId: String, date: LocalDate): Flow<DailyTrackingSummary> {
        return dailyTrackingDao.getDailyTrackingForDate(userId, date).map { entries ->
            val morningCompleted = entries.any { it.trackingType == DailyTrackingType.MORNING_ROUTINE && it.isCompleted }
            val preWorkoutCompleted = entries.any { it.trackingType == DailyTrackingType.PRE_WORKOUT && it.isCompleted }
            val postWorkoutCompleted = entries.any { it.trackingType == DailyTrackingType.POST_WORKOUT && it.isCompleted }
            val bedtimeCompleted = entries.any { it.trackingType == DailyTrackingType.BEDTIME_ROUTINE && it.isCompleted }
            
            val waterEntry = entries.find { it.trackingType == DailyTrackingType.WATER_INTAKE }
            val waterData = waterEntry?.let { gson.fromJson(it.data, WaterIntakeData::class.java) }
            val waterProgress = waterData?.let { it.totalMl.toFloat() / it.targetMl.toFloat() } ?: 0f
            
            val energyLevels = mutableListOf<Int>()
            entries.forEach { entry ->
                when (entry.trackingType) {
                    DailyTrackingType.MORNING_ROUTINE -> {
                        val data = gson.fromJson(entry.data, MorningTrackingData::class.java)
                        energyLevels.add(data.energyLevel)
                    }
                    DailyTrackingType.PRE_WORKOUT -> {
                        val data = gson.fromJson(entry.data, PreWorkoutTrackingData::class.java)
                        energyLevels.add(data.energyLevel)
                    }
                    else -> {}
                }
            }
            
            val completedCount = listOf(morningCompleted, preWorkoutCompleted, postWorkoutCompleted, bedtimeCompleted).count { it }
            val completionPercentage = completedCount / 4f
            
            DailyTrackingSummary(
                userId = userId,
                date = date,
                morningCompleted = morningCompleted,
                preWorkoutCompleted = preWorkoutCompleted,
                postWorkoutCompleted = postWorkoutCompleted,
                bedtimeCompleted = bedtimeCompleted,
                waterIntakeProgress = waterProgress.coerceAtMost(1f),
                totalWaterMl = waterData?.totalMl ?: 0,
                energyLevelAverage = if (energyLevels.isNotEmpty()) energyLevels.average().toFloat() else 0f,
                completionPercentage = completionPercentage
            )
        }
    }

    override fun getDailyTrackingHistory(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTrackingSummary>> {
        return dailyTrackingDao.getDailyTrackingForDateRange(userId, startDate, endDate).map { entries ->
            entries.groupBy { it.date }.map { (date, dayEntries) ->
                val morningCompleted = dayEntries.any { it.trackingType == DailyTrackingType.MORNING_ROUTINE && it.isCompleted }
                val preWorkoutCompleted = dayEntries.any { it.trackingType == DailyTrackingType.PRE_WORKOUT && it.isCompleted }
                val postWorkoutCompleted = dayEntries.any { it.trackingType == DailyTrackingType.POST_WORKOUT && it.isCompleted }
                val bedtimeCompleted = dayEntries.any { it.trackingType == DailyTrackingType.BEDTIME_ROUTINE && it.isCompleted }
                
                val waterEntry = dayEntries.find { it.trackingType == DailyTrackingType.WATER_INTAKE }
                val waterData = waterEntry?.let { gson.fromJson(it.data, WaterIntakeData::class.java) }
                val waterProgress = waterData?.let { it.totalMl.toFloat() / it.targetMl.toFloat() } ?: 0f
                
                val completedCount = listOf(morningCompleted, preWorkoutCompleted, postWorkoutCompleted, bedtimeCompleted).count { it }
                val completionPercentage = completedCount / 4f
                
                DailyTrackingSummary(
                    userId = userId,
                    date = date,
                    morningCompleted = morningCompleted,
                    preWorkoutCompleted = preWorkoutCompleted,
                    postWorkoutCompleted = postWorkoutCompleted,
                    bedtimeCompleted = bedtimeCompleted,
                    waterIntakeProgress = waterProgress.coerceAtMost(1f),
                    totalWaterMl = waterData?.totalMl ?: 0,
                    energyLevelAverage = 0f, // Calculate if needed
                    completionPercentage = completionPercentage
                )
            }.sortedByDescending { it.date }
        }
    }

    override suspend fun addWaterEntry(userId: String, date: LocalDate, amountMl: Int, source: String): Result<String> {
        return try {
            val existingWaterData = getWaterIntake(userId, date) ?: WaterIntakeData()
            val newEntry = WaterEntry(
                timestamp = LocalDateTime.now(),
                amountMl = amountMl,
                source = source
            )
            val updatedData = existingWaterData.copy(
                totalMl = existingWaterData.totalMl + amountMl,
                entries = existingWaterData.entries + newEntry
            )
            saveWaterIntake(userId, date, updatedData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTotalWaterIntakeForDate(userId: String, date: LocalDate): Int {
        return dailyTrackingDao.getTotalWaterIntakeForDate(userId, date) ?: 0
    }

    override fun getWaterIntakeHistory(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<WaterIntakeData>> {
        return dailyTrackingDao.getDailyTrackingByTypeAndDateRange(userId, DailyTrackingType.WATER_INTAKE, startDate, endDate)
            .map { entries ->
                entries.mapNotNull { entry ->
                    try {
                        gson.fromJson(entry.data, WaterIntakeData::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
    }

    override suspend fun markTrackingCompleted(userId: String, date: LocalDate, type: DailyTrackingType): Result<Unit> {
        return try {
            val entry = dailyTrackingDao.getDailyTrackingByType(userId, date, type)
            if (entry != null) {
                val updatedEntry = entry.copy(isCompleted = true)
                dailyTrackingDao.updateDailyTracking(updatedEntry)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isTrackingCompleted(userId: String, date: LocalDate, type: DailyTrackingType): Boolean {
        return try {
            val entry = dailyTrackingDao.getDailyTrackingByType(userId, date, type)
            entry?.isCompleted ?: false
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getDailyTrackingForDate(userId: String, date: LocalDate): Flow<List<DailyTrackingEntry>> {
        return dailyTrackingDao.getDailyTrackingForDate(userId, date)
    }

    override suspend fun deleteDailyTrackingForDate(userId: String, date: LocalDate): Result<Unit> {
        return try {
            dailyTrackingDao.deleteDailyTrackingForDate(userId, date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}