package com.beaconledger.welltrack.presentation.dietary

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
fun DietaryRestrictionsScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    viewModel: DietaryRestrictionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(userId) {
        viewModel.setUserId(userId)
    }
    
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.clearSaveSuccess()
            // Could show a snackbar or navigate back
        }
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
                text = "Dietary Restrictions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(
                onClick = { viewModel.saveDietaryProfile() },
                enabled = !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
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
                // Dietary Restrictions Section
                item {
                    DietaryRestrictionsSection(
                        selectedRestrictions = uiState.selectedRestrictions,
                        restrictionSeverities = uiState.restrictionSeverities,
                        onToggleRestriction = viewModel::toggleDietaryRestriction,
                        onSetSeverity = viewModel::setRestrictionSeverity
                    )
                }
                
                // Allergies Section
                item {
                    AllergiesSection(
                        selectedAllergies = uiState.selectedAllergies,
                        allergySeverities = uiState.allergySeverities,
                        onAddAllergy = viewModel::addAllergy,
                        onRemoveAllergy = viewModel::removeAllergy,
                        onSetSeverity = viewModel::setAllergySeverity
                    )
                }
                
                // Food Preferences Section
                item {
                    FoodPreferencesSection(
                        ingredientPreferences = uiState.ingredientPreferences,
                        cuisinePreferences = uiState.cuisinePreferences,
                        cookingMethodPreferences = uiState.cookingMethodPreferences,
                        onUpdateIngredientPreference = viewModel::updateIngredientPreference,
                        onRemoveIngredientPreference = viewModel::removeIngredientPreference,
                        onUpdateCuisinePreference = viewModel::updateCuisinePreference,
                        onRemoveCuisinePreference = viewModel::removeCuisinePreference,
                        onUpdateCookingMethodPreference = viewModel::updateCookingMethodPreference,
                        onRemoveCookingMethodPreference = viewModel::removeCookingMethodPreference
                    )
                }
            }
        }
        
        // Error handling
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show snackbar or error dialog
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun DietaryRestrictionsSection(
    selectedRestrictions: Set<DietaryRestrictionType>,
    restrictionSeverities: Map<DietaryRestrictionType, RestrictionSeverity>,
    onToggleRestriction: (DietaryRestrictionType) -> Unit,
    onSetSeverity: (DietaryRestrictionType, RestrictionSeverity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dietary Restrictions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Select your dietary restrictions and preferences",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Group restrictions by category
            val restrictionsByCategory = DietaryRestrictionType.values().groupBy { it.category }
            
            restrictionsByCategory.forEach { (category, restrictions) ->
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                restrictions.forEach { restriction ->
                    RestrictionItem(
                        restriction = restriction,
                        isSelected = selectedRestrictions.contains(restriction),
                        severity = restrictionSeverities[restriction] ?: RestrictionSeverity.MODERATE,
                        onToggle = { onToggleRestriction(restriction) },
                        onSetSeverity = { severity -> onSetSeverity(restriction, severity) }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun RestrictionItem(
    restriction: DietaryRestrictionType,
    isSelected: Boolean,
    severity: RestrictionSeverity,
    onToggle: () -> Unit,
    onSetSeverity: (RestrictionSeverity) -> Unit
) {
    var showSeverityDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = restriction.displayName,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = restriction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (isSelected) {
            TextButton(
                onClick = { showSeverityDialog = true }
            ) {
                Text(severity.displayName)
            }
        }
    }
    
    if (showSeverityDialog) {
        SeveritySelectionDialog(
            title = "Restriction Severity",
            currentSeverity = severity,
            severityOptions = RestrictionSeverity.values().toList(),
            onSeveritySelected = { newSeverity ->
                onSetSeverity(newSeverity)
                showSeverityDialog = false
            },
            onDismiss = { showSeverityDialog = false }
        )
    }
}

@Composable
private fun AllergiesSection(
    selectedAllergies: Set<String>,
    allergySeverities: Map<String, AllergySeverity>,
    onAddAllergy: (String, AllergySeverity) -> Unit,
    onRemoveAllergy: (String) -> Unit,
    onSetSeverity: (String, AllergySeverity) -> Unit
) {
    var showAddAllergyDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Allergies & Intolerances",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = { showAddAllergyDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Allergy")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (selectedAllergies.isEmpty()) {
                Text(
                    text = "No allergies specified",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                selectedAllergies.forEach { allergen ->
                    AllergyItem(
                        allergen = allergen,
                        severity = allergySeverities[allergen] ?: AllergySeverity.MODERATE,
                        onRemove = { onRemoveAllergy(allergen) },
                        onSetSeverity = { severity -> onSetSeverity(allergen, severity) }
                    )
                }
            }
        }
    }
    
    if (showAddAllergyDialog) {
        AddAllergyDialog(
            onAddAllergy = { allergen, severity ->
                onAddAllergy(allergen, severity)
                showAddAllergyDialog = false
            },
            onDismiss = { showAddAllergyDialog = false }
        )
    }
}

@Composable
private fun AllergyItem(
    allergen: String,
    severity: AllergySeverity,
    onRemove: () -> Unit,
    onSetSeverity: (AllergySeverity) -> Unit
) {
    var showSeverityDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = allergen,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = severity.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = when (severity) {
                    AllergySeverity.MILD -> MaterialTheme.colorScheme.primary
                    AllergySeverity.MODERATE -> MaterialTheme.colorScheme.tertiary
                    AllergySeverity.SEVERE -> MaterialTheme.colorScheme.error
                    AllergySeverity.ANAPHYLAXIS -> MaterialTheme.colorScheme.error
                }
            )
        }
        
        Row {
            TextButton(onClick = { showSeverityDialog = true }) {
                Text("Edit")
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
    
    if (showSeverityDialog) {
        AllergySeveritySelectionDialog(
            allergen = allergen,
            currentSeverity = severity,
            onSeveritySelected = { newSeverity ->
                onSetSeverity(newSeverity)
                showSeverityDialog = false
            },
            onDismiss = { showSeverityDialog = false }
        )
    }
}

@Composable
private fun FoodPreferencesSection(
    ingredientPreferences: Map<String, PreferenceLevel>,
    cuisinePreferences: Map<String, PreferenceLevel>,
    cookingMethodPreferences: Map<String, PreferenceLevel>,
    onUpdateIngredientPreference: (String, PreferenceLevel) -> Unit,
    onRemoveIngredientPreference: (String) -> Unit,
    onUpdateCuisinePreference: (String, PreferenceLevel) -> Unit,
    onRemoveCuisinePreference: (String) -> Unit,
    onUpdateCookingMethodPreference: (String, PreferenceLevel) -> Unit,
    onRemoveCookingMethodPreference: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Food Preferences",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ingredient Preferences
            PreferenceSubsection(
                title = "Ingredients",
                preferences = ingredientPreferences,
                onUpdatePreference = onUpdateIngredientPreference,
                onRemovePreference = onRemoveIngredientPreference
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cuisine Preferences
            PreferenceSubsection(
                title = "Cuisines",
                preferences = cuisinePreferences,
                onUpdatePreference = onUpdateCuisinePreference,
                onRemovePreference = onRemoveCuisinePreference
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cooking Method Preferences
            PreferenceSubsection(
                title = "Cooking Methods",
                preferences = cookingMethodPreferences,
                onUpdatePreference = onUpdateCookingMethodPreference,
                onRemovePreference = onRemoveCookingMethodPreference
            )
        }
    }
}

@Composable
private fun PreferenceSubsection(
    title: String,
    preferences: Map<String, PreferenceLevel>,
    onUpdatePreference: (String, PreferenceLevel) -> Unit,
    onRemovePreference: (String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add $title")
            }
        }
        
        if (preferences.isEmpty()) {
            Text(
                text = "No $title preferences set",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            preferences.forEach { (item, preference) ->
                PreferenceItem(
                    item = item,
                    preference = preference,
                    onUpdatePreference = { newPreference -> onUpdatePreference(item, newPreference) },
                    onRemove = { onRemovePreference(item) }
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddPreferenceDialog(
            title = "Add $title Preference",
            onAddPreference = { item, preference ->
                onUpdatePreference(item, preference)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun PreferenceItem(
    item: String,
    preference: PreferenceLevel,
    onUpdatePreference: (PreferenceLevel) -> Unit,
    onRemove: () -> Unit
) {
    var showPreferenceDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = preference.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = when (preference) {
                    PreferenceLevel.LOVE -> MaterialTheme.colorScheme.primary
                    PreferenceLevel.LIKE -> MaterialTheme.colorScheme.primary
                    PreferenceLevel.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                    PreferenceLevel.DISLIKE -> MaterialTheme.colorScheme.error
                    PreferenceLevel.HATE -> MaterialTheme.colorScheme.error
                }
            )
        }
        
        Row {
            TextButton(onClick = { showPreferenceDialog = true }) {
                Text("Edit")
            }
            
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove")
            }
        }
    }
    
    if (showPreferenceDialog) {
        PreferenceLevelSelectionDialog(
            item = item,
            currentPreference = preference,
            onPreferenceSelected = { newPreference ->
                onUpdatePreference(newPreference)
                showPreferenceDialog = false
            },
            onDismiss = { showPreferenceDialog = false }
        )
    }
}