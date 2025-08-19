package com.beaconledger.welltrack.presentation.pantry

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryItemCard(
    item: PantryItem,
    onUpdateQuantity: (Double) -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    var showQuantityDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.ingredientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.category.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (item.location != null) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Text(
                                text = item.location,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Status indicators
                    if (item.isLowStock) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF3E0)
                            )
                        ) {
                            Text(
                                text = "Low Stock",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFF6F00)
                            )
                        }
                    }
                    
                    item.expiryDate?.let { expiryDate ->
                        val daysUntilExpiry = ChronoUnit.DAYS.between(
                            LocalDate.now(),
                            LocalDate.parse(expiryDate)
                        ).toInt()
                        
                        if (daysUntilExpiry <= 7) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when {
                                        daysUntilExpiry <= 0 -> Color(0xFFFFEBEE)
                                        daysUntilExpiry <= 3 -> Color(0xFFFFF3E0)
                                        else -> Color(0xFFF3E5F5)
                                    }
                                )
                            ) {
                                Text(
                                    text = when {
                                        daysUntilExpiry <= 0 -> "Expired"
                                        daysUntilExpiry == 1 -> "Expires tomorrow"
                                        else -> "Expires in $daysUntilExpiry days"
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        daysUntilExpiry <= 0 -> Color(0xFFD32F2F)
                                        daysUntilExpiry <= 3 -> Color(0xFFFF6F00)
                                        else -> Color(0xFF7B1FA2)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { showQuantityDialog = true }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Update Qty")
                }
                
                TextButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit")
                }
                
                TextButton(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remove")
                }
            }
        }
    }
    
    if (showQuantityDialog) {
        UpdateQuantityDialog(
            currentQuantity = item.quantity,
            unit = item.unit,
            onDismiss = { showQuantityDialog = false },
            onUpdate = { newQuantity ->
                onUpdateQuantity(newQuantity)
                showQuantityDialog = false
            }
        )
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Remove Item") },
            text = { Text("Are you sure you want to remove ${item.ingredientName} from your pantry?") },
            confirmButton = {
                Button(
                    onClick = {
                        onRemove()
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun UpdateQuantityDialog(
    currentQuantity: Double,
    unit: String,
    onDismiss: () -> Unit,
    onUpdate: (Double) -> Unit
) {
    var newQuantity by remember { mutableStateOf(currentQuantity.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Quantity") },
        text = {
            Column {
                Text("Current: $currentQuantity $unit")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = newQuantity,
                    onValueChange = { newQuantity = it },
                    label = { Text("New Quantity") },
                    suffix = { Text(unit) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    newQuantity.toDoubleOrNull()?.let { quantity ->
                        onUpdate(quantity)
                    }
                },
                enabled = newQuantity.toDoubleOrNull() != null
            ) {
                Text("Update")
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
fun PantryAlertsSection(
    alerts: List<PantryAlert>,
    onDismissAlert: (String) -> Unit,
    onViewExpiring: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3E0)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF6F00),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Pantry Alerts (${alerts.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE65100)
                    )
                }
                
                TextButton(onClick = onViewExpiring) {
                    Text("View All")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            alerts.take(3).forEach { alert ->
                PantryAlertItem(
                    alert = alert,
                    onDismiss = { onDismissAlert(alert.id) }
                )
                
                if (alert != alerts.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            if (alerts.size > 3) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "And ${alerts.size - 3} more alerts...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PantryAlertItem(
    alert: PantryAlert,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE65100)
            )
            
            alert.daysUntilExpiry?.let { days ->
                Text(
                    text = when {
                        days <= 0 -> "Expired"
                        days == 1 -> "Expires tomorrow"
                        else -> "Expires in $days days"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}