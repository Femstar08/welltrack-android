package com.beaconledger.welltrack.presentation.fitness

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.fitness.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VO2MaxTrackingScreen(
    currentVO2Max: Double?,
    vo2MaxGoal: VO2MaxGoal?,
    vo2MaxHistory: List<VO2MaxProgress>,
    trainingRecommendations: VO2MaxTrainingRecommendations?,
    onSetGoal: (Double, String) -> Unit,
    onRecordMeasurement: (Double, VO2MaxTestType, String?) -> Unit,
    onStartNorwegian4x4: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalDialog by remember { mutableStateOf(false) }
    var showMeasurementDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "VO2 Max Tracking",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current VO2 Max Status
            item {
                VO2MaxStatusCard(
                    currentVO2Max = currentVO2Max,
                    goal = vo2MaxGoal,
                    onSetGoal = { showGoalDialog = true },
                    onRecordMeasurement = { showMeasurementDialog = true }
                )
            }

            // Norwegian 4x4 Quick Start
            item {
                Norwegian4x4QuickStartCard(
                    onStartWorkout = onStartNorwegian4x4
                )
            }

            // Training Recommendations
            trainingRecommendations?.let { recommendations ->
                item {
                    TrainingRecommendationsCard(
                        recommendations = recommendations
                    )
                }
            }

            // Progress History
            if (vo2MaxHistory.isNotEmpty()) {
                item {
                    Text(
                        text = "Progress History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                items(vo2MaxHistory.sortedByDescending { it.date }) { progress ->
                    VO2MaxProgressItem(progress = progress)
                }
            }
        }
    }

    // Goal Setting Dialog
    if (showGoalDialog) {
        VO2MaxGoalDialog(
            currentVO2Max = currentVO2Max,
            onDismiss = { showGoalDialog = false },
            onSetGoal = { target, date ->
                onSetGoal(target, date)
                showGoalDialog = false
            }
        )
    }

    // Measurement Recording Dialog
    if (showMeasurementDialog) {
        VO2MaxMeasurementDialog(
            onDismiss = { showMeasurementDialog = false },
            onRecordMeasurement = { vo2Max, testType, notes ->
                onRecordMeasurement(vo2Max, testType, notes)
                showMeasurementDialog = false
            }
        )
    }
}

@Composable
fun VO2MaxStatusCard(
    currentVO2Max: Double?,
    goal: VO2MaxGoal?,
    onSetGoal: () -> Unit,
    onRecordMeasurement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VO2 Max Status",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current VO2 Max
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = currentVO2Max?.let { "${it.toInt()} ml/kg/min" } ?: "Not measured",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                goal?.let { goalData ->
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "Goal",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${goalData.targetVO2Max.toInt()} ml/kg/min",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Progress bar if goal is set
            goal?.let { goalData ->
                currentVO2Max?.let { current ->
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val progress = (current / goalData.targetVO2Max).coerceIn(0.0, 1.0)
                    
                    LinearProgressIndicator(
                        progress = progress.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "${(progress * 100).toInt()}% of goal achieved",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onRecordMeasurement,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Record Test")
                }

                Button(
                    onClick = onSetGoal,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (goal == null) "Set Goal" else "Update Goal")
                }
            }
        }
    }
}

@Composable
fun Norwegian4x4QuickStartCard(
    onStartWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Timer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Norwegian 4x4 Workout",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "The most effective method for improving VO2 max. 4 intervals of 4 minutes at 85-95% max heart rate.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Workout details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WorkoutDetailItem(
                    label = "Duration",
                    value = "39 min",
                    icon = Icons.Default.Add
                )
                
                WorkoutDetailItem(
                    label = "Intensity",
                    value = "High",
                    icon = Icons.Default.Add
                )
                
                WorkoutDetailItem(
                    label = "Equipment",
                    value = "Optional",
                    icon = Icons.Default.Add
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onStartWorkout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Norwegian 4x4")
            }
        }
    }
}

@Composable
fun WorkoutDetailItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun TrainingRecommendationsCard(
    recommendations: VO2MaxTrainingRecommendations,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Recommendations",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "Training Recommendations",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            recommendations.recommendations.forEach { recommendation ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    
                    Text(
                        text = recommendation,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trend indicator
            val trendColor = when (recommendations.currentTrend) {
                ImprovementTrend.EXCELLENT -> Color.Green
                ImprovementTrend.GOOD -> Color.Blue
                ImprovementTrend.SLOW -> Color(0xFFFF9800)
                ImprovementTrend.DECLINING -> Color.Red
                ImprovementTrend.UNKNOWN -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current Trend: ",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                Text(
                    text = recommendations.currentTrend.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = trendColor
                )
            }
        }
    }
}

@Composable
fun VO2MaxProgressItem(
    progress: VO2MaxProgress,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
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
                    text = "${progress.measuredVO2Max.toInt()} ml/kg/min",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    text = progress.testType.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Text(
                text = progress.date.split("T")[0], // Show just the date part
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun VO2MaxGoalDialog(
    currentVO2Max: Double?,
    onDismiss: () -> Unit,
    onSetGoal: (Double, String) -> Unit
) {
    var targetVO2Max by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Set VO2 Max Goal")
        },
        text = {
            Column {
                currentVO2Max?.let { current ->
                    Text(
                        text = "Current VO2 Max: ${current.toInt()} ml/kg/min",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = targetVO2Max,
                    onValueChange = { targetVO2Max = it },
                    label = { Text("Target VO2 Max (ml/kg/min)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = targetDate,
                    onValueChange = { targetDate = it },
                    label = { Text("Target Date (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    targetVO2Max.toDoubleOrNull()?.let { target ->
                        onSetGoal(target, targetDate)
                    }
                }
            ) {
                Text("Set Goal")
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
fun VO2MaxMeasurementDialog(
    onDismiss: () -> Unit,
    onRecordMeasurement: (Double, VO2MaxTestType, String?) -> Unit
) {
    var vo2MaxValue by remember { mutableStateOf("") }
    var selectedTestType by remember { mutableStateOf(VO2MaxTestType.MANUAL_ENTRY) }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Record VO2 Max Test")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = vo2MaxValue,
                    onValueChange = { vo2MaxValue = it },
                    label = { Text("VO2 Max (ml/kg/min)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Simplified dropdown for now - full implementation can be added later
                OutlinedTextField(
                    value = selectedTestType.name.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Test Type") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    vo2MaxValue.toDoubleOrNull()?.let { value ->
                        onRecordMeasurement(value, selectedTestType, notes.ifBlank { null })
                    }
                }
            ) {
                Text("Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun VO2MaxTrackingScreenPreview() {
    MaterialTheme {
        VO2MaxTrackingScreen(
            currentVO2Max = 45.0,
            vo2MaxGoal = VO2MaxGoal(
                currentVO2Max = 45.0,
                targetVO2Max = 50.0,
                targetDate = "2024-06-01",
                trainingPlan = VO2MaxTrainingPlan.NORWEGIAN_4X4_FOCUSED
            ),
            vo2MaxHistory = listOf(
                VO2MaxProgress(
                    date = "2024-01-15T10:00:00",
                    measuredVO2Max = 45.0,
                    testType = VO2MaxTestType.FIELD_TEST_COOPER
                ),
                VO2MaxProgress(
                    date = "2024-01-01T10:00:00",
                    measuredVO2Max = 42.0,
                    testType = VO2MaxTestType.GARMIN_ESTIMATE
                )
            ),
            trainingRecommendations = VO2MaxTrainingRecommendations(
                recommendations = listOf(
                    "Maintain current training frequency of 2-3 Norwegian 4x4 sessions per week",
                    "Good intensity levels. Try to consistently hit 85-95% max HR during work intervals"
                ),
                recommendedFrequency = "2-3 sessions per week",
                estimatedTimeToGoal = "8-12 weeks for significant improvement",
                currentTrend = ImprovementTrend.GOOD,
                nextTestRecommendation = "Consider testing in 1-2 weeks to track progress"
            ),
            onSetGoal = { _, _ -> },
            onRecordMeasurement = { _, _, _ -> },
            onStartNorwegian4x4 = {},
            onBackClick = {}
        )
    }
}