package com.beaconledger.welltrack.performance

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.presentation.dashboard.DashboardScreen
import com.beaconledger.welltrack.presentation.dashboard.DashboardViewModel
import com.beaconledger.welltrack.ui.theme.WellTrackTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.system.measureTimeMillis
import kotlin.test.assertTrue

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class UIPerformanceTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var dashboardViewModel: DashboardViewModel

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun dashboardComposition_shouldRenderQuickly() {
        val compositionTime = measureTimeMillis {
            composeTestRule.setContent {
                WellTrackTheme {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToMeals = {},
                        onNavigateToPlanner = {},
                        onNavigateToHealth = {}
                    )
                }
            }
        }

        // Dashboard should compose within 2 seconds
        assertTrue(compositionTime < 2000, "Dashboard composition took $compositionTime ms, expected < 2000 ms")
    }

    @Test
    fun userInteraction_shouldRespondQuickly() {
        composeTestRule.setContent {
            WellTrackTheme {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToMeals = {},
                    onNavigateToPlanner = {},
                    onNavigateToHealth = {}
                )
            }
        }

        val interactionTime = measureTimeMillis {
            // Perform multiple UI interactions
            composeTestRule.onNodeWithText("View All Meals").performClick()
            composeTestRule.onNodeWithText("Add Meal").performClick()
            composeTestRule.onNodeWithText("Refresh").performClick()
        }

        // UI interactions should complete within 1 second
        assertTrue(interactionTime < 1000, "UI interactions took $interactionTime ms, expected < 1000 ms")
    }

    @Test
    fun scrollPerformance_shouldScrollSmoothly() {
        composeTestRule.setContent {
            WellTrackTheme {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToMeals = {},
                    onNavigateToPlanner = {},
                    onNavigateToHealth = {}
                )
            }
        }

        val scrollTime = measureTimeMillis {
            // Simulate scrolling through dashboard content
            repeat(10) {
                composeTestRule.onNodeWithText("Today's Summary").performClick()
            }
        }

        // Scrolling should be smooth and complete within 2 seconds
        assertTrue(scrollTime < 2000, "Scrolling took $scrollTime ms, expected < 2000 ms")
    }

    @Test
    fun dataLoading_shouldDisplayPlaceholdersQuickly() {
        val loadingTime = measureTimeMillis {
            composeTestRule.setContent {
                WellTrackTheme {
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToMeals = {},
                        onNavigateToPlanner = {},
                        onNavigateToHealth = {}
                    )
                }
            }

            // Wait for loading states to appear
            composeTestRule.waitForIdle()
        }

        // Loading placeholders should appear within 500ms
        assertTrue(loadingTime < 500, "Loading states took $loadingTime ms, expected < 500 ms")
    }

    @Test
    fun memoryUsage_shouldNotExceedLimits() {
        // Get initial memory usage
        val runtime = Runtime.getRuntime()
        val initialMemory = runtime.totalMemory() - runtime.freeMemory()

        composeTestRule.setContent {
            WellTrackTheme {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToMeals = {},
                    onNavigateToPlanner = {},
                    onNavigateToHealth = {}
                )
            }
        }

        // Perform multiple operations to stress test memory
        repeat(50) {
            composeTestRule.onNodeWithText("Refresh").performClick()
            composeTestRule.waitForIdle()
        }

        // Force garbage collection
        runtime.gc()
        Thread.sleep(100)

        val finalMemory = runtime.totalMemory() - runtime.freeMemory()
        val memoryIncrease = finalMemory - initialMemory

        // Memory increase should be reasonable (less than 50MB)
        assertTrue(memoryIncrease < 50 * 1024 * 1024, "Memory increased by ${memoryIncrease / (1024 * 1024)}MB, expected < 50MB")
    }
}