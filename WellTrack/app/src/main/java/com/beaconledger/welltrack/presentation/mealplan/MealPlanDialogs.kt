package com.beaconledger.welltrack.presentation.mealplan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.beaconledger.welltrack.domain.usecase.MealPrepSchedule
import com.beaconledger.welltrack.domain.usecase.MealPrepTask

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanPreferencesDialog(
    onDismiss: () -> Unit,
    onSave: (MealPlanPreferences) -> Unit,
    initialPreferences: MealPlanPreferences = MealPlanPreferences()
) {
    var targetCalories by remember { mutableStateOf(initialPreferences.targetCalories?.toString() ?: "") }
    var targetProtein by remember { mutableStateOf(initialPreferences.targetProtein?.toString() ?: "") }
    var targetCarbs by remember { mutableStateOf(initialPreferences.targetCarbs?.toString() ?: "") }
    var targetFat by remember { mutableStateOf(initialPreferences.targetFat?.toString() ?: "") }
    var cookingTimePreference by remember { mutableStateOf(initialPreferences.cookingTimePreference) }
    var varietyLevel by remember { mutableStateOf(initialPreferences.varietyLevel) }
    var preferredIngredients by remember { mutableStateOf(initialPreferences.preferredIngredients.joinToString(", ")) }
    var avoidedIngredients by remember { mutableStateOf(initialPreferences.avoidedIngredients.joinToString(", ")) }
    var mealPrepDays by remember { mutableStateOf(initialPreferences.mealPrepDays.toSet()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Meal Plan Preferences",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Nutrition targets
                    item {
                        Text(
                            text = "Nutrition Targets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = targetCalories,
                                onValueChange = { targetCalories = it },
                                label = { Text("Calories") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = targetProtein,
                                onValueChange = { targetProtein = it },
                                label = { Text("Protein (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = targetCarbs,
                                onValueChange = { targetCarbs = it },
                                label = { Text("Carbs (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            
                            OutlinedTextField(
                                value = targetFat,
                                onValueChange = { targetFat = it },
                                label = { Text("Fat (g)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Cooking preferences
                    item {
                        Text(
                            text = "Cooking Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    item {
                        Column {
                            Text(
                                text = "Cooking Time Preference",
                                style = MaterialTheme.typography.labelLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            CookingTimePreference.values().forEach { preference ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = cookingTimePreference == preference,
                                            onClick = { cookingTimePreference = preference }
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = cookingTimePreference == preference,
                                        onClick = { cookingTimePreference = preference }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = when (preference) {
                                            CookingTimePreference.QUICK -> "Quick (Under 30 min)"
                                            CookingTimePreference.MODERATE -> "Moderate (30-60 min)"
                                            CookingTimePreference.EXTENDED -> "Extended (Over 60 min)"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Column {
                            Text(
                                text = "Variety Level",
                                style = MaterialTheme.typography.labelLarge
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            VarietyLevel.values().forEach { level ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = varietyLevel == level,
                                            onClick = { varietyLevel = level }
                                        )
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = varietyLevel == level,
                                        onClick = { varietyLevel = level }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = when (level) {
                                            VarietyLevel.LOW -> "Low (Repeat meals frequently)"
                                            VarietyLevel.MODERATE -> "Moderate (Some repetition)"
                                            VarietyLevel.HIGH -> "High (Maximum variety)"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Ingredients
                    item {
                        Text(
                            text = "Ingredient Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = preferredIngredients,
                            onValueChange = { preferredIngredients = it },
                            label = { Text("Preferred Ingredients") },
                            placeholder = { Text("chicken, rice, broccoli...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = avoidedIngredients,
                            onValueChange = { avoidedIngredients = it },
                            label = { Text("Avoided Ingredients") },
                            placeholder = { Text("nuts, dairy, gluten...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }

                    // Meal prep days
                    item {
                        Text(
                            text = "Meal Prep Days",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    item {
                        val daysOfWeek = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
                        
                        Column {
                            daysOfWeek.forEach { day ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = mealPrepDays.contains(day),
                                        onCheckedChange = { checked ->
                                            mealPrepDays = if (checked) {
                                                mealPrepDays + day
                                            } else {
                                                mealPrepDays - day
                                            }
                                        }
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(text = day)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                            val preferences = MealPlanPreferences(
                                targetCalories = targetCalories.toIntOrNull(),
                                targetProtein = targetProtein.toIntOrNull(),
                                targetCarbs = targetCarbs.toIntOrNull(),
                                targetFat = targetFat.toIntOrNull(),
                                preferredIngredients = preferredIngredients.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                avoidedIngredients = avoidedIngredients.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                cookingTimePreference = cookingTimePreference,
                                varietyLevel = varietyLevel,
                                mealPrepDays = mealPrepDays.toList()
                            )
                            onSave(preferences)
                        }
                    ) {
                        Text("Save & Generate")
                    }
                }
            }
        }
    }
}

@Composable
fun MealPrepScheduleDialog(
    schedule: MealPrepSchedule,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meal Prep Schedule",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total time summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Prep Time",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "${schedule.totalEstimatedTime / 60}h ${schedule.totalEstimatedTime % 60}m",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Prep tasks
                Text(
                    text = "Prep Tasks",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(schedule.prepTasks) { task ->
                        MealPrepTaskCard(task = task)
                    }
                }

                // Recommendations
                if (schedule.recommendations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Recommendations",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            schedule.recommendations.forEach { recommendation ->
                                Row(
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = recommendation,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                if (recommendation != schedule.recommendations.last()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got it!")
                }
            }
        }
    }
}

@Composable
private fun MealPrepTaskCard(
    task: MealPrepTask
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "Suggested: ${task.suggestedDay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${task.estimatedTime}min",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    AssistChip(
                        onClick = { },
                        label = { 
                            Text(
                                text = task.priority.name,
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = when (task.priority) {
                                com.beaconledger.welltrack.domain.usecase.MealPrepPriority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                com.beaconledger.welltrack.domain.usecase.MealPrepPriority.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer
                                com.beaconledger.welltrack.domain.usecase.MealPrepPriority.LOW -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    )
                }
            }
            
            if (task.recipes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Recipes: ${task.recipes.joinToString(", ") { it.name }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCustomMealDialog(
    mealType: MealType,
    onDismiss: () -> Unit,
    onSave: (String, Int, String?) -> Unit
) {
    var mealName by remember { mutableStateOf("") }
    var servings by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Add ${mealType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = mealName,
                    onValueChange = { mealName = it },
                    label = { Text("Meal Name") },
                    placeholder = { Text("e.g., Grilled Chicken Salad") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = servings,
                    onValueChange = { servings = it },
                    label = { Text("Servings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("Any additional notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                            if (mealName.isNotBlank()) {
                                onSave(
                                    mealName.trim(),
                                    servings.toIntOrNull() ?: 1,
                                    notes.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = mealName.isNotBlank()
                    ) {
                        Text("Add Meal")
                    }
                }
            }
        }
    }
}