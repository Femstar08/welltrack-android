package com.beaconledger.welltrack.meal

import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.data.model.MealScore
import com.beaconledger.welltrack.data.model.MealStatus
import org.junit.Test
import org.junit.Assert.*

class MealRatingTest {
    
    @Test
    fun `meal should support rating and favorite functionality`() {
        // Given
        val meal = Meal(
            id = "test-meal-id",
            userId = "test-user-id",
            recipeId = null,
            timestamp = "2024-01-01T12:00:00",
            mealType = MealType.LUNCH,
            portions = 1.0f,
            nutritionInfo = "{}",
            score = MealScore.B,
            status = MealStatus.EATEN,
            notes = "Test meal",
            rating = 4.0f,
            isFavorite = true
        )
        
        // Then
        assertEquals(4.0f, meal.rating)
        assertTrue(meal.isFavorite)
    }
    
    @Test
    fun `meal should support null rating`() {
        // Given
        val meal = Meal(
            id = "test-meal-id",
            userId = "test-user-id",
            recipeId = null,
            timestamp = "2024-01-01T12:00:00",
            mealType = MealType.LUNCH,
            portions = 1.0f,
            nutritionInfo = "{}",
            score = MealScore.B,
            status = MealStatus.EATEN,
            notes = "Test meal",
            rating = null,
            isFavorite = false
        )
        
        // Then
        assertNull(meal.rating)
        assertFalse(meal.isFavorite)
    }
    
    @Test
    fun `meal should have default values for rating and favorite`() {
        // Given
        val meal = Meal(
            id = "test-meal-id",
            userId = "test-user-id",
            recipeId = null,
            timestamp = "2024-01-01T12:00:00",
            mealType = MealType.LUNCH,
            portions = 1.0f,
            nutritionInfo = "{}",
            score = MealScore.B,
            status = MealStatus.EATEN,
            notes = "Test meal"
        )
        
        // Then
        assertNull(meal.rating)
        assertFalse(meal.isFavorite)
    }
}