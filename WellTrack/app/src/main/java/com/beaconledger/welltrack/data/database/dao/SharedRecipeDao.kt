package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.SharedRecipe
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedRecipeDao {
    @Query("SELECT * FROM shared_recipes WHERE familyGroupId = :groupId")
    fun getSharedRecipes(groupId: String): Flow<List<SharedRecipe>>

    @Query("SELECT * FROM shared_recipes WHERE id = :recipeId")
    suspend fun getSharedRecipeById(recipeId: String): SharedRecipe?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedRecipe(sharedRecipe: SharedRecipe)

    @Update
    suspend fun updateSharedRecipe(sharedRecipe: SharedRecipe)

    @Delete
    suspend fun deleteSharedRecipe(sharedRecipe: SharedRecipe)
}