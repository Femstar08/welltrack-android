package com.beaconledger.welltrack.data.nutrition

import com.beaconledger.welltrack.data.model.Ingredient
import com.beaconledger.welltrack.data.model.IngredientCategory
import com.beaconledger.welltrack.data.model.NutritionInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NutritionCalculator @Inject constructor() {

    /**
     * Calculate nutritional information for a list of ingredients
     * This is a simplified implementation - in a real app, you would use a comprehensive food database
     */
    fun calculateNutritionInfo(ingredients: List<Ingredient>): NutritionInfo {
        var totalCalories = 0.0
        var totalCarbs = 0.0
        var totalProteins = 0.0
        var totalFats = 0.0
        var totalFiber = 0.0
        var totalSodium = 0.0
        var totalPotassium = 0.0

        ingredients.forEach { ingredient ->
            val nutrition = estimateNutritionForIngredient(ingredient)
            totalCalories += nutrition.calories
            totalCarbs += nutrition.carbohydrates
            totalProteins += nutrition.proteins
            totalFats += nutrition.fats
            totalFiber += nutrition.fiber
            totalSodium += nutrition.sodium
            totalPotassium += nutrition.potassium
        }

        return NutritionInfo(
            calories = totalCalories,
            carbohydrates = totalCarbs,
            proteins = totalProteins,
            fats = totalFats,
            fiber = totalFiber,
            sodium = totalSodium,
            potassium = totalPotassium
        )
    }

    /**
     * Estimate nutrition for a single ingredient based on category and quantity
     * This is a simplified estimation - real implementation would use a food database API
     */
    private fun estimateNutritionForIngredient(ingredient: Ingredient): NutritionInfo {
        val baseNutrition = getBaseNutritionByCategory(ingredient.category)
        val multiplier = calculateQuantityMultiplier(ingredient.quantity, ingredient.unit)

        return NutritionInfo(
            calories = baseNutrition.calories * multiplier,
            carbohydrates = baseNutrition.carbohydrates * multiplier,
            proteins = baseNutrition.proteins * multiplier,
            fats = baseNutrition.fats * multiplier,
            fiber = baseNutrition.fiber * multiplier,
            sodium = baseNutrition.sodium * multiplier,
            potassium = baseNutrition.potassium * multiplier
        )
    }

    /**
     * Get base nutrition values per 100g for different ingredient categories
     */
    private fun getBaseNutritionByCategory(category: IngredientCategory): NutritionInfo {
        return when (category) {
            IngredientCategory.PROTEIN -> NutritionInfo(
                calories = 250.0,
                carbohydrates = 0.0,
                proteins = 25.0,
                fats = 15.0,
                fiber = 0.0,
                sodium = 400.0,
                potassium = 300.0
            )
            IngredientCategory.VEGETABLES -> NutritionInfo(
                calories = 25.0,
                carbohydrates = 5.0,
                proteins = 2.0,
                fats = 0.2,
                fiber = 3.0,
                sodium = 10.0,
                potassium = 200.0
            )
            IngredientCategory.FRUITS -> NutritionInfo(
                calories = 60.0,
                carbohydrates = 15.0,
                proteins = 1.0,
                fats = 0.3,
                fiber = 2.5,
                sodium = 2.0,
                potassium = 180.0
            )
            IngredientCategory.GRAINS -> NutritionInfo(
                calories = 350.0,
                carbohydrates = 70.0,
                proteins = 12.0,
                fats = 2.5,
                fiber = 8.0,
                sodium = 5.0,
                potassium = 150.0
            )
            IngredientCategory.DAIRY -> NutritionInfo(
                calories = 150.0,
                carbohydrates = 5.0,
                proteins = 8.0,
                fats = 10.0,
                fiber = 0.0,
                sodium = 100.0,
                potassium = 140.0
            )
            IngredientCategory.OILS -> NutritionInfo(
                calories = 900.0,
                carbohydrates = 0.0,
                proteins = 0.0,
                fats = 100.0,
                fiber = 0.0,
                sodium = 0.0,
                potassium = 0.0
            )
            IngredientCategory.SPICES -> NutritionInfo(
                calories = 300.0,
                carbohydrates = 50.0,
                proteins = 10.0,
                fats = 5.0,
                fiber = 25.0,
                sodium = 50.0,
                potassium = 1000.0
            )
            IngredientCategory.CONDIMENTS -> NutritionInfo(
                calories = 100.0,
                carbohydrates = 20.0,
                proteins = 2.0,
                fats = 1.0,
                fiber = 1.0,
                sodium = 1000.0,
                potassium = 100.0
            )
            IngredientCategory.BEVERAGES -> NutritionInfo(
                calories = 40.0,
                carbohydrates = 10.0,
                proteins = 0.0,
                fats = 0.0,
                fiber = 0.0,
                sodium = 10.0,
                potassium = 50.0
            )
            IngredientCategory.OTHER -> NutritionInfo(
                calories = 100.0,
                carbohydrates = 15.0,
                proteins = 3.0,
                fats = 3.0,
                fiber = 2.0,
                sodium = 100.0,
                potassium = 150.0
            )
        }
    }

    /**
     * Calculate quantity multiplier based on unit
     * Converts various units to a standardized multiplier for 100g base values
     */
    private fun calculateQuantityMultiplier(quantity: Double, unit: String): Double {
        return when (unit.lowercase()) {
            "g", "gram", "grams" -> quantity / 100.0
            "kg", "kilogram", "kilograms" -> quantity * 10.0
            "oz", "ounce", "ounces" -> quantity * 0.283495 // 1 oz = 28.3495g
            "lb", "pound", "pounds" -> quantity * 4.53592 // 1 lb = 453.592g
            "cup", "cups" -> quantity * 2.4 // Approximate for average ingredients
            "tbsp", "tablespoon", "tablespoons" -> quantity * 0.15
            "tsp", "teaspoon", "teaspoons" -> quantity * 0.05
            "ml", "milliliter", "milliliters" -> quantity / 100.0 // Assuming density ~1g/ml
            "l", "liter", "liters" -> quantity * 10.0
            "piece", "pieces", "item", "items" -> quantity * 1.0 // Assume 100g per piece
            "large" -> quantity * 1.5 // Large items ~150g
            "medium" -> quantity * 1.0 // Medium items ~100g
            "small" -> quantity * 0.5 // Small items ~50g
            else -> quantity // Default to quantity as-is
        }
    }

    /**
     * Calculate nutrition per serving
     */
    fun calculateNutritionPerServing(totalNutrition: NutritionInfo, servings: Int): NutritionInfo {
        if (servings <= 0) return totalNutrition
        
        return NutritionInfo(
            calories = totalNutrition.calories / servings,
            carbohydrates = totalNutrition.carbohydrates / servings,
            proteins = totalNutrition.proteins / servings,
            fats = totalNutrition.fats / servings,
            fiber = totalNutrition.fiber / servings,
            sodium = totalNutrition.sodium / servings,
            potassium = totalNutrition.potassium / servings,
            micronutrients = totalNutrition.micronutrients.mapValues { it.value / servings }
        )
    }

    /**
     * Scale nutrition information by a factor (e.g., for different serving sizes)
     */
    fun scaleNutrition(nutrition: NutritionInfo, factor: Double): NutritionInfo {
        return NutritionInfo(
            calories = nutrition.calories * factor,
            carbohydrates = nutrition.carbohydrates * factor,
            proteins = nutrition.proteins * factor,
            fats = nutrition.fats * factor,
            fiber = nutrition.fiber * factor,
            sodium = nutrition.sodium * factor,
            potassium = nutrition.potassium * factor,
            micronutrients = nutrition.micronutrients.mapValues { it.value * factor }
        )
    }
}