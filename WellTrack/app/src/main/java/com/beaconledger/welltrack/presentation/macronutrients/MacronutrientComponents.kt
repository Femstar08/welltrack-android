package com.beaconledger.welltrack.presentation.macronutrients

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.repository.TrendDirection
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CoreNutrientsCard(
    summary: MacronutrientSummary?,
    onWaterAdd: () -> Unit
) {
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
                    text = "Core Nutrients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (summary != null) {
                    Text(
                        text = "${summary.totalCalories} kcal",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (summary != null) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        NutrientProgressItem(
                            name = "Protein",
                            current = summary.totalProtein,
                            target = summary.targets?.proteinGrams ?: 0.0,
                            unit = "g",
                            progress = summary.proteinProgress,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    item {
                        NutrientProgressItem(
                            name = "Carbs",
                            current = summary.totalCarbs,
                            target = summary.targets?.carbsGrams ?: 0.0,
                            unit = "g",
                            progress = summary.carbsProgress,
                            color = Color(0xFF2196F3)
                        )
                    }
                    item {
                        NutrientProgressItem(
                            name = "Fat",
                            current = summary.totalFat,
                            target = summary.targets?.fatGrams ?: 0.0,
                            unit = "g",
                            progress = summary.fatProgress,
                            color = Color(0xFFFF9800)
                        )
                    }
                    item {
                        NutrientProgressItem(
                            name = "Fiber",
                            current = summary.totalFiber,
                            target = summary.targets?.fiberGrams ?: 0.0,
                            unit = "g",
                            progress = summary.fiberProgress,
                            color = Color(0xFF9C27B0)
                        )
                    }
                    item {
                        WaterProgressItem(
                            current = summary.totalWater,
                            target = summary.targets?.waterMl ?: 0,
                            progress = summary.waterProgress,
                            onAddWater = onWaterAdd
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No data for selected date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun NutrientProgressItem(
    name: String,
    current: Double,
    target: Double,
    unit: String,
    progress: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(60.dp),
                color = color,
                strokeWidth = 6.dp,
                trackColor = color.copy(alpha = 0.2f)
            )
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "${current.toInt()}/${target.toInt()}$unit",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WaterProgressItem(
    current: Int,
    target: Int,
    progress: Float,
    onAddWater: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.size(60.dp),
                color = Color(0xFF00BCD4),
                strokeWidth = 6.dp,
                trackColor = Color(0xFF00BCD4).copy(alpha = 0.2f)
            )
            IconButton(
                onClick = onAddWater,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Water",
                    tint = Color(0xFF00BCD4)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Water",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        
        Text(
            text = "${current}/${target}ml",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun MacronutrientBalanceCard(
    balance: MacronutrientBalance
) {
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
                    text = "Macronutrient Balance",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Icon(
                    imageVector = if (balance.isBalanced) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (balance.isBalanced) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pie chart representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                MacronutrientPieChart(
                    proteinPercentage = balance.proteinPercentage,
                    carbsPercentage = balance.carbsPercentage,
                    fatPercentage = balance.fatPercentage
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroLegendItem(
                    color = Color(0xFF4CAF50),
                    label = "Protein",
                    percentage = balance.proteinPercentage,
                    calories = balance.caloriesFromProtein
                )
                MacroLegendItem(
                    color = Color(0xFF2196F3),
                    label = "Carbs",
                    percentage = balance.carbsPercentage,
                    calories = balance.caloriesFromCarbs
                )
                MacroLegendItem(
                    color = Color(0xFFFF9800),
                    label = "Fat",
                    percentage = balance.fatPercentage,
                    calories = balance.caloriesFromFat
                )
            }

            // Recommendations
            if (balance.recommendations.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Recommendations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                balance.recommendations.forEach { recommendation ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        modifier = Modifier.padding(vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = recommendation,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacronutrientPieChart(
    proteinPercentage: Float,
    carbsPercentage: Float,
    fatPercentage: Float,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier.size(150.dp)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.8f
        
        // Convert percentages to angles
        val proteinAngle = (proteinPercentage / 100f) * 360f
        val carbsAngle = (carbsPercentage / 100f) * 360f
        val fatAngle = (fatPercentage / 100f) * 360f
        
        var startAngle = -90f
        
        // Draw protein arc
        drawArc(
            color = Color(0xFF4CAF50),
            startAngle = startAngle,
            sweepAngle = proteinAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += proteinAngle
        
        // Draw carbs arc
        drawArc(
            color = Color(0xFF2196F3),
            startAngle = startAngle,
            sweepAngle = carbsAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += carbsAngle
        
        // Draw fat arc
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
fun MacroLegendItem(
    color: Color,
    label: String,
    percentage: Float,
    calories: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${calories}kcal",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun CustomNutrientsCard(
    customNutrients: List<CustomNutrient>,
    summary: MacronutrientSummary?,
    onAddCustomNutrient: () -> Unit,
    onRemoveCustomNutrient: (String) -> Unit
) {
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
                    text = "Custom Nutrients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddCustomNutrient) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Custom Nutrient"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            customNutrients.forEach { nutrient ->
                val currentValue = summary?.customNutrientTotals?.get(nutrient.name) ?: 0.0
                val progress = summary?.customNutrientProgress?.get(nutrient.name) ?: 0f
                
                CustomNutrientItem(
                    nutrient = nutrient,
                    currentValue = currentValue,
                    progress = progress,
                    onRemove = { onRemoveCustomNutrient(nutrient.id) }
                )
                
                if (nutrient != customNutrients.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun CustomNutrientItem(
    nutrient: CustomNutrient,
    currentValue: Double,
    progress: Float,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = nutrient.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${currentValue}${if (nutrient.targetValue != null) "/${nutrient.targetValue}" else ""}${nutrient.unit}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (nutrient.targetValue != null) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .width(80.dp)
                    .padding(horizontal = 8.dp),
                color = when (nutrient.priority) {
                    NutrientPriority.CORE -> Color(0xFF4CAF50)
                    NutrientPriority.IMPORTANT -> Color(0xFF2196F3)
                    NutrientPriority.OPTIONAL -> Color(0xFF9E9E9E)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
        
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun NutrientTrendCard(
    nutrientName: String,
    trend: NutrientTrend
) {
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
                    text = "$nutrientName Trend",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (trend.trend) {
                            TrendDirection.INCREASING -> Icons.Default.KeyboardArrowUp
                            TrendDirection.DECREASING -> Icons.Default.KeyboardArrowDown
                            TrendDirection.STABLE -> Icons.Default.Remove
                        },
                        contentDescription = null,
                        tint = when (trend.trend) {
                            TrendDirection.INCREASING -> Color(0xFF4CAF50)
                            TrendDirection.DECREASING -> Color(0xFFF44336)
                            TrendDirection.STABLE -> Color(0xFF9E9E9E)
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${(trend.adherenceRate * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Avg: ${trend.averageIntake.toInt()} / ${trend.averageTarget.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}