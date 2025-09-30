package com.beaconledger.welltrack.data.export

import android.content.Context
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WellTrackDatabase,
    private val gson: Gson
) {
    
    private val exportDir = File(context.getExternalFilesDir(null), "exports")
    
    init {
        if (!exportDir.exists()) {
            exportDir.mkdirs()
        }
    }
    
    suspend fun exportUserDataToJson(
        userId: String,
        request: ExportRequest
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportData = collectUserData(userId, request)
            val fileName = generateFileName(userId, "json", request.exportType)
            val file = File(exportDir, fileName)
            
            FileWriter(file).use { writer ->
                gson.toJson(exportData, writer)
            }
            
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun exportUserDataToCsv(
        userId: String,
        request: ExportRequest
    ): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val files = mutableListOf<File>()

            if (request.includeMealData) {
                val mealsFile = exportMealsToCsv(userId, request.dateRange)
                files.add(mealsFile)
            }

            if (request.includeHealthData) {
                val healthFile = exportHealthDataToCsv(userId, request.dateRange)
                files.add(healthFile)
            }

            if (request.includeSupplementData) {
                val supplementsFile = exportSupplementsToCsv(userId, request.dateRange)
                files.add(supplementsFile)
            }

            if (request.includeBiomarkerData) {
                val biomarkersFile = exportBiomarkersToCsv(userId, request.dateRange)
                files.add(biomarkersFile)
            }

            if (request.includeGoalData) {
                val goalsFile = exportGoalsToCsv(userId, request.dateRange)
                files.add(goalsFile)
            }

            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateHealthReport(
        userId: String,
        request: ExportRequest
    ): Result<HealthReport> = withContext(Dispatchers.IO) {
        try {
            val exportData = collectUserData(userId, request)

            val healthReport = HealthReport(
                userId = userId,
                reportPeriod = request.dateRange ?: DateRange(
                    LocalDateTime.now().minusDays(30),
                    LocalDateTime.now()
                ),
                summary = generateHealthSummary(exportData),
                nutritionAnalysis = generateNutritionAnalysis(exportData.meals),
                fitnessMetrics = generateFitnessMetrics(exportData.healthMetrics),
                supplementAdherence = generateSupplementAdherence(exportData.supplements),
                biomarkerTrends = generateBiomarkerTrends(exportData.biomarkers),
                goalProgress = generateGoalProgressReport(exportData.goals),
                recommendations = generateRecommendations(exportData),
                generatedAt = LocalDateTime.now()
            )

            Result.success(healthReport)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun collectUserData(
        userId: String,
        request: ExportRequest
    ): UserExportData {
        val userProfile = database.userDao().getUserById(userId)
        
        val meals = if (request.includeMealData) {
            request.dateRange?.let { range ->
                database.mealDao().getMealsInDateRange(userId, range.startDate, range.endDate)
            } ?: database.mealDao().getAllMealsForUser(userId)
        } else emptyList()
        
        val healthMetrics = if (request.includeHealthData) {
            request.dateRange?.let { range ->
                database.healthMetricDao().getMetricsInDateRange(userId, range.startDate, range.endDate)
            } ?: database.healthMetricDao().getAllMetricsForUser(userId)
        } else emptyList()
        
        val supplements = if (request.includeSupplementData) {
            database.supplementDao().getAllSupplementsForUser(userId)
        } else emptyList()
        
        val biomarkers = if (request.includeBiomarkerData) {
            database.biomarkerDao().getAllBiomarkersForUser(userId)
        } else emptyList()
        
        val goals = if (request.includeGoalData) {
            database.goalDao().getAllGoalsForUser(userId)
        } else emptyList()
        
        return UserExportData(
            userProfile = userProfile,
            meals = meals,
            healthMetrics = healthMetrics,
            supplements = supplements,
            biomarkers = biomarkers,
            goals = goals,
            exportMetadata = ExportMetadata(
                exportId = UUID.randomUUID().toString(),
                userId = userId,
                exportType = request.exportType,
                exportedAt = LocalDateTime.now(),
                dateRange = request.dateRange,
                appVersion = getAppVersion()
            )
        )
    }
    
    private suspend fun exportMealsToCsv(userId: String, dateRange: DateRange?): File {
        val meals = dateRange?.let { range ->
            database.mealDao().getMealsInDateRange(userId, range.startDate, range.endDate)
        } ?: database.mealDao().getAllMealsForUser(userId)
        
        val fileName = generateFileName(userId, "csv", ExportType.MEAL_HISTORY, "meals")
        val file = File(exportDir, fileName)
        
        FileWriter(file).use { writer ->
            // CSV Header
            writer.append("Date,Time,Meal Type,Recipe Name,Calories,Protein,Carbs,Fat,Fiber,Score,Status,Notes\n")
            
            meals.forEach { meal ->
                writer.append("${meal.timestamp.toLocalDate()},")
                writer.append("${meal.timestamp.toLocalTime()},")
                writer.append("${meal.mealType},")
                writer.append("\"${meal.recipeName ?: ""}\",")
                writer.append("${meal.nutritionInfo?.calories ?: 0},")
                writer.append("${meal.nutritionInfo?.protein ?: 0},")
                writer.append("${meal.nutritionInfo?.carbohydrates ?: 0},")
                writer.append("${meal.nutritionInfo?.fat ?: 0},")
                writer.append("${meal.nutritionInfo?.fiber ?: 0},")
                writer.append("${meal.score?.grade ?: ""},")
                writer.append("${meal.status},")
                writer.append("\"${meal.notes ?: ""}\"\n")
            }
        }
        
        return file
    }
    
    private suspend fun exportHealthDataToCsv(userId: String, dateRange: DateRange?): File {
        val healthMetrics = dateRange?.let { range ->
            database.healthMetricDao().getMetricsInDateRange(userId, range.startDate, range.endDate)
        } ?: database.healthMetricDao().getAllMetricsForUser(userId)
        
        val fileName = generateFileName(userId, "csv", ExportType.HEALTH_REPORT, "health_data")
        val file = File(exportDir, fileName)
        
        FileWriter(file).use { writer ->
            writer.append("Date,Time,Metric Type,Value,Unit,Source,Notes\n")
            
            healthMetrics.forEach { metric ->
                writer.append("${metric.timestamp.toLocalDate()},")
                writer.append("${metric.timestamp.toLocalTime()},")
                writer.append("${metric.type},")
                writer.append("${metric.value},")
                writer.append("${metric.unit},")
                writer.append("${metric.source},")
                writer.append("\"${metric.metadata ?: ""}\"\n")
            }
        }
        
        return file
    }
    
    private suspend fun exportSupplementsToCsv(userId: String, dateRange: DateRange?): File {
        val supplements = database.supplementDao().getAllSupplementsForUser(userId)
        
        val fileName = generateFileName(userId, "csv", ExportType.SUPPLEMENT_LOG, "supplements")
        val file = File(exportDir, fileName)
        
        FileWriter(file).use { writer ->
            writer.append("Date,Supplement Name,Dosage,Unit,Frequency,Taken,Notes\n")
            
            supplements.forEach { supplement ->
                writer.append("${supplement.createdAt.toLocalDate()},")
                writer.append("\"${supplement.name}\",")
                writer.append("${supplement.dosage},")
                writer.append("${supplement.unit},")
                writer.append("${supplement.frequency},")
                writer.append("${supplement.isTaken},")
                writer.append("\"${supplement.notes ?: ""}\"\n")
            }
        }
        
        return file
    }
    
    private suspend fun exportBiomarkersToCsv(userId: String, dateRange: DateRange?): File {
        val biomarkers = database.biomarkerDao().getAllBiomarkersForUser(userId)
        
        val fileName = generateFileName(userId, "csv", ExportType.BIOMARKER_REPORT, "biomarkers")
        val file = File(exportDir, fileName)
        
        FileWriter(file).use { writer ->
            writer.append("Date,Biomarker Type,Value,Unit,Reference Range,Status,Notes\n")
            
            biomarkers.forEach { biomarker ->
                writer.append("${biomarker.testDate.toLocalDate()},")
                writer.append("${biomarker.type},")
                writer.append("${biomarker.value},")
                writer.append("${biomarker.unit},")
                writer.append("\"${biomarker.referenceRange ?: ""}\",")
                writer.append("${biomarker.status},")
                writer.append("\"${biomarker.notes ?: ""}\"\n")
            }
        }
        
        return file
    }
    
    private suspend fun exportGoalsToCsv(userId: String, dateRange: DateRange?): File {
        val goals = database.goalDao().getAllGoalsForUser(userId)
        
        val fileName = generateFileName(userId, "csv", ExportType.GOAL_PROGRESS, "goals")
        val file = File(exportDir, fileName)
        
        FileWriter(file).use { writer ->
            writer.append("Goal Type,Title,Target Value,Current Value,Progress %,Status,Start Date,Target Date,Completed Date\n")
            
            goals.forEach { goal ->
                writer.append("${goal.type},")
                writer.append("\"${goal.title}\",")
                writer.append("${goal.targetValue},")
                writer.append("${goal.currentValue},")
                writer.append("${goal.progressPercentage},")
                writer.append("${goal.status},")
                writer.append("${goal.startDate},")
                writer.append("${goal.targetDate ?: ""},")
                writer.append("${goal.completedDate ?: ""}\n")
            }
        }
        
        return file
    }
    
    private fun generateFileName(
        userId: String,
        extension: String,
        exportType: ExportType,
        suffix: String? = null
    ): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val userPrefix = userId.take(8)
        val typeString = exportType.name.lowercase()
        val suffixString = suffix?.let { "_$it" } ?: ""
        
        return "welltrack_${userPrefix}_${typeString}${suffixString}_${timestamp}.${extension}"
    }
    
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName
        } catch (e: Exception) {
            "unknown"
        }
    }

    // Health report generation helper methods
    private fun generateHealthSummary(exportData: UserExportData): HealthSummary {
        return HealthSummary(
            totalMealsLogged = exportData.meals.size,
            averageMealScore = exportData.meals.mapNotNull { it.score?.grade?.toFloatOrNull() }.average().toFloat(),
            supplementComplianceRate = calculateSupplementCompliance(exportData.supplements),
            activeGoals = exportData.goals.count { it.isActive },
            completedGoals = exportData.goals.count { it.currentValue >= it.targetValue },
            healthConnectDataPoints = exportData.healthMetrics.size
        )
    }

    private fun generateNutritionAnalysis(meals: List<Meal>): NutritionAnalysis {
        val mealsByDay = meals.groupBy { it.timestamp.toLocalDate() }
        val dailyCalories = mealsByDay.map { (_, dayMeals) ->
            dayMeals.sumOf { meal -> meal.nutritionInfo?.calories ?: 0.0 }
        }

        val totalNutrition = meals.mapNotNull { it.nutritionInfo }.fold(
            Triple(0.0, 0.0, 0.0) // protein, carbs, fat
        ) { acc, nutrition ->
            Triple(
                acc.first + (nutrition.protein ?: 0.0),
                acc.second + (nutrition.carbohydrates ?: 0.0),
                acc.third + (nutrition.fat ?: 0.0)
            )
        }

        return NutritionAnalysis(
            averageDailyCalories = dailyCalories.average(),
            macronutrientBreakdown = mapOf(
                "Protein" to totalNutrition.first / meals.size,
                "Carbohydrates" to totalNutrition.second / meals.size,
                "Fat" to totalNutrition.third / meals.size
            ),
            micronutrientStatus = emptyMap(), // Would need detailed micronutrient tracking
            hydrationAverage = 2.5, // Default value, would need hydration tracking
            mealTimingPatterns = meals.groupBy { it.mealType }.mapValues { it.value.size }
        )
    }

    private fun generateFitnessMetrics(healthMetrics: List<HealthMetric>): FitnessMetrics {
        val stepMetrics = healthMetrics.filter { it.type == HealthMetricType.STEPS }
        val heartRateMetrics = healthMetrics.filter { it.type == HealthMetricType.HEART_RATE }

        return FitnessMetrics(
            averageSteps = stepMetrics.map { it.value.toInt() }.average().toInt(),
            averageHeartRate = heartRateMetrics.map { it.value.toInt() }.average().toInt(),
            workoutFrequency = calculateWorkoutFrequency(healthMetrics),
            sleepQuality = calculateSleepQuality(healthMetrics),
            stressLevels = calculateStressLevels(healthMetrics)
        )
    }

    private fun generateSupplementAdherence(supplements: List<Supplement>): SupplementAdherence {
        val totalSupplements = supplements.size
        val takenSupplements = supplements.count { it.isTaken }

        return SupplementAdherence(
            totalSupplements = totalSupplements,
            adherenceRate = if (totalSupplements > 0) takenSupplements.toFloat() / totalSupplements else 0f,
            missedDoses = totalSupplements - takenSupplements,
            supplementEffectiveness = supplements.associate {
                it.name to if (it.isTaken) "Adherent" else "Missed"
            }
        )
    }

    private fun generateBiomarkerTrends(biomarkers: List<Biomarker>): List<BiomarkerTrend> {
        return biomarkers.groupBy { it.type }.map { (type, markers) ->
            val sorted = markers.sortedBy { it.testDate }
            val latest = sorted.lastOrNull()
            val previous = sorted.dropLast(1).lastOrNull()

            BiomarkerTrend(
                biomarkerType = type.toString(),
                trend = if (latest != null && previous != null) {
                    when {
                        latest.value > previous.value -> "improving"
                        latest.value < previous.value -> "declining"
                        else -> "stable"
                    }
                } else "stable",
                latestValue = latest?.value ?: 0.0,
                previousValue = previous?.value,
                targetRange = latest?.referenceRange ?: "Unknown"
            )
        }
    }

    private fun generateGoalProgressReport(goals: List<Goal>): List<com.beaconledger.welltrack.data.model.GoalProgress> {
        return goals.map { goal ->
            com.beaconledger.welltrack.data.model.GoalProgress(
                goalId = goal.id,
                goalType = goal.type.toString(),
                targetValue = goal.targetValue,
                currentValue = goal.currentValue,
                progressPercentage = if (goal.targetValue > 0) {
                    ((goal.currentValue / goal.targetValue) * 100).toFloat()
                } else 0f,
                expectedCompletionDate = if (goal.currentValue < goal.targetValue) {
                    // Simple calculation - could be enhanced with trend analysis
                    LocalDateTime.now().plusDays(
                        ChronoUnit.DAYS.between(LocalDate.now(), goal.targetDate)
                    )
                } else null
            )
        }
    }

    private fun generateRecommendations(exportData: UserExportData): List<String> {
        val recommendations = mutableListOf<String>()

        // Nutrition recommendations
        if (exportData.meals.isNotEmpty()) {
            val avgScore = exportData.meals.mapNotNull { it.score?.grade?.toFloatOrNull() }.average()
            if (avgScore < 7.0) {
                recommendations.add("Consider improving meal quality - focus on more whole foods and balanced nutrition.")
            }
        }

        // Goal recommendations
        val overdueGoals = exportData.goals.filter {
            it.targetDate.isBefore(LocalDate.now()) && it.currentValue < it.targetValue
        }
        if (overdueGoals.isNotEmpty()) {
            recommendations.add("${overdueGoals.size} goals are overdue. Consider adjusting timelines or strategies.")
        }

        // Supplement recommendations
        val missedSupplements = exportData.supplements.count { !it.isTaken }
        if (missedSupplements > 0) {
            recommendations.add("Improve supplement adherence - you've missed $missedSupplements doses recently.")
        }

        // Health data recommendations
        if (exportData.healthMetrics.isNotEmpty()) {
            recommendations.add("Great job tracking your health metrics! Consistent monitoring helps identify trends.")
        }

        if (recommendations.isEmpty()) {
            recommendations.add("Keep up the excellent work maintaining your health tracking routine!")
        }

        return recommendations
    }

    // Helper calculation methods
    private fun calculateSupplementCompliance(supplements: List<Supplement>): Float {
        return if (supplements.isNotEmpty()) {
            supplements.count { it.isTaken }.toFloat() / supplements.size
        } else 0f
    }

    private fun calculateWorkoutFrequency(healthMetrics: List<HealthMetric>): Int {
        // Simple calculation based on activity metrics
        val recentDays = 7
        val activityMetrics = healthMetrics.filter {
            it.type == HealthMetricType.CALORIES_BURNED &&
            it.timestamp.isAfter(LocalDateTime.now().minusDays(recentDays.toLong()))
        }
        return activityMetrics.count { it.value > 300 } // Assume >300 calories = workout
    }

    private fun calculateSleepQuality(healthMetrics: List<HealthMetric>): Float? {
        val sleepMetrics = healthMetrics.filter { it.type == HealthMetricType.SLEEP_DURATION }
        return if (sleepMetrics.isNotEmpty()) {
            val avgSleep = sleepMetrics.map { it.value }.average()
            // Convert hours to quality score (optimal 7-9 hours = 10/10)
            when {
                avgSleep >= 7 && avgSleep <= 9 -> 9.0f
                avgSleep >= 6 && avgSleep <= 10 -> 7.0f
                else -> 5.0f
            }
        } else null
    }

    private fun calculateStressLevels(healthMetrics: List<HealthMetric>): Float? {
        // Would need stress-specific metrics, for now return null
        return null
    }
}

data class UserExportData(
    val userProfile: User?,
    val meals: List<Meal>,
    val healthMetrics: List<HealthMetric>,
    val supplements: List<Supplement>,
    val biomarkers: List<Biomarker>,
    val goals: List<Goal>,
    val exportMetadata: ExportMetadata
)

data class ExportMetadata(
    val exportId: String,
    val userId: String,
    val exportType: ExportType,
    val exportedAt: LocalDateTime,
    val dateRange: DateRange?,
    val appVersion: String
)