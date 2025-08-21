package com.beaconledger.welltrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.presentation.navigation.WellTrackNavigation
import com.beaconledger.welltrack.ui.theme.WellTrackTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun bottomNavigation_displaysAllTabs() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackNavigation()
            }
        }

        // Verify all bottom navigation tabs are displayed
        composeTestRule.onNodeWithText("Dashboard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Meals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Planner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Health").assertIsDisplayed()
        composeTestRule.onNodeWithText("Profile").assertIsDisplayed()
    }

    @Test
    fun navigation_switchesBetweenTabs() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackNavigation()
            }
        }

        // Navigate to Meals tab
        composeTestRule.onNodeWithText("Meals").performClick()
        composeTestRule.onNodeWithText("Today's Meals").assertIsDisplayed()

        // Navigate to Planner tab
        composeTestRule.onNodeWithText("Planner").performClick()
        composeTestRule.onNodeWithText("Meal Planner").assertIsDisplayed()

        // Navigate to Health tab
        composeTestRule.onNodeWithText("Health").performClick()
        composeTestRule.onNodeWithText("Health Metrics").assertIsDisplayed()

        // Navigate to Profile tab
        composeTestRule.onNodeWithText("Profile").performClick()
        composeTestRule.onNodeWithText("User Profile").assertIsDisplayed()
    }

    @Test
    fun dashboard_displaysKeyMetrics() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackNavigation()
            }
        }

        // Verify dashboard elements
        composeTestRule.onNodeWithText("Today's Summary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Protein").assertIsDisplayed()
        composeTestRule.onNodeWithText("Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Steps").assertIsDisplayed()
    }

    @Test
    fun profileSwitching_showsProfileSelector() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackNavigation()
            }
        }

        // Navigate to Profile tab
        composeTestRule.onNodeWithText("Profile").performClick()

        // Click profile switcher
        composeTestRule.onNodeWithContentDescription("Switch Profile").performClick()

        // Verify profile selection dialog
        composeTestRule.onNodeWithText("Select Profile").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add New Profile").assertIsDisplayed()
    }

    @Test
    fun mealPlanner_showsWeeklyView() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackNavigation()
            }
        }

        // Navigate to Planner tab
        composeTestRule.onNodeWithText("Planner").performClick()

        // Verify weekly planner elements
        composeTestRule.onNodeWithText("This Week").assertIsDisplayed()
        composeTestRule.onNodeWithText("Monday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tuesday").assertIsDisplayed()
        composeTestRule.onNodeWithText("Generate Plan").assertIsDisplayed()
    }

    @Test
    fun healthMetrics_showsIntegrationOptions() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackNavigation()
            }
        }

        // Navigate to Health tab
        composeTestRule.onNodeWithText("Health").performClick()

        // Verify health integration options
        composeTestRule.onNodeWithText("Health Connect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Garmin Connect").assertIsDisplayed()
        composeTestRule.onNodeWithText("Samsung Health").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manual Entry").assertIsDisplayed()
    }
}