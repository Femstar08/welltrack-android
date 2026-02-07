package com.beaconledger.welltrack.presentation.accessibility

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.accessibility.*

/**
 * Screen for configuring accessibility settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessibilitySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccessibilitySettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val accessibilitySettings = rememberAccessibilitySettings()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Accessibility Settings",
                        modifier = Modifier.semantics {
                            heading()
                        }
                    ) 
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Go back"
                            role = Role.Button
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .semantics {
                    contentDescription = "Accessibility settings page"
                },
            verticalArrangement = Arrangement.spacedBy(accessibilitySettings.recommendedSpacing)
        ) {
            // Current accessibility status
            AccessibilityStatusCard(accessibilitySettings)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Visual accessibility settings
            VisualAccessibilitySection(
                uiState = uiState,
                onHighContrastToggle = viewModel::toggleHighContrast,
                onReduceAnimationsToggle = viewModel::toggleReduceAnimations,
                onLargeTextToggle = viewModel::toggleLargeText
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Audio accessibility settings
            AudioAccessibilitySection(
                uiState = uiState,
                onScreenReaderOptimizationToggle = viewModel::toggleScreenReaderOptimization,
                onAudioDescriptionsToggle = viewModel::toggleAudioDescriptions
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Motor accessibility settings
            MotorAccessibilitySection(
                uiState = uiState,
                onLargeTouchTargetsToggle = viewModel::toggleLargeTouchTargets,
                onReduceMotionToggle = viewModel::toggleReduceMotion
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Cognitive accessibility settings
            CognitiveAccessibilitySection(
                uiState = uiState,
                onSimplifiedUIToggle = viewModel::toggleSimplifiedUI,
                onExtendedTimeoutsToggle = viewModel::toggleExtendedTimeouts
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Test accessibility features
            AccessibilityTestSection()
        }
    }
}

@Composable
private fun AccessibilityStatusCard(accessibilitySettings: UIAccessibilitySettings) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Current accessibility status"
                role = Role.Text
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Accessibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Accessibility Status",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            
            StatusItem(
                label = "TalkBack",
                isEnabled = accessibilitySettings.isTalkBackEnabled,
                description = if (accessibilitySettings.isTalkBackEnabled) "Active" else "Not active"
            )
            
            StatusItem(
                label = "Large Text",
                isEnabled = accessibilitySettings.isLargeTextEnabled,
                description = "Font scale: ${String.format("%.1f", accessibilitySettings.fontScale)}x"
            )
            
            StatusItem(
                label = "High Contrast",
                isEnabled = accessibilitySettings.isHighContrastEnabled,
                description = if (accessibilitySettings.isHighContrastEnabled) "Enabled" else "Disabled"
            )
            
            StatusItem(
                label = "Reduce Animations",
                isEnabled = accessibilitySettings.shouldReduceAnimations,
                description = if (accessibilitySettings.shouldReduceAnimations) "Enabled" else "Disabled"
            )
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    isEnabled: Boolean,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "$label: $description"
                role = Role.Text
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = if (isEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeUp,
                contentDescription = null,
                tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VisualAccessibilitySection(
    uiState: AccessibilitySettingsUiState,
    onHighContrastToggle: () -> Unit,
    onReduceAnimationsToggle: () -> Unit,
    onLargeTextToggle: () -> Unit
) {
    AccessibilitySection(
        title = "Visual",
        icon = Icons.Default.Contrast
    ) {
        AccessibleCheckbox(
            checked = uiState.highContrastEnabled,
            onCheckedChange = { onHighContrastToggle() },
            label = "High Contrast Mode",
            description = "Increases color contrast for better visibility"
        )
        
        AccessibleCheckbox(
            checked = uiState.reduceAnimationsEnabled,
            onCheckedChange = { onReduceAnimationsToggle() },
            label = "Reduce Animations",
            description = "Minimizes motion and transitions"
        )
        
        AccessibleCheckbox(
            checked = uiState.largeTextEnabled,
            onCheckedChange = { onLargeTextToggle() },
            label = "Force Large Text",
            description = "Override system font size settings"
        )
    }
}

@Composable
private fun AudioAccessibilitySection(
    uiState: AccessibilitySettingsUiState,
    onScreenReaderOptimizationToggle: () -> Unit,
    onAudioDescriptionsToggle: () -> Unit
) {
    AccessibilitySection(
        title = "Audio",
        icon = Icons.Default.VolumeUp
    ) {
        AccessibleCheckbox(
            checked = uiState.screenReaderOptimizationEnabled,
            onCheckedChange = { onScreenReaderOptimizationToggle() },
            label = "Screen Reader Optimization",
            description = "Optimize interface for TalkBack and other screen readers"
        )
        
        AccessibleCheckbox(
            checked = uiState.audioDescriptionsEnabled,
            onCheckedChange = { onAudioDescriptionsToggle() },
            label = "Audio Descriptions",
            description = "Provide audio descriptions for visual content"
        )
    }
}

@Composable
private fun MotorAccessibilitySection(
    uiState: AccessibilitySettingsUiState,
    onLargeTouchTargetsToggle: () -> Unit,
    onReduceMotionToggle: () -> Unit
) {
    AccessibilitySection(
        title = "Motor",
        icon = Icons.Default.Accessibility
    ) {
        AccessibleCheckbox(
            checked = uiState.largeTouchTargetsEnabled,
            onCheckedChange = { onLargeTouchTargetsToggle() },
            label = "Large Touch Targets",
            description = "Increase size of buttons and interactive elements"
        )
        
        AccessibleCheckbox(
            checked = uiState.reduceMotionEnabled,
            onCheckedChange = { onReduceMotionToggle() },
            label = "Reduce Motion",
            description = "Minimize parallax and motion effects"
        )
    }
}

@Composable
private fun CognitiveAccessibilitySection(
    uiState: AccessibilitySettingsUiState,
    onSimplifiedUIToggle: () -> Unit,
    onExtendedTimeoutsToggle: () -> Unit
) {
    AccessibilitySection(
        title = "Cognitive",
        icon = Icons.Default.TextFields
    ) {
        AccessibleCheckbox(
            checked = uiState.simplifiedUIEnabled,
            onCheckedChange = { onSimplifiedUIToggle() },
            label = "Simplified Interface",
            description = "Reduce visual complexity and distractions"
        )
        
        AccessibleCheckbox(
            checked = uiState.extendedTimeoutsEnabled,
            onCheckedChange = { onExtendedTimeoutsToggle() },
            label = "Extended Timeouts",
            description = "Allow more time for interactions and form completion"
        )
    }
}

@Composable
private fun AccessibilitySection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics { heading() }
                )
            }
            
            content()
        }
    }
}

@Composable
private fun AccessibilityTestSection() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Test Accessibility Features",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() }
            )
            
            Text(
                "Use these buttons to test accessibility features:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AccessibleButton(
                    onClick = { /* Test TalkBack announcement */ },
                    contentDescription = "Test screen reader announcement",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test TalkBack")
                }
                
                AccessibleButton(
                    onClick = { /* Test focus navigation */ },
                    contentDescription = "Test keyboard navigation",
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Test Focus")
                }
            }
        }
    }
}