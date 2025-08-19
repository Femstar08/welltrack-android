package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.MealPrepRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class MealPrepUseCaseTest {

    private lateinit var repository: MealPrepRepository
    private lateinit var getMealPrepGuidanceUseCase: GetMealPrepGuidanceUseCase
    private lateinit var createLeftoverUseCase: CreateLeftoverUseCase
    private lateinit var getLeftoverSuggestionsUseCase: GetLeftoverSuggestionsUseCase

    @Before
    fun setup() {
        repository = mockk()
        getMealPrepGuidanceUseCase = GetMealPrepGuidanceUseCase(repository)
        createLeftoverUseCase = CreateLeftoverUseCase(repository)
        getLeftoverSuggestionsUseCase = GetLeftoverSuggestionsUseCase(repository)
    }

    @Test
    fun `getMealPrepGuidance returns success when repository succeeds`() = runTest {
        // Given
        val recipeId = "recipe123"
        val expectedGuidance = MealPrepGuidanceResponse(
            prepInstructions = listOf(
                PrepStep(1, "Prep vegetables", 10, listOf("knife", "cutting board"))
            ),
            cookingMethods = listOf(
                CookingMethod(
                    method = CookingMethodType.SAUTEING,
                    temperature = "Medium heat",
                    duration = 15,
                    instructions = "Heat oil and sauté vegetables"
                )
            ),
            timingGuidance = TimingGuidance(
                totalPrepTime = 30,
                activeTime = 20,
                passiveTime = 10,
                optimalSchedule = listOf(
                    ScheduleStep(1, "Prep", 0, 15),
                    ScheduleStep(2, "Cook", 15, 15)
                )
            ),
            storageRecommendations = listOf(
                ContainerType(
                    type = ContainerTypeEnum.GLASS_CONTAINER,
                    size = "Medium",
                    material = "Glass",
                    suitableFor = listOf("Hot foods")
                )
            ),
            refrigerationGuideline = RefrigerationGuideline(
                temperature = "32-40°F",
                maxDuration = 3,
                storageInstructions = "Store in airtight container",
                qualityIndicators = listOf("Fresh smell")
            ),
            freezingInstructions = FreezingInstruction(
                isFreezable = true,
                maxDuration = 30,
                freezingInstructions = "Cool completely before freezing",
                thawingInstructions = "Thaw in refrigerator"
            )
        )

        coEvery { repository.getMealPrepGuidance(recipeId) } returns Result.success(expectedGuidance)

        // When
        val result = getMealPrepGuidanceUseCase(recipeId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedGuidance, result.getOrNull())
    }

    @Test
    fun `createLeftover creates leftover with correct data`() = runTest {
        // Given
        val userId = "user123"
        val mealId = "meal123"
        val recipeId = "recipe123"
        val name = "Leftover Pasta"
        val quantity = 2.0
        val unit = "portions"
        val storageLocation = StorageLocation.REFRIGERATOR
        val containerType = "Glass Container"
        val nutritionInfo = NutritionInfo(calories = 300.0, protein = 15.0)
        val shelfLifeDays = 3
        val notes = "Delicious pasta"
        val expectedLeftoverId = "leftover123"

        coEvery { 
            repository.createLeftover(any()) 
        } returns Result.success(expectedLeftoverId)

        // When
        val result = createLeftoverUseCase(
            userId, mealId, recipeId, name, quantity, unit,
            storageLocation, containerType, nutritionInfo, shelfLifeDays, notes
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedLeftoverId, result.getOrNull())
    }

    @Test
    fun `getLeftoverSuggestions returns suggestions for given leftovers`() = runTest {
        // Given
        val leftoverIds = listOf("leftover1", "leftover2")
        val additionalIngredients = listOf("cheese", "herbs")
        val maxPrepTime = 20
        val expectedSuggestions = LeftoverSuggestionResponse(
            combinations = listOf(
                LeftoverCombination(
                    id = "combo1",
                    name = "Leftover Stir-fry",
                    description = "Quick stir-fry with leftovers",
                    leftoverIds = "[\"leftover1\", \"leftover2\"]",
                    reheatingInstructions = "[]",
                    additionalIngredients = "[]",
                    prepTime = 15,
                    servings = 2,
                    nutritionInfo = "{}"
                )
            ),
            quickIdeas = listOf("Make a sandwich", "Add to soup"),
            safetyReminders = listOf("Reheat to 165°F")
        )

        coEvery { 
            repository.getLeftoverSuggestions(any()) 
        } returns Result.success(expectedSuggestions)

        // When
        val result = getLeftoverSuggestionsUseCase(leftoverIds, additionalIngredients, maxPrepTime)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedSuggestions, result.getOrNull())
    }

    @Test
    fun `getMealPrepGuidance returns failure when repository fails`() = runTest {
        // Given
        val recipeId = "recipe123"
        val expectedError = Exception("Database error")

        coEvery { repository.getMealPrepGuidance(recipeId) } returns Result.failure(expectedError)

        // When
        val result = getMealPrepGuidanceUseCase(recipeId)

        // Then
        assertTrue(result.isFailure)
        assertEquals(expectedError, result.exceptionOrNull())
    }
}