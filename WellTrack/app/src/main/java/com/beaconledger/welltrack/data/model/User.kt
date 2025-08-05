package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val profilePhoto: String? = null,
    val age: Int? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

data class UserPreferences(
    val fitnessGoals: List<FitnessGoal> = emptyList(),
    val dietaryRestrictions: List<DietaryRestriction> = emptyList(),
    val preferredIngredients: List<String> = emptyList(),
    val notificationSettings: NotificationSettings = NotificationSettings()
)

enum class FitnessGoal {
    WEIGHT_LOSS,
    MUSCLE_GAIN,
    MAINTENANCE,
    ENDURANCE,
    STRENGTH,
    VO2_MAX_IMPROVEMENT
}

enum class DietaryRestriction {
    VEGETARIAN,
    VEGAN,
    GLUTEN_FREE,
    KETO,
    HIGH_PROTEIN,
    LOW_CARB,
    FLEXITARIAN,
    PESCATARIAN,
    CALORIE_CONSCIOUS
}

data class NotificationSettings(
    val mealReminders: Boolean = true,
    val supplementReminders: Boolean = true,
    val pantryAlerts: Boolean = true,
    val healthGoalAlerts: Boolean = true
)