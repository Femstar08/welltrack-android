package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "custom_habits")
data class CustomHabit(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val targetFrequency: HabitFrequency,
    val targetValue: Int = 1, // e.g., 10 for "10 push-ups"
    val unit: String? = null, // e.g., "reps", "minutes", "glasses"
    val category: HabitCategory = HabitCategory.OTHER,
    val isActive: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey
    val id: String,
    val habitId: String,
    val userId: String,
    val completedValue: Int = 1,
    val completedAt: String = LocalDateTime.now().toString(),
    val notes: String? = null
)

enum class HabitFrequency(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    CUSTOM("Custom")
}

enum class HabitCategory(val displayName: String) {
    FITNESS("Fitness"),
    WELLNESS("Wellness"),
    NUTRITION("Nutrition"),
    MINDFULNESS("Mindfulness"),
    OTHER("Other")
}