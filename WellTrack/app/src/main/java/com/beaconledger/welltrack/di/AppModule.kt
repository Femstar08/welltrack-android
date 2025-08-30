package com.beaconledger.welltrack.di

import android.content.Context
import androidx.work.WorkManager
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.config.SecureConfigLoader
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideEnvironmentConfig(
        @ApplicationContext context: Context
    ): EnvironmentConfig {
        return EnvironmentConfig(context)
    }

    @Provides
    @Singleton
    fun provideSecureConfigLoader(
        @ApplicationContext context: Context
    ): SecureConfigLoader {
        return SecureConfigLoader(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
        return WorkManager.getInstance(context)
    }
}