package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.domain.repository.CostBudgetRepository
import com.beaconledger.welltrack.domain.usecase.ProfileContextUseCase
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class CostBudgetUseCaseTest {

    private lateinit var costBudgetUseCase: CostBudgetUseCase
    private val mockCostBudgetRepository = mockk<CostBudgetRepository>()
    private val mockProfileContextUseCase = mockk<ProfileContextUseCase>()

    private val testUserId = "test-user-id"
    private val testUser = User(
        id = testUserId,
        email = "test@example.com",
        name = "Test User",
        profilePhoto = null,
        age = 30
    )

    @Before
    fun setup() {
        costBudgetUseCase = CostBudgetUseCase(
            costBudgetRepository = mockCostBudgetRepository,
            profileContextUseCase = mockProfileContextUseCase
        )
    }

    @Test
    fun `calculateAndSaveMealCost should calculate and save meal cost successfully`() = runTest {
        // Given
        val recipe = createTestRecipe()
        val mealId = "test-meal-id"
        val servings = 2
        val mealType = MealType.LUNCH
        val date = LocalDate.now()

        val expectedMealCost = createTestMealCost(testUserId, mealId, recipe.name, mealType, date)

        coEvery { 
            mockCostBudgetRepository.calculateMealCost(recipe, servings) 
        } returns Result.success(expectedMealCost)
        
        coEvery { 
            mockCostBudgetRepository.saveMealCost(any()) 
        } returns Result.success(Unit)
        
        coEvery { 
            mockCostBudgetRepository.updateBudgetTracking(testUserId, BudgetPeriod.WEEKLY) 
        } returns Result.success(createTestBudgetTracking(BudgetPeriod.WEEKLY))
        
        coEvery { 
            mockCostBudgetRepository.updateBudgetTracking(testUserId, BudgetPeriod.MONTHLY) 
        } returns Result.success(createTestBudgetTracking(BudgetPeriod.MONTHLY))

        // When
        val result = costBudgetUseCase.calculateAndSaveMealCost(
            userId = testUserId,
            mealId = mealId,
            recipe = recipe,
            servings = servings,
            mealType = mealType,
            date = date
        )

        // Then
        assertTrue(result.isSuccess)
        val mealCost = result.getOrNull()
        assertNotNull(mealCost)
        assertEquals(testUserId, mealCost?.userId)
        assertEquals(mealId, mealCost?.mealId)
        assertEquals(recipe.name, mealCost?.recipeName)
        assertEquals(mealType, mealCost?.mealType)
        assertEquals(date, mealCost?.date)

        coVerify { mockCostBudgetRepository.calculateMealCost(recipe, servings) }
        coVerify { mockCostBudgetRepository.saveMealCost(any()) }
        coVerify { mockCostBudgetRepository.updateBudgetTracking(testUserId, BudgetPeriod.WEEKLY) }
        coVerify { mockCostBudgetRepository.updateBudgetTracking(testUserId, BudgetPeriod.MONTHLY) }
    }

    @Test
    fun `createBudgetSettings should create new budget settings successfully`() = runTest {
        // Given
        val weeklyBudget = 100.0
        val monthlyBudget = 400.0
        val alertThreshold = 0.8
        val enableAlerts = true
        val currency = "USD"

        coEvery { 
            mockCostBudgetRepository.saveBudgetSettings(any()) 
        } returns Result.success(Unit)

        // When
        val result = costBudgetUseCase.createBudgetSettings(
            userId = testUserId,
            weeklyBudget = weeklyBudget,
            monthlyBudget = monthlyBudget,
            alertThreshold = alertThreshold,
            enableAlerts = enableAlerts,
            currency = currency
        )

        // Then
        assertTrue(result.isSuccess)
        val settings = result.getOrNull()
        assertNotNull(settings)
        assertEquals(testUserId, settings?.userId)
        assertEquals(weeklyBudget, settings?.weeklyBudget)
        assertEquals(monthlyBudget, settings?.monthlyBudget)
        assertEquals(alertThreshold, settings?.alertThreshold)
        assertEquals(enableAlerts, settings?.enableAlerts)
        assertEquals(currency, settings?.currency)

        coVerify { mockCostBudgetRepository.saveBudgetSettings(any()) }
    }

    @Test
    fun `updateBudgetSettings should update existing settings successfully`() = runTest {
        // Given
        val existingSettings = createTestBudgetSettings()
        val newWeeklyBudget = 150.0
        val newMonthlyBudget = 600.0

        coEvery { 
            mockCostBudgetRepository.getBudgetSettings(testUserId) 
        } returns existingSettings
        
        coEvery { 
            mockCostBudgetRepository.updateBudgetSettings(any()) 
        } returns Result.success(Unit)

        // When
        val result = costBudgetUseCase.updateBudgetSettings(
            userId = testUserId,
            weeklyBudget = newWeeklyBudget,
            monthlyBudget = newMonthlyBudget
        )

        // Then
        assertTrue(result.isSuccess)
        val updatedSettings = result.getOrNull()
        assertNotNull(updatedSettings)
        assertEquals(newWeeklyBudget, updatedSettings?.weeklyBudget)
        assertEquals(newMonthlyBudget, updatedSettings?.monthlyBudget)
        assertEquals(existingSettings.alertThreshold, updatedSettings?.alertThreshold)
        assertEquals(existingSettings.enableAlerts, updatedSettings?.enableAlerts)
        assertEquals(existingSettings.currency, updatedSettings?.currency)

        coVerify { mockCostBudgetRepository.getBudgetSettings(testUserId) }
        coVerify { mockCostBudgetRepository.updateBudgetSettings(any()) }
    }

    @Test
    fun `getCurrentBudgetStatus should return budget status with tracking data`() = runTest {
        // Given
        val budgetSettings = createTestBudgetSettings()
        val weeklyTracking = createTestBudgetTracking(BudgetPeriod.WEEKLY)
        val monthlyTracking = createTestBudgetTracking(BudgetPeriod.MONTHLY)

        coEvery { 
            mockCostBudgetRepository.getBudgetSettings(testUserId) 
        } returns budgetSettings
        
        coEvery { 
            mockCostBudgetRepository.getCurrentBudgetTracking(testUserId, BudgetPeriod.WEEKLY) 
        } returns weeklyTracking
        
        coEvery { 
            mockCostBudgetRepository.getCurrentBudgetTracking(testUserId, BudgetPeriod.MONTHLY) 
        } returns monthlyTracking

        // When
        val budgetStatus = costBudgetUseCase.getCurrentBudgetStatus(testUserId)

        // Then
        assertTrue(budgetStatus.hasSettings)
        assertEquals(budgetSettings.weeklyBudget, budgetStatus.weeklyBudget)
        assertEquals(budgetSettings.monthlyBudget, budgetStatus.monthlyBudget)
        assertEquals(weeklyTracking, budgetStatus.weeklyTracking)
        assertEquals(monthlyTracking, budgetStatus.monthlyTracking)
        assertEquals(budgetSettings.currency, budgetStatus.currency)

        coVerify { mockCostBudgetRepository.getBudgetSettings(testUserId) }
        coVerify { mockCostBudgetRepository.getCurrentBudgetTracking(testUserId, BudgetPeriod.WEEKLY) }
        coVerify { mockCostBudgetRepository.getCurrentBudgetTracking(testUserId, BudgetPeriod.MONTHLY) }
    }

    @Test
    fun `getCostAnalysis should return comprehensive cost analysis`() = runTest {
        // Given
        val days = 30
        val totalSpent = 250.0
        val averageCostPerServing = 8.5
        val cheapestRecipes = listOf(
            RecipeCostSummary("Cheap Recipe", 5.0, 3),
            RecipeCostSummary("Budget Meal", 6.0, 2)
        )
        val expensiveRecipes = listOf(
            RecipeCostSummary("Premium Recipe", 15.0, 1),
            RecipeCostSummary("Gourmet Meal", 12.0, 2)
        )
        val costByMealType = listOf(
            MealTypeCostSummary(MealType.BREAKFAST, 6.0, 10),
            MealTypeCostSummary(MealType.LUNCH, 9.0, 8),
            MealTypeCostSummary(MealType.DINNER, 12.0, 7)
        )
        val dailySpending = listOf(
            DailySpending(LocalDate.now().minusDays(1), 25.0),
            DailySpending(LocalDate.now(), 30.0)
        )

        coEvery { 
            mockCostBudgetRepository.getTotalSpentForPeriod(testUserId, any(), any()) 
        } returns totalSpent
        
        coEvery { 
            mockCostBudgetRepository.getAverageCostPerServing(testUserId, any(), any()) 
        } returns averageCostPerServing
        
        coEvery { 
            mockCostBudgetRepository.getCheapestRecipes(testUserId, any(), any()) 
        } returns cheapestRecipes
        
        coEvery { 
            mockCostBudgetRepository.getMostExpensiveRecipes(testUserId, any(), any()) 
        } returns expensiveRecipes
        
        coEvery { 
            mockCostBudgetRepository.getCostByMealType(testUserId, any(), any()) 
        } returns costByMealType
        
        coEvery { 
            mockCostBudgetRepository.getDailySpendingTrend(testUserId, any(), any()) 
        } returns dailySpending

        // When
        val costAnalysis = costBudgetUseCase.getCostAnalysis(testUserId, days)

        // Then
        assertEquals(totalSpent, costAnalysis.totalSpent, 0.01)
        assertEquals(averageCostPerServing, costAnalysis.averageCostPerServing, 0.01)
        assertEquals(totalSpent / days, costAnalysis.averageDailySpending, 0.01)
        assertEquals(cheapestRecipes.take(5), costAnalysis.cheapestRecipes)
        assertEquals(expensiveRecipes.take(5), costAnalysis.mostExpensiveRecipes)
        assertEquals(costByMealType, costAnalysis.costByMealType)
        assertEquals(dailySpending, costAnalysis.dailySpendingTrend)
        assertEquals(days, costAnalysis.periodDays)
    }

    @Test
    fun `estimateShoppingListCost should calculate total cost with breakdown`() = runTest {
        // Given
        val shoppingList = listOf(
            ShoppingListItem(
                id = "item1",
                shoppingListId = "list1",
                name = "chicken breast",
                quantity = 1.0,
                unit = "kg"
            ),
            ShoppingListItem(
                id = "item2",
                shoppingListId = "list1",
                name = "rice",
                quantity = 2.0,
                unit = "kg"
            )
        )

        val totalCost = 15.98
        val chickenPrice = IngredientPrice(
            id = "price1",
            ingredientName = "chicken breast",
            price = 8.99,
            unit = "kg",
            lastUpdated = LocalDateTime.now()
        )
        val ricePrice = IngredientPrice(
            id = "price2",
            ingredientName = "rice",
            price = 2.49,
            unit = "kg",
            lastUpdated = LocalDateTime.now()
        )

        coEvery { 
            mockCostBudgetRepository.estimateShoppingListCost(shoppingList) 
        } returns Result.success(totalCost)
        
        coEvery { 
            mockCostBudgetRepository.getIngredientPrice("chicken breast") 
        } returns chickenPrice
        
        coEvery { 
            mockCostBudgetRepository.getIngredientPrice("rice") 
        } returns ricePrice

        // When
        val result = costBudgetUseCase.estimateShoppingListCost(shoppingList)

        // Then
        assertTrue(result.isSuccess)
        val estimate = result.getOrNull()
        assertNotNull(estimate)
        assertEquals(totalCost, estimate?.totalCost)
        assertEquals(2, estimate?.itemBreakdown?.size)
        assertFalse(estimate?.hasEstimatedPrices ?: true)

        coVerify { mockCostBudgetRepository.estimateShoppingListCost(shoppingList) }
    }

    @Test
    fun `updateIngredientPrice should save new ingredient price successfully`() = runTest {
        // Given
        val ingredientName = "tomatoes"
        val price = 4.99
        val unit = "kg"
        val storeId = "store123"
        val storeName = "Local Market"

        coEvery { 
            mockCostBudgetRepository.saveIngredientPrice(any()) 
        } returns Result.success(Unit)

        // When
        val result = costBudgetUseCase.updateIngredientPrice(
            ingredientName = ingredientName,
            price = price,
            unit = unit,
            storeId = storeId,
            storeName = storeName
        )

        // Then
        assertTrue(result.isSuccess)

        coVerify { 
            mockCostBudgetRepository.saveIngredientPrice(
                match { ingredientPrice ->
                    ingredientPrice.ingredientName == ingredientName.lowercase() &&
                    ingredientPrice.price == price &&
                    ingredientPrice.unit == unit &&
                    ingredientPrice.storeId == storeId &&
                    ingredientPrice.storeName == storeName &&
                    !ingredientPrice.isEstimated
                }
            ) 
        }
    }

    // Helper methods for creating test data
    private fun createTestRecipe(): Recipe {
        return Recipe(
            id = "recipe-1",
            name = "Test Recipe",
            prepTime = 15,
            cookTime = 30,
            servings = 4,
            instructions = "[]",
            nutritionInfo = "{}",
            sourceType = RecipeSource.MANUAL
        )
    }

    private fun createTestMealCost(
        userId: String,
        mealId: String,
        recipeName: String,
        mealType: MealType,
        date: LocalDate
    ): MealCost {
        return MealCost(
            id = "meal-cost-1",
            userId = userId,
            mealId = mealId,
            recipeName = recipeName,
            totalCost = 12.50,
            costPerServing = 6.25,
            servings = 2,
            date = date,
            mealType = mealType,
            ingredientCosts = listOf(
                IngredientCostBreakdown(
                    ingredientName = "chicken breast",
                    quantity = 0.5,
                    unit = "kg",
                    unitPrice = 8.99,
                    totalCost = 4.50
                ),
                IngredientCostBreakdown(
                    ingredientName = "rice",
                    quantity = 0.3,
                    unit = "kg",
                    unitPrice = 2.49,
                    totalCost = 0.75
                )
            )
        )
    }

    private fun createTestBudgetSettings(): BudgetSettings {
        return BudgetSettings(
            id = "settings-1",
            userId = testUserId,
            weeklyBudget = 100.0,
            monthlyBudget = 400.0,
            alertThreshold = 0.8,
            enableAlerts = true,
            currency = "USD",
            lastUpdated = LocalDateTime.now()
        )
    }

    private fun createTestBudgetTracking(period: BudgetPeriod): BudgetTracking {
        val now = LocalDate.now()
        return BudgetTracking(
            id = "tracking-1",
            userId = testUserId,
            period = period,
            periodStart = now.minusDays(7),
            periodEnd = now,
            budgetLimit = if (period == BudgetPeriod.WEEKLY) 100.0 else 400.0,
            totalSpent = if (period == BudgetPeriod.WEEKLY) 75.0 else 300.0,
            remainingBudget = if (period == BudgetPeriod.WEEKLY) 25.0 else 100.0,
            mealCount = 10,
            averageCostPerMeal = if (period == BudgetPeriod.WEEKLY) 7.5 else 30.0,
            lastUpdated = LocalDateTime.now()
        )
    }
}