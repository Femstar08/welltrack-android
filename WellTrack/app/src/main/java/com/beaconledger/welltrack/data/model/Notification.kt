package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val scheduledTime: LocalDateTime,
    val isRecurring: Boolean = false,
    val recurringPattern: String? = null, // JSON for recurring pattern
    val isEnabled: Boolean = true,
    val metadata: String? = null, // JSON for additional data
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val lastTriggered: LocalDateTime? = null
)

enum class NotificationType {
    MEAL_REMINDER,
    SUPPLEMENT_REMINDER,
    PANTRY_EXPIRY_ALERT,
    MOTIVATIONAL_HEALTH_GOAL,
    WATER_REMINDER,
    BLOOD_TEST_REMINDER,
    MEAL_PREP_REMINDER
}

data class NotificationPreferences(
    val userId: String,
    val mealRemindersEnabled: Boolean = true,
    val supplementRemindersEnabled: Boolean = true,
    val pantryAlertsEnabled: Boolean = true,
    val motivationalNotificationsEnabled: Boolean = true,
    val waterRemindersEnabled: Boolean = true,
    val bloodTestRemindersEnabled: Boolean = true,
    val mealPrepRemindersEnabled: Boolean = true,
    val quietHoursStart: String? = null, // HH:mm format
    val quietHoursEnd: String? = null, // HH:mm format
    val snoozeMinutes: Int = 15
)

data class MealReminderData(
    val mealType: String,
    val recipeName: String? = null,
    val plannedTime: LocalDateTime
)

data class SupplementReminderData(
    val supplementName: String,
    val dosage: String,
    val frequency: String
)

data class PantryExpiryData(
    val ingredientName: String,
    val expiryDate: LocalDateTime,
    val suggestedRecipes: List<String> = emptyList()
)

data class MotivationalData(
    val goalType: String,
    val currentProgress: Double,
    val targetValue: Double,
    val encouragementMessage: String
)

sealed class NotificationAction {
    object Dismiss : NotificationAction()
    data class Snooze(val minutes: Int) : NotificationAction()
    data class MarkComplete(val itemId: String) : NotificationAction()
    data class ViewRecipes(val ingredientName: String) : NotificationAction()
    data class OpenMealPlanner(val date: LocalDateTime) : NotificationAction()
}