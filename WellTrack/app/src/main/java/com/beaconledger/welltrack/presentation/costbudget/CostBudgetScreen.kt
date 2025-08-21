package com.beaconledger.welltrack.presentation.costbudget

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CostBudgetScreen(
    viewModel: CostBudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.refreshData()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Cost & Budget",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(onClick = { viewModel.showPriceUpdateDialog() }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Update prices"
                    )
                }
                
                IconButton(onClick = { viewModel.refreshData() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Budget Status
                item {
                    uiState.budgetStatus?.let { budgetStatus ->
                        BudgetStatusCard(
                            budgetStatus = budgetStatus,
                            onSetupBudget = { viewModel.showBudgetSettingsDialog() }
                        )
                    }
                }
                
                // Budget Alerts
                item {
                    BudgetAlertsSection(
                        alerts = uiState.budgetAlerts,
                        onDismissAlert = { alertId -> viewModel.dismissAlert(alertId) }
                    )
                }
                
                // Cost Analysis
                item {
                    uiState.costAnalysis?.let { costAnalysis ->
                        CostAnalysisCard(costAnalysis = costAnalysis)
                    }
                }
                
                // Recipe Cost Comparison
                item {
                    RecipeCostComparisonCard(
                        comparisons = uiState.recipeCostComparisons
                    )
                }
                
                // Optimization Suggestions
                item {
                    OptimizationSuggestionsCard(
                        suggestions = uiState.optimizationSuggestions
                    )
                }
                
                // Shopping List Cost Estimate
                item {
                    uiState.shoppingListCostEstimate?.let { estimate ->
                        ShoppingListCostEstimateCard(estimate = estimate)
                    }
                }
            }
        }
    }
    
    // Dialogs
    if (uiState.showBudgetSettingsDialog) {
        BudgetSettingsDialog(
            currentWeeklyBudget = uiState.budgetStatus?.weeklyBudget,
            currentMonthlyBudget = uiState.budgetStatus?.monthlyBudget,
            currentAlertThreshold = 0.8, // Default threshold
            currentEnableAlerts = true,
            currentCurrency = uiState.budgetStatus?.currency ?: "USD",
            onSave = { weekly, monthly, threshold, alerts, currency ->
                viewModel.setBudgetSettings(weekly, monthly, threshold, alerts, currency)
            },
            onDismiss = { viewModel.hideBudgetSettingsDialog() }
        )
    }
    
    if (uiState.showPriceUpdateDialog) {
        IngredientPriceUpdateDialog(
            onSave = { name, price, unit, storeId, storeName ->
                viewModel.updateIngredientPrice(name, price, unit, storeId, storeName)
            },
            onDismiss = { viewModel.hidePriceUpdateDialog() }
        )
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
    
    // Success message handling
    uiState.successMessage?.let { message ->
        LaunchedEffect(message) {
            // Show success snackbar
            viewModel.clearSuccessMessage()
        }
    }
}

@Composable
fun ShoppingListCostEstimateCard(
    estimate: ShoppingListCostEstimate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Shopping List Cost Estimate",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Total Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Estimated Cost",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${String.format("%.2f", estimate.totalCost)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (estimate.hasEstimatedPrices) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Some prices are estimated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}