package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import kotlinx.coroutines.flow.Flow

interface SupplementRepository {
    
    // Supplement Library Management
    fun getAllSupplements(): Flow<List<Supplement>>
    fun getSupplementsByCategory(category: SupplementCategory): Flow<List<Supplement>>
    fun searchSupplements(query: String): Flow<List<Supplement>>
    suspend fun getSupplementById(id: String): Supplement?
    suspend fun getSupplementByBarcode(barcode: String): Supplement?
    suspend fun saveSupplement(supplement: Supplement): Result<String>
    suspend fun updateSupplement(supplement: Supplement): Result<Unit>
    suspend fun deleteSupplement(supplement: Supplement): Result<Unit>
    suspend fun importSupplementFromBarcode(barcode: String): Result<Supplement>
    
    // User Supplement Management
    fun getActiveUserSupplements(userId: String): Flow<List<UserSupplementWithDetails>>
    fun getAllUserSupplements(userId: String): Flow<List<UserSupplementWithDetails>>
    suspend fun getUserSupplementById(id: String): UserSupplement?
    suspend fun addUserSupplement(userSupplement: UserSupplement): Result<String>
    suspend fun updateUserSupplement(userSupplement: UserSupplement): Result<Unit>
    suspend fun deactivateUserSupplement(id: String): Result<Unit>
    suspend fun deleteAllUserSupplements(userId: String): Result<Unit>
    
    // Supplement Intake Tracking
    fun getSupplementIntakesForDate(userId: String, date: String): Flow<List<SupplementIntakeWithDetails>>
    fun getSupplementIntakesInDateRange(userId: String, startDate: String, endDate: String): Flow<List<SupplementIntakeWithDetails>>
    suspend fun logSupplementIntake(intake: SupplementIntake): Result<String>
    suspend fun updateSupplementIntake(intake: SupplementIntake): Result<Unit>
    suspend fun deleteSupplementIntake(intake: SupplementIntake): Result<Unit>
    suspend fun generateScheduledIntakes(userId: String, date: String): Result<List<SupplementIntake>>
    suspend fun markIntakeAsCompleted(intakeId: String, actualDosage: Double, notes: String?): Result<Unit>
    suspend fun markIntakeAsSkipped(intakeId: String, notes: String?): Result<Unit>
    
    // Analytics and Insights
    suspend fun getSupplementAdherenceStats(userId: String, startDate: String, endDate: String): List<SupplementAdherenceStats>
    suspend fun getSupplementAdherenceByType(userId: String, startDate: String, endDate: String): List<SupplementTypeAdherence>
    suspend fun getTodaySupplementSummary(userId: String): SupplementDailySummary
    suspend fun calculateNutritionalContribution(userId: String, date: String): SupplementNutrition
    
    // Scheduling and Reminders
    suspend fun getUpcomingSupplementReminders(userId: String): List<SupplementReminder>
    suspend fun getMissedSupplements(userId: String, date: String): List<UserSupplementWithDetails>
}

