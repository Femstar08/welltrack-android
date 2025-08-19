package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.PantryRepository
import com.beaconledger.welltrack.domain.repository.ExpiringItemWithSuggestions
import com.beaconledger.welltrack.domain.repository.ProductInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryUseCase @Inject constructor(
    private val pantryRepository: PantryRepository
) {
    
    fun getPantryItemsForUser(userId: String): Flow<List<PantryItem>> {
        return pantryRepository.getPantryItemsForUser(userId)
    }
    
    fun getPantryItemsByCategory(userId: String, category: IngredientCategory): Flow<List<PantryItem>> {
        return pantryRepository.getPantryItemsByCategory(userId, category)
    }
    
    suspend fun addPantryItem(userId: String, request: PantryItemRequest): Result<PantryItem> {
        return pantryRepository.addPantryItem(userId, request)
    }
    
    suspend fun updatePantryItem(userId: String, ingredientName: String, request: PantryUpdateRequest): Result<PantryItem> {
        return pantryRepository.updatePantryItem(userId, ingredientName, request)
    }
    
    suspend fun deletePantryItem(userId: String, ingredientName: String): Result<Unit> {
        return pantryRepository.deletePantryItem(userId, ingredientName)
    }
    
    suspend fun updateQuantity(userId: String, ingredientName: String, newQuantity: Double): Result<Unit> {
        return pantryRepository.updateQuantity(userId, ingredientName, newQuantity)
    }
    
    // Barcode scanning functionality
    suspend fun addPantryItemByBarcode(userId: String, barcode: String): Result<PantryItem> {
        return pantryRepository.addPantryItemByBarcode(userId, barcode)
    }
    
    suspend fun getProductInfoByBarcode(barcode: String): Result<ProductInfo> {
        return pantryRepository.getProductInfoByBarcode(barcode)
    }
    
    // Expiry and alerts functionality
    fun getPantryAlerts(userId: String): Flow<PantryAlertsData> {
        return combine(
            pantryRepository.getLowStockItems(userId),
            pantryRepository.getExpiringItems(userId),
            pantryRepository.getExpiredItems(userId)
        ) { lowStock, expiring, expired ->
            PantryAlertsData(
                lowStockItems = lowStock,
                expiringItems = expiring,
                expiredItems = expired,
                totalAlerts = lowStock.size + expiring.size + expired.size
            )
        }
    }
    
    suspend fun getExpiringItemsWithRecipeSuggestions(userId: String): List<ExpiringItemWithSuggestions> {
        return pantryRepository.getExpiringItemsWithRecipeSuggestions(userId)
    }
    
    suspend fun getPantryAlertsDetailed(userId: String): List<PantryAlert> {
        return pantryRepository.getPantryAlerts(userId)
    }
    
    // Shopping list integration
    suspend fun syncFromShoppingList(userId: String, shoppingListItems: List<ShoppingListItem>): Result<Unit> {
        return pantryRepository.syncFromShoppingList(userId, shoppingListItems)
    }
    
    suspend fun markItemAsPurchased(userId: String, ingredientName: String, quantity: Double, unit: String): Result<Unit> {
        return pantryRepository.markItemAsPurchased(userId, ingredientName, quantity, unit)
    }
    
    // Usage tracking
    suspend fun recordIngredientUsage(
        userId: String,
        ingredientName: String,
        quantityUsed: Double,
        unit: String,
        recipeId: String? = null,
        mealId: String? = null,
        usageType: UsageType = UsageType.RECIPE
    ): Result<Unit> {
        val usage = IngredientUsageHistory(
            id = UUID.randomUUID().toString(),
            userId = userId,
            ingredientName = ingredientName,
            recipeId = recipeId,
            mealId = mealId,
            quantityUsed = quantityUsed,
            unit = unit,
            usageDate = LocalDateTime.now().toString(),
            usageType = usageType
        )
        
        return pantryRepository.recordIngredientUsage(userId, usage)
    }
    
    suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats? {
        return pantryRepository.getIngredientUsageStats(userId, ingredientName)
    }
    
    // Search and utility functions
    suspend fun searchPantryItems(userId: String, query: String, limit: Int = 10): List<PantryItem> {
        return pantryRepository.searchPantryItems(userId, query, limit)
    }
    
    suspend fun getPantryLocations(userId: String): List<String> {
        return pantryRepository.getPantryLocations(userId)
    }
    
    // Maintenance functions
    suspend fun updateLowStockStatus(userId: String): Result<Unit> {
        return pantryRepository.updateLowStockStatus(userId)
    }
    
    suspend fun cleanupExpiredItems(userId: String): Result<Int> {
        return pantryRepository.cleanupExpiredItems(userId)
    }
    
    // Pantry overview data
    fun getPantryOverview(userId: String): Flow<PantryOverviewData> {
        return combine(
            pantryRepository.getPantryItemsForUser(userId),
            pantryRepository.getLowStockItems(userId),
            pantryRepository.getExpiringItems(userId)
        ) { allItems, lowStock, expiring ->
            val categoryCounts = allItems.groupBy { it.category }.mapValues { it.value.size }
            val locationCounts = allItems.groupBy { it.location ?: "Unknown" }.mapValues { it.value.size }
            
            PantryOverviewData(
                totalItems = allItems.size,
                lowStockCount = lowStock.size,
                expiringCount = expiring.size,
                categoryCounts = categoryCounts,
                locationCounts = locationCounts,
                recentlyAdded = allItems.sortedByDescending { it.createdAt }.take(5),
                needsAttention = (lowStock + expiring).distinctBy { it.id }.take(10)
            )
        }
    }
}

data class PantryAlertsData(
    val lowStockItems: List<PantryItem>,
    val expiringItems: List<PantryItem>,
    val expiredItems: List<PantryItem>,
    val totalAlerts: Int
)

data class PantryOverviewData(
    val totalItems: Int,
    val lowStockCount: Int,
    val expiringCount: Int,
    val categoryCounts: Map<IngredientCategory, Int>,
    val locationCounts: Map<String, Int>,
    val recentlyAdded: List<PantryItem>,
    val needsAttention: List<PantryItem>
)