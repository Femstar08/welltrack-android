package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.MacronutrientDao
import com.beaconledger.welltrack.data.repository.MacronutrientRepositoryImpl
import com.beaconledger.welltrack.domain.repository.MacronutrientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MacronutrientModule {

    @Provides
    @Singleton
    fun provideMacronutrientDao(database: WellTrackDatabase): MacronutrientDao {
        return database.macronutrientDao()
    }

    @Provides
    @Singleton
    fun provideMacronutrientRepository(
        macronutrientDao: MacronutrientDao
    ): MacronutrientRepository {
        return MacronutrientRepositoryImpl(macronutrientDao)
    }
}