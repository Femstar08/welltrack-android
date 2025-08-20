package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface PantryRepository {
    
    // Basic CRUD operations
    fun getPantryItemsForUser(userId: String): Flow<List<PantryItem>>
    fun getPantryItemsByCategory(userId: String, category: IngredientCategory): Flow<List<PantryItem>>
    suspend fun getPantryItem(userId: String, ingredientName: String): PantryItem?
    suspend fun addPantryItem(userId: String, request: PantryItemRequest): Result<PantryItem>
    suspend fun updatePantryItem(userId: String, ingredientName: String, request: PantryUpdateRequest): Result<PantryItem>
    suspend fun deletePantryItem(userId: String, ingredientName: String): Result<Unit>
    
    // Inventory management
    suspend fun updateQuantity(userId: String, ingredientName: String, newQuantity: Double): Result<Unit>
    suspend fun reduceQuantity(userId: String, ingredientName: String, usedQuantity: Double): Result<Unit>
    suspend fun addQuantity(userId: String, ingredientName: String, addedQuantity: Double): Result<Unit>
    
    // Barcode scanning
    suspend fun addPantryItemByBarcode(userId: String, barcode: String): Result<PantryItem>
    suspend fun getProductInfoByBarcode(barcode: String): Result<ProductInfo>
    
    // Expiry and alerts
    fun getLowStockItems(userId: String): Flow<List<PantryItem>>
    fun getExpiringItems(userId: String): Flow<List<PantryItem>>
    fun getExpiringItems(userId: String, daysAhead: Int): Flow<List<PantryItem>>
    fun getExpiredItems(userId: String): Flow<List<PantryItem>>
    suspend fun getPantryAlerts(userId: String): List<PantryAlert>
    suspend fun getExpiringItemsWithRecipeSuggestions(userId: String): List<ExpiringItemWithSuggestions>
    
    // Shopping list integration
    suspend fun syncFromShoppingList(userId: String, shoppingListItems: List<ShoppingListItem>): Result<Unit>
    suspend fun markItemAsPurchased(userId: String, ingredientName: String, quantity: Double, unit: String): Result<Unit>
    
    // Analytics and usage
    suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats?
    suspend fun recordIngredientUsage(userId: String, usage: IngredientUsageHistory): Result<Unit>
    suspend fun getPantryLocations(userId: String): List<String>
    suspend fun searchPantryItems(userId: String, query: String, limit: Int = 10): List<PantryItem>
    
    // Maintenance
    suspend fun updateLowStockStatus(userId: String): Result<Unit>
    suspend fun cleanupExpiredItems(userId: String): Result<Int>
}

data class ProductInfo(
    val name: String,
    val barcode: String,
    val category: IngredientCategory,
    val defaultUnit: String,
    val nutritionInfo: Map<String, Double>? = null,
    val brand: String? = null,
    val imageUrl: String? = null
)

data class ExpiringItemWithSuggestions(
    val pantryItem: PantryItem,
    val daysUntilExpiry: Int,
    val suggestedRecipes: List<Recipe>
)