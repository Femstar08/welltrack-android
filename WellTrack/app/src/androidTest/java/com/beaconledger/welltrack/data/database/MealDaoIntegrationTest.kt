package com.beaconledger.welltrack.data.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@RunWith(AndroidJUnit4::class)
class MealDaoIntegrationTest {

    private lateinit var database: WellTrackDatabase
    private lateinit var mealDao: MealDao

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
    fun insertAndRetrieveMeal() = runTest {
        // Insert meal
        mealDao.insertMeal(testMeal)

        // Retrieve meal
        val retrievedMeal = mealDao.getMealById("meal1")

        // Verify
        assertNotNull(retrievedMeal)
        assertEquals(testMeal.id, retrievedMeal.id)
        assertEquals(testMeal.userId, retrievedMeal.userId)
        assertEquals(testMeal.mealType, retrievedMeal.mealType)
        assertEquals(testMeal.nutritionInfo.calories, retrievedMeal.nutritionInfo.calories)
    }

    @Test
    fun getMealsForUserAndDate() = runTest {
        // Insert meals
        val today = LocalDate.now()
        val meal1 = testMeal.copy(id = "meal1", timestamp = today.atTime(8, 0))
        val meal2 = testMeal.copy(id = "meal2", timestamp = today.atTime(12, 0))
        val meal3 = testMeal.copy(id = "meal3", timestamp = today.plusDays(1).atTime(8, 0))

        mealDao.insertMeal(meal1)
        mealDao.insertMeal(meal2)
        mealDao.insertMeal(meal3)

        // Retrieve meals for today
        val todayMeals = mealDao.getMealsForUserAndDate("user1", today).first()

        // Verify
        assertEquals(2, todayMeals.size)
        assertEquals("meal1", todayMeals[0].id)
        assertEquals("meal2", todayMeals[1].id)
    }

    @Test
    fun updateMealStatus() = runTest {
        // Insert meal
        mealDao.insertMeal(testMeal)

        // Update status
        mealDao.updateMealStatus("meal1", MealStatus.EATEN)

        // Verify
        val updatedMeal = mealDao.getMealById("meal1")
        assertNotNull(updatedMeal)
        assertEquals(MealStatus.EATEN, updatedMeal.status)
    }

    @Test
    fun deleteMeal() = runTest {
        // Insert meal
        mealDao.insertMeal(testMeal)

        // Verify insertion
        assertNotNull(mealDao.getMealById("meal1"))

        // Delete meal
        mealDao.deleteMeal("meal1")

        // Verify deletion
        assertNull(mealDao.getMealById("meal1"))
    }

    @Test
    fun getMealsByType() = runTest {
        // Insert meals of different types
        val breakfast = testMeal.copy(id = "breakfast", mealType = MealType.BREAKFAST)
        val lunch = testMeal.copy(id = "lunch", mealType = MealType.LUNCH)
        val dinner = testMeal.copy(id = "dinner", mealType = MealType.DINNER)

        mealDao.insertMeal(breakfast)
        mealDao.insertMeal(lunch)
        mealDao.insertMeal(dinner)

        // Retrieve breakfast meals
        val breakfastMeals = mealDao.getMealsByType("user1", MealType.BREAKFAST).first()

        // Verify
        assertEquals(1, breakfastMeals.size)
        assertEquals("breakfast", breakfastMeals[0].id)
        assertEquals(MealType.BREAKFAST, breakfastMeals[0].mealType)
    }
}