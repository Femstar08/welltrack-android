package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.RecipeIngredient
import com.beaconledger.welltrack.data.model.IngredientCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeIngredientDao {
    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY name ASC")
    suspend fun getIngredientsByRecipeId(recipeId: String): List<RecipeIngredient>

    @Query("SELECT * FROM recipe_ingredients WHERE recipeId = :recipeId ORDER BY name ASC")
    fun getIngredientsByRecipeIdFlow(recipeId: String): Flow<List<RecipeIngredient>>

    @Query("SELECT * FROM recipe_ingredients WHERE category = :category ORDER BY name ASC")
    fun getIngredientsByCategory(category: IngredientCategory): Flow<List<RecipeIngredient>>

    @Query("SELECT DISTINCT name FROM recipe_ingredients ORDER BY name ASC")
    suspend fun getAllIngredientNames(): List<String>

    @Query("SELECT * FROM recipe_ingredients WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    fun searchIngredients(searchQuery: String): Flow<List<RecipeIngredient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: RecipeIngredient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredients(ingredients: List<RecipeIngredient>)

    @Update
    suspend fun updateIngredient(ingredient: RecipeIngredient)

    @Delete
    suspend fun deleteIngredient(ingredient: RecipeIngredient)

    @Query("DELETE FROM recipe_ingredients WHERE recipeId = :recipeId")
    suspend fun deleteIngredientsByRecipeId(recipeId: String)

    @Query("DELETE FROM recipe_ingredients WHERE id = :ingredientId")
    suspend fun deleteIngredientById(ingredientId: String)
}