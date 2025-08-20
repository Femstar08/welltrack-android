package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.*
import com.beaconledger.welltrack.data.remote.SupabaseClient
import com.beaconledger.welltrack.data.repository.SocialRepositoryImpl
import com.beaconledger.welltrack.domain.repository.SocialRepository
import com.beaconledger.welltrack.domain.usecase.SocialUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SocialModule {

    @Provides
    @Singleton
    fun provideFamilyGroupDao(database: WellTrackDatabase): FamilyGroupDao {
        return database.familyGroupDao()
    }

    @Provides
    @Singleton
    fun provideFamilyMemberDao(database: WellTrackDatabase): FamilyMemberDao {
        return database.familyMemberDao()
    }

    @Provides
    @Singleton
    fun provideSharedMealPlanDao(database: WellTrackDatabase): SharedMealPlanDao {
        return database.sharedMealPlanDao()
    }

    @Provides
    @Singleton
    fun provideSharedRecipeDao(database: WellTrackDatabase): SharedRecipeDao {
        return database.sharedRecipeDao()
    }

    @Provides
    @Singleton
    fun provideCollaborativeMealPrepDao(database: WellTrackDatabase): CollaborativeMealPrepDao {
        return database.collaborativeMealPrepDao()
    }

    @Provides
    @Singleton
    fun provideAchievementDao(database: WellTrackDatabase): AchievementDao {
        return database.achievementDao()
    }

    @Provides
    @Singleton
    fun provideSharedAchievementDao(database: WellTrackDatabase): SharedAchievementDao {
        return database.sharedAchievementDao()
    }

    @Provides
    @Singleton
    fun provideSharedShoppingListDao(database: WellTrackDatabase): SharedShoppingListDao {
        return database.sharedShoppingListDao()
    }

    @Provides
    @Singleton
    fun provideSharedShoppingListItemDao(database: WellTrackDatabase): SharedShoppingListItemDao {
        return database.sharedShoppingListItemDao()
    }

    @Provides
    @Singleton
    fun provideSocialRepository(
        familyGroupDao: FamilyGroupDao,
        familyMemberDao: FamilyMemberDao,
        sharedMealPlanDao: SharedMealPlanDao,
        sharedRecipeDao: SharedRecipeDao,
        collaborativeMealPrepDao: CollaborativeMealPrepDao,
        achievementDao: AchievementDao,
        sharedAchievementDao: SharedAchievementDao,
        sharedShoppingListDao: SharedShoppingListDao,
        sharedShoppingListItemDao: SharedShoppingListItemDao,
        supabaseClient: SupabaseClient
    ): SocialRepository {
        return SocialRepositoryImpl(
            familyGroupDao = familyGroupDao,
            familyMemberDao = familyMemberDao,
            sharedMealPlanDao = sharedMealPlanDao,
            sharedRecipeDao = sharedRecipeDao,
            collaborativeMealPrepDao = collaborativeMealPrepDao,
            achievementDao = achievementDao,
            sharedAchievementDao = sharedAchievementDao,
            sharedShoppingListDao = sharedShoppingListDao,
            sharedShoppingListItemDao = sharedShoppingListItemDao,
            supabaseClient = supabaseClient
        )
    }

    @Provides
    @Singleton
    fun provideSocialUseCase(
        socialRepository: SocialRepository
    ): SocialUseCase {
        return SocialUseCase(socialRepository)
    }
}