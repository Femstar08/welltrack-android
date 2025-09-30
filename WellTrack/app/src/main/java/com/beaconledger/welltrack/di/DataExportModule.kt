package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.repository.DataExportRepositoryImpl
import com.beaconledger.welltrack.domain.repository.DataExportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataExportModule {
    
    @Binds
    @Singleton
    abstract fun bindDataExportRepository(
        dataExportRepositoryImpl: DataExportRepositoryImpl
    ): DataExportRepository
}