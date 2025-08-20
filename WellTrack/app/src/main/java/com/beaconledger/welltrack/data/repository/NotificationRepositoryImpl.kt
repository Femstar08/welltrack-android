package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.NotificationDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.notification.NotificationScheduler
import com.beaconledger.welltrack.domain.repository.NotificationRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val recipeDao: RecipeDao,
    private val notificationScheduler: NotificationScheduler
) : NotificationRepository {
    
    private val gson = Gson()
    
    // Store user preferences in memory for quick access
    private val userPreferences = mutableMapOf<String, NotificationPreferences>()
    
    override suspend fun createNotification(notification: NotificationEntity): Result<String> {
        return try {
            notificationDao.insertNotification(notification)
            Result.success(notification.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsForUser(userId)
    }
    
    override suspend fun getNotificationsByType(userId: String, type: NotificationType): Flow<List<NotificationEntity>> {
        return notificationDao.getNotificationsByType(userId, type)
    }
    
    override suspend fun updateNotification(notification: NotificationEntity): Result<Unit> {
        return try {
            notificationDao.updateNotification(notification)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationDao.deleteNotificationById(notificationId)
            notificationScheduler.cancelNotification(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNotificationsByType(userId: String, type: NotificationType): Result<Unit> {
        return try {
            notificationDao.deleteNotificationsByType(userId, type)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getUserNotificationPreferences(userId: String): NotificationPreferences? {
        return userPreferences[userId] ?: NotificationPreferences(userId)
    }
    
    override suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit> {
        return try {
            userPreferences[preferences.userId] = preferences
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun scheduleNotification(notification: NotificationEntity): Result<Unit> {
        return try {
            notificationDao.insertNotification(notification)
            notificationScheduler.scheduleNotification(notification)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelScheduledNotification(notificationId: String): Result<Unit> {
        return try {
            notificationScheduler.cancelNotification(notificationId)
            notificationDao.updateNotificationEnabled(notificationId, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun rescheduleNotification(notificationId: String, newTime: LocalDateTime): Result<Unit> {
        return try {
            val notification = notificationDao.getNotificationById(notificationId)
            if (notification != null) {
                val updatedNotification = notification.copy(scheduledTime = newTime)
                notificationDao.updateNotification(updatedNotification)
                notificationScheduler.rescheduleNotification(updatedNotification, newTime)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun scheduleMealReminder(
        userId: String,
        mealType: String,
        scheduledTime: LocalDateTime,
        recipeName: String?,
        isRecurring: Boolean
    ): Result<String> {
        val notificationId = UUID.randomUUID().toString()
        val mealData = MealReminderData(mealType, recipeName, scheduledTime)
        
        val notification = NotificationEntity(
            id = notificationId,
            userId = userId,
            type = NotificationType.MEAL_REMINDER,
            title = "Meal Reminder",
            message = "Time for ${mealType.lowercase()}${recipeName?.let { " - $it" } ?: ""}",
            scheduledTime = scheduledTime,
            isRecurring = isRecurring,
            metadata = gson.toJson(mealData)
        )
        
        return scheduleNotification(notification).map { notificationId }
    }
    
    override suspend fun scheduleSupplementReminder(
        userId: String,
        supplementName: String,
        dosage: String,
        scheduledTime: LocalDateTime,
        frequency: String,
        isRecurring: Boolean
    ): Result<String> {
        val notificationId = UUID.randomUUID().toString()
        val supplementData = SupplementReminderData(supplementName, dosage, frequency)
        
        val notification = NotificationEntity(
            id = notificationId,
            userId = userId,
            type = NotificationType.SUPPLEMENT_REMINDER,
            title = "Supplement Reminder",
            message = "Time to take $supplementName ($dosage)",
            scheduledTime = scheduledTime,
            isRecurring = isRecurring,
            metadata = gson.toJson(supplementData)
        )
        
        return scheduleNotification(notification).map { notificationId }
    }
    
    override suspend fun schedulePantryExpiryAlert(
        userId: String,
        ingredientName: String,
        expiryDate: LocalDateTime,
        suggestedRecipes: List<String>
    ): Result<String> {
        val notificationId = UUID.randomUUID().toString()
        val pantryData = PantryExpiryData(ingredientName, expiryDate, suggestedRecipes)
        
        // Schedule notification 1 day before expiry
        val alertTime = expiryDate.minusDays(1)
        
        val notification = NotificationEntity(
            id = notificationId,
            userId = userId,
            type = NotificationType.PANTRY_EXPIRY_ALERT,
            title = "Ingredient Expiring Soon",
            message = "$ingredientName expires tomorrow${if (suggestedRecipes.isNotEmpty()) " - Check recipe suggestions!" else ""}",
            scheduledTime = alertTime,
            isRecurring = false,
            metadata = gson.toJson(pantryData)
        )
        
        return scheduleNotification(notification).map { notificationId }
    }
    
    override suspend fun scheduleMotivationalNotification(
        userId: String,
        goalType: String,
        currentProgress: Double,
        targetValue: Double,
        scheduledTime: LocalDateTime
    ): Result<String> {
        val notificationId = UUID.randomUUID().toString()
        val encouragementMessage = generateMotivationalMessage(goalType, currentProgress / targetValue)
        val motivationalData = MotivationalData(goalType, currentProgress, targetValue, encouragementMessage)
        
        val notification = NotificationEntity(
            id = notificationId,
            userId = userId,
            type = NotificationType.MOTIVATIONAL_HEALTH_GOAL,
            title = "Health Goal Update",
            message = encouragementMessage,
            scheduledTime = scheduledTime,
            isRecurring = false,
            metadata = gson.toJson(motivationalData)
        )
        
        return scheduleNotification(notification).map { notificationId }
    }
    
    override suspend fun snoozeNotification(notificationId: String, snoozeMinutes: Int): Result<Unit> {
        val newTime = LocalDateTime.now().plusMinutes(snoozeMinutes.toLong())
        return rescheduleNotification(notificationId, newTime)
    }
    
    override suspend fun markNotificationComplete(notificationId: String, itemId: String): Result<Unit> {
        return try {
            // Mark notification as completed and disable it
            notificationDao.updateNotificationEnabled(notificationId, false)
            notificationDao.updateLastTriggered(notificationId, LocalDateTime.now())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun dismissNotification(notificationId: String): Result<Unit> {
        return try {
            notificationDao.updateLastTriggered(notificationId, LocalDateTime.now())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun shouldShowNotification(notificationId: String): Boolean {
        val notification = notificationDao.getNotificationById(notificationId) ?: return false
        val preferences = getUserNotificationPreferences(notification.userId) ?: return true
        
        // Check if notification type is enabled
        val isTypeEnabled = when (notification.type) {
            NotificationType.MEAL_REMINDER -> preferences.mealRemindersEnabled
            NotificationType.SUPPLEMENT_REMINDER -> preferences.supplementRemindersEnabled
            NotificationType.PANTRY_EXPIRY_ALERT -> preferences.pantryAlertsEnabled
            NotificationType.MOTIVATIONAL_HEALTH_GOAL -> preferences.motivationalNotificationsEnabled
            NotificationType.WATER_REMINDER -> preferences.waterRemindersEnabled
            NotificationType.BLOOD_TEST_REMINDER -> preferences.bloodTestRemindersEnabled
            NotificationType.MEAL_PREP_REMINDER -> preferences.mealPrepRemindersEnabled
        }
        
        if (!isTypeEnabled) return false
        
        // Check quiet hours
        val currentTime = LocalTime.now()
        val quietStart = preferences.quietHoursStart?.let { LocalTime.parse(it) }
        val quietEnd = preferences.quietHoursEnd?.let { LocalTime.parse(it) }
        
        if (quietStart != null && quietEnd != null) {
            val isInQuietHours = if (quietStart.isBefore(quietEnd)) {
                currentTime.isAfter(quietStart) && currentTime.isBefore(quietEnd)
            } else {
                currentTime.isAfter(quietStart) || currentTime.isBefore(quietEnd)
            }
            if (isInQuietHours) return false
        }
        
        return true
    }
    
    override suspend fun updateLastTriggered(notificationId: String, triggeredTime: LocalDateTime): Result<Unit> {
        return try {
            notificationDao.updateLastTriggered(notificationId, triggeredTime)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun scheduleNextRecurrence(notificationId: String): Result<Unit> {
        return try {
            val notification = notificationDao.getNotificationById(notificationId)
            if (notification?.isRecurring == true) {
                // Calculate next occurrence based on recurring pattern
                val nextTime = calculateNextRecurrence(notification)
                if (nextTime != null) {
                    val nextNotification = notification.copy(
                        id = UUID.randomUUID().toString(),
                        scheduledTime = nextTime,
                        lastTriggered = null
                    )
                    scheduleNotification(nextNotification)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPendingNotifications(): List<NotificationEntity> {
        return notificationDao.getPendingNotifications(LocalDateTime.now())
    }
    
    override suspend fun scheduleRecurringNotifications(userId: String): Result<Unit> {
        return try {
            val recurringNotifications = notificationDao.getRecurringNotifications()
            notificationScheduler.scheduleRecurringNotifications(recurringNotifications)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cancelAllNotificationsForUser(userId: String): Result<Unit> {
        return try {
            notificationDao.deleteAllNotificationsForUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun generateMotivationalMessage(goalType: String, progress: Double): String {
        val progressPercent = (progress * 100).toInt()
        
        return when {
            progressPercent >= 90 -> when (goalType.lowercase()) {
                "protein" -> "Amazing! You're crushing your protein goals! 💪"
                "water" -> "Fantastic hydration today! Keep it flowing! 💧"
                "steps" -> "You're on fire with those steps! Almost there! 🔥"
                "fiber" -> "Excellent fiber intake! Your gut health is thanking you! 🌱"
                else -> "Outstanding progress on your $goalType goal! You're almost there! 🎯"
            }
            progressPercent >= 70 -> when (goalType.lowercase()) {
                "protein" -> "Great protein progress! A little more to hit your target! 🥩"
                "water" -> "Good hydration so far! Don't forget to keep sipping! 💧"
                "steps" -> "Nice step count! Keep moving to reach your goal! 👟"
                "fiber" -> "Good fiber intake! Add some more veggies or fruits! 🥬"
                else -> "Great work on your $goalType goal! You're doing well! 👍"
            }
            progressPercent >= 50 -> when (goalType.lowercase()) {
                "protein" -> "Halfway to your protein goal! Time for a protein-rich snack? 🥜"
                "water" -> "You're halfway there! Remember to stay hydrated! 💧"
                "steps" -> "Good start on steps! Maybe take a walk? 🚶"
                "fiber" -> "Decent fiber so far! Consider adding some whole grains! 🌾"
                else -> "You're halfway to your $goalType goal! Keep it up! 📈"
            }
            else -> when (goalType.lowercase()) {
                "protein" -> "Let's boost that protein intake! Your muscles will thank you! 💪"
                "water" -> "Time to hydrate! Your body needs more water today! 💧"
                "steps" -> "Let's get moving! Every step counts towards your goal! 🏃"
                "fiber" -> "Your digestive system needs more fiber! Try some fruits or veggies! 🍎"
                else -> "Let's work on that $goalType goal! You've got this! 🌟"
            }
        }
    }
    
    override suspend fun findRecipesForExpiringIngredient(ingredientName: String): List<String> {
        return try {
            recipeDao.findRecipesByIngredient(ingredientName).take(3).map { it.name }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun optimizeNotificationTiming(userId: String, baseTime: LocalDateTime): LocalDateTime {
        val preferences = getUserNotificationPreferences(userId)
        
        // If in quiet hours, schedule for after quiet hours end
        val quietEnd = preferences?.quietHoursEnd?.let { LocalTime.parse(it) }
        if (quietEnd != null) {
            val currentTime = LocalTime.now()
            val quietStart = preferences.quietHoursStart?.let { LocalTime.parse(it) }
            
            if (quietStart != null) {
                val isInQuietHours = if (quietStart.isBefore(quietEnd)) {
                    currentTime.isAfter(quietStart) && currentTime.isBefore(quietEnd)
                } else {
                    currentTime.isAfter(quietStart) || currentTime.isBefore(quietEnd)
                }
                
                if (isInQuietHours) {
                    return baseTime.with(quietEnd)
                }
            }
        }
        
        return baseTime
    }
    
    private fun calculateNextRecurrence(notification: NotificationEntity): LocalDateTime? {
        // Simple daily recurrence for now
        return when (notification.type) {
            NotificationType.MEAL_REMINDER -> notification.scheduledTime.plusDays(1)
            NotificationType.SUPPLEMENT_REMINDER -> notification.scheduledTime.plusDays(1)
            NotificationType.WATER_REMINDER -> notification.scheduledTime.plusHours(2)
            else -> null
        }
    }
}