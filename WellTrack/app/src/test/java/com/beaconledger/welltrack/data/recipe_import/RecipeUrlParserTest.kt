package com.beaconledger.welltrack.data.recipe_import

import com.beaconledger.welltrack.data.model.IngredientCategory
import com.beaconledger.welltrack.data.model.RecipeSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.MockedStatic
import org.mockito.Mockito.*

class RecipeUrlParserTest {

    private lateinit var parser: RecipeUrlParser

    @Before
    fun setup() {
        parser = RecipeUrlParser()
    }

    @Test
    fun `parseRecipeFromUrl should return failure for invalid URL`() = runTest {
        val result = parser.parseRecipeFromUrl("invalid-url")
        
        assertTrue(result.isFailure)
    }

    @Test
    fun `parseRecipeFromUrl should return failure for unreachable URL`() = runTest {
        val result = parser.parseRecipeFromUrl("https://nonexistent-recipe-site.com/recipe")
        
        assertTrue(result.isFailure)
    }

    @Test
    fun `categorizeIngredient should correctly categorize protein ingredients`() {
        val parser = RecipeUrlParser()
        
        // Use reflection to access private method for testing
        val method = parser.javaClass.getDeclaredMethod("categorizeIngredient", String::class.java)
        method.isAccessible = true
        
        val chickenCategory = method.invoke(parser, "chicken breast") as IngredientCategory
        val beefCategory = method.invoke(parser, "ground beef") as IngredientCategory
        val eggCategory = method.invoke(parser, "eggs") as IngredientCategory
        
        assertEquals(IngredientCategory.PROTEIN, chickenCategory)
        assertEquals(IngredientCategory.PROTEIN, beefCategory)
        assertEquals(IngredientCategory.PROTEIN, eggCategory)
    }

    @Test
    fun `categorizeIngredient should correctly categorize vegetable ingredients`() {
        val parser = RecipeUrlParser()
        
        val method = parser.javaClass.getDeclaredMethod("categorizeIngredient", String::class.java)
        method.isAccessible = true
        
        val onionCategory = method.invoke(parser, "yellow onion") as IngredientCategory
        val carrotCategory = method.invoke(parser, "carrots") as IngredientCategory
        val garlicCategory = method.invoke(parser, "garlic cloves") as IngredientCategory
        
        assertEquals(IngredientCategory.VEGETABLES, onionCategory)
        assertEquals(IngredientCategory.VEGETABLES, carrotCategory)
        assertEquals(IngredientCategory.VEGETABLES, garlicCategory)
    }

    @Test
    fun `parseTimeToMinutes should correctly parse ISO 8601 duration`() {
        val parser = RecipeUrlParser()
        
        val method = parser.javaClass.getDeclaredMethod("parseTimeToMinutes", String::class.java)
        method.isAccessible = true
        
        val minutes15 = method.invoke(parser, "PT15M") as Int
        val minutes30 = method.invoke(parser, "PT30M") as Int
        val hour1 = method.invoke(parser, "PT1H") as Int
        val hour1min30 = method.invoke(parser, "PT1H30M") as Int
        
        assertEquals(15, minutes15)
        assertEquals(30, minutes30)
        assertEquals(60, hour1)
        assertEquals(90, hour1min30)
    }

    @Test
    fun `parseTimeToMinutes should correctly parse text duration`() {
        val parser = RecipeUrlParser()
        
        val method = parser.javaClass.getDeclaredMethod("parseTimeToMinutes", String::class.java)
        method.isAccessible = true
        
        val minutes15 = method.invoke(parser, "15 minutes") as Int
        val hour1 = method.invoke(parser, "1 hour") as Int
        val empty = method.invoke(parser, "") as Int
        val invalid = method.invoke(parser, "invalid") as Int
        
        assertEquals(15, minutes15)
        assertEquals(60, hour1)
        assertEquals(0, empty)
        assertEquals(0, invalid)
    }

    @Test
    fun `parseIngredientText should correctly parse ingredient with quantity and unit`() {
        val parser = RecipeUrlParser()
        
        val method = parser.javaClass.getDeclaredMethod("parseIngredientText", String::class.java)
        method.isAccessible = true
        
        val ingredient = method.invoke(parser, "2 cups flour") as com.beaconledger.welltrack.data.model.Ingredient
        
        assertEquals("flour", ingredient.name)
        assertEquals(2.0, ingredient.quantity, 0.01)
        assertEquals("cups", ingredient.unit)
        assertEquals(IngredientCategory.GRAINS, ingredient.category)
    }

    @Test
    fun `parseIngredientText should handle ingredient without quantity`() {
        val parser = RecipeUrlParser()
        
        val method = parser.javaClass.getDeclaredMethod("parseIngredientText", String::class.java)
        method.isAccessible = true
        
        val ingredient = method.invoke(parser, "salt to taste") as com.beaconledger.welltrack.data.model.Ingredient
        
        assertEquals("salt to taste", ingredient.name)
        assertEquals(1.0, ingredient.quantity, 0.01)
        assertEquals("", ingredient.unit)
        assertEquals(IngredientCategory.SPICES, ingredient.category)
    }
}