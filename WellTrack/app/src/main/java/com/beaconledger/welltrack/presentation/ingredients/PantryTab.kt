package com.beaconledger.welltrack.presentation.ingredients

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@Composable
fun PantryTab(
    uiState: IngredientPreferenceUiState,
    onAddPantryItem: (String, Double, String, IngredientCategory, String?, String?) -> Unit,
    onUpdateQuantity: (String, Double) -> Unit,
    onRemovePantryItem: (String) -> Unit,
    onShowPantryDialog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Add button
        Button(
            onClick = onShowPantryDialog,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Pantry Item")
        }
        
        // Alerts section
        if (uiState.pantryAlerts.isNotEmpty()) {
            Text(
                text = "Alerts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            uiState.pantryAlerts.take(3).forEach { alert ->
                PantryAlertCard(alert = alert)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Pantry items
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (uiState.pantryItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Pantry Items (${uiState.pantryItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                items(uiState.pantryItems) { item ->
                    PantryItemCard(
                        item = item,
                        onUpdateQuantity = { newQuantity -> 
                            onUpdateQuantity(item.ingredientName, newQuantity)
                        },
                        onRemove = { onRemovePantryItem(item.ingredientName) }
                    )
                }
            } else {
                item {
                    EmptyPantryState(onShowPantryDialog)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryAlertCard(alert: PantryAlert) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (alert.severity) {
                AlertSeverity.CRITICAL -> Color(0xFFFFEBEE)
                AlertSeverity.HIGH -> Color(0xFFFFF3E0)
                AlertSeverity.MEDIUM -> Color(0xFFFFF8E1)
                AlertSeverity.LOW -> Color(0xFFF3E5F5)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (alert.alertType) {
                    AlertType.EXPIRED -> Icons.Default.Warning
                    AlertType.EXPIRY_WARNING -> Icons.Default.Schedule
                    AlertType.LOW_STOCK -> Icons.Default.Inventory
                    AlertType.OUT_OF_STOCK -> Icons.Default.RemoveShoppingCart
                },
                contentDescription = null,
                tint = Color(android.graphics.Color.parseColor(alert.severity.color)),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = alert.message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PantryItemCard(
    item: PantryItem,
    onUpdateQuantity: (Double) -> Unit,
    onRemove: () -> Unit
) {
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
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${item.quantity} ${item.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    item.location?.let { location ->
                        Text(
                            text = "Location: $location",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    item.expiryDate?.let { expiry ->
                        Text(
                            text = "Expires: $expiry",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                IconButton(onClick = onRemove) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove item",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            if (item.isLowStock) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Low Stock",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPantryState(
    onShowPantryDialog: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Kitchen,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Your pantry is empty",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Add ingredients to track inventory and get expiry alerts",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        
        Button(onClick = onShowPantryDialog) {
            Text("Add Your First Item")
        }
    }
}