package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "recipes")
data class Recipe(
    @PrimaryKey
    val id: String,
    val name: String,
    val prepTime: Int, // minutes
    val cookTime: Int, // minutes
    val servings: Int,
    val instructions: String, // JSON string of List<String>
    val nutritionInfo: String, // JSON string of NutritionInfo
    val sourceType: RecipeSource,
    val sourceUrl: String? = null,
    val rating: Float? = null,
    val tags: String = "", // JSON string of List<String>
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "recipe_ingredients")
data class RecipeIngredient(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val isOptional: Boolean = false,
    val notes: String? = null
)

data class Ingredient(
    val name: String,
    val quantity: Double,
    val unit: String,
    val category: IngredientCategory = IngredientCategory.OTHER,
    val isOptional: Boolean = false,
    val notes: String? = null
)

data class NutritionInfo(
    val calories: Double,
    val carbohydrates: Double, // grams
    val proteins: Double, // grams
    val fats: Double, // grams
    val fiber: Double, // grams
    val sodium: Double, // mg
    val potassium: Double, // mg
    val micronutrients: Map<String, Double> = emptyMap()
)

enum class RecipeSource {
    MANUAL,
    URL_IMPORT,
    OCR_SCAN
}

enum class IngredientCategory {
    PROTEIN,
    VEGETABLES,
    FRUITS,
    GRAINS,
    DAIRY,
    SPICES,
    OILS,
    CONDIMENTS,
    BEVERAGES,
    OTHER;
    
    val displayName: String
        get() = when (this) {
            PROTEIN -> "Protein"
            VEGETABLES -> "Vegetables"
            FRUITS -> "Fruits"
            GRAINS -> "Grains & Starches"
            DAIRY -> "Dairy"
            SPICES -> "Herbs & Spices"
            OILS -> "Oils & Fats"
            CONDIMENTS -> "Condiments & Sauces"
            BEVERAGES -> "Beverages"
            OTHER -> "Other"
        }
}

data class RecipeStep(
    val stepNumber: Int,
    val instruction: String,
    val duration: Int? = null, // in minutes
    val temperature: String? = null,
    val equipment: List<String> = emptyList()
)

data class RecipeCreateRequest(
    val name: String,
    val description: String? = null,
    val prepTime: Int,
    val cookTime: Int,
    val servings: Int,
    val ingredients: List<Ingredient>,
    val steps: List<RecipeStep>,
    val tags: List<String> = emptyList(),
    val difficulty: RecipeDifficulty = RecipeDifficulty.MEDIUM,
    val cuisine: String? = null
)

data class RecipeUpdateRequest(
    val name: String?,
    val description: String?,
    val prepTime: Int?,
    val cookTime: Int?,
    val servings: Int?,
    val ingredients: List<Ingredient>?,
    val steps: List<RecipeStep>?,
    val tags: List<String>?,
    val difficulty: RecipeDifficulty?,
    val cuisine: String?,
    val rating: Float?
)

enum class RecipeDifficulty(val displayName: String, val level: Int) {
    EASY("Easy", 1),
    MEDIUM("Medium", 2),
    HARD("Hard", 3),
    EXPERT("Expert", 4)
}