package com.beaconledger.welltrack.data.workout

import com.beaconledger.welltrack.data.model.*
import java.util.UUID

/**
 * Norwegian 4x4 workout template and utilities
 * 
 * The Norwegian 4x4 method is a high-intensity interval training protocol
 * specifically designed to improve VO2 max. It consists of:
 * - 10-minute warm-up
 * - 4 intervals of 4 minutes at 85-95% max heart rate
 * - 3 minutes active recovery between intervals
 * - 5-minute cool-down
 */
object Norwegian4x4Template {
    
    fun createNorwegian4x4Workout(
        userId: String,
        userMaxHeartRate: Int,
        exerciseType: Norwegian4x4ExerciseType = Norwegian4x4ExerciseType.RUNNING
    ): ExerciseTemplate {
        val targetHeartRate = (userMaxHeartRate * 0.85).toInt()
        
        return ExerciseTemplate(
            id = UUID.randomUUID().toString(),
            name = "Norwegian 4x4 - ${exerciseType.displayName}",
            description = createDescription(exerciseType),
            category = ExerciseCategory.INTERVAL_TRAINING,
            muscleGroups = getMuscleGroups(exerciseType),
            equipment = getEquipment(exerciseType),
            instructions = createInstructions(exerciseType, targetHeartRate),
            difficulty = ExerciseDifficulty.ADVANCED,
            estimatedCaloriesPerMinute = getCaloriesPerMinute(exerciseType),
            targetHeartRateZone = HeartRateZone.ZONE_4_THRESHOLD,
            isCardio = true,
            isStrength = false,
            metadata = createMetadata(targetHeartRate, exerciseType)
        )
    }
    
    fun createNorwegian4x4Session(
        userMaxHeartRate: Int,
        exerciseType: Norwegian4x4ExerciseType = Norwegian4x4ExerciseType.RUNNING
    ): Norwegian4x4Session {
        val targetHeartRate = (userMaxHeartRate * 0.85).toInt()
        
        return Norwegian4x4Session(
            intervals = (1..4).map { intervalNumber ->
                Norwegian4x4Interval(
                    intervalNumber = intervalNumber,
                    workDuration = 4,
                    restDuration = if (intervalNumber < 4) 3 else 0,
                    targetHeartRate = targetHeartRate
                )
            },
            warmupDuration = 10,
            cooldownDuration = 5,
            targetIntensity = 85
        )
    }
    
    private fun createDescription(exerciseType: Norwegian4x4ExerciseType): String {
        return """
            The Norwegian 4x4 method is a scientifically proven high-intensity interval training protocol 
            designed to maximize VO2 max improvements. This ${exerciseType.displayName.lowercase()} workout consists of:
            
            • 10-minute warm-up at moderate intensity
            • 4 intervals of 4 minutes at 85-95% max heart rate
            • 3 minutes active recovery between intervals
            • 5-minute cool-down
            
            Research shows this method can improve VO2 max by 10-15% in 8-12 weeks when performed 2-3 times per week.
        """.trimIndent()
    }
    
    private fun getMuscleGroups(exerciseType: Norwegian4x4ExerciseType): String {
        val muscleGroups = when (exerciseType) {
            Norwegian4x4ExerciseType.RUNNING -> listOf(
                MuscleGroup.QUADRICEPS,
                MuscleGroup.HAMSTRINGS,
                MuscleGroup.GLUTES,
                MuscleGroup.CALVES,
                MuscleGroup.CORE,
                MuscleGroup.CARDIOVASCULAR_SYSTEM
            )
            Norwegian4x4ExerciseType.CYCLING -> listOf(
                MuscleGroup.QUADRICEPS,
                MuscleGroup.HAMSTRINGS,
                MuscleGroup.GLUTES,
                MuscleGroup.CALVES,
                MuscleGroup.CARDIOVASCULAR_SYSTEM
            )
            Norwegian4x4ExerciseType.ROWING -> listOf(
                MuscleGroup.BACK,
                MuscleGroup.SHOULDERS,
                MuscleGroup.BICEPS,
                MuscleGroup.CORE,
                MuscleGroup.QUADRICEPS,
                MuscleGroup.HAMSTRINGS,
                MuscleGroup.CARDIOVASCULAR_SYSTEM
            )
            Norwegian4x4ExerciseType.ELLIPTICAL -> listOf(
                MuscleGroup.FULL_BODY,
                MuscleGroup.CARDIOVASCULAR_SYSTEM
            )
        }
        return muscleGroups.joinToString(",") { it.name }
    }
    
    private fun getEquipment(exerciseType: Norwegian4x4ExerciseType): String {
        val equipment = when (exerciseType) {
            Norwegian4x4ExerciseType.RUNNING -> listOf(Equipment.NONE) // or TREADMILL
            Norwegian4x4ExerciseType.CYCLING -> listOf(Equipment.STATIONARY_BIKE)
            Norwegian4x4ExerciseType.ROWING -> listOf(Equipment.ROWING_MACHINE)
            Norwegian4x4ExerciseType.ELLIPTICAL -> listOf(Equipment.ELLIPTICAL)
        }
        return equipment.joinToString(",") { it.name }
    }
    
    private fun createInstructions(exerciseType: Norwegian4x4ExerciseType, targetHeartRate: Int): String {
        val baseInstructions = listOf(
            "Warm up for 10 minutes at moderate intensity (60-70% max HR)",
            "Perform 4 intervals of 4 minutes each at high intensity (85-95% max HR, target: $targetHeartRate bpm)",
            "Take 3 minutes active recovery between intervals (60-70% max HR)",
            "Cool down for 5 minutes at low intensity"
        )
        
        val specificInstructions = when (exerciseType) {
            Norwegian4x4ExerciseType.RUNNING -> listOf(
                "Maintain a steady, challenging pace during work intervals",
                "Use a comfortable jogging pace during recovery periods",
                "Focus on consistent breathing and form"
            )
            Norwegian4x4ExerciseType.CYCLING -> listOf(
                "Increase resistance or cadence during work intervals",
                "Maintain light pedaling during recovery periods",
                "Keep upper body relaxed and focus on leg power"
            )
            Norwegian4x4ExerciseType.ROWING -> listOf(
                "Maintain strong, consistent strokes during work intervals",
                "Use light, easy strokes during recovery periods",
                "Focus on proper rowing technique: legs, core, arms"
            )
            Norwegian4x4ExerciseType.ELLIPTICAL -> listOf(
                "Increase resistance and maintain high cadence during work intervals",
                "Use moderate resistance during recovery periods",
                "Engage both upper and lower body throughout"
            )
        }
        
        return (baseInstructions + specificInstructions).joinToString(",")
    }
    
    private fun getCaloriesPerMinute(exerciseType: Norwegian4x4ExerciseType): Double {
        return when (exerciseType) {
            Norwegian4x4ExerciseType.RUNNING -> 12.0
            Norwegian4x4ExerciseType.CYCLING -> 10.0
            Norwegian4x4ExerciseType.ROWING -> 11.0
            Norwegian4x4ExerciseType.ELLIPTICAL -> 9.0
        }
    }
    
    private fun createMetadata(targetHeartRate: Int, exerciseType: Norwegian4x4ExerciseType): String {
        val metadata = mapOf(
            "protocol" to "Norwegian 4x4",
            "targetHeartRate" to targetHeartRate,
            "exerciseType" to exerciseType.name,
            "totalDuration" to 39, // 10 + (4*4) + (3*3) + 5
            "workIntervals" to 4,
            "workDuration" to 4,
            "restDuration" to 3,
            "vo2MaxFocused" to true,
            "researchBacked" to true
        )
        return metadata.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
    
    fun calculateVO2MaxImprovement(
        sessions: List<Norwegian4x4Session>,
        timeframeDays: Int
    ): VO2MaxImprovementEstimate {
        val completedSessions = sessions.filter { session ->
            session.intervals.all { it.completed }
        }
        
        val sessionsPerWeek = (completedSessions.size.toDouble() / timeframeDays) * 7
        val averageHeartRateAchievement = completedSessions.mapNotNull { session ->
            session.actualAverageHeartRate?.let { actual ->
                (actual.toDouble() / session.targetIntensity) * 100
            }
        }.average()
        
        // Research-based estimation formula
        val baseImprovement = when {
            sessionsPerWeek >= 3 -> 0.15 // 15% improvement potential
            sessionsPerWeek >= 2 -> 0.12 // 12% improvement potential
            else -> 0.08 // 8% improvement potential
        }
        
        val intensityMultiplier = (averageHeartRateAchievement / 100).coerceIn(0.5, 1.2)
        val estimatedImprovement = baseImprovement * intensityMultiplier
        
        return VO2MaxImprovementEstimate(
            estimatedImprovementPercentage = estimatedImprovement,
            sessionsCompleted = completedSessions.size,
            averageIntensityAchieved = averageHeartRateAchievement,
            recommendedFrequency = if (sessionsPerWeek < 2) "Increase to 2-3 sessions per week" else "Maintain current frequency",
            timeToTarget = estimateTimeToTarget(estimatedImprovement, sessionsPerWeek)
        )
    }
    
    private fun estimateTimeToTarget(improvementRate: Double, sessionsPerWeek: Double): String {
        val weeksTo10PercentImprovement = (0.10 / improvementRate) * (2.5 / sessionsPerWeek)
        return when {
            weeksTo10PercentImprovement <= 8 -> "6-8 weeks for significant improvement"
            weeksTo10PercentImprovement <= 12 -> "8-12 weeks for significant improvement"
            else -> "12+ weeks for significant improvement"
        }
    }
}

enum class Norwegian4x4ExerciseType(val displayName: String) {
    RUNNING("Running"),
    CYCLING("Cycling"),
    ROWING("Rowing"),
    ELLIPTICAL("Elliptical")
}

data class VO2MaxImprovementEstimate(
    val estimatedImprovementPercentage: Double,
    val sessionsCompleted: Int,
    val averageIntensityAchieved: Double,
    val recommendedFrequency: String,
    val timeToTarget: String
)