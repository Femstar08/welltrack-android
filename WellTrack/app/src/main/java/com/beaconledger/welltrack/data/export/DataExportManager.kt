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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
            val fileName = generateFileName(userId, "json", request.exportType)
            val file = File(exportDir, fileName)
            
            FileWriter(file).use { writer ->
                val jsonWriter = com.google.gson.stream.JsonWriter(writer)
                jsonWriter.beginObject() // Start UserExportData object

                // Write export metadata
                val exportMetadata = ExportMetadata(
                    exportId = UUID.randomUUID().toString(),
                    userId = userId,
                    exportType = request.exportType,
                    exportedAt = LocalDateTime.now(),
                    dateRange = request.dateRange,
                    appVersion = getAppVersion()
                )
                jsonWriter.name("exportMetadata")
                gson.toJson(exportMetadata, ExportMetadata::class.java, jsonWriter)

                // Write user profile
                val userProfile = database.userDao().getUserById(userId)
                jsonWriter.name("userProfile")
                gson.toJson(userProfile, User::class.java, jsonWriter)

                // Write meals
                if (request.includeMealData) {
                    jsonWriter.name("meals")
                    jsonWriter.beginArray()
                    val meals = request.dateRange?.let { range ->
                        database.mealDao().getMealsInDateRange(userId, range.startDate, range.endDate)
                    } ?: database.mealDao().getAllMealsForUser(userId)
                    meals.forEach { meal ->
                        gson.toJson(meal, Meal::class.java, jsonWriter)
                    }
                    jsonWriter.endArray()
                }

                // Write health metrics
                if (request.includeHealthData) {
                    jsonWriter.name("healthMetrics")
                    jsonWriter.beginArray()
                    val healthMetrics = request.dateRange?.let { range ->
                        database.healthMetricDao().getMetricsInDateRange(userId, range.startDate, range.endDate)
                    } ?: database.healthMetricDao().getAllMetricsForUser(userId)
                    healthMetrics.forEach { metric ->
                        gson.toJson(metric, HealthMetric::class.java, jsonWriter)
                    }
                    jsonWriter.endArray()
                }

                // Write supplements
                if (request.includeSupplementData) {
                    jsonWriter.name("supplements")
                    jsonWriter.beginArray()
                    val supplements = database.supplementDao().getAllSupplementsForUser(userId)
                    supplements.forEach { supplement ->
                        gson.toJson(supplement, Supplement::class.java, jsonWriter)
                    }
                    jsonWriter.endArray()
                }

                // Write biomarkers
                if (request.includeBiomarkerData) {
                    jsonWriter.name("biomarkers")
                    jsonWriter.beginArray()
                    val biomarkers = database.biomarkerDao().getAllBiomarkersForUser(userId)
                    biomarkers.forEach { biomarker ->
                        gson.toJson(biomarker, BiomarkerEntry::class.java, jsonWriter)
                    }
                    jsonWriter.endArray()
                }

                // Write goals
                if (request.includeGoalData) {
                    jsonWriter.name("goals")
                    jsonWriter.beginArray()
                    val goals = database.goalDao().getAllGoalsForUser(userId)
                    goals.forEach { goal ->
                        gson.toJson(goal, Goal::class.java, jsonWriter)
                    }
                    jsonWriter.endArray()
                }

                jsonWriter.endObject() // End UserExportData object
                jsonWriter.flush()
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
                reportPeriod = request.dateRange ?: ExportDateRange(
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
    
    private suspend fun exportMealsToCsv(userId: String, dateRange: ExportDateRange?): File {
        val meals = dateRange?.let { range ->
            database.mealDao().getMealsInDateRange(userId, range.startDate, range.endDate)
        } ?: database.mealDao().getAllMealsForUser(userId)
        
        val fileName = generateFileName(userId, "csv", ExportType.MEAL_HISTORY, "meals")
        val file = File(exportDir, fileName)
        
        FileWriter(file).use { writer ->
            // CSV Header
            writer.append("Date,Time,Meal Type,Recipe Name,Calories,Protein,Carbs,Fat,Fiber,Score,Status,Notes\n")
            
            meals.forEach { meal ->
                val timestamp = LocalDateTime.parse(meal.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                writer.append("${timestamp.toLocalDate()},")
                writer.append("${timestamp.toLocalTime()},")
                writer.append("${meal.mealType},")
                writer.append("\"${meal.recipeId ?: ""}\",")
                writer.append("0,") // Calories - would need to parse JSON nutritionInfo
                writer.append("0,") // Protein
                writer.append("0,") // Carbs
                writer.append("0,") // Fat
                writer.append("0,") // Fiber
                writer.append("${meal.score.grade},")
                writer.append("${meal.status},")
                writer.append("\"${meal.notes ?: ""}\"\n")
            }
        }
        
        return file
    }
    
    private suspend fun exportHealthDataToCsv(userId: String, dateRange: ExportDateRange?): File {
        val healthMetrics = dateRange?.let { range ->
            database.healthMetricDao().getMetricsInDateRange(userId, range.startDate, range.endDate)
        } ?: database.healthMetricDao().getAllMetricsForUser(userId)
        
        val fileName = generateFileName(userId, "csv", ExportType.HEALTH_REPORT, "health_data")
        val file = File(exportDir, fileName)
        
        FileWriter(file).use { writer ->
            writer.append("Date,Time,Metric Type,Value,Unit,Source,Notes\n")
            
            healthMetrics.forEach { metric ->
                val timestamp = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                writer.append("${timestamp.toLocalDate()},")
                writer.append("${timestamp.toLocalTime()},")
                writer.append("${metric.type},")
                writer.append("${metric.value},")
                writer.append("${metric.unit},")
                writer.append("${metric.source},")
                writer.append("\"${metric.metadata ?: ""}\"\n")
            }
        }
        
        return file
    }
    
    private suspend fun exportSupplementsToCsv(userId: String, dateRange: ExportDateRange?): File {
        val supplements = database.supplementDao().getAllSupplementsForUser(userId)

        val fileName = generateFileName(userId, "csv", ExportType.SUPPLEMENT_LOG, "supplements")
        val file = File(exportDir, fileName)

        FileWriter(file).use { writer ->
            writer.append("Date,Supplement Name,Category,Serving Size,Unit,Notes\n")

            supplements.forEach { supplement ->
                val createdAt = LocalDateTime.parse(supplement.createdAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                writer.append("${createdAt.toLocalDate()},")
                writer.append("\"${supplement.name}\",")
                writer.append("${supplement.category},")
                writer.append("${supplement.servingSize},")
                writer.append("${supplement.servingUnit},")
                writer.append("\"${supplement.description ?: ""}\"\n")
            }
        }

        return file
    }
    
    private suspend fun exportBiomarkersToCsv(userId: String, dateRange: ExportDateRange?): File {
        val biomarkers = database.biomarkerDao().getAllBiomarkersForUser(userId)

        val fileName = generateFileName(userId, "csv", ExportType.BIOMARKER_REPORT, "biomarkers")
        val file = File(exportDir, fileName)

        FileWriter(file).use { writer ->
            writer.append("Date,Biomarker Type,Value,Unit,Reference Range Min,Reference Range Max,Within Range,Lab Name,Notes\n")

            biomarkers.forEach { biomarker ->
                val testDate = LocalDate.parse(biomarker.testDate)
                writer.append("${testDate},")
                writer.append("${biomarker.biomarkerType},")
                writer.append("${biomarker.value},")
                writer.append("${biomarker.unit},")
                writer.append("${biomarker.referenceRangeMin ?: ""},")
                writer.append("${biomarker.referenceRangeMax ?: ""},")
                writer.append("${biomarker.isWithinRange ?: ""},")
                writer.append("\"${biomarker.labName ?: ""}\",")
                writer.append("\"${biomarker.notes ?: ""}\"\n")
            }
        }

        return file
    }
    
    private suspend fun exportGoalsToCsv(userId: String, dateRange: ExportDateRange?): File {
        val goals = database.goalDao().getAllGoalsForUser(userId)

        val fileName = generateFileName(userId, "csv", ExportType.GOAL_PROGRESS, "goals")
        val file = File(exportDir, fileName)

        FileWriter(file).use { writer ->
            writer.append("Goal Type,Title,Category,Target Value,Current Value,Progress %,Unit,Priority,Is Active,Start Date,Target Date\n")

            goals.forEach { goal ->
                val progressPercentage = if (goal.targetValue > 0) {
                    ((goal.currentValue / goal.targetValue) * 100).toFloat()
                } else 0f
                writer.append("${goal.type},")
                writer.append("\"${goal.title}\",")
                writer.append("${goal.category},")
                writer.append("${goal.targetValue},")
                writer.append("${goal.currentValue},")
                writer.append("${progressPercentage},")
                writer.append("${goal.unit},")
                writer.append("${goal.priority},")
                writer.append("${goal.isActive},")
                writer.append("${goal.startDate},")
                writer.append("${goal.targetDate}\n")
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
            packageInfo.versionName ?: "unknown"
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
        val mealsByDay = meals.groupBy {
            LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
        }
        val dailyCalories = mealsByDay.map { (_, dayMeals) ->
            // Would need to parse JSON nutritionInfo - for now use 0
            0.0
        }

        return NutritionAnalysis(
            averageDailyCalories = if (dailyCalories.isNotEmpty()) dailyCalories.average() else 0.0,
            macronutrientBreakdown = mapOf<String, Double>(
                "Protein" to 0.0,
                "Carbohydrates" to 0.0,
                "Fat" to 0.0
            ),
            micronutrientStatus = emptyMap<String, String>(), // Would need detailed micronutrient tracking
            hydrationAverage = 2.5, // Default value, would need hydration tracking
            mealTimingPatterns = meals.groupBy { it.mealType }.mapValues<String, List<*>, Int> { it.value.size }
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

    private fun generateSupplementAdherence(supplements: List<Supplement>): ExportSupplementAdherence {
        val totalSupplements = supplements.size

        return ExportSupplementAdherence(
            totalSupplements = totalSupplements,
            adherenceRate = 0f, // Would need SupplementIntake data
            missedDoses = 0,
            supplementEffectiveness = supplements.associate { supplement ->
                supplement.name to "Unknown" // Would need SupplementIntake data
            }
        )
    }

    private fun generateBiomarkerTrends(biomarkers: List<BiomarkerEntry>): List<ExportBiomarkerTrend> {
        return biomarkers.groupBy { it.biomarkerType }.map { (type, markers) ->
            val sorted = markers.sortedBy { it.testDate }
            val latest = sorted.lastOrNull()
            val previous = sorted.dropLast(1).lastOrNull()

            ExportBiomarkerTrend(
                biomarkerType = type.name,
                trend = if (latest != null && previous != null) {
                    when {
                        latest.value > previous.value -> "improving"
                        latest.value < previous.value -> "declining"
                        else -> "stable"
                    }
                } else "stable",
                latestValue = latest?.value ?: 0.0,
                previousValue = previous?.value,
                targetRange = "${latest?.referenceRangeMin ?: 0.0} - ${latest?.referenceRangeMax ?: 0.0}"
            )
        }
    }

    private fun generateGoalProgressReport(goals: List<Goal>): List<ExportGoalProgress> {
        return goals.map { goal ->
            ExportGoalProgress(
                goalId = goal.id,
                goalType = goal.type.name,
                targetValue = goal.targetValue,
                currentValue = goal.currentValue,
                progressPercentage = if (goal.targetValue > 0) {
                    ((goal.currentValue / goal.targetValue) * 100).toFloat().coerceIn(0f, 100f)
                } else {
                    0f
                },
                expectedCompletionDate = goal.targetDate.atStartOfDay()
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
            it.targetDate.isBefore(LocalDate.now()) && it.currentValue < it.targetValue && it.isActive
        }
        if (overdueGoals.isNotEmpty()) {
            recommendations.add("${overdueGoals.size} goals are overdue. Consider adjusting timelines or strategies.")
        }

        // Supplement recommendations - would need SupplementIntake data
        if (exportData.supplements.isNotEmpty()) {
            recommendations.add("Continue tracking your supplement intake for better adherence insights.")
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
        // Would need SupplementIntake data to calculate actual compliance
        return 0f
    }

    private fun calculateWorkoutFrequency(healthMetrics: List<HealthMetric>): Int {
        // Simple calculation based on activity metrics
        val recentDays = 7
        val cutoffDate = LocalDateTime.now().minusDays(recentDays.toLong())
        val activityMetrics = healthMetrics.filter {
            it.type == HealthMetricType.CALORIES_BURNED &&
            LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME).isAfter(cutoffDate)
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
    val biomarkers: List<BiomarkerEntry>,
    val goals: List<Goal>,
    val exportMetadata: ExportMetadata? = null
)

data class ExportMetadata(
    val exportId: String,
    val userId: String,
    val exportType: ExportType,
    val exportedAt: LocalDateTime,
    val dateRange: ExportDateRange?,
    val appVersion: String
)