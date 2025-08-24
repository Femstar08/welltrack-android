package com.beaconledger.welltrack.presentation.biomarkers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.BiomarkerEntryInput
import com.beaconledger.welltrack.domain.usecase.ValidationResult
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderDialog(
    onDismiss: () -> Unit,
    onCreateReminder: (BloodTestType, String, String?, ReminderFrequency, LocalDate?) -> Unit,
    getDefaultFrequency: (BloodTestType) -> ReminderFrequency,
    modifier: Modifier = Modifier
) {
    var selectedTestType by remember { mutableStateOf<BloodTestType?>(null) }
    var reminderName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedFrequency by remember { mutableStateOf<ReminderFrequency?>(null) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showTestTypeDropdown by remember { mutableStateOf(false) }
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    
    // Update frequency when test type changes
    LaunchedEffect(selectedTestType) {
        selectedTestType?.let { testType ->
            selectedFrequency = getDefaultFrequency(testType)
            if (reminderName.isEmpty()) {
                reminderName = "${testType.name.replace("_", " ")} Test"
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Create Blood Test Reminder",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Test Type Dropdown
                ExposedDropdownMenuBox(
                    expanded = showTestTypeDropdown,
                    onExpandedChange = { showTestTypeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedTestType?.name?.replace("_", " ") ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Test Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTestTypeDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showTestTypeDropdown,
                        onDismissRequest = { showTestTypeDropdown = false }
                    ) {
                        BloodTestType.values().forEach { testType ->
                            DropdownMenuItem(
                                text = { Text(testType.name.replace("_", " ")) },
                                onClick = {
                                    selectedTestType = testType
                                    showTestTypeDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // Reminder Name
                OutlinedTextField(
                    value = reminderName,
                    onValueChange = { reminderName = it },
                    label = { Text("Reminder Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                // Frequency Dropdown
                ExposedDropdownMenuBox(
                    expanded = showFrequencyDropdown,
                    onExpandedChange = { showFrequencyDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedFrequency?.name?.replace("_", " ") ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Frequency") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFrequencyDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = showFrequencyDropdown,
                        onDismissRequest = { showFrequencyDropdown = false }
                    ) {
                        ReminderFrequency.values().forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.name.replace("_", " ")) },
                                onClick = {
                                    selectedFrequency = frequency
                                    showFrequencyDropdown = false
                                }
                            )
                        }
                    }
                }
                
                // First Due Date (Optional)
                OutlinedTextField(
                    value = selectedDate?.format(DateTimeFormatter.ISO_LOCAL_DATE) ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("First Due Date (Optional)") },
                    trailingIcon = {
                        IconButton(onClick = { /* TODO: Show date picker */ }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            selectedTestType?.let { testType ->
                                selectedFrequency?.let { frequency ->
                                    onCreateReminder(
                                        testType,
                                        reminderName,
                                        description.takeIf { it.isNotBlank() },
                                        frequency,
                                        selectedDate
                                    )
                                }
                            }
                        },
                        enabled = selectedTestType != null && reminderName.isNotBlank() && selectedFrequency != null
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
fun AddBiomarkerEntryDialog(
    testType: BloodTestType?,
    onDismiss: () -> Unit,
    onSaveEntry: (BloodTestType, BiomarkerType, Double, String, LocalDate, String?, String?, Double?, Double?) -> Unit,
    getBiomarkersForTestType: (BloodTestType) -> List<BiomarkerType>,
    getBiomarkerReference: (BiomarkerType) -> BiomarkerReference?,
    validateValue: (BiomarkerType, Double, String) -> ValidationResult,
    modifier: Modifier = Modifier
) {
    var selectedTestType by remember { mutableStateOf(testType) }
    var selectedBiomarkerType by remember { mutableStateOf<BiomarkerType?>(null) }
    var value by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var testDate by remember { mutableStateOf(LocalDate.now()) }
    var notes by remember { mutableStateOf("") }
    var labName by remember { mutableStateOf("") }
    var referenceMin by remember { mutableStateOf("") }
    var referenceMax by remember { mutableStateOf("") }
    
    var showTestTypeDropdown by remember { mutableStateOf(false) }
    var showBiomarkerDropdown by remember { mutableStateOf(false) }
    var validationResult by remember { mutableStateOf<ValidationResult>(ValidationResult.Valid) }
    
    // Update unit and reference ranges when biomarker type changes
    LaunchedEffect(selectedBiomarkerType) {
        selectedBiomarkerType?.let { biomarkerType ->
            val reference = getBiomarkerReference(biomarkerType)
            reference?.let { ref ->
                unit = ref.unit
                referenceMin = ref.normalRangeMin.toString()
                referenceMax = ref.normalRangeMax.toString()
            }
        }
    }
    
    // Validate value when it changes
    LaunchedEffect(selectedBiomarkerType, value, unit) {
        if (selectedBiomarkerType != null && value.isNotBlank() && unit.isNotBlank()) {
            value.toDoubleOrNull()?.let { doubleValue ->
                validationResult = validateValue(selectedBiomarkerType!!, doubleValue, unit)
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Add Biomarker Entry",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Test Type Dropdown
                item {
                    ExposedDropdownMenuBox(
                        expanded = showTestTypeDropdown,
                        onExpandedChange = { showTestTypeDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTestType?.name?.replace("_", " ") ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Test Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTestTypeDropdown) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showTestTypeDropdown,
                            onDismissRequest = { showTestTypeDropdown = false }
                        ) {
                            BloodTestType.values().forEach { testType ->
                                DropdownMenuItem(
                                    text = { Text(testType.name.replace("_", " ")) },
                                    onClick = {
                                        selectedTestType = testType
                                        selectedBiomarkerType = null // Reset biomarker selection
                                        showTestTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Biomarker Type Dropdown
                item {
                    val availableBiomarkers = selectedTestType?.let { getBiomarkersForTestType(it) } ?: emptyList()
                    
                    ExposedDropdownMenuBox(
                        expanded = showBiomarkerDropdown,
                        onExpandedChange = { showBiomarkerDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = selectedBiomarkerType?.name?.replace("_", " ") ?: "",
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Biomarker") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showBiomarkerDropdown) },
                            enabled = availableBiomarkers.isNotEmpty(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = showBiomarkerDropdown,
                            onDismissRequest = { showBiomarkerDropdown = false }
                        ) {
                            availableBiomarkers.forEach { biomarkerType ->
                                DropdownMenuItem(
                                    text = { Text(biomarkerType.name.replace("_", " ")) },
                                    onClick = {
                                        selectedBiomarkerType = biomarkerType
                                        showBiomarkerDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Value and Unit
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            label = { Text("Value") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = validationResult is ValidationResult.Invalid,
                            modifier = Modifier.weight(2f)
                        )
                        
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Unit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Validation message
                item {
                    when (validationResult) {
                        is ValidationResult.Warning -> {
                            Text(
                                text = validationResult.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        is ValidationResult.Invalid -> {
                            Text(
                                text = validationResult.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        else -> { /* No message for valid */ }
                    }
                }
                
                // Reference Range
                item {
                    Text(
                        text = "Reference Range (Optional)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = referenceMin,
                            onValueChange = { referenceMin = it },
                            label = { Text("Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = referenceMax,
                            onValueChange = { referenceMax = it },
                            label = { Text("Max") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Test Date
                item {
                    OutlinedTextField(
                        value = testDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Test Date") },
                        trailingIcon = {
                            IconButton(onClick = { /* TODO: Show date picker */ }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Lab Name
                item {
                    OutlinedTextField(
                        value = labName,
                        onValueChange = { labName = it },
                        label = { Text("Lab Name (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Notes
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
                
                // Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        
                        Button(
                            onClick = {
                                selectedTestType?.let { testType ->
                                    selectedBiomarkerType?.let { biomarkerType ->
                                        value.toDoubleOrNull()?.let { doubleValue ->
                                            onSaveEntry(
                                                testType,
                                                biomarkerType,
                                                doubleValue,
                                                unit,
                                                testDate,
                                                notes.takeIf { it.isNotBlank() },
                                                labName.takeIf { it.isNotBlank() },
                                                referenceMin.toDoubleOrNull(),
                                                referenceMax.toDoubleOrNull()
                                            )
                                        }
                                    }
                                }
                            },
                            enabled = selectedTestType != null && 
                                     selectedBiomarkerType != null && 
                                     value.isNotBlank() && 
                                     unit.isNotBlank() &&
                                     validationResult !is ValidationResult.Invalid
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkBiomarkerEntryDialog(
    testType: BloodTestType,
    onDismiss: () -> Unit,
    onSaveEntries: (BloodTestType, LocalDate, List<BiomarkerEntryInput>, String?, String?) -> Unit,
    getBiomarkersForTestType: (BloodTestType) -> List<BiomarkerType>,
    getBiomarkerReference: (BiomarkerType) -> BiomarkerReference?,
    validateValue: (BiomarkerType, Double, String) -> ValidationResult,
    modifier: Modifier = Modifier
) {
    var testDate by remember { mutableStateOf(LocalDate.now()) }
    var labName by remember { mutableStateOf("") }
    var sessionNotes by remember { mutableStateOf("") }
    
    val availableBiomarkers = getBiomarkersForTestType(testType)
    val entries = remember { 
        mutableStateMapOf<BiomarkerType, BiomarkerEntryInput>().apply {
            availableBiomarkers.forEach { biomarkerType ->
                val reference = getBiomarkerReference(biomarkerType)
                this[biomarkerType] = BiomarkerEntryInput(
                    biomarkerType = biomarkerType,
                    value = 0.0,
                    unit = reference?.unit ?: "",
                    referenceRangeMin = reference?.normalRangeMin,
                    referenceRangeMax = reference?.normalRangeMax
                )
            }
        }
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Add ${testType.name.replace("_", " ")} Results",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Test Date and Lab Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = testDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Test Date") },
                        trailingIcon = {
                            IconButton(onClick = { /* TODO: Show date picker */ }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select date")
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    OutlinedTextField(
                        value = labName,
                        onValueChange = { labName = it },
                        label = { Text("Lab Name") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Biomarker entries
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(availableBiomarkers) { biomarkerType ->
                        val entry = entries[biomarkerType]!!
                        var valueText by remember { mutableStateOf("") }
                        var notesText by remember { mutableStateOf("") }
                        
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = biomarkerType.name.replace("_", " "),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = valueText,
                                        onValueChange = { 
                                            valueText = it
                                            it.toDoubleOrNull()?.let { doubleValue ->
                                                entries[biomarkerType] = entry.copy(value = doubleValue)
                                            }
                                        },
                                        label = { Text("Value") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        modifier = Modifier.weight(2f)
                                    )
                                    
                                    OutlinedTextField(
                                        value = entry.unit,
                                        onValueChange = { 
                                            entries[biomarkerType] = entry.copy(unit = it)
                                        },
                                        label = { Text("Unit") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                
                                if (entry.referenceRangeMin != null && entry.referenceRangeMax != null) {
                                    Text(
                                        text = "Normal range: ${entry.referenceRangeMin} - ${entry.referenceRangeMax} ${entry.unit}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                OutlinedTextField(
                                    value = notesText,
                                    onValueChange = { 
                                        notesText = it
                                        entries[biomarkerType] = entry.copy(notes = it.takeIf { it.isNotBlank() })
                                    },
                                    label = { Text("Notes (Optional)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Session Notes
                OutlinedTextField(
                    value = sessionNotes,
                    onValueChange = { sessionNotes = it },
                    label = { Text("Session Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val validEntries = entries.values.filter { it.value > 0 }
                            if (validEntries.isNotEmpty()) {
                                onSaveEntries(
                                    testType,
                                    testDate,
                                    validEntries,
                                    labName.takeIf { it.isNotBlank() },
                                    sessionNotes.takeIf { it.isNotBlank() }
                                )
                            }
                        },
                        enabled = entries.values.any { it.value > 0 }
                    ) {
                        Text("Save All")
                    }
                }
            }
        }
    }
}