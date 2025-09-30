package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "data_exports")
data class DataExport(
    @PrimaryKey
    val id: String,
    val userId: String,
    val exportType: ExportType,
    val format: ExportFormat,
    val status: ExportStatus,
    val filePath: String?,
    val fileSize: Long?,
    val dateRange: DateRange?,
    val includeHealthData: Boolean,
    val includeMealData: Boolean,
    val includeSupplementData: Boolean,
    val includeBiomarkerData: Boolean,
    val includeGoalData: Boolean,
    val createdAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val errorMessage: String?
)

enum class ExportType {
    FULL_BACKUP,
    HEALTH_REPORT,
    MEAL_HISTORY,
    SUPPLEMENT_LOG,
    BIOMARKER_REPORT,
    GOAL_PROGRESS,
    CUSTOM_SELECTION
}

enum class ExportFormat {
    JSON,
    CSV,
    PDF,
    ZIP
}

enum class ExportStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class DateRange(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

data class ExportRequest(
    val userId: String,
    val exportType: ExportType,
    val format: ExportFormat,
    val dateRange: DateRange?,
    val includeHealthData: Boolean = true,
    val includeMealData: Boolean = true,
    val includeSupplementData: Boolean = true,
    val includeBiomarkerData: Boolean = true,
    val includeGoalData: Boolean = true,
    val customFields: List<String> = emptyList()
)

data class ImportRequest(
    val userId: String,
    val sourceApp: String,
    val filePath: String,
    val dataType: ImportDataType,
    val mergeStrategy: MergeStrategy
)

enum class ImportDataType {
    HEALTH_DATA,
    MEAL_DATA,
    SUPPLEMENT_DATA,
    FULL_BACKUP
}

enum class MergeStrategy {
    REPLACE_ALL,
    MERGE_NEW_ONLY,
    MERGE_WITH_CONFLICT_RESOLUTION
}

data class HealthReport(
    val userId: String,
    val reportPeriod: DateRange,
    val summary: HealthSummary,
    val nutritionAnalysis: NutritionAnalysis,
    val fitnessMetrics: FitnessMetrics,
    val supplementAdherence: SupplementAdherence,
    val biomarkerTrends: List<BiomarkerTrend>,
    val goalProgress: List<GoalProgress>,
    val recommendations: List<String>,
    val generatedAt: LocalDateTime
)

data class HealthSummary(
    val totalMealsLogged: Int,
    val averageMealScore: Float,
    val supplementComplianceRate: Float,
    val activeGoals: Int,
    val completedGoals: Int,
    val healthConnectDataPoints: Int
)

data class NutritionAnalysis(
    val averageDailyCalories: Double,
    val macronutrientBreakdown: Map<String, Double>,
    val micronutrientStatus: Map<String, String>,
    val hydrationAverage: Double,
    val mealTimingPatterns: Map<String, Int>
)

data class FitnessMetrics(
    val averageSteps: Int,
    val averageHeartRate: Int?,
    val workoutFrequency: Int,
    val sleepQuality: Float?,
    val stressLevels: Float?
)

data class SupplementAdherence(
    val totalSupplements: Int,
    val adherenceRate: Float,
    val missedDoses: Int,
    val supplementEffectiveness: Map<String, String>
)

data class BiomarkerTrend(
    val biomarkerType: String,
    val trend: String, // "improving", "stable", "declining"
    val latestValue: Double,
    val previousValue: Double?,
    val targetRange: String
)

data class GoalProgress(
    val goalId: String,
    val goalType: String,
    val targetValue: Double,
    val currentValue: Double,
    val progressPercentage: Float,
    val expectedCompletionDate: LocalDateTime?
)