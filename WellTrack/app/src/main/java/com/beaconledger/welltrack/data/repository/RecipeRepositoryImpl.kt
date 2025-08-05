package com.beaconledger.welltrack.data.repository

import android.net.Uri
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.database.dao.RecipeIngredientDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepositoryImpl @Inject constructor(
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val nutritionCalculator: com.beaconledger.welltrack.data.nutrition.NutritionCalculator,
    private val urlParser: com.beaconledger.welltrack.data.recipe_import.RecipeUrlParser,
    private val ocrParser: com.beaconledger.welltrack.data.recipe_import.RecipeOcrParser
) : RecipeRepository {

    override fun getAllRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllRecipes()
    }

    override suspend fun getRecipeById(id: String): Result<Recipe?> {
        return try {
            val recipe = recipeDao.getRecipeById(id)
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveRecipe(recipe: Recipe): Result<String> {
        return try {
            recipeDao.insertRecipe(recipe)
            Result.success(recipe.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createRecipe(request: RecipeCreateRequest): Result<String> {
        return try {
            val recipeId = UUID.randomUUID().toString()
            val currentTime = LocalDateTime.now().toString()
            
            // Create recipe
            val recipe = Recipe(
                id = recipeId,
                name = request.name,
                prepTime = request.prepTime,
                cookTime = request.cookTime,
                servings = request.servings,
                instructions = serializeSteps(request.steps),
                nutritionInfo = nutritionCalculator.calculateNutritionInfo(request.ingredients).let { serializeNutritionInfo(it) },
                sourceType = RecipeSource.MANUAL,
                tags = serializeStringList(request.tags),
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            // Save recipe
            recipeDao.insertRecipe(recipe)
            
            // Save ingredients
            val recipeIngredients = request.ingredients.mapIndexed { index, ingredient ->
                RecipeIngredient(
                    id = UUID.randomUUID().toString(),
                    recipeId = recipeId,
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    category = ingredient.category,
                    isOptional = ingredient.isOptional,
                    notes = ingredient.notes
                )
            }
            
            recipeIngredientDao.insertIngredients(recipeIngredients)
            
            Result.success(recipeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRecipe(recipe: Recipe): Result<Unit> {
        return try {
            val updatedRecipe = recipe.copy(updatedAt = LocalDateTime.now().toString())
            recipeDao.updateRecipe(updatedRecipe)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateRecipe(recipeId: String, request: RecipeUpdateRequest): Result<Unit> {
        return try {
            val existingRecipe = recipeDao.getRecipeById(recipeId)
                ?: return Result.failure(Exception("Recipe not found"))

            val updatedRecipe = existingRecipe.copy(
                name = request.name ?: existingRecipe.name,
                prepTime = request.prepTime ?: existingRecipe.prepTime,
                cookTime = request.cookTime ?: existingRecipe.cookTime,
                servings = request.servings ?: existingRecipe.servings,
                instructions = request.steps?.let { serializeSteps(it) } ?: existingRecipe.instructions,
                nutritionInfo = request.ingredients?.let { 
                    nutritionCalculator.calculateNutritionInfo(it).let { nutrition -> serializeNutritionInfo(nutrition) }
                } ?: existingRecipe.nutritionInfo,
                tags = request.tags?.let { serializeStringList(it) } ?: existingRecipe.tags,
                rating = request.rating ?: existingRecipe.rating,
                updatedAt = LocalDateTime.now().toString()
            )

            recipeDao.updateRecipe(updatedRecipe)

            // Update ingredients if provided
            request.ingredients?.let { ingredients ->
                // Delete existing ingredients
                recipeIngredientDao.deleteIngredientsByRecipeId(recipeId)
                
                // Insert new ingredients
                val recipeIngredients = ingredients.map { ingredient ->
                    RecipeIngredient(
                        id = UUID.randomUUID().toString(),
                        recipeId = recipeId,
                        name = ingredient.name,
                        quantity = ingredient.quantity,
                        unit = ingredient.unit,
                        category = ingredient.category,
                        isOptional = ingredient.isOptional,
                        notes = ingredient.notes
                    )
                }
                
                recipeIngredientDao.insertIngredients(recipeIngredients)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            // Delete ingredients first
            recipeIngredientDao.deleteIngredientsByRecipeId(recipeId)
            // Delete recipe
            recipeDao.deleteRecipeById(recipeId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun searchRecipes(query: String): Flow<List<Recipe>> {
        return recipeDao.searchRecipes(query)
    }

    override suspend fun getRecipesByRating(minRating: Float): Flow<List<Recipe>> {
        return recipeDao.getRecipesByRating(minRating)
    }

    override suspend fun getRecipeIngredients(recipeId: String): Result<List<Ingredient>> {
        return try {
            val recipeIngredients = recipeIngredientDao.getIngredientsByRecipeId(recipeId)
            val ingredients = recipeIngredients.map { recipeIngredient ->
                Ingredient(
                    name = recipeIngredient.name,
                    quantity = recipeIngredient.quantity,
                    unit = recipeIngredient.unit,
                    category = recipeIngredient.category,
                    isOptional = recipeIngredient.isOptional,
                    notes = recipeIngredient.notes
                )
            }
            Result.success(ingredients)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun importFromUrl(url: String): Result<Recipe> {
        return try {
            // Use the injected URL parser
            val parseResult = urlParser.parseRecipeFromUrl(url)
            
            if (parseResult.isFailure) {
                return Result.failure(parseResult.exceptionOrNull() ?: Exception("Failed to parse URL"))
            }
            
            val parsedRecipe = parseResult.getOrThrow()
            val nutritionInfo = nutritionCalculator.calculateNutritionInfo(parsedRecipe.ingredients)
            val currentTime = LocalDateTime.now().toString()
            
            val recipe = Recipe(
                id = UUID.randomUUID().toString(),
                name = parsedRecipe.name,
                prepTime = parsedRecipe.prepTime,
                cookTime = parsedRecipe.cookTime,
                servings = parsedRecipe.servings,
                instructions = serializeSteps(parsedRecipe.instructions),
                nutritionInfo = serializeNutritionInfo(nutritionInfo),
                sourceType = RecipeSource.URL_IMPORT,
                sourceUrl = url,
                tags = serializeStringList(parsedRecipe.tags),
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            // Save the recipe and ingredients
            recipeDao.insertRecipe(recipe)
            
            val recipeIngredients = parsedRecipe.ingredients.map { ingredient ->
                RecipeIngredient(
                    id = UUID.randomUUID().toString(),
                    recipeId = recipe.id,
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    category = ingredient.category,
                    isOptional = ingredient.isOptional,
                    notes = ingredient.notes
                )
            }
            
            recipeIngredientDao.insertIngredients(recipeIngredients)
            
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun parseFromOCR(imageUri: Uri): Result<Recipe> {
        return try {
            // Use the injected OCR parser
            val parseResult = ocrParser.parseRecipeFromImage(imageUri)
            
            if (parseResult.isFailure) {
                return Result.failure(parseResult.exceptionOrNull() ?: Exception("Failed to parse OCR"))
            }
            
            val parsedRecipe = parseResult.getOrThrow()
            val nutritionInfo = nutritionCalculator.calculateNutritionInfo(parsedRecipe.ingredients)
            val currentTime = LocalDateTime.now().toString()
            
            val recipe = Recipe(
                id = UUID.randomUUID().toString(),
                name = parsedRecipe.name,
                prepTime = parsedRecipe.prepTime,
                cookTime = parsedRecipe.cookTime,
                servings = parsedRecipe.servings,
                instructions = serializeSteps(parsedRecipe.instructions),
                nutritionInfo = serializeNutritionInfo(nutritionInfo),
                sourceType = RecipeSource.OCR_SCAN,
                tags = serializeStringList(parsedRecipe.tags),
                createdAt = currentTime,
                updatedAt = currentTime
            )
            
            // Save the recipe and ingredients
            recipeDao.insertRecipe(recipe)
            
            val recipeIngredients = parsedRecipe.ingredients.map { ingredient ->
                RecipeIngredient(
                    id = UUID.randomUUID().toString(),
                    recipeId = recipe.id,
                    name = ingredient.name,
                    quantity = ingredient.quantity,
                    unit = ingredient.unit,
                    category = ingredient.category,
                    isOptional = ingredient.isOptional,
                    notes = ingredient.notes
                )
            }
            
            recipeIngredientDao.insertIngredients(recipeIngredients)
            
            Result.success(recipe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper methods for serialization
    private fun serializeSteps(steps: List<RecipeStep>): String {
        return steps.joinToString("|||") { step ->
            "${step.stepNumber}::${step.instruction}::${step.duration ?: ""}::${step.temperature ?: ""}::${step.equipment.joinToString(",")}"
        }
    }

    private fun deserializeSteps(serialized: String): List<RecipeStep> {
        if (serialized.isEmpty()) return emptyList()
        return serialized.split("|||").mapNotNull { stepStr ->
            val parts = stepStr.split("::")
            if (parts.size >= 2) {
                RecipeStep(
                    stepNumber = parts[0].toIntOrNull() ?: 1,
                    instruction = parts[1],
                    duration = parts.getOrNull(2)?.toIntOrNull(),
                    temperature = parts.getOrNull(3)?.takeIf { it.isNotEmpty() },
                    equipment = parts.getOrNull(4)?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
                )
            } else null
        }
    }

    private fun serializeStringList(list: List<String>): String {
        return list.joinToString(",")
    }

    private fun serializeNutritionInfo(nutrition: NutritionInfo): String {
        return "${nutrition.calories},${nutrition.carbohydrates},${nutrition.proteins},${nutrition.fats},${nutrition.fiber},${nutrition.sodium},${nutrition.potassium}"
    }


}