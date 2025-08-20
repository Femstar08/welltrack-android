package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MacronutrientRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacronutrientUseCaseTest {

    private lateinit var macronutrientRepository: MacronutrientRepository
    private lateinit var macronutrientUseCase: MacronutrientUseCase

    private val testUserId = "test_user_id"
    private val testDate = LocalDate.now()

    @Before
    fun setup() {
        macronutrientRepository = mockk()
        macronutrientUseCase = MacronutrientUseCase(macronutrientRepository)
    }

    @Test
    fun `setDailyTargets should create and save macronutrient target`() = runTest {
        // Given
        val calories = 2000
        val protein = 150.0
        val carbs = 250.0
        val fat = 67.0
        val fiber = 25.0
        val water = 2500
        val targetId = "target_id"

        coEvery { 
            macronutrientRepository.setDailyTargets(any(), any(), any()) 
        } returns Result.success(targetId)

        // When
        val result = macronutrientUseCase.setDailyTargets(
            testUserId, testDate, calories, protein, carbs, fat, fiber, water
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(targetId, result.getOrNull())
        coVerify { 
            macronutrientRepository.setDailyTargets(
                testUserId, 
                testDate, 
                match { target ->
                    target.userId == testUserId &&
                    target.date == testDate &&
                    target.caloriesTarget == calories &&
                    target.proteinGrams == protein &&
                    target.carbsGrams == carbs &&
                    target.fatGrams == fat &&
                    target.fiberGrams == fiber &&
                    target.waterMl == water
                }
            ) 
        }
    }

    @Test
    fun `logManualNutrientIntake should create and save nutrient intake`() = runTest {
        // Given
        val calories = 500
        val protein = 30.0
        val carbs = 60.0
        val fat = 15.0
        val fiber = 5.0
        val water = 250
        val intakeId = "intake_id"

        coEvery { 
            macronutrientRepository.logNutrientIntake(any(), any()) 
        } returns Result.success(intakeId)

        // When
        val result = macronutrientUseCase.logManualNutrientIntake(
            testUserId, testDate, calories, protein, carbs, fat, fiber, water
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(intakeId, result.getOrNull())
        coVerify { 
            macronutrientRepository.logNutrientIntake(
                testUserId,
                match { intake ->
                    intake.userId == testUserId &&
                    intake.date == testDate &&
                    intake.calories == calories &&
                    intake.proteinGrams == protein &&
                    intake.carbsGrams == carbs &&
                    intake.fatGrams == fat &&
                    intake.fiberGrams == fiber &&
                    intake.waterMl == water &&
                    intake.source == NutrientSource.MANUAL_ENTRY
                }
            ) 
        }
    }

    @Test
    fun `logWaterIntake should create water-only intake entry`() = runTest {
        // Given
        val waterMl = 250
        val intakeId = "intake_id"

        coEvery { 
            macronutrientRepository.logNutrientIntake(any(), any()) 
        } returns Result.success(intakeId)

        // When
        val result = macronutrientUseCase.logWaterIntake(testUserId, testDate, waterMl)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(intakeId, result.getOrNull())
        coVerify { 
            macronutrientRepository.logNutrientIntake(
                testUserId,
                match { intake ->
                    intake.userId == testUserId &&
                    intake.date == testDate &&
                    intake.calories == 0 &&
                    intake.proteinGrams == 0.0 &&
                    intake.carbsGrams == 0.0 &&
                    intake.fatGrams == 0.0 &&
                    intake.fiberGrams == 0.0 &&
                    intake.waterMl == waterMl &&
                    intake.source == NutrientSource.MANUAL_ENTRY
                }
            ) 
        }
    }

    @Test
    fun `calculateAndSetProteinTarget should calculate protein based on body weight and activity`() = runTest {
        // Given
        val bodyWeight = 70.0
        val activityLevel = ActivityLevel.MODERATELY_ACTIVE
        val goal = FitnessGoal.MUSCLE_GAIN
        val expectedProteinGrams = bodyWeight * 1.6 * 1.2 // 134.4g
        
        val proteinTarget = ProteinTarget(
            userId = testUserId,
            bodyWeightKg = bodyWeight,
            activityLevel = activityLevel,
            goal = goal,
            recommendedGramsPerKg = 1.92,
            totalTargetGrams = expectedProteinGrams
        )

        coEvery { 
            macronutrientRepository.calculateProteinTarget(any(), any(), any(), any()) 
        } returns proteinTarget

        coEvery { 
            macronutrientRepository.getDailyTargets(any(), any()) 
        } returns null

        coEvery { 
            macronutrientRepository.setDailyTargets(any(), any(), any()) 
        } returns Result.success("target_id")

        // When
        val result = macronutrientUseCase.calculateAndSetProteinTarget(
            testUserId, testDate, bodyWeight, activityLevel, goal
        )

        // Then
        assertTrue(result.isSuccess)
        val returnedTarget = result.getOrNull()
        assertEquals(expectedProteinGrams, returnedTarget?.totalTargetGrams)
        assertEquals(activityLevel, returnedTarget?.activityLevel)
        assertEquals(goal, returnedTarget?.goal)
    }

    @Test
    fun `calculateAndSetFiberTarget should calculate fiber based on age and gender`() = runTest {
        // Given
        val age = 30
        val gender = Gender.FEMALE
        val expectedFiberGrams = 25.0 // For female under 50
        
        val fiberTarget = FiberTarget(
            userId = testUserId,
            age = age,
            gender = gender,
            recommendedGrams = expectedFiberGrams
        )

        coEvery { 
            macronutrientRepository.calculateFiberTarget(any(), any(), any()) 
        } returns fiberTarget

        coEvery { 
            macronutrientRepository.getDailyTargets(any(), any()) 
        } returns null

        coEvery { 
            macronutrientRepository.setDailyTargets(any(), any(), any()) 
        } returns Result.success("target_id")

        // When
        val result = macronutrientUseCase.calculateAndSetFiberTarget(
            testUserId, testDate, age, gender
        )

        // Then
        assertTrue(result.isSuccess)
        val returnedTarget = result.getOrNull()
        assertEquals(expectedFiberGrams, returnedTarget?.recommendedGrams)
        assertEquals(age, returnedTarget?.age)
        assertEquals(gender, returnedTarget?.gender)
    }

    @Test
    fun `calculateAndSetWaterTarget should calculate water based on body weight and activity`() = runTest {
        // Given
        val bodyWeight = 70.0
        val activityLevel = ActivityLevel.MODERATELY_ACTIVE
        val expectedWaterMl = (bodyWeight * 35 * 1.2).toInt() // 2940ml
        
        coEvery { 
            macronutrientRepository.calculateWaterTarget(any(), any(), any()) 
        } returns expectedWaterMl

        coEvery { 
            macronutrientRepository.getDailyTargets(any(), any()) 
        } returns null

        coEvery { 
            macronutrientRepository.setDailyTargets(any(), any(), any()) 
        } returns Result.success("target_id")

        // When
        val result = macronutrientUseCase.calculateAndSetWaterTarget(
            testUserId, testDate, bodyWeight, activityLevel
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedWaterMl, result.getOrNull())
    }

    @Test
    fun `addCustomNutrient should create and save custom nutrient`() = runTest {
        // Given
        val name = "Vitamin D"
        val unit = "IU"
        val targetValue = 1000.0
        val category = NutrientCategory.VITAMIN
        val priority = NutrientPriority.IMPORTANT
        val nutrientId = "nutrient_id"

        coEvery { 
            macronutrientRepository.addCustomNutrient(any(), any()) 
        } returns Result.success(nutrientId)

        // When
        val result = macronutrientUseCase.addCustomNutrient(
            testUserId, name, unit, targetValue, category, priority
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(nutrientId, result.getOrNull())
        coVerify { 
            macronutrientRepository.addCustomNutrient(
                testUserId,
                match { nutrient ->
                    nutrient.userId == testUserId &&
                    nutrient.name == name &&
                    nutrient.unit == unit &&
                    nutrient.targetValue == targetValue &&
                    nutrient.category == category &&
                    nutrient.priority == priority &&
                    nutrient.isActive
                }
            ) 
        }
    }

    @Test
    fun `getDailySummary should return flow from repository`() = runTest {
        // Given
        val mockSummary = MacronutrientSummary(
            userId = testUserId,
            date = testDate,
            targets = null,
            totalCalories = 1500,
            totalProtein = 120.0,
            totalCarbs = 180.0,
            totalFat = 50.0,
            totalFiber = 20.0,
            totalWater = 2000,
            customNutrientTotals = emptyMap(),
            caloriesProgress = 0.75f,
            proteinProgress = 0.8f,
            carbsProgress = 0.72f,
            fatProgress = 0.75f,
            fiberProgress = 0.8f,
            waterProgress = 0.8f,
            customNutrientProgress = emptyMap()
        )

        coEvery { 
            macronutrientRepository.getDailySummary(testUserId, testDate) 
        } returns flowOf(mockSummary)

        // When
        val result = macronutrientUseCase.getDailySummary(testUserId, testDate)

        // Then
        result.collect { summary ->
            assertEquals(mockSummary.userId, summary.userId)
            assertEquals(mockSummary.totalCalories, summary.totalCalories)
            assertEquals(mockSummary.totalProtein, summary.totalProtein)
            assertEquals(mockSummary.proteinProgress, summary.proteinProgress)
        }
    }

    @Test
    fun `getProteinTargetRecommendation should calculate correct protein target`() = runTest {
        // Given
        val bodyWeight = 70.0
        val activityLevel = ActivityLevel.VERY_ACTIVE
        val goal = FitnessGoal.STRENGTH
        
        // Expected: 70 * 1.8 * 1.15 = 144.9g
        val expectedProtein = 144.9

        // When
        val result = macronutrientUseCase.getProteinTargetRecommendation(
            bodyWeight, activityLevel, goal
        )

        // Then
        assertEquals(expectedProtein, result, 0.1)
    }

    @Test
    fun `getFiberTargetRecommendation should return correct fiber target for demographics`() = runTest {
        // Test cases for different demographics
        val testCases = listOf(
            Triple(25, Gender.MALE, 38.0),      // Young male
            Triple(25, Gender.FEMALE, 25.0),    // Young female
            Triple(55, Gender.MALE, 30.0),      // Older male
            Triple(55, Gender.FEMALE, 21.0),    // Older female
            Triple(30, Gender.OTHER, 25.0)      // Other gender
        )

        testCases.forEach { (age, gender, expectedFiber) ->
            // When
            val result = macronutrientUseCase.getFiberTargetRecommendation(age, gender)

            // Then
            assertEquals(expectedFiber, result, "Failed for age $age, gender $gender")
        }
    }

    @Test
    fun `getWaterTargetRecommendation should calculate correct water target`() = runTest {
        // Given
        val bodyWeight = 70.0
        val activityLevel = ActivityLevel.MODERATELY_ACTIVE
        
        // Expected: 70 * 35 * 1.2 = 2940ml
        val expectedWater = 2940

        // When
        val result = macronutrientUseCase.getWaterTargetRecommendation(
            bodyWeight, activityLevel
        )

        // Then
        assertEquals(expectedWater, result)
    }
}