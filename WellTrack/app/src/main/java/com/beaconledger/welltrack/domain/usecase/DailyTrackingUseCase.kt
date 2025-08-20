package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DailyTrackingRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyTrackingUseCase @Inject constructor(
    private val dailyTrackingRepository: DailyTrackingRepository
) {

    suspend fun saveMorningTracking(userId: String, date: LocalDate, data: MorningTrackingData): Result<String> {
        return dailyTrackingRepository.saveMorningTracking(userId, date, data)
    }

    suspend fun savePreWorkoutTracking(userId: String, date: LocalDate, data: PreWorkoutTrackingData): Result<String> {
        return dailyTrackingRepository.savePreWorkoutTracking(userId, date, data)
    }

    suspend fun savePostWorkoutTracking(userId: String, date: LocalDate, data: PostWorkoutTrackingData): Result<String> {
        return dailyTrackingRepository.savePostWorkoutTracking(userId, date, data)
    }

    suspend fun saveBedtimeTracking(userId: String, date: LocalDate, data: BedtimeTrackingData): Result<String> {
        return dailyTrackingRepository.saveBedtimeTracking(userId, date, data)
    }

    suspend fun addWaterIntake(userId: String, date: LocalDate, amountMl: Int, source: String = "Water"): Result<String> {
        return dailyTrackingRepository.addWaterEntry(userId, date, amountMl, source)
    }

    suspend fun getMorningTracking(userId: String, date: LocalDate): MorningTrackingData? {
        return dailyTrackingRepository.getMorningTracking(userId, date)
    }

    suspend fun getPreWorkoutTracking(userId: String, date: LocalDate): PreWorkoutTrackingData? {
        return dailyTrackingRepository.getPreWorkoutTracking(userId, date)
    }

    suspend fun getPostWorkoutTracking(userId: String, date: LocalDate): PostWorkoutTrackingData? {
        return dailyTrackingRepository.getPostWorkoutTracking(userId, date)
    }

    suspend fun getBedtimeTracking(userId: String, date: LocalDate): BedtimeTrackingData? {
        return dailyTrackingRepository.getBedtimeTracking(userId, date)
    }

    suspend fun getWaterIntake(userId: String, date: LocalDate): WaterIntakeData? {
        return dailyTrackingRepository.getWaterIntake(userId, date)
    }

    fun getDailyTrackingSummary(userId: String, date: LocalDate): Flow<DailyTrackingSummary> {
        return dailyTrackingRepository.getDailyTrackingSummary(userId, date)
    }

    fun getDailyTrackingHistory(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTrackingSummary>> {
        return dailyTrackingRepository.getDailyTrackingHistory(userId, startDate, endDate)
    }

    suspend fun getTotalWaterIntakeForDate(userId: String, date: LocalDate): Int {
        return dailyTrackingRepository.getTotalWaterIntakeForDate(userId, date)
    }

    suspend fun isTrackingCompleted(userId: String, date: LocalDate, type: DailyTrackingType): Boolean {
        return dailyTrackingRepository.isTrackingCompleted(userId, date, type)
    }

    suspend fun markTrackingCompleted(userId: String, date: LocalDate, type: DailyTrackingType): Result<Unit> {
        return dailyTrackingRepository.markTrackingCompleted(userId, date, type)
    }

    fun getWaterIntakeProgress(userId: String, date: LocalDate): Flow<Float> {
        return dailyTrackingRepository.getDailyTrackingSummary(userId, date)
            .kotlinx.coroutines.flow.map { it.waterIntakeProgress }
    }

    suspend fun getRecommendedWaterIntake(userWeight: Double?): Int {
        // Basic calculation: 35ml per kg of body weight, minimum 2L, maximum 4L
        return when {
            userWeight == null -> 2500 // Default 2.5L
            userWeight < 50 -> 2000 // Minimum 2L
            userWeight > 100 -> 4000 // Maximum 4L
            else -> (userWeight * 35).toInt()
        }
    }

    suspend fun calculateMacronutrientProgress(userId: String, date: LocalDate): MacronutrientProgress? {
        val bedtimeData = getBedtimeTracking(userId, date)
        return bedtimeData?.let { data ->
            val targets = data.macronutrientsTarget
            val actual = data.macronutrientsActual
            
            if (targets != null && actual != null) {
                MacronutrientProgress(
                    caloriesProgress = actual.caloriesActual.toFloat() / targets.caloriesTarget.toFloat(),
                    proteinProgress = actual.proteinGrams.toFloat() / targets.proteinGrams.toFloat(),
                    carbsProgress = actual.carbsGrams.toFloat() / targets.carbsGrams.toFloat(),
                    fatProgress = actual.fatGrams.toFloat() / targets.fatGrams.toFloat(),
                    fiberProgress = actual.fiberGrams.toFloat() / targets.fiberGrams.toFloat()
                )
            } else null
        }
    }
}

data class MacronutrientProgress(
    val caloriesProgress: Float,
    val proteinProgress: Float,
    val carbsProgress: Float,
    val fatProgress: Float,
    val fiberProgress: Float
)