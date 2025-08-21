package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.MealDao
import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.model.MealStatus
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.data.model.NutritionInfo
import com.beaconledger.welltrack.data.remote.SupabaseClient
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class MealRepositoryImplTest {

    @Mock
    private lateinit var mealDao: MealDao

    @Mock
    private lateinit var supabaseClient: SupabaseClient

    private lateinit var repository: MealRepositoryImpl

    private val testMeal = Meal(
        id = "meal1",
        userId = "user1",
        recipeId = "recipe1",
        timestamp = LocalDateTime.now(),
        mealType = MealType.BREAKFAST,
        portions = 1.0f,
        nutritionInfo = NutritionInfo(
            calories = 300.0,
            protein = 20.0,
            carbs = 30.0,
            fat = 10.0,
            fiber = 5.0,
            sugar = 8.0,
            sodium = 400.0
        ),
        status = MealStatus.PLANNED,
        notes = "Test meal"
    )

    @Before
    fun setup() {
        repository = MealRepositoryImpl(mealDao, supabaseClient)
    }

    @Test
    fun `logMeal saves meal to local database`() = runTest {
        // When
        val result = repository.logMeal(testMeal)

        // Then
        assertTrue(result.isSuccess)
        verify(mealDao).insertMeal(testMeal)
    }

    @Test
    fun `getMealsForDate returns meals from local database`() = runTest {
        // Given
        val date = LocalDate.now()
        val expectedMeals = listOf(testMeal)
        whenever(mealDao.getMealsForUserAndDate("user1", date)).thenReturn(flowOf(expectedMeals))

        // When
        val result = repository.getMealsForDate("user1", date)

        // Then
        result.collect { meals ->
            assertEquals(expectedMeals, meals)
        }
    }

    @Test
    fun `updateMealStatus updates meal status in database`() = runTest {
        // When
        val result = repository.updateMealStatus("meal1", MealStatus.EATEN)

        // Then
        assertTrue(result.isSuccess)
        verify(mealDao).updateMealStatus("meal1", MealStatus.EATEN)
    }

    @Test
    fun `getMealById returns meal from database`() = runTest {
        // Given
        whenever(mealDao.getMealById("meal1")).thenReturn(testMeal)

        // When
        val result = repository.getMealById("meal1")

        // Then
        assertEquals(testMeal, result)
    }

    @Test
    fun `deleteMeal removes meal from database`() = runTest {
        // When
        val result = repository.deleteMeal("meal1")

        // Then
        assertTrue(result.isSuccess)
        verify(mealDao).deleteMeal("meal1")
    }
}