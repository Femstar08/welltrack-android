package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val type: WorkoutType,
    val category: WorkoutCategory,
    val duration: Int, // in minutes
    val caloriesBurned: Int? = null,
    val averageHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val exercises: String, // JSON string of List<WorkoutExercise>
    val notes: String? = null,
    val completedAt: String,
    val createdAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "exercise_templates")
data class ExerciseTemplate(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val category: ExerciseCategory,
    val muscleGroups: String, // JSON string of List<MuscleGroup>
    val equipment: String, // JSON string of List<Equipment>
    val instructions: String, // JSON string of List<String>
    val difficulty: ExerciseDifficulty,
    val estimatedCaloriesPerMinute: Double? = null,
    val targetHeartRateZone: HeartRateZone? = null,
    val isCardio: Boolean = false,
    val isStrength: Boolean = false,
    val metadata: String? = null // JSON for additional exercise-specific data
)

data class WorkoutExercise(
    val exerciseId: String,
    val exerciseName: String,
    val sets: List<ExerciseSet>,
    val restBetweenSets: Int? = null, // seconds
    val notes: String? = null
)

data class ExerciseSet(
    val setNumber: Int,
    val reps: Int? = null,
    val weight: Double? = null, // in kg
    val duration: Int? = null, // in seconds
    val distance: Double? = null, // in meters
    val restAfter: Int? = null, // seconds
    val completed: Boolean = false
)

// Norwegian 4x4 specific data structure
data class Norwegian4x4Session(
    val intervals: List<Norwegian4x4Interval>,
    val warmupDuration: Int = 10, // minutes
    val cooldownDuration: Int = 5, // minutes
    val targetIntensity: Int = 85, // percentage of max heart rate
    val actualAverageHeartRate: Int? = null,
    val perceivedExertion: Int? = null // RPE scale 1-10
)

data class Norwegian4x4Interval(
    val intervalNumber: Int,
    val workDuration: Int = 4, // minutes
    val restDuration: Int = 3, // minutes
    val targetHeartRate: Int? = null,
    val actualAverageHeartRate: Int? = null,
    val actualMaxHeartRate: Int? = null,
    val distance: Double? = null, // if running/cycling
    val pace: String? = null, // e.g., "5:30/km"
    val completed: Boolean = false
)

enum class WorkoutType {
    CARDIO,
    STRENGTH,
    HIIT,
    YOGA,
    PILATES,
    STRETCHING,
    SPORTS,
    NORWEGIAN_4X4,
    CUSTOM
}

enum class WorkoutCategory {
    ENDURANCE,
    STRENGTH_TRAINING,
    FLEXIBILITY,
    BALANCE,
    SPORTS_SPECIFIC,
    REHABILITATION,
    VO2_MAX_TRAINING,
    INTERVAL_TRAINING
}

enum class ExerciseCategory {
    CARDIO,
    STRENGTH,
    FLEXIBILITY,
    BALANCE,
    PLYOMETRIC,
    CORE,
    FUNCTIONAL,
    REHABILITATION,
    INTERVAL_TRAINING
}

enum class ExerciseDifficulty {
    BEGINNER,
    INTERMEDIATE,
    ADVANCED,
    EXPERT
}

enum class MuscleGroup {
    CHEST,
    BACK,
    SHOULDERS,
    BICEPS,
    TRICEPS,
    FOREARMS,
    CORE,
    QUADRICEPS,
    HAMSTRINGS,
    GLUTES,
    CALVES,
    FULL_BODY,
    CARDIOVASCULAR_SYSTEM
}

enum class Equipment {
    NONE,
    DUMBBELLS,
    BARBELL,
    KETTLEBELL,
    RESISTANCE_BANDS,
    PULL_UP_BAR,
    BENCH,
    TREADMILL,
    STATIONARY_BIKE,
    ROWING_MACHINE,
    ELLIPTICAL,
    YOGA_MAT,
    FOAM_ROLLER,
    MEDICINE_BALL,
    SUSPENSION_TRAINER
}

enum class HeartRateZone {
    ZONE_1_RECOVERY,      // 50-60% max HR
    ZONE_2_AEROBIC,       // 60-70% max HR
    ZONE_3_TEMPO,         // 70-80% max HR
    ZONE_4_THRESHOLD,     // 80-90% max HR
    ZONE_5_NEUROMUSCULAR  // 90-100% max HR
}

// VO2 Max specific data structures
data class VO2MaxGoal(
    val currentVO2Max: Double? = null,
    val targetVO2Max: Double,
    val targetDate: String,
    val trainingPlan: VO2MaxTrainingPlan,
    val progressTracking: List<VO2MaxProgress> = emptyList()
)

data class VO2MaxProgress(
    val date: String,
    val measuredVO2Max: Double,
    val testType: VO2MaxTestType,
    val notes: String? = null
)

enum class VO2MaxTestType {
    LABORATORY_TEST,
    FIELD_TEST_COOPER,
    FIELD_TEST_BEEP,
    GARMIN_ESTIMATE,
    SAMSUNG_ESTIMATE,
    MANUAL_ENTRY
}

enum class VO2MaxTrainingPlan {
    NORWEGIAN_4X4_FOCUSED,
    MIXED_INTERVAL_TRAINING,
    LONG_SLOW_DISTANCE,
    POLARIZED_TRAINING,
    CUSTOM
}

// Workout result for tracking performance
data class WorkoutResult(
    val workoutId: String,
    val completionPercentage: Double,
    val actualDuration: Int, // minutes
    val caloriesBurned: Int? = null,
    val averageHeartRate: Int? = null,
    val maxHeartRate: Int? = null,
    val perceivedExertion: Int? = null, // RPE 1-10
    val vo2MaxContribution: Double? = null, // estimated VO2 max improvement
    val notes: String? = null
)