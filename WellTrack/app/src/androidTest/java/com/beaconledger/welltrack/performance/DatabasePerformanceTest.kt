package com.beaconledger.welltrack.performance

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.MealDao
import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.model.MealStatus
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.data.model.NutritionInfo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DatabasePerformanceTest {

    private lateinit var database: WellTrackDatabase
    private lateinit var mealDao: MealDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WellTrackDatabase::class.java
        ).allowMainThreadQueries().build()

        mealDao = database.mealDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun bulkInsertPerformance_shouldCompleteWithinTimeLimit() = runTest {
        val meals = generateTestMeals(1000)
        
        val insertTime = measureTimeMillis {
            meals.forEach { meal ->
                mealDao.insertMeal(meal)
            }
        }

        // Should complete bulk insert within 5 seconds
        assertTrue(insertTime < 5000, "Bulk insert took $insertTime ms, expected < 5000 ms")
    }

    @Test
    fun queryPerformance_shouldRetrieveDataQuickly() = runTest {
        // Insert test data
        val meals = generateTestMeals(500)
        meals.forEach { meal ->
            mealDao.insertMeal(meal)
        }

        val queryTime = measureTimeMillis {
            val result = mealDao.getMealsForUserAndDate("user1", LocalDate.now()).first()
            assertTrue(result.isNotEmpty())
        }

        // Query should complete within 1 second
        assertTrue(queryTime < 1000, "Query took $queryTime ms, expected < 1000 ms")
    }

    @Test
    fun complexQueryPerformance_shouldHandleLargeDataset() = runTest {
        // Insert large dataset
        val meals = generateTestMeals(2000)
        meals.forEach { meal ->
            mealDao.insertMeal(meal)
        }

        val queryTime = measureTimeMillis {
            val result = mealDao.getMealsByType("user1", MealType.BREAKFAST).first()
            assertTrue(result.isNotEmpty())
        }

        // Complex query should complete within 2 seconds
        assertTrue(queryTime < 2000, "Complex query took $queryTime ms, expected < 2000 ms")
    }

    @Test
    fun updatePerformance_shouldUpdateQuickly() = runTest {
        // Insert test meals
        val meals = generateTestMeals(100)
        meals.forEach { meal ->
            mealDao.insertMeal(meal)
        }

        val updateTime = measureTimeMillis {
            meals.forEach { meal ->
                mealDao.updateMealStatus(meal.id, MealStatus.EATEN)
            }
        }

        // Updates should complete within 2 seconds
        assertTrue(updateTime < 2000, "Updates took $updateTime ms, expected < 2000 ms")
    }

    @Test
    fun deletePerformance_shouldDeleteQuickly() = runTest {
        // Insert test meals
        val meals = generateTestMeals(100)
        meals.forEach { meal ->
            mealDao.insertMeal(meal)
        }

        val deleteTime = measureTimeMillis {
            meals.forEach { meal ->
                mealDao.deleteMeal(meal.id)
            }
        }

        // Deletes should complete within 1 second
        assertTrue(deleteTime < 1000, "Deletes took $deleteTime ms, expected < 1000 ms")
    }

    private fun generateTestMeals(count: Int): List<Meal> {
        return (1..count).map { index ->
            Meal(
                id = "meal$index",
                userId = "user1",
                recipeId = "recipe$index",
                timestamp = LocalDateTime.now().minusDays((index % 30).toLong()),
                mealType = MealType.values()[index % MealType.values().size],
                portions = 1.0f,
                nutritionInfo = NutritionInfo(
                    calories = 300.0 + (index % 200),
                    protein = 20.0 + (index % 10),
                    carbs = 30.0 + (index % 20),
                    fat = 10.0 + (index % 15),
                    fiber = 5.0 + (index % 5),
                    sugar = 8.0 + (index % 10),
                    sodium = 400.0 + (index % 100)
                ),
                status = MealStatus.values()[index % MealStatus.values().size],
                notes = "Test meal $index"
            )
        }
    }
}