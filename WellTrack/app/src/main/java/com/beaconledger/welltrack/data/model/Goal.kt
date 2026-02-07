package com.beaconledger.welltrack.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverters
import com.beaconledger.welltrack.data.database.Converters
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "goals")
@TypeConverters(Converters::class)
data class Goal(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: GoalType,
    val title: String,
    val description: String?,
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val unit: String,
    val startDate: LocalDate,
    val targetDate: LocalDate,
    val isActive: Boolean = true,
    val priority: GoalPriority = GoalPriority.MEDIUM,
    val category: GoalCategory,
    val milestones: List<GoalMilestone> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

@Entity(tableName = "goal_progress")
@TypeConverters(Converters::class)
data class GoalProgress(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val value: Double,
    val notes: String?,
    val recordedAt: LocalDateTime = LocalDateTime.now(),
    val source: ProgressSource = ProgressSource.MANUAL
)

@Entity(tableName = "goal_milestones")
@TypeConverters(Converters::class)
data class GoalMilestone(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val title: String,
    val description: String?,
    val targetValue: Double,
    val targetDate: LocalDate?,
    val isCompleted: Boolean = false,
    val completedAt: LocalDateTime?,
    val order: Int
)

@Entity(tableName = "goal_predictions")
@TypeConverters(Converters::class)
data class GoalPrediction(
    @PrimaryKey
    val id: String,
    val goalId: String,
    val predictedCompletionDate: LocalDate,
    val confidenceScore: Float, // 0.0 to 1.0
    val trendAnalysis: GoalTrend,
    val recommendedAdjustments: List<String>,
    val calculatedAt: LocalDateTime = LocalDateTime.now()
)

enum class GoalType {
    WEIGHT_LOSS,
    WEIGHT_GAIN,
    MUSCLE_GAIN,
    BODY_FAT_REDUCTION,
    FITNESS_PERFORMANCE,
    NUTRITION_TARGET,
    HABIT_FORMATION,
    HEALTH_METRIC,
    CUSTOM
}

enum class GoalCategory {
    WEIGHT,
    FITNESS,
    NUTRITION,
    HABITS,
    HEALTH
}

enum class GoalPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class ProgressSource {
    MANUAL,
    HEALTH_CONNECT,
    GARMIN,
    GARMIN_CONNECT,
    SAMSUNG_HEALTH,
    MEAL_LOGGING,
    HABIT_TRACKING,
    AUTOMATIC
}

enum class GoalTrend {
    ON_TRACK,
    AHEAD_OF_SCHEDULE,
    BEHIND_SCHEDULE,
    STAGNANT,
    DECLINING,
    ACCELERATING
}

data class GoalWithProgress(
    @Embedded val goal: Goal,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val progressEntries: List<GoalProgress>,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val milestones: List<GoalMilestone>,
    @Relation(
        parentColumn = "id",
        entityColumn = "goalId"
    )
    val predictions: List<GoalPrediction>
) {
    val prediction: GoalPrediction? get() = predictions.maxByOrNull { it.calculatedAt }
}

data class GoalStatistics(
    val completionPercentage: Float,
    val daysRemaining: Int,
    val averageDailyProgress: Double,
    val requiredDailyProgress: Double,
    val trendDirection: GoalTrend,
    val milestoneCompletionRate: Float
)