package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.cooking.CookingGuidanceService
import com.beaconledger.welltrack.data.database.dao.CookingSessionDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.CookingRepository
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookingRepositoryImpl @Inject constructor(
    private val cookingSessionDao: CookingSessionDao,
    private val recipeRepository: RecipeRepository,
    private val cookingGuidanceService: CookingGuidanceService
) : CookingRepository {

    override fun getCookingSessionsByUser(userId: String): Flow<List<CookingSession>> {
        return cookingSessionDao.getCookingSessionsByUser(userId)
    }

    override suspend fun getCookingSessionById(sessionId: String): Result<CookingSession?> {
        return try {
            val session = cookingSessionDao.getCookingSessionById(sessionId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLatestCookingSessionForRecipe(recipeId: String, userId: String): Result<CookingSession?> {
        return try {
            val session = cookingSessionDao.getLatestCookingSessionForRecipe(recipeId, userId)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getActiveCookingSessions(userId: String): Flow<List<CookingSession>> {
        return cookingSessionDao.getActiveCookingSessions(userId)
    }

    override suspend fun saveCookingSession(session: CookingSession): Result<String> {
        return try {
            cookingSessionDao.insertCookingSession(session)
            Result.success(session.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateCookingSession(session: CookingSession): Result<Unit> {
        return try {
            cookingSessionDao.updateCookingSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCookingSession(sessionId: String): Result<Unit> {
        return try {
            cookingSessionDao.deleteCookingSession(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scaleRecipe(recipeId: String, targetServings: Int): Result<ScaledRecipe> {
        return try {
            val recipeResult = recipeRepository.getRecipeById(recipeId)
            if (recipeResult.isFailure) {
                return Result.failure(recipeResult.exceptionOrNull() ?: Exception("Recipe not found"))
            }
            
            val recipe = recipeResult.getOrNull() ?: return Result.failure(Exception("Recipe not found"))
            
            val ingredientsResult = recipeRepository.getRecipeIngredients(recipeId)
            if (ingredientsResult.isFailure) {
                return Result.failure(ingredientsResult.exceptionOrNull() ?: Exception("Ingredients not found"))
            }
            
            val ingredients = ingredientsResult.getOrThrow()
            val scaledRecipe = cookingGuidanceService.scaleRecipe(recipe, ingredients, targetServings)
            
            Result.success(scaledRecipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startCookingSession(recipeId: String, userId: String, targetServings: Int): Result<CookingSession> {
        return try {
            val recipeResult = recipeRepository.getRecipeById(recipeId)
            if (recipeResult.isFailure) {
                return Result.failure(recipeResult.exceptionOrNull() ?: Exception("Recipe not found"))
            }
            
            val recipe = recipeResult.getOrNull() ?: return Result.failure(Exception("Recipe not found"))
            val scalingFactor = targetServings.toFloat() / recipe.servings.toFloat()
            
            val session = cookingGuidanceService.createCookingSession(
                recipeId = recipeId,
                userId = userId,
                scaledServings = targetServings,
                scalingFactor = scalingFactor
            )
            
            cookingSessionDao.insertCookingSession(session)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeStep(sessionId: String, stepIndex: Int): Result<CookingSession> {
        return try {
            val session = cookingSessionDao.getCookingSessionById(sessionId)
                ?: return Result.failure(Exception("Cooking session not found"))
            
            val updatedSession = cookingGuidanceService.completeStep(session, stepIndex)
            cookingSessionDao.updateCookingSession(updatedSession)
            
            Result.success(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun uncheckStep(sessionId: String, stepIndex: Int): Result<CookingSession> {
        return try {
            val session = cookingSessionDao.getCookingSessionById(sessionId)
                ?: return Result.failure(Exception("Cooking session not found"))
            
            val updatedSession = cookingGuidanceService.uncheckStep(session, stepIndex)
            cookingSessionDao.updateCookingSession(updatedSession)
            
            Result.success(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun startTimer(sessionId: String, stepNumber: Int, name: String, durationMinutes: Int): Result<Pair<CookingSession, CookingTimer>> {
        return try {
            val session = cookingSessionDao.getCookingSessionById(sessionId)
                ?: return Result.failure(Exception("Cooking session not found"))
            
            val (updatedSession, timer) = cookingGuidanceService.startTimer(session, stepNumber, name, durationMinutes)
            cookingSessionDao.updateCookingSession(updatedSession)
            
            Result.success(Pair(updatedSession, timer))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun stopTimer(sessionId: String, timerId: String): Result<CookingSession> {
        return try {
            val session = cookingSessionDao.getCookingSessionById(sessionId)
                ?: return Result.failure(Exception("Cooking session not found"))
            
            val updatedSession = cookingGuidanceService.stopTimer(session, timerId)
            cookingSessionDao.updateCookingSession(updatedSession)
            
            Result.success(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeCookingSession(sessionId: String): Result<CookingSession> {
        return try {
            val session = cookingSessionDao.getCookingSessionById(sessionId)
                ?: return Result.failure(Exception("Cooking session not found"))
            
            val completedSession = cookingGuidanceService.completeCookingSession(session)
            cookingSessionDao.updateCookingSession(completedSession)
            
            Result.success(completedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}