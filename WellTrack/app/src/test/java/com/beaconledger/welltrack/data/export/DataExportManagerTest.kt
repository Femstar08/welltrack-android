package com.beaconledger.welltrack.data.export

import android.content.Context
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.model.*
import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.io.File
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataExportManagerTest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var database: WellTrackDatabase

    @Mock
    private lateinit var userDao: UserDao

    @Mock
    private lateinit var mealDao: MealDao

    @Mock
    private lateinit var healthMetricDao: HealthMetricDao

    @Mock
    private lateinit var supplementDao: SupplementDao

    @Mock
    private lateinit var biomarkerDao: BiomarkerDao

    @Mock
    private lateinit var goalDao: GoalDao

    private lateinit var gson: Gson
    private lateinit var dataExportManager: DataExportManager

    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        gson = Gson()

        // Setup database DAO mocks
        whenever(database.userDao()).thenReturn(userDao)
        whenever(database.mealDao()).thenReturn(mealDao)
        whenever(database.healthMetricDao()).thenReturn(healthMetricDao)
        whenever(database.supplementDao()).thenReturn(supplementDao)
        whenever(database.biomarkerDao()).thenReturn(biomarkerDao)
        whenever(database.goalDao()).thenReturn(goalDao)

        dataExportManager = DataExportManager(context, database, gson)
    }

    @Test
    fun `exportUserDataToJson should create valid JSON export`() = runTest {
        // Given
        val exportRequest = createTestExportRequest()
        val testUser = createTestUser()
        val testMeals = listOf(createTestMeal())
        val testHealthMetrics = listOf(createTestHealthMetric())
        val testSupplements = listOf(createTestSupplement())
        val testBiomarkers = listOf(createTestBiomarker())
        val testGoals = listOf(createTestGoal())

        // Setup temp directory
        val tempDir = createTempDir()
        whenever(context.getExternalFilesDir(null)).thenReturn(tempDir)

        // Setup DAO responses
        whenever(userDao.getUserById(testUserId)).thenReturn(testUser)
        whenever(mealDao.getAllMealsForUser(testUserId)).thenReturn(testMeals)
        whenever(healthMetricDao.getAllMetricsForUser(testUserId)).thenReturn(testHealthMetrics)
        whenever(supplementDao.getAllSupplementsForUser(testUserId)).thenReturn(testSupplements)
        whenever(biomarkerDao.getAllBiomarkersForUser(testUserId)).thenReturn(testBiomarkers)
        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(testGoals)

        // When
        val result = dataExportManager.exportUserDataToJson(testUserId, exportRequest)

        // Then
        assertTrue(result.isSuccess)
        val exportFile = result.getOrNull()!!
        assertTrue(exportFile.exists())
        assertTrue(exportFile.name.contains("welltrack"))
        assertTrue(exportFile.name.endsWith(".json"))

        // Clean up
        exportFile.delete()
        tempDir.deleteRecursively()
    }

    @Test
    fun `exportUserDataToCsv should create CSV files for each data type`() = runTest {
        // Given
        val exportRequest = createTestExportRequest(
            includeMealData = true,
            includeHealthData = true,
            includeSupplementData = false,
            includeBiomarkerData = false,
            includeGoalData = true
        )

        val tempDir = createTempDir()
        whenever(context.getExternalFilesDir(null)).thenReturn(tempDir)

        // Setup DAO responses
        whenever(mealDao.getAllMealsForUser(testUserId)).thenReturn(listOf(createTestMeal()))
        whenever(healthMetricDao.getAllMetricsForUser(testUserId)).thenReturn(listOf(createTestHealthMetric()))
        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(listOf(createTestGoal()))

        // When
        val result = dataExportManager.exportUserDataToCsv(testUserId, exportRequest)

        // Then
        assertTrue(result.isSuccess)
        val exportFiles = result.getOrNull()!!
        assertEquals(3, exportFiles.size) // meals, health, goals

        exportFiles.forEach { file ->
            assertTrue(file.exists())
            assertTrue(file.name.endsWith(".csv"))
        }

        // Clean up
        exportFiles.forEach { it.delete() }
        tempDir.deleteRecursively()
    }

    @Test
    fun `generateHealthReport should create comprehensive report`() = runTest {
        // Given
        val exportRequest = createTestExportRequest()
        val testUser = createTestUser()
        val testMeals = listOf(
            createTestMeal(calories = 500.0),
            createTestMeal(calories = 600.0),
            createTestMeal(calories = 400.0)
        )
        val testHealthMetrics = listOf(
            createTestHealthMetric(type = HealthMetricType.STEPS, value = 10000.0),
            createTestHealthMetric(type = HealthMetricType.HEART_RATE, value = 75.0)
        )
        val testSupplements = listOf(
            createTestSupplement(isTaken = true),
            createTestSupplement(isTaken = false)
        )
        val testGoals = listOf(
            createTestGoal(currentValue = 75.0, targetValue = 100.0),
            createTestGoal(currentValue = 100.0, targetValue = 100.0)
        )

        // Setup DAO responses
        whenever(userDao.getUserById(testUserId)).thenReturn(testUser)
        whenever(mealDao.getAllMealsForUser(testUserId)).thenReturn(testMeals)
        whenever(healthMetricDao.getAllMetricsForUser(testUserId)).thenReturn(testHealthMetrics)
        whenever(supplementDao.getAllSupplementsForUser(testUserId)).thenReturn(testSupplements)
        whenever(biomarkerDao.getAllBiomarkersForUser(testUserId)).thenReturn(emptyList())
        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(testGoals)

        // When
        val result = dataExportManager.generateHealthReport(testUserId, exportRequest)

        // Then
        assertTrue(result.isSuccess)
        val healthReport = result.getOrNull()!!

        assertEquals(testUserId, healthReport.userId)
        assertEquals(3, healthReport.summary.totalMealsLogged)
        assertEquals(2, healthReport.summary.activeGoals)
        assertEquals(1, healthReport.summary.completedGoals)
        assertEquals(0.5f, healthReport.supplementAdherence.adherenceRate)
        assertEquals(2, healthReport.supplementAdherence.totalSupplements)
        assertEquals(1, healthReport.supplementAdherence.missedDoses)
    }

    @Test
    fun `generateHealthReport should handle empty data gracefully`() = runTest {
        // Given
        val exportRequest = createTestExportRequest()

        // Setup DAO responses with empty data
        whenever(userDao.getUserById(testUserId)).thenReturn(null)
        whenever(mealDao.getAllMealsForUser(testUserId)).thenReturn(emptyList())
        whenever(healthMetricDao.getAllMetricsForUser(testUserId)).thenReturn(emptyList())
        whenever(supplementDao.getAllSupplementsForUser(testUserId)).thenReturn(emptyList())
        whenever(biomarkerDao.getAllBiomarkersForUser(testUserId)).thenReturn(emptyList())
        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(emptyList())

        // When
        val result = dataExportManager.generateHealthReport(testUserId, exportRequest)

        // Then
        assertTrue(result.isSuccess)
        val healthReport = result.getOrNull()!!

        assertEquals(0, healthReport.summary.totalMealsLogged)
        assertEquals(0, healthReport.summary.activeGoals)
        assertEquals(0, healthReport.summary.completedGoals)
        assertTrue(healthReport.recommendations.isNotEmpty())
    }

    @Test
    fun `generateNutritionAnalysis should calculate averages correctly`() = runTest {
        // Given
        val meals = listOf(
            createTestMeal(calories = 500.0, protein = 25.0, carbs = 60.0, fat = 20.0),
            createTestMeal(calories = 600.0, protein = 30.0, carbs = 70.0, fat = 25.0),
            createTestMeal(calories = 400.0, protein = 20.0, carbs = 50.0, fat = 15.0)
        )

        val exportRequest = createTestExportRequest()

        // Setup minimum required mocks
        whenever(userDao.getUserById(testUserId)).thenReturn(createTestUser())
        whenever(mealDao.getAllMealsForUser(testUserId)).thenReturn(meals)
        whenever(healthMetricDao.getAllMetricsForUser(testUserId)).thenReturn(emptyList())
        whenever(supplementDao.getAllSupplementsForUser(testUserId)).thenReturn(emptyList())
        whenever(biomarkerDao.getAllBiomarkersForUser(testUserId)).thenReturn(emptyList())
        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(emptyList())

        // When
        val result = dataExportManager.generateHealthReport(testUserId, exportRequest)

        // Then
        assertTrue(result.isSuccess)
        val healthReport = result.getOrNull()!!
        val nutrition = healthReport.nutritionAnalysis

        assertEquals(500.0, nutrition.averageDailyCalories, 0.1)
        assertEquals(25.0, nutrition.macronutrientBreakdown["Protein"], 0.1)
        assertEquals(60.0, nutrition.macronutrientBreakdown["Carbohydrates"], 0.1)
        assertEquals(20.0, nutrition.macronutrientBreakdown["Fat"], 0.1)
    }

    @Test
    fun `exportUserDataToJson should handle database errors gracefully`() = runTest {
        // Given
        val exportRequest = createTestExportRequest()
        val tempDir = createTempDir()
        whenever(context.getExternalFilesDir(null)).thenReturn(tempDir)

        // Setup DAO to throw exception
        whenever(userDao.getUserById(testUserId)).thenThrow(RuntimeException("Database error"))

        // When
        val result = dataExportManager.exportUserDataToJson(testUserId, exportRequest)

        // Then
        assertTrue(result.isFailure)

        // Clean up
        tempDir.deleteRecursively()
    }

    // Helper methods to create test data
    private fun createTestExportRequest(
        includeMealData: Boolean = true,
        includeHealthData: Boolean = true,
        includeSupplementData: Boolean = true,
        includeBiomarkerData: Boolean = true,
        includeGoalData: Boolean = true
    ) = ExportRequest(
        userId = testUserId,
        exportType = ExportType.FULL_BACKUP,
        format = ExportFormat.JSON,
        dateRange = DateRange(
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now()
        ),
        includeHealthData = includeHealthData,
        includeMealData = includeMealData,
        includeSupplementData = includeSupplementData,
        includeBiomarkerData = includeBiomarkerData,
        includeGoalData = includeGoalData
    )

    private fun createTestUser() = User(
        id = testUserId,
        email = "test@example.com",
        displayName = "Test User",
        isActive = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private fun createTestMeal(
        calories: Double = 500.0,
        protein: Double = 25.0,
        carbs: Double = 60.0,
        fat: Double = 20.0
    ) = Meal(
        id = "meal_${System.currentTimeMillis()}",
        userId = testUserId,
        mealType = MealType.LUNCH,
        recipeName = "Test Meal",
        timestamp = LocalDateTime.now(),
        nutritionInfo = NutritionInfo(
            calories = calories,
            protein = protein,
            carbohydrates = carbs,
            fat = fat
        ),
        status = MealStatus.COMPLETED,
        notes = null
    )

    private fun createTestHealthMetric(
        type: HealthMetricType = HealthMetricType.STEPS,
        value: Double = 10000.0
    ) = HealthMetric(
        id = "metric_${System.currentTimeMillis()}",
        userId = testUserId,
        type = type,
        value = value,
        unit = "steps",
        timestamp = LocalDateTime.now(),
        source = DataSource.HEALTH_CONNECT,
        metadata = null
    )

    private fun createTestSupplement(
        isTaken: Boolean = true
    ) = Supplement(
        id = "supplement_${System.currentTimeMillis()}",
        userId = testUserId,
        name = "Test Supplement",
        dosage = 100.0,
        unit = "mg",
        frequency = SupplementFrequency.DAILY,
        isTaken = isTaken,
        notes = null,
        createdAt = LocalDateTime.now()
    )

    private fun createTestBiomarker() = Biomarker(
        id = "biomarker_${System.currentTimeMillis()}",
        userId = testUserId,
        type = BiomarkerType.CHOLESTEROL_TOTAL,
        value = 180.0,
        unit = "mg/dL",
        testDate = LocalDateTime.now(),
        referenceRange = "150-200",
        status = BiomarkerStatus.NORMAL,
        notes = null
    )

    private fun createTestGoal(
        currentValue: Double = 50.0,
        targetValue: Double = 100.0
    ) = Goal(
        id = "goal_${System.currentTimeMillis()}",
        userId = testUserId,
        type = GoalType.WEIGHT_LOSS,
        title = "Test Goal",
        description = "Test goal description",
        targetValue = targetValue,
        currentValue = currentValue,
        unit = "kg",
        startDate = java.time.LocalDate.now(),
        targetDate = java.time.LocalDate.now().plusDays(30),
        isActive = true,
        priority = GoalPriority.MEDIUM,
        category = GoalCategory.WEIGHT,
        milestones = emptyList(),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}