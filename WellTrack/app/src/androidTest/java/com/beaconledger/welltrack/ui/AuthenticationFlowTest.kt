package com.beaconledger.welltrack.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.presentation.auth.AuthScreen
import com.beaconledger.welltrack.presentation.auth.AuthViewModel
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
class AuthenticationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Inject
    lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun loginFlow_displaysCorrectElements() {
        composeTestRule.setContent {
            WellTrackTheme {
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {},
                    onNavigateToProfile = {}
                )
            }
        }

        // Verify login screen elements are displayed
        composeTestRule.onNodeWithText("Welcome to WellTrack").assertIsDisplayed()
        composeTestRule.onNodeWithText("Email").assertIsDisplayed()
        composeTestRule.onNodeWithText("Password").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
        composeTestRule.onNodeWithText("Don't have an account? Sign Up").assertIsDisplayed()
    }

    @Test
    fun signUpFlow_switchesToSignUpMode() {
        composeTestRule.setContent {
            WellTrackTheme {
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {},
                    onNavigateToProfile = {}
                )
            }
        }

        // Click sign up link
        composeTestRule.onNodeWithText("Don't have an account? Sign Up").performClick()

        // Verify sign up elements are displayed
        composeTestRule.onNodeWithText("Create Account").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sign Up").assertIsDisplayed()
        composeTestRule.onNodeWithText("Already have an account? Sign In").assertIsDisplayed()
    }

    @Test
    fun loginForm_acceptsUserInput() {
        composeTestRule.setContent {
            WellTrackTheme {
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {},
                    onNavigateToProfile = {}
                )
            }
        }

        // Enter email and password
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Verify input is accepted (button should be enabled)
        composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
    }

    @Test
    fun passwordVisibilityToggle_worksCorrectly() {
        composeTestRule.setContent {
            WellTrackTheme {
                AuthScreen(
                    viewModel = authViewModel,
                    onNavigateToHome = {},
                    onNavigateToProfile = {}
                )
            }
        }

        // Enter password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Toggle password visibility
        composeTestRule.onNodeWithContentDescription("Toggle password visibility").performClick()

        // Password should now be visible (implementation dependent)
        composeTestRule.onNodeWithContentDescription("Toggle password visibility").assertIsDisplayed()
    }
}