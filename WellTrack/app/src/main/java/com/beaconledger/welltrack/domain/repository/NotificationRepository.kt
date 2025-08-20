package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface NotificationRepository {
    
    // Basic CRUD operations
    suspend fun createNotification(notification: NotificationEntity): Result<String>
    suspend fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>
    suspend fun getNotificationsByType(userId: String, type: NotificationType): Flow<List<NotificationEntity>>
    suspend fun updateNotification(notification: NotificationEntity): Result<Unit>
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    suspend fun deleteNotificationsByType(userId: String, type: NotificationType): Result<Unit>
    
    // Notification preferences
    suspend fun getUserNotificationPreferences(userId: String): NotificationPreferences?
    suspend fun updateNotificationPreferences(preferences: NotificationPreferences): Result<Unit>
    
    // Scheduling operations
    suspend fun scheduleNotification(notification: NotificationEntity): Result<Unit>
    suspend fun cancelScheduledNotification(notificationId: String): Result<Unit>
    suspend fun rescheduleNotification(notificationId: String, newTime: LocalDateTime): Result<Unit>
    
    // Meal reminder notifications
    suspend fun scheduleMealReminder(
        userId: String,
        mealType: String,
        scheduledTime: LocalDateTime,
        recipeName: String? = null,
        isRecurring: Boolean = false
    ): Result<String>
    
    // Supplement reminder notifications
    suspend fun scheduleSupplementReminder(
        userId: String,
        supplementName: String,
        dosage: String,
        scheduledTime: LocalDateTime,
        frequency: String,
        isRecurring: Boolean = true
    ): Result<String>
    
    // Pantry expiry notifications
    suspend fun schedulePantryExpiryAlert(
        userId: String,
        ingredientName: String,
        expiryDate: LocalDateTime,
        suggestedRecipes: List<String> = emptyList()
    ): Result<String>
    
    // Motivational notifications
    suspend fun scheduleMotivationalNotification(
        userId: String,
        goalType: String,
        currentProgress: Double,
        targetValue: Double,
        scheduledTime: LocalDateTime
    ): Result<String>
    
    // Notification actions
    suspend fun snoozeNotification(notificationId: String, snoozeMinutes: Int): Result<Unit>
    suspend fun markNotificationComplete(notificationId: String, itemId: String): Result<Unit>
    suspend fun dismissNotification(notificationId: String): Result<Unit>
    
    // Utility methods
    suspend fun shouldShowNotification(notificationId: String): Boolean
    suspend fun updateLastTriggered(notificationId: String, triggeredTime: LocalDateTime): Result<Unit>
    suspend fun scheduleNextRecurrence(notificationId: String): Result<Unit>
    suspend fun getPendingNotifications(): List<NotificationEntity>
    
    // Bulk operations
    suspend fun scheduleRecurringNotifications(userId: String): Result<Unit>
    suspend fun cancelAllNotificationsForUser(userId: String): Result<Unit>
    
    // Smart notification features
    suspend fun generateMotivationalMessage(goalType: String, progress: Double): String
    suspend fun findRecipesForExpiringIngredient(ingredientName: String): List<String>
    suspend fun optimizeNotificationTiming(userId: String, baseTime: LocalDateTime): LocalDateTime
}