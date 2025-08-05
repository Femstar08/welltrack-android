package com.beaconledger.welltrack.domain.repository

import android.net.Uri
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow

interface RecipeRepository {
    fun getAllRecipes(): Flow<List<Recipe>>
    suspend fun getRecipeById(id: String): Result<Recipe?>
    suspend fun saveRecipe(recipe: Recipe): Result<String>
    suspend fun createRecipe(request: RecipeCreateRequest): Result<String>
    suspend fun updateRecipe(recipe: Recipe): Result<Unit>
    suspend fun updateRecipe(recipeId: String, request: RecipeUpdateRequest): Result<Unit>
    suspend fun deleteRecipe(recipeId: String): Result<Unit>
    suspend fun searchRecipes(query: String): Flow<List<Recipe>>
    suspend fun getRecipesByRating(minRating: Float): Flow<List<Recipe>>
    suspend fun importFromUrl(url: String): Result<Recipe>
    suspend fun parseFromOCR(imageUri: Uri): Result<Recipe>
    suspend fun getRecipeIngredients(recipeId: String): Result<List<Ingredient>>
}