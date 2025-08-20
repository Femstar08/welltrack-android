package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

// Enhanced dietary restriction data models for comprehensive management

@Entity(tableName = "user_dietary_restrictions")
data class UserDietaryRestriction(
    @PrimaryKey
    val id: String,
    val userId: String,
    val restrictionType: DietaryRestrictionType,
    val severity: RestrictionSeverity = RestrictionSeverity.MODERATE,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "user_allergies")
data class UserAllergy(
    @PrimaryKey
    val id: String,
    val userId: String,
    val allergen: String,
    val severity: AllergySeverity,
    val symptoms: String? = null,
    val notes: String? = null,
    val isActive: Boolean = true,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "user_food_preferences")
data class UserFoodPreference(
    @PrimaryKey
    val id: String,
    val userId: String,
    val preferenceType: FoodPreferenceType,
    val item: String, // ingredient, cuisine, cooking method, etc.
    val preference: PreferenceLevel, // LOVE, LIKE, NEUTRAL, DISLIKE, HATE
    val notes: String? = null,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "meal_dietary_tags")
data class MealDietaryTag(
    @PrimaryKey
    val id: String,
    val mealId: String,
    val tagType: DietaryTagType,
    val tagValue: String,
    val confidence: Float = 1.0f, // How confident we are in this tag (0.0 - 1.0)
    val source: TagSource = TagSource.AUTOMATIC,
    val createdAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "recipe_dietary_tags")
data class RecipeDietaryTag(
    @PrimaryKey
    val id: String,
    val recipeId: String,
    val tagType: DietaryTagType,
    val tagValue: String,
    val confidence: Float = 1.0f,
    val source: TagSource = TagSource.AUTOMATIC,
    val createdAt: String = LocalDateTime.now().toString()
)

// Enhanced enums for comprehensive dietary management

enum class DietaryRestrictionType(
    val displayName: String,
    val description: String,
    val category: RestrictionCategory
) {
    // Plant-based diets
    VEGETARIAN("Vegetarian", "No meat, poultry, or fish", RestrictionCategory.PLANT_BASED),
    VEGAN("Vegan", "No animal products", RestrictionCategory.PLANT_BASED),
    FLEXITARIAN("Flexitarian", "Mostly vegetarian with occasional meat", RestrictionCategory.PLANT_BASED),
    PESCATARIAN("Pescatarian", "Vegetarian plus fish and seafood", RestrictionCategory.PLANT_BASED),
    
    // Medical/Health diets
    GLUTEN_FREE("Gluten-Free", "No gluten-containing grains", RestrictionCategory.MEDICAL),
    DAIRY_FREE("Dairy-Free", "No dairy products", RestrictionCategory.MEDICAL),
    NUT_FREE("Nut-Free", "No tree nuts or peanuts", RestrictionCategory.MEDICAL),
    SOY_FREE("Soy-Free", "No soy products", RestrictionCategory.MEDICAL),
    EGG_FREE("Egg-Free", "No eggs or egg products", RestrictionCategory.MEDICAL),
    SHELLFISH_FREE("Shellfish-Free", "No shellfish or crustaceans", RestrictionCategory.MEDICAL),
    
    // Macronutrient-focused diets
    KETO("Ketogenic", "Very low carb, high fat", RestrictionCategory.MACRONUTRIENT),
    LOW_CARB("Low Carb", "Reduced carbohydrate intake", RestrictionCategory.MACRONUTRIENT),
    HIGH_PROTEIN("High Protein", "Increased protein intake", RestrictionCategory.MACRONUTRIENT),
    LOW_FAT("Low Fat", "Reduced fat intake", RestrictionCategory.MACRONUTRIENT),
    LOW_SODIUM("Low Sodium", "Reduced sodium intake", RestrictionCategory.MACRONUTRIENT),
    
    // Calorie management
    CALORIE_CONSCIOUS("Calorie Conscious", "Focused on calorie control", RestrictionCategory.CALORIE),
    WEIGHT_LOSS("Weight Loss", "Calorie deficit for weight loss", RestrictionCategory.CALORIE),
    WEIGHT_GAIN("Weight Gain", "Calorie surplus for weight gain", RestrictionCategory.CALORIE),
    
    // Religious/Cultural
    HALAL("Halal", "Islamic dietary laws", RestrictionCategory.RELIGIOUS),
    KOSHER("Kosher", "Jewish dietary laws", RestrictionCategory.RELIGIOUS),
    HINDU_VEGETARIAN("Hindu Vegetarian", "Hindu dietary practices", RestrictionCategory.RELIGIOUS),
    
    // Specialized diets
    PALEO("Paleo", "Paleolithic diet principles", RestrictionCategory.SPECIALIZED),
    MEDITERRANEAN("Mediterranean", "Mediterranean diet pattern", RestrictionCategory.SPECIALIZED),
    DASH("DASH", "Dietary Approaches to Stop Hypertension", RestrictionCategory.SPECIALIZED),
    WHOLE30("Whole30", "30-day elimination diet", RestrictionCategory.SPECIALIZED)
}

enum class RestrictionCategory(val displayName: String) {
    PLANT_BASED("Plant-Based"),
    MEDICAL("Medical/Allergies"),
    MACRONUTRIENT("Macronutrient Focus"),
    CALORIE("Calorie Management"),
    RELIGIOUS("Religious/Cultural"),
    SPECIALIZED("Specialized Diets")
}

enum class RestrictionSeverity(val displayName: String, val level: Int) {
    MILD("Mild - Flexible", 1),
    MODERATE("Moderate - Usually avoid", 2),
    STRICT("Strict - Always avoid", 3),
    MEDICAL("Medical - Must avoid", 4)
}

enum class AllergySeverity(val displayName: String, val level: Int) {
    MILD("Mild discomfort", 1),
    MODERATE("Moderate reaction", 2),
    SEVERE("Severe reaction", 3),
    ANAPHYLAXIS("Life-threatening", 4)
}

enum class FoodPreferenceType(val displayName: String) {
    INGREDIENT("Ingredient"),
    CUISINE("Cuisine"),
    COOKING_METHOD("Cooking Method"),
    FLAVOR_PROFILE("Flavor Profile"),
    TEXTURE("Texture"),
    SPICE_LEVEL("Spice Level")
}

enum class PreferenceLevel(val displayName: String, val score: Int) {
    HATE("Hate", -2),
    DISLIKE("Dislike", -1),
    NEUTRAL("Neutral", 0),
    LIKE("Like", 1),
    LOVE("Love", 2)
}

enum class DietaryTagType(val displayName: String) {
    RESTRICTION("Dietary Restriction"),
    ALLERGEN("Allergen"),
    CUISINE("Cuisine"),
    COOKING_METHOD("Cooking Method"),
    MEAL_TYPE("Meal Type"),
    NUTRITION_FOCUS("Nutrition Focus"),
    INGREDIENT_HIGHLIGHT("Key Ingredient")
}

enum class TagSource(val displayName: String) {
    AUTOMATIC("Automatic Analysis"),
    USER_MANUAL("User Added"),
    RECIPE_IMPORT("Recipe Import"),
    AI_ANALYSIS("AI Analysis")
}

// Data classes for UI and business logic

data class DietaryProfile(
    val restrictions: List<UserDietaryRestriction>,
    val allergies: List<UserAllergy>,
    val preferences: List<UserFoodPreference>
)

data class DietaryCompatibility(
    val isCompatible: Boolean,
    val violations: List<DietaryViolation>,
    val warnings: List<DietaryWarning>,
    val score: Float // 0.0 to 1.0, higher is better match
)

data class DietaryViolation(
    val type: ViolationType,
    val restriction: DietaryRestrictionType?,
    val allergen: String?,
    val severity: RestrictionSeverity,
    val description: String,
    val affectedIngredients: List<String>
)

data class DietaryWarning(
    val type: WarningType,
    val description: String,
    val suggestion: String?
)

enum class ViolationType {
    DIETARY_RESTRICTION,
    ALLERGEN,
    STRONG_DISLIKE
}

enum class WarningType {
    PREFERENCE_MISMATCH,
    NUTRITIONAL_CONCERN,
    INGREDIENT_SUBSTITUTION_AVAILABLE
}

// Request/Response models for API

data class DietaryRestrictionsUpdateRequest(
    val restrictions: List<DietaryRestrictionRequest>,
    val allergies: List<AllergyRequest>,
    val preferences: List<FoodPreferenceRequest>
)

data class DietaryRestrictionRequest(
    val restrictionType: DietaryRestrictionType,
    val severity: RestrictionSeverity,
    val notes: String?
)

data class AllergyRequest(
    val allergen: String,
    val severity: AllergySeverity,
    val symptoms: String?,
    val notes: String?
)

data class FoodPreferenceRequest(
    val preferenceType: FoodPreferenceType,
    val item: String,
    val preference: PreferenceLevel,
    val notes: String?
)