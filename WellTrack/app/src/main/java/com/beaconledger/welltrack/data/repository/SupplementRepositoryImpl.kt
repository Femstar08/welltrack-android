package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.SupplementDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.domain.repository.SupplementRepository
import com.beaconledger.welltrack.data.barcode.BarcodeService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Singleton
class SupplementRepositoryImpl @Inject constructor(
    private val supplementDao: SupplementDao,
    private val barcodeService: BarcodeService
) : SupplementRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // Supplement Library Management
    override fun getAllSupplements(): Flow<List<Supplement>> = supplementDao.getAllSupplements()
    
    override fun getSupplementsByCategory(category: SupplementCategory): Flow<List<Supplement>> = 
        supplementDao.getSupplementsByCategory(category)
    
    override fun searchSupplements(query: String): Flow<List<Supplement>> = 
        supplementDao.searchSupplements(query)
    
    override suspend fun getSupplementById(id: String): Supplement? = 
        supplementDao.getSupplementById(id)
    
    override suspend fun getSupplementByBarcode(barcode: String): Supplement? = 
        supplementDao.getSupplementByBarcode(barcode)
    
    override suspend fun saveSupplement(supplement: Supplement): Result<String> {
        return try {
            supplementDao.insertSupplement(supplement)
            Result.success(supplement.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSupplement(supplement: Supplement): Result<Unit> {
        return try {
            supplementDao.updateSupplement(supplement)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSupplement(supplement: Supplement): Result<Unit> {
        return try {
            supplementDao.deleteSupplement(supplement)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun importSupplementFromBarcode(barcode: String): Result<Supplement> {
        return try {
            val supplementInfo = barcodeService.getSupplementInfo(barcode)
                ?: return Result.failure(Exception("Could not retrieve supplement information"))
            
            val supplement = Supplement(
                id = UUID.randomUUID().toString(),
                name = supplementInfo.name,
                brand = supplementInfo.brand,
                description = supplementInfo.description,
                servingSize = supplementInfo.servingSize,
                servingUnit = supplementInfo.servingUnit,
                nutritionalInfo = json.encodeToString(supplementInfo.nutrition),
                barcode = barcode,
                imageUrl = supplementInfo.imageUrl,
                category = supplementInfo.category,
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString()
            )
            supplementDao.insertSupplement(supplement)
            Result.success(supplement)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // User Supplement Management
    override fun getActiveUserSupplements(userId: String): Flow<List<UserSupplementWithDetails>> = 
        supplementDao.getActiveUserSupplements(userId)
    
    override fun getAllUserSupplements(userId: String): Flow<List<UserSupplementWithDetails>> = 
        supplementDao.getAllUserSupplements(userId)
    
    override suspend fun getUserSupplementById(id: String): UserSupplement? = 
        supplementDao.getUserSupplementById(id)
    
    override suspend fun addUserSupplement(userSupplement: UserSupplement): Result<String> {
        return try {
            // Check if user already has this supplement active
            val existing = supplementDao.getActiveUserSupplementBySupplementId(
                userSupplement.userId, 
                userSupplement.supplementId
            )
            if (existing != null) {
                return Result.failure(Exception("Supplement already added to user's library"))
            }
            
            supplementDao.insertUserSupplement(userSupplement)
            Result.success(userSupplement.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateUserSupplement(userSupplement: UserSupplement): Result<Unit> {
        return try {
            supplementDao.updateUserSupplement(userSupplement.copy(
                updatedAt = LocalDateTime.now().toString()
            ))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deactivateUserSupplement(id: String): Result<Unit> {
        return try {
            supplementDao.deactivateUserSupplement(id, LocalDateTime.now().toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteAllUserSupplements(userId: String): Result<Unit> {
        return try {
            supplementDao.deleteAllUserSupplements(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Supplement Intake Tracking
    override fun getSupplementIntakesForDate(userId: String, date: String): Flow<List<SupplementIntakeWithDetails>> = 
        supplementDao.getSupplementIntakesForDate(userId, date)
    
    override fun getSupplementIntakesInDateRange(userId: String, startDate: String, endDate: String): Flow<List<SupplementIntakeWithDetails>> = 
        supplementDao.getSupplementIntakesInDateRange(userId, startDate, endDate)
    
    override suspend fun logSupplementIntake(intake: SupplementIntake): Result<String> {
        return try {
            supplementDao.insertSupplementIntake(intake)
            Result.success(intake.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateSupplementIntake(intake: SupplementIntake): Result<Unit> {
        return try {
            supplementDao.updateSupplementIntake(intake)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteSupplementIntake(intake: SupplementIntake): Result<Unit> {
        return try {
            supplementDao.deleteSupplementIntake(intake)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generateScheduledIntakes(userId: String, date: String): Result<List<SupplementIntake>> {
        return try {
            val userSupplements = supplementDao.getActiveUserSupplements(userId).first()
            val scheduledIntakes = mutableListOf<SupplementIntake>()
            
            for (userSupplement in userSupplements) {
                val schedules = json.decodeFromString<List<SupplementSchedule>>(userSupplement.scheduledTimes)
                
                for (schedule in schedules) {
                    val scheduledDateTime = "${date}T${schedule.time}:00"
                    
                    // Check if intake already exists for this time
                    val existingIntakes = supplementDao.getIntakesForUserSupplementOnDate(
                        userSupplement.id, 
                        date
                    )
                    
                    val alreadyScheduled = existingIntakes.any { 
                        it.scheduledAt == scheduledDateTime 
                    }
                    
                    if (!alreadyScheduled) {
                        scheduledIntakes.add(
                            SupplementIntake(
                                id = UUID.randomUUID().toString(),
                                userId = userId,
                                userSupplementId = userSupplement.id,
                                actualDosage = userSupplement.dosage,
                                dosageUnit = userSupplement.dosageUnit,
                                takenAt = scheduledDateTime,
                                scheduledAt = scheduledDateTime,
                                status = IntakeStatus.TAKEN,
                                notes = null,
                                createdAt = LocalDateTime.now().toString()
                            )
                        )
                    }
                }
            }
            
            if (scheduledIntakes.isNotEmpty()) {
                supplementDao.insertSupplementIntakes(scheduledIntakes)
            }
            
            Result.success(scheduledIntakes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markIntakeAsCompleted(intakeId: String, actualDosage: Double, notes: String?): Result<Unit> {
        return try {
            val intake = supplementDao.getUserSupplementById(intakeId)
                ?: return Result.failure(Exception("Intake not found"))
            
            // This should be getting the SupplementIntake, not UserSupplement
            // Let me fix this logic
            val existingIntakes = supplementDao.getIntakesForUserSupplementOnDate(
                intakeId, 
                LocalDate.now().toString()
            )
            
            if (existingIntakes.isNotEmpty()) {
                val intakeToUpdate = existingIntakes.first().copy(
                    actualDosage = actualDosage,
                    status = IntakeStatus.TAKEN,
                    notes = notes,
                    takenAt = LocalDateTime.now().toString()
                )
                supplementDao.updateSupplementIntake(intakeToUpdate)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markIntakeAsSkipped(intakeId: String, notes: String?): Result<Unit> {
        return try {
            val existingIntakes = supplementDao.getIntakesForUserSupplementOnDate(
                intakeId, 
                LocalDate.now().toString()
            )
            
            if (existingIntakes.isNotEmpty()) {
                val intakeToUpdate = existingIntakes.first().copy(
                    status = IntakeStatus.SKIPPED,
                    notes = notes
                )
                supplementDao.updateSupplementIntake(intakeToUpdate)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Analytics and Insights
    override suspend fun getSupplementAdherenceStats(userId: String, startDate: String, endDate: String): List<SupplementAdherenceStats> = 
        supplementDao.getSupplementAdherenceStats(userId, startDate, endDate)
    
    override suspend fun getSupplementAdherenceByType(userId: String, startDate: String, endDate: String): List<SupplementTypeAdherence> = 
        supplementDao.getSupplementAdherenceByType(userId, startDate, endDate)
    
    override suspend fun getTodaySupplementSummary(userId: String): SupplementDailySummary {
        val today = LocalDate.now().toString()
        val takenCount = supplementDao.getTakenSupplementCountForDate(userId, today)
        val totalCount = supplementDao.getTotalScheduledSupplementCountForDate(userId, today)
        
        return SupplementDailySummary(
            date = today,
            totalTaken = takenCount,
            totalScheduled = totalCount,
            totalMissed = totalCount - takenCount,
            adherencePercentage = if (totalCount > 0) (takenCount.toFloat() / totalCount.toFloat()) * 100f else 0f
        )
    }
    
    override suspend fun calculateNutritionalContribution(userId: String, date: String): SupplementNutrition {
        val intakes = supplementDao.getSupplementIntakesForDate(userId, date).first()
        val takenIntakes = intakes.filter { it.status == IntakeStatus.TAKEN }
        
        var totalNutrition = SupplementNutrition()
        
        for (intake in takenIntakes) {
            try {
                // Get nutrition info from the supplement, not the intake
                val supplement = supplementDao.getSupplementById(intake.userSupplementId)
                val nutrition = supplement?.let { 
                    json.decodeFromString<SupplementNutrition>(it.nutritionalInfo) 
                } ?: continue
                totalNutrition = totalNutrition.copy(
                    calories = (totalNutrition.calories ?: 0.0) + (nutrition.calories ?: 0.0),
                    protein = (totalNutrition.protein ?: 0.0) + (nutrition.protein ?: 0.0),
                    carbs = (totalNutrition.carbs ?: 0.0) + (nutrition.carbs ?: 0.0),
                    fat = (totalNutrition.fat ?: 0.0) + (nutrition.fat ?: 0.0),
                    fiber = (totalNutrition.fiber ?: 0.0) + (nutrition.fiber ?: 0.0),
                    sugar = (totalNutrition.sugar ?: 0.0) + (nutrition.sugar ?: 0.0),
                    sodium = (totalNutrition.sodium ?: 0.0) + (nutrition.sodium ?: 0.0),
                    potassium = (totalNutrition.potassium ?: 0.0) + (nutrition.potassium ?: 0.0),
                    calcium = (totalNutrition.calcium ?: 0.0) + (nutrition.calcium ?: 0.0),
                    iron = (totalNutrition.iron ?: 0.0) + (nutrition.iron ?: 0.0),
                    vitaminD = (totalNutrition.vitaminD ?: 0.0) + (nutrition.vitaminD ?: 0.0),
                    vitaminB12 = (totalNutrition.vitaminB12 ?: 0.0) + (nutrition.vitaminB12 ?: 0.0),
                    vitaminC = (totalNutrition.vitaminC ?: 0.0) + (nutrition.vitaminC ?: 0.0),
                    magnesium = (totalNutrition.magnesium ?: 0.0) + (nutrition.magnesium ?: 0.0),
                    zinc = (totalNutrition.zinc ?: 0.0) + (nutrition.zinc ?: 0.0),
                    omega3 = (totalNutrition.omega3 ?: 0.0) + (nutrition.omega3 ?: 0.0)
                )
            } catch (e: Exception) {
                // Skip if nutrition info can't be parsed
                continue
            }
        }
        
        return totalNutrition
    }
    
    // Scheduling and Reminders
    override suspend fun getUpcomingSupplementReminders(userId: String): List<SupplementReminder> {
        val userSupplements = supplementDao.getActiveUserSupplements(userId).first()
        val reminders = mutableListOf<SupplementReminder>()
        val currentTime = LocalTime.now()
        
        for (userSupplement in userSupplements) {
            try {
                val schedules = json.decodeFromString<List<SupplementSchedule>>(userSupplement.scheduledTimes)
                
                for (schedule in schedules) {
                    val scheduledTime = LocalTime.parse(schedule.time)
                    val isOverdue = currentTime.isAfter(scheduledTime)
                    
                    reminders.add(
                        SupplementReminder(
                            id = "${userSupplement.id}-${schedule.time}",
                            userSupplementId = userSupplement.id,
                            supplementName = userSupplement.supplementName,
                            dosage = userSupplement.dosage,
                            dosageUnit = userSupplement.dosageUnit,
                            scheduledTime = schedule.time,
                            isOverdue = isOverdue
                        )
                    )
                }
            } catch (e: Exception) {
                // Skip if schedule can't be parsed
                continue
            }
        }
        
        return reminders.sortedBy { it.scheduledTime }
    }
    
    override suspend fun getMissedSupplements(userId: String, date: String): List<UserSupplementWithDetails> {
        val userSupplements = supplementDao.getActiveUserSupplements(userId).first()
        val intakes = supplementDao.getSupplementIntakesForDate(userId, date).first()
        
        val takenSupplementIds = intakes
            .filter { it.status == IntakeStatus.TAKEN }
            .map { it.userSupplementId }
            .toSet()
        
        return userSupplements.filter { it.id !in takenSupplementIds }
    }
}