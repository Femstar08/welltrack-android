package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BiomarkerDao {
    
    // Blood Test Reminders
    @Query("SELECT * FROM blood_test_reminders WHERE userId = :userId AND isActive = 1 ORDER BY nextDueDate ASC")
    fun getActiveReminders(userId: String): Flow<List<BloodTestReminder>>
    
    @Query("SELECT * FROM blood_test_reminders WHERE userId = :userId ORDER BY nextDueDate ASC")
    fun getAllReminders(userId: String): Flow<List<BloodTestReminder>>
    
    @Query("SELECT * FROM blood_test_reminders WHERE userId = :userId AND nextDueDate <= :currentDate AND isActive = 1")
    suspend fun getOverdueReminders(userId: String, currentDate: String): List<BloodTestReminder>
    
    @Query("SELECT * FROM blood_test_reminders WHERE id = :reminderId")
    suspend fun getReminderById(reminderId: String): BloodTestReminder?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: BloodTestReminder)
    
    @Update
    suspend fun updateReminder(reminder: BloodTestReminder)
    
    @Delete
    suspend fun deleteReminder(reminder: BloodTestReminder)
    
    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId")
    suspend fun getBiomarkersForUser(userId: String): List<BiomarkerEntry>
    
    @Query("UPDATE blood_test_reminders SET skipCount = skipCount + 1, nextDueDate = :newDueDate WHERE id = :reminderId")
    suspend fun skipReminder(reminderId: String, newDueDate: String)
    
    @Query("UPDATE blood_test_reminders SET lastCompletedDate = :completedDate, nextDueDate = :nextDueDate, skipCount = 0 WHERE id = :reminderId")
    suspend fun markReminderCompleted(reminderId: String, completedDate: String, nextDueDate: String)
    
    // Biomarker Entries
    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId ORDER BY testDate DESC")
    fun getAllBiomarkerEntries(userId: String): Flow<List<BiomarkerEntry>>
    
    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId AND biomarkerType = :type ORDER BY testDate DESC")
    fun getBiomarkerEntriesByType(userId: String, type: BiomarkerType): Flow<List<BiomarkerEntry>>
    
    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId AND testType = :testType ORDER BY testDate DESC")
    fun getBiomarkerEntriesByTestType(userId: String, testType: BloodTestType): Flow<List<BiomarkerEntry>>
    
    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId AND testDate BETWEEN :startDate AND :endDate ORDER BY testDate DESC")
    fun getBiomarkerEntriesInDateRange(userId: String, startDate: String, endDate: String): Flow<List<BiomarkerEntry>>
    
    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId AND biomarkerType = :type AND testDate >= :sinceDate ORDER BY testDate DESC LIMIT :limit")
    suspend fun getRecentBiomarkerEntries(userId: String, type: BiomarkerType, sinceDate: String, limit: Int): List<BiomarkerEntry>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiomarkerEntry(entry: BiomarkerEntry)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiomarkerEntries(entries: List<BiomarkerEntry>)
    
    @Update
    suspend fun updateBiomarker(entry: BiomarkerEntry)
    
    @Delete
    suspend fun deleteBiomarkerEntry(entry: BiomarkerEntry)
    
    @Query("DELETE FROM biomarker_entries WHERE userId = :userId")
    suspend fun deleteAllBiomarkerEntries(userId: String)
    
    // Test Sessions
    @Query("SELECT * FROM biomarker_test_sessions WHERE userId = :userId ORDER BY testDate DESC")
    fun getTestSessions(userId: String): Flow<List<BiomarkerTestSession>>
    
    @Query("SELECT * FROM biomarker_test_sessions WHERE id = :sessionId")
    suspend fun getTestSessionById(sessionId: String): BiomarkerTestSession?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTestSession(session: BiomarkerTestSession)
    
    @Update
    suspend fun updateTestSession(session: BiomarkerTestSession)
    
    @Delete
    suspend fun deleteTestSession(session: BiomarkerTestSession)
    
    // Analytics queries
    @Query("""
        SELECT biomarkerType, COUNT(*) as count 
        FROM biomarker_entries 
        WHERE userId = :userId 
        GROUP BY biomarkerType 
        ORDER BY count DESC
    """)
    suspend fun getBiomarkerEntryStats(userId: String): List<BiomarkerTypeCount>
    
    @Query("""
        SELECT COUNT(DISTINCT testDate) 
        FROM biomarker_entries 
        WHERE userId = :userId AND testDate >= :sinceDate
    """)
    suspend fun getTestDaysCount(userId: String, sinceDate: String): Int
    
    @Query("""
        SELECT * FROM biomarker_entries 
        WHERE userId = :userId AND biomarkerType = :type 
        ORDER BY testDate DESC 
        LIMIT 2
    """)
    suspend fun getLastTwoBiomarkerEntries(userId: String, type: BiomarkerType): List<BiomarkerEntry>
    
    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId")
    suspend fun getAllBiomarkersForUser(userId: String): List<BiomarkerEntry>
    
    @Query("DELETE FROM biomarker_entries WHERE userId = :userId")
    suspend fun deleteAllBiomarkersForUser(userId: String)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBiomarker(biomarker: BiomarkerEntry)

    @Query("SELECT * FROM biomarker_entries WHERE userId = :userId AND biomarkerType = :type AND testDate = :testDate")
    suspend fun getBiomarkerByTypeAndDate(userId: String, type: BiomarkerType, testDate: String): BiomarkerEntry?
}

data class BiomarkerTypeCount(
    val biomarkerType: BiomarkerType,
    val count: Int
)