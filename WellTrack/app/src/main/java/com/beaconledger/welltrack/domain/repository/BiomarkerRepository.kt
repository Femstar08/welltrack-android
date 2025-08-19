package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface BiomarkerRepository {
    
    // Blood Test Reminders
    fun getActiveReminders(userId: String): Flow<List<BloodTestReminder>>
    fun getAllReminders(userId: String): Flow<List<BloodTestReminder>>
    suspend fun getOverdueReminders(userId: String): List<BloodTestReminderWithStatus>
    suspend fun createReminder(reminder: BloodTestReminder): Result<String>
    suspend fun updateReminder(reminder: BloodTestReminder): Result<Unit>
    suspend fun deleteReminder(reminderId: String): Result<Unit>
    suspend fun skipReminder(reminderId: String): Result<Unit>
    suspend fun markReminderCompleted(reminderId: String, completedDate: String): Result<Unit>
    
    // Biomarker Entries
    fun getAllBiomarkerEntries(userId: String): Flow<List<BiomarkerEntry>>
    fun getBiomarkerEntriesByType(userId: String, type: BiomarkerType): Flow<List<BiomarkerEntry>>
    fun getBiomarkerEntriesByTestType(userId: String, testType: BloodTestType): Flow<List<BiomarkerEntry>>
    fun getBiomarkerEntriesInDateRange(userId: String, startDate: String, endDate: String): Flow<List<BiomarkerEntry>>
    suspend fun saveBiomarkerEntry(entry: BiomarkerEntry): Result<String>
    suspend fun saveBiomarkerEntries(entries: List<BiomarkerEntry>): Result<Unit>
    suspend fun updateBiomarkerEntry(entry: BiomarkerEntry): Result<Unit>
    suspend fun deleteBiomarkerEntry(entryId: String): Result<Unit>
    
    // Test Sessions
    fun getTestSessions(userId: String): Flow<List<BiomarkerTestSession>>
    suspend fun createTestSession(session: BiomarkerTestSession): Result<String>
    suspend fun updateTestSession(session: BiomarkerTestSession): Result<Unit>
    suspend fun deleteTestSession(sessionId: String): Result<Unit>
    
    // Analytics and Insights
    suspend fun getBiomarkerTrends(userId: String, biomarkerType: BiomarkerType): BiomarkerTrend
    suspend fun getBiomarkerInsights(userId: String): List<BiomarkerInsight>
    suspend fun getBiomarkerStats(userId: String): Map<BiomarkerType, Int>
    
    // Reference Data
    fun getBiomarkerReference(biomarkerType: BiomarkerType): BiomarkerReference?
    fun getBiomarkerCategories(): List<BiomarkerCategory>
    fun getBiomarkersForTestType(testType: BloodTestType): List<BiomarkerType>
}