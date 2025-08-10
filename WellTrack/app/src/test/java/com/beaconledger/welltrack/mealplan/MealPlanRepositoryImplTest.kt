package com.beaconledger.welltrack.mealplan

import com.beaconledger.welltrack.data.database.dao.MealPlanDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.repository.MealPlanRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class MealPlanRepositoryImplTest {

    private lateinit var mealPlanDao: MealPlanDao
    private lateinit var recipeDao: RecipeDao
    private lateinit var repository: MealPlanRepositoryImpl

    @Before
    fun setup() {
        mealPlanDao = mockk()
        recipeDao = mockk()
        repository = MealPlanRepositoryImpl(mealPlanDao, recipeDao)
    }

    @Test
    fun `createMealPlan should insert meal plan and return id`() = runTest {
        // Given
        val mealPlan = MealPlan(
            id = "test-plan",
            userId = "test-user",
            weekStartDate = "2024-01-01",
            weekEndDate = "2024-01-07",
            isActive = true
        )

        coEvery { mealPlanDao.insertMealPlan(mealPlan) } returns Unit

        // When
        val result = repository.createMealPlan(mealPlan)

        // Then
        assertTrue(result.isSuccess)
        assertEquals("test-plan", result.getOrNull())
        coVerify { mealPlanDao.insertMealPlan(mealPlan) }
    }

    @Test
    fun `getMealPlanForDate should return meal plan for specific date`() = runTest {
        // Given
        val userId = "test-user"
        val date = LocalDate.of(2024, 1, 3)
        val expectedMealPlan = MealPlan(
            id = "test-plan",
            userId = userId,
            weekStartDate = "2024-01-01",
            weekEndDate = "2024-01-07",
            isActive = true
        )

        coEvery { 
            mealPlanDao.getMealPlanForDate(userId, "2024-01-03") 
        } returns expectedMealPlan

        // When
        val result = repository.getMealPlanForDate(userId, date)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMealPlan, result.getOrNull())
    }

    @Test
    fun `activateMealPlan should deactivate all plans and activate specified plan`() = runTest {
        // Given
        val mealPlanId = "test-plan"
        val userId = "test-user"
        val existingPlan = MealPlan(
            id = mealPlanId,
            userId = userId,
            weekStartDate = "2024-01-01",
            weekEndDate = "2024-01-07",
            isActive = false
        )

        coEvery { mealPlanDao.deactivateAllMealPlans(userId) } returns Unit
        coEvery { mealPlanDao.getMealPlanById(mealPlanId) } returns existingPlan
        coEvery { mealPlanDao.updateMealPlan(any()) } returns Unit

        // When
        val result = repository.activateMealPlan(mealPlanId, userId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mealPlanDao.deactivateAllMealPlans(userId) }
        coVerify { 
            mealPlanDao.updateMealPlan(
                match { plan -> 
                    plan.id == mealPlanId && plan.isActive 
                }
            ) 
        }
    }

    @Test
    fun `getPlannedMealsForWeek should return meals for date range`() = runTest {
        // Given
        val userId = "test-user"
        val weekStartDate = LocalDate.of(2024, 1, 1)
        val expectedMeals = listOf(
            PlannedMeal(
                id = "meal-1",
                mealPlanId = "plan-1",
                userId = userId,
                date = "2024-01-01",
                mealType = MealType.BREAKFAST
            ),
            PlannedMeal(
                id = "meal-2",
                mealPlanId = "plan-1",
                userId = userId,
                date = "2024-01-02",
                mealType = MealType.LUNCH
            )
        )

        coEvery { 
            mealPlanDao.getPlannedMealsForDateRange(userId, "2024-01-01", "2024-01-07") 
        } returns expectedMeals

        // When
        val result = repository.getPlannedMealsForWeek(userId, weekStartDate)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedMeals, result.getOrNull())
    }

    @Test
    fun `markMealAsCompleted should update meal status to completed`() = runTest {
        // Given
        val plannedMealId = "meal-123"

        coEvery { 
            mealPlanDao.updatePlannedMealStatus(plannedMealId, PlannedMealStatus.COMPLETED, any()) 
        } returns Unit

        // When
        val result = repository.markMealAsCompleted(plannedMealId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            mealPlanDao.updatePlannedMealStatus(
                plannedMealId, 
                PlannedMealStatus.COMPLETED, 
                any()
            ) 
        }
    }

    @Test
    fun `markMealAsSkipped should update meal status to skipped`() = runTest {
        // Given
        val plannedMealId = "meal-123"

        coEvery { 
            mealPlanDao.updatePlannedMealStatus(plannedMealId, PlannedMealStatus.SKIPPED, null) 
        } returns Unit

        // When
        val result = repository.markMealAsSkipped(plannedMealId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            mealPlanDao.updatePlannedMealStatus(
                plannedMealId, 
                PlannedMealStatus.SKIPPED, 
                null
            ) 
        }
    }

    @Test
    fun `generateWeeklyMealPlan should return error when no recipes available`() = runTest {
        // Given
        val request = MealPlanGenerationRequest(
            userId = "test-user",
            weekStartDate = LocalDate.of(2024, 1, 1),
            preferences = MealPlanPreferences()
        )

        coEvery { recipeDao.getRecipesByUserId("test-user") } returns emptyList()

        // When
        val result = repository.generateWeeklyMealPlan(request)

        // Then
        assertTrue(result.isSuccess)
        val generationResult = result.getOrNull()
        assertNotNull(generationResult)
        assertFalse(generationResult!!.success)
        assertEquals("No recipes available for meal plan generation", generationResult.error)
    }

    @Test
    fun `generateWeeklyMealPlan should create meal plan with available recipes`() = runTest {
        // Given
        val request = MealPlanGenerationRequest(
            userId = "test-user",
            weekStartDate = LocalDate.of(2024, 1, 1),
            preferences = MealPlanPreferences(
                cookingTimePreference = CookingTimePreference.MODERATE
            )
        )

        val availableRecipes = listOf(
            Recipe(
                id = "recipe-1",
                name = "Breakfast Recipe",
                ingredients = emptyList(),
                instructions = emptyList(),
                nutritionInfo = NutritionInfo(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                prepTime = 10,
                cookTime = 15,
                servings = 2,
                tags = listOf("breakfast"),
                source = RecipeSource.MANUAL
            ),
            Recipe(
                id = "recipe-2",
                name = "Lunch Recipe",
                ingredients = emptyList(),
                instructions = emptyList(),
                nutritionInfo = NutritionInfo(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                prepTime = 20,
                cookTime = 30,
                servings = 4,
                tags = listOf("lunch"),
                source = RecipeSource.MANUAL
            )
        )

        coEvery { recipeDao.getRecipesByUserId("test-user") } returns availableRecipes
        coEvery { mealPlanDao.createMealPlanWithMeals(any(), any(), any()) } returns Unit

        // When
        val result = repository.generateWeeklyMealPlan(request)

        // Then
        assertTrue(result.isSuccess)
        val generationResult = result.getOrNull()
        assertNotNull(generationResult)
        assertTrue(generationResult!!.success)
        assertNotNull(generationResult.mealPlan)
        
        coVerify { mealPlanDao.createMealPlanWithMeals(any(), any(), any()) }
    }

    @Test
    fun `getDailyMealPlan should return daily plan with meals and supplements`() = runTest {
        // Given
        val userId = "test-user"
        val date = LocalDate.of(2024, 1, 1)
        val plannedMeals = listOf(
            PlannedMeal(
                id = "meal-1",
                mealPlanId = "plan-1",
                userId = userId,
                date = "2024-01-01",
                mealType = MealType.BREAKFAST
            ),
            PlannedMeal(
                id = "meal-2",
                mealPlanId = "plan-1",
                userId = userId,
                date = "2024-01-01",
                mealType = MealType.LUNCH
            )
        )
        val plannedSupplements = listOf(
            PlannedSupplement(
                id = "supp-1",
                mealPlanId = "plan-1",
                userId = userId,
                date = "2024-01-01",
                supplementName = "Vitamin D",
                dosage = "1000 IU"
            )
        )

        coEvery { 
            mealPlanDao.getPlannedMealsForDate(userId, "2024-01-01") 
        } returns plannedMeals
        
        coEvery { 
            mealPlanDao.getPlannedSupplementsForDate(userId, "2024-01-01") 
        } returns plannedSupplements

        // When
        val result = repository.getDailyMealPlan(userId, date)

        // Then
        assertTrue(result.isSuccess)
        val dailyPlan = result.getOrNull()
        assertNotNull(dailyPlan)
        assertEquals(date, dailyPlan!!.date)
        assertNotNull(dailyPlan.breakfast)
        assertNotNull(dailyPlan.lunch)
        assertEquals(1, dailyPlan.supplements.size)
    }

    @Test
    fun `getMealPlanAdherence should calculate adherence percentage`() = runTest {
        // Given
        val userId = "test-user"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)

        coEvery { 
            mealPlanDao.getTotalPlannedMealsCount(userId, "2024-01-01", "2024-01-07") 
        } returns 21 // 3 meals per day for 7 days
        
        coEvery { 
            mealPlanDao.getCompletedMealsCount(userId, "2024-01-01", "2024-01-07") 
        } returns 15 // 15 completed meals

        // When
        val result = repository.getMealPlanAdherence(userId, startDate, endDate)

        // Then
        assertTrue(result.isSuccess)
        val adherence = result.getOrNull()
        assertNotNull(adherence)
        assertEquals(15.0 / 21.0, adherence!!, 0.001) // ~71.4%
    }
}