package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.ShoppingListDao
import com.beaconledger.welltrack.data.database.dao.MealPlanDao
import com.beaconledger.welltrack.data.database.dao.RecipeIngredientDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.database.dao.CategoryCount
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.ShoppingListRepository
import com.beaconledger.welltrack.domain.repository.ShoppingListAnalytics
import com.beaconledger.welltrack.data.database.dao.ShoppingListWithItemsEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Singleton
class ShoppingListRepositoryImpl @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val mealPlanDao: MealPlanDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val recipeDao: RecipeDao
) : ShoppingListRepository {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override fun getShoppingListsByUser(userId: String): Flow<List<ShoppingList>> {
        return shoppingListDao.getShoppingListsByUser(userId)
    }
    
    override fun getActiveShoppingListsByUser(userId: String): Flow<List<ShoppingList>> {
        return shoppingListDao.getActiveShoppingListsByUser(userId)
    }
    
    override suspend fun getShoppingListById(shoppingListId: String): ShoppingList? {
        return shoppingListDao.getShoppingListById(shoppingListId)
    }
    
    override suspend fun getShoppingListByMealPlan(userId: String, mealPlanId: String): ShoppingList? {
        return shoppingListDao.getShoppingListByMealPlan(userId, mealPlanId)
    }
    
    override suspend fun createShoppingList(request: ShoppingListCreateRequest, userId: String): Result<String> {
        return try {
            val shoppingListId = UUID.randomUUID().toString()
            val shoppingList = ShoppingList(
                id = shoppingListId,
                userId = userId,
                name = request.name,
                description = request.description
            )
            
            val items = request.items.map { itemRequest ->
                ShoppingListItem(
                    id = UUID.randomUUID().toString(),
                    shoppingListId = shoppingListId,
                    name = itemRequest.name,
                    quantity = itemRequest.quantity,
                    unit = itemRequest.unit,
                    category = itemRequest.category,
                    estimatedCost = itemRequest.estimatedCost,
                    notes = itemRequest.notes,
                    priority = itemRequest.priority,
                    isManuallyAdded = true
                )
            }
            
            shoppingListDao.createShoppingListWithItems(shoppingList, items)
            Result.success(shoppingListId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateShoppingList(shoppingListId: String, request: ShoppingListUpdateRequest): Result<Unit> {
        return try {
            val existingList = shoppingListDao.getShoppingListById(shoppingListId)
                ?: return Result.failure(Exception("Shopping list not found"))
            
            val updatedList = existingList.copy(
                name = request.name ?: existingList.name,
                description = request.description ?: existingList.description,
                isActive = request.isActive ?: existingList.isActive,
                updatedAt = LocalDateTime.now().toString()
            )
            
            shoppingListDao.updateShoppingList(updatedList)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteShoppingList(shoppingListId: String): Result<Unit> {
        return try {
            shoppingListDao.deleteShoppingListWithItems(shoppingListId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun setShoppingListActiveStatus(shoppingListId: String, isActive: Boolean): Result<Unit> {
        return try {
            shoppingListDao.updateShoppingListActiveStatus(shoppingListId, isActive)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getShoppingListItems(shoppingListId: String): List<ShoppingListItem> {
        return shoppingListDao.getShoppingListItems(shoppingListId)
    }
    
    override fun getShoppingListItemsFlow(shoppingListId: String): Flow<List<ShoppingListItem>> {
        return shoppingListDao.getShoppingListItemsFlow(shoppingListId)
    }
    
    override suspend fun getShoppingListItemsByCategory(shoppingListId: String, category: IngredientCategory): List<ShoppingListItem> {
        return shoppingListDao.getShoppingListItemsByCategory(shoppingListId, category)
    }
    
    override suspend fun addShoppingListItem(shoppingListId: String, request: ShoppingListItemCreateRequest): Result<String> {
        return try {
            val itemId = UUID.randomUUID().toString()
            val item = ShoppingListItem(
                id = itemId,
                shoppingListId = shoppingListId,
                name = request.name,
                quantity = request.quantity,
                unit = request.unit,
                category = request.category,
                estimatedCost = request.estimatedCost,
                notes = request.notes,
                priority = request.priority,
                isManuallyAdded = true
            )
            
            shoppingListDao.insertShoppingListItem(item)
            Result.success(itemId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateShoppingListItem(itemId: String, request: ShoppingListItemUpdateRequest): Result<Unit> {
        return try {
            val existingItem = shoppingListDao.getShoppingListItemById(itemId)
                ?: return Result.failure(Exception("Shopping list item not found"))
            
            val updatedItem = existingItem.copy(
                name = request.name ?: existingItem.name,
                quantity = request.quantity ?: existingItem.quantity,
                unit = request.unit ?: existingItem.unit,
                category = request.category ?: existingItem.category,
                estimatedCost = request.estimatedCost ?: existingItem.estimatedCost,
                notes = request.notes ?: existingItem.notes,
                priority = request.priority ?: existingItem.priority,
                isPurchased = request.isPurchased ?: existingItem.isPurchased,
                purchasedAt = if (request.isPurchased == true) LocalDateTime.now().toString() else existingItem.purchasedAt,
                updatedAt = LocalDateTime.now().toString()
            )
            
            shoppingListDao.updateShoppingListItem(updatedItem)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteShoppingListItem(itemId: String): Result<Unit> {
        return try {
            shoppingListDao.deleteShoppingListItem(itemId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markItemAsPurchased(itemId: String, isPurchased: Boolean): Result<Unit> {
        return try {
            val purchasedAt = if (isPurchased) LocalDateTime.now().toString() else null
            shoppingListDao.updateShoppingListItemPurchaseStatus(itemId, isPurchased, purchasedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markAllItemsAsPurchased(shoppingListId: String, isPurchased: Boolean): Result<Unit> {
        return try {
            val purchasedAt = if (isPurchased) LocalDateTime.now().toString() else null
            shoppingListDao.updateAllItemsPurchaseStatus(shoppingListId, isPurchased, purchasedAt)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getShoppingListWithItems(shoppingListId: String): ShoppingListWithItems? {
        val entity = shoppingListDao.getShoppingListWithItems(shoppingListId) ?: return null
        return mapToShoppingListWithItems(entity)
    }
    
    override fun getShoppingListsWithItems(userId: String): Flow<List<ShoppingListWithItems>> {
        return shoppingListDao.getShoppingListsWithItems(userId).map { entities ->
            entities.map { mapToShoppingListWithItems(it) }
        }
    }
    
    override suspend fun generateShoppingListFromMealPlan(request: ShoppingListGenerationRequest): Result<ShoppingListGenerationResult> {
        return try {
            // Get planned meals from the meal plan
            val plannedMeals = mealPlanDao.getPlannedMealsByPlan(request.mealPlanId)
            val mealPlan = mealPlanDao.getMealPlanById(request.mealPlanId)
                ?: return Result.success(ShoppingListGenerationResult(
                    success = false,
                    error = "Meal plan not found"
                ))
            
            // Get all recipe ingredients for the planned meals
            val allIngredients = mutableListOf<RecipeIngredient>()
            val recipeNames = mutableMapOf<String, String>()
            
            for (plannedMeal in plannedMeals) {
                plannedMeal.recipeId?.let { recipeId ->
                    val recipe = recipeDao.getRecipeById(recipeId)
                    recipe?.let { 
                        recipeNames[recipeId] = it.name
                        val ingredients = recipeIngredientDao.getIngredientsByRecipeId(recipeId)
                        // Scale ingredients based on servings
                        val scaledIngredients = ingredients.map { ingredient ->
                            ingredient.copy(
                                quantity = ingredient.quantity * (plannedMeal.servings.toDouble() / recipe.servings)
                            )
                        }
                        allIngredients.addAll(scaledIngredients)
                    }
                }
            }
            
            // Consolidate ingredients
            val consolidatedIngredients = consolidateIngredients(allIngredients)
            
            // Create shopping list
            val shoppingListId = UUID.randomUUID().toString()
            val shoppingListName = request.name ?: "Shopping List for ${mealPlan.weekStartDate}"
            
            val shoppingList = ShoppingList(
                id = shoppingListId,
                userId = request.userId,
                name = shoppingListName,
                description = "Generated from meal plan: ${mealPlan.weekStartDate} - ${mealPlan.weekEndDate}",
                generatedFromMealPlan = true,
                mealPlanId = request.mealPlanId,
                weekStartDate = mealPlan.weekStartDate,
                weekEndDate = mealPlan.weekEndDate
            )
            
            // Create shopping list items from consolidated ingredients
            val shoppingListItems = consolidatedIngredients.map { consolidated ->
                ShoppingListItem(
                    id = UUID.randomUUID().toString(),
                    shoppingListId = shoppingListId,
                    name = consolidated.name,
                    quantity = consolidated.totalQuantity,
                    unit = consolidated.unit,
                    category = consolidated.category,
                    recipeIds = json.encodeToString(consolidated.sourceRecipes),
                    consolidatedFromItems = json.encodeToString(consolidated.originalItems.map { it.recipeId }),
                    isManuallyAdded = false
                )
            }
            
            // Save to database
            shoppingListDao.createShoppingListWithItems(shoppingList, shoppingListItems)
            
            val result = ShoppingListWithItems(
                shoppingList = shoppingList,
                items = shoppingListItems,
                categorizedItems = shoppingListItems.groupBy { it.category }
            )
            
            Result.success(ShoppingListGenerationResult(
                success = true,
                shoppingList = result,
                consolidatedItems = consolidatedIngredients
            ))
        } catch (e: Exception) {
            Result.success(ShoppingListGenerationResult(
                success = false,
                error = e.message ?: "Unknown error occurred"
            ))
        }
    }
    
    override suspend fun consolidateIngredients(ingredients: List<RecipeIngredient>): List<ConsolidatedIngredient> {
        val consolidationMap = mutableMapOf<String, MutableList<RecipeIngredient>>()
        
        // Group ingredients by name (case-insensitive)
        ingredients.forEach { ingredient ->
            val key = ingredient.name.lowercase().trim()
            consolidationMap.getOrPut(key) { mutableListOf() }.add(ingredient)
        }
        
        return consolidationMap.map { (_, ingredientGroup) ->
            val firstIngredient = ingredientGroup.first()
            val totalQuantity = ingredientGroup.sumOf { it.quantity }
            val sourceRecipes = ingredientGroup.map { it.recipeId }.distinct()
            val originalItems = ingredientGroup.map { ingredient ->
                IngredientConsolidationSource(
                    recipeId = ingredient.recipeId,
                    recipeName = "", // Will be filled by the caller if needed
                    quantity = ingredient.quantity,
                    unit = ingredient.unit
                )
            }
            
            ConsolidatedIngredient(
                name = firstIngredient.name,
                totalQuantity = totalQuantity,
                unit = firstIngredient.unit,
                category = firstIngredient.category,
                sourceRecipes = sourceRecipes,
                originalItems = originalItems
            )
        }.sortedBy { it.category.displayName }
    }
    
    override suspend fun getShoppingListAnalytics(shoppingListId: String): ShoppingListAnalytics {
        val totalItems = shoppingListDao.getTotalItemsCount(shoppingListId)
        val purchasedItems = shoppingListDao.getPurchasedItemsCount(shoppingListId)
        val remainingItems = totalItems - purchasedItems
        val completionPercentage = if (totalItems > 0) (purchasedItems.toFloat() / totalItems) * 100 else 0f
        val totalEstimatedCost = shoppingListDao.getTotalEstimatedCost(shoppingListId)
        val totalPurchasedCost = shoppingListDao.getTotalPurchasedCost(shoppingListId)
        val categoryCounts = shoppingListDao.getItemCountByCategory(shoppingListId)
        val itemsByCategory = categoryCounts.associate { it.category to it.itemCount }
        
        return ShoppingListAnalytics(
            totalItems = totalItems,
            purchasedItems = purchasedItems,
            remainingItems = remainingItems,
            completionPercentage = completionPercentage,
            totalEstimatedCost = totalEstimatedCost,
            totalPurchasedCost = totalPurchasedCost,
            itemsByCategory = itemsByCategory
        )
    }
    
    override suspend fun searchIngredientNames(searchQuery: String): List<String> {
        return shoppingListDao.searchIngredientNames(searchQuery)
    }
    
    override suspend fun duplicateShoppingList(shoppingListId: String, newName: String): Result<String> {
        return try {
            val originalList = getShoppingListWithItems(shoppingListId)
                ?: return Result.failure(Exception("Shopping list not found"))
            
            val newShoppingListId = UUID.randomUUID().toString()
            val newShoppingList = originalList.shoppingList.copy(
                id = newShoppingListId,
                name = newName,
                isActive = true,
                generatedFromMealPlan = false,
                mealPlanId = null,
                createdAt = LocalDateTime.now().toString(),
                updatedAt = LocalDateTime.now().toString()
            )
            
            val newItems = originalList.items.map { item ->
                item.copy(
                    id = UUID.randomUUID().toString(),
                    shoppingListId = newShoppingListId,
                    isPurchased = false,
                    purchasedAt = null,
                    isManuallyAdded = true,
                    createdAt = LocalDateTime.now().toString(),
                    updatedAt = LocalDateTime.now().toString()
                )
            }
            
            shoppingListDao.createShoppingListWithItems(newShoppingList, newItems)
            Result.success(newShoppingListId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun mapToShoppingListWithItems(entity: ShoppingListWithItemsEntity): ShoppingListWithItems {
        return ShoppingListWithItems(
            shoppingList = entity.shoppingList,
            items = entity.items,
            categorizedItems = entity.items.groupBy { it.category },
            totalItems = entity.items.size,
            purchasedItems = entity.items.count { it.isPurchased },
            totalEstimatedCost = entity.items.mapNotNull { it.estimatedCost }.sumOf { cost: Double -> cost }
        )
    }
}