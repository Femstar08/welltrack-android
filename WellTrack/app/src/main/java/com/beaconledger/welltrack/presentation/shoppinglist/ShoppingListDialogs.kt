package com.beaconledger.welltrack.presentation.shoppinglist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun CreateShoppingListDialog(
    onDismiss: () -> Unit,
    onConfirm: (ShoppingListCreateRequest) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Shopping List") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("List Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        val request = ShoppingListCreateRequest(
                            name = name.trim(),
                            description = description.takeIf { it.isNotBlank() }?.trim()
                        )
                        onConfirm(request)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
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
fun AddShoppingListItemDialog(
    name: String,
    quantity: String,
    unit: String,
    category: IngredientCategory,
    ingredientSuggestions: List<String>,
    onNameChange: (String) -> Unit,
    onQuantityChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onCategoryChange: (IngredientCategory) -> Unit,
    onSearchIngredients: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ShoppingListItemCreateRequest) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var estimatedCost by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(ShoppingItemPriority.NORMAL) }
    var showSuggestions by remember { mutableStateOf(false) }
    
    // Trigger search when name changes
    LaunchedEffect(name) {
        if (name.length >= 2) {
            onSearchIngredients(name)
            showSuggestions = true
        } else {
            showSuggestions = false
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add Item",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Item name with suggestions
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            onNameChange(it)
                            showSuggestions = it.length >= 2
                        },
                        label = { Text("Item Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    if (showSuggestions && ingredientSuggestions.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            LazyColumn {
                                items(ingredientSuggestions.take(5)) { suggestion ->
                                    TextButton(
                                        onClick = {
                                            onNameChange(suggestion)
                                            showSuggestions = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = suggestion,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Quantity and unit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantity,
                        onValueChange = onQuantityChange,
                        label = { Text("Quantity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = unit,
                        onValueChange = onUnitChange,
                        label = { Text("Unit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                
                // Category
                CategoryDropdown(
                    selectedCategory = category,
                    onCategorySelected = onCategoryChange
                )
                
                // Priority
                PriorityDropdown(
                    selectedPriority = priority,
                    onPrioritySelected = { priority = it }
                )
                
                // Estimated cost
                OutlinedTextField(
                    value = estimatedCost,
                    onValueChange = { estimatedCost = it },
                    label = { Text("Estimated Cost ($)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
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
                            if (name.isNotBlank() && quantity.isNotBlank() && unit.isNotBlank()) {
                                val request = ShoppingListItemCreateRequest(
                                    name = name.trim(),
                                    quantity = quantity.toDoubleOrNull() ?: 1.0,
                                    unit = unit.trim(),
                                    category = category,
                                    estimatedCost = estimatedCost.toDoubleOrNull(),
                                    notes = notes.takeIf { it.isNotBlank() }?.trim(),
                                    priority = priority
                                )
                                onConfirm(request)
                            }
                        },
                        enabled = name.isNotBlank() && quantity.isNotBlank() && unit.isNotBlank()
                    ) {
                        Text("Add Item")
                    }
                }
            }
        }
    }
}

@Composable
fun GenerateShoppingListDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, GenerationOptions) -> Unit
) {
    var mealPlanId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var includeExistingPantryItems by remember { mutableStateOf(false) }
    var consolidateSimilarItems by remember { mutableStateOf(true) }
    var excludeCategories by remember { mutableStateOf(emptyList<IngredientCategory>()) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Generate from Meal Plan",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                OutlinedTextField(
                    value = mealPlanId,
                    onValueChange = { mealPlanId = it },
                    label = { Text("Meal Plan ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Shopping List Name (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Options
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = includeExistingPantryItems,
                        onCheckedChange = { includeExistingPantryItems = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Include existing pantry items")
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = consolidateSimilarItems,
                        onCheckedChange = { consolidateSimilarItems = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Consolidate similar items")
                }
                
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
                            if (mealPlanId.isNotBlank()) {
                                val options = GenerationOptions(
                                    includeExistingPantryItems = includeExistingPantryItems,
                                    consolidateSimilarItems = consolidateSimilarItems,
                                    excludeCategories = excludeCategories
                                )
                                onConfirm(
                                    mealPlanId.trim(),
                                    name.takeIf { it.isNotBlank() }?.trim(),
                                    options
                                )
                            }
                        },
                        enabled = mealPlanId.isNotBlank()
                    ) {
                        Text("Generate")
                    }
                }
            }
        }
    }
}

@Composable
fun DuplicateShoppingListDialog(
    originalName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf("$originalName (Copy)") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Duplicate Shopping List") },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("New List Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank()) {
                        onConfirm(newName.trim())
                    }
                },
                enabled = newName.isNotBlank()
            ) {
                Text("Duplicate")
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
fun CategoryFilterDialog(
    selectedCategory: IngredientCategory?,
    onDismiss: () -> Unit,
    onCategorySelected: (IngredientCategory?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter by Category") },
        text = {
            LazyColumn {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == null,
                            onClick = { onCategorySelected(null) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("All Categories")
                    }
                }
                
                items(IngredientCategory.values()) { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { onCategorySelected(category) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(category.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

data class GenerationOptions(
    val includeExistingPantryItems: Boolean = false,
    val consolidateSimilarItems: Boolean = true,
    val excludeCategories: List<IngredientCategory> = emptyList()
)