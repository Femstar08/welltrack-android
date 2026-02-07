package com.beaconledger.welltrack.data.import

import android.content.Context
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.export.UserExportData
import com.beaconledger.welltrack.data.export.ExportMetadata
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
import java.time.format.DateTimeFormatter
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
                
                val dateRangePair = dateRange?.let {
                    Pair(it.startDate, it.endDate)
                } ?: Pair(null, null)

                Result.success(ImportPreview(
                    fileName = file.name,
                    fileSize = file.length(),
                    dataType = "Full Backup",
                    recordCount = recordCount,
                    dateRange = dateRangePair,
                    conflicts = emptyList(), // TODO: Implement actual conflict detection logic here
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
                        com.beaconledger.welltrack.data.model.ExportDateRange(
                            LocalDateTime.parse(minDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            LocalDateTime.parse(maxDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        )
                    } else null
                } else null
                
                val dateRangePair = dateRange?.let {
                    Pair(it.startDate, it.endDate)
                } ?: Pair(null, null)

                Result.success(ImportPreview(
                    fileName = file.name,
                    fileSize = file.length(),
                    dataType = "Health Metrics",
                    recordCount = healthData.size,
                    dateRange = dateRangePair,
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
                        com.beaconledger.welltrack.data.model.ExportDateRange(
                            LocalDateTime.parse(minDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            LocalDateTime.parse(maxDate, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        )
                    } else null
                } else null
                
                val dateRangePair = dateRange?.let {
                    Pair(it.startDate, it.endDate)
                } ?: Pair(null, null)

                Result.success(ImportPreview(
                    fileName = file.name,
                    fileSize = file.length(),
                    dataType = "Meals",
                    recordCount = mealData.size,
                    dateRange = dateRangePair,
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
                    fileName = file.name,
                    fileSize = file.length(),
                    dataType = "Supplements",
                    recordCount = supplementData.size,
                    dateRange = Pair(null, null),
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
            val jsonReader = com.google.gson.stream.JsonReader(reader)
            jsonReader.beginObject() // Start parsing UserExportData object

            var exportMetadata: ExportMetadata? = null
            var userProfile: UserProfile? = null

            val meals = mutableListOf<Meal>()
            val healthMetrics = mutableListOf<HealthMetric>()
            val supplements = mutableListOf<Supplement>()
            val biomarkers = mutableListOf<BiomarkerEntry>()
            val goals = mutableListOf<Goal>()

            while (jsonReader.hasNext()) {
                when (jsonReader.nextName()) {
                    "exportMetadata" -> exportMetadata = gson.fromJson(jsonReader, ExportMetadata::class.java)
                    "userProfile" -> userProfile = gson.fromJson(jsonReader, UserProfile::class.java)
                    "meals" -> {
                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            meals.add(gson.fromJson(jsonReader, Meal::class.java))
                        }
                        jsonReader.endArray()
                    }
                    "healthMetrics" -> {
                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            healthMetrics.add(gson.fromJson(jsonReader, HealthMetric::class.java))
                        }
                        jsonReader.endArray()
                    }
                    "supplements" -> {
                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            supplements.add(gson.fromJson(jsonReader, Supplement::class.java))
                        }
                        jsonReader.endArray()
                    }
                    "biomarkers" -> {
                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            biomarkers.add(gson.fromJson(jsonReader, BiomarkerEntry::class.java))
                        }
                        jsonReader.endArray()
                    }
                    "goals" -> {
                        jsonReader.beginArray()
                        while (jsonReader.hasNext()) {
                            goals.add(gson.fromJson(jsonReader, Goal::class.java))
                        }
                        jsonReader.endArray()
                    }
                    else -> jsonReader.skipValue()
                }
            }
            jsonReader.endObject() // End parsing UserExportData object

            val exportData = UserExportData(
                exportMetadata = exportMetadata,
                userProfile = userProfile,
                meals = meals,
                healthMetrics = healthMetrics,
                supplements = supplements,
                biomarkers = biomarkers,
                goals = goals
            )
            
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
            // Explicitly clear large lists to free up memory after import
            meals.clear()
            healthMetrics.clear()
            supplements.clear()
            biomarkers.clear()
            goals.clear()
        }
    }
    
    private suspend fun importHealthData(file: File, request: ImportRequest) {
        val healthData = when (file.extension.lowercase()) {
            "json" -> {
                val healthMetrics = mutableListOf<HealthMetric>()
                FileReader(file).use { fileReader ->
                    val jsonReader = com.google.gson.stream.JsonReader(fileReader)
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        healthMetrics.add(gson.fromJson(jsonReader, HealthMetric::class.java))
                    }
                    jsonReader.endArray()
                }
                healthMetrics
            }
            "csv" -> {
                importHealthDataFromCsv(file, request.userId)
            }
            else -> throw Exception("Unsupported file format for health data import")
        }

        when (request.mergeStrategy) {
            MergeStrategy.REPLACE_ALL -> {
                database.healthMetricDao().deleteAllMetricsForUser(request.userId)
                val updatedHealthData = healthData.map { metric ->
                    metric.copy(id = java.util.UUID.randomUUID().toString(), userId = request.userId)
                }
                database.healthMetricDao().insertHealthMetrics(updatedHealthData)
            }
            MergeStrategy.MERGE_NEW_ONLY -> {
                val newMetrics = healthData.filter { metric ->
                    val metricTimestamp = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    database.healthMetricDao().getMetricByTypeAndTimestamp(request.userId, metric.type, metricTimestamp) == null
                }.map { metric ->
                    metric.copy(id = java.util.UUID.randomUUID().toString(), userId = request.userId)
                }
                database.healthMetricDao().insertHealthMetrics(newMetrics)
            }
            MergeStrategy.MERGE_WITH_CONFLICT_RESOLUTION -> {
                val metricsToInsert = mutableListOf<HealthMetric>()
                val metricsToUpdate = mutableListOf<HealthMetric>()

                healthData.forEach { metric ->
                    val metricTimestamp = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val existing = database.healthMetricDao().getMetricByTypeAndTimestamp(
                        request.userId, metric.type, metricTimestamp
                    )
                    if (existing != null) {
                        val sourcePriority = mapOf<DataSource, Int>(
                            DataSource.GARMIN to 3,
                            DataSource.HEALTH_CONNECT to 2,
                            DataSource.SAMSUNG_HEALTH to 2,
                            DataSource.MANUAL_ENTRY to 1
                        )

                        val existingPriority = sourcePriority[existing.source] ?: 0
                        val importedPriority = sourcePriority[metric.source] ?: 0

                        if (importedPriority >= existingPriority) {
                            metricsToUpdate.add(metric.copy(id = existing.id, userId = request.userId))
                        }
                    } else {
                        metricsToInsert.add(metric.copy(id = java.util.UUID.randomUUID().toString(), userId = request.userId))
                    }
                }
                if (metricsToInsert.isNotEmpty()) database.healthMetricDao().insertHealthMetrics(metricsToInsert)
                if (metricsToUpdate.isNotEmpty()) database.healthMetricDao().updateHealthMetrics(metricsToUpdate)
            }
        }
    }

    private suspend fun importMealData(file: File, request: ImportRequest) {
        val mealData = when (file.extension.lowercase()) {
            "json" -> {
                val meals = mutableListOf<Meal>()
                FileReader(file).use { fileReader ->
                    val jsonReader = com.google.gson.stream.JsonReader(fileReader)
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        meals.add(gson.fromJson(jsonReader, Meal::class.java))
                    }
                    jsonReader.endArray()
                }
                meals
            }
            "csv" -> {
                importMealsFromCsv(file, request.userId)
            }
            else -> throw Exception("Unsupported file format for meal data import")
        }

        when (request.mergeStrategy) {
            MergeStrategy.REPLACE_ALL -> {
                database.mealDao().deleteAllMealsForUser(request.userId)
                val updatedMealData = mealData.map { meal ->
                    meal.copy(id = java.util.UUID.randomUUID().toString(), userId = request.userId)
                }
                database.mealDao().insertAllMeals(updatedMealData)
            }
            MergeStrategy.MERGE_NEW_ONLY -> {
                val newMeals = mealData.filter { meal ->
                    val mealTimestamp = LocalDateTime.parse(meal.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    database.mealDao().getMealByTimestamp(request.userId, mealTimestamp) == null
                }.map { meal ->
                    meal.copy(id = java.util.UUID.randomUUID().toString(), userId = request.userId)
                }
                database.mealDao().insertAllMeals(newMeals)
            }
            MergeStrategy.MERGE_WITH_CONFLICT_RESOLUTION -> {
                val mealsToInsert = mutableListOf<Meal>()
                val mealsToUpdate = mutableListOf<Meal>()

                mealData.forEach { meal ->
                    val mealTimestamp = LocalDateTime.parse(meal.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                    val existing = database.mealDao().getMealByTimestamp(
                        request.userId, mealTimestamp
                    )
                    if (existing != null) {
                        val mergedNutrition = when {
                            meal.nutritionInfo.isNotEmpty() && existing.nutritionInfo.isEmpty() -> meal.nutritionInfo
                            meal.nutritionInfo.isEmpty() && existing.nutritionInfo.isNotEmpty() -> existing.nutritionInfo
                            meal.nutritionInfo.isNotEmpty() && existing.nutritionInfo.isNotEmpty() -> {
                                // Prefer imported data for nutrition info
                                meal.nutritionInfo
                            }
                            else -> "{}"
                        }

                        mealsToUpdate.add(meal.copy(
                            id = existing.id,
                            nutritionInfo = mergedNutrition,
                            notes = listOfNotNull(existing.notes, meal.notes).joinToString("; ")
                        ))
                    } else {
                        mealsToInsert.add(meal.copy(id = java.util.UUID.randomUUID().toString(), userId = request.userId))
                    }
                }
                if (mealsToInsert.isNotEmpty()) database.mealDao().insertAllMeals(mealsToInsert)
                if (mealsToUpdate.isNotEmpty()) database.mealDao().updateAllMeals(mealsToUpdate)
            }
        }
    }

    private suspend fun importSupplementData(file: File, request: ImportRequest) {
        val supplementData = when (file.extension.lowercase()) {
            "json" -> {
                val supplements = mutableListOf<Supplement>()
                FileReader(file).use { fileReader ->
                    val jsonReader = com.google.gson.stream.JsonReader(fileReader)
                    jsonReader.beginArray()
                    while (jsonReader.hasNext()) {
                        supplements.add(gson.fromJson(jsonReader, Supplement::class.java))
                    }
                    jsonReader.endArray()
                }
                supplements
            }
            "csv" -> {
                importSupplementsFromCsv(file, request.userId)
            }
            else -> throw Exception("Unsupported file format for supplement data import")
        }

        when (request.mergeStrategy) {
            MergeStrategy.REPLACE_ALL -> {
                database.supplementDao().deleteAllUserSupplementsForUser(request.userId)
            }
            else -> {}
        }

        supplementData.forEach { supplement ->
            val updatedSupplement = supplement.copy(
                id = java.util.UUID.randomUUID().toString()
            )

            when (request.mergeStrategy) {
                MergeStrategy.REPLACE_ALL -> {
                    database.supplementDao().insertSupplement(updatedSupplement)
                }
                MergeStrategy.MERGE_NEW_ONLY -> {
                    val existing = database.supplementDao().getSupplementByNameAndDate(
                        supplement.name, supplement.createdAt
                    )
                    if (existing == null) {
                        database.supplementDao().insertSupplement(updatedSupplement)
                    }
                }
                MergeStrategy.MERGE_WITH_CONFLICT_RESOLUTION -> {
                    val existing = database.supplementDao().getSupplementByNameAndDate(
                        supplement.name, supplement.createdAt
                    )
                    if (existing != null) {
                        // Update existing supplement with merged data
                        val resolvedSupplement = updatedSupplement.copy(
                            id = existing.id
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
        database.supplementDao().deleteAllUserSupplementsForUser(userId)
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
            database.supplementDao().insertSupplement(supplement)
        }

        exportData.biomarkers.forEach { biomarker ->
            database.biomarkerDao().insertBiomarker(biomarker.copy(userId = userId))
        }

        exportData.goals.forEach { goal ->
            database.goalDao().insertGoal(goal.copy(userId = userId))
        }
    }
    
    private suspend fun insertNewData(userId: String, exportData: UserExportData) {
        exportData.meals.forEach { meal ->
            val existing = database.mealDao().getMealByTimestamp(userId, LocalDateTime.parse(meal.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            if (existing == null) {
                database.mealDao().insertMeal(meal.copy(userId = userId))
            }
        }

        exportData.healthMetrics.forEach { metric ->
            val existing = database.healthMetricDao().getMetricByTypeAndTimestamp(userId, metric.type, LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            if (existing == null) {
                database.healthMetricDao().insertHealthMetric(metric.copy(userId = userId))
            }
        }

        exportData.supplements.forEach { supplement ->
            val existing = database.supplementDao().getSupplementByNameAndDate(supplement.name, supplement.createdAt)
            if (existing == null) {
                database.supplementDao().insertSupplement(supplement)
            }
        }

        exportData.biomarkers.forEach { biomarker ->
            val existing = database.biomarkerDao().getBiomarkerByTypeAndDate(userId, biomarker.biomarkerType, biomarker.testDate)
            if (existing == null) {
                database.biomarkerDao().insertBiomarker(biomarker.copy(userId = userId))
            }
        }

        exportData.goals.forEach { goal ->
            val existing = database.goalDao().getGoalById(goal.id)
            if (existing == null) {
                database.goalDao().insertGoal(goal.copy(userId = userId))
            }
        }
    }
    
    private suspend fun mergeWithConflictResolution(userId: String, exportData: UserExportData) {
        // Meals
        exportData.meals.forEach { meal ->
            val existing = database.mealDao().getMealByTimestamp(userId, LocalDateTime.parse(meal.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            if (existing != null) {
                val mergedNutrition = when {
                    meal.nutritionInfo.isNotEmpty() && existing.nutritionInfo.isEmpty() -> meal.nutritionInfo
                    meal.nutritionInfo.isEmpty() && existing.nutritionInfo.isNotEmpty() -> existing.nutritionInfo
                    meal.nutritionInfo.isNotEmpty() && existing.nutritionInfo.isNotEmpty() -> meal.nutritionInfo // Prefer imported
                    else -> "{}"
                }
                val mergedMeal = meal.copy(
                    id = existing.id,
                    userId = userId,
                    nutritionInfo = mergedNutrition,
                    notes = listOfNotNull(existing.notes, meal.notes).joinToString("; ")
                )
                database.mealDao().updateMeal(mergedMeal)
            } else {
                database.mealDao().insertMeal(meal.copy(userId = userId))
            }
        }

        // Health Metrics (reusing existing logic from importHealthData)
        exportData.healthMetrics.forEach { metric ->
            val metricTimestamp = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val existing = database.healthMetricDao().getMetricByTypeAndTimestamp(userId, metric.type, metricTimestamp)
            if (existing != null) {
                val sourcePriority = mapOf(
                    DataSource.GARMIN to 3,
                    DataSource.HEALTH_CONNECT to 2,
                    DataSource.SAMSUNG_HEALTH to 2,
                    DataSource.MANUAL_ENTRY to 1
                )
                val existingPriority = sourcePriority[existing.source] ?: 0
                val importedPriority = sourcePriority[metric.source] ?: 0

                if (importedPriority >= existingPriority) {
                    database.healthMetricDao().updateMetric(metric.copy(id = existing.id, userId = userId))
                }
            } else {
                database.healthMetricDao().insertHealthMetric(metric.copy(userId = userId))
            }
        }

        // Supplements
        exportData.supplements.forEach { supplement ->
            val existing = database.supplementDao().getSupplementByNameAndDate(supplement.name, supplement.createdAt)
            if (existing != null) {
                database.supplementDao().updateSupplement(supplement.copy(id = existing.id))
            } else {
                database.supplementDao().insertSupplement(supplement)
            }
        }

        // Biomarkers
        exportData.biomarkers.forEach { biomarker ->
            val existing = database.biomarkerDao().getBiomarkerByTypeAndDate(userId, biomarker.biomarkerType, biomarker.testDate)
            if (existing != null) {
                database.biomarkerDao().updateBiomarker(biomarker.copy(id = existing.id, userId = userId))
            } else {
                database.biomarkerDao().insertBiomarker(biomarker.copy(userId = userId))
            }
        }

        // Goals
        exportData.goals.forEach { goal ->
            val existing = database.goalDao().getGoalById(goal.id)
            if (existing != null) {
                database.goalDao().updateGoal(goal.copy(userId = userId))
            } else {
                database.goalDao().insertGoal(goal.copy(userId = userId))
            }
        }
    }

    // CSV import helper methods
    private fun importHealthDataFromCsv(file: File, userId: String): List<HealthMetric> {
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
                    userId = userId,
                    type = HealthMetricType.valueOf(valueMap["Metric Type"] ?: "STEPS"),
                    value = valueMap["Value"]?.toDoubleOrNull() ?: 0.0,
                    unit = valueMap["Unit"] ?: "",
                    timestamp = (parseDateTime(valueMap["Date"], valueMap["Time"]) ?: LocalDateTime.now()).toString(),
                    source = DataSource.valueOf(valueMap["Source"] ?: "MANUAL_ENTRY"),
                    metadata = valueMap["Notes"]
                )
            } catch (e: Exception) {
                null // Skip invalid lines
            }
        }
    }

    private fun importMealsFromCsv(file: File, userId: String): List<Meal> {
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
                    userId = userId,
                    recipeId = valueMap["Recipe Name"]?.takeIf { it.isNotBlank() },
                    timestamp = (parseDateTime(valueMap["Date"], valueMap["Time"]) ?: LocalDateTime.now()).toString(),
                    mealType = MealType.valueOf(valueMap["Meal Type"] ?: "LUNCH"),
                    nutritionInfo = gson.toJson(mapOf(
                        "calories" to valueMap["Calories"]?.toDoubleOrNull(),
                        "protein" to valueMap["Protein"]?.toDoubleOrNull(),
                        "carbohydrates" to valueMap["Carbs"]?.toDoubleOrNull(),
                        "fat" to valueMap["Fat"]?.toDoubleOrNull(),
                        "fiber" to valueMap["Fiber"]?.toDoubleOrNull()
                    )),
                    score = MealScore.C, // Default score
                    status = MealStatus.valueOf(valueMap["Status"] ?: "PLANNED"),
                    notes = valueMap["Notes"]?.takeIf { it.isNotBlank() }
                )
            } catch (e: Exception) {
                null // Skip invalid lines
            }
        }
    }

    private fun importSupplementsFromCsv(file: File, userId: String): List<Supplement> {
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
                    name = valueMap["Supplement Name"] ?: "",
                    brand = null,
                    description = valueMap["Notes"]?.takeIf { it.isNotBlank() },
                    servingSize = valueMap["Dosage"] ?: "1",
                    servingUnit = valueMap["Unit"] ?: "mg",
                    nutritionalInfo = "{}",
                    barcode = null,
                    imageUrl = null,
                    category = SupplementCategory.OTHER,
                    createdAt = (parseDateTime(valueMap["Date"], null) ?: LocalDateTime.now()).toString(),
                    updatedAt = (parseDateTime(valueMap["Date"], null) ?: LocalDateTime.now()).toString()
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