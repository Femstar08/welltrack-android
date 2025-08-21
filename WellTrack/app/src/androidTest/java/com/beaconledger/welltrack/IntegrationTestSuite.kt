package com.beaconledger.welltrack

import com.beaconledger.welltrack.data.database.HealthMetricDaoIntegrationTest
import com.beaconledger.welltrack.data.database.MealDaoIntegrationTest
import com.beaconledger.welltrack.performance.DatabasePerformanceTest
import com.beaconledger.welltrack.performance.UIPerformanceTest
import com.beaconledger.welltrack.ui.AuthenticationFlowTest
import com.beaconledger.welltrack.ui.MealLoggingFlowTest
import com.beaconledger.welltrack.ui.NavigationFlowTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Comprehensive integration test suite for WellTrack application
 * 
 * This suite includes:
 * - Database integration tests
 * - UI flow tests for critical user journeys
 * - Performance tests for database and UI
 * - End-to-end workflow tests
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Database Integration Tests
    MealDaoIntegrationTest::class,
    HealthMetricDaoIntegrationTest::class,
    
    // UI Flow Tests
    AuthenticationFlowTest::class,
    MealLoggingFlowTest::class,
    NavigationFlowTest::class,
    
    // Performance Tests
    DatabasePerformanceTest::class,
    UIPerformanceTest::class
)
class IntegrationTestSuite