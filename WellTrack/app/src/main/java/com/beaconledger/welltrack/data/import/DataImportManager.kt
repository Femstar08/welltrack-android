package com.beaconledger.welltrack.data.import

import android.content.Context
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.domain.repository.ImportConflict
import com.beaconledger.welltrack.domain.repository.ImportPreview
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileReader
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WellTrackDatabase,
    private val gson: Gson
) {
    
    suspend fun validateImportFile(
        filePath: String,
        dataType: ImportDataType
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(Exception("File does not exist"))
            }
            
            when (dataType) {
                ImportDataType.FULL_BACKUP -> validateFullBackup(file)
                ImportDataType.HEALTH_DATA -> validateHealthData(file)
                ImportDataType.MEAL_DATA -> validateMealData(file)
                ImportDataType.SUPPLEMENT_DATA -> validateSupplementData(file)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun previewImportData(
        filePath: String,
        dataType: ImportDataType
    ): Result<ImportPreview> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            when (dataType) {
                ImportDataType.FULL_BACKUP -> previewFullBackup(file)
                ImportDataType.HEALTH_DATA -> previewHealthData(file)
                ImportDataType.MEAL_DATA -> previewMealData(file)
                ImportDataType.SUPPLEMENT_DATA -> previewSupplementData(file)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun importData(request: ImportRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(request.filePath)
            
            when (request.dataType) {
                ImportDataType.FULL_BACKUP -> importFullBackup(file, request)
                ImportDataType.HEALTH_DATA -> importHealthData(file, request)
                ImportDataType.MEAL_DATA -> importMealData(file, request)
                ImportDataType.SUPPLEMENT_DATA -> importSupplementData(file, request)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun validateFullBackup(file: File): Result<Boolean> {
        return try {
            FileReader(file).use { reader ->
                val exportData = gson.fromJson(reader, UserExportData::class.java)
                val isValid = exportData.exportMetadata != null && 
                             exportData.userProfile != null
                Result.success(isValid)
            }
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("Invalid JSON format"))
        }
    }
    
    private fun validateHealthData(file: File): Result<Boolean> {
        return try {
            FileReader(file).use { reader ->
                val healthData = gson.fromJson(reader, Array<HealthMetric>::class.java)
                Result.success(healthData.isNotEmpty())
            }
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("Invalid health data format"))
        }
    }
    
    private fun validateMealData(file: File): Result<Boolean> {
        return try {
            FileReader(file).use { reader ->
                val mealData = gson.fromJson(reader, Array<Meal>::class.java)
                Result.success(mealData.isNotEmpty())
            }
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("Invalid meal data format"))
        }
    }
    
    private fun validateSupplementData(file: File): Result<Boolean> {
        return try {
            FileReader(file).use { reader ->
                val supplementData = gson.fromJson(reader, Array<Supplement>::class.java)
                Result.success(supplementData.isNotEmpty())
            }
        } catch (e: JsonSyntaxException) {
            Result.failure(Exception("Invalid supplement data format"))
        }
    }
    
    private fun previewFullBackup(file: File): Result<ImportPreview> {
        return try {
            FileReader(file).use { reader ->
                val exportData = gson.fromJson(reader, UserExportData::class.java)
                
                val recordCount = exportData.meals.size + 
                                exportData.healthMetrics.size + 
                                exportData.supplements.size + 
                                exportData.biomarkers.size + 
                                exportData.goals.size
                
                val dataTypes = mutableListOf<String>()
                if (exportData.meals.isNotEmpty()) dataTypes.add("Meals")
                if (exportData.healthMetrics.isNotEmpty()) dataTypes.add("Health Metrics")
                if (exportData.supplements.isNotEmpty()) dataTypes.add("Supplements")
                if (exportData.biomarkers.isNotEmpty()) dataTypes.add("Biomarkers")
                if (exportData.goals.isNotEmpty()) dataTypes.add("Goals")
                
                val dateRange = exportData.exportMetadata.dateRange
                
                Result.success(ImportPreview(
                    recordCount = recordCount,
                    dataTypes = dataTypes,
                    dateRange = dateRange,
                    conflicts = emptyList(), // TODO: Implement conflict detection
                    warnings = emptyList()
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun previewHealthData(file: File): Result<ImportPreview> {
        return try {
            FileReader(file).use { reader ->
                val healthData = gson.fromJson(reader, Array<HealthMetric>::class.java)
                
                val dateRange = if (healthData.isNotEmpty()) {
                    val minDate = healthData.minByOrNull { it.timestamp }?.timestamp
                    val maxDate = healthData.maxByOrNull { it.timestamp }?.timestamp
                    if (minDate != null && maxDate != null) {
                        DateRange(minDate, maxDate)
                    } else null
                } else null
                
                Result.success(ImportPreview(
                    recordCount = healthData.size,
                    dataTypes = listOf("Health Metrics"),
                    dateRange = dateRange,
                    conflicts = emptyList(),
                    warnings = emptyList()
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun previewMealData(file: File): Result<ImportPreview> {
        return try {
            FileReader(file).use { reader ->
                val mealData = gson.fromJson(reader, Array<Meal>::class.java)
                
                val dateRange = if (mealData.isNotEmpty()) {
                    val minDate = mealData.minByOrNull { it.timestamp }?.timestamp
                    val maxDate = mealData.maxByOrNull { it.timestamp }?.timestamp
                    if (minDate != null && maxDate != null) {
                        DateRange(minDate, maxDate)
                    } else null
                } else null
                
                Result.success(ImportPreview(
                    recordCount = mealData.size,
                    dataTypes = listOf("Meals"),
                    dateRange = dateRange,
                    conflicts = emptyList(),
                    warnings = emptyList()
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun previewSupplementData(file: File): Result<ImportPreview> {
        return try {
            FileReader(file).use { reader ->
                val supplementData = gson.fromJson(reader, Array<Supplement>::class.java)
                
                Result.success(ImportPreview(
                    recordCount = supplementData.size,
                    dataTypes = listOf("Supplements"),
                    dateRange = null,
                    conflicts = emptyList(),
                    warnings = emptyList()
                ))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun importFullBackup(file: File, request: ImportRequest) {
        FileReader(file).use { reader ->
            val exportData = gson.fromJson(reader, UserExportData::class.java)
            
            when (request.mergeStrategy) {
                MergeStrategy.REPLACE_ALL -> {
                    // Clear existing data for user
                    clearUserData(request.userId)
                    insertAllData(request.userId, exportData)
                }
                MergeStrategy.MERGE_NEW_ONLY -> {
                    insertNewData(request.userId, exportData)
                }
                MergeStrategy.MERGE_WITH_CONFLICT_RESOLUTION -> {
                    mergeWithConflictResolution(request.userId, exportData)
                }
            }
        }
    }
    
    private suspend fun importHealthData(file: File, request: ImportRequest) {
        val healthData = when (file.extension.lowercase()) {
            "json" -> {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, Array<HealthMetric>::class.java).toList()
                }
            }
            "csv" -> {
                importHealthDataFromCsv(file)
            }
            else -> throw Exception("Unsupported file format for health data import")
        }

        when (request.mergeStrategy) {
            MergeStrategy.REPLACE_ALL -> {
                database.healthMetricDao().deleteAllMetricsForUser(request.userId)
            }
            else -> {}
        }

        healthData.forEach { metric ->
            val updatedMetric = metric.copy(
                id = java.util.UUID.randomUUID().toString(),
                userId = request.userId
            )

            when (request.mergeStrategy) {
                MergeStrategy.REPLACE_ALL -> {
                    database.healthMetricDao().insertMetric(updatedMetric)
                }
                MergeStrategy.MERGE_NEW_ONLY -> {
                    val existing = database.healthMetricDao().getMetricByTypeAndTimestamp(
                        request.userId, metric.type, metric.timestamp
                    )
                    if (existing == null) {
                        database.healthMetricDao().insertMetric(updatedMetric)
                    }
                }
                MergeStrategy.MERGE_WITH_CONFLICT_RESOLUTION -> {
                    val existing = database.healthMetricDao().getMetricByTypeAndTimestamp(
                        request.userId, metric.type, metric.timestamp
                    )
                    if (existing != null) {
                        // Resolve conflict by choosing source with higher priority
                        val sourcePriority = mapOf(
                            DataSource.GARMIN_CONNECT to 3,
                            DataSource.HEALTH_CONNECT to 2,
                            DataSource.SAMSUNG_HEALTH to 2,
                            DataSource.MANUAL_ENTRY to 1
                        )

                        val existingPriority = sourcePriority[existing.source] ?: 0
                        val importedPriority = sourcePriority[metric.source] ?: 0

                        if (importedPriority >= existingPriority) {
                            database.healthMetricDao().updateMetric(updatedMetric.copy(id = existing.id))
                        }
                    } else {
                        database.healthMetricDao().insertMetric(updatedMetric)
                    }
                }
            }
        }
    }
    
    private suspend fun importMealData(file: File, request: ImportRequest) {
        val mealData = when (file.extension.lowercase()) {
            "json" -> {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, Array<Meal>::class.java).toList()
                }
            }
            "csv" -> {
                importMealsFromCsv(file)
            }
            else -> throw Exception("Unsupported file format for meal data import")
        }

        when (request.mergeStrategy) {
            MergeStrategy.REPLACE_ALL -> {
                database.mealDao().deleteAllMealsForUser(request.userId)
            }
            else -> {}
        }

        mealData.forEach { meal ->
            val updatedMeal = meal.copy(
                id = java.util.UUID.randomUUID().toString(),
                userId = request.userId
            )

            when (request.mergeStrategy) {
                MergeStrategy.REPLACE_ALL -> {
                    database.mealDao().insertMeal(updatedMeal)
                }
                MergeStrategy.MERGE_NEW_ONLY -> {
                    val existing = database.mealDao().getMealByTimestamp(
                        request.userId, meal.timestamp
                    )
                    if (existing == null) {
                        database.mealDao().insertMeal(updatedMeal)
                    }
                }
                MergeStrategy.MERGE_WITH_CONFLICT_RESOLUTION -> {
                    val existing = database.mealDao().getMealByTimestamp(
                        request.userId, meal.timestamp
                    )
                    if (existing != null) {
                        // Merge nutrition info if one has better data
                        val mergedNutrition = when {
                            meal.nutritionInfo != null && existing.nutritionInfo == null -> meal.nutritionInfo
                            meal.nutritionInfo == null && existing.nutritionInfo != null -> existing.nutritionInfo
                            meal.nutritionInfo != null && existing.nutritionInfo != null -> {
                                // Prefer imported data for nutrition info
                                meal.nutritionInfo
                            }
                            else -> null
                        }

                        val mergedMeal = updatedMeal.copy(
                            id = existing.id,
                            nutritionInfo = mergedNutrition,
                            notes = listOfNotNull(existing.notes, meal.notes).joinToString("; ")
                        )
                        database.mealDao().updateMeal(mergedMeal)
                    } else {
                        database.mealDao().insertMeal(updatedMeal)
                    }
                }
            }
        }
    }
    
    private suspend fun importSupplementData(file: File, request: ImportRequest) {
        val supplementData = when (file.extension.lowercase()) {
            "json" -> {
                FileReader(file).use { reader ->
                    gson.fromJson(reader, Array<Supplement>::class.java).toList()
                }
            }
            "csv" -> {
                importSupplementsFromCsv(file)
            }
            else -> throw Exception("Unsupported file format for supplement data import")
        }

        when (request.mergeStrategy) {
            MergeStrategy.REPLACE_ALL -> {
                database.supplementDao().deleteAllSupplementsForUser(request.userId)
            }
            else -> {}
        }

        supplementData.forEach { supplement ->
            val updatedSupplement = supplement.copy(
                id = java.util.UUID.randomUUID().toString(),
                userId = request.userId
            )

            when (request.mergeStrategy) {
                MergeStrategy.REPLACE_ALL -> {
                    database.supplementDao().insertSupplement(updatedSupplement)
                }
                MergeStrategy.MERGE_NEW_ONLY -> {
                    val existing = database.supplementDao().getSupplementByNameAndDate(
                        request.userId, supplement.name, supplement.createdAt
                    )
                    if (existing == null) {
                        database.supplementDao().insertSupplement(updatedSupplement)
                    }
                }
                MergeStrategy.MERGE_WITH_CONFLICT_RESOLUTION -> {
                    val existing = database.supplementDao().getSupplementByNameAndDate(
                        request.userId, supplement.name, supplement.createdAt
                    )
                    if (existing != null) {
                        // Prefer "taken" status if either record shows taken
                        val resolvedSupplement = updatedSupplement.copy(
                            id = existing.id,
                            isTaken = existing.isTaken || supplement.isTaken
                        )
                        database.supplementDao().updateSupplement(resolvedSupplement)
                    } else {
                        database.supplementDao().insertSupplement(updatedSupplement)
                    }
                }
            }
        }
    }
    
    private suspend fun clearUserData(userId: String) {
        database.mealDao().deleteAllMealsForUser(userId)
        database.healthMetricDao().deleteAllMetricsForUser(userId)
        database.supplementDao().deleteAllSupplementsForUser(userId)
        database.biomarkerDao().deleteAllBiomarkersForUser(userId)
        database.goalDao().deleteAllGoalsForUser(userId)
    }
    
    private suspend fun insertAllData(userId: String, exportData: UserExportData) {
        exportData.meals.forEach { meal ->
            database.mealDao().insertMeal(meal.copy(userId = userId))
        }
        
        exportData.healthMetrics.forEach { metric ->
            database.healthMetricDao().insertHealthMetric(metric.copy(userId = userId))
        }
        
        exportData.supplements.forEach { supplement ->
            database.supplementDao().insertSupplement(supplement.copy(userId = userId))
        }
        
        exportData.biomarkers.forEach { biomarker ->
            database.biomarkerDao().insertBiomarker(biomarker.copy(userId = userId))
        }
        
        exportData.goals.forEach { goal ->
            database.goalDao().insertGoal(goal.copy(userId = userId))
        }
    }
    
    private suspend fun insertNewData(userId: String, exportData: UserExportData) {
        // Implementation for merge new only strategy
        insertAllData(userId, exportData) // Simplified for now
    }
    
    private suspend fun mergeWithConflictResolution(userId: String, exportData: UserExportData) {
        // Implementation for conflict resolution strategy
        insertAllData(userId, exportData) // Simplified for now
    }

    // CSV import helper methods
    private fun importHealthDataFromCsv(file: File): List<HealthMetric> {
        val lines = file.readLines()
        if (lines.isEmpty()) return emptyList()

        val headers = lines.first().split(",")
        val dataLines = lines.drop(1)

        return dataLines.mapNotNull { line ->
            try {
                val values = line.split(",")
                if (values.size != headers.size) return@mapNotNull null

                val valueMap = headers.zip(values).toMap()

                HealthMetric(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = "", // Will be set during import
                    type = HealthMetricType.valueOf(valueMap["Metric Type"] ?: "STEPS"),
                    value = valueMap["Value"]?.toDoubleOrNull() ?: 0.0,
                    unit = valueMap["Unit"] ?: "",
                    timestamp = parseDateTime(valueMap["Date"], valueMap["Time"]) ?: LocalDateTime.now(),
                    source = DataSource.valueOf(valueMap["Source"] ?: "MANUAL_ENTRY"),
                    metadata = valueMap["Notes"]
                )
            } catch (e: Exception) {
                null // Skip invalid lines
            }
        }
    }

    private fun importMealsFromCsv(file: File): List<Meal> {
        val lines = file.readLines()
        if (lines.isEmpty()) return emptyList()

        val headers = lines.first().split(",")
        val dataLines = lines.drop(1)

        return dataLines.mapNotNull { line ->
            try {
                val values = line.split(",")
                if (values.size != headers.size) return@mapNotNull null

                val valueMap = headers.zip(values).toMap()

                Meal(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = "", // Will be set during import
                    mealType = MealType.valueOf(valueMap["Meal Type"] ?: "LUNCH"),
                    recipeName = valueMap["Recipe Name"],
                    timestamp = parseDateTime(valueMap["Date"], valueMap["Time"]) ?: LocalDateTime.now(),
                    nutritionInfo = NutritionInfo(
                        calories = valueMap["Calories"]?.toDoubleOrNull(),
                        protein = valueMap["Protein"]?.toDoubleOrNull(),
                        carbohydrates = valueMap["Carbs"]?.toDoubleOrNull(),
                        fat = valueMap["Fat"]?.toDoubleOrNull(),
                        fiber = valueMap["Fiber"]?.toDoubleOrNull()
                    ),
                    status = MealStatus.valueOf(valueMap["Status"] ?: "PLANNED"),
                    notes = valueMap["Notes"]?.takeIf { it.isNotBlank() }
                )
            } catch (e: Exception) {
                null // Skip invalid lines
            }
        }
    }

    private fun importSupplementsFromCsv(file: File): List<Supplement> {
        val lines = file.readLines()
        if (lines.isEmpty()) return emptyList()

        val headers = lines.first().split(",")
        val dataLines = lines.drop(1)

        return dataLines.mapNotNull { line ->
            try {
                val values = line.split(",")
                if (values.size != headers.size) return@mapNotNull null

                val valueMap = headers.zip(values).toMap()

                Supplement(
                    id = java.util.UUID.randomUUID().toString(),
                    userId = "", // Will be set during import
                    name = valueMap["Supplement Name"] ?: "",
                    dosage = valueMap["Dosage"]?.toDoubleOrNull() ?: 0.0,
                    unit = valueMap["Unit"] ?: "mg",
                    frequency = SupplementFrequency.valueOf(valueMap["Frequency"] ?: "DAILY"),
                    isTaken = valueMap["Taken"]?.toBooleanStrictOrNull() ?: false,
                    notes = valueMap["Notes"]?.takeIf { it.isNotBlank() },
                    createdAt = parseDateTime(valueMap["Date"], null) ?: LocalDateTime.now()
                )
            } catch (e: Exception) {
                null // Skip invalid lines
            }
        }
    }

    // Utility methods
    private fun parseDateTime(dateStr: String?, timeStr: String?): LocalDateTime? {
        return try {
            val dateTimePattern = when {
                timeStr != null -> "$dateStr $timeStr"
                dateStr?.contains("T") == true -> dateStr
                else -> "$dateStr 00:00:00"
            }

            LocalDateTime.parse(dateTimePattern,
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        } catch (e: Exception) {
            try {
                // Try ISO format
                LocalDateTime.parse(dateStr)
            } catch (e2: Exception) {
                null
            }
        }
    }
}