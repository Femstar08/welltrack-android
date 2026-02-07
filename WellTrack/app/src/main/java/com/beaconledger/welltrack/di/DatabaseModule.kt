package com.beaconledger.welltrack.di

import android.content.Context
import androidx.room.Room
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideWellTrackDatabase(@ApplicationContext context: Context): WellTrackDatabase {
        return Room.databaseBuilder(
            context,
            WellTrackDatabase::class.java,
            DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideUserDao(database: WellTrackDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideRecipeDao(database: WellTrackDatabase): RecipeDao {
        return database.recipeDao()
    }

    @Provides
    fun provideRecipeIngredientDao(database: WellTrackDatabase): RecipeIngredientDao {
        return database.recipeIngredientDao()
    }

    @Provides
    fun provideMealDao(database: WellTrackDatabase): MealDao {
        return database.mealDao()
    }

    @Provides
    fun provideHealthMetricDao(database: WellTrackDatabase): HealthMetricDao {
        return database.healthMetricDao()
    }

    @Provides
    fun provideProfileDao(database: WellTrackDatabase): ProfileDao {
        return database.userProfileDao()
    }

    @Provides
    fun provideWorkoutDao(database: WellTrackDatabase): WorkoutDao {
        return database.workoutDao()
    }

    @Provides
    fun provideCookingSessionDao(database: WellTrackDatabase): CookingSessionDao {
        return database.cookingSessionDao()
    }

    @Provides
    fun provideMealPlanDao(database: WellTrackDatabase): MealPlanDao {
        return database.mealPlanDao()
    }

    @Provides
    fun provideMealPrepDao(database: WellTrackDatabase): MealPrepDao {
        return database.mealPrepDao()
    }

    @Provides
    fun provideSupplementDao(database: WellTrackDatabase): com.beaconledger.welltrack.data.database.dao.SupplementDao {
        return database.supplementDao()
    }

    @Provides
    fun providePantryDao(database: WellTrackDatabase): com.beaconledger.welltrack.data.database.dao.PantryDao {
        return database.pantryItemDao()
    }

    @Provides
    fun provideIngredientUsageDao(database: WellTrackDatabase): com.beaconledger.welltrack.data.database.dao.IngredientUsageDao {
        return database.ingredientUsageDao()
    }

    @Provides
    fun provideShoppingListDao(database: WellTrackDatabase): com.beaconledger.welltrack.data.database.dao.ShoppingListDao {
        return database.shoppingListDao()
    }

    @Provides
    @Singleton
    fun provideMealPrepRepository(
        mealPrepDao: MealPrepDao,
        gson: com.google.gson.Gson
    ): com.beaconledger.welltrack.domain.repository.MealPrepRepository {
        return com.beaconledger.welltrack.data.repository.MealPrepRepositoryImpl(mealPrepDao, gson)
    }
}