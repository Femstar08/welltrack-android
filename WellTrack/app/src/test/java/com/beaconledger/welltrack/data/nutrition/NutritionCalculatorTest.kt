package com.beaconledger.welltrack.data.nutrition

import com.beaconledger.welltrack.data.model.Ingredient
import com.beaconledger.welltrack.data.model.IngredientCategory
import com.beaconledger.welltrack.data.model.NutritionInfo
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class NutritionCalculatorTest {

    private lateinit var nutritionCalculator: NutritionCalculator

    @Before
    fun setup() {
        nutritionCalculator = NutritionCalculator()
    }

    @Test
    fun `calculateNutritionInfo returns correct totals for multiple ingredients`() {
        // Given
        val ingredients = listOf(
            Ingredient("Chicken Breast", 200.0, "g", IngredientCategory.PROTEIN),
            Ingredient("Rice", 100.0, "g", IngredientCategory.GRAINS),
            Ingredient("Broccoli", 150.0, "g", IngredientCategory.VEGETABLES)
        )

        // When
        val result = nutritionCalculator.calculateNutritionInfo(ingredients)

        // Then
        assertTrue(result.calories > 0)
        assertTrue(result.proteins > 0)
        assertTrue(result.carbohydrates > 0)
        assertTrue(result.fats >= 0)
        assertTrue(result.fiber >= 0)
        assertTrue(result.sodium >= 0)
        assertTrue(result.potassium >= 0)
    }

    @Test
    fun `calculateNutritionInfo handles empty ingredient list`() {
        // Given
        val ingredients = emptyList<Ingredient>()

        // When
        val result = nutritionCalculator.calculateNutritionInfo(ingredients)

        // Then
        assertEquals(0.0, result.calories, 0.01)
        assertEquals(0.0, result.proteins, 0.01)
        assertEquals(0.0, result.carbohydrates, 0.01)
        assertEquals(0.0, result.fats, 0.01)
        assertEquals(0.0, result.fiber, 0.01)
        assertEquals(0.0, result.sodium, 0.01)
        assertEquals(0.0, result.potassium, 0.01)
    }

    @Test
    fun `calculateNutritionInfo handles different units correctly`() {
        // Given
        val ingredients = listOf(
            Ingredient("Flour", 1.0, "kg", IngredientCategory.GRAINS), // 1000g
            Ingredient("Oil", 2.0, "tbsp", IngredientCategory.OILS),   // ~30g
            Ingredient("Salt", 1.0, "tsp", IngredientCategory.SPICES)  // ~5g
        )

        // When
        val result = nutritionCalculator.calculateNutritionInfo(ingredients)

        // Then
        // Flour (1kg) should contribute significantly more than oil (2 tbsp) or salt (1 tsp)
        assertTrue(result.calories > 3000) // Flour is high calorie
        assertTrue(result.carbohydrates > 600) // Flour is high carb
    }

    @Test
    fun `calculateNutritionPerServing divides nutrition correctly`() {
        // Given
        val totalNutrition = NutritionInfo(
            calories = 800.0,
            carbohydrates = 100.0,
            proteins = 40.0,
            fats = 20.0,
            fiber = 10.0,
            sodium = 400.0,
            potassium = 600.0
        )
        val servings = 4

        // When
        val result = nutritionCalculator.calculateNutritionPerServing(totalNutrition, servings)

        // Then
        assertEquals(200.0, result.calories, 0.01)
        assertEquals(25.0, result.carbohydrates, 0.01)
        assertEquals(10.0, result.proteins, 0.01)
        assertEquals(5.0, result.fats, 0.01)
        assertEquals(2.5, result.fiber, 0.01)
        assertEquals(100.0, result.sodium, 0.01)
        assertEquals(150.0, result.potassium, 0.01)
    }

    @Test
    fun `calculateNutritionPerServing handles zero servings`() {
        // Given
        val totalNutrition = NutritionInfo(
            calories = 800.0,
            carbohydrates = 100.0,
            proteins = 40.0,
            fats = 20.0,
            fiber = 10.0,
            sodium = 400.0,
            potassium = 600.0
        )
        val servings = 0

        // When
        val result = nutritionCalculator.calculateNutritionPerServing(totalNutrition, servings)

        // Then
        assertEquals(totalNutrition, result)
    }

    @Test
    fun `scaleNutrition scales all values correctly`() {
        // Given
        val nutrition = NutritionInfo(
            calories = 100.0,
            carbohydrates = 20.0,
            proteins = 10.0,
            fats = 5.0,
            fiber = 3.0,
            sodium = 200.0,
            potassium = 150.0
        )
        val factor = 2.5

        // When
        val result = nutritionCalculator.scaleNutrition(nutrition, factor)

        // Then
        assertEquals(250.0, result.calories, 0.01)
        assertEquals(50.0, result.carbohydrates, 0.01)
        assertEquals(25.0, result.proteins, 0.01)
        assertEquals(12.5, result.fats, 0.01)
        assertEquals(7.5, result.fiber, 0.01)
        assertEquals(500.0, result.sodium, 0.01)
        assertEquals(375.0, result.potassium, 0.01)
    }

    @Test
    fun `protein category ingredients have high protein content`() {
        // Given
        val proteinIngredient = Ingredient("Chicken", 100.0, "g", IngredientCategory.PROTEIN)

        // When
        val result = nutritionCalculator.calculateNutritionInfo(listOf(proteinIngredient))

        // Then
        assertTrue("Protein should be significant", result.proteins > 20.0)
        assertTrue("Calories should be moderate to high", result.calories > 200.0)
    }

    @Test
    fun `vegetable category ingredients have low calories and high fiber`() {
        // Given
        val vegetableIngredient = Ingredient("Broccoli", 100.0, "g", IngredientCategory.VEGETABLES)

        // When
        val result = nutritionCalculator.calculateNutritionInfo(listOf(vegetableIngredient))

        // Then
        assertTrue("Calories should be low", result.calories < 50.0)
        assertTrue("Fiber should be present", result.fiber > 2.0)
        assertTrue("Potassium should be high", result.potassium > 150.0)
    }

    @Test
    fun `oil category ingredients have very high calories and fat`() {
        // Given
        val oilIngredient = Ingredient("Olive Oil", 100.0, "g", IngredientCategory.OILS)

        // When
        val result = nutritionCalculator.calculateNutritionInfo(listOf(oilIngredient))

        // Then
        assertTrue("Calories should be very high", result.calories > 800.0)
        assertTrue("Fat should be very high", result.fats > 90.0)
        assertEquals("Carbs should be zero", 0.0, result.carbohydrates, 0.01)
        assertEquals("Protein should be zero", 0.0, result.proteins, 0.01)
    }
}