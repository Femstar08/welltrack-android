package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.Recipe
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes ORDER BY createdAt DESC")
    fun getAllRecipes(): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): Recipe?

    @Query("SELECT * FROM recipes WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchRecipes(searchQuery: String): Flow<List<Recipe>>

    @Query("SELECT * FROM recipes WHERE rating >= :minRating ORDER BY rating DESC")
    fun getRecipesByRating(minRating: Float): Flow<List<Recipe>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: Recipe)

    @Update
    suspend fun updateRecipe(recipe: Recipe)

    @Delete
    suspend fun deleteRecipe(recipe: Recipe)

    @Query("DELETE FROM recipes WHERE id = :recipeId")
    suspend fun deleteRecipeById(recipeId: String)
    
    @Query("""
        SELECT DISTINCT r.* FROM recipes r 
        INNER JOIN recipe_ingredients ri ON r.id = ri.recipeId 
        WHERE ri.name LIKE '%' || :ingredientName || '%' 
        ORDER BY r.rating DESC, r.createdAt DESC 
        LIMIT :limit
    """)
    suspend fun getRecipesByIngredient(ingredientName: String, limit: Int = 10): List<Recipe>
}