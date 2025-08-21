package com.beaconledger.welltrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.presentation.meal.MealLoggingScreen
import com.beaconledger.welltrack.presentation.meal.MealLoggingViewModel
import com.beaconledger.welltrack.ui.theme.WellTrackTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class MealLoggingFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var mealLoggingViewModel: MealLoggingViewModel

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun mealLoggingScreen_displaysCorrectElements() {
        composeTestRule.setContent {
            WellTrackTheme {
                MealLoggingScreen(
                    viewModel = mealLoggingViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Verify meal logging screen elements
        composeTestRule.onNodeWithText("Log Meal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manual Entry").assertIsDisplayed()
        composeTestRule.onNodeWithText("Camera").assertIsDisplayed()
        composeTestRule.onNodeWithText("From Recipe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Import URL").assertIsDisplayed()
    }

    @Test
    fun manualEntry_showsInputFields() {
        composeTestRule.setContent {
            WellTrackTheme {
                MealLoggingScreen(
                    viewModel = mealLoggingViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Click manual entry
        composeTestRule.onNodeWithText("Manual Entry").performClick()

        // Verify input fields are displayed
        composeTestRule.onNodeWithText("Meal Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Calories").assertIsDisplayed()
        composeTestRule.onNodeWithText("Protein (g)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Carbs (g)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Fat (g)").assertIsDisplayed()
    }

    @Test
    fun manualEntry_acceptsNutritionInput() {
        composeTestRule.setContent {
            WellTrackTheme {
                MealLoggingScreen(
                    viewModel = mealLoggingViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Click manual entry
        composeTestRule.onNodeWithText("Manual Entry").performClick()

        // Enter meal details
        composeTestRule.onNodeWithText("Meal Name").performTextInput("Grilled Chicken")
        composeTestRule.onNodeWithText("Calories").performTextInput("300")
        composeTestRule.onNodeWithText("Protein (g)").performTextInput("25")
        composeTestRule.onNodeWithText("Carbs (g)").performTextInput("5")
        composeTestRule.onNodeWithText("Fat (g)").performTextInput("15")

        // Verify save button is available
        composeTestRule.onNodeWithText("Save Meal").assertIsDisplayed()
    }

    @Test
    fun mealTypeSelection_showsOptions() {
        composeTestRule.setContent {
            WellTrackTheme {
                MealLoggingScreen(
                    viewModel = mealLoggingViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Click manual entry
        composeTestRule.onNodeWithText("Manual Entry").performClick()

        // Click meal type dropdown
        composeTestRule.onNodeWithText("Breakfast").performClick()

        // Verify meal type options
        composeTestRule.onNodeWithText("Breakfast").assertIsDisplayed()
        composeTestRule.onNodeWithText("Lunch").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dinner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Snack").assertIsDisplayed()
    }

    @Test
    fun cameraOption_showsCameraInterface() {
        composeTestRule.setContent {
            WellTrackTheme {
                MealLoggingScreen(
                    viewModel = mealLoggingViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Click camera option
        composeTestRule.onNodeWithText("Camera").performClick()

        // Verify camera interface elements
        composeTestRule.onNodeWithContentDescription("Camera capture").assertIsDisplayed()
        composeTestRule.onNodeWithText("Take Photo").assertIsDisplayed()
    }

    @Test
    fun urlImport_showsUrlInput() {
        composeTestRule.setContent {
            WellTrackTheme {
                MealLoggingScreen(
                    viewModel = mealLoggingViewModel,
                    onNavigateBack = {}
                )
            }
        }

        // Click URL import
        composeTestRule.onNodeWithText("Import URL").performClick()

        // Verify URL input field
        composeTestRule.onNodeWithText("Recipe URL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Import Recipe").assertIsDisplayed()
    }
}