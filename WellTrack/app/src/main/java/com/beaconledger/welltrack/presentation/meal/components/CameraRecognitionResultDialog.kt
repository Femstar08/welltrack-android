package com.beaconledger.welltrack.presentation.meal.components

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
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.domain.usecase.MealFromCameraResult

@Composable
fun CameraRecognitionResultDialog(
    result: MealFromCameraResult,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
        ) {
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
                        text = "Meal Recognition Result",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recognition Confidence
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = getConfidenceColor(result.recognitionResult.confidence)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recognition Confidence: ${(result.recognitionResult.confidence * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Suggested Meal Name
                Text(
                    text = "Suggested Name: ${result.recognitionResult.suggestedMealName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Recognized Ingredients
                Text(
                    text = "Recognized Ingredients",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(result.recognitionResult.recognizedIngredients) { ingredient ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = ingredient.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${ingredient.quantity} ${ingredient.unit}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nutrition Summary
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Estimated Nutrition",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val nutrition = result.recognitionResult.nutritionInfo
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            NutritionItem(
                                label = "Calories",
                                value = "${nutrition.calories.toInt()}",
                                modifier = Modifier.weight(1f)
                            )
                            NutritionItem(
                                label = "Protein",
                                value = "${nutrition.proteins.toInt()}g",
                                modifier = Modifier.weight(1f)
                            )
                            NutritionItem(
                                label = "Carbs",
                                value = "${nutrition.carbohydrates.toInt()}g",
                                modifier = Modifier.weight(1f)
                            )
                            NutritionItem(
                                label = "Fat",
                                value = "${nutrition.fats.toInt()}g",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal Score
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Meal Score:",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Surface(
                        color = Color(android.graphics.Color.parseColor(result.meal.score.colorCode)),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = result.meal.score.grade,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
                        Text("Close")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Got it!")
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getConfidenceColor(confidence: Float): Color {
    return when {
        confidence >= 0.7f -> Color(0xFF4CAF50) // Green
        confidence >= 0.5f -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}