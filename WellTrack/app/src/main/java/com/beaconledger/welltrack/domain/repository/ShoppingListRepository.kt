package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    
    // Shopping List operations
    fun getShoppingListsByUser(userId: String): Flow<List<ShoppingList>>
    fun getActiveShoppingListsByUser(userId: String): Flow<List<ShoppingList>>
    suspend fun getShoppingListById(shoppingListId: String): ShoppingList?
    suspend fun getShoppingListByMealPlan(userId: String, mealPlanId: String): ShoppingList?
    suspend fun createShoppingList(request: ShoppingListCreateRequest, userId: String): Result<String>
    suspend fun updateShoppingList(shoppingListId: String, request: ShoppingListUpdateRequest): Result<Unit>
    suspend fun deleteShoppingList(shoppingListId: String): Result<Unit>
    suspend fun setShoppingListActiveStatus(shoppingListId: String, isActive: Boolean): Result<Unit>
    
    // Shopping List Item operations
    suspend fun getShoppingListItems(shoppingListId: String): List<ShoppingListItem>
    fun getShoppingListItemsFlow(shoppingListId: String): Flow<List<ShoppingListItem>>
    suspend fun getShoppingListItemsByCategory(shoppingListId: String, category: IngredientCategory): List<ShoppingListItem>
    suspend fun addShoppingListItem(shoppingListId: String, request: ShoppingListItemCreateRequest): Result<String>
    suspend fun updateShoppingListItem(itemId: String, request: ShoppingListItemUpdateRequest): Result<Unit>
    suspend fun deleteShoppingListItem(itemId: String): Result<Unit>
    suspend fun markItemAsPurchased(itemId: String, isPurchased: Boolean): Result<Unit>
    suspend fun markAllItemsAsPurchased(shoppingListId: String, isPurchased: Boolean): Result<Unit>
    
    // Shopping List with Items operations
    suspend fun getShoppingListWithItems(shoppingListId: String): ShoppingListWithItems?
    fun getShoppingListsWithItems(userId: String): Flow<List<ShoppingListWithItems>>
    
    // Shopping List Generation operations
    suspend fun generateShoppingListFromMealPlan(request: ShoppingListGenerationRequest): Result<ShoppingListGenerationResult>
    suspend fun consolidateIngredients(ingredients: List<RecipeIngredient>): List<ConsolidatedIngredient>
    
    // Analytics and utility operations
    suspend fun getShoppingListAnalytics(shoppingListId: String): ShoppingListAnalytics
    suspend fun searchIngredientNames(searchQuery: String): List<String>
    suspend fun duplicateShoppingList(shoppingListId: String, newName: String): Result<String>
}

data class ShoppingListAnalytics(
    val totalItems: Int,
    val purchasedItems: Int,
    val remainingItems: Int,
    val completionPercentage: Float,
    val totalEstimatedCost: Double?,
    val totalPurchasedCost: Double?,
    val itemsByCategory: Map<IngredientCategory, Int>
)