package com.beaconledger.welltrack.utils

import com.beaconledger.welltrack.data.model.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * Utility class for creating test data and common test operations
 */
object TestUtils {

    /**
     * Creates a test user with default values
     */
    fun createTestUser(
        id: String = "test_user_${Random.nextInt()}",
        email: String = "test${Random.nextInt()}@example.com",
        name: String = "Test User"
    ): User {
        return User(
            id = id,
            email = email,
            name = name,
            profilePhoto = null,
            age = 30,
            fitnessGoals = listOf(FitnessGoal.WEIGHT_LOSS, FitnessGoal.MUSCLE_GAIN),
            dietaryRestrictions = listOf(DietaryRestriction.VEGETARIAN),
            preferences = UserPreferences(
                notificationsEnabled = true,
                darkModeEnabled = false,
                measurementUnit = MeasurementUnit.METRIC
            )
        )
    }

    /**
     * Creates a test meal with default values
     */
    fun createTestMeal(
        id: String = "test_meal_${Random.nextInt()}",
        userId: String = "test_user",
        mealType: MealType = MealType.BREAKFAST
    ): Meal {
        return Meal(
            id = id,
            userId = userId,
            recipeId = "test_recipe",
            timestamp = LocalDateTime.now(),
            mealType = mealType,
            portions = 1.0f,
            nutritionInfo = createTestNutritionInfo(),
            status = MealStatus.PLANNED,
            notes = "Test meal notes"
        )
    }

    /**
     * Creates test nutrition info with realistic values
     */
    fun createTestNutritionInfo(): NutritionInfo {
        return NutritionInfo(
            calories = 300.0 + Random.nextDouble(0.0, 200.0),
            protein = 20.0 + Random.nextDouble(0.0, 15.0),
            carbs = 30.0 + Random.nextDouble(0.0, 20.0),
            fat = 10.0 + Random.nextDouble(0.0, 15.0),
            fiber = 5.0 + Random.nextDouble(0.0, 10.0),
            sugar = 8.0 + Random.nextDouble(0.0, 12.0),
            sodium = 400.0 + Random.nextDouble(0.0, 200.0)
        )
    }

    /**
     * Creates a test recipe with default values
     */
    fun createTestRecipe(
        id: String = "test_recipe_${Random.nextInt()}",
        name: String = "Test Recipe"
    ): Recipe {
        return Recipe(
            id = id,
            name = name,
            ingredients = listOf(
                createTestIngredient("Chicken breast", 200.0, "g"),
                createTestIngredient("Rice", 100.0, "g"),
                createTestIngredient("Vegetables", 150.0, "g")
            ),
            instructions = listOf(
                "Prepare ingredients",
                "Cook chicken",
                "Cook rice",
                "Steam vegetables",
                "Serve together"
            ),
            nutritionInfo = createTestNutritionInfo(),
            prepTime = 15,
            cookTime = 30,
            servings = 2,
            tags = listOf("healthy", "protein", "balanced"),
            rating = 4.5f,
            source = RecipeSource.MANUAL_ENTRY
        )
    }

    /**
     * Creates a test ingredient
     */
    fun createTestIngredient(
        name: String,
        quantity: Double,
        unit: String
    ): Ingredient {
        return Ingredient(
            id = "ingredient_${Random.nextInt()}",
            name = name,
            quantity = quantity,
            unit = unit,
            nutritionInfo = createTestNutritionInfo()
        )
    }

    /**
     * Creates a test health metric
     */
    fun createTestHealthMetric(
        id: String = "test_metric_${Random.nextInt()}",
        userId: String = "test_user",
        type: HealthMetricType = HealthMetricType.HEART_RATE
    ): HealthMetric {
        return HealthMetric(
            id = id,
            userId = userId,
            type = type,
            value = getRealisticValueForMetric(type),
            unit = getUnitForMetric(type),
            timestamp = LocalDateTime.now(),
            source = DataSource.HEALTH_CONNECT,
            confidence = 1.0f,
            isManualEntry = false
        )
    }

    /**
     * Creates a test supplement
     */
    fun createTestSupplement(
        id: String = "test_supplement_${Random.nextInt()}",
        name: String = "Test Supplement"
    ): Supplement {
        return Supplement(
            id = id,
            name = name,
            brand = "Test Brand",
            dosage = "500mg",
            frequency = SupplementFrequency.DAILY,
            nutritionInfo = createTestNutritionInfo(),
            notes = "Test supplement notes"
        )
    }

    /**
     * Creates a test biomarker
     */
    fun createTestBiomarker(
        id: String = "test_biomarker_${Random.nextInt()}",
        userId: String = "test_user",
        type: BiomarkerType = BiomarkerType.VITAMIN_D
    ): Biomarker {
        return Biomarker(
            id = id,
            userId = userId,
            type = type,
            value = getRealisticValueForBiomarker(type),
            unit = getUnitForBiomarker(type),
            testDate = LocalDate.now(),
            referenceRange = getReferenceRangeForBiomarker(type),
            notes = "Test biomarker notes"
        )
    }

    /**
     * Gets realistic values for different health metrics
     */
    private fun getRealisticValueForMetric(type: HealthMetricType): Double {
        return when (type) {
            HealthMetricType.HEART_RATE -> 60.0 + Random.nextDouble(0.0, 40.0)
            HealthMetricType.WEIGHT -> 60.0 + Random.nextDouble(0.0, 40.0)
            HealthMetricType.STEPS -> 5000.0 + Random.nextDouble(0.0, 10000.0)
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC -> 110.0 + Random.nextDouble(0.0, 30.0)
            HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> 70.0 + Random.nextDouble(0.0, 20.0)
            HealthMetricType.BODY_FAT_PERCENTAGE -> 15.0 + Random.nextDouble(0.0, 15.0)
            HealthMetricType.SLEEP_HOURS -> 6.0 + Random.nextDouble(0.0, 4.0)
            else -> Random.nextDouble(0.0, 100.0)
        }
    }

    /**
     * Gets appropriate units for health metrics
     */
    private fun getUnitForMetric(type: HealthMetricType): String {
        return when (type) {
            HealthMetricType.HEART_RATE -> "bpm"
            HealthMetricType.WEIGHT -> "kg"
            HealthMetricType.STEPS -> "steps"
            HealthMetricType.BLOOD_PRESSURE_SYSTOLIC, HealthMetricType.BLOOD_PRESSURE_DIASTOLIC -> "mmHg"
            HealthMetricType.BODY_FAT_PERCENTAGE -> "%"
            HealthMetricType.SLEEP_HOURS -> "hours"
            else -> "unit"
        }
    }

    /**
     * Gets realistic values for biomarkers
     */
    private fun getRealisticValueForBiomarker(type: BiomarkerType): Double {
        return when (type) {
            BiomarkerType.VITAMIN_D -> 20.0 + Random.nextDouble(0.0, 30.0)
            BiomarkerType.VITAMIN_B12 -> 200.0 + Random.nextDouble(0.0, 600.0)
            BiomarkerType.IRON -> 10.0 + Random.nextDouble(0.0, 20.0)
            BiomarkerType.TESTOSTERONE -> 300.0 + Random.nextDouble(0.0, 700.0)
            BiomarkerType.CORTISOL -> 5.0 + Random.nextDouble(0.0, 20.0)
            else -> Random.nextDouble(0.0, 100.0)
        }
    }

    /**
     * Gets appropriate units for biomarkers
     */
    private fun getUnitForBiomarker(type: BiomarkerType): String {
        return when (type) {
            BiomarkerType.VITAMIN_D -> "ng/mL"
            BiomarkerType.VITAMIN_B12 -> "pg/mL"
            BiomarkerType.IRON -> "μg/dL"
            BiomarkerType.TESTOSTERONE -> "ng/dL"
            BiomarkerType.CORTISOL -> "μg/dL"
            else -> "unit"
        }
    }

    /**
     * Gets reference ranges for biomarkers
     */
    private fun getReferenceRangeForBiomarker(type: BiomarkerType): String {
        return when (type) {
            BiomarkerType.VITAMIN_D -> "30-100 ng/mL"
            BiomarkerType.VITAMIN_B12 -> "200-900 pg/mL"
            BiomarkerType.IRON -> "60-170 μg/dL"
            BiomarkerType.TESTOSTERONE -> "300-1000 ng/dL"
            BiomarkerType.CORTISOL -> "6-23 μg/dL"
            else -> "Normal range"
        }
    }

    /**
     * Creates a list of test meals for a date range
     */
    fun createTestMealsForDateRange(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        mealsPerDay: Int = 3
    ): List<Meal> {
        val meals = mutableListOf<Meal>()
        var currentDate = startDate

        while (!currentDate.isAfter(endDate)) {
            repeat(mealsPerDay) { mealIndex ->
                val mealType = when (mealIndex) {
                    0 -> MealType.BREAKFAST
                    1 -> MealType.LUNCH
                    2 -> MealType.DINNER
                    else -> MealType.SNACK
                }

                meals.add(
                    createTestMeal(
                        userId = userId,
                        mealType = mealType
                    ).copy(
                        timestamp = currentDate.atTime(8 + mealIndex * 4, 0)
                    )
                )
            }
            currentDate = currentDate.plusDays(1)
        }

        return meals
    }

    /**
     * Asserts that two nutrition info objects are approximately equal
     */
    fun assertNutritionInfoEquals(
        expected: NutritionInfo,
        actual: NutritionInfo,
        delta: Double = 0.01
    ) {
        kotlin.test.assertEquals(expected.calories, actual.calories, delta)
        kotlin.test.assertEquals(expected.protein, actual.protein, delta)
        kotlin.test.assertEquals(expected.carbs, actual.carbs, delta)
        kotlin.test.assertEquals(expected.fat, actual.fat, delta)
        kotlin.test.assertEquals(expected.fiber, actual.fiber, delta)
        kotlin.test.assertEquals(expected.sugar, actual.sugar, delta)
        kotlin.test.assertEquals(expected.sodium, actual.sodium, delta)
    }
}