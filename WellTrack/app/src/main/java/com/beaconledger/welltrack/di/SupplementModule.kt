package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.SupplementDao
import com.beaconledger.welltrack.data.repository.SupplementRepositoryImpl
import com.beaconledger.welltrack.domain.repository.SupplementRepository
import com.beaconledger.welltrack.domain.usecase.SupplementUseCase
import com.beaconledger.welltrack.data.barcode.BarcodeService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupplementModule {

    @Provides
    fun provideSupplementDao(database: WellTrackDatabase): SupplementDao {
        return database.supplementDao()
    }

    @Provides
    @Singleton
    fun provideSupplementRepository(
        supplementDao: SupplementDao,
        barcodeService: BarcodeService
    ): SupplementRepository {
        return SupplementRepositoryImpl(supplementDao, barcodeService)
    }

    @Provides
    @Singleton
    fun provideSupplementUseCase(
        supplementRepository: SupplementRepository
    ): SupplementUseCase {
        return SupplementUseCase(supplementRepository)
    }
}