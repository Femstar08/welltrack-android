package com.beaconledger.welltrack.presentation.dietary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.data.model.*

@Composable
fun SeveritySelectionDialog(
    title: String,
    currentSeverity: RestrictionSeverity,
    severityOptions: List<RestrictionSeverity>,
    onSeveritySelected: (RestrictionSeverity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSeverity by remember { mutableStateOf(currentSeverity) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Select the severity level for this restriction:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                severityOptions.forEach { severity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSeverity == severity,
                                onClick = { selectedSeverity = severity }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeverity == severity,
                            onClick = { selectedSeverity = severity }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = severity.displayName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = getSeverityDescription(severity),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSeveritySelected(selectedSeverity) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AllergySeveritySelectionDialog(
    allergen: String,
    currentSeverity: AllergySeverity,
    onSeveritySelected: (AllergySeverity) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSeverity by remember { mutableStateOf(currentSeverity) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Allergy Severity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Select the severity level for $allergen:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                AllergySeverity.values().forEach { severity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSeverity == severity,
                                onClick = { selectedSeverity = severity }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeverity == severity,
                            onClick = { selectedSeverity = severity }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = severity.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                color = when (severity) {
                                    AllergySeverity.MILD -> MaterialTheme.colorScheme.primary
                                    AllergySeverity.MODERATE -> MaterialTheme.colorScheme.tertiary
                                    AllergySeverity.SEVERE -> MaterialTheme.colorScheme.error
                                    AllergySeverity.ANAPHYLAXIS -> MaterialTheme.colorScheme.error
                                }
                            )
                            Text(
                                text = getAllergySeverityDescription(severity),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSeveritySelected(selectedSeverity) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddAllergyDialog(
    onAddAllergy: (String, AllergySeverity) -> Unit,
    onDismiss: () -> Unit
) {
    var allergenName by remember { mutableStateOf("") }
    var selectedSeverity by remember { mutableStateOf(AllergySeverity.MODERATE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Allergy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = allergenName,
                    onValueChange = { allergenName = it },
                    label = { Text("Allergen Name") },
                    placeholder = { Text("e.g., Peanuts, Shellfish, Dairy") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Severity Level:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AllergySeverity.values().forEach { severity ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedSeverity == severity,
                                onClick = { selectedSeverity = severity }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedSeverity == severity,
                            onClick = { selectedSeverity = severity }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = severity.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (allergenName.isNotBlank()) {
                        onAddAllergy(allergenName.trim(), selectedSeverity)
                    }
                },
                enabled = allergenName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddPreferenceDialog(
    title: String,
    onAddPreference: (String, PreferenceLevel) -> Unit,
    onDismiss: () -> Unit
) {
    var itemName by remember { mutableStateOf("") }
    var selectedPreference by remember { mutableStateOf(PreferenceLevel.LIKE) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Name") },
                    placeholder = { Text("Enter name...") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Preference Level:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PreferenceLevel.values().forEach { preference ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedPreference == preference,
                                onClick = { selectedPreference = preference }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPreference == preference,
                            onClick = { selectedPreference = preference }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = preference.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (preference) {
                                PreferenceLevel.LOVE -> MaterialTheme.colorScheme.primary
                                PreferenceLevel.LIKE -> MaterialTheme.colorScheme.primary
                                PreferenceLevel.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                                PreferenceLevel.DISLIKE -> MaterialTheme.colorScheme.error
                                PreferenceLevel.HATE -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (itemName.isNotBlank()) {
                        onAddPreference(itemName.trim(), selectedPreference)
                    }
                },
                enabled = itemName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PreferenceLevelSelectionDialog(
    item: String,
    currentPreference: PreferenceLevel,
    onPreferenceSelected: (PreferenceLevel) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPreference by remember { mutableStateOf(currentPreference) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Preference Level",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "How do you feel about $item?",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                PreferenceLevel.values().forEach { preference ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedPreference == preference,
                                onClick = { selectedPreference = preference }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPreference == preference,
                            onClick = { selectedPreference = preference }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = preference.displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = when (preference) {
                                PreferenceLevel.LOVE -> MaterialTheme.colorScheme.primary
                                PreferenceLevel.LIKE -> MaterialTheme.colorScheme.primary
                                PreferenceLevel.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                                PreferenceLevel.DISLIKE -> MaterialTheme.colorScheme.error
                                PreferenceLevel.HATE -> MaterialTheme.colorScheme.error
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onPreferenceSelected(selectedPreference) }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DietaryCompatibilityDialog(
    compatibility: DietaryCompatibility,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Dietary Compatibility",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Compatibility Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Compatibility Score:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "${(compatibility.score * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (compatibility.isCompatible) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Violations
                if (compatibility.violations.isNotEmpty()) {
                    Text(
                        text = "Violations:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(compatibility.violations) { violation ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = violation.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    if (violation.affectedIngredients.isNotEmpty()) {
                                        Text(
                                            text = "Affected: ${violation.affectedIngredients.joinToString(", ")}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onErrorContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Warnings
                if (compatibility.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Warnings:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(compatibility.warnings) { warning ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    Text(
                                        text = warning.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    warning.suggestion?.let { suggestion ->
                                        Text(
                                            text = "Suggestion: $suggestion",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

// Helper functions for descriptions
private fun getSeverityDescription(severity: RestrictionSeverity): String {
    return when (severity) {
        RestrictionSeverity.MILD -> "Flexible approach, occasional exceptions okay"
        RestrictionSeverity.MODERATE -> "Usually avoid, but not strict"
        RestrictionSeverity.STRICT -> "Always avoid, no exceptions"
        RestrictionSeverity.MEDICAL -> "Medical requirement, must avoid"
    }
}

private fun getAllergySeverityDescription(severity: AllergySeverity): String {
    return when (severity) {
        AllergySeverity.MILD -> "Minor discomfort or digestive issues"
        AllergySeverity.MODERATE -> "Noticeable symptoms, avoid when possible"
        AllergySeverity.SEVERE -> "Serious reaction, always avoid"
        AllergySeverity.ANAPHYLAXIS -> "Life-threatening, emergency medical attention required"
    }
}