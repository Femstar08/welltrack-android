package com.beaconledger.welltrack.cooking

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.CookingRepository
import com.beaconledger.welltrack.domain.usecase.CookingGuidanceUseCase
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

class CookingGuidanceUseCaseTest {
    
    @Mock
    private lateinit var cookingRepository: CookingRepository
    
    private lateinit var cookingGuidanceUseCase: CookingGuidanceUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        cookingGuidanceUseCase = CookingGuidanceUseCase(cookingRepository)
    }
    
    @Test
    fun `scaleRecipe returns failure when target servings is zero`() = runTest {
        // Given
        val recipeId = "recipe123"
        val targetServings = 0
        
        // When
        val result = cookingGuidanceUseCase.scaleRecipe(recipeId, targetServings)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Target servings must be greater than 0", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `scaleRecipe returns failure when target servings is negative`() = runTest {
        // Given
        val recipeId = "recipe123"
        val targetServings = -1
        
        // When
        val result = cookingGuidanceUseCase.scaleRecipe(recipeId, targetServings)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Target servings must be greater than 0", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `scaleRecipe returns success when target servings is valid`() = runTest {
        // Given
        val recipeId = "recipe123"
        val targetServings = 4
        val scaledRecipe = createTestScaledRecipe()
        
        whenever(cookingRepository.scaleRecipe(recipeId, targetServings))
            .thenReturn(Result.success(scaledRecipe))
        
        // When
        val result = cookingGuidanceUseCase.scaleRecipe(recipeId, targetServings)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(scaledRecipe, result.getOrNull())
    }
    
    @Test
    fun `startCookingSession returns failure when target servings is zero`() = runTest {
        // Given
        val recipeId = "recipe123"
        val userId = "user456"
        val targetServings = 0
        
        // When
        val result = cookingGuidanceUseCase.startCookingSession(recipeId, userId, targetServings)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Target servings must be greater than 0", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `startCookingSession returns success when parameters are valid`() = runTest {
        // Given
        val recipeId = "recipe123"
        val userId = "user456"
        val targetServings = 4
        val cookingSession = createTestCookingSession()
        
        whenever(cookingRepository.startCookingSession(recipeId, userId, targetServings))
            .thenReturn(Result.success(cookingSession))
        
        // When
        val result = cookingGuidanceUseCase.startCookingSession(recipeId, userId, targetServings)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(cookingSession, result.getOrNull())
    }
    
    @Test
    fun `completeStep returns failure when step index is negative`() = runTest {
        // Given
        val sessionId = "session123"
        val stepIndex = -1
        
        // When
        val result = cookingGuidanceUseCase.completeStep(sessionId, stepIndex)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Step index must be non-negative", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `completeStep returns success when step index is valid`() = runTest {
        // Given
        val sessionId = "session123"
        val stepIndex = 0
        val updatedSession = createTestCookingSession().copy(completedSteps = "[0]")
        
        whenever(cookingRepository.completeStep(sessionId, stepIndex))
            .thenReturn(Result.success(updatedSession))
        
        // When
        val result = cookingGuidanceUseCase.completeStep(sessionId, stepIndex)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(updatedSession, result.getOrNull())
    }
    
    @Test
    fun `uncheckStep returns failure when step index is negative`() = runTest {
        // Given
        val sessionId = "session123"
        val stepIndex = -1
        
        // When
        val result = cookingGuidanceUseCase.uncheckStep(sessionId, stepIndex)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Step index must be non-negative", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `uncheckStep returns success when step index is valid`() = runTest {
        // Given
        val sessionId = "session123"
        val stepIndex = 0
        val updatedSession = createTestCookingSession().copy(completedSteps = "[]")
        
        whenever(cookingRepository.uncheckStep(sessionId, stepIndex))
            .thenReturn(Result.success(updatedSession))
        
        // When
        val result = cookingGuidanceUseCase.uncheckStep(sessionId, stepIndex)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(updatedSession, result.getOrNull())
    }
    
    @Test
    fun `startTimer returns failure when duration is zero`() = runTest {
        // Given
        val sessionId = "session123"
        val stepNumber = 1
        val name = "Test Timer"
        val durationMinutes = 0
        
        // When
        val result = cookingGuidanceUseCase.startTimer(sessionId, stepNumber, name, durationMinutes)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Timer duration must be greater than 0", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `startTimer returns failure when name is blank`() = runTest {
        // Given
        val sessionId = "session123"
        val stepNumber = 1
        val name = ""
        val durationMinutes = 5
        
        // When
        val result = cookingGuidanceUseCase.startTimer(sessionId, stepNumber, name, durationMinutes)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        assertEquals("Timer name cannot be blank", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `startTimer returns success when parameters are valid`() = runTest {
        // Given
        val sessionId = "session123"
        val stepNumber = 1
        val name = "Test Timer"
        val durationMinutes = 5
        val session = createTestCookingSession()
        val timer = createTestTimer()
        
        whenever(cookingRepository.startTimer(sessionId, stepNumber, name, durationMinutes))
            .thenReturn(Result.success(Pair(session, timer)))
        
        // When
        val result = cookingGuidanceUseCase.startTimer(sessionId, stepNumber, name, durationMinutes)
        
        // Then
        assertTrue(result.isSuccess)
        val (resultSession, resultTimer) = result.getOrThrow()
        assertEquals(session, resultSession)
        assertEquals(timer, resultTimer)
    }
    
    @Test
    fun `pauseCookingSession returns failure when session not found`() = runTest {
        // Given
        val sessionId = "nonexistent"
        
        whenever(cookingRepository.getCookingSessionById(sessionId))
            .thenReturn(Result.failure(Exception("Session not found")))
        
        // When
        val result = cookingGuidanceUseCase.pauseCookingSession(sessionId)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals("Session not found", result.exceptionOrNull()?.message)
    }
    
    @Test
    fun `pauseCookingSession returns success when session exists`() = runTest {
        // Given
        val sessionId = "session123"
        val session = createTestCookingSession()
        val pausedSession = session.copy(status = CookingStatus.PAUSED)
        
        whenever(cookingRepository.getCookingSessionById(sessionId))
            .thenReturn(Result.success(session))
        whenever(cookingRepository.updateCookingSession(pausedSession))
            .thenReturn(Result.success(Unit))
        
        // When
        val result = cookingGuidanceUseCase.pauseCookingSession(sessionId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(CookingStatus.PAUSED, result.getOrThrow().status)
    }
    
    @Test
    fun `resumeCookingSession returns success when session exists`() = runTest {
        // Given
        val sessionId = "session123"
        val session = createTestCookingSession().copy(status = CookingStatus.PAUSED)
        val resumedSession = session.copy(status = CookingStatus.IN_PROGRESS)
        
        whenever(cookingRepository.getCookingSessionById(sessionId))
            .thenReturn(Result.success(session))
        whenever(cookingRepository.updateCookingSession(resumedSession))
            .thenReturn(Result.success(Unit))
        
        // When
        val result = cookingGuidanceUseCase.resumeCookingSession(sessionId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(CookingStatus.IN_PROGRESS, result.getOrThrow().status)
    }
    
    @Test
    fun `getCookingSessionsByUser returns flow from repository`() {
        // Given
        val userId = "user123"
        val sessions = listOf(createTestCookingSession())
        val flow = flowOf(sessions)
        
        whenever(cookingRepository.getCookingSessionsByUser(userId))
            .thenReturn(flow)
        
        // When
        val result = cookingGuidanceUseCase.getCookingSessionsByUser(userId)
        
        // Then
        assertEquals(flow, result)
    }
    
    @Test
    fun `getActiveCookingSessions returns flow from repository`() {
        // Given
        val userId = "user123"
        val sessions = listOf(createTestCookingSession())
        val flow = flowOf(sessions)
        
        whenever(cookingRepository.getActiveCookingSessions(userId))
            .thenReturn(flow)
        
        // When
        val result = cookingGuidanceUseCase.getActiveCookingSessions(userId)
        
        // Then
        assertEquals(flow, result)
    }
    
    private fun createTestScaledRecipe(): ScaledRecipe {
        val recipe = Recipe(
            id = "recipe123",
            name = "Test Recipe",
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            instructions = "1::Mix ingredients||2::Cook for 30 minutes",
            nutritionInfo = "500,50,20,15",
            sourceType = RecipeSource.MANUAL,
            sourceUrl = null,
            rating = 4.5f,
            tags = "[]",
            createdAt = LocalDateTime.now().toString(),
            updatedAt = LocalDateTime.now().toString()
        )
        
        val scaledIngredients = listOf(
            ScaledIngredient(
                name = "Flour",
                originalQuantity = 2.0,
                scaledQuantity = 4.0,
                unit = "cups"
            )
        )
        
        val scaledSteps = listOf(
            CookingStep(
                stepNumber = 1,
                instruction = "Mix ingredients",
                scaledInstruction = "Mix ingredients"
            )
        )
        
        return ScaledRecipe(
            recipe = recipe,
            scaledIngredients = scaledIngredients,
            scaledSteps = scaledSteps,
            scalingFactor = 2.0f,
            targetServings = 4
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
    
    private fun createTestTimer(): CookingTimer {
        return CookingTimer(
            id = "timer123",
            stepNumber = 1,
            name = "Test Timer",
            durationMinutes = 5,
            startTime = System.currentTimeMillis(),
            isActive = true,
            isCompleted = false
        )
    }
}