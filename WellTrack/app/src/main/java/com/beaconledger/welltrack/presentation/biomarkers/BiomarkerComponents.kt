package com.beaconledger.welltrack.presentation.biomarkers

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderCard(
    reminder: BloodTestReminder,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.reminderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = reminder.testType.name.replace("_", " "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    reminder.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete reminder",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Next due: ${reminder.nextDueDate}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Text(
                        text = "Frequency: ${reminder.frequency.name.replace("_", " ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (reminder.skipCount > 0) {
                        Text(
                            text = "Skipped: ${reminder.skipCount}/${reminder.maxSkips}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (reminder.canSkip && reminder.skipCount < reminder.maxSkips) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Skip", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    
                    Button(
                        onClick = onAddEntry,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Add Results", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Button(
                        onClick = onComplete,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Mark Done", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverdueReminderCard(
    reminderWithStatus: BloodTestReminderWithStatus,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
    onAddEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = "Overdue",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminderWithStatus.reminder.reminderName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    Text(
                        text = "Overdue by ${-reminderWithStatus.daysUntilNext} days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (reminderWithStatus.reminder.canSkip && 
                    reminderWithStatus.reminder.skipCount < reminderWithStatus.reminder.maxSkips) {
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Skip", style = MaterialTheme.typography.labelSmall)
                    }
                }
                
                Button(
                    onClick = onAddEntry,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Add Results", style = MaterialTheme.typography.labelSmall)
                }
                
                Button(
                    onClick = onComplete,
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Mark Done", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiomarkerEntryCard(
    entry: BiomarkerEntry,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.biomarkerType.name.replace("_", " "),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = entry.testType.name.replace("_", " "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Status indicator
                entry.isWithinRange?.let { withinRange ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (withinRange) Color.Green.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (withinRange) "Normal" else "Abnormal",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (withinRange) Color.Green else Color.Red,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "${entry.value} ${entry.unit}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (entry.referenceRangeMin != null && entry.referenceRangeMax != null) {
                        Text(
                            text = "Range: ${entry.referenceRangeMin} - ${entry.referenceRangeMax} ${entry.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = entry.testDate,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    entry.labName?.let { lab ->
                        Text(
                            text = lab,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            entry.notes?.let { notes ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiomarkerTrendCard(
    biomarkerType: BiomarkerType,
    entries: List<BiomarkerEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = biomarkerType.name.replace("_", " "),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (entries.size >= 2) {
                val latest = entries.last()
                val previous = entries[entries.size - 2]
                val change = latest.value - previous.value
                val changePercentage = (change / previous.value) * 100
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Latest: ${latest.value} ${latest.unit}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        Text(
                            text = "Previous: ${previous.value} ${previous.unit}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    change > 0 -> Icons.Default.KeyboardArrowUp
                                    change < 0 -> Icons.Default.KeyboardArrowDown
                                    else -> Icons.Default.Delete
                                },
                                contentDescription = null,
                                tint = when {
                                    change > 0 -> Color.Green
                                    change < 0 -> Color.Red
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Text(
                                text = "${if (change > 0) "+" else ""}${String.format("%.1f", changePercentage)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = when {
                                    change > 0 -> Color.Green
                                    change < 0 -> Color.Red
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        
                        Text(
                            text = "${entries.size} readings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsSummaryCard(
    entries: List<BiomarkerEntry>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val totalEntries = entries.size
            val uniqueBiomarkers = entries.map { it.biomarkerType }.distinct().size
            val recentEntries = entries.filter { 
                LocalDate.parse(it.testDate, DateTimeFormatter.ISO_LOCAL_DATE)
                    .isAfter(LocalDate.now().minusMonths(3))
            }.size
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = totalEntries.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Total Tests",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uniqueBiomarkers.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Biomarkers",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = recentEntries.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Recent (3mo)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}