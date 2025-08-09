package com.beaconledger.welltrack.presentation.ingredients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.data.model.PreferenceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIngredientPreferenceDialog(
    onDismiss: () -> Unit,
    onAddPreferred: (String, Int) -> Unit,
    onAddDisliked: (String, String?) -> Unit,
    onAddAllergic: (String, String?) -> Unit
) {
    var ingredientName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(PreferenceType.PREFERRED) }
    var priority by remember { mutableStateOf("5") }
    var notes by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add Ingredient Preference",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Ingredient name input
                OutlinedTextField(
                    value = ingredientName,
                    onValueChange = { ingredientName = it },
                    label = { Text("Ingredient Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )
                
                // Preference type selection
                Text(
                    text = "Preference Type",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                PreferenceType.values().forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Priority input (only for preferred ingredients)
                if (selectedType == PreferenceType.PREFERRED) {
                    OutlinedTextField(
                        value = priority,
                        onValueChange = { priority = it },
                        label = { Text("Priority (1-10)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                
                // Notes input
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    maxLines = 3
                )
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            if (ingredientName.isNotBlank()) {
                                when (selectedType) {
                                    PreferenceType.PREFERRED -> {
                                        val priorityValue = priority.toIntOrNull() ?: 5
                                        onAddPreferred(ingredientName.trim(), priorityValue)
                                    }
                                    PreferenceType.DISLIKED -> {
                                        onAddDisliked(ingredientName.trim(), notes.takeIf { it.isNotBlank() })
                                    }
                                    PreferenceType.ALLERGIC -> {
                                        onAddAllergic(ingredientName.trim(), notes.takeIf { it.isNotBlank() })
                                    }
                                    PreferenceType.NEUTRAL -> {
                                        // Handle neutral if needed
                                    }
                                }
                            }
                        },
                        enabled = ingredientName.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}