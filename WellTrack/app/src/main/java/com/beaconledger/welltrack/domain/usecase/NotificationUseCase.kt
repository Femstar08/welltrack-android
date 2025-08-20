package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.NotificationRepository
import com.beaconledger.welltrack.domain.repository.MealPlanRepository
import com.beaconledger.welltrack.domain.repository.SupplementRepository
import com.beaconledger.welltrack.domain.repository.PantryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val mealPlanRepository: MealPlanRepository,
    private val supplementRepository: SupplementRepository,
    private val pantryRepository: PantryRepository
) {
    
    suspend fun getUserNotifications(userId: String): Flow<List<NotificationEntity>> {
        return notificationRepository.getNotificationsForUser(userId)
    }
    
    suspend fun getNotificationPreferences(userId: String): NotificationPreferences {
        return notificationRepository.getUserNotificationPreferences(userId) 
            ?: NotificationPreferences(userId)
    }
    
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit> {
        return notificationRepository.updateNotificationPreferences(preferences)
    }
    
    suspend fun setupDailyMealReminders(userId: String): Result<Unit> {
        return try {
            // Clear existing meal reminders
            notificationRepository.deleteNotificationsByType(userId, NotificationType.MEAL_REMINDER)
            
            val preferences = getNotificationPreferences(userId)
            if (!preferences.mealRemindersEnabled) {
                return Result.success(Unit)
            }
            
            // Schedule reminders for typical meal times
            val mealTimes = mapOf(
                "Breakfast" to LocalTime.of(8, 0),
                "Lunch" to LocalTime.of(12, 30),
                "Dinner" to LocalTime.of(18, 30),
                "Snack" to LocalTime.of(15, 0)
            )
            
            val today = LocalDateTime.now().toLocalDate()
            mealTimes.forEach { (mealType, time) ->
                val scheduledTime = today.atTime(time)
                if (scheduledTime.isAfter(LocalDateTime.now())) {
                    notificationRepository.scheduleMealReminder(
                        userId = userId,
                        mealType = mealType,
                        scheduledTime = scheduledTime,
                        isRecurring = true
                    )
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setupSupplementReminders(userId: String): Result<Unit> {
        return try {
            // Clear existing supplement reminders
            notificationRepository.deleteNotificationsByType(userId, NotificationType.SUPPLEMENT_REMINDER)
            
            val preferences = getNotificationPreferences(userId)
            if (!preferences.supplementRemindersEnabled) {
                return Result.success(Unit)
            }
            
            // Get user's supplements and create reminders
            supplementRepository.getActiveUserSupplements(userId).collect { supplements ->
                supplements.forEach { supplement ->
                    // Schedule based on supplement frequency
                    val times = getSupplementReminderTimes(supplement.frequency.toString())
                    times.forEach { time ->
                        val scheduledTime = LocalDateTime.now().toLocalDate().atTime(time)
                        if (scheduledTime.isAfter(LocalDateTime.now())) {
                            notificationRepository.scheduleSupplementReminder(
                                userId = userId,
                                supplementName = supplement.customName ?: supplement.supplementName,
                                dosage = "${supplement.dosage} ${supplement.dosageUnit}",
                                scheduledTime = scheduledTime,
                                frequency = supplement.frequency.toString(),
                                isRecurring = true
                            )
                        }
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun setupPantryExpiryAlerts(userId: String): Result<Unit> {
        return try {
            // Clear existing pantry alerts
            notificationRepository.deleteNotificationsByType(userId, NotificationType.PANTRY_EXPIRY_ALERT)
            
            val preferences = getNotificationPreferences(userId)
            if (!preferences.pantryAlertsEnabled) {
                return Result.success(Unit)
            }
            
            // Get pantry items expiring in the next 7 days
            pantryRepository.getExpiringItems(userId, 7).collect { expiringItems ->
                expiringItems.forEach { item ->
                    val suggestedRecipes = notificationRepository.findRecipesForExpiringIngredient(item.ingredientName)
                    
                    // Convert string to LocalDateTime
                    val expiryDateTime = item.expiryDate?.let { 
                        LocalDateTime.parse(it) 
                    } ?: LocalDateTime.now().plusDays(1)
                    
                    notificationRepository.schedulePantryExpiryAlert(
                        userId = userId,
                        ingredientName = item.ingredientName,
                        expiryDate = expiryDateTime,
                        suggestedRecipes = suggestedRecipes
                    )
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun scheduleMotivationalNotifications(userId: String): Result<Unit> {
        return try {
            val preferences = getNotificationPreferences(userId)
            if (!preferences.motivationalNotificationsEnabled) {
                return Result.success(Unit)
            }
            
            // Schedule motivational notifications for evening review
            val eveningTime = LocalDateTime.now().toLocalDate().atTime(20, 0)
            if (eveningTime.isAfter(LocalDateTime.now())) {
                notificationRepository.scheduleMotivationalNotification(
                    userId = userId,
                    goalType = "Daily Progress",
                    currentProgress = 75.0, // This would be calculated from actual data
                    targetValue = 100.0,
                    scheduledTime = eveningTime
                )
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun snoozeNotification(notificationId: String, snoozeMinutes: Int = 15): Result<Unit> {
        return notificationRepository.snoozeNotification(notificationId, snoozeMinutes)
    }
    
    suspend fun markNotificationComplete(notificationId: String, itemId: String): Result<Unit> {
        return notificationRepository.markNotificationComplete(notificationId, itemId)
    }
    
    suspend fun dismissNotification(notificationId: String): Result<Unit> {
        return notificationRepository.dismissNotification(notificationId)
    }
    
    suspend fun setupAllNotifications(userId: String): Result<Unit> {
        return try {
            setupDailyMealReminders(userId)
            setupSupplementReminders(userId)
            setupPantryExpiryAlerts(userId)
            scheduleMotivationalNotifications(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun cancelAllNotifications(userId: String): Result<Unit> {
        return notificationRepository.cancelAllNotificationsForUser(userId)
    }
    
    private fun getSupplementReminderTimes(frequency: String): List<LocalTime> {
        return when (frequency.lowercase()) {
            "once daily", "daily" -> listOf(LocalTime.of(9, 0))
            "twice daily" -> listOf(LocalTime.of(9, 0), LocalTime.of(21, 0))
            "three times daily" -> listOf(LocalTime.of(9, 0), LocalTime.of(14, 0), LocalTime.of(21, 0))
            "with breakfast" -> listOf(LocalTime.of(8, 0))
            "with lunch" -> listOf(LocalTime.of(12, 30))
            "with dinner" -> listOf(LocalTime.of(18, 30))
            "before bed" -> listOf(LocalTime.of(22, 0))
            else -> listOf(LocalTime.of(9, 0)) // Default to morning
        }
    }
}