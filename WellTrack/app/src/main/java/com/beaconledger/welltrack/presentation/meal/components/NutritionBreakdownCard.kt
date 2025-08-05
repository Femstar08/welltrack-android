package com.beaconledger.welltrack.presentation.meal.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.meal.MealScoreBreakdown
import com.beaconledger.welltrack.data.model.NutritionInfo
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NutritionBreakdownCard(
    nutritionInfo: NutritionInfo,
    scoreBreakdown: MealScoreBreakdown,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
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
                    text = "Nutritional Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Overall Score Badge
                Surface(
                    color = Color(android.graphics.Color.parseColor(scoreBreakdown.grade.colorCode)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = scoreBreakdown.grade.grade,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macronutrient Chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Pie Chart
                MacronutrientPieChart(
                    nutritionInfo = nutritionInfo,
                    modifier = Modifier.size(120.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Macronutrient Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MacronutrientLegendItem(
                        color = Color(0xFF4CAF50),
                        label = "Protein",
                        value = "${nutritionInfo.proteins.toInt()}g",
                        percentage = calculateMacroPercentage(nutritionInfo.proteins * 4, nutritionInfo.calories)
                    )
                    MacronutrientLegendItem(
                        color = Color(0xFF2196F3),
                        label = "Carbs",
                        value = "${nutritionInfo.carbohydrates.toInt()}g",
                        percentage = calculateMacroPercentage(nutritionInfo.carbohydrates * 4, nutritionInfo.calories)
                    )
                    MacronutrientLegendItem(
                        color = Color(0xFFFF9800),
                        label = "Fat",
                        value = "${nutritionInfo.fats.toInt()}g",
                        percentage = calculateMacroPercentage(nutritionInfo.fats * 9, nutritionInfo.calories)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Detailed Nutrition Grid
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Detailed Nutrition",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            NutritionDetailRow("Calories", "${nutritionInfo.calories.toInt()}")
                            NutritionDetailRow("Fiber", "${nutritionInfo.fiber.toInt()}g")
                            NutritionDetailRow("Sodium", "${nutritionInfo.sodium.toInt()}mg")
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            NutritionDetailRow("Potassium", "${nutritionInfo.potassium.toInt()}mg")
                            if (nutritionInfo.micronutrients.isNotEmpty()) {
                                NutritionDetailRow("Vitamins", "${nutritionInfo.micronutrients.size}")
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Score Breakdown
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Score Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ScoreBreakdownItem("Protein Quality", scoreBreakdown.proteinScore, 15)
                    ScoreBreakdownItem("Fiber Content", scoreBreakdown.fiberScore, 15)
                    ScoreBreakdownItem("Fat Balance", scoreBreakdown.fatScore, 10)
                    ScoreBreakdownItem("Sodium Level", scoreBreakdown.sodiumScore, 10)
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = scoreBreakdown.overallFeedback,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MacronutrientPieChart(
    nutritionInfo: NutritionInfo,
    modifier: Modifier = Modifier
) {
    val proteinCalories = nutritionInfo.proteins * 4
    val carbCalories = nutritionInfo.carbohydrates * 4
    val fatCalories = nutritionInfo.fats * 9
    val totalCalories = proteinCalories + carbCalories + fatCalories
    
    if (totalCalories <= 0) return
    
    val proteinAngle = (proteinCalories / totalCalories * 360).toFloat()
    val carbAngle = (carbCalories / totalCalories * 360).toFloat()
    val fatAngle = (fatCalories / totalCalories * 360).toFloat()
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        
        var startAngle = -90f
        
        // Protein slice
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = startAngle,
            sweepAngle = proteinAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += proteinAngle
        
        // Carb slice
        drawArc(
            color = Color(0xFF2196F3),
            startAngle = startAngle,
            sweepAngle = carbAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += carbAngle
        
        // Fat slice
        drawArc(
            color = Color(0xFFFF9800),
            startAngle = startAngle,
            sweepAngle = fatAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
    }
}

@Composable
private fun MacronutrientLegendItem(
    color: Color,
    label: String,
    value: String,
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(12.dp),
            color = color,
            shape = MaterialTheme.shapes.extraSmall
        ) {}
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "$value ($percentage%)",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun NutritionDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ScoreBreakdownItem(
    label: String,
    score: Int,
    maxScore: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = "$score/$maxScore",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        LinearProgressIndicator(
            progress = (score.toFloat() / maxScore).coerceIn(0f, 1f),
            modifier = Modifier.width(60.dp),
            color = when {
                score >= maxScore * 0.8 -> Color(0xFF4CAF50)
                score >= maxScore * 0.6 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }
        )
    }
}

private fun calculateMacroPercentage(macroCalories: Double, totalCalories: Double): Int {
    return if (totalCalories > 0) {
        ((macroCalories / totalCalories) * 100).toInt()
    } else {
        0
    }
}