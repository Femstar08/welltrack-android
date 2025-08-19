package com.beaconledger.welltrack.presentation.mealprep

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateLeftoverDialog(
    onDismiss: () -> Unit,
    onCreateLeftover: (
        name: String,
        quantity: Double,
        unit: String,
        storageLocation: StorageLocation,
        containerType: String,
        shelfLifeDays: Int,
        notes: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("portions") }
    var selectedStorageLocation by remember { mutableStateOf(StorageLocation.REFRIGERATOR) }
    var selectedContainerType by remember { mutableStateOf(ContainerTypeEnum.GLASS_CONTAINER) }
    var shelfLifeDays by remember { mutableStateOf("3") }
    var notes by remember { mutableStateOf("") }

    val units = listOf("portions", "cups", "grams", "ounces", "pieces", "servings")
    val storageLocations = StorageLocation.values().toList()
    val containerTypes = ContainerTypeEnum.values().toList()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Create Leftover",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Leftover Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    singleLine = true
                )

                // Quantity and Unit
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = { quantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    var unitExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = unitExpanded,
                        onExpandedChange = { unitExpanded = !unitExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = unitExpanded,
                            onDismissRequest = { unitExpanded = false }
                        ) {
                            units.forEach { unitOption ->
                                DropdownMenuItem(
                                    text = { Text(unitOption) },
                                    onClick = {
                                        unit = unitOption
                                        unitExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Storage Location
                Text(
                    text = "Storage Location",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    storageLocations.forEach { location ->
                        FilterChip(
                            onClick = { selectedStorageLocation = location },
                            label = { Text(location.displayName) },
                            selected = selectedStorageLocation == location,
                            leadingIcon = {
                                Icon(
                                    imageVector = getStorageLocationIcon(location),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                // Container Type
                Text(
                    text = "Container Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                var containerExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = containerExpanded,
                    onExpandedChange = { containerExpanded = !containerExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    OutlinedTextField(
                        value = selectedContainerType.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Container") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = containerExpanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = containerExpanded,
                        onDismissRequest = { containerExpanded = false }
                    ) {
                        containerTypes.forEach { containerType ->
                            DropdownMenuItem(
                                text = { Text(containerType.displayName) },
                                onClick = {
                                    selectedContainerType = containerType
                                    containerExpanded = false
                                }
                            )
                        }
                    }
                }

                // Shelf Life
                OutlinedTextField(
                    value = shelfLifeDays,
                    onValueChange = { shelfLifeDays = it },
                    label = { Text("Shelf Life (days)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    maxLines = 3
                )

                // Buttons
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
                            val quantityValue = quantity.toDoubleOrNull() ?: 0.0
                            val shelfLife = shelfLifeDays.toIntOrNull() ?: 3
                            onCreateLeftover(
                                name,
                                quantityValue,
                                unit,
                                selectedStorageLocation,
                                selectedContainerType.displayName,
                                shelfLife,
                                notes.takeIf { it.isNotBlank() }
                            )
                        },
                        enabled = name.isNotBlank() && quantity.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

private fun getStorageLocationIcon(location: StorageLocation): androidx.compose.ui.graphics.vector.ImageVector {
    return when (location) {
        StorageLocation.REFRIGERATOR -> Icons.Default.Place
        StorageLocation.FREEZER -> Icons.Default.Place
        StorageLocation.PANTRY -> Icons.Default.Place
        StorageLocation.COUNTER -> Icons.Default.Home
    }
}