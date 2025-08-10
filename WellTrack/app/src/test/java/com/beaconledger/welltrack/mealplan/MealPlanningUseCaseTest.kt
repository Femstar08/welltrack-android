package com.beaconledger.welltrack.mealplan

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MealPlanRepository
import com.beaconledger.welltrack.domain.usecase.MealPlanningUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class MealPlanningUseCaseTest {

    private lateinit var mealPlanRepository: MealPlanRepository
    private lateinit var mealPlanningUseCase: MealPlanningUseCase

    @Before
    fun setup() {
        mealPlanRepository = mockk()
        mealPlanningUseCase = MealPlanningUseCase(mealPlanRepository)
    }

    @Test
    fun `generateWeeklyMealPlan should create meal plan with preferences`() = runTest {
        // Given
        val userId = "test-user"
        val weekStartDate = LocalDate.of(2024, 1, 1)
        val preferences = MealPlanPreferences(
            targetCalories = 2000,
            targetProtein = 150,
            cookingTimePreference = CookingTimePreference.MODERATE
        )

        val mockGenerationResult = MealPlanGenerationResult(
            success = true,
            mealPlan = WeeklyMealPlan(
                mealPlan = MealPlan(
                    id = "test-plan",
                    userId = userId,
                    weekStartDate = weekStartDate.toString(),
                    weekEndDate = weekStartDate.plusDays(6).toString(),
                    isActive = true
                ),
                plannedMeals = emptyList(),
                plannedSupplements = emptyList()
            )
        )

        coEvery { 
            mealPlanRepository.getMealPlanPreferences(userId) 
        } returns Result.success(null)

        coEvery { 
            mealPlanRepository.generateWeeklyMealPlan(any()) 
        } returns Result.success(mockGenerationResult)

        // When
        val result = mealPlanningUseCase.generateWeeklyMealPlan(
            userId = userId,
            weekStartDate = weekStartDate,
            preferences = preferences
        )

        // Then
        assertTrue(result.isSuccess)
        val generationResult = result.getOrNull()
        assertNotNull(generationResult)
        assertTrue(generationResult!!.success)
        assertNotNull(generationResult.mealPlan)

        coVerify { 
            mealPlanRepository.generateWeeklyMealPlan(
                match { request ->
                    request.userId == userId &&
                    request.weekStartDate == weekStartDate &&
                    request.preferences.targetCalories == 2000 &&
                    request.preferences.targetProtein == 150
                }
            )
        }
    }

    @Test
    fun `generateWeeklyMealPlan should use saved preferences when none provided`() = runTest {
        // Given
        val userId = "test-user"
        val weekStartDate = LocalDate.of(2024, 1, 1)
        val savedPreferences = MealPlanPreferences(
            targetCalories = 1800,
            cookingTimePreference = CookingTimePreference.QUICK
        )

        val mockGenerationResult = MealPlanGenerationResult(
            success = true,
            mealPlan = WeeklyMealPlan(
                mealPlan = MealPlan(
                    id = "test-plan",
                    userId = userId,
                    weekStartDate = weekStartDate.toString(),
                    weekEndDate = weekStartDate.plusDays(6).toString(),
                    isActive = true
                ),
                plannedMeals = emptyList(),
                plannedSupplements = emptyList()
            )
        )

        coEvery { 
            mealPlanRepository.getMealPlanPreferences(userId) 
        } returns Result.success(savedPreferences)

        coEvery { 
            mealPlanRepository.generateWeeklyMealPlan(any()) 
        } returns Result.success(mockGenerationResult)

        // When
        val result = mealPlanningUseCase.generateWeeklyMealPlan(
            userId = userId,
            weekStartDate = weekStartDate,
            preferences = null
        )

        // Then
        assertTrue(result.isSuccess)

        coVerify { 
            mealPlanRepository.generateWeeklyMealPlan(
                match { request ->
                    request.preferences.targetCalories == 1800 &&
                    request.preferences.cookingTimePreference == CookingTimePreference.QUICK
                }
            )
        }
    }

    @Test
    fun `updatePlannedMeal should update meal with new recipe`() = runTest {
        // Given
        val plannedMealId = "meal-123"
        val newRecipeId = "recipe-456"
        val existingMeal = PlannedMeal(
            id = plannedMealId,
            mealPlanId = "plan-123",
            userId = "user-123",
            date = "2024-01-01",
            mealType = MealType.LUNCH,
            recipeId = "old-recipe",
            servings = 1
        )

        coEvery { 
            mealPlanRepository.getPlannedMealById(plannedMealId) 
        } returns Result.success(existingMeal)

        coEvery { 
            mealPlanRepository.updatePlannedMeal(any()) 
        } returns Result.success(Unit)

        // When
        val result = mealPlanningUseCase.updatePlannedMeal(
            plannedMealId = plannedMealId,
            newRecipeId = newRecipeId
        )

        // Then
        assertTrue(result.isSuccess)

        coVerify { 
            mealPlanRepository.updatePlannedMeal(
                match { meal ->
                    meal.id == plannedMealId &&
                    meal.recipeId == newRecipeId
                }
            )
        }
    }

    @Test
    fun `addCustomMeal should create new meal plan if none exists`() = runTest {
        // Given
        val userId = "test-user"
        val date = LocalDate.of(2024, 1, 1)
        val mealType = MealType.BREAKFAST
        val customMealName = "Oatmeal with berries"

        coEvery { 
            mealPlanRepository.getWeeklyMealPlan(userId, any()) 
        } returns Result.success(null)

        coEvery { 
            mealPlanRepository.createMealPlan(any()) 
        } returns Result.success("new-plan-id")

        coEvery { 
            mealPlanRepository.createPlannedMeal(any()) 
        } returns Result.success("new-meal-id")

        // When
        val result = mealPlanningUseCase.addCustomMeal(
            userId = userId,
            date = date,
            mealType = mealType,
            customMealName = customMealName
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals("new-meal-id", result.getOrNull())

        coVerify { mealPlanRepository.createMealPlan(any()) }
        coVerify { 
            mealPlanRepository.createPlannedMeal(
                match { meal ->
                    meal.userId == userId &&
                    meal.date == date.toString() &&
                    meal.mealType == mealType &&
                    meal.customMealName == customMealName
                }
            )
        }
    }

    @Test
    fun `markMealAsCompleted should update meal status`() = runTest {
        // Given
        val plannedMealId = "meal-123"

        coEvery { 
            mealPlanRepository.markMealAsCompleted(plannedMealId) 
        } returns Result.success(Unit)

        // When
        val result = mealPlanningUseCase.markMealAsCompleted(plannedMealId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mealPlanRepository.markMealAsCompleted(plannedMealId) }
    }

    @Test
    fun `markMealAsSkipped should update meal status`() = runTest {
        // Given
        val plannedMealId = "meal-123"

        coEvery { 
            mealPlanRepository.markMealAsSkipped(plannedMealId) 
        } returns Result.success(Unit)

        // When
        val result = mealPlanningUseCase.markMealAsSkipped(plannedMealId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mealPlanRepository.markMealAsSkipped(plannedMealId) }
    }

    @Test
    fun `getMealPlanAdherence should calculate correct percentage`() = runTest {
        // Given
        val userId = "test-user"
        val startDate = LocalDate.of(2024, 1, 1)
        val endDate = LocalDate.of(2024, 1, 7)
        val adherence = 0.75 // 75%

        coEvery { 
            mealPlanRepository.getMealPlanAdherence(userId, startDate, endDate) 
        } returns Result.success(adherence)

        // When
        val result = mealPlanningUseCase.getMealPlanAdherence(userId, startDate, endDate)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(0.75, result.getOrNull()!!, 0.001)
    }

    @Test
    fun `optimizeMealPrepSchedule should generate schedule with tasks`() = runTest {
        // Given
        val weeklyMealPlan = WeeklyMealPlan(
            mealPlan = MealPlan(
                id = "plan-123",
                userId = "user-123",
                weekStartDate = "2024-01-01",
                weekEndDate = "2024-01-07",
                isActive = true
            ),
            plannedMeals = emptyList(),
            plannedSupplements = emptyList(),
            recipes = listOf(
                Recipe(
                    id = "recipe-1",
                    name = "Grilled Chicken",
                    ingredients = emptyList(),
                    instructions = emptyList(),
                    nutritionInfo = NutritionInfo(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    prepTime = 15,
                    cookTime = 25,
                    servings = 4,
                    tags = listOf("grill", "protein"),
                    source = RecipeSource.MANUAL
                ),
                Recipe(
                    id = "recipe-2",
                    name = "Baked Salmon",
                    ingredients = emptyList(),
                    instructions = emptyList(),
                    nutritionInfo = NutritionInfo(0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    prepTime = 10,
                    cookTime = 20,
                    servings = 2,
                    tags = listOf("bake", "fish"),
                    source = RecipeSource.MANUAL
                )
            )
        )
        val mealPrepDays = listOf("Sunday")

        // When
        val result = mealPlanningUseCase.optimizeMealPrepSchedule(weeklyMealPlan, mealPrepDays)

        // Then
        assertTrue(result.isSuccess)
        val schedule = result.getOrNull()
        assertNotNull(schedule)
        assertTrue(schedule!!.prepTasks.isNotEmpty())
        assertTrue(schedule.totalEstimatedTime > 0)
    }
}