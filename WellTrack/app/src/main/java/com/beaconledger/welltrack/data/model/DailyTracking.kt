package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "daily_tracking_entries")
data class DailyTrackingEntry(
    @PrimaryKey val id: String,
    val userId: String,
    val date: LocalDate,
    val trackingType: DailyTrackingType,
    val timestamp: LocalDateTime,
    val data: String, // JSON string containing tracking data
    val isCompleted: Boolean = false,
    val notes: String? = null
)

enum class DailyTrackingType {
    MORNING_ROUTINE,
    PRE_WORKOUT,
    POST_WORKOUT,
    BEDTIME_ROUTINE,
    WATER_INTAKE
}

data class MorningTrackingData(
    val waterIntakeMl: Int = 0,
    val supplementsTaken: List<String> = emptyList(),
    val meal1Logged: Boolean = false,
    val energyLevel: Int = 5, // 1-10 scale
    val sleepQuality: Int = 5, // 1-10 scale
    val mood: String = "",
    val weight: Double? = null
)

data class PreWorkoutTrackingData(
    val supplementsTaken: List<String> = emptyList(),
    val snackConsumed: String? = null,
    val energyLevel: Int = 5, // 1-10 scale
    val hydrationMl: Int = 0,
    val workoutType: String = "",
    val plannedDuration: Int = 0 // minutes
)

data class PostWorkoutTrackingData(
    val recoveryMealLogged: Boolean = false,
    val mood: Int = 5, // 1-10 scale
    val performanceRating: Int = 5, // 1-10 scale
    val fatigue: Int = 5, // 1-10 scale
    val hydrationMl: Int = 0,
    val supplementsTaken: List<String> = emptyList(),
    val workoutNotes: String = ""
)

data class BedtimeTrackingData(
    val dinnerLogged: Boolean = false,
    val macronutrientsTarget: MacronutrientTargets? = null,
    val macronutrientsActual: MacronutrientActual? = null,
    val supplementsTaken: List<String> = emptyList(),
    val relaxationActivity: String? = null,
    val screenTimeHours: Double = 0.0,
    val bedtimeReadiness: Int = 5, // 1-10 scale
    val stressLevel: Int = 5 // 1-10 scale
)

data class WaterIntakeData(
    val totalMl: Int = 0,
    val targetMl: Int = 2500, // Default 2.5L
    val entries: List<WaterEntry> = emptyList()
)

data class WaterEntry(
    val timestamp: LocalDateTime,
    val amountMl: Int,
    val source: String = "Water" // Water, Tea, Coffee, etc.
)

data class MacronutrientTargets(
    val caloriesTarget: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double
)

data class MacronutrientActual(
    val caloriesActual: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double
)

data class DailyTrackingSummary(
    val userId: String,
    val date: LocalDate,
    val morningCompleted: Boolean = false,
    val preWorkoutCompleted: Boolean = false,
    val postWorkoutCompleted: Boolean = false,
    val bedtimeCompleted: Boolean = false,
    val waterIntakeProgress: Float = 0f, // 0.0 to 1.0
    val totalWaterMl: Int = 0,
    val energyLevelAverage: Float = 0f,
    val completionPercentage: Float = 0f
)