package com.beaconledger.welltrack.accessibility

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.accessibility.AccessibilityTestingUtils.assertColorContrast
import com.beaconledger.welltrack.accessibility.AccessibilityTestingUtils.performAccessibilityAudit
import com.beaconledger.welltrack.ui.theme.AccessibleColors
import com.beaconledger.welltrack.ui.theme.WellTrackTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive accessibility compliance tests for WCAG 2.1 AA standards
 */
@RunWith(AndroidJUnit4::class)
class AccessibilityComplianceTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testColorContrastCompliance() {
        // Test primary colors
        assertColorContrast(
            foreground = AccessibleColors.AccessiblePrimary,
            background = androidx.compose.ui.graphics.Color.White,
            elementDescription = "Primary color on white background"
        )
        
        assertColorContrast(
            foreground = AccessibleColors.AccessibleOnPrimary,
            background = AccessibleColors.AccessiblePrimary,
            elementDescription = "Primary container text"
        )
        
        // Test secondary colors
        assertColorContrast(
            foreground = AccessibleColors.AccessibleSecondary,
            background = androidx.compose.ui.graphics.Color.White,
            elementDescription = "Secondary color on white background"
        )
        
        // Test error colors
        assertColorContrast(
            foreground = AccessibleColors.AccessibleError,
            background = androidx.compose.ui.graphics.Color.White,
            elementDescription = "Error color on white background"
        )
        
        // Test warning colors
        assertColorContrast(
            foreground = AccessibleColors.AccessibleWarning,
            background = androidx.compose.ui.graphics.Color.White,
            elementDescription = "Warning color on white background"
        )
        
        // Test success colors
        assertColorContrast(
            foreground = AccessibleColors.AccessibleSuccess,
            background = androidx.compose.ui.graphics.Color.White,
            elementDescription = "Success color on white background"
        )
        
        // Test meal score colors
        assertColorContrast(
            foreground = AccessibleColors.AccessibleScoreA,
            background = androidx.compose.ui.graphics.Color.White,
            elementDescription = "Score A color on white background"
        )
        
        assertColorContrast(
            foreground = AccessibleColors.AccessibleScoreE,
            background = androidx.compose.ui.graphics.Color.White,
            elementDescription = "Score E color on white background"
        )
    }
    
    @Test
    fun testAccessibleButtonComponents() {
        composeTestRule.setContent {
            WellTrackTheme {
                AccessibleButton(
                    onClick = { },
                    contentDescription = "Test button for accessibility"
                ) {
                    androidx.compose.material3.Text("Test Button")
                }
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
    
    @Test
    fun testAccessibleTextFieldComponents() {
        composeTestRule.setContent {
            WellTrackTheme {
                AccessibleTextField(
                    value = "",
                    onValueChange = { },
                    label = "Test Field",
                    required = true,
                    helperText = "This is a test field"
                )
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
    
    @Test
    fun testAccessibleCheckboxComponents() {
        composeTestRule.setContent {
            WellTrackTheme {
                AccessibleCheckbox(
                    checked = false,
                    onCheckedChange = { },
                    label = "Test Checkbox",
                    description = "This is a test checkbox"
                )
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
    
    @Test
    fun testAccessibleRadioGroupComponents() {
        composeTestRule.setContent {
            WellTrackTheme {
                AccessibleRadioGroup(
                    options = listOf("Option 1", "Option 2", "Option 3"),
                    selectedOption = "Option 1",
                    onOptionSelected = { },
                    label = "Test Radio Group",
                    descriptions = mapOf(
                        "Option 1" to "First option",
                        "Option 2" to "Second option",
                        "Option 3" to "Third option"
                    )
                )
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
    
    @Test
    fun testAccessibleAlertComponents() {
        composeTestRule.setContent {
            WellTrackTheme {
                androidx.compose.foundation.layout.Column {
                    AccessibleAlert(
                        message = "This is a success message",
                        type = AlertType.SUCCESS,
                        title = "Success"
                    )
                    
                    AccessibleAlert(
                        message = "This is an error message",
                        type = AlertType.ERROR,
                        title = "Error"
                    )
                    
                    AccessibleAlert(
                        message = "This is a warning message",
                        type = AlertType.WARNING,
                        title = "Warning"
                    )
                    
                    AccessibleAlert(
                        message = "This is an info message",
                        type = AlertType.INFO,
                        title = "Information"
                    )
                }
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
    
    @Test
    fun testAccessibleSliderComponents() {
        composeTestRule.setContent {
            WellTrackTheme {
                AccessibleSlider(
                    value = 0.5f,
                    onValueChange = { },
                    label = "Test Slider",
                    valueRange = 0f..1f,
                    valueFormatter = { "${(it * 100).toInt()}" },
                    unit = "%"
                )
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
    
    @Test
    fun testHighContrastTheme() {
        composeTestRule.setContent {
            WellTrackTheme(highContrast = true) {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text("High contrast text")
                    
                    AccessibleButton(
                        onClick = { },
                        contentDescription = "High contrast button"
                    ) {
                        androidx.compose.material3.Text("Button")
                    }
                    
                    AccessibleTextField(
                        value = "",
                        onValueChange = { },
                        label = "High contrast field"
                    )
                }
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
    
    @Test
    fun testKeyboardNavigation() {
        composeTestRule.setContent {
            WellTrackTheme {
                KeyboardNavigationProvider {
                    androidx.compose.foundation.layout.Column {
                        AccessibleButton(
                            onClick = { },
                            contentDescription = "First button"
                        ) {
                            androidx.compose.material3.Text("Button 1")
                        }
                        
                        AccessibleButton(
                            onClick = { },
                            contentDescription = "Second button"
                        ) {
                            androidx.compose.material3.Text("Button 2")
                        }
                        
                        AccessibleTextField(
                            value = "",
                            onValueChange = { },
                            label = "Text field"
                        )
                    }
                }
            }
        }
        
        composeTestRule.performAccessibilityAudit()
    }
}