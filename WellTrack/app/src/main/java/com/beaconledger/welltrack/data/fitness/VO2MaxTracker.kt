package com.beaconledger.welltrack.data.fitness

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.workout.Norwegian4x4Template
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for tracking VO2 Max progress and providing training recommendations
 */
@Singleton
class VO2MaxTracker @Inject constructor() {
    
    private val _currentVO2MaxGoal = MutableStateFlow<VO2MaxGoal?>(null)
    val currentVO2MaxGoal: StateFlow<VO2MaxGoal?> = _currentVO2MaxGoal.asStateFlow()
    
    private val _vo2MaxHistory = MutableStateFlow<List<VO2MaxProgress>>(emptyList())
    val vo2MaxHistory: StateFlow<List<VO2MaxProgress>> = _vo2MaxHistory.asStateFlow()
    
    /**
     * Sets a new VO2 Max goal for the user
     */
    fun setVO2MaxGoal(
        currentVO2Max: Double?,
        targetVO2Max: Double,
        targetDate: String,
        trainingPlan: VO2MaxTrainingPlan = VO2MaxTrainingPlan.NORWEGIAN_4X4_FOCUSED
    ) {
        val goal = VO2MaxGoal(
            currentVO2Max = currentVO2Max,
            targetVO2Max = targetVO2Max,
            targetDate = targetDate,
            trainingPlan = trainingPlan,
            progressTracking = _vo2MaxHistory.value
        )
        _currentVO2MaxGoal.value = goal
    }
    
    /**
     * Records a new VO2 Max measurement
     */
    fun recordVO2MaxMeasurement(
        measuredVO2Max: Double,
        testType: VO2MaxTestType,
        notes: String? = null
    ) {
        val progress = VO2MaxProgress(
            date = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            measuredVO2Max = measuredVO2Max,
            testType = testType,
            notes = notes
        )
        
        val updatedHistory = _vo2MaxHistory.value + progress
        _vo2MaxHistory.value = updatedHistory
        
        // Update current goal with new progress
        _currentVO2MaxGoal.value?.let { currentGoal ->
            _currentVO2MaxGoal.value = currentGoal.copy(
                progressTracking = updatedHistory,
                currentVO2Max = measuredVO2Max // Update current as the latest measurement
            )
        }
    }
    
    /**
     * Calculates VO2 Max improvement rate based on historical data
     */
    fun calculateImprovementRate(): VO2MaxImprovementRate? {
        val history = _vo2MaxHistory.value
        if (history.size < 2) return null
        
        val sortedHistory = history.sortedBy { it.date }
        val firstMeasurement = sortedHistory.first()
        val latestMeasurement = sortedHistory.last()
        
        val improvementAmount = latestMeasurement.measuredVO2Max - firstMeasurement.measuredVO2Max
        val improvementPercentage = (improvementAmount / firstMeasurement.measuredVO2Max) * 100
        
        // Calculate time difference in weeks
        val firstDate = LocalDateTime.parse(firstMeasurement.date)
        val latestDate = LocalDateTime.parse(latestMeasurement.date)
        val daysDifference = java.time.Duration.between(firstDate, latestDate).toDays()
        val weeksDifference = daysDifference / 7.0
        
        val improvementPerWeek = if (weeksDifference > 0) improvementPercentage / weeksDifference else 0.0
        
        return VO2MaxImprovementRate(
            totalImprovement = improvementAmount,
            improvementPercentage = improvementPercentage,
            timeframeDays = daysDifference.toInt(),
            improvementPerWeek = improvementPerWeek,
            trend = when {
                improvementPerWeek > 0.5 -> ImprovementTrend.EXCELLENT
                improvementPerWeek > 0.2 -> ImprovementTrend.GOOD
                improvementPerWeek > 0 -> ImprovementTrend.SLOW
                else -> ImprovementTrend.DECLINING
            }
        )
    }
    
    /**
     * Provides training recommendations based on current progress
     */
    fun getTrainingRecommendations(
        norwegian4x4Sessions: List<Norwegian4x4Session>,
        timeframeDays: Int
    ): VO2MaxTrainingRecommendations {
        val currentGoal = _currentVO2MaxGoal.value
        val improvementRate = calculateImprovementRate()
        val norwegian4x4Analysis = Norwegian4x4Template.calculateVO2MaxImprovement(
            norwegian4x4Sessions, 
            timeframeDays
        )
        
        val recommendations = mutableListOf<String>()
        
        // Frequency recommendations
        val sessionsPerWeek = (norwegian4x4Sessions.size.toDouble() / timeframeDays) * 7
        when {
            sessionsPerWeek < 2 -> {
                recommendations.add("Increase Norwegian 4x4 sessions to 2-3 times per week for optimal VO2 max improvement")
                recommendations.add("Consider adding one additional cardio session per week")
            }
            sessionsPerWeek > 3 -> {
                recommendations.add("Consider reducing frequency to 2-3 sessions per week to allow proper recovery")
                recommendations.add("Add strength training or cross-training on off days")
            }
            else -> {
                recommendations.add("Maintain current training frequency of 2-3 Norwegian 4x4 sessions per week")
            }
        }
        
        // Intensity recommendations
        val averageIntensity = norwegian4x4Analysis.averageIntensityAchieved
        when {
            averageIntensity < 80 -> {
                recommendations.add("Focus on reaching target heart rate zones during work intervals")
                recommendations.add("Consider heart rate monitor for better intensity tracking")
            }
            averageIntensity > 95 -> {
                recommendations.add("Excellent intensity achievement! Maintain current effort levels")
            }
            else -> {
                recommendations.add("Good intensity levels. Try to consistently hit 85-95% max HR during work intervals")
            }
        }
        
        // Progress-based recommendations
        improvementRate?.let { rate ->
            when (rate.trend) {
                ImprovementTrend.EXCELLENT -> {
                    recommendations.add("Outstanding progress! Continue current training approach")
                }
                ImprovementTrend.GOOD -> {
                    recommendations.add("Good progress. Consider adding variety with different cardio exercises")
                }
                ImprovementTrend.SLOW -> {
                    recommendations.add("Progress is slow. Consider increasing training intensity or frequency")
                    recommendations.add("Ensure adequate recovery and nutrition")
                }
                ImprovementTrend.DECLINING -> {
                    recommendations.add("Consider taking a recovery week to prevent overtraining")
                    recommendations.add("Review training intensity and ensure proper form")
                }
                ImprovementTrend.UNKNOWN -> {
                    recommendations.add("Continue tracking your progress to get personalized recommendations")
                }
            }
        }
        
        // Goal-specific recommendations
        currentGoal?.let { goal ->
            val currentVO2Max = goal.currentVO2Max ?: _vo2MaxHistory.value.lastOrNull()?.measuredVO2Max
            currentVO2Max?.let { current ->
                val remainingImprovement = goal.targetVO2Max - current
                val remainingImprovementPercentage = (remainingImprovement / current) * 100
                
                if (remainingImprovementPercentage > 15) {
                    recommendations.add("Target improvement of ${remainingImprovementPercentage.toInt()}% is ambitious. Consider extending timeline or adjusting target")
                } else if (remainingImprovementPercentage < 5) {
                    recommendations.add("You're close to your goal! Maintain consistency to achieve target")
                }
            }
        }
        
        return VO2MaxTrainingRecommendations(
            recommendations = recommendations,
            recommendedFrequency = norwegian4x4Analysis.recommendedFrequency,
            estimatedTimeToGoal = norwegian4x4Analysis.timeToTarget,
            currentTrend = improvementRate?.trend ?: ImprovementTrend.UNKNOWN,
            nextTestRecommendation = getNextTestRecommendation()
        )
    }
    
    private fun getNextTestRecommendation(): String {
        val lastTest = _vo2MaxHistory.value.maxByOrNull { it.date }
        return if (lastTest == null) {
            "Take a baseline VO2 max test to establish starting point"
        } else {
            val lastTestDate = LocalDateTime.parse(lastTest.date)
            val daysSinceLastTest = java.time.Duration.between(lastTestDate, LocalDateTime.now()).toDays()
            
            when {
                daysSinceLastTest < 14 -> "Wait at least 2 weeks before next test"
                daysSinceLastTest < 28 -> "Consider testing in 1-2 weeks to track progress"
                else -> "Recommended to test VO2 max to track progress"
            }
        }
    }
    
    /**
     * Estimates VO2 Max based on workout performance
     */
    fun estimateVO2MaxFromWorkout(
        workout: Workout,
        userAge: Int,
        userWeight: Double // in kg
    ): Double? {
        // This is a simplified estimation - in practice, you'd use more sophisticated algorithms
        return when (workout.type) {
            WorkoutType.NORWEGIAN_4X4 -> {
                workout.averageHeartRate?.let { avgHR ->
                    // Simplified estimation based on heart rate and duration
                    val hrReserve = avgHR - (220 - userAge) * 0.6 // Rough resting HR estimate
                    val intensity = hrReserve / ((220 - userAge) * 0.4)
                    
                    // Base VO2 max estimation (very simplified)
                    val baseVO2Max = 15.0 + (intensity * 25.0) // Rough estimation
                    baseVO2Max.coerceIn(20.0, 80.0) // Reasonable bounds
                }
            }
            else -> null
        }
    }
    
    /**
     * Gets VO2 Max category based on age and gender
     */
    fun getVO2MaxCategory(vo2Max: Double, age: Int, isMale: Boolean): VO2MaxCategory {
        // Simplified categorization - in practice, you'd use detailed lookup tables
        val categories = if (isMale) {
            when {
                age < 30 -> listOf(32.0, 38.0, 44.0, 50.0, 56.0)
                age < 40 -> listOf(31.0, 35.0, 41.0, 45.0, 52.0)
                age < 50 -> listOf(25.0, 31.0, 35.0, 41.0, 45.0)
                else -> listOf(21.0, 26.0, 31.0, 35.0, 41.0)
            }
        } else {
            when {
                age < 30 -> listOf(27.0, 31.0, 35.0, 41.0, 45.0)
                age < 40 -> listOf(26.0, 30.0, 33.0, 37.0, 41.0)
                age < 50 -> listOf(22.0, 26.0, 30.0, 33.0, 37.0)
                else -> listOf(17.0, 21.0, 24.0, 30.0, 33.0)
            }
        }
        
        return when {
            vo2Max < categories[0] -> VO2MaxCategory.POOR
            vo2Max < categories[1] -> VO2MaxCategory.FAIR
            vo2Max < categories[2] -> VO2MaxCategory.AVERAGE
            vo2Max < categories[3] -> VO2MaxCategory.GOOD
            vo2Max < categories[4] -> VO2MaxCategory.EXCELLENT
            else -> VO2MaxCategory.SUPERIOR
        }
    }
}

data class VO2MaxImprovementRate(
    val totalImprovement: Double,
    val improvementPercentage: Double,
    val timeframeDays: Int,
    val improvementPerWeek: Double,
    val trend: ImprovementTrend
)

data class VO2MaxTrainingRecommendations(
    val recommendations: List<String>,
    val recommendedFrequency: String,
    val estimatedTimeToGoal: String,
    val currentTrend: ImprovementTrend,
    val nextTestRecommendation: String
)

enum class ImprovementTrend {
    EXCELLENT,
    GOOD,
    SLOW,
    DECLINING,
    UNKNOWN
}

enum class VO2MaxCategory {
    POOR,
    FAIR,
    AVERAGE,
    GOOD,
    EXCELLENT,
    SUPERIOR
}