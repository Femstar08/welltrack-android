package com.beaconledger.welltrack.presentation.recipe

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeScalingDialog(
    recipe: Recipe,
    ingredients: List<Ingredient>,
    onDismiss: () -> Unit,
    onStartCooking: (Int) -> Unit
) {
    var targetServings by remember { mutableStateOf(recipe.servings.toString()) }
    val scalingFactor = remember(targetServings) {
        val target = targetServings.toIntOrNull() ?: recipe.servings
        target.toFloat() / recipe.servings.toFloat()
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Scale Recipe",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recipe Info
                Text(
                    text = recipe.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Original: ${recipe.servings} servings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Serving Size Input
                OutlinedTextField(
                    value = targetServings,
                    onValueChange = { targetServings = it },
                    label = { Text("Target Servings") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Scaling Factor Display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Scaling Factor: ${String.format("%.2f", scalingFactor)}x",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Scaled Ingredients Preview
                Text(
                    text = "Scaled Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(ingredients) { ingredient ->
                        ScaledIngredientItem(
                            ingredient = ingredient,
                            scalingFactor = scalingFactor
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val target = targetServings.toIntOrNull() ?: recipe.servings
                            onStartCooking(target)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = targetServings.toIntOrNull() != null && targetServings.toIntOrNull()!! > 0
                    ) {
                        Text("Start Cooking")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScaledIngredientItem(
    ingredient: Ingredient,
    scalingFactor: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = ingredient.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${String.format("%.1f", ingredient.quantity * scalingFactor)} ${ingredient.unit}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            if (scalingFactor != 1f) {
                Text(
                    text = "was ${ingredient.quantity} ${ingredient.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}