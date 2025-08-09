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
fun PreferencesTab(
    uiState: IngredientPreferenceUiState,
    onAddPreferred: (String, Int) -> Unit,
    onAddDisliked: (String, String?) -> Unit,
    onAddAllergic: (String, String?) -> Unit,
    onRemovePreference: (String) -> Unit,
    onShowAddDialog: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Add button
        Button(
            onClick = onShowAddDialog,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Ingredient Preference")
        }
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Preferred Ingredients Section
            if (uiState.preferredIngredients.isNotEmpty()) {
                item {
                    PreferenceSectionHeader(
                        title = "Preferred Ingredients",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFF4CAF50)
                    )
                }
                
                items(uiState.preferredIngredients) { preference ->
                    IngredientPreferenceCard(
                        preference = preference,
                        onRemove = { onRemovePreference(preference.ingredientName) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            
            // Disliked Ingredients Section
            if (uiState.dislikedIngredients.isNotEmpty()) {
                item {
                    PreferenceSectionHeader(
                        title = "Disliked Ingredients",
                        icon = Icons.Default.ThumbDown,
                        color = Color(0xFFFF9800)
                    )
                }
                
                items(uiState.dislikedIngredients) { preference ->
                    IngredientPreferenceCard(
                        preference = preference,
                        onRemove = { onRemovePreference(preference.ingredientName) }
                    )
                }
                
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            
            // Allergic Ingredients Section
            if (uiState.allergicIngredients.isNotEmpty()) {
                item {
                    PreferenceSectionHeader(
                        title = "Allergic Ingredients",
                        icon = Icons.Default.Warning,
                        color = Color(0xFFF44336)
                    )
                }
                
                items(uiState.allergicIngredients) { preference ->
                    IngredientPreferenceCard(
                        preference = preference,
                        onRemove = { onRemovePreference(preference.ingredientName) }
                    )
                }
            }
            
            // Empty state
            if (uiState.preferences.isEmpty()) {
                item {
                    EmptyPreferencesState(onShowAddDialog)
                }
            }
        }
    }
}

@Composable
private fun PreferenceSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IngredientPreferenceCard(
    preference: IngredientPreference,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = preference.ingredientName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (preference.preferenceType == PreferenceType.PREFERRED && preference.priority > 0) {
                    Text(
                        text = "Priority: ${preference.priority}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                preference.notes?.let { notes ->
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove preference",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun EmptyPreferencesState(
    onShowAddDialog: () -> Unit
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
            text = "No ingredient preferences yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Add your preferred, disliked, or allergic ingredients to get personalized meal suggestions",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )
        
        Button(onClick = onShowAddDialog) {
            Text("Add Your First Preference")
        }
    }
}