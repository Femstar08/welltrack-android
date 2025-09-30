package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.GoalDao
import com.beaconledger.welltrack.data.repository.GoalRepositoryImpl
import com.beaconledger.welltrack.domain.repository.GoalRepository
import com.beaconledger.welltrack.domain.usecase.GoalUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GoalModule {
    
    @Provides
    @Singleton
    fun provideGoalDao(database: WellTrackDatabase): GoalDao {
        return database.goalDao()
    }
    
    @Provides
    @Singleton
    fun provideGoalRepository(goalDao: GoalDao): GoalRepository {
        return GoalRepositoryImpl(goalDao)
    }
    
    @Provides
    @Singleton
    fun provideGoalUseCase(goalRepository: GoalRepository): GoalUseCase {
        return GoalUseCase(goalRepository)
    }
}