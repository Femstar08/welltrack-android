package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.database.dao.RecipeIngredientDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.nutrition.NutritionCalculator
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class RecipeRepositoryImplTest {

    private lateinit var recipeDao: RecipeDao
    private lateinit var recipeIngredientDao: RecipeIngredientDao
    private lateinit var nutritionCalculator: NutritionCalculator
    private lateinit var repository: RecipeRepositoryImpl

    @Before
    fun setup() {
        recipeDao = mockk()
        recipeIngredientDao = mockk()
        nutritionCalculator = mockk()
        repository = RecipeRepositoryImpl(recipeDao, recipeIngredientDao, nutritionCalculator)
    }

    @Test
    fun `getAllRecipes returns flow of recipes`() = runTest {
        // Given
        val expectedRecipes = listOf(
            Recipe(
                id = "1",
                name = "Test Recipe",
                prepTime = 15,
                cookTime = 30,
                servings = 4,
                instructions = "Test instructions",
                nutritionInfo = "100,20,5,3,2,200,150",
                sourceType = RecipeSource.MANUAL,
                createdAt = "2023-01-01T00:00:00",
                updatedAt = "2023-01-01T00:00:00"
            )
        )
        every { recipeDao.getAllRecipes() } returns flowOf(expectedRecipes)

        // When
        val result = repository.getAllRecipes()

        // Then
        result.collect { recipes ->
            assertEquals(expectedRecipes, recipes)
        }
        verify { recipeDao.getAllRecipes() }
    }

    @Test
    fun `getRecipeById returns recipe when found`() = runTest {
        // Given
        val recipeId = "test-id"
        val expectedRecipe = Recipe(
            id = recipeId,
            name = "Test Recipe",
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            instructions = "Test instructions",
            nutritionInfo = "100,20,5,3,2,200,150",
            sourceType = RecipeSource.MANUAL,
            createdAt = "2023-01-01T00:00:00",
            updatedAt = "2023-01-01T00:00:00"
        )
        coEvery { recipeDao.getRecipeById(recipeId) } returns expectedRecipe

        // When
        val result = repository.getRecipeById(recipeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedRecipe, result.getOrNull())
        coVerify { recipeDao.getRecipeById(recipeId) }
    }

    @Test
    fun `getRecipeById returns null when not found`() = runTest {
        // Given
        val recipeId = "non-existent-id"
        coEvery { recipeDao.getRecipeById(recipeId) } returns null

        // When
        val result = repository.getRecipeById(recipeId)

        // Then
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
        coVerify { recipeDao.getRecipeById(recipeId) }
    }

    @Test
    fun `createRecipe successfully creates recipe with ingredients`() = runTest {
        // Given
        val ingredients = listOf(
            Ingredient("Flour", 200.0, "g", IngredientCategory.GRAINS),
            Ingredient("Eggs", 2.0, "large", IngredientCategory.PROTEIN)
        )
        val steps = listOf(
            RecipeStep(1, "Mix flour"),
            RecipeStep(2, "Add eggs")
        )
        val request = RecipeCreateRequest(
            name = "Test Recipe",
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            ingredients = ingredients,
            steps = steps
        )
        val expectedNutrition = NutritionInfo(
            calories = 300.0,
            carbohydrates = 50.0,
            proteins = 15.0,
            fats = 8.0,
            fiber = 3.0,
            sodium = 200.0,
            potassium = 150.0
        )

        every { nutritionCalculator.calculateNutritionInfo(ingredients) } returns expectedNutrition
        coEvery { recipeDao.insertRecipe(any()) } just Runs
        coEvery { recipeIngredientDao.insertIngredients(any()) } just Runs

        // When
        val result = repository.createRecipe(request)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        
        coVerify { recipeDao.insertRecipe(any()) }
        coVerify { recipeIngredientDao.insertIngredients(any()) }
        verify { nutritionCalculator.calculateNutritionInfo(ingredients) }
    }

    @Test
    fun `updateRecipe successfully updates existing recipe`() = runTest {
        // Given
        val recipeId = "test-id"
        val existingRecipe = Recipe(
            id = recipeId,
            name = "Old Name",
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            instructions = "Old instructions",
            nutritionInfo = "100,20,5,3,2,200,150",
            sourceType = RecipeSource.MANUAL,
            createdAt = "2023-01-01T00:00:00",
            updatedAt = "2023-01-01T00:00:00"
        )
        val updateRequest = RecipeUpdateRequest(
            name = "New Name",
            prepTime = 20,
            cookTime = null,
            servings = null,
            ingredients = null,
            steps = null,
            tags = null,
            difficulty = null,
            cuisine = null,
            rating = 4.5f
        )

        coEvery { recipeDao.getRecipeById(recipeId) } returns existingRecipe
        coEvery { recipeDao.updateRecipe(any()) } just Runs

        // When
        val result = repository.updateRecipe(recipeId, updateRequest)

        // Then
        assertTrue(result.isSuccess)
        coVerify { recipeDao.getRecipeById(recipeId) }
        coVerify { recipeDao.updateRecipe(any()) }
    }

    @Test
    fun `deleteRecipe successfully deletes recipe and ingredients`() = runTest {
        // Given
        val recipeId = "test-id"
        coEvery { recipeIngredientDao.deleteIngredientsByRecipeId(recipeId) } just Runs
        coEvery { recipeDao.deleteRecipeById(recipeId) } just Runs

        // When
        val result = repository.deleteRecipe(recipeId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { recipeIngredientDao.deleteIngredientsByRecipeId(recipeId) }
        coVerify { recipeDao.deleteRecipeById(recipeId) }
    }

    @Test
    fun `getRecipeIngredients returns list of ingredients`() = runTest {
        // Given
        val recipeId = "test-id"
        val recipeIngredients = listOf(
            RecipeIngredient(
                id = "1",
                recipeId = recipeId,
                name = "Flour",
                quantity = 200.0,
                unit = "g",
                category = IngredientCategory.GRAINS
            ),
            RecipeIngredient(
                id = "2",
                recipeId = recipeId,
                name = "Eggs",
                quantity = 2.0,
                unit = "large",
                category = IngredientCategory.PROTEIN
            )
        )
        val expectedIngredients = listOf(
            Ingredient("Flour", 200.0, "g", IngredientCategory.GRAINS),
            Ingredient("Eggs", 2.0, "large", IngredientCategory.PROTEIN)
        )

        coEvery { recipeIngredientDao.getIngredientsByRecipeId(recipeId) } returns recipeIngredients

        // When
        val result = repository.getRecipeIngredients(recipeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedIngredients, result.getOrNull())
        coVerify { recipeIngredientDao.getIngredientsByRecipeId(recipeId) }
    }

    @Test
    fun `searchRecipes returns filtered recipes`() = runTest {
        // Given
        val query = "pasta"
        val expectedRecipes = listOf(
            Recipe(
                id = "1",
                name = "Pasta Carbonara",
                prepTime = 15,
                cookTime = 20,
                servings = 4,
                instructions = "Cook pasta...",
                nutritionInfo = "400,60,18,12,3,300,200",
                sourceType = RecipeSource.MANUAL,
                createdAt = "2023-01-01T00:00:00",
                updatedAt = "2023-01-01T00:00:00"
            )
        )
        every { recipeDao.searchRecipes(query) } returns flowOf(expectedRecipes)

        // When
        val result = repository.searchRecipes(query)

        // Then
        result.collect { recipes ->
            assertEquals(expectedRecipes, recipes)
        }
        verify { recipeDao.searchRecipes(query) }
    }
}