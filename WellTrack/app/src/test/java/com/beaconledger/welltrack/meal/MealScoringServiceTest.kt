package com.beaconledger.welltrack.meal

import com.beaconledger.welltrack.data.meal.MealScoringService
import com.beaconledger.welltrack.data.model.MealScore
import com.beaconledger.welltrack.data.model.NutritionInfo
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MealScoringServiceTest {
    
    private lateinit var mealScoringService: MealScoringService
    
    @Before
    fun setup() {
        mealScoringService = MealScoringService()
    }
    
    @Test
    fun `calculateMealScore should return A for excellent nutrition`() {
        // Given - High protein, high fiber, balanced macros, low sodium
        val nutritionInfo = NutritionInfo(
            calories = 400.0,
            carbohydrates = 30.0, // 30% of calories
            proteins = 30.0, // 30% of calories  
            fats = 18.0, // 40% of calories
            fiber = 12.0, // High fiber
            sodium = 400.0, // Low sodium
            potassium = 600.0 // High potassium
        )
        
        // When
        val score = mealScoringService.calculateMealScore(nutritionInfo)
        
        // Then
        assertEquals(MealScore.A, score)
    }
    
    @Test
    fun `calculateMealScore should return E for poor nutrition`() {
        // Given - Low protein, no fiber, high fat, high sodium
        val nutritionInfo = NutritionInfo(
            calories = 600.0,
            carbohydrates = 60.0, // 40% of calories
            proteins = 5.0, // 3% of calories - very low
            fats = 40.0, // 60% of calories - very high
            fiber = 0.0, // No fiber
            sodium = 2000.0, // Very high sodium
            potassium = 50.0 // Low potassium
        )
        
        // When
        val score = mealScoringService.calculateMealScore(nutritionInfo)
        
        // Then
        assertEquals(MealScore.E, score)
    }
    
    @Test
    fun `calculateMealScore should return C for average nutrition`() {
        // Given - Moderate nutrition values
        val nutritionInfo = NutritionInfo(
            calories = 350.0,
            carbohydrates = 40.0, // 46% of calories
            proteins = 15.0, // 17% of calories
            fats = 15.0, // 39% of calories
            fiber = 4.0, // Moderate fiber
            sodium = 800.0, // Moderate sodium
            potassium = 300.0 // Moderate potassium
        )
        
        // When
        val score = mealScoringService.calculateMealScore(nutritionInfo)
        
        // Then
        assertEquals(MealScore.C, score)
    }
    
    @Test
    fun `getMealScoreBreakdown should provide detailed scoring`() {
        // Given
        val nutritionInfo = NutritionInfo(
            calories = 400.0,
            carbohydrates = 30.0,
            proteins = 25.0, // Good protein
            fats = 18.0,
            fiber = 10.0, // High fiber
            sodium = 500.0, // Low sodium
            potassium = 500.0 // High potassium
        )
        
        // When
        val breakdown = mealScoringService.getMealScoreBreakdown(nutritionInfo)
        
        // Then
        assertTrue(breakdown.totalScore > 70) // Should be good score
        assertTrue(breakdown.proteinScore > 5) // Good protein score
        assertTrue(breakdown.fiberScore > 10) // High fiber score
        assertTrue(breakdown.sodiumScore > 5) // Good sodium score
        assertNotNull(breakdown.overallFeedback)
        assertTrue(breakdown.overallFeedback.isNotBlank())
    }
    
    @Test
    fun `calculateMealScore should handle zero calories gracefully`() {
        // Given
        val nutritionInfo = NutritionInfo(
            calories = 0.0,
            carbohydrates = 0.0,
            proteins = 0.0,
            fats = 0.0,
            fiber = 0.0,
            sodium = 0.0,
            potassium = 0.0
        )
        
        // When
        val score = mealScoringService.calculateMealScore(nutritionInfo)
        
        // Then
        assertNotNull(score) // Should not crash
        assertTrue(score in listOf(MealScore.A, MealScore.B, MealScore.C, MealScore.D, MealScore.E))
    }
}