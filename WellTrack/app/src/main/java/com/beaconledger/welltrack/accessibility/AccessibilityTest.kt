package com.beaconledger.welltrack.accessibility

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Simple test to verify accessibility components compile correctly
 */
@Composable
fun AccessibilityTestScreen() {
    MaterialTheme {
        // Test basic accessibility components
        AccessibleButton(
            onClick = { },
            contentDescription = "Test button"
        ) {
            // Button content
        }

        AccessibleTextField(
            value = "",
            onValueChange = { },
            label = "Test field"
        )

        AccessibleCheckbox(
            checked = false,
            onCheckedChange = { },
            label = "Test checkbox"
        )

        AccessibleAlert(
            message = "Test alert",
            type = AlertType.INFO
        )

        AccessibleSlider(
            value = 0.5f,
            onValueChange = { },
            label = "Test slider"
        )
    }
}