package com.beaconledger.welltrack.presentation.dailytracking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTrackingScreen(
    modifier: Modifier = Modifier,
    viewModel: DailyTrackingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Date selector
        DateSelector(
            selectedDate = selectedDate,
            onDateSelected = viewModel::setSelectedDate
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Daily summary card
        uiState.dailySummary?.let { summary ->
            DailySummaryCard(summary = summary)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tracking sections
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                TrackingSectionCard(
                    title = "Morning Routine",
                    icon = Icons.Default.WbSunny,
                    isCompleted = uiState.dailySummary?.morningCompleted ?: false,
                    onClick = { /* Navigate to morning tracking */ }
                )
            }

            item {
                TrackingSectionCard(
                    title = "Pre-Workout",
                    icon = Icons.Default.FitnessCenter,
                    isCompleted = uiState.dailySummary?.preWorkoutCompleted ?: false,
                    onClick = { /* Navigate to pre-workout tracking */ }
                )
            }

            item {
                TrackingSectionCard(
                    title = "Post-Workout",
                    icon = Icons.Default.Done,
                    isCompleted = uiState.dailySummary?.postWorkoutCompleted ?: false,
                    onClick = { /* Navigate to post-workout tracking */ }
                )
            }

            item {
                TrackingSectionCard(
                    title = "Bedtime Routine",
                    icon = Icons.Default.Bedtime,
                    isCompleted = uiState.dailySummary?.bedtimeCompleted ?: false,
                    onClick = { /* Navigate to bedtime tracking */ }
                )
            }

            item {
                WaterIntakeCard(
                    totalMl = uiState.dailySummary?.totalWaterMl ?: 0,
                    progress = uiState.dailySummary?.waterIntakeProgress ?: 0f,
                    onAddWater = { amount -> viewModel.addWaterIntake(amount) }
                )
            }
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or error dialog
            viewModel.clearError()
        }
    }
}

@Composable
private fun DateSelector(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onDateSelected(selectedDate.minusDays(1)) }
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
            }

            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            IconButton(
                onClick = { onDateSelected(selectedDate.plusDays(1)) }
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
            }
        }
    }
}

@Composable
private fun DailySummaryCard(
    summary: DailyTrackingSummary
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Daily Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Completion percentage
            LinearProgressIndicator(
                progress = summary.completionPercentage,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${(summary.completionPercentage * 100).toInt()}% Complete",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickStat(
                    label = "Water",
                    value = "${summary.totalWaterMl}ml",
                    progress = summary.waterIntakeProgress
                )

                if (summary.energyLevelAverage > 0) {
                    QuickStat(
                        label = "Energy",
                        value = "${summary.energyLevelAverage.toInt()}/10",
                        progress = summary.energyLevelAverage / 10f
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStat(
    label: String,
    value: String,
    progress: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        LinearProgressIndicator(
            progress = progress.coerceIn(0f, 1f),
            modifier = Modifier.width(60.dp)
        )
    }
}

@Composable
private fun TrackingSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go to section"
                )
            }
        }
    }
}

@Composable
private fun WaterIntakeCard(
    totalMl: Int,
    progress: Float,
    onAddWater: (Int) -> Unit
) {
    var showAddWaterDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Water Intake",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = { showAddWaterDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add water")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${totalMl}ml / ${(totalMl / progress.coerceAtLeast(0.01f)).toInt()}ml",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Quick add buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(250, 500, 750).forEach { amount ->
                    OutlinedButton(
                        onClick = { onAddWater(amount) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("${amount}ml")
                    }
                }
            }
        }
    }

    if (showAddWaterDialog) {
        AddWaterDialog(
            onDismiss = { showAddWaterDialog = false },
            onConfirm = { amount ->
                onAddWater(amount)
                showAddWaterDialog = false
            }
        )
    }
}

@Composable
private fun AddWaterDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Water Intake") },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Amount (ml)") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toIntOrNull()?.let { onConfirm(it) }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}