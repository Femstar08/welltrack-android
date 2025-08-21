package com.beaconledger.welltrack.presentation.ux

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.presentation.components.EnhancedProfileSwitcher
import com.beaconledger.welltrack.presentation.components.UserProfile
import com.beaconledger.welltrack.presentation.components.ActivityLevel
import com.beaconledger.welltrack.presentation.design.*
import com.beaconledger.welltrack.ui.theme.WellTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UXOptimizationIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testEnhancedProfileSwitcherVisualIndicators() {
        val profiles = listOf(
            UserProfile("1", "John Doe", 30, ActivityLevel.MODERATELY_ACTIVE),
            UserProfile("2", "Jane Smith", 25, ActivityLevel.VERY_ACTIVE)
        )
        
        composeTestRule.setContent {
            WellTrackTheme {
                EnhancedProfileSwitcher(
                    activeProfile = profiles[0],
                    allProfiles = profiles,
                    onProfileSwitch = {},
                    onAddProfile = {}
                )
            }
        }
        
        // Verify profile switcher displays correctly
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("2 profiles").assertIsDisplayed()
        
        // Test profile switching
        composeTestRule.onNodeWithText("John Doe").performClick()
        composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    }

    @Test
    fun testWellTrackDesignSystemConsistency() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackCard {
                    WellTrackButton(
                        onClick = {},
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Medium
                    ) {
                        androidx.compose.material3.Text("Test Button")
                    }
                }
            }
        }
        
        // Verify design system components render correctly
        composeTestRule.onNodeWithText("Test Button").assertIsDisplayed()
    }

    @Test
    fun testStatusIndicatorStates() {
        composeTestRule.setContent {
            WellTrackTheme {
                androidx.compose.foundation.layout.Column {
                    StatusIndicator(status = Status.Success)
                    StatusIndicator(status = Status.Warning)
                    StatusIndicator(status = Status.Error)
                    StatusIndicator(status = Status.Loading)
                }
            }
        }
        
        // Verify all status indicators are displayed
        composeTestRule.onNodeWithText("Success").assertIsDisplayed()
        composeTestRule.onNodeWithText("Warning").assertIsDisplayed()
        composeTestRule.onNodeWithText("Error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()
    }

    @Test
    fun testProgressBarAnimations() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackProgressBar(
                    progress = 0.75f,
                    label = "Test Progress",
                    showPercentage = true,
                    animated = true
                )
            }
        }
        
        // Verify progress bar displays correctly
        composeTestRule.onNodeWithText("Test Progress").assertIsDisplayed()
        composeTestRule.onNodeWithText("75%").assertIsDisplayed()
    }

    @Test
    fun testAvatarComponent() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackAvatar(
                    name = "John Doe",
                    showOnlineIndicator = true,
                    isOnline = true
                )
            }
        }
        
        // Verify avatar displays initials
        composeTestRule.onNodeWithText("JD").assertIsDisplayed()
    }

    @Test
    fun testEmptyStateComponent() {
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackEmptyState(
                    icon = androidx.compose.material.icons.Icons.Default.Restaurant,
                    title = "No meals logged",
                    description = "Start tracking your nutrition by logging your first meal",
                    actionLabel = "Log Meal",
                    onActionClick = {}
                )
            }
        }
        
        // Verify empty state displays correctly
        composeTestRule.onNodeWithText("No meals logged").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start tracking your nutrition by logging your first meal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Log Meal").assertIsDisplayed()
    }

    @Test
    fun testTextFieldValidation() {
        var value = ""
        var errorMessage: String? = null
        
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = "Test Field",
                    isError = errorMessage != null,
                    errorMessage = errorMessage
                )
            }
        }
        
        // Verify text field displays correctly
        composeTestRule.onNodeWithText("Test Field").assertIsDisplayed()
        
        // Test error state
        errorMessage = "This field is required"
        composeTestRule.setContent {
            WellTrackTheme {
                WellTrackTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = "Test Field",
                    isError = errorMessage != null,
                    errorMessage = errorMessage
                )
            }
        }
        
        composeTestRule.onNodeWithText("This field is required").assertIsDisplayed()
    }

    @Test
    fun testButtonVariants() {
        composeTestRule.setContent {
            WellTrackTheme {
                androidx.compose.foundation.layout.Column {
                    WellTrackButton(
                        onClick = {},
                        variant = ButtonVariant.Primary
                    ) {
                        androidx.compose.material3.Text("Primary")
                    }
                    
                    WellTrackButton(
                        onClick = {},
                        variant = ButtonVariant.Secondary
                    ) {
                        androidx.compose.material3.Text("Secondary")
                    }
                    
                    WellTrackButton(
                        onClick = {},
                        variant = ButtonVariant.Tertiary
                    ) {
                        androidx.compose.material3.Text("Tertiary")
                    }
                }
            }
        }
        
        // Verify all button variants are displayed
        composeTestRule.onNodeWithText("Primary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Secondary").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tertiary").assertIsDisplayed()
    }

    @Test
    fun testResponsiveDesignElements() {
        composeTestRule.setContent {
            WellTrackTheme {
                androidx.compose.foundation.layout.Column {
                    // Test different card sizes
                    WellTrackCard(
                        cornerRadius = WellTrackCorners.sm,
                        elevation = WellTrackElevation.sm
                    ) {
                        androidx.compose.material3.Text("Small Card")
                    }
                    
                    WellTrackCard(
                        cornerRadius = WellTrackCorners.lg,
                        elevation = WellTrackElevation.lg
                    ) {
                        androidx.compose.material3.Text("Large Card")
                    }
                }
            }
        }
        
        // Verify cards with different styling are displayed
        composeTestRule.onNodeWithText("Small Card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Large Card").assertIsDisplayed()
    }
}