package com.beaconledger.welltrack.presentation.supplements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TodaySummaryCard(
    summary: SupplementDailySummary,
    upcomingReminders: List<SupplementReminder>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "${summary.adherencePercentage.toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        summary.adherencePercentage >= 90 -> Color(0xFF4CAF50)
                        summary.adherencePercentage >= 70 -> Color(0xFFFF9800)
                        else -> Color(0xFFF44336)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProgressItem(
                    label = "Taken",
                    count = summary.totalTaken,
                    color = Color(0xFF4CAF50)
                )
                
                ProgressItem(
                    label = "Scheduled",
                    count = summary.totalScheduled,
                    color = MaterialTheme.colorScheme.primary
                )
                
                ProgressItem(
                    label = "Missed",
                    count = summary.totalMissed,
                    color = Color(0xFFF44336)
                )
            }
            
            // Upcoming reminders
            if (upcomingReminders.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Next: ${upcomingReminders.first().supplementName} at ${upcomingReminders.first().scheduledTime}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ProgressItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun UserSupplementCard(
    userSupplement: UserSupplementWithDetails,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
    onLogIntake: (Double, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showIntakeDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth()
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
                        text = userSupplement.customName ?: userSupplement.supplementName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (userSupplement.brand != null) {
                        Text(
                            text = userSupplement.brand,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${userSupplement.dosage} ${userSupplement.dosageUnit}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = userSupplement.frequency.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(onClick = { showIntakeDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Log Intake",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit"
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
            
            if (userSupplement.notes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userSupplement.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    if (showIntakeDialog) {
        LogIntakeDialog(
            supplementName = userSupplement.customName ?: userSupplement.supplementName,
            defaultDosage = userSupplement.dosage,
            defaultUnit = userSupplement.dosageUnit,
            onDismiss = { showIntakeDialog = false },
            onConfirm = { dosage, unit, notes ->
                onLogIntake(dosage, unit, notes)
                showIntakeDialog = false
            }
        )
    }
}

@Composable
fun SupplementIntakeCard(
    intake: SupplementIntakeWithDetails,
    onMarkCompleted: (String, Double, String?) -> Unit,
    onMarkSkipped: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (intake.status) {
                IntakeStatus.TAKEN -> MaterialTheme.colorScheme.surfaceVariant
                IntakeStatus.SKIPPED -> MaterialTheme.colorScheme.errorContainer
                IntakeStatus.MISSED -> MaterialTheme.colorScheme.errorContainer
                IntakeStatus.PARTIAL -> MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = intake.customName ?: intake.supplementName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${intake.actualDosage} ${intake.dosageUnit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                if (intake.scheduledAt != null) {
                    val scheduledTime = LocalTime.parse(intake.scheduledAt.substringAfter("T").substringBefore(":"))
                    Text(
                        text = "Scheduled: ${scheduledTime.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row {
                when (intake.status) {
                    IntakeStatus.TAKEN -> {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = Color(0xFF4CAF50)
                        )
                    }
                    IntakeStatus.SKIPPED -> {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Skipped",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    IntakeStatus.MISSED -> {
                        Row {
                            IconButton(
                                onClick = { onMarkCompleted(intake.id, intake.actualDosage, null) }
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Mark as taken"
                                )
                            }
                            
                            IconButton(
                                onClick = { onMarkSkipped(intake.id, null) }
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Mark as skipped"
                                )
                            }
                        }
                    }
                    IntakeStatus.PARTIAL -> {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Partial",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SupplementLibraryCard(
    supplement: Supplement,
    onAddToUser: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = supplement.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                if (supplement.brand != null) {
                    Text(
                        text = supplement.brand,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Text(
                    text = supplement.category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "${supplement.servingSize} ${supplement.servingUnit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Button(
                onClick = onAddToUser,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add")
            }
        }
    }
}

@Composable
fun MissedSupplementCard(
    userSupplement: UserSupplementWithDetails,
    onLogIntake: (Double, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showIntakeDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userSupplement.customName ?: userSupplement.supplementName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${userSupplement.dosage} ${userSupplement.dosageUnit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Text(
                    text = "Missed today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Button(
                onClick = { showIntakeDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Log Now")
            }
        }
    }
    
    if (showIntakeDialog) {
        LogIntakeDialog(
            supplementName = userSupplement.customName ?: userSupplement.supplementName,
            defaultDosage = userSupplement.dosage,
            defaultUnit = userSupplement.dosageUnit,
            onDismiss = { showIntakeDialog = false },
            onConfirm = { dosage, unit, notes ->
                onLogIntake(dosage, unit, notes)
                showIntakeDialog = false
            }
        )
    }
}

@Composable
fun SupplementAdherenceChart(
    summary: SupplementDailySummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Adherence Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Simple progress indicator for now
            LinearProgressIndicator(
                progress = summary.adherencePercentage / 100f,
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    summary.adherencePercentage >= 90 -> Color(0xFF4CAF50)
                    summary.adherencePercentage >= 70 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${summary.adherencePercentage.toInt()}% adherence rate",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun NutritionalContributionCard(
    nutrition: SupplementNutrition,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Nutritional Contribution",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Display key nutrients
            nutrition.calories?.let {
                NutrientRow("Calories", "${it.toInt()}", "kcal")
            }
            
            nutrition.protein?.let {
                NutrientRow("Protein", String.format("%.1f", it), "g")
            }
            
            nutrition.vitaminD?.let {
                NutrientRow("Vitamin D", String.format("%.0f", it), "IU")
            }
            
            nutrition.omega3?.let {
                NutrientRow("Omega-3", String.format("%.0f", it), "mg")
            }
        }
    }
}

@Composable
private fun NutrientRow(
    name: String,
    value: String,
    unit: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}