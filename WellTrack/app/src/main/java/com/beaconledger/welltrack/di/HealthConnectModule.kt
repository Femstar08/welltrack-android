package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.repository.HealthConnectRepositoryImpl
import com.beaconledger.welltrack.domain.repository.HealthConnectRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HealthConnectModule {
    
    @Binds
    @Singleton
    abstract fun bindHealthConnectRepository(
        healthConnectRepositoryImpl: HealthConnectRepositoryImpl
    ): HealthConnectRepository
}