package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.dao.NotificationDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.notification.NotificationScheduler
import com.beaconledger.welltrack.data.repository.NotificationRepositoryImpl
import com.beaconledger.welltrack.domain.repository.NotificationRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NotificationModule {
    
    @Provides
    @Singleton
    fun provideNotificationRepository(
        notificationDao: NotificationDao,
        recipeDao: RecipeDao,
        notificationScheduler: NotificationScheduler
    ): NotificationRepository {
        return NotificationRepositoryImpl(
            notificationDao = notificationDao,
            recipeDao = recipeDao,
            notificationScheduler = notificationScheduler
        )
    }
}