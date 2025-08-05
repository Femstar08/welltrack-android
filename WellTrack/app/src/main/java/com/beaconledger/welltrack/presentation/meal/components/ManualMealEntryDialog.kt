package com.beaconledger.welltrack.presentation.meal.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.beaconledger.welltrack.data.model.Ingredient
import com.beaconledger.welltrack.data.model.IngredientCategory

@Composable
fun ManualMealEntryDialog(
    ingredients: List<Ingredient>,
    portions: Float,
    notes: String,
    onDismiss: () -> Unit,
    onAddIngredient: (Ingredient) -> Unit,
    onRemoveIngredient: (Int) -> Unit,
    onPortionsChange: (Float) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirm: (String) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var showAddIngredientDialog by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Manual Meal Entry",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal Name
                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Portions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Portions:",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { 
                                if (portions > 0.5f) onPortionsChange(portions - 0.5f) 
                            }
                        ) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                        }
                        
                        Text(
                            text = portions.toString(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        
                        IconButton(
                            onClick = { onPortionsChange(portions + 0.5f) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ingredients Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ingredients (${ingredients.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    TextButton(
                        onClick = { showAddIngredientDialog = true }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Ingredients List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(ingredients) { index, ingredient ->
                        IngredientItem(
                            ingredient = ingredient,
                            onRemove = { onRemoveIngredient(index) }
                        )
                    }
                    
                    if (ingredients.isEmpty()) {
                        item {
                            Text(
                                text = "No ingredients added yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { onConfirm(mealName.ifBlank { "Custom Meal" }) },
                        modifier = Modifier.weight(1f),
                        enabled = ingredients.isNotEmpty()
                    ) {
                        Text("Log Meal")
                    }
                }
            }
        }
    }
    
    if (showAddIngredientDialog) {
        AddIngredientDialog(
            onDismiss = { showAddIngredientDialog = false },
            onAddIngredient = { ingredient ->
                onAddIngredient(ingredient)
                showAddIngredientDialog = false
            }
        )
    }
}

@Composable
private fun IngredientItem(
    ingredient: Ingredient,
    onRemove: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${ingredient.quantity} ${ingredient.unit} • ${ingredient.category.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIngredientDialog(
    onDismiss: () -> Unit,
    onAddIngredient: (Ingredient) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("g") }
    var category by remember { mutableStateOf(IngredientCategory.OTHER) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }
    
    val units = listOf("g", "kg", "oz", "lb", "cup", "tbsp", "tsp", "ml", "l", "piece", "large", "medium", "small")
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Add Ingredient",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                    
                    ExposedDropdownMenuBox(
                        expanded = showUnitDropdown,
                        onExpandedChange = { showUnitDropdown = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Unit") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showUnitDropdown) },
                            modifier = Modifier.menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showUnitDropdown,
                            onDismissRequest = { showUnitDropdown = false }
                        ) {
                            units.forEach { unitOption ->
                                DropdownMenuItem(
                                    text = { Text(unitOption) },
                                    onClick = {
                                        unit = unitOption
                                        showUnitDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = it }
                ) {
                    OutlinedTextField(
                        value = category.displayName,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        IngredientCategory.values().forEach { categoryOption ->
                            DropdownMenuItem(
                                text = { Text(categoryOption.displayName) },
                                onClick = {
                                    category = categoryOption
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val quantityValue = quantity.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && quantityValue > 0) {
                                onAddIngredient(
                                    Ingredient(
                                        name = name,
                                        quantity = quantityValue,
                                        unit = unit,
                                        category = category
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && quantity.toDoubleOrNull() != null && quantity.toDoubleOrNull()!! > 0
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}