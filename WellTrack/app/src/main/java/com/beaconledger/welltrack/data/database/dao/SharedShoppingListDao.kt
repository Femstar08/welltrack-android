package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.SharedShoppingList
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedShoppingListDao {
    @Query("SELECT * FROM shared_shopping_lists WHERE familyGroupId = :groupId")
    fun getSharedShoppingLists(groupId: String): Flow<List<SharedShoppingList>>

    @Query("SELECT * FROM shared_shopping_lists WHERE id = :listId")
    suspend fun getSharedShoppingListById(listId: String): SharedShoppingList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedShoppingList(sharedShoppingList: SharedShoppingList)

    @Update
    suspend fun updateSharedShoppingList(sharedShoppingList: SharedShoppingList)

    @Delete
    suspend fun deleteSharedShoppingList(sharedShoppingList: SharedShoppingList)
}