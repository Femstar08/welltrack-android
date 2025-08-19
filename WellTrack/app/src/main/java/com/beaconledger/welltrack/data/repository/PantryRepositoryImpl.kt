package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.barcode.BarcodeService
import com.beaconledger.welltrack.data.database.dao.PantryDao
import com.beaconledger.welltrack.data.database.dao.IngredientUsageDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.PantryRepository
import com.beaconledger.welltrack.domain.repository.ProductInfo
import com.beaconledger.welltrack.domain.repository.ExpiringItemWithSuggestions
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PantryRepositoryImpl @Inject constructor(
    private val pantryDao: PantryDao,
    private val ingredientUsageDao: IngredientUsageDao,
    private val recipeDao: RecipeDao,
    private val barcodeService: BarcodeService
) : PantryRepository {
    
    override fun getPantryItemsForUser(userId: String): Flow<List<PantryItem>> {
        return pantryDao.getPantryItemsForUser(userId)
    }
    
    override fun getPantryItemsByCategory(userId: String, category: IngredientCategory): Flow<List<PantryItem>> {
        return pantryDao.getPantryItemsByCategory(userId, category)
    }
    
    override suspend fun getPantryItem(userId: String, ingredientName: String): PantryItem? {
        return pantryDao.getPantryItem(userId, ingredientName)
    }
    
    override suspend fun addPantryItem(userId: String, request: PantryItemRequest): Result<PantryItem> {
        return try {
            val pantryItem = PantryItem(
                id = UUID.randomUUID().toString(),
                userId = userId,
                ingredientName = request.ingredientName,
                quantity = request.quantity,
                unit = request.unit,
                category = request.category,
                purchaseDate = request.purchaseDate,
                expiryDate = request.expiryDate,
                barcode = request.barcode,
                location = request.location,
                isLowStock = request.minimumQuantity?.let { request.quantity <= it } ?: false,
                minimumQuantity = request.minimumQuantity
            )
            
            pantryDao.insertPantryItem(pantryItem)
            Result.success(pantryItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updatePantryItem(userId: String, ingredientName: String, request: PantryUpdateRequest): Result<PantryItem> {
        return try {
            val existingItem = pantryDao.getPantryItem(userId, ingredientName)
                ?: return Result.failure(Exception("Pantry item not found"))
            
            val updatedItem = existingItem.copy(
                quantity = request.quantity ?: existingItem.quantity,
                unit = request.unit ?: existingItem.unit,
                expiryDate = request.expiryDate ?: existingItem.expiryDate,
                location = request.location ?: existingItem.location,
                isLowStock = request.isLowStock ?: existingItem.isLowStock,
                minimumQuantity = request.minimumQuantity ?: existingItem.minimumQuantity,
                updatedAt = LocalDateTime.now().toString()
            )
            
            pantryDao.updatePantryItem(updatedItem)
            Result.success(updatedItem)
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
    
    override suspend fun updateQuantity(userId: String, ingredientName: String, newQuantity: Double): Result<Unit> {
        return try {
            val existingItem = pantryDao.getPantryItem(userId, ingredientName)
                ?: return Result.failure(Exception("Pantry item not found"))
            
            val updatedItem = existingItem.copy(
                quantity = newQuantity,
                isLowStock = existingItem.minimumQuantity?.let { newQuantity <= it } ?: false,
                updatedAt = LocalDateTime.now().toString()
            )
            
            pantryDao.updatePantryItem(updatedItem)
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
    
    override suspend fun addPantryItemByBarcode(userId: String, barcode: String): Result<PantryItem> {
        return try {
            val productInfo = barcodeService.getProductInfo(barcode)
                ?: return Result.failure(Exception("Product not found for barcode: $barcode"))
            
            val request = PantryItemRequest(
                ingredientName = productInfo.name,
                quantity = 1.0,
                unit = productInfo.defaultUnit,
                category = productInfo.category,
                barcode = barcode
            )
            
            addPantryItem(userId, request)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getProductInfoByBarcode(barcode: String): Result<ProductInfo> {
        return try {
            val productInfo = barcodeService.getProductInfo(barcode)
                ?: return Result.failure(Exception("Product not found for barcode: $barcode"))
            Result.success(productInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    
    override suspend fun getPantryAlerts(userId: String): List<PantryAlert> {
        val alerts = mutableListOf<PantryAlert>()
        val today = LocalDate.now()
        
        // Get expired items
        val expiredItems = pantryDao.getExpiredItems(userId).let { flow ->
            // Convert flow to list for this operation
            val items = mutableListOf<PantryItem>()
            flow.collect { items.addAll(it) }
            items
        }
        
        expiredItems.forEach { item ->
            alerts.add(
                PantryAlert(
                    id = UUID.randomUUID().toString(),
                    ingredientName = item.ingredientName,
                    alertType = AlertType.EXPIRED,
                    message = "${item.ingredientName} has expired",
                    severity = AlertSeverity.CRITICAL,
                    expiryDate = item.expiryDate,
                    daysUntilExpiry = item.expiryDate?.let { 
                        ChronoUnit.DAYS.between(LocalDate.parse(it), today).toInt() 
                    },
                    currentQuantity = item.quantity,
                    minimumQuantity = item.minimumQuantity
                )
            )
        }
        
        // Get expiring items
        val expiringItems = pantryDao.getExpiringItems(userId).let { flow ->
            val items = mutableListOf<PantryItem>()
            flow.collect { items.addAll(it) }
            items
        }
        
        expiringItems.forEach { item ->
            val daysUntilExpiry = item.expiryDate?.let { 
                ChronoUnit.DAYS.between(today, LocalDate.parse(it)).toInt() 
            } ?: 0
            
            alerts.add(
                PantryAlert(
                    id = UUID.randomUUID().toString(),
                    ingredientName = item.ingredientName,
                    alertType = AlertType.EXPIRY_WARNING,
                    message = "${item.ingredientName} expires in $daysUntilExpiry days",
                    severity = when {
                        daysUntilExpiry <= 1 -> AlertSeverity.HIGH
                        daysUntilExpiry <= 3 -> AlertSeverity.MEDIUM
                        else -> AlertSeverity.LOW
                    },
                    expiryDate = item.expiryDate,
                    daysUntilExpiry = daysUntilExpiry,
                    currentQuantity = item.quantity,
                    minimumQuantity = item.minimumQuantity
                )
            )
        }
        
        // Get low stock items
        val lowStockItems = pantryDao.getLowStockItems(userId).let { flow ->
            val items = mutableListOf<PantryItem>()
            flow.collect { items.addAll(it) }
            items
        }
        
        lowStockItems.forEach { item ->
            alerts.add(
                PantryAlert(
                    id = UUID.randomUUID().toString(),
                    ingredientName = item.ingredientName,
                    alertType = if (item.quantity <= 0) AlertType.OUT_OF_STOCK else AlertType.LOW_STOCK,
                    message = if (item.quantity <= 0) 
                        "${item.ingredientName} is out of stock" 
                    else 
                        "${item.ingredientName} is running low (${item.quantity} ${item.unit} remaining)",
                    severity = if (item.quantity <= 0) AlertSeverity.HIGH else AlertSeverity.MEDIUM,
                    expiryDate = item.expiryDate,
                    daysUntilExpiry = null,
                    currentQuantity = item.quantity,
                    minimumQuantity = item.minimumQuantity
                )
            )
        }
        
        return alerts.sortedByDescending { it.severity.ordinal }
    }
    
    override suspend fun getExpiringItemsWithRecipeSuggestions(userId: String): List<ExpiringItemWithSuggestions> {
        val expiringItems = pantryDao.getExpiringItems(userId).let { flow ->
            val items = mutableListOf<PantryItem>()
            flow.collect { items.addAll(it) }
            items
        }
        
        return expiringItems.map { item ->
            val daysUntilExpiry = item.expiryDate?.let { 
                ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(it)).toInt() 
            } ?: 0
            
            // Find recipes that use this ingredient
            val suggestedRecipes = recipeDao.getRecipesByIngredient(item.ingredientName, 5)
            
            ExpiringItemWithSuggestions(
                pantryItem = item,
                daysUntilExpiry = daysUntilExpiry,
                suggestedRecipes = suggestedRecipes
            )
        }
    }
    
    override suspend fun syncFromShoppingList(userId: String, shoppingListItems: List<ShoppingListItem>): Result<Unit> {
        return try {
            shoppingListItems.filter { it.isPurchased }.forEach { item ->
                val existingItem = pantryDao.getPantryItem(userId, item.name)
                
                if (existingItem != null) {
                    // Add to existing quantity
                    pantryDao.addQuantity(userId, item.name, item.quantity)
                } else {
                    // Create new pantry item
                    val pantryItem = PantryItem(
                        id = UUID.randomUUID().toString(),
                        userId = userId,
                        ingredientName = item.name,
                        quantity = item.quantity,
                        unit = item.unit,
                        category = item.category,
                        purchaseDate = LocalDate.now().toString()
                    )
                    pantryDao.insertPantryItem(pantryItem)
                }
            }
            
            pantryDao.updateLowStockStatus(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun markItemAsPurchased(userId: String, ingredientName: String, quantity: Double, unit: String): Result<Unit> {
        return try {
            val existingItem = pantryDao.getPantryItem(userId, ingredientName)
            
            if (existingItem != null) {
                pantryDao.addQuantity(userId, ingredientName, quantity)
            } else {
                val pantryItem = PantryItem(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    ingredientName = ingredientName,
                    quantity = quantity,
                    unit = unit,
                    category = IngredientCategory.OTHER, // Default category
                    purchaseDate = LocalDate.now().toString()
                )
                pantryDao.insertPantryItem(pantryItem)
            }
            
            pantryDao.updateLowStockStatus(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getIngredientUsageStats(userId: String, ingredientName: String): IngredientUsageStats? {
        return ingredientUsageDao.getIngredientUsageStats(userId, ingredientName)
    }
    
    override suspend fun recordIngredientUsage(userId: String, usage: IngredientUsageHistory): Result<Unit> {
        return try {
            ingredientUsageDao.insertUsageHistory(usage)
            
            // Reduce pantry quantity if item exists
            val pantryItem = pantryDao.getPantryItem(userId, usage.ingredientName)
            if (pantryItem != null) {
                pantryDao.reduceQuantity(userId, usage.ingredientName, usage.quantityUsed)
                pantryDao.updateLowStockStatus(userId)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPantryLocations(userId: String): List<String> {
        return pantryDao.getPantryLocations(userId)
    }
    
    override suspend fun searchPantryItems(userId: String, query: String, limit: Int): List<PantryItem> {
        return pantryDao.searchPantryItems(userId, query, limit)
    }
    
    override suspend fun updateLowStockStatus(userId: String): Result<Unit> {
        return try {
            pantryDao.updateLowStockStatus(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun cleanupExpiredItems(userId: String): Result<Int> {
        return try {
            val expiredItems = pantryDao.getExpiredItems(userId).let { flow ->
                val items = mutableListOf<PantryItem>()
                flow.collect { items.addAll(it) }
                items
            }
            
            val count = expiredItems.size
            expiredItems.forEach { item ->
                pantryDao.deletePantryItem(item)
            }
            
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}