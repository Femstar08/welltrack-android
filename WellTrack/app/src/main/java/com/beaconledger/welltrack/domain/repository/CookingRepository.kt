package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface CookingRepository {
    fun getCookingSessionsByUser(userId: String): Flow<List<CookingSession>>
    suspend fun getCookingSessionById(sessionId: String): Result<CookingSession?>
    suspend fun getLatestCookingSessionForRecipe(recipeId: String, userId: String): Result<CookingSession?>
    fun getActiveCookingSessions(userId: String): Flow<List<CookingSession>>
    suspend fun saveCookingSession(session: CookingSession): Result<String>
    suspend fun updateCookingSession(session: CookingSession): Result<Unit>
    suspend fun deleteCookingSession(sessionId: String): Result<Unit>
    suspend fun scaleRecipe(recipeId: String, targetServings: Int): Result<ScaledRecipe>
    suspend fun startCookingSession(recipeId: String, userId: String, targetServings: Int): Result<CookingSession>
    suspend fun completeStep(sessionId: String, stepIndex: Int): Result<CookingSession>
    suspend fun uncheckStep(sessionId: String, stepIndex: Int): Result<CookingSession>
    suspend fun startTimer(sessionId: String, stepNumber: Int, name: String, durationMinutes: Int): Result<Pair<CookingSession, CookingTimer>>
    suspend fun stopTimer(sessionId: String, timerId: String): Result<CookingSession>
    suspend fun completeCookingSession(sessionId: String): Result<CookingSession>
}