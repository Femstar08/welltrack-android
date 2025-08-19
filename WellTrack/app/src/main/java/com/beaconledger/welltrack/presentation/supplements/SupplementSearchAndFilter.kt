package com.beaconledger.welltrack.presentation.supplements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.SupplementCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementSearchAndFilter(
    searchQuery: String,
    selectedCategory: SupplementCategory?,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilter: (SupplementCategory?) -> Unit,
    onScanBarcode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBarcodeScanner by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search supplements...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            )
            
            // Barcode Scanner Button
            IconButton(
                onClick = { showBarcodeScanner = true },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.Search,
                    contentDescription = "Scan Barcode",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            item {
                FilterChip(
                    onClick = { onCategoryFilter(null) },
                    label = { Text("All") },
                    selected = selectedCategory == null,
                    leadingIcon = if (selectedCategory == null) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
            
            items(SupplementCategory.values()) { category ->
                FilterChip(
                    onClick = { onCategoryFilter(category) },
                    label = { 
                        Text(
                            category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        ) 
                    },
                    selected = selectedCategory == category,
                    leadingIcon = if (selectedCategory == category) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
    
    // Barcode Scanner Dialog (placeholder)
    if (showBarcodeScanner) {
        BarcodeScannerDialog(
            onDismiss = { showBarcodeScanner = false },
            onBarcodeScanned = { barcode ->
                onScanBarcode(barcode)
                showBarcodeScanner = false
            }
        )
    }
}

@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    // This is a placeholder implementation
    // In a real app, you would integrate with CameraX and ML Kit for barcode scanning
    var manualBarcode by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Scan Barcode") },
        text = {
            Column {
                Text("Camera scanning would be implemented here using CameraX and ML Kit.")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("For now, enter barcode manually:")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = manualBarcode,
                    onValueChange = { manualBarcode = it },
                    label = { Text("Barcode") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (manualBarcode.isNotBlank()) {
                        onBarcodeScanned(manualBarcode)
                    }
                },
                enabled = manualBarcode.isNotBlank()
            ) {
                Text("Scan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}