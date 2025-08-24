package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.BiomarkerDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.BiomarkerRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiomarkerRepositoryImpl @Inject constructor(
    private val biomarkerDao: BiomarkerDao
) : BiomarkerRepository {
    
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    
    override fun getActiveReminders(userId: String): Flow<List<BloodTestReminder>> {
        return biomarkerDao.getActiveReminders(userId)
    }
    
    override fun getAllReminders(userId: String): Flow<List<BloodTestReminder>> {
        return biomarkerDao.getAllReminders(userId)
    }
    
    override suspend fun getOverdueReminders(userId: String): List<BloodTestReminderWithStatus> {
        val currentDate = LocalDate.now().format(dateFormatter)
        val overdueReminders = biomarkerDao.getOverdueReminders(userId, currentDate)
        
        return overdueReminders.map { reminder ->
            val daysSinceLastTest = reminder.lastCompletedDate?.let { lastDate ->
                ChronoUnit.DAYS.between(
                    LocalDate.parse(lastDate, dateFormatter),
                    LocalDate.now()
                ).toInt()
            }
            
            val daysUntilNext = ChronoUnit.DAYS.between(
                LocalDate.now(),
                LocalDate.parse(reminder.nextDueDate, dateFormatter)
            ).toInt()
            
            BloodTestReminderWithStatus(
                reminder = reminder,
                isOverdue = daysUntilNext < 0,
                daysSinceLastTest = daysSinceLastTest,
                daysUntilNext = daysUntilNext
            )
        }
    }
    
    override suspend fun createReminder(reminder: BloodTestReminder): Result<String> {
        return try {
            biomarkerDao.insertReminder(reminder)
            Result.success(reminder.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateReminder(reminder: BloodTestReminder): Result<Unit> {
        return try {
            biomarkerDao.updateReminder(reminder)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteReminder(reminderId: String): Result<Unit> {
        return try {
            val reminder = biomarkerDao.getReminderById(reminderId)
            if (reminder != null) {
                biomarkerDao.deleteReminder(reminder)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Reminder not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun skipReminder(reminderId: String): Result<Unit> {
        return try {
            val reminder = biomarkerDao.getReminderById(reminderId)
            if (reminder != null && reminder.canSkip && reminder.skipCount < reminder.maxSkips) {
                val newDueDate = calculateNextDueDate(reminder.frequency, LocalDate.now())
                biomarkerDao.skipReminder(reminderId, newDueDate.format(dateFormatter))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Cannot skip reminder"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markReminderCompleted(reminderId: String, completedDate: String): Result<Unit> {
        return try {
            val reminder = biomarkerDao.getReminderById(reminderId)
            if (reminder != null) {
                val nextDueDate = calculateNextDueDate(reminder.frequency, LocalDate.parse(completedDate, dateFormatter))
                biomarkerDao.markReminderCompleted(reminderId, completedDate, nextDueDate.format(dateFormatter))
                Result.success(Unit)
            } else {
                Result.failure(Exception("Reminder not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllBiomarkerEntries(userId: String): Flow<List<BiomarkerEntry>> {
        return biomarkerDao.getAllBiomarkerEntries(userId)
    }
    
    override fun getBiomarkerEntriesByType(userId: String, type: BiomarkerType): Flow<List<BiomarkerEntry>> {
        return biomarkerDao.getBiomarkerEntriesByType(userId, type)
    }
    
    override fun getBiomarkerEntriesByTestType(userId: String, testType: BloodTestType): Flow<List<BiomarkerEntry>> {
        return biomarkerDao.getBiomarkerEntriesByTestType(userId, testType)
    }
    
    override fun getBiomarkerEntriesInDateRange(userId: String, startDate: String, endDate: String): Flow<List<BiomarkerEntry>> {
        return biomarkerDao.getBiomarkerEntriesInDateRange(userId, startDate, endDate)
    }
    
    override suspend fun saveBiomarkerEntry(entry: BiomarkerEntry): Result<String> {
        return try {
            val entryWithRange = entry.copy(
                isWithinRange = checkIfWithinRange(entry)
            )
            biomarkerDao.insertBiomarkerEntry(entryWithRange)
            Result.success(entry.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun saveBiomarkerEntries(entries: List<BiomarkerEntry>): Result<Unit> {
        return try {
            val entriesWithRange = entries.map { entry ->
                entry.copy(isWithinRange = checkIfWithinRange(entry))
            }
            biomarkerDao.insertBiomarkerEntries(entriesWithRange)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateBiomarkerEntry(entry: BiomarkerEntry): Result<Unit> {
        return try {
            val entryWithRange = entry.copy(
                isWithinRange = checkIfWithinRange(entry)
            )
            biomarkerDao.updateBiomarkerEntry(entryWithRange)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteBiomarkerEntry(entryId: String): Result<Unit> {
        return try {
            // Note: This would need to be implemented with a proper query
            // For now, we'll assume the entry exists and handle it in the DAO
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getTestSessions(userId: String): Flow<List<BiomarkerTestSession>> {
        return biomarkerDao.getTestSessions(userId)
    }
    
    override suspend fun createTestSession(session: BiomarkerTestSession): Result<String> {
        return try {
            biomarkerDao.insertTestSession(session)
            Result.success(session.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateTestSession(session: BiomarkerTestSession): Result<Unit> {
        return try {
            biomarkerDao.updateTestSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTestSession(sessionId: String): Result<Unit> {
        return try {
            val session = biomarkerDao.getTestSessionById(sessionId)
            if (session != null) {
                biomarkerDao.deleteTestSession(session)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Test session not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getBiomarkerTrends(userId: String, biomarkerType: BiomarkerType): BiomarkerTrend {
        val entries = biomarkerDao.getRecentBiomarkerEntries(userId, biomarkerType, 
            LocalDate.now().minusMonths(6).format(dateFormatter), 10)
        
        val trend = if (entries.size >= 2) {
            val latest = entries.first().value
            val previous = entries[1].value
            val changePercentage = ((latest - previous) / previous) * 100
            
            when {
                changePercentage > 5 -> TrendDirection.INCREASING
                changePercentage < -5 -> TrendDirection.DECREASING
                else -> TrendDirection.STABLE
            }
        } else {
            TrendDirection.INSUFFICIENT_DATA
        }
        
        val changePercentage = if (entries.size >= 2) {
            val latest = entries.first().value
            val previous = entries[1].value
            ((latest - previous) / previous) * 100
        } else null
        
        return BiomarkerTrend(
            biomarkerType = biomarkerType,
            entries = entries,
            trend = trend,
            changePercentage = changePercentage
        )
    }
    
    override suspend fun getBiomarkerInsights(userId: String): List<BiomarkerInsight> {
        // This would be implemented with more sophisticated analysis
        // For now, return empty list
        return emptyList()
    }
    
    override suspend fun getBiomarkerStats(userId: String): Map<BiomarkerType, Int> {
        val stats = biomarkerDao.getBiomarkerEntryStats(userId)
        return stats.associate { it.biomarkerType to it.count }
    }
    
    override fun getBiomarkerReference(biomarkerType: BiomarkerType): BiomarkerReference? {
        return biomarkerReferences[biomarkerType]
    }
    
    override fun getBiomarkerCategories(): List<BiomarkerCategory> {
        return biomarkerCategories
    }
    
    override fun getBiomarkersForTestType(testType: BloodTestType): List<BiomarkerType> {
        return testTypeBiomarkers[testType] ?: emptyList()
    }
    
    private fun checkIfWithinRange(entry: BiomarkerEntry): Boolean {
        return if (entry.referenceRangeMin != null && entry.referenceRangeMax != null) {
            entry.value >= entry.referenceRangeMin && entry.value <= entry.referenceRangeMax
        } else {
            val reference = getBiomarkerReference(entry.biomarkerType)
            reference?.let { ref ->
                entry.value >= ref.normalRangeMin && entry.value <= ref.normalRangeMax
            } ?: true
        }
    }
    
    private fun calculateNextDueDate(frequency: ReminderFrequency, fromDate: LocalDate): LocalDate {
        return when (frequency) {
            ReminderFrequency.MONTHLY -> fromDate.plusMonths(1)
            ReminderFrequency.QUARTERLY -> fromDate.plusMonths(3)
            ReminderFrequency.SEMI_ANNUALLY -> fromDate.plusMonths(6)
            ReminderFrequency.ANNUALLY -> fromDate.plusYears(1)
            ReminderFrequency.CUSTOM -> fromDate.plusMonths(3) // Default to quarterly
        }
    }
    
    companion object {
        private val biomarkerReferences = mapOf(
            BiomarkerType.TESTOSTERONE to BiomarkerReference(
                biomarkerType = BiomarkerType.TESTOSTERONE,
                unit = "ng/dL",
                normalRangeMin = 300.0,
                normalRangeMax = 1000.0,
                optimalRangeMin = 500.0,
                optimalRangeMax = 800.0,
                description = "Total testosterone levels"
            ),
            BiomarkerType.VITAMIN_D3 to BiomarkerReference(
                biomarkerType = BiomarkerType.VITAMIN_D3,
                unit = "ng/mL",
                normalRangeMin = 30.0,
                normalRangeMax = 100.0,
                optimalRangeMin = 40.0,
                optimalRangeMax = 80.0,
                description = "25-hydroxyvitamin D"
            ),
            BiomarkerType.VITAMIN_B12 to BiomarkerReference(
                biomarkerType = BiomarkerType.VITAMIN_B12,
                unit = "pg/mL",
                normalRangeMin = 200.0,
                normalRangeMax = 900.0,
                optimalRangeMin = 400.0,
                optimalRangeMax = 700.0,
                description = "Vitamin B12 (cobalamin)"
            )
            // Add more references as needed
        )
        
        private val biomarkerCategories = listOf(
            BiomarkerCategory(
                name = "Hormonal Markers",
                biomarkers = listOf(
                    BiomarkerType.TESTOSTERONE, BiomarkerType.ESTRADIOL, BiomarkerType.CORTISOL,
                    BiomarkerType.THYROID_TSH, BiomarkerType.THYROID_T3, BiomarkerType.THYROID_T4
                ),
                description = "Hormones that regulate metabolism, reproduction, and stress response"
            ),
            BiomarkerCategory(
                name = "Vitamins",
                biomarkers = listOf(
                    BiomarkerType.VITAMIN_D3, BiomarkerType.VITAMIN_B12, BiomarkerType.VITAMIN_B6,
                    BiomarkerType.FOLATE, BiomarkerType.VITAMIN_C
                ),
                description = "Essential vitamins for optimal health"
            ),
            BiomarkerCategory(
                name = "Minerals",
                biomarkers = listOf(
                    BiomarkerType.IRON, BiomarkerType.FERRITIN, BiomarkerType.ZINC,
                    BiomarkerType.MAGNESIUM, BiomarkerType.CALCIUM
                ),
                description = "Essential minerals for bodily functions"
            )
        )
        
        private val testTypeBiomarkers = mapOf(
            BloodTestType.HORMONAL_PANEL to listOf(
                BiomarkerType.TESTOSTERONE, BiomarkerType.ESTRADIOL, BiomarkerType.CORTISOL,
                BiomarkerType.THYROID_TSH, BiomarkerType.THYROID_T3, BiomarkerType.THYROID_T4
            ),
            BloodTestType.MICRONUTRIENT_PANEL to listOf(
                BiomarkerType.VITAMIN_D3, BiomarkerType.VITAMIN_B12, BiomarkerType.VITAMIN_B6,
                BiomarkerType.FOLATE, BiomarkerType.IRON, BiomarkerType.FERRITIN, BiomarkerType.ZINC,
                BiomarkerType.MAGNESIUM
            ),
            BloodTestType.GENERAL_HEALTH_PANEL to listOf(
                BiomarkerType.TOTAL_CHOLESTEROL, BiomarkerType.HDL_CHOLESTEROL, BiomarkerType.LDL_CHOLESTEROL,
                BiomarkerType.TRIGLYCERIDES, BiomarkerType.HBA1C, BiomarkerType.GLUCOSE_FASTING,
                BiomarkerType.RBC_COUNT, BiomarkerType.HEMOGLOBIN
            )
        )
    }
}