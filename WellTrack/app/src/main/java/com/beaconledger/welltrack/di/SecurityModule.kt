package com.beaconledger.welltrack.di

import android.content.Context
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.remote.SupabaseClient
import com.beaconledger.welltrack.data.security.*
import com.beaconledger.welltrack.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideSecurePreferencesManager(
        @ApplicationContext context: Context
    ): SecurePreferencesManager {
        return SecurePreferencesManager(context)
    }
    
    @Provides
    @Singleton
    fun provideBiometricAuthManager(
        @ApplicationContext context: Context
    ): BiometricAuthManager {
        return BiometricAuthManager(context)
    }
    
    @Provides
    @Singleton
    fun provideAppLockManager(
        @ApplicationContext context: Context,
        securePreferencesManager: SecurePreferencesManager
    ): AppLockManager {
        return AppLockManager(context, securePreferencesManager)
    }
    
    @Provides
    @Singleton
    fun provideAuditLogger(
        @ApplicationContext context: Context,
        database: WellTrackDatabase
    ): AuditLogger {
        return AuditLogger(context, database)
    }
    
    @Provides
    @Singleton
    fun providePrivacyControlsManager(
        @ApplicationContext context: Context,
        securePreferencesManager: SecurePreferencesManager,
        auditLogger: AuditLogger
    ): PrivacyControlsManager {
        return PrivacyControlsManager(context, securePreferencesManager, auditLogger)
    }
    
    @Provides
    @Singleton
    fun provideSecureDataDeletionManager(
        @ApplicationContext context: Context,
        database: WellTrackDatabase,
        supabaseClient: SupabaseClient,
        securePreferencesManager: SecurePreferencesManager,
        auditLogger: AuditLogger
    ): SecureDataDeletionManager {
        return SecureDataDeletionManager(
            context,
            database,
            supabaseClient,
            securePreferencesManager,
            auditLogger
        )
    }
    
    @Provides
    @Singleton
    fun provideSecurityIntegrationManager(
        @ApplicationContext context: Context,
        biometricAuthManager: BiometricAuthManager,
        appLockManager: AppLockManager,
        privacyControlsManager: PrivacyControlsManager,
        secureDataDeletionManager: SecureDataDeletionManager,
        auditLogger: AuditLogger,
        securePreferencesManager: SecurePreferencesManager
    ): SecurityIntegrationManager {
        return SecurityIntegrationManager(
            context,
            biometricAuthManager,
            appLockManager,
            privacyControlsManager,
            secureDataDeletionManager,
            auditLogger,
            securePreferencesManager
        )
    }
}