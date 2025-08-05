package com.beaconledger.welltrack.presentation.meal

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.presentation.meal.components.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MealLoggingScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: MealLoggingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val todaysMeals by viewModel.todaysMeals.collectAsState()
    
    var showMealTypeDialog by remember { mutableStateOf(false) }
    var showManualEntryDialog by remember { mutableStateOf(false) }
    var showRecipeSelectionDialog by remember { mutableStateOf(false) }
    var showCameraDialog by remember { mutableStateOf(false) }
    
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.logMealFromCamera(context, it) }
    }
    
    LaunchedEffect(userId) {
        viewModel.setCurrentUserId(userId)
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
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            
            Text(
                text = "Log Meal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Meal Type Selection
        MealTypeSelector(
            selectedMealType = uiState.selectedMealType,
            onMealTypeSelected = viewModel::setMealType
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Logging Options
        Text(
            text = "How would you like to log your meal?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Logging Method Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Camera Recognition
            ElevatedCard(
                modifier = Modifier.weight(1f),
                onClick = {
                    if (cameraPermissionState.status.isGranted) {
                        showCameraDialog = true
                    } else {
                        cameraPermissionState.launchPermissionRequest()
                    }
                }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Camera",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Camera",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Manual Entry
            ElevatedCard(
                modifier = Modifier.weight(1f),
                onClick = { showManualEntryDialog = true }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Manual",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Manual",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // From Recipe
            ElevatedCard(
                modifier = Modifier.weight(1f),
                onClick = { showRecipeSelectionDialog = true }
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.List,
                        contentDescription = "Recipe",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recipe",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Today's Meals
        Text(
            text = "Today's Meals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(todaysMeals) { meal ->
                MealCard(
                    meal = meal,
                    nutritionInfo = viewModel.getMealNutritionInfo(meal),
                    onStatusChange = { status -> viewModel.updateMealStatus(meal.id, status) },
                    onRatingChange = { rating -> viewModel.updateMealRating(meal.id, rating) },
                    onFavoriteToggle = { isFavorite -> viewModel.toggleMealFavorite(meal.id, isFavorite) }
                )
            }
        }
    }
    
    // Loading Indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    // Error Snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or handle error
            viewModel.clearError()
        }
    }
    
    // Camera Recognition Result Dialog
    uiState.cameraRecognitionResult?.let { result ->
        CameraRecognitionResultDialog(
            result = result,
            onDismiss = { viewModel.clearCameraResult() },
            onConfirm = { viewModel.clearCameraResult() }
        )
    }
    
    // Dialogs
    if (showCameraDialog) {
        CameraOptionsDialog(
            onDismiss = { showCameraDialog = false },
            onTakePhoto = { 
                showCameraDialog = false
                // In a real implementation, you would navigate to camera screen
                // For now, we'll just use gallery picker
                imagePickerLauncher.launch("image/*")
            },
            onSelectFromGallery = {
                showCameraDialog = false
                imagePickerLauncher.launch("image/*")
            }
        )
    }
    
    if (showManualEntryDialog) {
        ManualMealEntryDialog(
            ingredients = uiState.manualIngredients,
            portions = uiState.portions,
            notes = uiState.notes,
            onDismiss = { showManualEntryDialog = false },
            onAddIngredient = viewModel::addIngredient,
            onRemoveIngredient = viewModel::removeIngredient,
            onPortionsChange = viewModel::setPortions,
            onNotesChange = viewModel::setNotes,
            onConfirm = { mealName ->
                viewModel.logManualMeal(mealName)
                showManualEntryDialog = false
            }
        )
    }
    
    if (showRecipeSelectionDialog) {
        RecipeSelectionDialog(
            recipes = recipes,
            portions = uiState.portions,
            notes = uiState.notes,
            onDismiss = { showRecipeSelectionDialog = false },
            onPortionsChange = viewModel::setPortions,
            onNotesChange = viewModel::setNotes,
            onRecipeSelected = { recipeId ->
                viewModel.logMealFromRecipe(recipeId)
                showRecipeSelectionDialog = false
            }
        )
    }
}