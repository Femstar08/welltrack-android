package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    
    // Shopping List operations
    @Query("SELECT * FROM shopping_lists WHERE userId = :userId ORDER BY createdAt DESC")
    fun getShoppingListsByUser(userId: String): Flow<List<ShoppingList>>
    
    @Query("SELECT * FROM shopping_lists WHERE userId = :userId AND isActive = 1 ORDER BY createdAt DESC")
    fun getActiveShoppingListsByUser(userId: String): Flow<List<ShoppingList>>
    
    @Query("SELECT * FROM shopping_lists WHERE id = :shoppingListId")
    suspend fun getShoppingListById(shoppingListId: String): ShoppingList?
    
    @Query("SELECT * FROM shopping_lists WHERE userId = :userId AND mealPlanId = :mealPlanId")
    suspend fun getShoppingListByMealPlan(userId: String, mealPlanId: String): ShoppingList?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingList(shoppingList: ShoppingList)
    
    @Update
    suspend fun updateShoppingList(shoppingList: ShoppingList)
    
    @Query("DELETE FROM shopping_lists WHERE id = :shoppingListId")
    suspend fun deleteShoppingList(shoppingListId: String)
    
    @Query("SELECT * FROM shopping_lists WHERE userId = :userId")
    suspend fun getShoppingListsForUser(userId: String): List<ShoppingList>
    
    @Query("UPDATE shopping_lists SET isActive = :isActive WHERE id = :shoppingListId")
    suspend fun updateShoppingListActiveStatus(shoppingListId: String, isActive: Boolean)
    
    // Shopping List Item operations
    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :shoppingListId ORDER BY category ASC, name ASC")
    suspend fun getShoppingListItems(shoppingListId: String): List<ShoppingListItem>
    
    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :shoppingListId ORDER BY category ASC, name ASC")
    fun getShoppingListItemsFlow(shoppingListId: String): Flow<List<ShoppingListItem>>
    
    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :shoppingListId AND category = :category ORDER BY name ASC")
    suspend fun getShoppingListItemsByCategory(shoppingListId: String, category: IngredientCategory): List<ShoppingListItem>
    
    @Query("SELECT * FROM shopping_list_items WHERE shoppingListId = :shoppingListId AND isPurchased = :isPurchased ORDER BY category ASC, name ASC")
    suspend fun getShoppingListItemsByPurchaseStatus(shoppingListId: String, isPurchased: Boolean): List<ShoppingListItem>
    
    @Query("SELECT * FROM shopping_list_items WHERE id = :itemId")
    suspend fun getShoppingListItemById(itemId: String): ShoppingListItem?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingListItem(item: ShoppingListItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingListItems(items: List<ShoppingListItem>)
    
    @Update
    suspend fun updateShoppingListItem(item: ShoppingListItem)
    
    @Query("DELETE FROM shopping_list_items WHERE id = :itemId")
    suspend fun deleteShoppingListItem(itemId: String)
    
    @Query("DELETE FROM shopping_list_items WHERE shoppingListId = :shoppingListId")
    suspend fun deleteShoppingListItemsByListId(shoppingListId: String)
    
    @Query("UPDATE shopping_list_items SET isPurchased = :isPurchased, purchasedAt = :purchasedAt WHERE id = :itemId")
    suspend fun updateShoppingListItemPurchaseStatus(itemId: String, isPurchased: Boolean, purchasedAt: String?)
    
    @Query("UPDATE shopping_list_items SET isPurchased = :isPurchased, purchasedAt = :purchasedAt WHERE shoppingListId = :shoppingListId")
    suspend fun updateAllItemsPurchaseStatus(shoppingListId: String, isPurchased: Boolean, purchasedAt: String?)
    
    // Complex queries for shopping list analytics
    @Query("""
        SELECT COUNT(*) FROM shopping_list_items 
        WHERE shoppingListId = :shoppingListId AND isPurchased = 1
    """)
    suspend fun getPurchasedItemsCount(shoppingListId: String): Int
    
    @Query("""
        SELECT COUNT(*) FROM shopping_list_items 
        WHERE shoppingListId = :shoppingListId
    """)
    suspend fun getTotalItemsCount(shoppingListId: String): Int
    
    @Query("""
        SELECT SUM(estimatedCost) FROM shopping_list_items 
        WHERE shoppingListId = :shoppingListId AND estimatedCost IS NOT NULL
    """)
    suspend fun getTotalEstimatedCost(shoppingListId: String): Double?
    
    @Query("""
        SELECT SUM(estimatedCost) FROM shopping_list_items 
        WHERE shoppingListId = :shoppingListId AND isPurchased = 1 AND estimatedCost IS NOT NULL
    """)
    suspend fun getTotalPurchasedCost(shoppingListId: String): Double?
    
    @Query("""
        SELECT category, COUNT(*) as itemCount FROM shopping_list_items 
        WHERE shoppingListId = :shoppingListId 
        GROUP BY category 
        ORDER BY itemCount DESC
    """)
    suspend fun getItemCountByCategory(shoppingListId: String): List<CategoryCount>
    
    @Query("""
        SELECT DISTINCT name FROM shopping_list_items 
        WHERE name LIKE '%' || :searchQuery || '%' 
        ORDER BY name ASC 
        LIMIT 10
    """)
    suspend fun searchIngredientNames(searchQuery: String): List<String>
    
    // Transaction methods for atomic operations
    @Transaction
    suspend fun createShoppingListWithItems(
        shoppingList: ShoppingList,
        items: List<ShoppingListItem>
    ) {
        insertShoppingList(shoppingList)
        insertShoppingListItems(items)
    }
    
    @Transaction
    suspend fun replaceShoppingListItems(
        shoppingListId: String,
        items: List<ShoppingListItem>
    ) {
        deleteShoppingListItemsByListId(shoppingListId)
        insertShoppingListItems(items)
    }
    
    @Transaction
    suspend fun deleteShoppingListWithItems(shoppingListId: String) {
        deleteShoppingListItemsByListId(shoppingListId)
        deleteShoppingList(shoppingListId)
    }
    
    // Query for getting complete shopping list with items
    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :shoppingListId")
    suspend fun getShoppingListWithItems(shoppingListId: String): ShoppingListWithItemsEntity?
    
    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE userId = :userId ORDER BY createdAt DESC")
    fun getShoppingListsWithItems(userId: String): Flow<List<ShoppingListWithItemsEntity>>
}

// Entity for Room Transaction query
data class ShoppingListWithItemsEntity(
    @Embedded val shoppingList: ShoppingList,
    @Relation(
        parentColumn = "id",
        entityColumn = "shoppingListId"
    )
    val items: List<ShoppingListItem>
)

// Data class for category count query result
data class CategoryCount(
    val category: IngredientCategory,
    val itemCount: Int
)