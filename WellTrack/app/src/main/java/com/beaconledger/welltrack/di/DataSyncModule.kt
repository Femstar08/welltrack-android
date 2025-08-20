package com.beaconledger.welltrack.di

import android.content.Context
import com.beaconledger.welltrack.data.backup.BackupManager
import com.beaconledger.welltrack.data.cache.ConnectivityMonitor
import com.beaconledger.welltrack.data.cache.OfflineCacheManager
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.remote.SupabaseClient
import com.beaconledger.welltrack.data.repository.DataSyncRepositoryImpl
import com.beaconledger.welltrack.data.security.EncryptionManager
import com.beaconledger.welltrack.data.sync.DeviceIdProvider
import com.beaconledger.welltrack.data.sync.EntitySyncHandler
import com.beaconledger.welltrack.data.sync.SyncService
import com.beaconledger.welltrack.data.sync.handlers.HealthMetricSyncHandler
import com.beaconledger.welltrack.data.sync.handlers.MealSyncHandler
import com.beaconledger.welltrack.domain.repository.DataSyncRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSyncModule {
    
    @Binds
    abstract fun bindDataSyncRepository(
        dataSyncRepositoryImpl: DataSyncRepositoryImpl
    ): DataSyncRepository
    
    companion object {
        
        @Provides
        @Singleton
        fun provideEncryptionManager(): EncryptionManager {
            return EncryptionManager()
        }
        
        @Provides
        @Singleton
        fun provideDeviceIdProvider(
            @ApplicationContext context: Context
        ): DeviceIdProvider {
            return DeviceIdProvider(context)
        }
        
        @Provides
        @Singleton
        fun provideConnectivityMonitor(
            @ApplicationContext context: Context
        ): ConnectivityMonitor {
            return ConnectivityMonitor(context)
        }
        
        @Provides
        @Singleton
        fun provideSupabaseClient(): SupabaseClient {
            return SupabaseClient()
        }
        
        @Provides
        @Singleton
        fun provideBackupManager(
            database: WellTrackDatabase,
            encryptionManager: EncryptionManager,
            @ApplicationContext context: Context
        ): BackupManager {
            return BackupManager(database, encryptionManager, context)
        }
        
        @Provides
        @Singleton
        fun provideSyncService(
            database: WellTrackDatabase,
            encryptionManager: EncryptionManager,
            supabaseClient: SupabaseClient,
            entitySyncHandlers: Map<String, @JvmSuppressWildcards EntitySyncHandler<*>>,
            deviceIdProvider: DeviceIdProvider
        ): SyncService {
            return SyncService(
                database.syncStatusDao(),
                encryptionManager,
                supabaseClient,
                entitySyncHandlers,
                deviceIdProvider
            )
        }
        
        @Provides
        @Singleton
        fun provideOfflineCacheManager(
            database: WellTrackDatabase,
            syncService: SyncService,
            connectivityMonitor: ConnectivityMonitor,
            @ApplicationContext context: Context
        ): OfflineCacheManager {
            return OfflineCacheManager(database, syncService, connectivityMonitor, context)
        }
        
        @Provides
        @Singleton
        @IntoMap
        @StringKey("HealthMetric")
        fun provideHealthMetricSyncHandler(
            database: WellTrackDatabase,
            supabaseClient: SupabaseClient,
            encryptionManager: EncryptionManager
        ): EntitySyncHandler<*> {
            return HealthMetricSyncHandler(
                database.healthMetricDao(),
                supabaseClient,
                encryptionManager
            )
        }
        
        @Provides
        @Singleton
        @IntoMap
        @StringKey("Meal")
        fun provideMealSyncHandler(
            database: WellTrackDatabase,
            supabaseClient: SupabaseClient,
            encryptionManager: EncryptionManager
        ): EntitySyncHandler<*> {
            return MealSyncHandler(
                database.mealDao(),
                supabaseClient,
                encryptionManager
            )
        }
    }
}