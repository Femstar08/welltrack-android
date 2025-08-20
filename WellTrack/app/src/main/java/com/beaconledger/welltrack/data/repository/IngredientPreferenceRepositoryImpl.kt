package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.IngredientPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IngredientPreferenceRepositoryImpl @Inject constructor(
    private val ingredientPreferenceDao: IngredientPreferenceDao,
    private val pantryDao: PantryDao,
    private val ingredientUsageDao: IngredientUsageDao
) : IngredientPreferenceRepository {
    
    // Ingredient Preferences
    override fun getPreferencesForUser(userId: String): Flow<List<IngredientPreference>> {
        return ingredientPreferenceDao.getPreferencesForUser(userId)
    }
    
    override fun getPreferencesByType(userId: String, type: PreferenceType): Flow<List<IngredientPreference>> {
        return ingredientPreferenceDao.getPreferencesByType(userId, type)
    }
    
    override suspend fun getPreferenceForIngredient(userId: String, ingredientName: String): IngredientPreference? {
        return ingredientPreferenceDao.getPreferenceForIngredient(userId, ingredientName)
    }
    
    override suspend fun getPreferredIngredients(userId: String): List<IngredientPreference> {
        return ingredientPreferenceDao.getPreferredIngredients(userId)
    }
    
    override suspend fun getDislikedIngredients(userId: String): List<IngredientPreference> {
        return ingredientPreferenceDao.getDislikedIngredients(userId)
    }
    
    override suspend fun getAllergicIngredients(userId: String): List<IngredientPreference> {
        return ingredientPreferenceDao.getAllergicIngredients(userId)
    }
    
    override suspend fun savePreference(userId: String, request: IngredientPreferenceRequest): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val preference = IngredientPreference(
                id = id,
                userId = userId,
                ingredientName = request.ingredientName,
                preferenceType = request.preferenceType,
                priority = request.priority,
                notes = request.notes
            )
            ingredientPreferenceDao.insertPreference(preference)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }    

    override suspend fun updatePreference(preference: IngredientPreference): Result<Unit> {
        return try {
            val updatedPreference = preference.copy(updatedAt = LocalDateTime.now().toString())
            ingredientPreferenceDao.updatePreference(updatedPreference)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePreference(userId: String, ingredientName: String): Result<Unit> {
        return try {
            ingredientPreferenceDao.deletePreferenceForIngredient(userId, ingredientName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchIngredients(userId: String, query: String, limit: Int): List<String> {
        return ingredientPreferenceDao.searchIngredients(userId, query, limit)
    }
    
    // Pantry Management
    override fun getPantryItemsForUser(userId: String): Flow<List<PantryItem>> {
        return pantryDao.getPantryItemsForUser(userId)
    }
    
    override fun getPantryItemsByCategory(userId: String, category: IngredientCategory): Flow<List<PantryItem>> {
        return pantryDao.getPantryItemsByCategory(userId, category)
    }
    
    override suspend fun getPantryItem(userId: String, ingredientName: String): PantryItem? {
        return pantryDao.getPantryItem(userId, ingredientName)
    }
    
    override fun getLowStockItems(userId: String): Flow<List<PantryItem>> {
        return pantryDao.getLowStockItems(userId)
    }
    
    override fun getExpiringItems(userId: String): Flow<List<PantryItem>> {
        return pantryDao.getExpiringItems(userId)
    }
    
    override fun getExpiredItems(userId: String): Flow<List<PantryItem>> {
        return pantryDao.getExpiredItems(userId)
    }
    
    override suspend fun savePantryItem(userId: String, request: PantryItemRequest): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val item = PantryItem(
                id = id,
                userId = userId,
                ingredientName = request.ingredientName,
                quantity = request.quantity,
                unit = request.unit,
                category = request.category,
                purchaseDate = request.purchaseDate,
                expiryDate = request.expiryDate,
                barcode = request.barcode,
                location = request.location,
                minimumQuantity = request.minimumQuantity,
                isLowStock = request.minimumQuantity?.let { request.quantity <= it } ?: false
            )
            pantryDao.insertPantryItem(item)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }  
  
    override suspend fun updatePantryItem(item: PantryItem): Result<Unit> {
        return try {
            val updatedItem = item.copy(updatedAt = LocalDateTime.now().toString())
            pantryDao.updatePantryItem(updatedItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deletePantryItem(userId: String, ingredientName: String): Result<Unit> {
        return try {
            pantryDao.deletePantryItemByName(userId, ingredientName)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun reduceQuantity(userId: String, ingredientName: String, usedQuantity: Double): Result<Unit> {
        return try {
            pantryDao.reduceQuantity(userId, ingredientName, usedQuantity)
            pantryDao.updateLowStockStatus(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun addQuantity(userId: String, ingredientName: String, addedQuantity: Double): Result<Unit> {
        return try {
            pantryDao.addQuantity(userId, ingredientName, addedQuantity)
            pantryDao.updateLowStockStatus(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateLowStockStatus(userId: String): Result<Unit> {
        return try {
            pantryDao.updateLowStockStatus(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchPantryItems(userId: String, query: String, limit: Int): List<PantryItem> {
        return pantryDao.searchPantryItems(userId, query, limit)
    }
    
    override suspend fun getPantryLocations(userId: String): List<String> {
        return pantryDao.getPantryLocations(userId)
    } 
   
    // Usage History
    override fun getUsageHistoryForUser(userId: String): Flow<List<IngredientUsageHistory>> {
        return ingredientUsageDao.getUsageHistoryForUser(userId)
    }
    
    override fun getUsageHistoryForIngredient(userId: String, ingredientName: String): Flow<List<IngredientUsageHistory>> {
        return ingredientUsageDao.getUsageHistoryForIngredient(userId, ingredientName)
    }
    
    override fun getRecentUsageHistory(userId: String): Flow<List<IngredientUsageHistory>> {
        return flow {
            emit(ingredientUsageDao.getRecentUsageHistory(userId))
        }
    }
    
    override suspend fun getMostUsedIngredients(userId: String, limit: Int): List<IngredientUsageStats> {
        return ingredientUsageDao.getMostUsedIngredients(userId, limit).map { frequency ->
            IngredientUsageStats(
                ingredientName = frequency.ingredientName,
                usageCount = frequency.usageCount,
                lastUsed = "",
                firstUsed = "",
                totalQuantityUsed = 0.0,
                averageQuantityPerUse = 0.0,
                mostCommonUnit = ""
            )
        }
    }
    
    override suspend fun getRecentlyUsedIngredients(userId: String, limit: Int): List<IngredientUsageStats> {
        return ingredientUsageDao.getRecentlyUsedIngredients(userId).take(limit).map { ingredientName ->
            IngredientUsageStats(
                ingredientName = ingredientName,
                usageCount = 0,
                lastUsed = "",
                firstUsed = "",
                totalQuantityUsed = 0.0,
                averageQuantityPerUse = 0.0,
                mostCommonUnit = ""
            )
        }
    }
    
    override suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats? {
        return ingredientUsageDao.getIngredientUsageStats(userId, ingredientName)
    }
    
    override suspend fun recordIngredientUsage(
        userId: String,
        ingredientName: String,
        quantityUsed: Double,
        unit: String,
        usageType: UsageType,
        recipeId: String?,
        mealId: String?
    ): Result<String> {
        return try {
            val id = UUID.randomUUID().toString()
            val usage = IngredientUsageHistory(
                id = id,
                userId = userId,
                ingredientName = ingredientName,
                recipeId = recipeId,
                mealId = mealId,
                quantityUsed = quantityUsed,
                unit = unit,
                usageDate = LocalDateTime.now().toString(),
                usageType = usageType
            )
            ingredientUsageDao.insertUsageHistory(usage)
            
            // Also reduce pantry quantity if item exists
            val pantryItem = pantryDao.getPantryItem(userId, ingredientName)
            if (pantryItem != null) {
                reduceQuantity(userId, ingredientName, quantityUsed)
            }
            
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun searchUsedIngredients(userId: String, query: String, limit: Int): List<String> {
        return ingredientUsageDao.searchUsedIngredients(userId, query, limit)
    }    
   
 // Analytics and Suggestions
    override suspend fun getIngredientSuggestions(userId: String, limit: Int): List<IngredientSuggestion> {
        return try {
            val preferences = ingredientPreferenceDao.getPreferredIngredients(userId)
            val usageStats = ingredientUsageDao.getMostUsedIngredients(userId, limit * 2)
            
            val suggestions = mutableListOf<IngredientSuggestion>()
            
            // Add preferred ingredients
            preferences.forEach { pref ->
                val pantryItem = pantryDao.getPantryItem(userId, pref.ingredientName)
                val ingredientUsageStats = ingredientUsageDao.getIngredientUsageStats(userId, pref.ingredientName)
                
                suggestions.add(
                    IngredientSuggestion(
                        ingredientName = pref.ingredientName,
                        category = IngredientCategory.OTHER, // Would need to determine from recipe data
                        isPreferred = true,
                        isInPantry = pantryItem != null,
                        pantryQuantity = pantryItem?.quantity,
                        pantryUnit = pantryItem?.unit,
                        usageFrequency = ingredientUsageStats?.usageCount ?: 0,
                        lastUsed = ingredientUsageStats?.lastUsed,
                        priority = pref.priority
                    )
                )
            }
            
            // Add frequently used ingredients not already preferred
            usageStats.forEach { stats ->
                if (suggestions.none { it.ingredientName == stats.ingredientName }) {
                    val pantryItem = pantryDao.getPantryItem(userId, stats.ingredientName)
                    val preference = ingredientPreferenceDao.getPreferenceForIngredient(userId, stats.ingredientName)
                    
                    suggestions.add(
                        IngredientSuggestion(
                            ingredientName = stats.ingredientName,
                            category = IngredientCategory.OTHER,
                            isPreferred = preference?.preferenceType == PreferenceType.PREFERRED,
                            isInPantry = pantryItem != null,
                            pantryQuantity = pantryItem?.quantity,
                            pantryUnit = pantryItem?.unit,
                            usageFrequency = stats.usageCount,
                            lastUsed = "",
                            priority = preference?.priority ?: 0
                        )
                    )
                }
            }
            
            suggestions.sortedByDescending { it.priority * 10 + it.usageFrequency }.take(limit)
        } catch (e: Exception) {
            emptyList()
        }
    }  
  
    override suspend fun getPantryAlerts(userId: String): List<PantryAlert> {
        return try {
            val alerts = mutableListOf<PantryAlert>()
            
            // Get expiring items (using first() to get current value from Flow)
            val expiringItems = pantryDao.getExpiringItems(userId).first()
            expiringItems.forEach { item ->
                val daysUntilExpiry = item.expiryDate?.let { 
                    // Calculate days until expiry (simplified)
                    7 // Placeholder - would calculate actual days
                }
                
                alerts.add(
                    PantryAlert(
                        id = UUID.randomUUID().toString(),
                        ingredientName = item.ingredientName,
                        alertType = if (daysUntilExpiry != null && daysUntilExpiry <= 0) AlertType.EXPIRED else AlertType.EXPIRY_WARNING,
                        message = if (daysUntilExpiry != null && daysUntilExpiry <= 0) 
                            "${item.ingredientName} has expired" 
                        else 
                            "${item.ingredientName} expires in $daysUntilExpiry days",
                        severity = when {
                            daysUntilExpiry != null && daysUntilExpiry <= 0 -> AlertSeverity.CRITICAL
                            daysUntilExpiry != null && daysUntilExpiry <= 2 -> AlertSeverity.HIGH
                            daysUntilExpiry != null && daysUntilExpiry <= 5 -> AlertSeverity.MEDIUM
                            else -> AlertSeverity.LOW
                        },
                        expiryDate = item.expiryDate,
                        daysUntilExpiry = daysUntilExpiry,
                        currentQuantity = item.quantity,
                        minimumQuantity = item.minimumQuantity
                    )
                )
            }
            
            // Get low stock items (using first() to get current value from Flow)
            val lowStockItems = pantryDao.getLowStockItems(userId).first()
            lowStockItems.forEach { item ->
                alerts.add(
                    PantryAlert(
                        id = UUID.randomUUID().toString(),
                        ingredientName = item.ingredientName,
                        alertType = if (item.quantity <= 0) AlertType.OUT_OF_STOCK else AlertType.LOW_STOCK,
                        message = if (item.quantity <= 0) 
                            "${item.ingredientName} is out of stock" 
                        else 
                            "${item.ingredientName} is running low (${item.quantity} ${item.unit})",
                        severity = if (item.quantity <= 0) AlertSeverity.HIGH else AlertSeverity.MEDIUM,
                        expiryDate = null,
                        daysUntilExpiry = null,
                        currentQuantity = item.quantity,
                        minimumQuantity = item.minimumQuantity
                    )
                )
            }
            
            alerts.sortedByDescending { it.severity.ordinal }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun getIngredientRecommendationsForMealPlan(userId: String): List<String> {
        return try {
            val preferred = ingredientPreferenceDao.getPreferredIngredients(userId)
            val recentlyUsed = ingredientUsageDao.getRecentlyUsedIngredients(userId).take(10)
            
            val recommendations = mutableSetOf<String>()
            
            // Add preferred ingredients
            preferred.forEach { recommendations.add(it.ingredientName) }
            
            // Add recently used ingredients
            recentlyUsed.forEach { recommendations.add(it) }
            
            recommendations.toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun syncWithMealPlan(userId: String, mealPlanId: String): Result<Unit> {
        return try {
            // This would sync ingredient usage with meal plan data
            // Implementation would depend on meal plan structure
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}