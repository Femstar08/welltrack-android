package com.beaconledger.welltrack.data.cooking

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CookingGuidanceService @Inject constructor() {
    
    private val _activeTimers = MutableStateFlow<List<CookingTimer>>(emptyList())
    val activeTimers: StateFlow<List<CookingTimer>> = _activeTimers.asStateFlow()
    
    /**
     * Scales a recipe to the target number of servings
     */
    fun scaleRecipe(
        recipe: Recipe,
        ingredients: List<Ingredient>,
        targetServings: Int
    ): ScaledRecipe {
        val scalingFactor = targetServings.toFloat() / recipe.servings.toFloat()
        
        val scaledIngredients = ingredients.map { ingredient ->
            ScaledIngredient(
                name = ingredient.name,
                originalQuantity = ingredient.quantity,
                scaledQuantity = ingredient.quantity * scalingFactor,
                unit = ingredient.unit,
                category = ingredient.category,
                isOptional = ingredient.isOptional,
                notes = ingredient.notes
            )
        }
        
        val originalSteps = deserializeSteps(recipe.instructions)
        val scaledSteps = originalSteps.map { step ->
            CookingStep(
                stepNumber = step.stepNumber,
                instruction = step.instruction,
                duration = step.duration,
                temperature = step.temperature,
                equipment = step.equipment,
                scaledInstruction = scaleInstructionText(step.instruction, scalingFactor)
            )
        }
        
        val scaledRecipe = recipe.copy(
            servings = targetServings,
            nutritionInfo = scaleNutritionInfo(recipe.nutritionInfo, scalingFactor)
        )
        
        return ScaledRecipe(
            recipe = scaledRecipe,
            scaledIngredients = scaledIngredients,
            scaledSteps = scaledSteps,
            scalingFactor = scalingFactor,
            targetServings = targetServings
        )
    }
    
    /**
     * Creates a new cooking session
     */
    fun createCookingSession(
        recipeId: String,
        userId: String,
        scaledServings: Int,
        scalingFactor: Float
    ): CookingSession {
        return CookingSession(
            id = UUID.randomUUID().toString(),
            recipeId = recipeId,
            userId = userId,
            scaledServings = scaledServings,
            scalingFactor = scalingFactor,
            startedAt = LocalDateTime.now().toString(),
            currentStepIndex = 0,
            completedSteps = "[]",
            activeTimers = "[]",
            status = CookingStatus.IN_PROGRESS
        )
    }
    
    /**
     * Marks a step as completed in the cooking session
     */
    fun completeStep(session: CookingSession, stepIndex: Int): CookingSession {
        val completedSteps = deserializeCompletedSteps(session.completedSteps).toMutableList()
        if (!completedSteps.contains(stepIndex)) {
            completedSteps.add(stepIndex)
        }
        
        return session.copy(
            completedSteps = serializeCompletedSteps(completedSteps),
            currentStepIndex = if (stepIndex == session.currentStepIndex) stepIndex + 1 else session.currentStepIndex
        )
    }
    
    /**
     * Unchecks a completed step
     */
    fun uncheckStep(session: CookingSession, stepIndex: Int): CookingSession {
        val completedSteps = deserializeCompletedSteps(session.completedSteps).toMutableList()
        completedSteps.remove(stepIndex)
        
        return session.copy(
            completedSteps = serializeCompletedSteps(completedSteps)
        )
    }
    
    /**
     * Starts a timer for a cooking step
     */
    fun startTimer(
        session: CookingSession,
        stepNumber: Int,
        name: String,
        durationMinutes: Int
    ): Pair<CookingSession, CookingTimer> {
        val timer = CookingTimer(
            id = UUID.randomUUID().toString(),
            stepNumber = stepNumber,
            name = name,
            durationMinutes = durationMinutes,
            startTime = System.currentTimeMillis()
        )
        
        val activeTimers = deserializeActiveTimers(session.activeTimers).toMutableList()
        activeTimers.add(timer)
        
        val updatedSession = session.copy(
            activeTimers = serializeActiveTimers(activeTimers)
        )
        
        // Update the service's active timers
        _activeTimers.value = _activeTimers.value + timer
        
        return Pair(updatedSession, timer)
    }
    
    /**
     * Stops a timer
     */
    fun stopTimer(session: CookingSession, timerId: String): CookingSession {
        val activeTimers = deserializeActiveTimers(session.activeTimers).toMutableList()
        val timerIndex = activeTimers.indexOfFirst { it.id == timerId }
        
        if (timerIndex != -1) {
            val timer = activeTimers[timerIndex]
            activeTimers[timerIndex] = timer.copy(isActive = false, isCompleted = true)
            
            // Update the service's active timers
            _activeTimers.value = _activeTimers.value.map { 
                if (it.id == timerId) it.copy(isActive = false, isCompleted = true) else it
            }
        }
        
        return session.copy(
            activeTimers = serializeActiveTimers(activeTimers)
        )
    }
    
    /**
     * Completes the cooking session
     */
    fun completeCookingSession(session: CookingSession): CookingSession {
        // Stop all active timers
        val activeTimers = deserializeActiveTimers(session.activeTimers).map { timer ->
            timer.copy(isActive = false, isCompleted = true)
        }
        
        // Clear service's active timers for this session
        _activeTimers.value = _activeTimers.value.filter { timer ->
            !activeTimers.any { sessionTimer -> sessionTimer.id == timer.id }
        }
        
        return session.copy(
            completedAt = LocalDateTime.now().toString(),
            status = CookingStatus.COMPLETED,
            activeTimers = serializeActiveTimers(activeTimers)
        )
    }
    
    /**
     * Gets the remaining time for a timer in milliseconds
     */
    fun getTimerRemainingTime(timer: CookingTimer): Long {
        if (!timer.isActive || timer.isCompleted) return 0L
        
        val elapsedTime = System.currentTimeMillis() - timer.startTime
        val totalTime = timer.durationMinutes * 60 * 1000L
        val remainingTime = totalTime - elapsedTime
        
        return maxOf(0L, remainingTime)
    }
    
    /**
     * Checks if a timer has expired
     */
    fun isTimerExpired(timer: CookingTimer): Boolean {
        return getTimerRemainingTime(timer) <= 0L
    }
    
    // Helper methods for serialization/deserialization
    private fun deserializeSteps(serialized: String): List<RecipeStep> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split("|||").mapNotNull { stepStr ->
            val parts = stepStr.split("::")
            if (parts.size >= 2) {
                RecipeStep(
                    stepNumber = parts[0].toIntOrNull() ?: 1,
                    instruction = parts[1],
                    duration = parts.getOrNull(2)?.toIntOrNull(),
                    temperature = parts.getOrNull(3)?.takeIf { it.isNotEmpty() },
                    equipment = parts.getOrNull(4)?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                )
            } else null
        }
    }
    
    private fun deserializeCompletedSteps(serialized: String): List<Int> {
        if (serialized.isEmpty() || serialized == "[]") return emptyList()
        return try {
            serialized.removeSurrounding("[", "]")
                .split(",")
                .mapNotNull { it.trim().toIntOrNull() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun serializeCompletedSteps(steps: List<Int>): String {
        return "[${steps.joinToString(",")}]"
    }
    
    private fun deserializeActiveTimers(serialized: String): List<CookingTimer> {
        if (serialized.isEmpty() || serialized == "[]") return emptyList()
        return try {
            // Simple serialization for now - in production, use JSON
            emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun serializeActiveTimers(timers: List<CookingTimer>): String {
        // Simple serialization for now - in production, use JSON
        return "[]"
    }
    
    private fun scaleInstructionText(instruction: String, scalingFactor: Float): String {
        // Basic scaling of quantities in instruction text
        // This is a simplified implementation - in production, you'd want more sophisticated parsing
        return instruction
    }
    
    private fun scaleNutritionInfo(nutritionInfoSerialized: String, scalingFactor: Float): String {
        if (nutritionInfoSerialized.isEmpty()) return nutritionInfoSerialized
        
        val parts = nutritionInfoSerialized.split(",")
        val scaledParts = parts.map { part ->
            val value = part.toDoubleOrNull() ?: 0.0
            (value * scalingFactor).toString()
        }
        
        return scaledParts.joinToString(",")
    }
}