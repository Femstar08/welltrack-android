package com.beaconledger.welltrack.presentation.mealprep

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPrepScreen(
    recipeId: String,
    userId: String,
    modifier: Modifier = Modifier,
    viewModel: MealPrepViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mealPrepGuidance by viewModel.mealPrepGuidance.collectAsStateWithLifecycle()
    val leftoverSuggestions by viewModel.leftoverSuggestions.collectAsStateWithLifecycle()
    
    var showCreateLeftoverDialog by remember { mutableStateOf(false) }

    LaunchedEffect(recipeId) {
        viewModel.loadMealPrepGuidance(recipeId)
        viewModel.loadActiveLeftovers(userId)
        viewModel.loadStorageRecommendations(recipeId)
        viewModel.loadExpiringLeftovers(userId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meal Preparation Guidance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            FloatingActionButton(
                onClick = { showCreateLeftoverDialog = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create Leftover"
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Meal Prep Instructions
                mealPrepGuidance?.let { guidance ->
                    item {
                        MealPrepInstructionsCard(guidance = guidance)
                    }
                    
                    item {
                        StorageRecommendationsCard(guidance = guidance)
                    }
                }

                // Active Leftovers
                if (uiState.activeLeftovers.isNotEmpty()) {
                    item {
                        ActiveLeftoversCard(
                            leftovers = uiState.activeLeftovers,
                            onMarkConsumed = { leftoverId ->
                                viewModel.markLeftoverAsConsumed(leftoverId)
                            },
                            onGetSuggestions = { leftoverIds ->
                                viewModel.getLeftoverSuggestions(leftoverIds)
                            }
                        )
                    }
                }

                // Expiring Leftovers Alert
                if (uiState.expiringLeftovers.isNotEmpty()) {
                    item {
                        ExpiringLeftoversCard(
                            leftovers = uiState.expiringLeftovers,
                            onGetSuggestions = { leftoverIds ->
                                viewModel.getLeftoverSuggestions(leftoverIds)
                            }
                        )
                    }
                }

                // Leftover Suggestions
                leftoverSuggestions?.let { suggestions ->
                    item {
                        LeftoverSuggestionsCard(suggestions = suggestions)
                    }
                }
            }
        }

        // Error/Success Messages
        uiState.errorMessage?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar or handle error
                viewModel.clearMessages()
            }
        }

        uiState.successMessage?.let { success ->
            LaunchedEffect(success) {
                // Show snackbar or handle success
                viewModel.clearMessages()
            }
        }
    }
    
    // Create Leftover Dialog
    if (showCreateLeftoverDialog) {
        CreateLeftoverDialog(
            onDismiss = { showCreateLeftoverDialog = false },
            onCreateLeftover = { name, quantity, unit, storageLocation, containerType, shelfLifeDays, notes ->
                viewModel.createLeftover(
                    userId = userId,
                    mealId = "meal_${System.currentTimeMillis()}", // Generate temporary meal ID
                    recipeId = recipeId,
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    storageLocation = storageLocation,
                    containerType = containerType,
                    nutritionInfo = NutritionInfo(
                        calories = 0.0,
                        carbohydrates = 0.0,
                        proteins = 0.0,
                        fats = 0.0,
                        fiber = 0.0,
                        sodium = 0.0,
                        potassium = 0.0
                    ), // Default empty nutrition info
                    shelfLifeDays = shelfLifeDays,
                    notes = notes
                )
                showCreateLeftoverDialog = false
            }
        )
    }
}