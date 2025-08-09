package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface IngredientPreferenceRepository {
    
    // Ingredient Preferences
    fun getPreferencesForUser(userId: String): Flow<List<IngredientPreference>>
    fun getPreferencesByType(userId: String, type: PreferenceType): Flow<List<IngredientPreference>>
    suspend fun getPreferenceForIngredient(userId: String, ingredientName: String): IngredientPreference?
    suspend fun getPreferredIngredients(userId: String): List<IngredientPreference>
    suspend fun getDislikedIngredients(userId: String): List<IngredientPreference>
    suspend fun getAllergicIngredients(userId: String): List<IngredientPreference>
    suspend fun savePreference(userId: String, request: IngredientPreferenceRequest): Result<String>
    suspend fun updatePreference(preference: IngredientPreference): Result<Unit>
    suspend fun deletePreference(userId: String, ingredientName: String): Result<Unit>
    suspend fun searchIngredients(userId: String, query: String, limit: Int = 10): List<String>
    
    // Pantry Management
    fun getPantryItemsForUser(userId: String): Flow<List<PantryItem>>
    fun getPantryItemsByCategory(userId: String, category: IngredientCategory): Flow<List<PantryItem>>
    suspend fun getPantryItem(userId: String, ingredientName: String): PantryItem?
    fun getLowStockItems(userId: String): Flow<List<PantryItem>>
    fun getExpiringItems(userId: String): Flow<List<PantryItem>>
    fun getExpiredItems(userId: String): Flow<List<PantryItem>>
    suspend fun savePantryItem(userId: String, request: PantryItemRequest): Result<String>
    suspend fun updatePantryItem(item: PantryItem): Result<Unit>
    suspend fun deletePantryItem(userId: String, ingredientName: String): Result<Unit>
    suspend fun reduceQuantity(userId: String, ingredientName: String, usedQuantity: Double): Result<Unit>
    suspend fun addQuantity(userId: String, ingredientName: String, addedQuantity: Double): Result<Unit>
    suspend fun updateLowStockStatus(userId: String): Result<Unit>
    suspend fun searchPantryItems(userId: String, query: String, limit: Int = 10): List<PantryItem>
    suspend fun getPantryLocations(userId: String): List<String>
    
    // Usage History
    fun getUsageHistoryForUser(userId: String): Flow<List<IngredientUsageHistory>>
    fun getUsageHistoryForIngredient(userId: String, ingredientName: String): Flow<List<IngredientUsageHistory>>
    fun getRecentUsageHistory(userId: String): Flow<List<IngredientUsageHistory>>
    suspend fun getMostUsedIngredients(userId: String, limit: Int = 20): List<IngredientUsageStats>
    suspend fun getRecentlyUsedIngredients(userId: String, limit: Int = 20): List<IngredientUsageStats>
    suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats?
    suspend fun recordIngredientUsage(
        userId: String,
        ingredientName: String,
        quantityUsed: Double,
        unit: String,
        usageType: UsageType,
        recipeId: String? = null,
        mealId: String? = null
    ): Result<String>
    suspend fun searchUsedIngredients(userId: String, query: String, limit: Int = 10): List<String>
    
    // Analytics and Suggestions
    suspend fun getIngredientSuggestions(userId: String, limit: Int = 20): List<IngredientSuggestion>
    suspend fun getPantryAlerts(userId: String): List<PantryAlert>
    suspend fun getIngredientRecommendationsForMealPlan(userId: String): List<String>
    suspend fun syncWithMealPlan(userId: String, mealPlanId: String): Result<Unit>
}