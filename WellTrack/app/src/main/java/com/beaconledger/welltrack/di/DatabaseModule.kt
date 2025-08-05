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
            WellTrackDatabase.DATABASE_NAME
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
        return database.profileDao()
    }

    @Provides
    fun provideWorkoutDao(database: WellTrackDatabase): WorkoutDao {
        return database.workoutDao()
    }
}