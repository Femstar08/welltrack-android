package com.beaconledger.welltrack.accessibility

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Accessible button component with proper touch targets and semantic labels
 */
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    role: Role = Role.Button,
    content: @Composable RowScope.() -> Unit
) {
    val accessibilitySettings = rememberAccessibilitySettings()
    
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = accessibilitySettings.minimumTouchTargetSize)
            .semantics {
                this.role = role
                contentDescription?.let { this.contentDescription = it }
                if (!enabled) {
                    disabled()
                }
            },
        content = content
    )
}

/**
 * Accessible text field with proper labels and error handling
 */
@Composable
fun AccessibleTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    required: Boolean = false,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    val accessibilitySettings = rememberAccessibilitySettings()
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { 
                Text(
                    text = if (required) "$label *" else label,
                    fontSize = (14 * accessibilitySettings.fontScale).sp
                ) 
            },
            placeholder = placeholder?.let { { Text(it) } },
            isError = isError,
            singleLine = singleLine,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = accessibilitySettings.minimumTouchTargetSize)
                .semantics {
                    this.contentDescription = buildString {
                        append(label)
                        if (required) append(", required field")
                        if (isError && errorMessage != null) {
                            append(", error: $errorMessage")
                        }
                        helperText?.let { append(", $it") }
                    }
                    if (isError) {
                        this.error(errorMessage ?: "Invalid input")
                    }
                }
        )
        
        // Error message
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = (12 * accessibilitySettings.fontScale).sp,
                modifier = Modifier.semantics {
                    this.role = Role.Text
                    this.contentDescription = "Error: $errorMessage"
                }
            )
        }
        
        // Helper text
        if (helperText != null && !isError) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = (12 * accessibilitySettings.fontScale).sp,
                modifier = Modifier.semantics {
                    this.role = Role.Text
                    this.contentDescription = "Help: $helperText"
                }
            )
        }
    }
}

/**
 * Accessible checkbox with proper labels and touch targets
 */
@Composable
fun AccessibleCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String? = null
) {
    val accessibilitySettings = rememberAccessibilitySettings()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = accessibilitySettings.minimumTouchTargetSize)
            .clickable(
                enabled = enabled,
                role = Role.Checkbox,
                onClickLabel = if (checked) "Uncheck $label" else "Check $label"
            ) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp)
            .semantics(mergeDescendants = true) {
                this.role = Role.Checkbox
                this.contentDescription = buildString {
                    append(label)
                    append(if (checked) ", checked" else ", unchecked")
                    description?.let { append(", $it") }
                }
                this.stateDescription = if (checked) "Checked" else "Unchecked"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // Handled by row click
            enabled = enabled,
            modifier = Modifier.size(accessibilitySettings.minimumTouchTargetSize)
        )
        
        Spacer(modifier = Modifier.width(accessibilitySettings.recommendedSpacing))
        
        Column {
            Text(
                text = label,
                fontSize = (16 * accessibilitySettings.fontScale).sp,
                fontWeight = FontWeight.Medium
            )
            
            if (description != null) {
                Text(
                    text = description,
                    fontSize = (14 * accessibilitySettings.fontScale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Accessible radio button group with proper navigation
 */
@Composable
fun AccessibleRadioGroup(
    options: List<String>,
    selectedOption: String?,
    onOptionSelected: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    descriptions: Map<String, String> = emptyMap()
) {
    val accessibilitySettings = rememberAccessibilitySettings()
    
    Column(
        modifier = modifier.semantics {
            this.contentDescription = "$label, radio group with ${options.size} options"
        }
    ) {
        Text(
            text = label,
            fontSize = (16 * accessibilitySettings.fontScale).sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        options.forEach { option ->
            val isSelected = option == selectedOption
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = accessibilitySettings.minimumTouchTargetSize)
                    .selectable(
                        selected = isSelected,
                        enabled = enabled,
                        role = Role.RadioButton,
                        onClick = { onOptionSelected(option) }
                    )
                    .padding(vertical = 4.dp)
                    .semantics {
                        this.role = Role.RadioButton
                        this.contentDescription = buildString {
                            append(option)
                            append(if (isSelected) ", selected" else ", not selected")
                            descriptions[option]?.let { append(", $it") }
                        }
                        this.stateDescription = if (isSelected) "Selected" else "Not selected"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSelected,
                    onClick = null, // Handled by row selection
                    enabled = enabled,
                    modifier = Modifier.size(accessibilitySettings.minimumTouchTargetSize)
                )
                
                Spacer(modifier = Modifier.width(accessibilitySettings.recommendedSpacing))
                
                Column {
                    Text(
                        text = option,
                        fontSize = (16 * accessibilitySettings.fontScale).sp
                    )
                    
                    descriptions[option]?.let { description ->
                        Text(
                            text = description,
                            fontSize = (14 * accessibilitySettings.fontScale).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Accessible card component with proper focus and semantic information
 */
@Composable
fun AccessibleCard(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
    role: Role = Role.Button,
    content: @Composable ColumnScope.() -> Unit
) {
    val accessibilitySettings = rememberAccessibilitySettings()
    val interactionSource = remember { MutableInteractionSource() }
    
    Card(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(
                            enabled = enabled,
                            role = role,
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick
                        )
                        .semantics {
                            this.role = role
                            contentDescription?.let { this.contentDescription = it }
                            if (!enabled) disabled()
                        }
                } else {
                    Modifier
                }
            )
            .heightIn(min = if (onClick != null) accessibilitySettings.minimumTouchTargetSize else 0.dp),
        content = content
    )
}

/**
 * Accessible alert/status message component
 */
@Composable
fun AccessibleAlert(
    message: String,
    type: AlertType = AlertType.INFO,
    modifier: Modifier = Modifier,
    onDismiss: (() -> Unit)? = null,
    title: String? = null
) {
    val accessibilitySettings = rememberAccessibilitySettings()
    
    val (icon, containerColor, contentColor) = when (type) {
        AlertType.SUCCESS -> Triple(
            Icons.Default.Check,
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        AlertType.WARNING -> Triple(
            Icons.Default.Warning,
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        AlertType.ERROR -> Triple(
            Icons.Default.Error,
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        AlertType.INFO -> Triple(
            Icons.Default.Info,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                this.role = Role.Text
                this.contentDescription = buildString {
                    append("${type.name.lowercase()} alert")
                    title?.let { append(", $it") }
                    append(", $message")
                }
                this.liveRegion = LiveRegionMode.Polite
            },
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null, // Handled by card semantics
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (title != null) {
                    Text(
                        text = title,
                        fontSize = (16 * accessibilitySettings.fontScale).sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Text(
                    text = message,
                    fontSize = (14 * accessibilitySettings.fontScale).sp
                )
            }
            
            if (onDismiss != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(accessibilitySettings.minimumTouchTargetSize)
                        .semantics {
                            this.contentDescription = "Dismiss alert"
                            this.role = Role.Button
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check, // Using Check as close icon
                        contentDescription = null
                    )
                }
            }
        }
    }
}

enum class AlertType {
    SUCCESS, WARNING, ERROR, INFO
}

/**
 * Accessible slider component with proper labels and value announcements
 */
@Composable
fun AccessibleSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    valueFormatter: (Float) -> String = { it.toString() },
    unit: String = ""
) {
    val accessibilitySettings = rememberAccessibilitySettings()
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = (16 * accessibilitySettings.fontScale).sp,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${valueFormatter(value)} $unit".trim(),
                fontSize = (14 * accessibilitySettings.fontScale).sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = accessibilitySettings.minimumTouchTargetSize)
                .semantics {
                    this.contentDescription = buildString {
                        append("$label slider")
                        append(", current value ${valueFormatter(value)} $unit".trim())
                        append(", range from ${valueFormatter(valueRange.start)} to ${valueFormatter(valueRange.endInclusive)} $unit".trim())
                    }
                    this.stateDescription = "${valueFormatter(value)} $unit".trim()
                }
        )
    }
}