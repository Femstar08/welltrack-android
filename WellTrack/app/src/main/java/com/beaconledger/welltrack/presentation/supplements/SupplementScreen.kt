package com.beaconledger.welltrack.presentation.supplements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.presentation.components.ErrorMessage
import com.beaconledger.welltrack.presentation.components.LoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplementScreen(
    modifier: Modifier = Modifier,
    viewModel: SupplementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(modifier = modifier.fillMaxSize()) {
        // Top App Bar
        TopAppBar(
            title = { Text("Supplements") },
            actions = {
                IconButton(onClick = { viewModel.showCreateSupplementDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Supplement")
                }
            }
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal
        ) {
            SupplementTab.values().forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.setSelectedTab(tab) },
                    text = { 
                        Text(
                            when (tab) {
                                SupplementTab.MY_SUPPLEMENTS -> "My Supplements"
                                SupplementTab.LIBRARY -> "Library"
                                SupplementTab.ANALYTICS -> "Analytics"
                            }
                        )
                    }
                )
            }
        }
        
        // Content based on selected tab
        when (uiState.selectedTab) {
            SupplementTab.MY_SUPPLEMENTS -> {
                MySupplementsContent(
                    uiState = uiState,
                    onAddSupplement = { viewModel.showAddSupplementDialog() },
                    onEditSupplement = { viewModel.showEditSupplementDialog(it) },
                    onRemoveSupplement = { viewModel.removeSupplementFromUser(it) },
                    onLogIntake = { userSupplementId, dosage, unit, notes ->
                        viewModel.logSupplementIntake(userSupplementId, dosage, unit, notes)
                    },
                    onMarkCompleted = { intakeId, dosage, notes ->
                        viewModel.markIntakeAsCompleted(intakeId, dosage, notes)
                    },
                    onMarkSkipped = { intakeId, notes ->
                        viewModel.markIntakeAsSkipped(intakeId, notes)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            SupplementTab.LIBRARY -> {
                SupplementLibraryContent(
                    uiState = uiState,
                    onSearchQueryChange = { viewModel.searchSupplements(it) },
                    onCategoryFilter = { viewModel.filterByCategory(it) },
                    onAddToUser = { viewModel.showAddSupplementDialog(it) },
                    onScanBarcode = { viewModel.scanSupplementBarcode(it) },
                    modifier = Modifier.weight(1f)
                )
            }
            SupplementTab.ANALYTICS -> {
                SupplementAnalyticsContent(
                    uiState = uiState,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
        }
    }
    
    // Loading overlay
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    }
    
    // Dialogs
    if (uiState.showCreateDialog) {
        CreateSupplementDialog(
            onDismiss = { viewModel.hideCreateSupplementDialog() },
            onConfirm = { name, brand, description, servingSize, servingUnit, category, nutrition ->
                viewModel.createSupplement(name, brand, description, servingSize, servingUnit, category, nutrition)
            }
        )
    }
    
    if (uiState.showAddDialog) {
        AddSupplementDialog(
            supplement = uiState.selectedSupplement,
            onDismiss = { viewModel.hideAddSupplementDialog() },
            onConfirm = { supplementId, customName, dosage, dosageUnit, frequency, scheduledTimes, notes ->
                viewModel.addSupplementToUser(supplementId, customName, dosage, dosageUnit, frequency, scheduledTimes, notes)
            }
        )
    }
    
    if (uiState.showEditDialog) {
        uiState.selectedUserSupplement?.let { userSupplement ->
            EditSupplementDialog(
                userSupplement = userSupplement,
                onDismiss = { viewModel.hideEditSupplementDialog() },
                onConfirm = { customName, dosage, dosageUnit, frequency, scheduledTimes, notes ->
                    viewModel.updateUserSupplement(userSupplement.id, customName, dosage, dosageUnit, frequency, scheduledTimes, notes)
                }
            )
        }
    }
}

@Composable
private fun MySupplementsContent(
    uiState: SupplementUiState,
    onAddSupplement: () -> Unit,
    onEditSupplement: (com.beaconledger.welltrack.data.database.dao.UserSupplementWithDetails) -> Unit,
    onRemoveSupplement: (String) -> Unit,
    onLogIntake: (String, Double, String, String?) -> Unit,
    onMarkCompleted: (String, Double, String?) -> Unit,
    onMarkSkipped: (String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Summary Card
        item {
            TodaySummaryCard(
                summary = uiState.summary,
                upcomingReminders = uiState.upcomingReminders
            )
        }
        
        // Today's Intakes
        if (uiState.todayIntakes.isNotEmpty()) {
            item {
                Text(
                    text = "Today's Intakes",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            items(uiState.todayIntakes) { intake ->
                SupplementIntakeCard(
                    intake = intake,
                    onMarkCompleted = onMarkCompleted,
                    onMarkSkipped = onMarkSkipped
                )
            }
        }
        
        // My Supplements
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Supplements",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                IconButton(onClick = onAddSupplement) {
                    Icon(Icons.Default.Add, contentDescription = "Add Supplement")
                }
            }
        }
        
        if (uiState.userSupplements.isEmpty()) {
            item {
                EmptySupplementsMessage(onAddSupplement = onAddSupplement)
            }
        } else {
            items(uiState.userSupplements) { userSupplement ->
                UserSupplementCard(
                    userSupplement = userSupplement,
                    onEdit = { onEditSupplement(userSupplement) },
                    onRemove = { onRemoveSupplement(userSupplement.id) },
                    onLogIntake = { dosage, unit, notes ->
                        onLogIntake(userSupplement.id, dosage, unit, notes)
                    }
                )
            }
        }
        
        // Missed Supplements
        if (uiState.missedSupplements.isNotEmpty()) {
            item {
                Text(
                    text = "Missed Today",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            items(uiState.missedSupplements) { missedSupplement ->
                MissedSupplementCard(
                    userSupplement = missedSupplement,
                    onLogIntake = { dosage, unit, notes ->
                        onLogIntake(missedSupplement.id, dosage, unit, notes)
                    }
                )
            }
        }
    }
}

@Composable
private fun SupplementLibraryContent(
    uiState: SupplementUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategoryFilter: (SupplementCategory?) -> Unit,
    onAddToUser: (Supplement) -> Unit,
    onScanBarcode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Search and Filter Section
        SupplementSearchAndFilter(
            searchQuery = uiState.searchQuery,
            selectedCategory = uiState.selectedCategory,
            onSearchQueryChange = onSearchQueryChange,
            onCategoryFilter = onCategoryFilter,
            onScanBarcode = onScanBarcode
        )
        
        // Supplements List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.availableSupplements) { supplement ->
                SupplementLibraryCard(
                    supplement = supplement,
                    onAddToUser = { onAddToUser(supplement) }
                )
            }
        }
    }
}

@Composable
private fun SupplementAnalyticsContent(
    uiState: SupplementUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Supplement Analytics",
                style = MaterialTheme.typography.headlineMedium
            )
        }
        
        item {
            SupplementAdherenceChart(summary = uiState.summary)
        }
        
        item {
            NutritionalContributionCard(
                // This would need to be calculated and passed from the ViewModel
                nutrition = SupplementNutrition()
            )
        }
    }
}

@Composable
private fun EmptySupplementsMessage(
    onAddSupplement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No supplements added yet",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Text(
                text = "Add supplements to track your daily intake and adherence",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = onAddSupplement) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Supplement")
            }
        }
    }
}