package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.domain.repository.SupplementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime
import java.time.LocalDate
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Singleton
class SupplementUseCase @Inject constructor(
    private val supplementRepository: SupplementRepository
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Supplement Library Operations
    fun getAllSupplements(): Flow<List<Supplement>> = 
        supplementRepository.getAllSupplements()
    
    fun getSupplementsByCategory(category: SupplementCategory): Flow<List<Supplement>> = 
        supplementRepository.getSupplementsByCategory(category)
    
    fun searchSupplements(query: String): Flow<List<Supplement>> = 
        supplementRepository.searchSupplements(query)
    
    suspend fun getSupplementById(id: String): Supplement? = 
        supplementRepository.getSupplementById(id)
    
    suspend fun createSupplement(
        name: String,
        brand: String?,
        description: String?,
        servingSize: String,
        servingUnit: String,
        category: SupplementCategory,
        nutrition: SupplementNutrition
    ): Result<String> {
        val supplement = Supplement(
            id = UUID.randomUUID().toString(),
            name = name,
            brand = brand,
            description = description,
            servingSize = servingSize,
            servingUnit = servingUnit,
            nutritionalInfo = json.encodeToString(nutrition),
            barcode = null,
            imageUrl = null,
            category = category,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
        
        return supplementRepository.saveSupplement(supplement)
    }
    
    suspend fun updateSupplement(supplement: Supplement): Result<Unit> = 
        supplementRepository.updateSupplement(supplement.copy(
            updatedAt = LocalDateTime.now().toString()
        ))
    
    suspend fun deleteSupplement(supplement: Supplement): Result<Unit> = 
        supplementRepository.deleteSupplement(supplement)
    
    suspend fun importSupplementFromBarcode(barcode: String): Result<Supplement> = 
        supplementRepository.importSupplementFromBarcode(barcode)
    
    // User Supplement Management
    fun getUserSupplements(userId: String): Flow<List<UserSupplementWithDetails>> = 
        supplementRepository.getActiveUserSupplements(userId)
    
    suspend fun addSupplementToUser(
        userId: String,
        supplementId: String,
        customName: String?,
        dosage: Double,
        dosageUnit: String,
        frequency: SupplementFrequency,
        scheduledTimes: List<SupplementSchedule>,
        notes: String?
    ): Result<String> {
        val userSupplement = UserSupplement(
            id = UUID.randomUUID().toString(),
            userId = userId,
            supplementId = supplementId,
            customName = customName,
            dosage = dosage,
            dosageUnit = dosageUnit,
            frequency = frequency,
            scheduledTimes = json.encodeToString(scheduledTimes),
            isActive = true,
            notes = notes,
            startDate = LocalDate.now().toString(),
            endDate = null,
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
        
        return supplementRepository.addUserSupplement(userSupplement)
    }
    
    suspend fun updateUserSupplement(
        userSupplementId: String,
        customName: String?,
        dosage: Double,
        dosageUnit: String,
        frequency: SupplementFrequency,
        scheduledTimes: List<SupplementSchedule>,
        notes: String?
    ): Result<Unit> {
        val existing = supplementRepository.getUserSupplementById(userSupplementId)
            ?: return Result.failure(Exception("User supplement not found"))
        
        val updated = existing.copy(
            customName = customName,
            dosage = dosage,
            dosageUnit = dosageUnit,
            frequency = frequency,
            scheduledTimes = json.encodeToString(scheduledTimes),
            notes = notes,
            updatedAt = LocalDateTime.now().toString()
        )
        
        return supplementRepository.updateUserSupplement(updated)
    }
    
    suspend fun removeSupplementFromUser(userSupplementId: String): Result<Unit> = 
        supplementRepository.deactivateUserSupplement(userSupplementId)
    
    // Supplement Intake Tracking
    fun getTodaySupplementIntakes(userId: String): Flow<List<SupplementIntakeWithDetails>> = 
        supplementRepository.getSupplementIntakesForDate(userId, LocalDate.now().toString())
    
    fun getSupplementIntakesForDateRange(
        userId: String, 
        startDate: LocalDate, 
        endDate: LocalDate
    ): Flow<List<SupplementIntakeWithDetails>> = 
        supplementRepository.getSupplementIntakesInDateRange(
            userId, 
            startDate.toString(), 
            endDate.toString()
        )
    
    suspend fun logSupplementIntake(
        userId: String,
        userSupplementId: String,
        actualDosage: Double,
        dosageUnit: String,
        notes: String?
    ): Result<String> {
        val intake = SupplementIntake(
            id = UUID.randomUUID().toString(),
            userId = userId,
            userSupplementId = userSupplementId,
            actualDosage = actualDosage,
            dosageUnit = dosageUnit,
            takenAt = LocalDateTime.now().toString(),
            scheduledAt = null,
            status = IntakeStatus.TAKEN,
            notes = notes,
            createdAt = LocalDateTime.now().toString()
        )
        
        return supplementRepository.logSupplementIntake(intake)
    }
    
    suspend fun markScheduledIntakeAsCompleted(
        intakeId: String,
        actualDosage: Double,
        notes: String?
    ): Result<Unit> = 
        supplementRepository.markIntakeAsCompleted(intakeId, actualDosage, notes)
    
    suspend fun markScheduledIntakeAsSkipped(
        intakeId: String,
        notes: String?
    ): Result<Unit> = 
        supplementRepository.markIntakeAsSkipped(intakeId, notes)
    
    suspend fun generateTodayScheduledIntakes(userId: String): Result<List<SupplementIntake>> = 
        supplementRepository.generateScheduledIntakes(userId, LocalDate.now().toString())
    
    // Analytics and Dashboard
    suspend fun getTodaySupplementSummary(userId: String): SupplementDailySummary = 
        supplementRepository.getTodaySupplementSummary(userId)
    
    suspend fun getSupplementAdherenceStats(
        userId: String,
        days: Int = 30
    ): List<SupplementAdherenceStats> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        return supplementRepository.getSupplementAdherenceStats(
            userId,
            startDate.toString(),
            endDate.toString()
        )
    }
    
    suspend fun getSupplementAdherenceByType(
        userId: String,
        days: Int = 30
    ): List<SupplementTypeAdherence> {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())
        
        return supplementRepository.getSupplementAdherenceByType(
            userId,
            startDate.toString(),
            endDate.toString()
        )
    }
    
    suspend fun getTodayNutritionalContribution(userId: String): SupplementNutrition = 
        supplementRepository.calculateNutritionalContribution(userId, LocalDate.now().toString())
    
    // Reminders and Scheduling
    suspend fun getUpcomingReminders(userId: String): List<SupplementReminder> = 
        supplementRepository.getUpcomingSupplementReminders(userId)
    
    suspend fun getMissedSupplements(userId: String): List<UserSupplementWithDetails> = 
        supplementRepository.getMissedSupplements(userId, LocalDate.now().toString())
    
    // Combined Dashboard Data
    fun getSupplementDashboardData(userId: String): Flow<SupplementDashboardData> {
        return combine(
            getUserSupplements(userId),
            getTodaySupplementIntakes(userId)
        ) { userSupplements, todayIntakes ->
            val summary = supplementRepository.getTodaySupplementSummary(userId)
            val upcomingReminders = supplementRepository.getUpcomingSupplementReminders(userId)
            val missedSupplements = supplementRepository.getMissedSupplements(userId, LocalDate.now().toString())
            
            SupplementDashboardData(
                userSupplements = userSupplements,
                todayIntakes = todayIntakes,
                summary = summary,
                upcomingReminders = upcomingReminders,
                missedSupplements = missedSupplements
            )
        }
    }
    
    // Helper functions for creating common supplement schedules
    fun createDailySchedule(times: List<String>): List<SupplementSchedule> {
        return times.map { time ->
            SupplementSchedule(
                time = time,
                label = when (time) {
                    in "06:00".."09:00" -> "Morning"
                    in "11:00".."14:00" -> "Lunch"
                    in "17:00".."20:00" -> "Dinner"
                    in "21:00".."23:00" -> "Bedtime"
                    else -> "Custom"
                }
            )
        }
    }
    
    fun createMealBasedSchedule(): List<SupplementSchedule> {
        return listOf(
            SupplementSchedule("08:00", "With breakfast"),
            SupplementSchedule("12:30", "With lunch"),
            SupplementSchedule("19:00", "With dinner")
        )
    }
    
    fun createWorkoutSchedule(): List<SupplementSchedule> {
        return listOf(
            SupplementSchedule("07:00", "Pre-workout"),
            SupplementSchedule("09:00", "Post-workout")
        )
    }
}

data class SupplementDashboardData(
    val userSupplements: List<UserSupplementWithDetails>,
    val todayIntakes: List<SupplementIntakeWithDetails>,
    val summary: SupplementDailySummary,
    val upcomingReminders: List<SupplementReminder>,
    val missedSupplements: List<UserSupplementWithDetails>
)