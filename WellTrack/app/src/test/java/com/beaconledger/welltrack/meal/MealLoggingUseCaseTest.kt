package com.beaconledger.welltrack.meal

import com.beaconledger.welltrack.data.meal.MealRecognitionService
import com.beaconledger.welltrack.data.meal.MealScoringService
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.nutrition.NutritionCalculator
import com.beaconledger.welltrack.domain.repository.MealRepository
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import com.beaconledger.welltrack.domain.usecase.MealLoggingUseCase
import com.google.gson.Gson
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class MealLoggingUseCaseTest {
    
    private lateinit var mealRepository: MealRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var mealRecognitionService: MealRecognitionService
    private lateinit var mealScoringService: MealScoringService
    private lateinit var nutritionCalculator: NutritionCalculator
    private lateinit var gson: Gson
    private lateinit var mealLoggingUseCase: MealLoggingUseCase
    
    @Before
    fun setup() {
        mealRepository = mockk()
        recipeRepository = mockk()
        mealRecognitionService = mockk()
        mealScoringService = mockk()
        nutritionCalculator = mockk()
        gson = Gson()
        
        mealLoggingUseCase = MealLoggingUseCase(
            mealRepository = mealRepository,
            recipeRepository = recipeRepository,
            mealRecognitionService = mealRecognitionService,
            mealScoringService = mealScoringService,
            nutritionCalculator = nutritionCalculator,
            gson = gson
        )
    }
    
    @Test
    fun `logManualMeal should calculate nutrition and score correctly`() = runTest {
        // Given
        val userId = "test-user-id"
        val mealName = "Test Meal"
        val ingredients = listOf(
            Ingredient(
                name = "Chicken Breast",
                quantity = 100.0,
                unit = "g",
                category = IngredientCategory.PROTEIN
            ),
            Ingredient(
                name = "Broccoli",
                quantity = 150.0,
                unit = "g",
                category = IngredientCategory.VEGETABLES
            )
        )
        val mealType = MealType.LUNCH
        val portions = 1.0f
        
        val nutritionInfo = NutritionInfo(
            calories = 200.0,
            carbohydrates = 10.0,
            proteins = 30.0,
            fats = 5.0,
            fiber = 5.0,
            sodium = 100.0,
            potassium = 400.0
        )
        
        val mealScore = MealScore.A
        
        every { nutritionCalculator.calculateNutritionInfo(ingredients) } returns nutritionInfo
        every { nutritionCalculator.scaleNutrition(nutritionInfo, portions.toDouble()) } returns nutritionInfo
        every { mealScoringService.calculateMealScore(nutritionInfo) } returns mealScore
        every { mealRepository.logMeal(any()) } returns Result.success("meal-id")
        
        // When
        val result = mealLoggingUseCase.logManualMeal(
            userId = userId,
            mealName = mealName,
            ingredients = ingredients,
            mealType = mealType,
            portions = portions
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("meal-id", result.getOrNull())
        
        verify { nutritionCalculator.calculateNutritionInfo(ingredients) }
        verify { nutritionCalculator.scaleNutrition(nutritionInfo, portions.toDouble()) }
        verify { mealScoringService.calculateMealScore(nutritionInfo) }
        verify { mealRepository.logMeal(any()) }
    }
    
    @Test
    fun `logMealFromRecipe should use recipe nutrition info`() = runTest {
        // Given
        val userId = "test-user-id"
        val recipeId = "test-recipe-id"
        val mealType = MealType.DINNER
        val portions = 1.5f
        
        val nutritionInfo = NutritionInfo(
            calories = 300.0,
            carbohydrates = 20.0,
            proteins = 25.0,
            fats = 15.0,
            fiber = 8.0,
            sodium = 200.0,
            potassium = 500.0
        )
        
        val recipe = Recipe(
            id = recipeId,
            name = "Test Recipe",
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            instructions = "[]",
            nutritionInfo = gson.toJson(nutritionInfo),
            sourceType = RecipeSource.MANUAL
        )
        
        val scaledNutrition = nutritionInfo.copy(calories = 450.0) // 1.5x scaling
        val mealScore = MealScore.B
        
        every { recipeRepository.getRecipeById(recipeId) } returns Result.success(recipe)
        every { nutritionCalculator.scaleNutrition(nutritionInfo, portions.toDouble()) } returns scaledNutrition
        every { mealScoringService.calculateMealScore(scaledNutrition) } returns mealScore
        every { mealRepository.logMeal(any()) } returns Result.success("meal-id")
        
        // When
        val result = mealLoggingUseCase.logMealFromRecipe(
            userId = userId,
            recipeId = recipeId,
            mealType = mealType,
            portions = portions
        )
        
        // Then
        assertTrue(result.isSuccess)
        verify { recipeRepository.getRecipeById(recipeId) }
        verify { nutritionCalculator.scaleNutrition(nutritionInfo, portions.toDouble()) }
        verify { mealScoringService.calculateMealScore(scaledNutrition) }
        verify { mealRepository.logMeal(any()) }
    }
    
    @Test
    fun `updateMealStatus should call repository correctly`() = runTest {
        // Given
        val mealId = "test-meal-id"
        val status = MealStatus.EATEN
        
        every { mealRepository.updateMealStatus(mealId, status) } returns Result.success(Unit)
        
        // When
        val result = mealLoggingUseCase.updateMealStatus(mealId, status)
        
        // Then
        assertTrue(result.isSuccess)
        verify { mealRepository.updateMealStatus(mealId, status) }
    }
}