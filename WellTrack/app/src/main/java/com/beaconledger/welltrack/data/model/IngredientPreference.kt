package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "ingredient_preferences")
data class IngredientPreference(
    @PrimaryKey
    val id: String,
    val userId: String,
    val ingredientName: String,
    val preferenceType: PreferenceType,
    val priority: Int = 0, // Higher number = higher priority
    val notes: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey
    val id: String,
    val userId: String,
    val ingredientName: String,
    val quantity: Double,
    val unit: String,
    val category: IngredientCategory,
    val purchaseDate: String? = null,
    val expiryDate: String? = null,
    val barcode: String? = null,
    val location: String? = null, // e.g., "Fridge", "Pantry", "Freezer"
    val isLowStock: Boolean = false,
    val minimumQuantity: Double? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "ingredient_usage_history")
data class IngredientUsageHistory(
    @PrimaryKey
    val id: String,
    val userId: String,
    val ingredientName: String,
    val recipeId: String?,
    val mealId: String?,
    val quantityUsed: Double,
    val unit: String,
    val usageDate: String,
    val usageType: UsageType,
    val createdAt: String = LocalDateTime.now().toString()
)

enum class PreferenceType(val displayName: String) {
    PREFERRED("Preferred"),
    DISLIKED("Disliked"),
    ALLERGIC("Allergic"),
    NEUTRAL("Neutral")
}

enum class UsageType(val displayName: String) {
    RECIPE("Recipe"),
    MEAL_LOG("Meal Log"),
    SNACK("Snack"),
    COOKING("Cooking")
}

data class IngredientPreferenceRequest(
    val ingredientName: String,
    val preferenceType: PreferenceType,
    val priority: Int = 0,
    val notes: String? = null
)

data class PantryItemRequest(
    val ingredientName: String,
    val quantity: Double,
    val unit: String,
    val category: IngredientCategory,
    val purchaseDate: String? = null,
    val expiryDate: String? = null,
    val barcode: String? = null,
    val location: String? = null,
    val minimumQuantity: Double? = null
)

data class PantryUpdateRequest(
    val quantity: Double?,
    val unit: String?,
    val expiryDate: String?,
    val location: String?,
    val isLowStock: Boolean?,
    val minimumQuantity: Double?
)

data class IngredientSuggestion(
    val ingredientName: String,
    val category: IngredientCategory,
    val isPreferred: Boolean,
    val isInPantry: Boolean,
    val pantryQuantity: Double?,
    val pantryUnit: String?,
    val usageFrequency: Int,
    val lastUsed: String?,
    val priority: Int
)

data class PantryAlert(
    val id: String,
    val ingredientName: String,
    val alertType: AlertType,
    val message: String,
    val severity: AlertSeverity,
    val expiryDate: String?,
    val daysUntilExpiry: Int?,
    val currentQuantity: Double?,
    val minimumQuantity: Double?
)

enum class AlertType(val displayName: String) {
    EXPIRY_WARNING("Expiry Warning"),
    EXPIRED("Expired"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock")
}

enum class AlertSeverity(val displayName: String, val color: String) {
    LOW("Low", "#FFA726"), // Orange
    MEDIUM("Medium", "#FF7043"), // Deep Orange  
    HIGH("High", "#F44336"), // Red
    CRITICAL("Critical", "#D32F2F") // Dark Red
}

data class IngredientUsageStats(
    val ingredientName: String,
    val usageCount: Int,
    val lastUsed: String? = null,
    val firstUsed: String? = null,
    val totalQuantityUsed: Double? = null,
    val averageQuantityPerUse: Double? = null,
    val mostCommonUnit: String? = null
)