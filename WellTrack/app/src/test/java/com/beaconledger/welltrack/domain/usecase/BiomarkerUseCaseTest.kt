package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.BiomarkerRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class BiomarkerUseCaseTest {
    
    private lateinit var biomarkerRepository: BiomarkerRepository
    private lateinit var biomarkerUseCase: BiomarkerUseCase
    
    private val testUserId = "test-user-123"
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    @Before
    fun setup() {
        biomarkerRepository = mockk()
        biomarkerUseCase = BiomarkerUseCase(biomarkerRepository)
    }
    
    @Test
    fun `createBloodTestReminder should create reminder with correct data`() = runTest {
        // Given
        val testType = BloodTestType.HORMONAL_PANEL
        val reminderName = "Quarterly Hormone Check"
        val description = "Check testosterone and cortisol levels"
        val frequency = ReminderFrequency.QUARTERLY
        val firstDueDate = LocalDate.now().plusMonths(3)
        
        every { biomarkerRepository.createReminder(any()) } returns Result.success("reminder-id")
        
        // When
        val result = biomarkerUseCase.createBloodTestReminder(
            userId = testUserId,
            testType = testType,
            reminderName = reminderName,
            description = description,
            frequency = frequency,
            firstDueDate = firstDueDate
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("reminder-id", result.getOrNull())
        
        verify {
            biomarkerRepository.createReminder(
                match { reminder ->
                    reminder.userId == testUserId &&
                    reminder.testType == testType &&
                    reminder.reminderName == reminderName &&
                    reminder.description == description &&
                    reminder.frequency == frequency &&
                    reminder.nextDueDate == firstDueDate.format(dateFormatter) &&
                    reminder.isActive &&
                    reminder.canSkip &&
                    reminder.skipCount == 0 &&
                    reminder.maxSkips == 3
                }
            )
        }
    }
    
    @Test
    fun `createBloodTestReminder should use default due date when none provided`() = runTest {
        // Given
        val testType = BloodTestType.MICRONUTRIENT_PANEL
        val reminderName = "Vitamin Check"
        val frequency = ReminderFrequency.SEMI_ANNUALLY
        
        every { biomarkerRepository.createReminder(any()) } returns Result.success("reminder-id")
        
        // When
        val result = biomarkerUseCase.createBloodTestReminder(
            userId = testUserId,
            testType = testType,
            reminderName = reminderName,
            frequency = frequency
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        verify {
            biomarkerRepository.createReminder(
                match { reminder ->
                    // Should have a due date 6 months from now (semi-annually)
                    val expectedDate = LocalDate.now().plusMonths(6).format(dateFormatter)
                    reminder.nextDueDate == expectedDate
                }
            )
        }
    }
    
    @Test
    fun `saveBiomarkerEntry should create entry with correct data`() = runTest {
        // Given
        val testType = BloodTestType.HORMONAL_PANEL
        val biomarkerType = BiomarkerType.TESTOSTERONE
        val value = 650.0
        val unit = "ng/dL"
        val testDate = LocalDate.now().minusDays(1)
        val notes = "Morning test, fasted"
        val labName = "LabCorp"
        val refMin = 300.0
        val refMax = 1000.0
        
        every { biomarkerRepository.saveBiomarkerEntry(any()) } returns Result.success("entry-id")
        
        // When
        val result = biomarkerUseCase.saveBiomarkerEntry(
            userId = testUserId,
            testType = testType,
            biomarkerType = biomarkerType,
            value = value,
            unit = unit,
            testDate = testDate,
            notes = notes,
            labName = labName,
            referenceRangeMin = refMin,
            referenceRangeMax = refMax
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("entry-id", result.getOrNull())
        
        verify {
            biomarkerRepository.saveBiomarkerEntry(
                match { entry ->
                    entry.userId == testUserId &&
                    entry.testType == testType &&
                    entry.biomarkerType == biomarkerType &&
                    entry.value == value &&
                    entry.unit == unit &&
                    entry.testDate == testDate.format(dateFormatter) &&
                    entry.notes == notes &&
                    entry.labName == labName &&
                    entry.referenceRangeMin == refMin &&
                    entry.referenceRangeMax == refMax
                }
            )
        }
    }
    
    @Test
    fun `saveBulkBiomarkerEntries should create session and entries`() = runTest {
        // Given
        val testType = BloodTestType.MICRONUTRIENT_PANEL
        val testDate = LocalDate.now()
        val entries = listOf(
            BiomarkerEntryInput(
                biomarkerType = BiomarkerType.VITAMIN_D3,
                value = 45.0,
                unit = "ng/mL"
            ),
            BiomarkerEntryInput(
                biomarkerType = BiomarkerType.VITAMIN_B12,
                value = 550.0,
                unit = "pg/mL"
            )
        )
        val labName = "Quest Diagnostics"
        val sessionNotes = "Annual vitamin panel"
        
        every { biomarkerRepository.createTestSession(any()) } returns Result.success("session-id")
        every { biomarkerRepository.saveBiomarkerEntries(any()) } returns Result.success(Unit)
        
        // When
        val result = biomarkerUseCase.saveBulkBiomarkerEntries(
            userId = testUserId,
            testType = testType,
            testDate = testDate,
            entries = entries,
            labName = labName,
            sessionNotes = sessionNotes
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        verify {
            biomarkerRepository.createTestSession(
                match { session ->
                    session.userId == testUserId &&
                    session.testDate == testDate.format(dateFormatter) &&
                    session.labName == labName &&
                    session.notes == sessionNotes &&
                    session.totalMarkers == 2 &&
                    session.enteredMarkers == 2 &&
                    session.isComplete
                }
            )
        }
        
        verify {
            biomarkerRepository.saveBiomarkerEntries(
                match { biomarkerEntries ->
                    biomarkerEntries.size == 2 &&
                    biomarkerEntries.all { it.userId == testUserId } &&
                    biomarkerEntries.all { it.testType == testType } &&
                    biomarkerEntries.all { it.testDate == testDate.format(dateFormatter) } &&
                    biomarkerEntries.all { it.labName == labName }
                }
            )
        }
    }
    
    @Test
    fun `getDefaultReminderFrequency should return correct frequency for test type`() {
        // Test various test types
        assertEquals(ReminderFrequency.QUARTERLY, biomarkerUseCase.getDefaultReminderFrequency(BloodTestType.HORMONAL_PANEL))
        assertEquals(ReminderFrequency.SEMI_ANNUALLY, biomarkerUseCase.getDefaultReminderFrequency(BloodTestType.MICRONUTRIENT_PANEL))
        assertEquals(ReminderFrequency.ANNUALLY, biomarkerUseCase.getDefaultReminderFrequency(BloodTestType.GENERAL_HEALTH_PANEL))
        assertEquals(ReminderFrequency.ANNUALLY, biomarkerUseCase.getDefaultReminderFrequency(BloodTestType.LIPID_PANEL))
        assertEquals(ReminderFrequency.SEMI_ANNUALLY, biomarkerUseCase.getDefaultReminderFrequency(BloodTestType.THYROID_PANEL))
        assertEquals(ReminderFrequency.ANNUALLY, biomarkerUseCase.getDefaultReminderFrequency(BloodTestType.CUSTOM_TEST))
    }
    
    @Test
    fun `validateBiomarkerValue should return valid for normal values`() {
        // Given
        every { biomarkerRepository.getBiomarkerReference(BiomarkerType.TESTOSTERONE) } returns BiomarkerReference(
            biomarkerType = BiomarkerType.TESTOSTERONE,
            unit = "ng/dL",
            normalRangeMin = 300.0,
            normalRangeMax = 1000.0,
            description = "Total testosterone"
        )
        
        // When
        val result = biomarkerUseCase.validateBiomarkerValue(BiomarkerType.TESTOSTERONE, 650.0, "ng/dL")
        
        // Then
        assertTrue(result is ValidationResult.Valid)
    }
    
    @Test
    fun `validateBiomarkerValue should return invalid for negative values`() {
        // When
        val result = biomarkerUseCase.validateBiomarkerValue(BiomarkerType.TESTOSTERONE, -100.0, "ng/dL")
        
        // Then
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Value cannot be negative", (result as ValidationResult.Invalid).message)
    }
    
    @Test
    fun `validateBiomarkerValue should return invalid for wrong unit`() {
        // Given
        every { biomarkerRepository.getBiomarkerReference(BiomarkerType.TESTOSTERONE) } returns BiomarkerReference(
            biomarkerType = BiomarkerType.TESTOSTERONE,
            unit = "ng/dL",
            normalRangeMin = 300.0,
            normalRangeMax = 1000.0,
            description = "Total testosterone"
        )
        
        // When
        val result = biomarkerUseCase.validateBiomarkerValue(BiomarkerType.TESTOSTERONE, 650.0, "mg/dL")
        
        // Then
        assertTrue(result is ValidationResult.Invalid)
        assertEquals("Unit should be ng/dL", (result as ValidationResult.Invalid).message)
    }
    
    @Test
    fun `validateBiomarkerValue should return warning for extreme values`() {
        // Given
        every { biomarkerRepository.getBiomarkerReference(BiomarkerType.TESTOSTERONE) } returns BiomarkerReference(
            biomarkerType = BiomarkerType.TESTOSTERONE,
            unit = "ng/dL",
            normalRangeMin = 300.0,
            normalRangeMax = 1000.0,
            description = "Total testosterone"
        )
        
        // When - value is 20x higher than normal max
        val result = biomarkerUseCase.validateBiomarkerValue(BiomarkerType.TESTOSTERONE, 20000.0, "ng/dL")
        
        // Then
        assertTrue(result is ValidationResult.Warning)
        assertEquals("Value seems unusually high or low. Please double-check.", (result as ValidationResult.Warning).message)
    }
    
    @Test
    fun `skipReminder should call repository skip method`() = runTest {
        // Given
        val reminderId = "reminder-123"
        every { biomarkerRepository.skipReminder(reminderId) } returns Result.success(Unit)
        
        // When
        val result = biomarkerUseCase.skipReminder(reminderId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { biomarkerRepository.skipReminder(reminderId) }
    }
    
    @Test
    fun `markReminderCompleted should call repository with correct date`() = runTest {
        // Given
        val reminderId = "reminder-123"
        val completedDate = LocalDate.now().minusDays(1)
        every { biomarkerRepository.markReminderCompleted(reminderId, any()) } returns Result.success(Unit)
        
        // When
        val result = biomarkerUseCase.markReminderCompleted(reminderId, completedDate)
        
        // Then
        assertTrue(result.isSuccess)
        verify { 
            biomarkerRepository.markReminderCompleted(reminderId, completedDate.format(dateFormatter))
        }
    }
    
    @Test
    fun `markReminderCompleted should use current date when none provided`() = runTest {
        // Given
        val reminderId = "reminder-123"
        every { biomarkerRepository.markReminderCompleted(reminderId, any()) } returns Result.success(Unit)
        
        // When
        val result = biomarkerUseCase.markReminderCompleted(reminderId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { 
            biomarkerRepository.markReminderCompleted(reminderId, LocalDate.now().format(dateFormatter))
        }
    }
    
    @Test
    fun `getActiveReminders should return flow from repository`() = runTest {
        // Given
        val reminders = listOf(
            BloodTestReminder(
                id = "reminder-1",
                userId = testUserId,
                testType = BloodTestType.HORMONAL_PANEL,
                reminderName = "Hormone Check",
                frequency = ReminderFrequency.QUARTERLY,
                nextDueDate = LocalDate.now().plusMonths(3).format(dateFormatter),
                isActive = true,
                canSkip = true,
                skipCount = 0,
                maxSkips = 3,
                createdAt = "2024-01-01T00:00:00",
                updatedAt = "2024-01-01T00:00:00"
            )
        )
        every { biomarkerRepository.getActiveReminders(testUserId) } returns flowOf(reminders)
        
        // When
        val result = biomarkerUseCase.getActiveReminders(testUserId)
        
        // Then
        verify { biomarkerRepository.getActiveReminders(testUserId) }
    }
}