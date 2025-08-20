package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DailyTrackingRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DailyTrackingUseCaseTest {

    private val mockRepository = mockk<DailyTrackingRepository>()
    private lateinit var useCase: DailyTrackingUseCase

    private val testUserId = "test-user-id"
    private val testDate = LocalDate.now()

    @Before
    fun setup() {
        useCase = DailyTrackingUseCase(mockRepository)
    }

    @Test
    fun `saveMorningTracking should call repository with correct parameters`() = runTest {
        // Given
        val morningData = MorningTrackingData(
            waterIntakeMl = 500,
            energyLevel = 7,
            sleepQuality = 8,
            mood = "Good",
            weight = 70.5,
            meal1Logged = true
        )
        coEvery { mockRepository.saveMorningTracking(testUserId, testDate, morningData) } returns Result.success("test-id")

        // When
        val result = useCase.saveMorningTracking(testUserId, testDate, morningData)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockRepository.saveMorningTracking(testUserId, testDate, morningData) }
    }

    @Test
    fun `savePreWorkoutTracking should call repository with correct parameters`() = runTest {
        // Given
        val preWorkoutData = PreWorkoutTrackingData(
            energyLevel = 8,
            hydrationMl = 300,
            workoutType = "Strength Training",
            plannedDuration = 60,
            snackConsumed = "Banana"
        )
        coEvery { mockRepository.savePreWorkoutTracking(testUserId, testDate, preWorkoutData) } returns Result.success("test-id")

        // When
        val result = useCase.savePreWorkoutTracking(testUserId, testDate, preWorkoutData)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockRepository.savePreWorkoutTracking(testUserId, testDate, preWorkoutData) }
    }

    @Test
    fun `savePostWorkoutTracking should call repository with correct parameters`() = runTest {
        // Given
        val postWorkoutData = PostWorkoutTrackingData(
            recoveryMealLogged = true,
            mood = 9,
            performanceRating = 8,
            fatigue = 4,
            hydrationMl = 500,
            workoutNotes = "Great session"
        )
        coEvery { mockRepository.savePostWorkoutTracking(testUserId, testDate, postWorkoutData) } returns Result.success("test-id")

        // When
        val result = useCase.savePostWorkoutTracking(testUserId, testDate, postWorkoutData)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockRepository.savePostWorkoutTracking(testUserId, testDate, postWorkoutData) }
    }

    @Test
    fun `saveBedtimeTracking should call repository with correct parameters`() = runTest {
        // Given
        val bedtimeData = BedtimeTrackingData(
            dinnerLogged = true,
            bedtimeReadiness = 7,
            stressLevel = 3,
            screenTimeHours = 2.5,
            relaxationActivity = "Reading"
        )
        coEvery { mockRepository.saveBedtimeTracking(testUserId, testDate, bedtimeData) } returns Result.success("test-id")

        // When
        val result = useCase.saveBedtimeTracking(testUserId, testDate, bedtimeData)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockRepository.saveBedtimeTracking(testUserId, testDate, bedtimeData) }
    }

    @Test
    fun `addWaterIntake should call repository with correct parameters`() = runTest {
        // Given
        val amountMl = 250
        val source = "Water"
        coEvery { mockRepository.addWaterEntry(testUserId, testDate, amountMl, source) } returns Result.success("test-id")

        // When
        val result = useCase.addWaterIntake(testUserId, testDate, amountMl, source)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockRepository.addWaterEntry(testUserId, testDate, amountMl, source) }
    }

    @Test
    fun `getDailyTrackingSummary should return flow from repository`() = runTest {
        // Given
        val expectedSummary = DailyTrackingSummary(
            userId = testUserId,
            date = testDate,
            morningCompleted = true,
            preWorkoutCompleted = false,
            postWorkoutCompleted = false,
            bedtimeCompleted = true,
            waterIntakeProgress = 0.8f,
            totalWaterMl = 2000,
            energyLevelAverage = 7.5f,
            completionPercentage = 0.5f
        )
        every { mockRepository.getDailyTrackingSummary(testUserId, testDate) } returns flowOf(expectedSummary)

        // When
        val result = useCase.getDailyTrackingSummary(testUserId, testDate)

        // Then
        result.collect { summary ->
            assertEquals(expectedSummary, summary)
        }
        verify { mockRepository.getDailyTrackingSummary(testUserId, testDate) }
    }

    @Test
    fun `getMorningTracking should return data from repository`() = runTest {
        // Given
        val expectedData = MorningTrackingData(
            waterIntakeMl = 500,
            energyLevel = 7,
            sleepQuality = 8
        )
        coEvery { mockRepository.getMorningTracking(testUserId, testDate) } returns expectedData

        // When
        val result = useCase.getMorningTracking(testUserId, testDate)

        // Then
        assertEquals(expectedData, result)
        coVerify { mockRepository.getMorningTracking(testUserId, testDate) }
    }

    @Test
    fun `getRecommendedWaterIntake should calculate based on weight`() = runTest {
        // Test cases for different weights
        val testCases = listOf(
            null to 2500, // Default when no weight
            45.0 to 2000, // Minimum 2L for low weight
            70.0 to 2450, // 70kg * 35ml = 2450ml
            120.0 to 4000 // Maximum 4L for high weight
        )

        testCases.forEach { (weight, expected) ->
            val result = useCase.getRecommendedWaterIntake(weight)
            assertEquals(expected, result, "Failed for weight: $weight")
        }
    }

    @Test
    fun `calculateMacronutrientProgress should return progress when data available`() = runTest {
        // Given
        val targets = MacronutrientTargets(
            caloriesTarget = 2000,
            proteinGrams = 150.0,
            carbsGrams = 250.0,
            fatGrams = 67.0,
            fiberGrams = 25.0
        )
        val actual = MacronutrientActual(
            caloriesActual = 1800,
            proteinGrams = 135.0,
            carbsGrams = 200.0,
            fatGrams = 60.0,
            fiberGrams = 20.0
        )
        val bedtimeData = BedtimeTrackingData(
            macronutrientsTarget = targets,
            macronutrientsActual = actual
        )
        coEvery { mockRepository.getBedtimeTracking(testUserId, testDate) } returns bedtimeData

        // When
        val result = useCase.calculateMacronutrientProgress(testUserId, testDate)

        // Then
        assertNotNull(result)
        assertEquals(0.9f, result.caloriesProgress) // 1800/2000
        assertEquals(0.9f, result.proteinProgress) // 135/150
        assertEquals(0.8f, result.carbsProgress) // 200/250
        assertEquals(0.896f, result.fatProgress, 0.01f) // 60/67
        assertEquals(0.8f, result.fiberProgress) // 20/25
    }

    @Test
    fun `calculateMacronutrientProgress should return null when no data available`() = runTest {
        // Given
        coEvery { mockRepository.getBedtimeTracking(testUserId, testDate) } returns null

        // When
        val result = useCase.calculateMacronutrientProgress(testUserId, testDate)

        // Then
        assertEquals(null, result)
    }

    @Test
    fun `isTrackingCompleted should return repository result`() = runTest {
        // Given
        val trackingType = DailyTrackingType.MORNING_ROUTINE
        coEvery { mockRepository.isTrackingCompleted(testUserId, testDate, trackingType) } returns true

        // When
        val result = useCase.isTrackingCompleted(testUserId, testDate, trackingType)

        // Then
        assertTrue(result)
        coVerify { mockRepository.isTrackingCompleted(testUserId, testDate, trackingType) }
    }

    @Test
    fun `markTrackingCompleted should call repository`() = runTest {
        // Given
        val trackingType = DailyTrackingType.BEDTIME_ROUTINE
        coEvery { mockRepository.markTrackingCompleted(testUserId, testDate, trackingType) } returns Result.success(Unit)

        // When
        val result = useCase.markTrackingCompleted(testUserId, testDate, trackingType)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockRepository.markTrackingCompleted(testUserId, testDate, trackingType) }
    }
}