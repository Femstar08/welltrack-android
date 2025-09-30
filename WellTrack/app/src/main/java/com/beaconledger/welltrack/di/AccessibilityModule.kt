package com.beaconledger.welltrack.di

import android.content.Context
import com.beaconledger.welltrack.accessibility.AccessibilityManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dependency injection module for accessibility components
 */
@Module
@InstallIn(SingletonComponent::class)
object AccessibilityModule {
    
    @Provides
    @Singleton
    fun provideAccessibilityManager(
        @ApplicationContext context: Context
    ): AccessibilityManager {
        return AccessibilityManager(context)
    }
}