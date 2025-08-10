package com.beaconledger.welltrack.cooking

import com.beaconledger.welltrack.data.cooking.CookingGuidanceService
import com.beaconledger.welltrack.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class CookingGuidanceServiceTest {
    
    private lateinit var cookingGuidanceService: CookingGuidanceService
    
    @Before
    fun setup() {
        cookingGuidanceService = CookingGuidanceService()
    }
    
    @Test
    fun `scaleRecipe doubles ingredients when scaling from 2 to 4 servings`() {
        // Given
        val recipe = createTestRecipe(servings = 2)
        val ingredients = listOf(
            Ingredient(name = "Flour", quantity = 2.0, unit = "cups"),
            Ingredient(name = "Sugar", quantity = 1.0, unit = "cup"),
            Ingredient(name = "Eggs", quantity = 2.0, unit = "pieces")
        )
        val targetServings = 4
        
        // When
        val scaledRecipe = cookingGuidanceService.scaleRecipe(recipe, ingredients, targetServings)
        
        // Then
        assertEquals(targetServings, scaledRecipe.targetServings)
        assertEquals(2.0f, scaledRecipe.scalingFactor)
        
        val scaledIngredients = scaledRecipe.scaledIngredients
        assertEquals(4.0, scaledIngredients[0].scaledQuantity, 0.01) // Flour: 2 * 2 = 4
        assertEquals(2.0, scaledIngredients[1].scaledQuantity, 0.01) // Sugar: 1 * 2 = 2
        assertEquals(4.0, scaledIngredients[2].scaledQuantity, 0.01) // Eggs: 2 * 2 = 4
    }
    
    @Test
    fun `scaleRecipe halves ingredients when scaling from 4 to 2 servings`() {
        // Given
        val recipe = createTestRecipe(servings = 4)
        val ingredients = listOf(
            Ingredient(name = "Flour", quantity = 4.0, unit = "cups"),
            Ingredient(name = "Sugar", quantity = 2.0, unit = "cups")
        )
        val targetServings = 2
        
        // When
        val scaledRecipe = cookingGuidanceService.scaleRecipe(recipe, ingredients, targetServings)
        
        // Then
        assertEquals(targetServings, scaledRecipe.targetServings)
        assertEquals(0.5f, scaledRecipe.scalingFactor)
        
        val scaledIngredients = scaledRecipe.scaledIngredients
        assertEquals(2.0, scaledIngredients[0].scaledQuantity, 0.01) // Flour: 4 * 0.5 = 2
        assertEquals(1.0, scaledIngredients[1].scaledQuantity, 0.01) // Sugar: 2 * 0.5 = 1
    }
    
    @Test
    fun `createCookingSession creates session with correct initial state`() {
        // Given
        val recipeId = "recipe123"
        val userId = "user456"
        val scaledServings = 4
        val scalingFactor = 2.0f
        
        // When
        val session = cookingGuidanceService.createCookingSession(
            recipeId = recipeId,
            userId = userId,
            scaledServings = scaledServings,
            scalingFactor = scalingFactor
        )
        
        // Then
        assertEquals(recipeId, session.recipeId)
        assertEquals(userId, session.userId)
        assertEquals(scaledServings, session.scaledServings)
        assertEquals(scalingFactor, session.scalingFactor)
        assertEquals(0, session.currentStepIndex)
        assertEquals(CookingStatus.IN_PROGRESS, session.status)
        assertEquals("[]", session.completedSteps)
        assertEquals("[]", session.activeTimers)
        assertNotNull(session.id)
        assertNotNull(session.startedAt)
        assertNull(session.completedAt)
    }
    
    @Test
    fun `completeStep adds step to completed list and advances current step`() {
        // Given
        val session = createTestCookingSession()
        val stepIndex = 0
        
        // When
        val updatedSession = cookingGuidanceService.completeStep(session, stepIndex)
        
        // Then
        assertTrue(updatedSession.completedSteps.contains("0"))
        assertEquals(1, updatedSession.currentStepIndex)
    }
    
    @Test
    fun `completeStep does not advance current step if completing non-current step`() {
        // Given
        val session = createTestCookingSession().copy(currentStepIndex = 2)
        val stepIndex = 0
        
        // When
        val updatedSession = cookingGuidanceService.completeStep(session, stepIndex)
        
        // Then
        assertTrue(updatedSession.completedSteps.contains("0"))
        assertEquals(2, updatedSession.currentStepIndex) // Should remain unchanged
    }
    
    @Test
    fun `uncheckStep removes step from completed list`() {
        // Given
        val session = createTestCookingSession().copy(completedSteps = "[0,1,2]")
        val stepIndex = 1
        
        // When
        val updatedSession = cookingGuidanceService.uncheckStep(session, stepIndex)
        
        // Then
        assertFalse(updatedSession.completedSteps.contains("1"))
        assertTrue(updatedSession.completedSteps.contains("0"))
        assertTrue(updatedSession.completedSteps.contains("2"))
    }
    
    @Test
    fun `startTimer creates timer with correct properties`() {
        // Given
        val session = createTestCookingSession()
        val stepNumber = 1
        val timerName = "Boil Water"
        val durationMinutes = 10
        
        // When
        val (updatedSession, timer) = cookingGuidanceService.startTimer(
            session, stepNumber, timerName, durationMinutes
        )
        
        // Then
        assertEquals(stepNumber, timer.stepNumber)
        assertEquals(timerName, timer.name)
        assertEquals(durationMinutes, timer.durationMinutes)
        assertTrue(timer.isActive)
        assertFalse(timer.isCompleted)
        assertNotNull(timer.id)
        assertTrue(timer.startTime > 0)
    }
    
    @Test
    fun `completeCookingSession sets status to completed and completion time`() {
        // Given
        val session = createTestCookingSession()
        
        // When
        val completedSession = cookingGuidanceService.completeCookingSession(session)
        
        // Then
        assertEquals(CookingStatus.COMPLETED, completedSession.status)
        assertNotNull(completedSession.completedAt)
    }
    
    @Test
    fun `getTimerRemainingTime returns correct remaining time`() {
        // Given
        val durationMinutes = 5
        val timer = CookingTimer(
            id = "timer1",
            stepNumber = 1,
            name = "Test Timer",
            durationMinutes = durationMinutes,
            startTime = System.currentTimeMillis() - (2 * 60 * 1000), // Started 2 minutes ago
            isActive = true,
            isCompleted = false
        )
        
        // When
        val remainingTime = cookingGuidanceService.getTimerRemainingTime(timer)
        
        // Then
        val expectedRemainingMs = 3 * 60 * 1000L // 3 minutes remaining
        assertTrue("Remaining time should be around 3 minutes", 
                   Math.abs(remainingTime - expectedRemainingMs) < 1000) // Allow 1 second tolerance
    }
    
    @Test
    fun `getTimerRemainingTime returns zero for expired timer`() {
        // Given
        val timer = CookingTimer(
            id = "timer1",
            stepNumber = 1,
            name = "Test Timer",
            durationMinutes = 5,
            startTime = System.currentTimeMillis() - (10 * 60 * 1000), // Started 10 minutes ago
            isActive = true,
            isCompleted = false
        )
        
        // When
        val remainingTime = cookingGuidanceService.getTimerRemainingTime(timer)
        
        // Then
        assertEquals(0L, remainingTime)
    }
    
    @Test
    fun `isTimerExpired returns true for expired timer`() {
        // Given
        val timer = CookingTimer(
            id = "timer1",
            stepNumber = 1,
            name = "Test Timer",
            durationMinutes = 5,
            startTime = System.currentTimeMillis() - (10 * 60 * 1000), // Started 10 minutes ago
            isActive = true,
            isCompleted = false
        )
        
        // When
        val isExpired = cookingGuidanceService.isTimerExpired(timer)
        
        // Then
        assertTrue(isExpired)
    }
    
    @Test
    fun `isTimerExpired returns false for active timer`() {
        // Given
        val timer = CookingTimer(
            id = "timer1",
            stepNumber = 1,
            name = "Test Timer",
            durationMinutes = 5,
            startTime = System.currentTimeMillis() - (2 * 60 * 1000), // Started 2 minutes ago
            isActive = true,
            isCompleted = false
        )
        
        // When
        val isExpired = cookingGuidanceService.isTimerExpired(timer)
        
        // Then
        assertFalse(isExpired)
    }
    
    private fun createTestRecipe(servings: Int = 4): Recipe {
        return Recipe(
            id = "recipe123",
            name = "Test Recipe",
            prepTime = 15,
            cookTime = 30,
            servings = servings,
            instructions = "1::Mix ingredients||2::Cook for 30 minutes||3::Serve hot",
            nutritionInfo = "500,50,20,15", // calories, carbs, protein, fat
            sourceType = RecipeSource.MANUAL,
            sourceUrl = null,
            rating = 4.5f,
            tags = "[]",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
    }
    
    private fun createTestCookingSession(): CookingSession {
        return CookingSession(
            id = "session123",
            recipeId = "recipe123",
            userId = "user456",
            scaledServings = 4,
            scalingFactor = 1.0f,
            startedAt = LocalDateTime.now().toString(),
            completedAt = null,
            currentStepIndex = 0,
            completedSteps = "[]",
            activeTimers = "[]",
            status = CookingStatus.IN_PROGRESS,
            notes = null
        )
    }
}