package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.BiomarkerDao
import com.beaconledger.welltrack.data.repository.BiomarkerRepositoryImpl
import com.beaconledger.welltrack.domain.repository.BiomarkerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BiomarkerModule {
    
    @Provides
    @Singleton
    fun provideBiomarkerDao(database: WellTrackDatabase): BiomarkerDao {
        return database.biomarkerDao()
    }
    
    @Provides
    @Singleton
    fun provideBiomarkerRepository(
        biomarkerDao: BiomarkerDao
    ): BiomarkerRepository {
        return BiomarkerRepositoryImpl(biomarkerDao)
    }
}