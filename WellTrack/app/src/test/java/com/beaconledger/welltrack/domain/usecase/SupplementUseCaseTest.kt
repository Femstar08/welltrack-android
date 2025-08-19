package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.domain.repository.SupplementRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class SupplementUseCaseTest {

    private lateinit var supplementRepository: SupplementRepository
    private lateinit var supplementUseCase: SupplementUseCase

    @Before
    fun setup() {
        supplementRepository = mockk()
        supplementUseCase = SupplementUseCase(supplementRepository)
    }

    @Test
    fun `getAllSupplements returns flow of supplements`() = runTest {
        // Given
        val supplements = listOf(
            createTestSupplement("1", "Vitamin D3"),
            createTestSupplement("2", "Omega-3")
        )
        every { supplementRepository.getAllSupplements() } returns flowOf(supplements)

        // When
        val result = supplementRepository.getAllSupplements()

        // Then
        result.collect { supplementList ->
            assertEquals(2, supplementList.size)
            assertEquals("Vitamin D3", supplementList[0].name)
            assertEquals("Omega-3", supplementList[1].name)
        }
    }

    @Test
    fun `createSupplement creates supplement with correct data`() = runTest {
        // Given
        val nutrition = SupplementNutrition(vitaminD = 1000.0)
        every { supplementRepository.saveSupplement(any()) } returns Result.success("supplement-id")

        // When
        val result = supplementUseCase.createSupplement(
            name = "Vitamin D3",
            brand = "TestBrand",
            description = "Test supplement",
            servingSize = "1",
            servingUnit = "capsule",
            category = SupplementCategory.VITAMIN,
            nutrition = nutrition
        )

        // Then
        assertTrue(result.isSuccess)
        verify { supplementRepository.saveSupplement(any()) }
    }

    @Test
    fun `addSupplementToUser creates user supplement with schedules`() = runTest {
        // Given
        val schedules = listOf(
            SupplementSchedule("08:00", "Morning"),
            SupplementSchedule("20:00", "Evening")
        )
        every { supplementRepository.addUserSupplement(any()) } returns Result.success("user-supplement-id")

        // When
        val result = supplementUseCase.addSupplementToUser(
            userId = "user-1",
            supplementId = "supplement-1",
            customName = "My Vitamin D",
            dosage = 1000.0,
            dosageUnit = "IU",
            frequency = SupplementFrequency.TWICE_DAILY,
            scheduledTimes = schedules,
            notes = "Take with food"
        )

        // Then
        assertTrue(result.isSuccess)
        verify { supplementRepository.addUserSupplement(any()) }
    }

    @Test
    fun `logSupplementIntake creates intake record`() = runTest {
        // Given
        every { supplementRepository.logSupplementIntake(any()) } returns Result.success("intake-id")

        // When
        val result = supplementUseCase.logSupplementIntake(
            userId = "user-1",
            userSupplementId = "user-supplement-1",
            actualDosage = 1000.0,
            dosageUnit = "IU",
            notes = "Taken with breakfast"
        )

        // Then
        assertTrue(result.isSuccess)
        verify { supplementRepository.logSupplementIntake(any()) }
    }

    @Test
    fun `getTodaySupplementSummary returns correct summary`() = runTest {
        // Given
        val summary = SupplementDailySummary(
            totalScheduled = 5,
            totalTaken = 4,
            totalSkipped = 1,
            totalMissed = 0,
            adherencePercentage = 80f,
            upcomingCount = 2
        )
        coEvery { supplementRepository.getTodaySupplementSummary("user-1") } returns summary

        // When
        val result = supplementUseCase.getTodaySupplementSummary("user-1")

        // Then
        assertEquals(5, result.totalScheduled)
        assertEquals(4, result.totalTaken)
        assertEquals(80f, result.adherencePercentage, 0.01f)
    }

    @Test
    fun `createDailySchedule generates correct schedule times`() {
        // Given
        val times = listOf("08:00", "12:00", "18:00")

        // When
        val schedules = supplementUseCase.createDailySchedule(times)

        // Then
        assertEquals(3, schedules.size)
        assertEquals("08:00", schedules[0].time)
        assertEquals("Morning", schedules[0].label)
        assertEquals("12:00", schedules[1].time)
        assertEquals("Lunch", schedules[1].label)
        assertEquals("18:00", schedules[2].time)
        assertEquals("Dinner", schedules[2].label)
    }

    @Test
    fun `createMealBasedSchedule generates meal-based times`() {
        // When
        val schedules = supplementUseCase.createMealBasedSchedule()

        // Then
        assertEquals(3, schedules.size)
        assertEquals("08:00", schedules[0].time)
        assertEquals("With breakfast", schedules[0].label)
        assertEquals("12:30", schedules[1].time)
        assertEquals("With lunch", schedules[1].label)
        assertEquals("19:00", schedules[2].time)
        assertEquals("With dinner", schedules[2].label)
    }

    @Test
    fun `createWorkoutSchedule generates workout-based times`() {
        // When
        val schedules = supplementUseCase.createWorkoutSchedule()

        // Then
        assertEquals(2, schedules.size)
        assertEquals("07:00", schedules[0].time)
        assertEquals("Pre-workout", schedules[0].label)
        assertEquals("09:00", schedules[1].time)
        assertEquals("Post-workout", schedules[1].label)
    }

    private fun createTestSupplement(id: String, name: String): Supplement {
        return Supplement(
            id = id,
            name = name,
            brand = "TestBrand",
            description = "Test supplement",
            servingSize = "1",
            servingUnit = "capsule",
            nutritionalInfo = "{}",
            barcode = null,
            imageUrl = null,
            category = SupplementCategory.VITAMIN,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
    }

    private fun createTestUserSupplement(id: String, supplementId: String): UserSupplementWithDetails {
        return UserSupplementWithDetails(
            id = id,
            userId = "user-1",
            supplementId = supplementId,
            customName = null,
            dosage = 1000.0,
            dosageUnit = "IU",
            frequency = SupplementFrequency.ONCE_DAILY,
            scheduledTimes = """[{"time":"08:00","label":"Morning"}]""",
            isActive = true,
            notes = null,
            startDate = LocalDate.now().toString(),
            endDate = null,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString(),
            supplementName = "Vitamin D3",
            brand = "TestBrand",
            nutritionalInfo = "{}"
        )
    }
}