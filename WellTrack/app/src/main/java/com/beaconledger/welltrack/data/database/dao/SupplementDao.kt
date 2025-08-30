package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SupplementDao {
    
    // Supplement Library Operations
    @Query("SELECT * FROM supplements ORDER BY name ASC")
    fun getAllSupplements(): Flow<List<Supplement>>
    
    @Query("SELECT * FROM supplements WHERE category = :category ORDER BY name ASC")
    fun getSupplementsByCategory(category: SupplementCategory): Flow<List<Supplement>>
    
    @Query("SELECT * FROM supplements WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchSupplements(query: String): Flow<List<Supplement>>
    
    @Query("SELECT * FROM supplements WHERE barcode = :barcode LIMIT 1")
    suspend fun getSupplementByBarcode(barcode: String): Supplement?
    
    @Query("SELECT * FROM supplements WHERE id = :id")
    suspend fun getSupplementById(id: String): Supplement?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplement(supplement: Supplement)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplements(supplements: List<Supplement>)
    
    @Query("SELECT s.* FROM supplements s INNER JOIN user_supplements us ON s.id = us.supplementId WHERE us.userId = :userId")
    suspend fun getSupplementsForUser(userId: String): List<Supplement>
    
    @Update
    suspend fun updateSupplement(supplement: Supplement)
    
    @Delete
    suspend fun deleteSupplement(supplement: Supplement)
    
    // User Supplement Operations
    @Query("""
        SELECT us.*, s.name as supplementName, s.brand, s.nutritionalInfo 
        FROM user_supplements us 
        INNER JOIN supplements s ON us.supplementId = s.id 
        WHERE us.userId = :userId AND us.isActive = 1 
        ORDER BY s.name ASC
    """)
    fun getActiveUserSupplements(userId: String): Flow<List<UserSupplementWithDetails>>
    
    @Query("""
        SELECT us.*, s.name as supplementName, s.brand, s.nutritionalInfo 
        FROM user_supplements us 
        INNER JOIN supplements s ON us.supplementId = s.id 
        WHERE us.userId = :userId 
        ORDER BY s.name ASC
    """)
    fun getAllUserSupplements(userId: String): Flow<List<UserSupplementWithDetails>>
    
    @Query("SELECT * FROM user_supplements WHERE id = :id")
    suspend fun getUserSupplementById(id: String): UserSupplement?
    
    @Query("SELECT * FROM user_supplements WHERE userId = :userId AND supplementId = :supplementId AND isActive = 1 LIMIT 1")
    suspend fun getActiveUserSupplementBySupplementId(userId: String, supplementId: String): UserSupplement?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSupplement(userSupplement: UserSupplement)
    
    @Update
    suspend fun updateUserSupplement(userSupplement: UserSupplement)
    
    @Query("UPDATE user_supplements SET isActive = 0, updatedAt = :updatedAt WHERE id = :id")
    suspend fun deactivateUserSupplement(id: String, updatedAt: String)
    
    @Query("DELETE FROM user_supplements WHERE userId = :userId")
    suspend fun deleteAllUserSupplements(userId: String)
    
    // Supplement Intake Operations
    @Query("""
        SELECT si.*, us.customName, s.name as supplementName, s.brand 
        FROM supplement_intakes si 
        INNER JOIN user_supplements us ON si.userSupplementId = us.id 
        INNER JOIN supplements s ON us.supplementId = s.id 
        WHERE si.userId = :userId AND DATE(si.takenAt) = DATE(:date) 
        ORDER BY si.takenAt DESC
    """)
    fun getSupplementIntakesForDate(userId: String, date: String): Flow<List<SupplementIntakeWithDetails>>
    
    @Query("""
        SELECT si.*, us.customName, s.name as supplementName, s.brand 
        FROM supplement_intakes si 
        INNER JOIN user_supplements us ON si.userSupplementId = us.id 
        INNER JOIN supplements s ON us.supplementId = s.id 
        WHERE si.userId = :userId AND si.takenAt BETWEEN :startDate AND :endDate 
        ORDER BY si.takenAt DESC
    """)
    fun getSupplementIntakesInDateRange(userId: String, startDate: String, endDate: String): Flow<List<SupplementIntakeWithDetails>>
    
    @Query("SELECT * FROM supplement_intakes WHERE userSupplementId = :userSupplementId AND DATE(takenAt) = DATE(:date)")
    suspend fun getIntakesForUserSupplementOnDate(userSupplementId: String, date: String): List<SupplementIntake>
    
    @Query("SELECT COUNT(*) FROM supplement_intakes WHERE userId = :userId AND DATE(takenAt) = DATE(:date) AND status = 'TAKEN'")
    suspend fun getTakenSupplementCountForDate(userId: String, date: String): Int
    
    @Query("SELECT COUNT(*) FROM supplement_intakes WHERE userId = :userId AND DATE(takenAt) = DATE(:date)")
    suspend fun getTotalScheduledSupplementCountForDate(userId: String, date: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementIntake(intake: SupplementIntake)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplementIntakes(intakes: List<SupplementIntake>)
    
    @Update
    suspend fun updateSupplementIntake(intake: SupplementIntake)
    
    @Delete
    suspend fun deleteSupplementIntake(intake: SupplementIntake)
    
    @Query("DELETE FROM supplement_intakes WHERE userId = :userId")
    suspend fun deleteAllSupplementIntakes(userId: String)
    
    @Query("DELETE FROM supplements WHERE userId = :userId")
    suspend fun deleteAllSupplementsForUser(userId: String)
    
    // Analytics and Reporting
    @Query("""
        SELECT 
            DATE(si.takenAt) as date,
            COUNT(CASE WHEN si.status = 'TAKEN' THEN 1 END) as takenCount,
            COUNT(*) as totalCount
        FROM supplement_intakes si 
        WHERE si.userId = :userId AND si.takenAt BETWEEN :startDate AND :endDate 
        GROUP BY DATE(si.takenAt) 
        ORDER BY date DESC
    """)
    suspend fun getSupplementAdherenceStats(userId: String, startDate: String, endDate: String): List<SupplementAdherenceStats>
    
    @Query("""
        SELECT 
            s.name as supplementName,
            s.brand,
            COUNT(CASE WHEN si.status = 'TAKEN' THEN 1 END) as takenCount,
            COUNT(*) as totalCount
        FROM supplement_intakes si 
        INNER JOIN user_supplements us ON si.userSupplementId = us.id 
        INNER JOIN supplements s ON us.supplementId = s.id 
        WHERE si.userId = :userId AND si.takenAt BETWEEN :startDate AND :endDate 
        GROUP BY s.id, s.name, s.brand 
        ORDER BY s.name ASC
    """)
    suspend fun getSupplementAdherenceByType(userId: String, startDate: String, endDate: String): List<SupplementTypeAdherence>
    

}

// Data classes for complex queries
data class UserSupplementWithDetails(
    val id: String,
    val userId: String,
    val supplementId: String,
    val customName: String?,
    val dosage: Double,
    val dosageUnit: String,
    val frequency: SupplementFrequency,
    val scheduledTimes: String,
    val isActive: Boolean,
    val notes: String?,
    val startDate: String,
    val endDate: String?,
    val createdAt: String,
    val updatedAt: String,
    val supplementName: String,
    val brand: String?,
    val nutritionalInfo: String
)

data class SupplementIntakeWithDetails(
    val id: String,
    val userId: String,
    val userSupplementId: String,
    val actualDosage: Double,
    val dosageUnit: String,
    val takenAt: String,
    val scheduledAt: String?,
    val status: IntakeStatus,
    val notes: String?,
    val createdAt: String,
    val customName: String?,
    val supplementName: String,
    val brand: String?
)

data class SupplementAdherenceStats(
    val date: String,
    val takenCount: Int,
    val totalCount: Int
) {
    val adherencePercentage: Float
        get() = if (totalCount > 0) (takenCount.toFloat() / totalCount.toFloat()) * 100f else 0f
}

data class SupplementTypeAdherence(
    val supplementName: String,
    val brand: String?,
    val takenCount: Int,
    val totalCount: Int
) {
    val adherencePercentage: Float
        get() = if (totalCount > 0) (takenCount.toFloat() / totalCount.toFloat()) * 100f else 0f
}