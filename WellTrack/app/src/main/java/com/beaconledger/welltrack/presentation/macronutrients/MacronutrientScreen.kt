package com.beaconledger.welltrack.presentation.macronutrients

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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacronutrientScreen(
    viewModel: MacronutrientViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setUserId("current_user_id") // This would come from user session
        viewModel.loadMacronutrientBalance()
    }

    // Show snackbar for messages and errors
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar
            viewModel.clearMessage()
        }
    }

    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with date selector
        MacronutrientHeader(
            selectedDate = selectedDate,
            onDateChanged = viewModel::setSelectedDate,
            onTargetsClick = viewModel::showTargetDialog,
            onManualEntryClick = viewModel::showManualEntryDialog
        )

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
                // Core nutrients progress
                item {
                    CoreNutrientsCard(
                        summary = uiState.dailySummary,
                        onWaterAdd = { viewModel.logWaterIntake(250) }
                    )
                }

                // Macronutrient balance
                uiState.macronutrientBalance?.let { balance ->
                    item {
                        MacronutrientBalanceCard(balance = balance)
                    }
                }

                // Custom nutrients
                if (uiState.customNutrients.isNotEmpty()) {
                    item {
                        CustomNutrientsCard(
                            customNutrients = uiState.customNutrients,
                            summary = uiState.dailySummary,
                            onAddCustomNutrient = viewModel::showCustomNutrientDialog,
                            onRemoveCustomNutrient = viewModel::removeCustomNutrient
                        )
                    }
                } else {
                    item {
                        AddCustomNutrientPrompt(
                            onAddClick = viewModel::showCustomNutrientDialog
                        )
                    }
                }

                // Nutrient trends
                if (uiState.nutrientTrends.isNotEmpty()) {
                    items(uiState.nutrientTrends.toList()) { (nutrientName, trend) ->
                        NutrientTrendCard(
                            nutrientName = nutrientName,
                            trend = trend
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    if (uiState.showTargetDialog) {
        SetTargetsDialog(
            currentTargets = uiState.dailySummary?.targets,
            onDismiss = viewModel::hideTargetDialog,
            onSetTargets = { calories, protein, carbs, fat, fiber, water ->
                viewModel.setDailyTargets(calories, protein, carbs, fat, fiber, water)
                viewModel.hideTargetDialog()
            },
            onCalculateProtein = { bodyWeight, activityLevel, goal ->
                viewModel.calculateProteinTarget(bodyWeight, activityLevel, goal)
            },
            onCalculateFiber = { age, gender ->
                viewModel.calculateFiberTarget(age, gender)
            },
            onCalculateWater = { bodyWeight, activityLevel ->
                viewModel.calculateWaterTarget(bodyWeight, activityLevel)
            }
        )
    }

    if (uiState.showManualEntryDialog) {
        ManualNutrientEntryDialog(
            customNutrients = uiState.customNutrients,
            onDismiss = viewModel::hideManualEntryDialog,
            onLogNutrients = { calories, protein, carbs, fat, fiber, water, customNutrients ->
                viewModel.logManualNutrients(calories, protein, carbs, fat, fiber, water, customNutrients)
                viewModel.hideManualEntryDialog()
            }
        )
    }

    if (uiState.showCustomNutrientDialog) {
        AddCustomNutrientDialog(
            onDismiss = viewModel::hideCustomNutrientDialog,
            onAddNutrient = { name, unit, targetValue, category, priority ->
                viewModel.addCustomNutrient(name, unit, targetValue, category, priority)
                viewModel.hideCustomNutrientDialog()
            }
        )
    }
}

@Composable
private fun MacronutrientHeader(
    selectedDate: LocalDate,
    onDateChanged: (LocalDate) -> Unit,
    onTargetsClick: () -> Unit,
    onManualEntryClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Macronutrients",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = onTargetsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Set Targets"
                )
            }
            IconButton(onClick = onManualEntryClick) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Manual Entry"
                )
            }
        }
    }
}

@Composable
private fun AddCustomNutrientPrompt(
    onAddClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Track Custom Nutrients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Add vitamins, minerals, or other nutrients beyond the core five",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onAddClick) {
                Text("Add Custom Nutrient")
            }
        }
    }
}