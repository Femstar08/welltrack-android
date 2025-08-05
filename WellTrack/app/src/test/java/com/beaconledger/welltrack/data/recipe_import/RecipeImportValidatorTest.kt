package com.beaconledger.welltrack.data.recipe_import

import com.beaconledger.welltrack.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RecipeImportValidatorTest {

    private lateinit var validator: RecipeImportValidator

    @Before
    fun setup() {
        validator = RecipeImportValidator()
    }

    @Test
    fun `validateParsedRecipe should return valid for complete recipe`() {
        val parsedRecipe = createValidParsedRecipe()
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validateParsedRecipe should return error for missing name`() {
        val parsedRecipe = createValidParsedRecipe().copy(name = "")
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.MISSING_NAME))
    }

    @Test
    fun `validateParsedRecipe should return error for no ingredients`() {
        val parsedRecipe = createValidParsedRecipe().copy(ingredients = emptyList())
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.NO_INGREDIENTS))
    }

    @Test
    fun `validateParsedRecipe should return error for no instructions`() {
        val parsedRecipe = createValidParsedRecipe().copy(instructions = emptyList())
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.NO_INSTRUCTIONS))
    }

    @Test
    fun `validateParsedRecipe should return error for invalid prep time`() {
        val parsedRecipe = createValidParsedRecipe().copy(prepTime = -5)
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.INVALID_PREP_TIME))
    }

    @Test
    fun `validateParsedRecipe should return error for invalid cook time`() {
        val parsedRecipe = createValidParsedRecipe().copy(cookTime = -10)
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.INVALID_COOK_TIME))
    }

    @Test
    fun `validateParsedRecipe should return error for invalid servings`() {
        val parsedRecipe = createValidParsedRecipe().copy(servings = 0)
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.contains(ValidationError.INVALID_SERVINGS))
    }

    @Test
    fun `validateParsedRecipe should return warning for short name`() {
        val parsedRecipe = createValidParsedRecipe().copy(name = "AB")
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertTrue(result.isValid) // Still valid, just a warning
        assertTrue(result.warnings.contains(ValidationWarning.SHORT_NAME))
    }

    @Test
    fun `validateParsedRecipe should return warning for no timing info`() {
        val parsedRecipe = createValidParsedRecipe().copy(prepTime = 0, cookTime = 0)
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertTrue(result.isValid) // Still valid, just a warning
        assertTrue(result.warnings.contains(ValidationWarning.NO_TIMING_INFO))
    }

    @Test
    fun `validateParsedRecipe should return error for ingredient with missing name`() {
        val invalidIngredient = Ingredient(
            name = "",
            quantity = 1.0,
            unit = "cup"
        )
        val parsedRecipe = createValidParsedRecipe().copy(
            ingredients = listOf(invalidIngredient)
        )
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is ValidationError.MISSING_INGREDIENT_NAME })
    }

    @Test
    fun `validateParsedRecipe should return error for ingredient with invalid quantity`() {
        val invalidIngredient = Ingredient(
            name = "flour",
            quantity = -1.0,
            unit = "cup"
        )
        val parsedRecipe = createValidParsedRecipe().copy(
            ingredients = listOf(invalidIngredient)
        )
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is ValidationError.INVALID_INGREDIENT_QUANTITY })
    }

    @Test
    fun `validateParsedRecipe should return error for short instruction`() {
        val shortInstruction = RecipeStep(
            stepNumber = 1,
            instruction = "Mix"
        )
        val parsedRecipe = createValidParsedRecipe().copy(
            instructions = listOf(shortInstruction)
        )
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it is ValidationError.SHORT_INSTRUCTION })
    }

    @Test
    fun `validateParsedRecipe should detect OCR errors for OCR scanned recipes`() {
        val parsedRecipe = createValidParsedRecipe().copy(
            sourceType = RecipeSource.OCR_SCAN,
            name = "Chicken Rn Recipe" // "rn" is common OCR error for "m"
        )
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertTrue(result.warnings.contains(ValidationWarning.POTENTIAL_OCR_ERROR))
    }

    @Test
    fun `validateParsedRecipe should provide helpful suggestions`() {
        val parsedRecipe = createValidParsedRecipe().copy(
            name = "",
            prepTime = 0,
            cookTime = 0
        )
        
        val result = validator.validateParsedRecipe(parsedRecipe)
        
        assertTrue(result.suggestions.isNotEmpty())
        assertTrue(result.suggestions.any { it.contains("name") })
        assertTrue(result.suggestions.any { it.contains("timing") })
    }

    private fun createValidParsedRecipe(): ParsedRecipe {
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
                ),
                Ingredient(
                    name = "eggs",
                    quantity = 2.0,
                    unit = "pieces",
                    category = IngredientCategory.PROTEIN
                )
            ),
            instructions = listOf(
                RecipeStep(
                    stepNumber = 1,
                    instruction = "Mix the flour and eggs together in a large bowl"
                ),
                RecipeStep(
                    stepNumber = 2,
                    instruction = "Cook the mixture in a pan over medium heat"
                )
            ),
            sourceUrl = "https://example.com/recipe",
            sourceType = RecipeSource.URL_IMPORT
        )
    }
}