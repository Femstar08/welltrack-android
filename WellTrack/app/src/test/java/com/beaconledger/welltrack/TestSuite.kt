package com.beaconledger.welltrack

import com.beaconledger.welltrack.data.repository.AuthRepositoryImplTest
import com.beaconledger.welltrack.data.repository.HealthConnectRepositoryImplTest
import com.beaconledger.welltrack.data.repository.MealRepositoryImplTest
import com.beaconledger.welltrack.data.repository.RecipeRepositoryImplTest
import com.beaconledger.welltrack.domain.usecase.*
import com.beaconledger.welltrack.security.AuthenticationSecurityTest
import com.beaconledger.welltrack.security.EncryptionTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Comprehensive test suite for WellTrack application
 * 
 * This suite includes:
 * - Unit tests for all repository implementations
 * - Unit tests for all use cases
 * - Security tests for authentication and encryption
 * - Data validation tests
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Repository Tests
    AuthRepositoryImplTest::class,
    MealRepositoryImplTest::class,
    HealthConnectRepositoryImplTest::class,
    RecipeRepositoryImplTest::class,
    
    // Use Case Tests
    BiomarkerUseCaseTest::class,
    CostBudgetUseCaseTest::class,
    DailyTrackingUseCaseTest::class,
    DataSyncUseCaseTest::class,
    DietaryFilteringUseCaseTest::class,
    DietaryRestrictionsUseCaseTest::class,
    HealthConnectUseCaseTest::class,
    MacronutrientUseCaseTest::class,
    MealPrepUseCaseTest::class,
    NotificationUseCaseTest::class,
    PantryUseCaseTest::class,
    SocialUseCaseTest::class,
    SupplementUseCaseTest::class,
    
    // Security Tests
    AuthenticationSecurityTest::class,
    EncryptionTest::class
)
class TestSuite