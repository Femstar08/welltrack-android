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
import com.beaconledger.welltrack.data.model.*

@Composable
fun AnalyticsTab(
    uiState: IngredientPreferenceUiState
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Most Used Ingredients Section
        if (uiState.mostUsedIngredients.isNotEmpty()) {
            item {
                Text(
                    text = "Most Used Ingredients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(uiState.mostUsedIngredients.take(10)) { stats ->
                UsageStatsCard(stats = stats)
            }
            
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
        
        // Summary Stats
        item {
            AnalyticsSummaryCard(
                preferredCount = uiState.preferredIngredients.size,
                pantryCount = uiState.pantryItems.size,
                alertsCount = uiState.pantryAlerts.size
            )
        }
        
        // Empty state
        if (uiState.mostUsedIngredients.isEmpty()) {
            item {
                EmptyAnalyticsState()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UsageStatsCard(stats: IngredientUsageStats) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.ingredientName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Used ${stats.usageCount} times",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                stats.lastUsed?.let { lastUsed ->
                    Text(
                        text = "Last used: $lastUsed",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = "${stats.usageCount}x",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsSummaryCard(
    preferredCount: Int,
    pantryCount: Int,
    alertsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    icon = Icons.Default.Favorite,
                    count = preferredCount,
                    label = "Preferred"
                )
                
                SummaryItem(
                    icon = Icons.Default.Home,
                    count = pantryCount,
                    label = "In Pantry"
                )
                
                SummaryItem(
                    icon = Icons.Default.Warning,
                    count = alertsCount,
                    label = "Alerts"
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyAnalyticsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No analytics data yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Start using ingredients in recipes and meals to see usage analytics",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}