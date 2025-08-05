package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.CookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookingGuidanceUseCase @Inject constructor(
    private val cookingRepository: CookingRepository
) {
    
    fun getCookingSessionsByUser(userId: String): Flow<List<CookingSession>> {
        return cookingRepository.getCookingSessionsByUser(userId)
    }
    
    fun getActiveCookingSessions(userId: String): Flow<List<CookingSession>> {
        return cookingRepository.getActiveCookingSessions(userId)
    }
    
    suspend fun getCookingSessionById(sessionId: String): Result<CookingSession?> {
        return cookingRepository.getCookingSessionById(sessionId)
    }
    
    suspend fun scaleRecipe(recipeId: String, targetServings: Int): Result<ScaledRecipe> {
        if (targetServings <= 0) {
            return Result.failure(IllegalArgumentException("Target servings must be greater than 0"))
        }
        
        return cookingRepository.scaleRecipe(recipeId, targetServings)
    }
    
    suspend fun startCookingSession(recipeId: String, userId: String, targetServings: Int): Result<CookingSession> {
        if (targetServings <= 0) {
            return Result.failure(IllegalArgumentException("Target servings must be greater than 0"))
        }
        
        return cookingRepository.startCookingSession(recipeId, userId, targetServings)
    }
    
    suspend fun completeStep(sessionId: String, stepIndex: Int): Result<CookingSession> {
        if (stepIndex < 0) {
            return Result.failure(IllegalArgumentException("Step index must be non-negative"))
        }
        
        return cookingRepository.completeStep(sessionId, stepIndex)
    }
    
    suspend fun uncheckStep(sessionId: String, stepIndex: Int): Result<CookingSession> {
        if (stepIndex < 0) {
            return Result.failure(IllegalArgumentException("Step index must be non-negative"))
        }
        
        return cookingRepository.uncheckStep(sessionId, stepIndex)
    }
    
    suspend fun startTimer(sessionId: String, stepNumber: Int, name: String, durationMinutes: Int): Result<Pair<CookingSession, CookingTimer>> {
        if (durationMinutes <= 0) {
            return Result.failure(IllegalArgumentException("Timer duration must be greater than 0"))
        }
        
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Timer name cannot be blank"))
        }
        
        return cookingRepository.startTimer(sessionId, stepNumber, name, durationMinutes)
    }
    
    suspend fun stopTimer(sessionId: String, timerId: String): Result<CookingSession> {
        return cookingRepository.stopTimer(sessionId, timerId)
    }
    
    suspend fun completeCookingSession(sessionId: String): Result<CookingSession> {
        return cookingRepository.completeCookingSession(sessionId)
    }
    
    suspend fun pauseCookingSession(sessionId: String): Result<CookingSession> {
        val sessionResult = cookingRepository.getCookingSessionById(sessionId)
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull() ?: Exception("Session not found"))
        }
        
        val session = sessionResult.getOrNull() ?: return Result.failure(Exception("Session not found"))
        val pausedSession = session.copy(status = CookingStatus.PAUSED)
        
        val updateResult = cookingRepository.updateCookingSession(pausedSession)
        return if (updateResult.isSuccess) {
            Result.success(pausedSession)
        } else {
            Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to pause session"))
        }
    }
    
    suspend fun resumeCookingSession(sessionId: String): Result<CookingSession> {
        val sessionResult = cookingRepository.getCookingSessionById(sessionId)
        if (sessionResult.isFailure) {
            return Result.failure(sessionResult.exceptionOrNull() ?: Exception("Session not found"))
        }
        
        val session = sessionResult.getOrNull() ?: return Result.failure(Exception("Session not found"))
        val resumedSession = session.copy(status = CookingStatus.IN_PROGRESS)
        
        val updateResult = cookingRepository.updateCookingSession(resumedSession)
        return if (updateResult.isSuccess) {
            Result.success(resumedSession)
        } else {
            Result.failure(updateResult.exceptionOrNull() ?: Exception("Failed to resume session"))
        }
    }
}