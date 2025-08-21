package com.beaconledger.welltrack.presentation.pantry

import androidx.compose.foundation.clickable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(
    viewModel: PantryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showBarcodeScanner by remember { mutableStateOf(false) }
    var showExpiryDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Pantry Management",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { showBarcodeScanner = true }) {
                    Icon(Icons.Default.Camera, contentDescription = "Scan Barcode")
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Item")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Overview cards
        PantryOverviewCards(
            overview = uiState.overview,
            onShowExpiring = { showExpiryDialog = true }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Alerts section
        if (uiState.alerts.isNotEmpty()) {
            PantryAlertsSection(
                alerts = uiState.alerts,
                onDismissAlert = viewModel::dismissAlert,
                onViewExpiring = { showExpiryDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Pantry items list
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
                            viewModel.updateQuantity(item.ingredientName, newQuantity)
                        },
                        onEdit = { viewModel.editPantryItem(item) },
                        onRemove = { viewModel.removePantryItem(item.ingredientName) }
                    )
                }
            } else {
                item {
                    EmptyPantryState(
                        onAddItem = { showAddDialog = true },
                        onScanBarcode = { showBarcodeScanner = true }
                    )
                }
            }
        }
    }
    
    // Dialogs
    if (showAddDialog) {
        AddPantryItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { request ->
                viewModel.addPantryItem(request)
                showAddDialog = false
            }
        )
    }
    
    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { barcode ->
                viewModel.addPantryItemByBarcode(barcode)
                showBarcodeScanner = false
            }
        )
    }
    
    if (showExpiryDialog) {
        ExpiringItemsDialog(
            expiringItems = uiState.expiringItemsWithSuggestions,
            onDismiss = { showExpiryDialog = false },
            onUseInRecipe = { recipe -> 
                // Navigate to recipe or cooking screen
            }
        )
    }
    
    // Show loading or error states
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or error dialog
        }
    }
}

@Composable
private fun PantryOverviewCards(
    overview: PantryOverviewData?,
    onShowExpiring: () -> Unit
) {
    if (overview == null) return
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OverviewCard(
            title = "Total Items",
            value = overview.totalItems.toString(),
            icon = Icons.Default.Restaurant,
            modifier = Modifier.weight(1f)
        )
        
        OverviewCard(
            title = "Low Stock",
            value = overview.lowStockCount.toString(),
            icon = Icons.Default.Warning,
            color = if (overview.lowStockCount > 0) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        
        OverviewCard(
            title = "Expiring",
            value = overview.expiringCount.toString(),
            icon = Icons.Default.AccessTime,
            color = if (overview.expiringCount > 0) Color(0xFFF44336) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
            onClick = if (overview.expiringCount > 0) onShowExpiring else null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OverviewCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier.then(
            if (onClick != null) Modifier.clickable { onClick() } else Modifier
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyPantryState(
    onAddItem: () -> Unit,
    onScanBarcode: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Restaurant,
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
            text = "Add ingredients to track inventory, get expiry alerts, and manage your kitchen efficiently",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onScanBarcode,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.CameraEnhance, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan Barcode")
            }
            
            OutlinedButton(
                onClick = onAddItem,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Manually")
            }
        }
    }
}