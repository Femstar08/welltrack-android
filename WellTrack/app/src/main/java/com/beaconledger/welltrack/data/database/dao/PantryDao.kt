package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.PantryItem
import com.beaconledger.welltrack.data.model.IngredientCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryDao {
    
    @Query("SELECT * FROM pantry_items WHERE userId = :userId ORDER BY ingredientName ASC")
    fun getPantryItemsForUser(userId: String): Flow<List<PantryItem>>
    
    @Query("SELECT * FROM pantry_items WHERE userId = :userId AND category = :category ORDER BY ingredientName ASC")
    fun getPantryItemsByCategory(userId: String, category: IngredientCategory): Flow<List<PantryItem>>
    
    @Query("SELECT * FROM pantry_items WHERE userId = :userId AND ingredientName = :ingredientName LIMIT 1")
    suspend fun getPantryItem(userId: String, ingredientName: String): PantryItem?
    
    @Query("SELECT * FROM pantry_items WHERE userId = :userId AND isLowStock = 1 ORDER BY ingredientName ASC")
    fun getLowStockItems(userId: String): Flow<List<PantryItem>>
    
    @Query("SELECT * FROM pantry_items WHERE userId = :userId AND expiryDate IS NOT NULL AND date(expiryDate) <= date('now', '+7 days') ORDER BY expiryDate ASC")
    fun getExpiringItems(userId: String): Flow<List<PantryItem>>
    
    @Query("SELECT * FROM pantry_items WHERE userId = :userId AND expiryDate IS NOT NULL AND date(expiryDate) < date('now') ORDER BY expiryDate ASC")
    fun getExpiredItems(userId: String): Flow<List<PantryItem>>
    
    @Query("SELECT * FROM pantry_items WHERE userId = :userId AND ingredientName LIKE :query || '%' ORDER BY ingredientName ASC LIMIT :limit")
    suspend fun searchPantryItems(userId: String, query: String, limit: Int = 10): List<PantryItem>
    
    @Query("SELECT DISTINCT location FROM pantry_items WHERE userId = :userId AND location IS NOT NULL ORDER BY location ASC")
    suspend fun getPantryLocations(userId: String): List<String>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItem(item: PantryItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPantryItems(items: List<PantryItem>)
    
    @Update
    suspend fun updatePantryItem(item: PantryItem)
    
    @Delete
    suspend fun deletePantryItem(item: PantryItem)
    
    @Query("DELETE FROM pantry_items WHERE userId = :userId AND ingredientName = :ingredientName")
    suspend fun deletePantryItemByName(userId: String, ingredientName: String)
    
    @Query("DELETE FROM pantry_items WHERE userId = :userId")
    suspend fun deleteAllPantryItemsForUser(userId: String)
    
    @Query("UPDATE pantry_items SET quantity = quantity - :usedQuantity WHERE userId = :userId AND ingredientName = :ingredientName")
    suspend fun reduceQuantity(userId: String, ingredientName: String, usedQuantity: Double)
    
    @Query("UPDATE pantry_items SET quantity = quantity + :addedQuantity WHERE userId = :userId AND ingredientName = :ingredientName")
    suspend fun addQuantity(userId: String, ingredientName: String, addedQuantity: Double)
    
    @Query("UPDATE pantry_items SET isLowStock = CASE WHEN minimumQuantity IS NOT NULL AND quantity <= minimumQuantity THEN 1 ELSE 0 END WHERE userId = :userId")
    suspend fun updateLowStockStatus(userId: String)
    
    @Query("SELECT COUNT(*) FROM pantry_items WHERE userId = :userId")
    suspend fun getPantryItemCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM pantry_items WHERE userId = :userId AND isLowStock = 1")
    suspend fun getLowStockCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM pantry_items WHERE userId = :userId AND expiryDate IS NOT NULL AND date(expiryDate) <= date('now', '+7 days')")
    suspend fun getExpiringItemsCount(userId: String): Int
}