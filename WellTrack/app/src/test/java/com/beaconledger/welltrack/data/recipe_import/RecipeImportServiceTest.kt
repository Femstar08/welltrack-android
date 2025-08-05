package com.beaconledger.welltrack.data.recipe_import

import android.graphics.Bitmap
import android.net.Uri
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.nutrition.NutritionCalculator
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*

class RecipeImportServiceTest {

    @Mock
    private lateinit var urlParser: RecipeUrlParser

    @Mock
    private lateinit var ocrParser: RecipeOcrParser

    @Mock
    private lateinit var validator: RecipeImportValidator

    @Mock
    private lateinit var nutritionCalculator: NutritionCalculator

    private lateinit var importService: RecipeImportService

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        importService = RecipeImportService(urlParser, ocrParser, validator, nutritionCalculator)
    }

    @Test
    fun `importFromUrl should emit progress and success for valid URL`() = runTest {
        // Arrange
        val url = "https://example.com/recipe"
        val parsedRecipe = createMockParsedRecipe()
        val validationResult = ValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = emptyList(),
            suggestions = emptyList()
        )
        val nutritionInfo = createMockNutritionInfo()

        whenever(urlParser.parseRecipeFromUrl(url)).thenReturn(Result.success(parsedRecipe))
        whenever(validator.validateParsedRecipe(parsedRecipe)).thenReturn(validationResult)
        whenever(nutritionCalculator.calculateNutritionInfo(parsedRecipe.ingredients)).thenReturn(nutritionInfo)

        // Act
        val progressList = importService.importFromUrl(url).toList()

        // Assert
        assertTrue(progressList.isNotEmpty())
        assertTrue(progressList.first() is ImportProgress.Started)
        assertTrue(progressList.any { it is ImportProgress.InProgress })
        assertTrue(progressList.last() is ImportProgress.Success)

        val successProgress = progressList.last() as ImportProgress.Success
        assertEquals(parsedRecipe.name, successProgress.recipe.name)
        assertEquals(ImportSource.URL, successProgress.source)
    }

    @Test
    fun `importFromUrl should emit failure for invalid URL`() = runTest {
        // Arrange
        val url = "https://invalid-url.com/recipe"
        val exception = Exception("Failed to parse URL")

        whenever(urlParser.parseRecipeFromUrl(url)).thenReturn(Result.failure(exception))

        // Act
        val progressList = importService.importFromUrl(url).toList()

        // Assert
        assertTrue(progressList.isNotEmpty())
        assertTrue(progressList.first() is ImportProgress.Started)
        assertTrue(progressList.last() is ImportProgress.Failed)

        val failedProgress = progressList.last() as ImportProgress.Failed
        assertTrue(failedProgress.error.contains("Failed to parse recipe from URL"))
    }

    @Test
    fun `importFromImage should emit progress and success for valid image`() = runTest {
        // Arrange
        val imageUri = mock<Uri>()
        val parsedRecipe = createMockParsedRecipe()
        val validationResult = ValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = emptyList(),
            suggestions = emptyList()
        )
        val nutritionInfo = createMockNutritionInfo()

        whenever(ocrParser.parseRecipeFromImage(imageUri)).thenReturn(Result.success(parsedRecipe))
        whenever(validator.validateParsedRecipe(parsedRecipe)).thenReturn(validationResult)
        whenever(nutritionCalculator.calculateNutritionInfo(parsedRecipe.ingredients)).thenReturn(nutritionInfo)

        // Act
        val progressList = importService.importFromImage(imageUri).toList()

        // Assert
        assertTrue(progressList.isNotEmpty())
        assertTrue(progressList.first() is ImportProgress.Started)
        assertTrue(progressList.any { it is ImportProgress.InProgress })
        assertTrue(progressList.last() is ImportProgress.Success)

        val successProgress = progressList.last() as ImportProgress.Success
        assertEquals(parsedRecipe.name, successProgress.recipe.name)
        assertEquals(ImportSource.OCR, successProgress.source)
    }

    @Test
    fun `importFromBitmap should emit progress and success for valid bitmap`() = runTest {
        // Arrange
        val bitmap = mock<Bitmap>()
        val parsedRecipe = createMockParsedRecipe()
        val validationResult = ValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = emptyList(),
            suggestions = emptyList()
        )
        val nutritionInfo = createMockNutritionInfo()

        whenever(ocrParser.parseRecipeFromBitmap(bitmap)).thenReturn(Result.success(parsedRecipe))
        whenever(validator.validateParsedRecipe(parsedRecipe)).thenReturn(validationResult)
        whenever(nutritionCalculator.calculateNutritionInfo(parsedRecipe.ingredients)).thenReturn(nutritionInfo)

        // Act
        val progressList = importService.importFromBitmap(bitmap).toList()

        // Assert
        assertTrue(progressList.isNotEmpty())
        assertTrue(progressList.first() is ImportProgress.Started)
        assertTrue(progressList.any { it is ImportProgress.InProgress })
        assertTrue(progressList.last() is ImportProgress.Success)

        val successProgress = progressList.last() as ImportProgress.Success
        assertEquals(parsedRecipe.name, successProgress.recipe.name)
        assertEquals(ImportSource.OCR, successProgress.source)
    }

    private fun createMockParsedRecipe(): ParsedRecipe {
        return ParsedRecipe(
            name = "Test Recipe",
            description = "A test recipe",
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            ingredients = listOf(
                Ingredient(
                    name = "flour",
                    quantity = 2.0,
                    unit = "cups",
                    category = IngredientCategory.GRAINS
                )
            ),
            instructions = listOf(
                RecipeStep(
                    stepNumber = 1,
                    instruction = "Mix ingredients together"
                )
            ),
            sourceUrl = "https://example.com/recipe",
            sourceType = RecipeSource.URL_IMPORT
        )
    }

    private fun createMockNutritionInfo(): NutritionInfo {
        return NutritionInfo(
            calories = 300.0,
            carbohydrates = 45.0,
            proteins = 20.0,
            fats = 10.0,
            fiber = 5.0,
            sodium = 500.0,
            potassium = 400.0
        )
    }
}