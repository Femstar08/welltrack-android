package com.beaconledger.welltrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*

@Database(
    entities = [
        User::class,
        Recipe::class,
        RecipeIngredient::class,
        Meal::class,
        HealthMetric::class,
        CustomHabit::class,
        HabitCompletion::class,
        UserProfile::class,
        Workout::class,
        ExerciseTemplate::class,
        CookingSession::class,
        MealPlan::class,
        PlannedMeal::class,
        PlannedSupplement::class,
        IngredientPreference::class,
        PantryItem::class,
        IngredientUsageHistory::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WellTrackDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun mealDao(): MealDao
    abstract fun healthMetricDao(): HealthMetricDao
    abstract fun profileDao(): ProfileDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun cookingSessionDao(): CookingSessionDao
    abstract fun mealPlanDao(): MealPlanDao
    abstract fun ingredientPreferenceDao(): IngredientPreferenceDao
    abstract fun pantryDao(): PantryDao
    abstract fun ingredientUsageDao(): IngredientUsageDao

    companion object {
        const val DATABASE_NAME = "welltrack_database"
    }
}