package com.beaconledger.welltrack.di

import android.content.SharedPreferences
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.profile.ProfileSessionManager
import com.beaconledger.welltrack.data.profile.UserContextManager
import com.beaconledger.welltrack.data.repository.ProfileRepositoryImpl
import com.beaconledger.welltrack.data.repository.ProfileDataRepository
import com.beaconledger.welltrack.domain.repository.ProfileRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun provideProfileRepository(
        profileDao: ProfileDao
    ): ProfileRepository {
        return ProfileRepositoryImpl(profileDao)
    }

    @Provides
    @Singleton
    fun provideProfileSessionManager(
        sharedPreferences: SharedPreferences
    ): ProfileSessionManager {
        return ProfileSessionManager(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideUserContextManager(
        profileSessionManager: ProfileSessionManager
    ): UserContextManager {
        return UserContextManager(profileSessionManager)
    }

    @Provides
    @Singleton
    fun provideProfileDataRepository(
        userContextManager: UserContextManager,
        mealDao: MealDao,
        healthMetricDao: HealthMetricDao,
        recipeDao: RecipeDao,
        profileDao: ProfileDao
    ): ProfileDataRepository {
        return ProfileDataRepository(
            userContextManager,
            mealDao,
            healthMetricDao,
            recipeDao,
            profileDao
        )
    }
}