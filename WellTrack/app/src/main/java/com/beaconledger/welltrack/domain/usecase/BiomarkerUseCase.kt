package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.BiomarkerRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiomarkerUseCase @Inject constructor(
    private val biomarkerRepository: BiomarkerRepository
) {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    // Blood Test Reminders
    fun getActiveReminders(userId: String): Flow<List<BloodTestReminder>> {
        return biomarkerRepository.getActiveReminders(userId)
    }
    
    fun getAllReminders(userId: String): Flow<List<BloodTestReminder>> {
        return biomarkerRepository.getAllReminders(userId)
    }
    
    suspend fun getOverdueReminders(userId: String): List<BloodTestReminderWithStatus> {
        return biomarkerRepository.getOverdueReminders(userId)
    }
    
    suspend fun createBloodTestReminder(
        userId: String,
        testType: BloodTestType,
        reminderName: String,
        description: String? = null,
        frequency: ReminderFrequency,
        firstDueDate: LocalDate? = null
    ): Result<String> {
        val dueDate = firstDueDate ?: calculateInitialDueDate(frequency)
        val now = LocalDateTime.now().format(dateTimeFormatter)
        
        val reminder = BloodTestReminder(
            id = UUID.randomUUID().toString(),
            userId = userId,
            testType = testType,
            reminderName = reminderName,
            description = description,
            frequency = frequency,
            nextDueDate = dueDate.format(dateFormatter),
            lastCompletedDate = null,
            isActive = true,
            canSkip = true,
            skipCount = 0,
            maxSkips = 3,
            createdAt = now,
            updatedAt = now
        )
        
        return biomarkerRepository.createReminder(reminder)
    }
    
    suspend fun updateReminder(reminder: BloodTestReminder): Result<Unit> {
        val updatedReminder = reminder.copy(
            updatedAt = LocalDateTime.now().format(dateTimeFormatter)
        )
        return biomarkerRepository.updateReminder(updatedReminder)
    }
    
    suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return biomarkerRepository.deleteReminder(reminderId)
    }
    
    suspend fun skipReminder(reminderId: String): Result<Unit> {
        return biomarkerRepository.skipReminder(reminderId)
    }
    
    suspend fun markReminderCompleted(reminderId: String, completedDate: LocalDate? = null): Result<Unit> {
        val dateToUse = completedDate ?: LocalDate.now()
        return biomarkerRepository.markReminderCompleted(reminderId, dateToUse.format(dateFormatter))
    }
    
    // Biomarker Entries
    fun getAllBiomarkerEntries(userId: String): Flow<List<BiomarkerEntry>> {
        return biomarkerRepository.getAllBiomarkerEntries(userId)
    }
    
    fun getBiomarkerEntriesByType(userId: String, type: BiomarkerType): Flow<List<BiomarkerEntry>> {
        return biomarkerRepository.getBiomarkerEntriesByType(userId, type)
    }
    
    fun getBiomarkerEntriesByTestType(userId: String, testType: BloodTestType): Flow<List<BiomarkerEntry>> {
        return biomarkerRepository.getBiomarkerEntriesByTestType(userId, testType)
    }
    
    suspend fun saveBiomarkerEntry(
        userId: String,
        testType: BloodTestType,
        biomarkerType: BiomarkerType,
        value: Double,
        unit: String,
        testDate: LocalDate,
        notes: String? = null,
        labName: String? = null,
        referenceRangeMin: Double? = null,
        referenceRangeMax: Double? = null
    ): Result<String> {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        
        val entry = BiomarkerEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            testType = testType,
            biomarkerType = biomarkerType,
            value = value,
            unit = unit,
            referenceRangeMin = referenceRangeMin,
            referenceRangeMax = referenceRangeMax,
            testDate = testDate.format(dateFormatter),
            entryDate = now,
            notes = notes,
            labName = labName,
            isWithinRange = null, // Will be calculated in repository
            createdAt = now
        )
        
        return biomarkerRepository.saveBiomarkerEntry(entry)
    }
    
    suspend fun saveBulkBiomarkerEntries(
        userId: String,
        testType: BloodTestType,
        testDate: LocalDate,
        entries: List<BiomarkerEntryInput>,
        labName: String? = null,
        sessionNotes: String? = null
    ): Result<String> {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        val sessionId = UUID.randomUUID().toString()
        
        // Create test session
        val session = BiomarkerTestSession(
            id = sessionId,
            userId = userId,
            testDate = testDate.format(dateFormatter),
            labName = labName,
            notes = sessionNotes,
            totalMarkers = entries.size,
            enteredMarkers = entries.size,
            isComplete = true,
            createdAt = now,
            updatedAt = now
        )
        
        val sessionResult = biomarkerRepository.createTestSession(session)
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull() ?: Exception("Failed to create session"))
        }
        
        // Create biomarker entries
        val biomarkerEntries = entries.map { input ->
            BiomarkerEntry(
                id = UUID.randomUUID().toString(),
                userId = userId,
                testType = testType,
                biomarkerType = input.biomarkerType,
                value = input.value,
                unit = input.unit,
                referenceRangeMin = input.referenceRangeMin,
                referenceRangeMax = input.referenceRangeMax,
                testDate = testDate.format(dateFormatter),
                entryDate = now,
                notes = input.notes,
                labName = labName,
                isWithinRange = null, // Will be calculated in repository
                createdAt = now
            )
        }
        
        val entriesResult = biomarkerRepository.saveBiomarkerEntries(biomarkerEntries)
        return if (entriesResult.isSuccess) {
            Result.success(sessionId)
        } else {
            Result.failure(entriesResult.exceptionOrNull() ?: Exception("Failed to save entries"))
        }
    }
    
    suspend fun updateBiomarkerEntry(entry: BiomarkerEntry): Result<Unit> {
        return biomarkerRepository.updateBiomarkerEntry(entry)
    }
    
    suspend fun deleteBiomarkerEntry(entryId: String): Result<Unit> {
        return biomarkerRepository.deleteBiomarkerEntry(entryId)
    }
    
    // Test Sessions
    fun getTestSessions(userId: String): Flow<List<BiomarkerTestSession>> {
        return biomarkerRepository.getTestSessions(userId)
    }
    
    suspend fun createTestSession(
        userId: String,
        testDate: LocalDate,
        labName: String? = null,
        notes: String? = null,
        expectedMarkers: Int = 0
    ): Result<String> {
        val now = LocalDateTime.now().format(dateTimeFormatter)
        
        val session = BiomarkerTestSession(
            id = UUID.randomUUID().toString(),
            userId = userId,
            testDate = testDate.format(dateFormatter),
            labName = labName,
            notes = notes,
            totalMarkers = expectedMarkers,
            enteredMarkers = 0,
            isComplete = false,
            createdAt = now,
            updatedAt = now
        )
        
        return biomarkerRepository.createTestSession(session)
    }
    
    // Analytics and Insights
    suspend fun getBiomarkerTrends(userId: String, biomarkerType: BiomarkerType): BiomarkerTrend {
        return biomarkerRepository.getBiomarkerTrends(userId, biomarkerType)
    }
    
    suspend fun getBiomarkerInsights(userId: String): List<BiomarkerInsight> {
        return biomarkerRepository.getBiomarkerInsights(userId)
    }
    
    suspend fun getBiomarkerStats(userId: String): Map<BiomarkerType, Int> {
        return biomarkerRepository.getBiomarkerStats(userId)
    }
    
    // Reference Data
    fun getBiomarkerReference(biomarkerType: BiomarkerType): BiomarkerReference? {
        return biomarkerRepository.getBiomarkerReference(biomarkerType)
    }
    
    fun getBiomarkerCategories(): List<BiomarkerCategory> {
        return biomarkerRepository.getBiomarkerCategories()
    }
    
    fun getBiomarkersForTestType(testType: BloodTestType): List<BiomarkerType> {
        return biomarkerRepository.getBiomarkersForTestType(testType)
    }
    
    // Helper functions
    fun getDefaultReminderFrequency(testType: BloodTestType): ReminderFrequency {
        return when (testType) {
            BloodTestType.HORMONAL_PANEL -> ReminderFrequency.QUARTERLY
            BloodTestType.MICRONUTRIENT_PANEL -> ReminderFrequency.SEMI_ANNUALLY
            BloodTestType.GENERAL_HEALTH_PANEL -> ReminderFrequency.ANNUALLY
            BloodTestType.LIPID_PANEL -> ReminderFrequency.ANNUALLY
            BloodTestType.THYROID_PANEL -> ReminderFrequency.SEMI_ANNUALLY
            else -> ReminderFrequency.ANNUALLY
        }
    }
    
    fun validateBiomarkerValue(biomarkerType: BiomarkerType, value: Double, unit: String): ValidationResult {
        val reference = getBiomarkerReference(biomarkerType)
        
        return when {
            value < 0 -> ValidationResult.Invalid("Value cannot be negative")
            reference != null && reference.unit != unit -> ValidationResult.Invalid("Unit should be ${reference.unit}")
            reference != null && (value < reference.normalRangeMin * 0.1 || value > reference.normalRangeMax * 10) -> 
                ValidationResult.Warning("Value seems unusually high or low. Please double-check.")
            else -> ValidationResult.Valid
        }
    }
    
    private fun calculateInitialDueDate(frequency: ReminderFrequency): LocalDate {
        val now = LocalDate.now()
        return when (frequency) {
            ReminderFrequency.MONTHLY -> now.plusMonths(1)
            ReminderFrequency.QUARTERLY -> now.plusMonths(3)
            ReminderFrequency.SEMI_ANNUALLY -> now.plusMonths(6)
            ReminderFrequency.ANNUALLY -> now.plusYears(1)
            ReminderFrequency.CUSTOM -> now.plusMonths(3) // Default to quarterly
        }
    }
}

data class BiomarkerEntryInput(
    val biomarkerType: BiomarkerType,
    val value: Double,
    val unit: String,
    val referenceRangeMin: Double? = null,
    val referenceRangeMax: Double? = null,
    val notes: String? = null
)

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Warning(val message: String) : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}