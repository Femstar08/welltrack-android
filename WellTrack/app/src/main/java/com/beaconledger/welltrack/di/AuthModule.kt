package com.beaconledger.welltrack.di

import android.content.Context
import android.content.SharedPreferences
import com.beaconledger.welltrack.data.auth.SupabaseAuthManager
import com.beaconledger.welltrack.data.auth.SessionManager
import com.beaconledger.welltrack.data.network.SupabaseClient
import com.beaconledger.welltrack.data.repository.AuthRepositoryImpl
import com.beaconledger.welltrack.domain.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("welltrack_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return SupabaseClient()
    }

    @Provides
    @Singleton
    fun provideSupabaseAuthManager(
        @ApplicationContext context: Context,
        sharedPreferences: SharedPreferences,
        supabaseClient: SupabaseClient
    ): SupabaseAuthManager {
        return SupabaseAuthManager(context, sharedPreferences, supabaseClient)
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        sharedPreferences: SharedPreferences
    ): SessionManager {
        return SessionManager(sharedPreferences)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        supabaseAuthManager: SupabaseAuthManager,
        sessionManager: SessionManager
    ): AuthRepository {
        return AuthRepositoryImpl(supabaseAuthManager, sessionManager)
    }
}