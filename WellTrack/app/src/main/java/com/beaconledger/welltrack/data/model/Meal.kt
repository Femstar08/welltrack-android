package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey
    val id: String,
    val userId: String,
    val recipeId: String? = null,
    val timestamp: String,
    val mealType: MealType,
    val portions: Float = 1.0f,
    val nutritionInfo: String, // JSON string of NutritionInfo
    val score: MealScore,
    val status: MealStatus = MealStatus.PLANNED,
    val notes: String? = null,
    val rating: Float? = null, // 1-5 star rating
    val isFavorite: Boolean = false
)

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
    SUPPLEMENT
}

enum class MealScore(val grade: String, val colorCode: String) {
    A("A", "#4CAF50"), // Green
    B("B", "#8BC34A"), // Light Green
    C("C", "#FFEB3B"), // Yellow
    D("D", "#FF9800"), // Orange
    E("E", "#F44336")  // Red
}

enum class MealStatus {
    PLANNED,
    EATEN,
    SKIPPED
}