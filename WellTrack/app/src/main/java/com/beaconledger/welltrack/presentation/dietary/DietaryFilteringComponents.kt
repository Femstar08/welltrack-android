package com.beaconledger.welltrack.presentation.dietary

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*

/**
 * Component for displaying filtered recipes with compatibility information
 */
@Composable
fun FilteredRecipesDisplay(
    filteredRecipes: FilteredRecipes,
    onRecipeClick: (Recipe) -> Unit,
    onShowIncompatible: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Summary Card
        FilteringSummaryCard(
            compatibilityStats = filteredRecipes.compatibilityStats,
            onShowIncompatible = onShowIncompatible
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Compatible Recipes
        if (filteredRecipes.compatibleRecipes.isNotEmpty()) {
            Text(
                text = "Compatible Recipes",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredRecipes.compatibleRecipes) { recipe ->
                    CompatibleRecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) }
                    )
                }
            }
        } else {
            EmptyCompatibleRecipesMessage()
        }
    }
}

/**
 * Summary card showing filtering statistics
 */
@Composable
fun FilteringSummaryCard(
    compatibilityStats: CompatibilityStats,
    onShowIncompatible: () -> Unit,
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
                text = "Filtering Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    label = "Total",
                    value = compatibilityStats.totalRecipes.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
                
                StatisticItem(
                    label = "Compatible",
                    value = compatibilityStats.compatibleCount.toString(),
                    color = Color(0xFF4CAF50) // Green
                )
                
                StatisticItem(
                    label = "Incompatible",
                    value = compatibilityStats.incompatibleCount.toString(),
                    color = Color(0xFFF44336) // Red
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = compatibilityStats.compatibilityRate,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(compatibilityStats.compatibilityRate * 100).toInt()}% compatibility rate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (compatibilityStats.incompatibleCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onShowIncompatible
                ) {
                    Text("View Incompatible Recipes")
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual statistic item
 */
@Composable
fun StatisticItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Card for displaying a compatible recipe
 */
@Composable
fun CompatibleRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Compatible",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${recipe.prepTime + recipe.cookTime} min • ${recipe.servings} servings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "View recipe",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Component for displaying highlighted shopping list items
 */
@Composable
fun HighlightedShoppingListDisplay(
    highlightedList: HighlightedShoppingList,
    onItemClick: (HighlightedShoppingItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Summary
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Shopping List Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticItem(
                        label = "Total Items",
                        value = highlightedList.highlightedItems.size.toString(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    StatisticItem(
                        label = "Restricted",
                        value = highlightedList.restrictedItemsCount.toString(),
                        color = Color(0xFFF44336)
                    )
                    
                    StatisticItem(
                        label = "Warnings",
                        value = highlightedList.warningItemsCount.toString(),
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Items List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(highlightedList.highlightedItems) { item ->
                HighlightedShoppingItemCard(
                    item = item,
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}

/**
 * Card for displaying a highlighted shopping item
 */
@Composable
fun HighlightedShoppingItemCard(
    item: HighlightedShoppingItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightColor = when (item.highlightLevel) {
        HighlightLevel.CRITICAL -> Color(0x20FF0000)
        HighlightLevel.HIGH -> Color(0x20FF6B35)
        HighlightLevel.MEDIUM -> Color(0x20FFA500)
        HighlightLevel.LOW -> Color(0x20FFEB3B)
        HighlightLevel.NONE -> Color.Transparent
    }
    
    val borderColor = when (item.highlightLevel) {
        HighlightLevel.CRITICAL -> Color(0xFFFF0000)
        HighlightLevel.HIGH -> Color(0xFFFF6B35)
        HighlightLevel.MEDIUM -> Color(0xFFFFA500)
        HighlightLevel.LOW -> Color(0xFFFFEB3B)
        HighlightLevel.NONE -> Color.Transparent
    }
    
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (item.highlightLevel != HighlightLevel.NONE) {
                    Modifier
                        .background(highlightColor, RoundedCornerShape(8.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                } else Modifier
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Highlight icon
                when (item.highlightLevel) {
                    HighlightLevel.CRITICAL -> Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Critical",
                        tint = Color(0xFFFF0000),
                        modifier = Modifier.size(24.dp)
                    )
                    HighlightLevel.HIGH -> Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "High",
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(24.dp)
                    )
                    HighlightLevel.MEDIUM -> Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Medium",
                        tint = Color(0xFFFFA500),
                        modifier = Modifier.size(24.dp)
                    )
                    HighlightLevel.LOW -> Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Low",
                        tint = Color(0xFFFFEB3B),
                        modifier = Modifier.size(24.dp)
                    )
                    HighlightLevel.NONE -> Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "No issues",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = "${item.item.quantity} ${item.item.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Show restrictions and warnings
            if (item.restrictions.isNotEmpty() || item.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                item.restrictions.forEach { restriction ->
                    Text(
                        text = "⚠️ ${restriction.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F)
                    )
                }
                
                item.warnings.forEach { warning ->
                    Text(
                        text = "ℹ️ ${warning.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }
            
            // Show alternatives if available
            if (item.suggestedAlternatives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Alternatives: ${item.suggestedAlternatives.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

/**
 * Component for displaying recipe import validation results
 */
@Composable
fun RecipeImportValidationDisplay(
    validation: RecipeImportValidation,
    onProceedWithSubstitutions: () -> Unit,
    onProceedAnyway: () -> Unit,
    onCancel: () -> Unit,
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
                text = "Recipe Import Validation",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Recipe name
            Text(
                text = validation.recipe.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Compatibility status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (validation.compatibility.isCompatible) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (validation.compatibility.isCompatible) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (validation.compatibility.isCompatible) "Compatible" else "Has Issues",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (validation.compatibility.isCompatible) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            }
            
            // Show violations
            if (validation.compatibility.violations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Issues Found:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                validation.compatibility.violations.forEach { violation ->
                    Text(
                        text = "• ${violation.description}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
            
            // Show substitutions if available
            if (validation.suggestedSubstitutions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Suggested Substitutions:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                validation.suggestedSubstitutions.forEach { (ingredient, substitutions) ->
                    Text(
                        text = "• $ingredient → ${substitutions.first().substituteIngredient}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (validation.canImport) {
                    if (validation.suggestedSubstitutions.isNotEmpty()) {
                        Button(
                            onClick = onProceedWithSubstitutions,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Import with Substitutions")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onProceedAnyway,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Import As-Is")
                    }
                } else {
                    Text(
                        text = "Cannot import due to medical restrictions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                }
                
                TextButton(
                    onClick = onCancel
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

/**
 * Empty state when no compatible recipes are found
 */
@Composable
fun EmptyCompatibleRecipesMessage(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Compatible Recipes Found",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Try adjusting your dietary restrictions or filter criteria to see more recipes.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}