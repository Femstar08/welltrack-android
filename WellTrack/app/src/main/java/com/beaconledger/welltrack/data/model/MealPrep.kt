package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Data models for meal preparation guidance system
 */

@Entity(tableName = "meal_prep_instructions")
data class MealPrepInstruction(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val prepSteps: String, // JSON string of List<PrepStep>
    val cookingMethods: String, // JSON string of List<CookingMethod>
    val timingGuidance: String, // JSON string of TimingGuidance
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "storage_recommendations")
data class StorageRecommendation(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val containerTypes: String, // JSON string of List<ContainerType>
    val refrigerationGuidelines: String, // JSON string of RefrigerationGuideline
    val freezingInstructions: String, // JSON string of FreezingInstruction
    val shelfLife: Int, // days
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "leftovers")
data class Leftover(
    @PrimaryKey
    val id: String,
    val userId: String,
    val mealId: String,
    val recipeId: String?,
    val name: String,
    val quantity: Double,
    val unit: String,
    val storageDate: String, // LocalDateTime as string
    val expiryDate: String, // LocalDateTime as string
    val storageLocation: StorageLocation,
    val containerType: String,
    val nutritionInfo: String, // JSON string of NutritionInfo
    val notes: String? = null,
    val isConsumed: Boolean = false,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "leftover_combinations")
data class LeftoverCombination(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val leftoverIds: String, // JSON string of List<String>
    val reheatingInstructions: String, // JSON string of ReheatingInstruction
    val additionalIngredients: String, // JSON string of List<Ingredient>
    val prepTime: Int, // minutes
    val servings: Int,
    val nutritionInfo: String, // JSON string of NutritionInfo
    val createdAt: String = LocalDateTime.now().toString()
)

// Data classes for JSON serialization
data class PrepStep(
    val stepNumber: Int,
    val description: String,
    val duration: Int? = null, // minutes
    val equipment: List<String> = emptyList(),
    val tips: List<String> = emptyList()
)

data class CookingMethod(
    val method: CookingMethodType,
    val temperature: String? = null,
    val duration: Int? = null, // minutes
    val instructions: String,
    val benefits: List<String> = emptyList()
)

data class TimingGuidance(
    val totalPrepTime: Int, // minutes
    val activeTime: Int, // minutes
    val passiveTime: Int, // minutes
    val optimalSchedule: List<ScheduleStep>,
    val makeAheadTips: List<String> = emptyList()
)

data class ScheduleStep(
    val stepNumber: Int,
    val description: String,
    val timeOffset: Int, // minutes from start
    val duration: Int, // minutes
    val canBeDoneAhead: Boolean = false
)

data class ContainerType(
    val type: ContainerTypeEnum,
    val size: String,
    val material: String,
    val suitableFor: List<String>,
    val notes: String? = null
)

data class RefrigerationGuideline(
    val temperature: String,
    val maxDuration: Int, // days
    val storageInstructions: String,
    val qualityIndicators: List<String>
)

data class FreezingInstruction(
    val isFreezable: Boolean,
    val maxDuration: Int, // days
    val freezingInstructions: String,
    val thawingInstructions: String,
    val qualityNotes: String? = null
)

data class ReheatingInstruction(
    val method: ReheatingMethod,
    val temperature: String? = null,
    val duration: String,
    val instructions: String,
    val safetyNotes: List<String> = emptyList()
)

// Enums
enum class CookingMethodType(val displayName: String) {
    BAKING("Baking"),
    ROASTING("Roasting"),
    GRILLING("Grilling"),
    SAUTEING("Sautéing"),
    STEAMING("Steaming"),
    BOILING("Boiling"),
    SLOW_COOKING("Slow Cooking"),
    PRESSURE_COOKING("Pressure Cooking"),
    AIR_FRYING("Air Frying"),
    BROILING("Broiling")
}

enum class ContainerTypeEnum(val displayName: String) {
    GLASS_CONTAINER("Glass Container"),
    PLASTIC_CONTAINER("Plastic Container"),
    VACUUM_SEALED_BAG("Vacuum Sealed Bag"),
    FREEZER_BAG("Freezer Bag"),
    ALUMINUM_FOIL("Aluminum Foil"),
    PARCHMENT_PAPER("Parchment Paper"),
    MASON_JAR("Mason Jar"),
    SILICONE_CONTAINER("Silicone Container")
}

enum class StorageLocation(val displayName: String) {
    REFRIGERATOR("Refrigerator"),
    FREEZER("Freezer"),
    PANTRY("Pantry"),
    COUNTER("Counter")
}

enum class ReheatingMethod(val displayName: String) {
    MICROWAVE("Microwave"),
    OVEN("Oven"),
    STOVETOP("Stovetop"),
    AIR_FRYER("Air Fryer"),
    STEAMER("Steamer"),
    TOASTER_OVEN("Toaster Oven")
}

// Request/Response models
data class MealPrepGuidanceRequest(
    val recipeId: String,
    val servings: Int,
    val availableTime: Int, // minutes
    val equipment: List<String> = emptyList()
)

data class MealPrepGuidanceResponse(
    val prepInstructions: List<PrepStep>,
    val cookingMethods: List<CookingMethod>,
    val timingGuidance: TimingGuidance,
    val storageRecommendations: List<ContainerType>,
    val refrigerationGuideline: RefrigerationGuideline,
    val freezingInstructions: FreezingInstruction?
)

data class LeftoverSuggestionRequest(
    val leftoverIds: List<String>,
    val additionalIngredients: List<String> = emptyList(),
    val maxPrepTime: Int = 30 // minutes
)

data class LeftoverSuggestionResponse(
    val combinations: List<LeftoverCombination>,
    val quickIdeas: List<String>,
    val safetyReminders: List<String>
)

