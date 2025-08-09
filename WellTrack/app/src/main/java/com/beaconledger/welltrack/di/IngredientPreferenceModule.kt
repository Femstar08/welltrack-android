package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.IngredientPreferenceDao
import com.beaconledger.welltrack.data.database.dao.IngredientUsageDao
import com.beaconledger.welltrack.data.database.dao.PantryDao
import com.beaconledger.welltrack.data.repository.IngredientPreferenceRepositoryImpl
import com.beaconledger.welltrack.domain.repository.IngredientPreferenceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IngredientPreferenceModule {
    
    @Provides
    @Singleton
    fun provideIngredientPreferenceDao(database: WellTrackDatabase): IngredientPreferenceDao {
        return database.ingredientPreferenceDao()
    }
    
    @Provides
    @Singleton
    fun providePantryDao(database: WellTrackDatabase): PantryDao {
        return database.pantryDao()
    }
    
    @Provides
    @Singleton
    fun provideIngredientUsageDao(database: WellTrackDatabase): IngredientUsageDao {
        return database.ingredientUsageDao()
    }
    
    @Provides
    @Singleton
    fun provideIngredientPreferenceRepository(
        ingredientPreferenceDao: IngredientPreferenceDao,
        pantryDao: PantryDao,
        ingredientUsageDao: IngredientUsageDao
    ): IngredientPreferenceRepository {
        return IngredientPreferenceRepositoryImpl(
            ingredientPreferenceDao,
            pantryDao,
            ingredientUsageDao
        )
    }
}