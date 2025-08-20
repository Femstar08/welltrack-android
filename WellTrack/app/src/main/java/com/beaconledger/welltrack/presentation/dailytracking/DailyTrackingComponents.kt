package com.beaconledger.welltrack.presentation.dailytracking

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
import com.beaconledger.welltrack.data.model.*

@Composable
fun MorningTrackingDialog(
    initialData: MorningTrackingData = MorningTrackingData(),
    onDismiss: () -> Unit,
    onSave: (MorningTrackingData) -> Unit
) {
    var data by remember { mutableStateOf(initialData) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Morning Routine") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Water intake
                OutlinedTextField(
                    value = data.waterIntakeMl.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { amount -> 
                            data = data.copy(waterIntakeMl = amount) 
                        }
                    },
                    label = { Text("Water intake (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Energy level
                Text("Energy Level: ${data.energyLevel}/10")
                Slider(
                    value = data.energyLevel.toFloat(),
                    onValueChange = { data = data.copy(energyLevel = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Sleep quality
                Text("Sleep Quality: ${data.sleepQuality}/10")
                Slider(
                    value = data.sleepQuality.toFloat(),
                    onValueChange = { data = data.copy(sleepQuality = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Weight
                OutlinedTextField(
                    value = data.weight?.toString() ?: "",
                    onValueChange = { 
                        data = data.copy(weight = it.toDoubleOrNull())
                    },
                    label = { Text("Weight (kg) - Optional") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Mood
                OutlinedTextField(
                    value = data.mood,
                    onValueChange = { data = data.copy(mood = it) },
                    label = { Text("Mood") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Meal 1 logged
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = data.meal1Logged,
                        onCheckedChange = { data = data.copy(meal1Logged = it) }
                    )
                    Text("First meal logged")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(data) }) {
                Text("Save")
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
fun PreWorkoutTrackingDialog(
    initialData: PreWorkoutTrackingData = PreWorkoutTrackingData(),
    onDismiss: () -> Unit,
    onSave: (PreWorkoutTrackingData) -> Unit
) {
    var data by remember { mutableStateOf(initialData) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pre-Workout") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Energy level
                Text("Energy Level: ${data.energyLevel}/10")
                Slider(
                    value = data.energyLevel.toFloat(),
                    onValueChange = { data = data.copy(energyLevel = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Hydration
                OutlinedTextField(
                    value = data.hydrationMl.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { amount -> 
                            data = data.copy(hydrationMl = amount) 
                        }
                    },
                    label = { Text("Pre-workout hydration (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Workout type
                OutlinedTextField(
                    value = data.workoutType,
                    onValueChange = { data = data.copy(workoutType = it) },
                    label = { Text("Workout type") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Planned duration
                OutlinedTextField(
                    value = data.plannedDuration.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { duration -> 
                            data = data.copy(plannedDuration = duration) 
                        }
                    },
                    label = { Text("Planned duration (minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Snack consumed
                OutlinedTextField(
                    value = data.snackConsumed ?: "",
                    onValueChange = { data = data.copy(snackConsumed = it.takeIf { it.isNotBlank() }) },
                    label = { Text("Pre-workout snack (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(data) }) {
                Text("Save")
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
fun PostWorkoutTrackingDialog(
    initialData: PostWorkoutTrackingData = PostWorkoutTrackingData(),
    onDismiss: () -> Unit,
    onSave: (PostWorkoutTrackingData) -> Unit
) {
    var data by remember { mutableStateOf(initialData) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post-Workout") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Performance rating
                Text("Performance Rating: ${data.performanceRating}/10")
                Slider(
                    value = data.performanceRating.toFloat(),
                    onValueChange = { data = data.copy(performanceRating = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Mood
                Text("Mood: ${data.mood}/10")
                Slider(
                    value = data.mood.toFloat(),
                    onValueChange = { data = data.copy(mood = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Fatigue
                Text("Fatigue Level: ${data.fatigue}/10")
                Slider(
                    value = data.fatigue.toFloat(),
                    onValueChange = { data = data.copy(fatigue = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Hydration
                OutlinedTextField(
                    value = data.hydrationMl.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { amount -> 
                            data = data.copy(hydrationMl = amount) 
                        }
                    },
                    label = { Text("Post-workout hydration (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Recovery meal logged
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = data.recoveryMealLogged,
                        onCheckedChange = { data = data.copy(recoveryMealLogged = it) }
                    )
                    Text("Recovery meal logged")
                }

                // Workout notes
                OutlinedTextField(
                    value = data.workoutNotes,
                    onValueChange = { data = data.copy(workoutNotes = it) },
                    label = { Text("Workout notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(data) }) {
                Text("Save")
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
fun BedtimeTrackingDialog(
    initialData: BedtimeTrackingData = BedtimeTrackingData(),
    onDismiss: () -> Unit,
    onSave: (BedtimeTrackingData) -> Unit
) {
    var data by remember { mutableStateOf(initialData) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bedtime Routine") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Dinner logged
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = data.dinnerLogged,
                        onCheckedChange = { data = data.copy(dinnerLogged = it) }
                    )
                    Text("Dinner logged")
                }

                // Bedtime readiness
                Text("Bedtime Readiness: ${data.bedtimeReadiness}/10")
                Slider(
                    value = data.bedtimeReadiness.toFloat(),
                    onValueChange = { data = data.copy(bedtimeReadiness = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Stress level
                Text("Stress Level: ${data.stressLevel}/10")
                Slider(
                    value = data.stressLevel.toFloat(),
                    onValueChange = { data = data.copy(stressLevel = it.toInt()) },
                    valueRange = 1f..10f,
                    steps = 8
                )

                // Screen time
                OutlinedTextField(
                    value = data.screenTimeHours.toString(),
                    onValueChange = { 
                        it.toDoubleOrNull()?.let { hours -> 
                            data = data.copy(screenTimeHours = hours) 
                        }
                    },
                    label = { Text("Screen time today (hours)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Relaxation activity
                OutlinedTextField(
                    value = data.relaxationActivity ?: "",
                    onValueChange = { data = data.copy(relaxationActivity = it.takeIf { it.isNotBlank() }) },
                    label = { Text("Relaxation activity (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(data) }) {
                Text("Save")
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
fun EnergyLevelIndicator(
    level: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(10) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .then(
                        if (index < level) {
                            Modifier.background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                        } else {
                            Modifier.background(
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                CircleShape
                            )
                        }
                    )
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "$level/10",
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun WaterProgressIndicator(
    currentMl: Int,
    targetMl: Int,
    modifier: Modifier = Modifier
) {
    val progress = (currentMl.toFloat() / targetMl.toFloat()).coerceIn(0f, 1f)
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Water Intake",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${currentMl}ml / ${targetMl}ml",
                style = MaterialTheme.typography.bodySmall
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${(progress * 100).toInt()}% of daily goal",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}