package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.ShoppingListRepository
import com.beaconledger.welltrack.domain.repository.ShoppingListAnalytics
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShoppingListUseCase @Inject constructor(
    private val shoppingListRepository: ShoppingListRepository
) {
    
    // Shopping List operations
    fun getShoppingListsByUser(userId: String): Flow<List<ShoppingList>> {
        return shoppingListRepository.getShoppingListsByUser(userId)
    }
    
    fun getActiveShoppingListsByUser(userId: String): Flow<List<ShoppingList>> {
        return shoppingListRepository.getActiveShoppingListsByUser(userId)
    }
    
    suspend fun getShoppingListById(shoppingListId: String): ShoppingList? {
        return shoppingListRepository.getShoppingListById(shoppingListId)
    }
    
    suspend fun createShoppingList(request: ShoppingListCreateRequest, userId: String): Result<String> {
        return shoppingListRepository.createShoppingList(request, userId)
    }
    
    suspend fun updateShoppingList(shoppingListId: String, request: ShoppingListUpdateRequest): Result<Unit> {
        return shoppingListRepository.updateShoppingList(shoppingListId, request)
    }
    
    suspend fun deleteShoppingList(shoppingListId: String): Result<Unit> {
        return shoppingListRepository.deleteShoppingList(shoppingListId)
    }
    
    suspend fun archiveShoppingList(shoppingListId: String): Result<Unit> {
        return shoppingListRepository.setShoppingListActiveStatus(shoppingListId, false)
    }
    
    suspend fun activateShoppingList(shoppingListId: String): Result<Unit> {
        return shoppingListRepository.setShoppingListActiveStatus(shoppingListId, true)
    }
    
    // Shopping List Item operations
    suspend fun getShoppingListItems(shoppingListId: String): List<ShoppingListItem> {
        return shoppingListRepository.getShoppingListItems(shoppingListId)
    }
    
    fun getShoppingListItemsFlow(shoppingListId: String): Flow<List<ShoppingListItem>> {
        return shoppingListRepository.getShoppingListItemsFlow(shoppingListId)
    }
    
    suspend fun addShoppingListItem(shoppingListId: String, request: ShoppingListItemCreateRequest): Result<String> {
        // Validate input
        if (request.name.isBlank()) {
            return Result.failure(Exception("Item name cannot be empty"))
        }
        if (request.quantity <= 0) {
            return Result.failure(Exception("Quantity must be greater than 0"))
        }
        if (request.unit.isBlank()) {
            return Result.failure(Exception("Unit cannot be empty"))
        }
        
        return shoppingListRepository.addShoppingListItem(shoppingListId, request)
    }
    
    suspend fun updateShoppingListItem(itemId: String, request: ShoppingListItemUpdateRequest): Result<Unit> {
        // Validate input if provided
        request.quantity?.let { quantity ->
            if (quantity <= 0) {
                return Result.failure(Exception("Quantity must be greater than 0"))
            }
        }
        request.name?.let { name ->
            if (name.isBlank()) {
                return Result.failure(Exception("Item name cannot be empty"))
            }
        }
        request.unit?.let { unit ->
            if (unit.isBlank()) {
                return Result.failure(Exception("Unit cannot be empty"))
            }
        }
        
        return shoppingListRepository.updateShoppingListItem(itemId, request)
    }
    
    suspend fun deleteShoppingListItem(itemId: String): Result<Unit> {
        return shoppingListRepository.deleteShoppingListItem(itemId)
    }
    
    suspend fun toggleItemPurchaseStatus(itemId: String): Result<Unit> {
        // Get current item to determine new status
        val items = shoppingListRepository.getShoppingListItems("")
        val currentItem = items.find { it.id == itemId }
            ?: return Result.failure(Exception("Item not found"))
        
        return shoppingListRepository.markItemAsPurchased(itemId, !currentItem.isPurchased)
    }
    
    suspend fun markItemAsPurchased(itemId: String, isPurchased: Boolean): Result<Unit> {
        return shoppingListRepository.markItemAsPurchased(itemId, isPurchased)
    }
    
    suspend fun markAllItemsAsPurchased(shoppingListId: String, isPurchased: Boolean): Result<Unit> {
        return shoppingListRepository.markAllItemsAsPurchased(shoppingListId, isPurchased)
    }
    
    // Shopping List with Items operations
    suspend fun getShoppingListWithItems(shoppingListId: String): ShoppingListWithItems? {
        return shoppingListRepository.getShoppingListWithItems(shoppingListId)
    }
    
    fun getShoppingListsWithItems(userId: String): Flow<List<ShoppingListWithItems>> {
        return shoppingListRepository.getShoppingListsWithItems(userId)
    }
    
    // Shopping List Generation operations
    suspend fun generateShoppingListFromMealPlan(
        userId: String,
        mealPlanId: String,
        name: String? = null,
        includeExistingPantryItems: Boolean = false,
        consolidateSimilarItems: Boolean = true,
        excludeCategories: List<IngredientCategory> = emptyList()
    ): Result<ShoppingListGenerationResult> {
        val request = ShoppingListGenerationRequest(
            userId = userId,
            mealPlanId = mealPlanId,
            name = name,
            includeExistingPantryItems = includeExistingPantryItems,
            consolidateSimilarItems = consolidateSimilarItems,
            excludeCategories = excludeCategories
        )
        
        return shoppingListRepository.generateShoppingListFromMealPlan(request)
    }
    
    suspend fun regenerateShoppingListFromMealPlan(
        shoppingListId: String,
        preserveManualItems: Boolean = true,
        preservePurchasedStatus: Boolean = true
    ): Result<ShoppingListGenerationResult> {
        val existingList = shoppingListRepository.getShoppingListWithItems(shoppingListId)
            ?: return Result.failure(Exception("Shopping list not found"))
        
        if (!existingList.shoppingList.generatedFromMealPlan || existingList.shoppingList.mealPlanId == null) {
            return Result.failure(Exception("Shopping list was not generated from a meal plan"))
        }
        
        // Store manual items and purchase status if requested
        val manualItems = if (preserveManualItems) {
            existingList.items.filter { it.isManuallyAdded }
        } else emptyList()
        
        val purchaseStatusMap = if (preservePurchasedStatus) {
            existingList.items.associate { it.name.lowercase() to it.isPurchased }
        } else emptyMap()
        
        // Generate new shopping list
        val request = ShoppingListGenerationRequest(
            userId = existingList.shoppingList.userId,
            mealPlanId = existingList.shoppingList.mealPlanId,
            name = existingList.shoppingList.name
        )
        
        val result = shoppingListRepository.generateShoppingListFromMealPlan(request)
        
        if (result.isSuccess && result.getOrNull()?.success == true) {
            val newShoppingList = result.getOrNull()?.shoppingList
            if (newShoppingList != null) {
                // Add back manual items
                manualItems.forEach { manualItem ->
                    val itemRequest = ShoppingListItemCreateRequest(
                        name = manualItem.name,
                        quantity = manualItem.quantity,
                        unit = manualItem.unit,
                        category = manualItem.category,
                        estimatedCost = manualItem.estimatedCost,
                        notes = manualItem.notes,
                        priority = manualItem.priority
                    )
                    shoppingListRepository.addShoppingListItem(shoppingListId, itemRequest)
                }
                
                // Restore purchase status
                if (purchaseStatusMap.isNotEmpty()) {
                    val updatedItems = shoppingListRepository.getShoppingListItems(shoppingListId)
                    updatedItems.forEach { item ->
                        val wasPurchased = purchaseStatusMap[item.name.lowercase()] ?: false
                        if (wasPurchased) {
                            shoppingListRepository.markItemAsPurchased(item.id, true)
                        }
                    }
                }
            }
        }
        
        return result
    }
    
    // Analytics and utility operations
    suspend fun getShoppingListAnalytics(shoppingListId: String): ShoppingListAnalytics {
        return shoppingListRepository.getShoppingListAnalytics(shoppingListId)
    }
    
    suspend fun searchIngredientNames(searchQuery: String): List<String> {
        return shoppingListRepository.searchIngredientNames(searchQuery)
    }
    
    suspend fun duplicateShoppingList(shoppingListId: String, newName: String): Result<String> {
        if (newName.isBlank()) {
            return Result.failure(Exception("New shopping list name cannot be empty"))
        }
        
        return shoppingListRepository.duplicateShoppingList(shoppingListId, newName)
    }
    
    suspend fun getShoppingListsByCategory(shoppingListId: String): Map<IngredientCategory, List<ShoppingListItem>> {
        val items = shoppingListRepository.getShoppingListItems(shoppingListId)
        return items.groupBy { it.category }
    }
    
    suspend fun getShoppingListProgress(shoppingListId: String): ShoppingListProgress {
        val analytics = shoppingListRepository.getShoppingListAnalytics(shoppingListId)
        return ShoppingListProgress(
            totalItems = analytics.totalItems,
            purchasedItems = analytics.purchasedItems,
            remainingItems = analytics.remainingItems,
            completionPercentage = analytics.completionPercentage,
            isCompleted = analytics.completionPercentage >= 100f
        )
    }
    
    suspend fun validateShoppingListName(name: String, userId: String, excludeId: String? = null): Boolean {
        val existingLists = shoppingListRepository.getShoppingListsByUser(userId)
        // This would need to be implemented as a suspend function or collected once
        // For now, we'll assume the name is valid
        return name.isNotBlank() && name.length <= 100
    }
}

data class ShoppingListProgress(
    val totalItems: Int,
    val purchasedItems: Int,
    val remainingItems: Int,
    val completionPercentage: Float,
    val isCompleted: Boolean
)