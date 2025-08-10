package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cooking_sessions")
data class CookingSession(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val userId: String,
    val scaledServings: Int,
    val scalingFactor: Float,
    val startedAt: String,
    val completedAt: String? = null,
    val currentStepIndex: Int = 0,
    val completedSteps: String = "[]", // JSON array of step indices
    val activeTimers: String = "[]", // JSON array of CookingTimer
    val status: CookingStatus = CookingStatus.IN_PROGRESS,
    val notes: String? = null
)

enum class CookingStatus {
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    CANCELLED
}

data class CookingTimer(
    val id: String,
    val stepNumber: Int,
    val name: String,
    val durationMinutes: Int,
    val startTime: Long,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false
)

data class ScaledRecipe(
    val recipe: Recipe,
    val scaledIngredients: List<ScaledIngredient>,
    val scaledSteps: List<CookingStep>,
    val scalingFactor: Float,
    val targetServings: Int
)

data class ScaledIngredient(
    val name: String,
    val originalQuantity: Double,
    val scaledQuantity: Double,
    val unit: String,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val isOptional: Boolean = false,
    val notes: String? = null
)

data class CookingStep(
    val stepNumber: Int,
    val instruction: String,
    val duration: Int? = null, // in minutes
    val temperature: String? = null,
    val equipment: List<String> = emptyList(),
    val scaledInstruction: String = instruction
)