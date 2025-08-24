package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.SharedShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedShoppingListItemDao {
    @Query("SELECT * FROM shared_shopping_list_items WHERE shoppingListId = :listId")
    fun getSharedShoppingListItems(listId: String): Flow<List<SharedShoppingListItem>>

    @Query("SELECT * FROM shared_shopping_list_items WHERE id = :itemId")
    suspend fun getSharedShoppingListItemById(itemId: String): SharedShoppingListItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedShoppingListItem(sharedShoppingListItem: SharedShoppingListItem)

    @Update
    suspend fun updateSharedShoppingListItem(sharedShoppingListItem: SharedShoppingListItem)

    @Delete
    suspend fun deleteSharedShoppingListItem(sharedShoppingListItem: SharedShoppingListItem)
}