package com.beaconledger.welltrack.presentation.supplements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.data.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSupplementDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String?, String, String, SupplementCategory, SupplementNutrition) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var servingSize by remember { mutableStateOf("") }
    var servingUnit by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SupplementCategory.VITAMIN) }
    var expanded by remember { mutableStateOf(false) }
    
    // Nutrition fields
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var vitaminD by remember { mutableStateOf("") }
    var vitaminB12 by remember { mutableStateOf("") }
    var vitaminC by remember { mutableStateOf("") }
    var calcium by remember { mutableStateOf("") }
    var iron by remember { mutableStateOf("") }
    var magnesium by remember { mutableStateOf("") }
    var zinc by remember { mutableStateOf("") }
    var omega3 by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Create New Supplement",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Basic Information
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = servingSize,
                        onValueChange = { servingSize = it },
                        label = { Text("Serving Size *") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = servingUnit,
                        onValueChange = { servingUnit = it },
                        label = { Text("Unit *") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Category Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SupplementCategory.values().forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedCategory = category
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Nutritional Information (per serving)",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Nutrition fields in a grid
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = calories,
                            onValueChange = { calories = it },
                            label = { Text("Calories") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Protein (g)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = vitaminD,
                            onValueChange = { vitaminD = it },
                            label = { Text("Vitamin D (IU)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = vitaminB12,
                            onValueChange = { vitaminB12 = it },
                            label = { Text("B12 (mcg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = vitaminC,
                            onValueChange = { vitaminC = it },
                            label = { Text("Vitamin C (mg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = calcium,
                            onValueChange = { calcium = it },
                            label = { Text("Calcium (mg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = iron,
                            onValueChange = { iron = it },
                            label = { Text("Iron (mg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = magnesium,
                            onValueChange = { magnesium = it },
                            label = { Text("Magnesium (mg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = zinc,
                            onValueChange = { zinc = it },
                            label = { Text("Zinc (mg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = omega3,
                            onValueChange = { omega3 = it },
                            label = { Text("Omega-3 (mg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
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
                            if (name.isNotBlank() && servingSize.isNotBlank() && servingUnit.isNotBlank()) {
                                val nutrition = SupplementNutrition(
                                    calories = calories.toDoubleOrNull(),
                                    protein = protein.toDoubleOrNull(),
                                    vitaminD = vitaminD.toDoubleOrNull(),
                                    vitaminB12 = vitaminB12.toDoubleOrNull(),
                                    vitaminC = vitaminC.toDoubleOrNull(),
                                    calcium = calcium.toDoubleOrNull(),
                                    iron = iron.toDoubleOrNull(),
                                    magnesium = magnesium.toDoubleOrNull(),
                                    zinc = zinc.toDoubleOrNull(),
                                    omega3 = omega3.toDoubleOrNull()
                                )
                                
                                onConfirm(
                                    name,
                                    brand.takeIf { it.isNotBlank() },
                                    description.takeIf { it.isNotBlank() },
                                    servingSize,
                                    servingUnit,
                                    selectedCategory,
                                    nutrition
                                )
                            }
                        },
                        enabled = name.isNotBlank() && servingSize.isNotBlank() && servingUnit.isNotBlank()
                    ) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSupplementDialog(
    supplement: Supplement?,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Double, String, SupplementFrequency, List<SupplementSchedule>, String?) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf(supplement?.servingSize ?: "") }
    var dosageUnit by remember { mutableStateOf(supplement?.servingUnit ?: "") }
    var selectedFrequency by remember { mutableStateOf(SupplementFrequency.ONCE_DAILY) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    // Schedule times
    var morningTime by remember { mutableStateOf("08:00") }
    var lunchTime by remember { mutableStateOf("12:00") }
    var dinnerTime by remember { mutableStateOf("18:00") }
    var bedtimeTime by remember { mutableStateOf("22:00") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Add to My Supplements",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Supplement info
                supplement?.let {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (it.brand != null) {
                                Text(
                                    text = it.brand,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Custom Name (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = dosageUnit,
                        onValueChange = { dosageUnit = it },
                        label = { Text("Unit *") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Frequency Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Frequency *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SupplementFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedFrequency = frequency
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Schedule Times",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Schedule based on frequency
                when (selectedFrequency) {
                    SupplementFrequency.ONCE_DAILY -> {
                        OutlinedTextField(
                            value = morningTime,
                            onValueChange = { morningTime = it },
                            label = { Text("Time (HH:MM)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SupplementFrequency.TWICE_DAILY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = morningTime,
                                onValueChange = { morningTime = it },
                                label = { Text("Morning Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = dinnerTime,
                                onValueChange = { dinnerTime = it },
                                label = { Text("Evening Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    SupplementFrequency.THREE_TIMES_DAILY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = morningTime,
                                onValueChange = { morningTime = it },
                                label = { Text("Morning Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = lunchTime,
                                onValueChange = { lunchTime = it },
                                label = { Text("Lunch Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = dinnerTime,
                                onValueChange = { dinnerTime = it },
                                label = { Text("Dinner Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    else -> {
                        OutlinedTextField(
                            value = morningTime,
                            onValueChange = { morningTime = it },
                            label = { Text("Time (HH:MM)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
                            val dosageValue = dosage.toDoubleOrNull()
                            if (supplement != null && dosageValue != null && dosageUnit.isNotBlank()) {
                                val schedules = when (selectedFrequency) {
                                    SupplementFrequency.ONCE_DAILY -> listOf(
                                        SupplementSchedule(morningTime, "Morning")
                                    )
                                    SupplementFrequency.TWICE_DAILY -> listOf(
                                        SupplementSchedule(morningTime, "Morning"),
                                        SupplementSchedule(dinnerTime, "Evening")
                                    )
                                    SupplementFrequency.THREE_TIMES_DAILY -> listOf(
                                        SupplementSchedule(morningTime, "Morning"),
                                        SupplementSchedule(lunchTime, "Lunch"),
                                        SupplementSchedule(dinnerTime, "Dinner")
                                    )
                                    else -> listOf(
                                        SupplementSchedule(morningTime, "Daily")
                                    )
                                }
                                
                                onConfirm(
                                    supplement.id,
                                    customName.takeIf { it.isNotBlank() },
                                    dosageValue,
                                    dosageUnit,
                                    selectedFrequency,
                                    schedules,
                                    notes.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = supplement != null && dosage.toDoubleOrNull() != null && dosageUnit.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSupplementDialog(
    userSupplement: com.beaconledger.welltrack.data.database.dao.UserSupplementWithDetails,
    onDismiss: () -> Unit,
    onConfirm: (String?, Double, String, SupplementFrequency, List<SupplementSchedule>, String?) -> Unit
) {
    var customName by remember { mutableStateOf(userSupplement.customName ?: "") }
    var dosage by remember { mutableStateOf(userSupplement.dosage.toString()) }
    var dosageUnit by remember { mutableStateOf(userSupplement.dosageUnit) }
    var selectedFrequency by remember { mutableStateOf(userSupplement.frequency) }
    var notes by remember { mutableStateOf(userSupplement.notes ?: "") }
    var expanded by remember { mutableStateOf(false) }
    
    // Parse existing schedule times
    val existingSchedules = try {
        kotlinx.serialization.json.Json.decodeFromString<List<SupplementSchedule>>(userSupplement.scheduledTimes)
    } catch (e: Exception) {
        listOf(SupplementSchedule("08:00", "Morning"))
    }
    
    var morningTime by remember { mutableStateOf(existingSchedules.getOrNull(0)?.time ?: "08:00") }
    var lunchTime by remember { mutableStateOf(existingSchedules.getOrNull(1)?.time ?: "12:00") }
    var dinnerTime by remember { mutableStateOf(existingSchedules.getOrNull(2)?.time ?: "18:00") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Supplement",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Supplement info
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = userSupplement.supplementName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (userSupplement.brand != null) {
                            Text(
                                text = userSupplement.brand,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Custom Name (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = dosageUnit,
                        onValueChange = { dosageUnit = it },
                        label = { Text("Unit *") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Frequency Selection
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Frequency *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        SupplementFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedFrequency = frequency
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Schedule Times",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Schedule based on frequency (same as AddSupplementDialog)
                when (selectedFrequency) {
                    SupplementFrequency.ONCE_DAILY -> {
                        OutlinedTextField(
                            value = morningTime,
                            onValueChange = { morningTime = it },
                            label = { Text("Time (HH:MM)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SupplementFrequency.TWICE_DAILY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = morningTime,
                                onValueChange = { morningTime = it },
                                label = { Text("Morning Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = dinnerTime,
                                onValueChange = { dinnerTime = it },
                                label = { Text("Evening Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    SupplementFrequency.THREE_TIMES_DAILY -> {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = morningTime,
                                onValueChange = { morningTime = it },
                                label = { Text("Morning Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = lunchTime,
                                onValueChange = { lunchTime = it },
                                label = { Text("Lunch Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = dinnerTime,
                                onValueChange = { dinnerTime = it },
                                label = { Text("Dinner Time (HH:MM)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    else -> {
                        OutlinedTextField(
                            value = morningTime,
                            onValueChange = { morningTime = it },
                            label = { Text("Time (HH:MM)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
                            val dosageValue = dosage.toDoubleOrNull()
                            if (dosageValue != null && dosageUnit.isNotBlank()) {
                                val schedules = when (selectedFrequency) {
                                    SupplementFrequency.ONCE_DAILY -> listOf(
                                        SupplementSchedule(morningTime, "Morning")
                                    )
                                    SupplementFrequency.TWICE_DAILY -> listOf(
                                        SupplementSchedule(morningTime, "Morning"),
                                        SupplementSchedule(dinnerTime, "Evening")
                                    )
                                    SupplementFrequency.THREE_TIMES_DAILY -> listOf(
                                        SupplementSchedule(morningTime, "Morning"),
                                        SupplementSchedule(lunchTime, "Lunch"),
                                        SupplementSchedule(dinnerTime, "Dinner")
                                    )
                                    else -> listOf(
                                        SupplementSchedule(morningTime, "Daily")
                                    )
                                }
                                
                                onConfirm(
                                    customName.takeIf { it.isNotBlank() },
                                    dosageValue,
                                    dosageUnit,
                                    selectedFrequency,
                                    schedules,
                                    notes.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = dosage.toDoubleOrNull() != null && dosageUnit.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun LogIntakeDialog(
    supplementName: String,
    defaultDosage: Double,
    defaultUnit: String,
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String?) -> Unit
) {
    var dosage by remember { mutableStateOf(defaultDosage.toString()) }
    var dosageUnit by remember { mutableStateOf(defaultUnit) }
    var notes by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Log Intake",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = supplementName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = { Text("Dosage Taken *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = dosageUnit,
                        onValueChange = { dosageUnit = it },
                        label = { Text("Unit *") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
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
                            val dosageValue = dosage.toDoubleOrNull()
                            if (dosageValue != null && dosageUnit.isNotBlank()) {
                                onConfirm(
                                    dosageValue,
                                    dosageUnit,
                                    notes.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = dosage.toDoubleOrNull() != null && dosageUnit.isNotBlank()
                    ) {
                        Text("Log")
                    }
                }
            }
        }
    }
}