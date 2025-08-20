package com.beaconledger.welltrack.presentation.macronutrients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
fun SetTargetsDialog(
    currentTargets: MacronutrientTarget?,
    onDismiss: () -> Unit,
    onSetTargets: (Int, Double, Double, Double, Double, Int) -> Unit,
    onCalculateProtein: (Double, ActivityLevel, FitnessGoal) -> Unit,
    onCalculateFiber: (Int, Gender) -> Unit,
    onCalculateWater: (Double, ActivityLevel) -> Unit
) {
    var calories by remember { mutableStateOf(currentTargets?.caloriesTarget?.toString() ?: "2000") }
    var protein by remember { mutableStateOf(currentTargets?.proteinGrams?.toString() ?: "150") }
    var carbs by remember { mutableStateOf(currentTargets?.carbsGrams?.toString() ?: "250") }
    var fat by remember { mutableStateOf(currentTargets?.fatGrams?.toString() ?: "67") }
    var fiber by remember { mutableStateOf(currentTargets?.fiberGrams?.toString() ?: "25") }
    var water by remember { mutableStateOf(currentTargets?.waterMl?.toString() ?: "2500") }
    
    var showProteinCalculator by remember { mutableStateOf(false) }
    var showFiberCalculator by remember { mutableStateOf(false) }
    var showWaterCalculator by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Set Daily Targets",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Calories
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    suffix = { Text("kcal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Protein with calculator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein") },
                        suffix = { Text("g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showProteinCalculator = true }) {
                        Text("Calculate")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Carbs
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbohydrates") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fat
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Fiber with calculator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = fiber,
                        onValueChange = { fiber = it },
                        label = { Text("Fiber") },
                        suffix = { Text("g") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showFiberCalculator = true }) {
                        Text("Calculate")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Water with calculator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = water,
                        onValueChange = { water = it },
                        label = { Text("Water") },
                        suffix = { Text("ml") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = { showWaterCalculator = true }) {
                        Text("Calculate")
                    }
                }

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
                            onSetTargets(
                                calories.toIntOrNull() ?: 2000,
                                protein.toDoubleOrNull() ?: 150.0,
                                carbs.toDoubleOrNull() ?: 250.0,
                                fat.toDoubleOrNull() ?: 67.0,
                                fiber.toDoubleOrNull() ?: 25.0,
                                water.toIntOrNull() ?: 2500
                            )
                        }
                    ) {
                        Text("Set Targets")
                    }
                }
            }
        }
    }

    // Protein Calculator Dialog
    if (showProteinCalculator) {
        ProteinCalculatorDialog(
            onDismiss = { showProteinCalculator = false },
            onCalculate = { bodyWeight, activityLevel, goal ->
                onCalculateProtein(bodyWeight, activityLevel, goal)
                showProteinCalculator = false
            },
            onResult = { calculatedProtein ->
                protein = calculatedProtein.toString()
            }
        )
    }

    // Fiber Calculator Dialog
    if (showFiberCalculator) {
        FiberCalculatorDialog(
            onDismiss = { showFiberCalculator = false },
            onCalculate = { age, gender ->
                onCalculateFiber(age, gender)
                showFiberCalculator = false
            },
            onResult = { calculatedFiber ->
                fiber = calculatedFiber.toString()
            }
        )
    }

    // Water Calculator Dialog
    if (showWaterCalculator) {
        WaterCalculatorDialog(
            onDismiss = { showWaterCalculator = false },
            onCalculate = { bodyWeight, activityLevel ->
                onCalculateWater(bodyWeight, activityLevel)
                showWaterCalculator = false
            },
            onResult = { calculatedWater ->
                water = calculatedWater.toString()
            }
        )
    }
}

@Composable
fun ProteinCalculatorDialog(
    onDismiss: () -> Unit,
    onCalculate: (Double, ActivityLevel, FitnessGoal) -> Unit,
    onResult: (Double) -> Unit
) {
    var bodyWeight by remember { mutableStateOf("70") }
    var selectedActivityLevel by remember { mutableStateOf(ActivityLevel.MODERATELY_ACTIVE) }
    var selectedGoal by remember { mutableStateOf(FitnessGoal.MAINTENANCE) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Calculate Protein Target",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bodyWeight,
                    onValueChange = { bodyWeight = it },
                    label = { Text("Body Weight") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Activity Level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                ActivityLevel.values().forEach { level ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedActivityLevel == level,
                            onClick = { selectedActivityLevel = level }
                        )
                        Text(
                            text = level.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Fitness Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                FitnessGoal.values().forEach { goal ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedGoal == goal,
                            onClick = { selectedGoal = goal }
                        )
                        Text(
                            text = goal.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

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
                            val weight = bodyWeight.toDoubleOrNull() ?: 70.0
                            onCalculate(weight, selectedActivityLevel, selectedGoal)
                        }
                    ) {
                        Text("Calculate")
                    }
                }
            }
        }
    }
}

@Composable
fun FiberCalculatorDialog(
    onDismiss: () -> Unit,
    onCalculate: (Int, Gender) -> Unit,
    onResult: (Double) -> Unit
) {
    var age by remember { mutableStateOf("30") }
    var selectedGender by remember { mutableStateOf(Gender.FEMALE) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Calculate Fiber Target",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = age,
                    onValueChange = { age = it },
                    label = { Text("Age") },
                    suffix = { Text("years") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Gender",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Gender.values().forEach { gender ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedGender == gender,
                            onClick = { selectedGender = gender }
                        )
                        Text(
                            text = gender.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

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
                            val userAge = age.toIntOrNull() ?: 30
                            onCalculate(userAge, selectedGender)
                        }
                    ) {
                        Text("Calculate")
                    }
                }
            }
        }
    }
}

@Composable
fun WaterCalculatorDialog(
    onDismiss: () -> Unit,
    onCalculate: (Double, ActivityLevel) -> Unit,
    onResult: (Int) -> Unit
) {
    var bodyWeight by remember { mutableStateOf("70") }
    var selectedActivityLevel by remember { mutableStateOf(ActivityLevel.MODERATELY_ACTIVE) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Calculate Water Target",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bodyWeight,
                    onValueChange = { bodyWeight = it },
                    label = { Text("Body Weight") },
                    suffix = { Text("kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Activity Level",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                ActivityLevel.values().forEach { level ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedActivityLevel == level,
                            onClick = { selectedActivityLevel = level }
                        )
                        Text(
                            text = level.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

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
                            val weight = bodyWeight.toDoubleOrNull() ?: 70.0
                            onCalculate(weight, selectedActivityLevel)
                        }
                    ) {
                        Text("Calculate")
                    }
                }
            }
        }
    }
}

@Composable
fun ManualNutrientEntryDialog(
    customNutrients: List<CustomNutrient>,
    onDismiss: () -> Unit,
    onLogNutrients: (Int, Double, Double, Double, Double, Int, Map<String, Double>) -> Unit
) {
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var water by remember { mutableStateOf("") }
    
    val customNutrientValues = remember { mutableStateMapOf<String, String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Manual Nutrient Entry",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Core nutrients
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories") },
                    suffix = { Text("kcal") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbohydrates") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fiber,
                    onValueChange = { fiber = it },
                    label = { Text("Fiber") },
                    suffix = { Text("g") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = water,
                    onValueChange = { water = it },
                    label = { Text("Water") },
                    suffix = { Text("ml") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Custom nutrients
                if (customNutrients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Custom Nutrients",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    customNutrients.forEach { nutrient ->
                        OutlinedTextField(
                            value = customNutrientValues[nutrient.name] ?: "",
                            onValueChange = { customNutrientValues[nutrient.name] = it },
                            label = { Text(nutrient.name) },
                            suffix = { Text(nutrient.unit) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

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
                            val customValues = customNutrientValues.mapValues { (_, value) ->
                                value.toDoubleOrNull() ?: 0.0
                            }.filterValues { it > 0.0 }
                            
                            onLogNutrients(
                                calories.toIntOrNull() ?: 0,
                                protein.toDoubleOrNull() ?: 0.0,
                                carbs.toDoubleOrNull() ?: 0.0,
                                fat.toDoubleOrNull() ?: 0.0,
                                fiber.toDoubleOrNull() ?: 0.0,
                                water.toIntOrNull() ?: 0,
                                customValues
                            )
                        }
                    ) {
                        Text("Log Nutrients")
                    }
                }
            }
        }
    }
}

@Composable
fun AddCustomNutrientDialog(
    onDismiss: () -> Unit,
    onAddNutrient: (String, String, Double?, NutrientCategory, NutrientPriority) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var targetValue by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(NutrientCategory.VITAMIN) }
    var selectedPriority by remember { mutableStateOf(NutrientPriority.OPTIONAL) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add Custom Nutrient",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nutrient Name") },
                    placeholder = { Text("e.g., Vitamin D") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit") },
                    placeholder = { Text("e.g., IU, mg, mcg") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it },
                    label = { Text("Target Value (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                NutrientCategory.values().forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                        Text(
                            text = category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                NutrientPriority.values().forEach { priority ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority }
                        )
                        Text(
                            text = priority.name.lowercase().replaceFirstChar { it.uppercase() },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

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
                            if (name.isNotBlank() && unit.isNotBlank()) {
                                onAddNutrient(
                                    name.trim(),
                                    unit.trim(),
                                    targetValue.toDoubleOrNull(),
                                    selectedCategory,
                                    selectedPriority
                                )
                            }
                        },
                        enabled = name.isNotBlank() && unit.isNotBlank()
                    ) {
                        Text("Add Nutrient")
                    }
                }
            }
        }
    }
}