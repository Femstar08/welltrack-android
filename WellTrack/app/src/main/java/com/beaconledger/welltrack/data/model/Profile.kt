package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey
    val userId: String,
    val name: String,
    val profilePhotoUrl: String? = null,
    val age: Int? = null,
    val height: Float? = null, // in cm
    val weight: Float? = null, // in kg
    val activityLevel: ActivityLevel = ActivityLevel.MODERATE,
    val fitnessGoals: String = "", // JSON string of List<FitnessGoal>
    val dietaryRestrictions: String = "", // JSON string of List<DietaryRestriction>
    val allergies: String = "", // JSON string of List<String>
    val preferredIngredients: String = "", // JSON string of List<String>
    val dislikedIngredients: String = "", // JSON string of List<String>
    val cuisinePreferences: String = "", // JSON string of List<String>
    val cookingMethods: String = "", // JSON string of List<String>
    val notificationSettings: String = "", // JSON string of NotificationSettings
    val createdAt: String,
    val updatedAt: String
)

enum class ActivityLevel(val displayName: String, val multiplier: Float) {
    SEDENTARY("Sedentary (little/no exercise)", 1.2f),
    LIGHT("Light (light exercise 1-3 days/week)", 1.375f),
    MODERATE("Moderate (moderate exercise 3-5 days/week)", 1.55f),
    MODERATELY_ACTIVE("Moderately Active (moderate exercise 3-5 days/week)", 1.55f),
    ACTIVE("Active (hard exercise 6-7 days/week)", 1.725f),
    VERY_ACTIVE("Very Active (very hard exercise, physical job)", 1.9f)
}

data class ProfileCreationRequest(
    val name: String,
    val age: Int?,
    val height: Float?,
    val weight: Float?,
    val activityLevel: ActivityLevel,
    val fitnessGoals: List<FitnessGoal>,
    val dietaryRestrictions: List<DietaryRestriction>,
    val allergies: List<String>,
    val profilePhotoUri: String? = null
)

data class ProfileUpdateRequest(
    val name: String?,
    val age: Int?,
    val height: Float?,
    val weight: Float?,
    val activityLevel: ActivityLevel?,
    val fitnessGoals: List<FitnessGoal>?,
    val dietaryRestrictions: List<DietaryRestriction>?,
    val allergies: List<String>?,
    val preferredIngredients: List<String>?,
    val dislikedIngredients: List<String>?,
    val cuisinePreferences: List<String>?,
    val cookingMethods: List<String>?,
    val profilePhotoUri: String? = null
)

sealed class ProfileResult {
    object Loading : ProfileResult()
    data class Success(val profile: UserProfile) : ProfileResult()
    data class Error(val message: String) : ProfileResult()
}