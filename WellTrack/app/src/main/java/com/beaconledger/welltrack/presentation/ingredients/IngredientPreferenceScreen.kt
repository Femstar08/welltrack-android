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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredientPreferenceScreen(
    userId: String,
    viewModel: IngredientPreferenceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Ingredient Preferences",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = uiState.selectedTab.ordinal,
            modifier = Modifier.fillMaxWidth()
        ) {
            IngredientTab.values().forEach { tab ->
                Tab(
                    selected = uiState.selectedTab == tab,
                    onClick = { viewModel.setSelectedTab(tab) },
                    text = { Text(tab.displayName) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        when (uiState.selectedTab) {
            IngredientTab.PREFERENCES -> PreferencesTab(
                uiState = uiState,
                onAddPreferred = viewModel::addPreferredIngredient,
                onAddDisliked = viewModel::addDislikedIngredient,
                onAddAllergic = viewModel::addAllergicIngredient,
                onRemovePreference = viewModel::removePreference,
                onShowAddDialog = { viewModel.setShowAddDialog(true) }
            )
            IngredientTab.PANTRY -> PantryTab(
                uiState = uiState,
                onAddPantryItem = viewModel::addPantryItem,
                onUpdateQuantity = viewModel::updatePantryItemQuantity,
                onRemovePantryItem = viewModel::removePantryItem,
                onShowPantryDialog = { viewModel.setShowPantryDialog(true) }
            )
            IngredientTab.SUGGESTIONS -> SuggestionsTab(
                uiState = uiState,
                onAddPreferred = viewModel::addPreferredIngredient
            )
            IngredientTab.ANALYTICS -> AnalyticsTab(
                uiState = uiState
            )
        }
        
        // Loading indicator
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Add dialogs
        if (uiState.showAddDialog) {
            AddIngredientPreferenceDialog(
                onDismiss = { viewModel.setShowAddDialog(false) },
                onAddPreferred = { name, priority -> 
                    viewModel.addPreferredIngredient(name, priority)
                    viewModel.setShowAddDialog(false)
                },
                onAddDisliked = { name, notes -> 
                    viewModel.addDislikedIngredient(name, notes)
                    viewModel.setShowAddDialog(false)
                },
                onAddAllergic = { name, notes -> 
                    viewModel.addAllergicIngredient(name, notes)
                    viewModel.setShowAddDialog(false)
                }
            )
        }
        
        if (uiState.showPantryDialog) {
            AddPantryItemDialog(
                onDismiss = { viewModel.setShowPantryDialog(false) },
                onAddItem = { name, quantity, unit, category, expiry, location ->
                    viewModel.addPantryItem(name, quantity, unit, category, expiry, location)
                    viewModel.setShowPantryDialog(false)
                }
            )
        }
        
        // Snackbar messages
        uiState.errorMessage?.let { message ->
            LaunchedEffect(message) {
                // Show snackbar
                viewModel.clearMessages()
            }
        }
        
        uiState.successMessage?.let { message ->
            LaunchedEffect(message) {
                // Show snackbar
                viewModel.clearMessages()
            }
        }
    }
}