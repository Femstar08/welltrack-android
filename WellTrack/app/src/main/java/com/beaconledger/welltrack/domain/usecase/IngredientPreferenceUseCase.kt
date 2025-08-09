package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.IngredientPreferenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientPreferenceUseCase @Inject constructor(
    private val repository: IngredientPreferenceRepository
) {
    
    // Ingredient Preferences
    fun getPreferencesForUser(userId: String): Flow<List<IngredientPreference>> {
        return repository.getPreferencesForUser(userId)
    }
    
    fun getPreferencesByType(userId: String, type: PreferenceType): Flow<List<IngredientPreference>> {
        return repository.getPreferencesByType(userId, type)
    }
    
    suspend fun addPreferredIngredient(userId: String, ingredientName: String, priority: Int = 5): Result<String> {
        return repository.savePreference(
            userId,
            IngredientPreferenceRequest(
                ingredientName = ingredientName,
                preferenceType = PreferenceType.PREFERRED,
                priority = priority
            )
        )
    }
    
    suspend fun addDislikedIngredient(userId: String, ingredientName: String, notes: String? = null): Result<String> {
        return repository.savePreference(
            userId,
            IngredientPreferenceRequest(
                ingredientName = ingredientName,
                preferenceType = PreferenceType.DISLIKED,
                notes = notes
            )
        )
    }
    
    suspend fun addAllergicIngredient(userId: String, ingredientName: String, notes: String? = null): Result<String> {
        return repository.savePreference(
            userId,
            IngredientPreferenceRequest(
                ingredientName = ingredientName,
                preferenceType = PreferenceType.ALLERGIC,
                notes = notes
            )
        )
    }
    
    suspend fun updatePreferencePriority(preference: IngredientPreference, newPriority: Int): Result<Unit> {
        return repository.updatePreference(preference.copy(priority = newPriority))
    }
    
    suspend fun removePreference(userId: String, ingredientName: String): Result<Unit> {
        return repository.deletePreference(userId, ingredientName)
    }
    
    suspend fun searchIngredients(userId: String, query: String): List<String> {
        return repository.searchIngredients(userId, query)
    }    

    // Pantry Management
    fun getPantryItemsForUser(userId: String): Flow<List<PantryItem>> {
        return repository.getPantryItemsForUser(userId)
    }
    
    fun getPantryItemsByCategory(userId: String, category: IngredientCategory): Flow<List<PantryItem>> {
        return repository.getPantryItemsByCategory(userId, category)
    }
    
    fun getLowStockItems(userId: String): Flow<List<PantryItem>> {
        return repository.getLowStockItems(userId)
    }
    
    fun getExpiringItems(userId: String): Flow<List<PantryItem>> {
        return repository.getExpiringItems(userId)
    }
    
    suspend fun addPantryItem(
        userId: String,
        ingredientName: String,
        quantity: Double,
        unit: String,
        category: IngredientCategory,
        expiryDate: String? = null,
        location: String? = null
    ): Result<String> {
        return repository.savePantryItem(
            userId,
            PantryItemRequest(
                ingredientName = ingredientName,
                quantity = quantity,
                unit = unit,
                category = category,
                expiryDate = expiryDate,
                location = location
            )
        )
    }
    
    suspend fun updatePantryItemQuantity(userId: String, ingredientName: String, newQuantity: Double): Result<Unit> {
        val item = repository.getPantryItem(userId, ingredientName)
        return if (item != null) {
            repository.updatePantryItem(item.copy(quantity = newQuantity))
        } else {
            Result.failure(Exception("Pantry item not found"))
        }
    }
    
    suspend fun consumeIngredient(
        userId: String,
        ingredientName: String,
        quantityUsed: Double,
        unit: String,
        usageType: UsageType,
        recipeId: String? = null,
        mealId: String? = null
    ): Result<String> {
        return repository.recordIngredientUsage(
            userId, ingredientName, quantityUsed, unit, usageType, recipeId, mealId
        )
    }
    
    suspend fun removePantryItem(userId: String, ingredientName: String): Result<Unit> {
        return repository.deletePantryItem(userId, ingredientName)
    }
    
    suspend fun searchPantryItems(userId: String, query: String): List<PantryItem> {
        return repository.searchPantryItems(userId, query)
    }   
 
    // Analytics and Insights
    suspend fun getIngredientSuggestions(userId: String): List<IngredientSuggestion> {
        return repository.getIngredientSuggestions(userId)
    }
    
    suspend fun getPantryAlerts(userId: String): List<PantryAlert> {
        return repository.getPantryAlerts(userId)
    }
    
    suspend fun getMostUsedIngredients(userId: String, limit: Int = 10): List<IngredientUsageStats> {
        return repository.getMostUsedIngredients(userId, limit)
    }
    
    suspend fun getRecentlyUsedIngredients(userId: String, limit: Int = 10): List<IngredientUsageStats> {
        return repository.getRecentlyUsedIngredients(userId, limit)
    }
    
    suspend fun getIngredientRecommendationsForMealPlan(userId: String): List<String> {
        return repository.getIngredientRecommendationsForMealPlan(userId)
    }
    
    fun getUsageHistoryForIngredient(userId: String, ingredientName: String): Flow<List<IngredientUsageHistory>> {
        return repository.getUsageHistoryForIngredient(userId, ingredientName)
    }
    
    suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats? {
        return repository.getIngredientUsageStats(userId, ingredientName)
    }
    
    // Meal Plan Integration
    suspend fun getPreferredIngredientsForMealPlanning(userId: String): List<String> {
        val preferences = repository.getPreferredIngredients(userId)
        return preferences.sortedByDescending { it.priority }.map { it.ingredientName }
    }
    
    suspend fun getAvailableIngredientsFromPantry(userId: String): List<String> {
        val pantryItems = repository.getPantryItemsForUser(userId)
        val availableIngredients = mutableListOf<String>()
        
        pantryItems.collect { items ->
            items.filter { it.quantity > 0 }.forEach { item ->
                availableIngredients.add(item.ingredientName)
            }
        }
        
        return availableIngredients
    }
    
    suspend fun shouldAvoidIngredient(userId: String, ingredientName: String): Boolean {
        val preference = repository.getPreferenceForIngredient(userId, ingredientName)
        return preference?.preferenceType in listOf(PreferenceType.DISLIKED, PreferenceType.ALLERGIC)
    }
    
    suspend fun getIngredientPreferenceScore(userId: String, ingredientName: String): Int {
        val preference = repository.getPreferenceForIngredient(userId, ingredientName)
        val usageStats = repository.getIngredientUsageStats(userId, ingredientName)
        
        return when {
            preference?.preferenceType == PreferenceType.ALLERGIC -> -100
            preference?.preferenceType == PreferenceType.DISLIKED -> -50
            preference?.preferenceType == PreferenceType.PREFERRED -> preference.priority * 10
            usageStats != null -> usageStats.usageCount
            else -> 0
        }
    }
}