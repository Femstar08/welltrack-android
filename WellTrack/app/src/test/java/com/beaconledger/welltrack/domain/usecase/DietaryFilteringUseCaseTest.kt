package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.analysis.DietaryFilteringService
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class DietaryFilteringUseCaseTest {
    
    private lateinit var dietaryFilteringService: DietaryFilteringService
    private lateinit var dietaryRestrictionsRepository: DietaryRestrictionsRepository
    private lateinit var recipeRepository: RecipeRepository
    private lateinit var mealPlanRepository: MealPlanRepository
    private lateinit var shoppingListRepository: ShoppingListRepository
    private lateinit var useCase: DietaryFilteringUseCase
    
    private val testUserId = "test-user-id"
    private val testMealPlanId = "test-meal-plan-id"
    private val testShoppingListId = "test-shopping-list-id"
    
    @Before
    fun setup() {
        dietaryFilteringService = mockk()
        dietaryRestrictionsRepository = mockk()
        recipeRepository = mockk()
        mealPlanRepository = mockk()
        shoppingListRepository = mockk()
        
        useCase = DietaryFilteringUseCase(
            dietaryFilteringService,
            dietaryRestrictionsRepository,
            recipeRepository,
            mealPlanRepository,
            shoppingListRepository
        )
    }
    
    @Test
    fun `filterRecipes should return filtered recipes successfully`() = runTest {
        // Given
        val testRecipes = listOf(
            createTestRecipe("1", "Vegetarian Pasta"),
            createTestRecipe("2", "Chicken Curry")
        )
        val testDietaryProfile = createTestDietaryProfile()
        val expectedFilteredRecipes = FilteredRecipes(
            compatibleRecipes = listOf(testRecipes[0]),
            incompatibleRecipes = listOf(
                IncompatibleRecipe(
                    recipe = testRecipes[1],
                    compatibility = DietaryCompatibility(false, emptyList(), emptyList(), 0.3f),
                    failedCriteria = listOf("Violates vegetarian restriction")
                )
            ),
            totalFiltered = 2,
            compatibilityStats = CompatibilityStats(2, 1, 1, 0.5f)
        )
        
        coEvery { recipeRepository.getAllRecipes(testUserId) } returns flowOf(testRecipes)
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(testUserId) } returns flowOf(testDietaryProfile)
        coEvery { dietaryFilteringService.filterRecipes(testRecipes, testDietaryProfile, any()) } returns Result.success(expectedFilteredRecipes)
        
        // When
        val result = useCase.filterRecipes(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        val filteredRecipes = result.getOrNull()
        assertNotNull(filteredRecipes)
        assertEquals(1, filteredRecipes!!.compatibleRecipes.size)
        assertEquals(1, filteredRecipes.incompatibleRecipes.size)
        assertEquals("Vegetarian Pasta", filteredRecipes.compatibleRecipes[0].name)
    }
    
    @Test
    fun `filterRecipes with specific recipe IDs should filter only those recipes`() = runTest {
        // Given
        val recipeIds = listOf("1", "2")
        val testRecipes = listOf(
            createTestRecipe("1", "Vegetarian Pasta"),
            createTestRecipe("2", "Chicken Curry")
        )
        val testDietaryProfile = createTestDietaryProfile()
        val expectedFilteredRecipes = FilteredRecipes(
            compatibleRecipes = listOf(testRecipes[0]),
            incompatibleRecipes = listOf(
                IncompatibleRecipe(
                    recipe = testRecipes[1],
                    compatibility = DietaryCompatibility(false, emptyList(), emptyList(), 0.3f),
                    failedCriteria = listOf("Violates vegetarian restriction")
                )
            ),
            totalFiltered = 2,
            compatibilityStats = CompatibilityStats(2, 1, 1, 0.5f)
        )
        
        coEvery { recipeRepository.getRecipesByIds(recipeIds) } returns testRecipes
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(testUserId) } returns flowOf(testDietaryProfile)
        coEvery { dietaryFilteringService.filterRecipes(testRecipes, testDietaryProfile, any()) } returns Result.success(expectedFilteredRecipes)
        
        // When
        val result = useCase.filterRecipes(testUserId, recipeIds)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { recipeRepository.getRecipesByIds(recipeIds) }
        coVerify(exactly = 0) { recipeRepository.getAllRecipes(any()) }
    }
    
    @Test
    fun `filterMealPlan should return filtered meal plan successfully`() = runTest {
        // Given
        val testMealPlan = createTestWeeklyMealPlan()
        val testDietaryProfile = createTestDietaryProfile()
        val expectedFilteredMealPlan = FilteredMealPlan(
            originalMealPlan = testMealPlan,
            compatibleMeals = testMealPlan.plannedMeals.take(1),
            incompatibleMeals = listOf(
                IncompatibleMeal(
                    plannedMeal = testMealPlan.plannedMeals[1],
                    recipe = testMealPlan.recipes[1],
                    compatibility = DietaryCompatibility(false, emptyList(), emptyList(), 0.3f),
                    suggestedAlternatives = emptyList()
                )
            ),
            warnings = emptyList(),
            overallCompatibilityScore = 0.5f
        )
        
        coEvery { mealPlanRepository.getWeeklyMealPlan(testMealPlanId) } returns flowOf(testMealPlan)
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(testUserId) } returns flowOf(testDietaryProfile)
        coEvery { dietaryFilteringService.filterMealPlan(testUserId, testMealPlan, testDietaryProfile, 0.7f) } returns Result.success(expectedFilteredMealPlan)
        
        // When
        val result = useCase.filterMealPlan(testUserId, testMealPlanId)
        
        // Then
        assertTrue(result.isSuccess)
        val filteredMealPlan = result.getOrNull()
        assertNotNull(filteredMealPlan)
        assertEquals(1, filteredMealPlan!!.compatibleMeals.size)
        assertEquals(1, filteredMealPlan.incompatibleMeals.size)
        assertEquals(0.5f, filteredMealPlan.overallCompatibilityScore, 0.01f)
    }
    
    @Test
    fun `validateRecipeImport should return validation result successfully`() = runTest {
        // Given
        val testRecipe = createTestRecipe("1", "Chicken Curry")
        val testDietaryProfile = createTestDietaryProfile()
        val expectedValidation = RecipeImportValidation(
            recipe = testRecipe,
            compatibility = DietaryCompatibility(
                isCompatible = false,
                violations = listOf(
                    DietaryViolation(
                        type = ViolationType.DIETARY_RESTRICTION,
                        restriction = DietaryRestrictionType.VEGETARIAN,
                        allergen = null,
                        severity = RestrictionSeverity.STRICT,
                        description = "Contains meat",
                        affectedIngredients = listOf("chicken")
                    )
                ),
                warnings = emptyList(),
                score = 0.3f
            ),
            canImport = true,
            suggestedSubstitutions = mapOf(
                "chicken" to listOf(
                    IngredientSubstitution(
                        originalIngredient = "chicken",
                        substituteIngredient = "tofu",
                        substitutionRatio = "1:1",
                        nutritionalImpact = NutritionalImpact(0.0, 0.0, 0.0, 0.0),
                        confidenceLevel = 0.8f,
                        notes = "Substitute chicken with tofu for vegetarian compatibility"
                    )
                )
            )
        )
        
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(testUserId) } returns flowOf(testDietaryProfile)
        coEvery { dietaryFilteringService.validateRecipeImport(testRecipe, testDietaryProfile) } returns Result.success(expectedValidation)
        
        // When
        val result = useCase.validateRecipeImport(testUserId, testRecipe)
        
        // Then
        assertTrue(result.isSuccess)
        val validation = result.getOrNull()
        assertNotNull(validation)
        assertFalse(validation!!.compatibility.isCompatible)
        assertEquals(1, validation.compatibility.violations.size)
        assertEquals(1, validation.suggestedSubstitutions.size)
        assertTrue(validation.canImport)
    }
    
    @Test
    fun `highlightShoppingListRestrictions should return highlighted list successfully`() = runTest {
        // Given
        val testShoppingList = createTestShoppingListWithItems()
        val testDietaryProfile = createTestDietaryProfile()
        val expectedHighlightedList = HighlightedShoppingList(
            originalList = testShoppingList,
            highlightedItems = testShoppingList.items.map { item ->
                HighlightedShoppingItem(
                    item = item,
                    restrictions = if (item.name.contains("chicken")) {
                        listOf(
                            DietaryViolation(
                                type = ViolationType.DIETARY_RESTRICTION,
                                restriction = DietaryRestrictionType.VEGETARIAN,
                                allergen = null,
                                severity = RestrictionSeverity.STRICT,
                                description = "Contains meat",
                                affectedIngredients = listOf("chicken")
                            )
                        )
                    } else emptyList(),
                    warnings = emptyList(),
                    highlightLevel = if (item.name.contains("chicken")) HighlightLevel.HIGH else HighlightLevel.NONE,
                    suggestedAlternatives = if (item.name.contains("chicken")) listOf("tofu", "tempeh") else emptyList()
                )
            },
            restrictedItemsCount = 1,
            warningItemsCount = 0
        )
        
        coEvery { shoppingListRepository.getShoppingListWithItems(testShoppingListId) } returns testShoppingList
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(testUserId) } returns flowOf(testDietaryProfile)
        coEvery { dietaryFilteringService.highlightRestrictedIngredients(testShoppingList, testDietaryProfile) } returns Result.success(expectedHighlightedList)
        
        // When
        val result = useCase.highlightShoppingListRestrictions(testUserId, testShoppingListId)
        
        // Then
        assertTrue(result.isSuccess)
        val highlightedList = result.getOrNull()
        assertNotNull(highlightedList)
        assertEquals(1, highlightedList!!.restrictedItemsCount)
        assertEquals(0, highlightedList.warningItemsCount)
        assertTrue(highlightedList.highlightedItems.any { it.highlightLevel == HighlightLevel.HIGH })
    }
    
    @Test
    fun `generateIngredientSubstitutions should return substitutions successfully`() = runTest {
        // Given
        val testRecipe = createTestRecipe("1", "Chicken Curry")
        val testDietaryProfile = createTestDietaryProfile()
        val expectedSubstitutions = mapOf(
            "chicken" to listOf(
                IngredientSubstitution(
                    originalIngredient = "chicken",
                    substituteIngredient = "tofu",
                    substitutionRatio = "1:1",
                    nutritionalImpact = NutritionalImpact(0.0, 0.0, 0.0, 0.0),
                    confidenceLevel = 0.8f,
                    notes = "Substitute chicken with tofu for vegetarian compatibility"
                )
            )
        )
        
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(testUserId) } returns flowOf(testDietaryProfile)
        coEvery { dietaryFilteringService.generateIngredientSubstitutions(testRecipe, testDietaryProfile) } returns expectedSubstitutions
        
        // When
        val result = useCase.generateIngredientSubstitutions(testUserId, testRecipe)
        
        // Then
        assertTrue(result.isSuccess)
        val substitutions = result.getOrNull()
        assertNotNull(substitutions)
        assertEquals(1, substitutions!!.size)
        assertTrue(substitutions.containsKey("chicken"))
        assertEquals("tofu", substitutions["chicken"]!![0].substituteIngredient)
    }
    
    @Test
    fun `getCompatibleRecipesForMealPlanning should return limited compatible recipes`() = runTest {
        // Given
        val testRecipes = listOf(
            createTestRecipe("1", "Vegetarian Pasta"),
            createTestRecipe("2", "Vegan Salad"),
            createTestRecipe("3", "Chicken Curry")
        )
        val testDietaryProfile = createTestDietaryProfile()
        val filteredRecipes = FilteredRecipes(
            compatibleRecipes = testRecipes.take(2),
            incompatibleRecipes = listOf(
                IncompatibleRecipe(
                    recipe = testRecipes[2],
                    compatibility = DietaryCompatibility(false, emptyList(), emptyList(), 0.3f),
                    failedCriteria = listOf("Violates vegetarian restriction")
                )
            ),
            totalFiltered = 3,
            compatibilityStats = CompatibilityStats(3, 2, 1, 0.67f)
        )
        
        coEvery { recipeRepository.getAllRecipes(testUserId) } returns flowOf(testRecipes)
        coEvery { dietaryRestrictionsRepository.getUserDietaryProfile(testUserId) } returns flowOf(testDietaryProfile)
        coEvery { dietaryFilteringService.filterRecipes(testRecipes, testDietaryProfile, any()) } returns Result.success(filteredRecipes)
        
        // When
        val result = useCase.getCompatibleRecipesForMealPlanning(testUserId, MealType.LUNCH, maxResults = 2)
        
        // Then
        assertTrue(result.isSuccess)
        val compatibleRecipes = result.getOrNull()
        assertNotNull(compatibleRecipes)
        assertEquals(2, compatibleRecipes!!.size)
        assertTrue(compatibleRecipes.all { it.name != "Chicken Curry" })
    }
    
    // Helper methods for creating test data
    
    private fun createTestRecipe(id: String, name: String): Recipe {
        return Recipe(
            id = id,
            name = name,
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            instructions = "[]",
            nutritionInfo = "{}",
            sourceType = RecipeSource.MANUAL
        )
    }
    
    private fun createTestDietaryProfile(): DietaryProfile {
        return DietaryProfile(
            restrictions = listOf(
                UserDietaryRestriction(
                    id = "1",
                    userId = testUserId,
                    restrictionType = DietaryRestrictionType.VEGETARIAN,
                    severity = RestrictionSeverity.STRICT,
                    isActive = true
                )
            ),
            allergies = emptyList(),
            preferences = listOf(
                UserFoodPreference(
                    id = "1",
                    userId = testUserId,
                    preferenceType = FoodPreferenceType.INGREDIENT,
                    item = "pasta",
                    preference = PreferenceLevel.LOVE
                )
            )
        )
    }
    
    private fun createTestWeeklyMealPlan(): WeeklyMealPlan {
        val recipes = listOf(
            createTestRecipe("1", "Vegetarian Pasta"),
            createTestRecipe("2", "Chicken Curry")
        )
        
        val plannedMeals = listOf(
            PlannedMeal(
                id = "1",
                mealPlanId = testMealPlanId,
                userId = testUserId,
                date = "2024-01-01",
                mealType = MealType.LUNCH,
                recipeId = "1"
            ),
            PlannedMeal(
                id = "2",
                mealPlanId = testMealPlanId,
                userId = testUserId,
                date = "2024-01-01",
                mealType = MealType.DINNER,
                recipeId = "2"
            )
        )
        
        return WeeklyMealPlan(
            mealPlan = MealPlan(
                id = testMealPlanId,
                userId = testUserId,
                weekStartDate = "2024-01-01",
                weekEndDate = "2024-01-07"
            ),
            plannedMeals = plannedMeals,
            plannedSupplements = emptyList(),
            recipes = recipes
        )
    }
    
    private fun createTestShoppingListWithItems(): ShoppingListWithItems {
        val items = listOf(
            ShoppingListItem(
                id = "1",
                shoppingListId = testShoppingListId,
                name = "chicken breast",
                quantity = 1.0,
                unit = "lb"
            ),
            ShoppingListItem(
                id = "2",
                shoppingListId = testShoppingListId,
                name = "pasta",
                quantity = 1.0,
                unit = "box"
            )
        )
        
        return ShoppingListWithItems(
            shoppingList = ShoppingList(
                id = testShoppingListId,
                userId = testUserId,
                name = "Test Shopping List"
            ),
            items = items
        )
    }
}