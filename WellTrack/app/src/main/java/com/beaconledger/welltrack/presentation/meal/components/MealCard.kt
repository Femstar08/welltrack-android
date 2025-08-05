package com.beaconledger.welltrack.presentation.meal.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.Meal
import com.beaconledger.welltrack.data.model.MealStatus
import com.beaconledger.welltrack.data.model.NutritionInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MealCard(
    meal: Meal,
    nutritionInfo: NutritionInfo?,
    onStatusChange: (MealStatus) -> Unit,
    onRatingChange: (Float?) -> Unit = {},
    onFavoriteToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showNutritionDetails by remember { mutableStateOf(false) }
    
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = meal.mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = formatTimestamp(meal.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Meal Score Badge
                Surface(
                    color = Color(android.graphics.Color.parseColor(meal.score.colorCode)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = meal.score.grade,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Nutrition Summary
            nutritionInfo?.let { nutrition ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    NutritionSummaryItem(
                        label = "Calories",
                        value = "${nutrition.calories.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                    NutritionSummaryItem(
                        label = "Protein",
                        value = "${nutrition.proteins.toInt()}g",
                        modifier = Modifier.weight(1f)
                    )
                    NutritionSummaryItem(
                        label = "Carbs",
                        value = "${nutrition.carbohydrates.toInt()}g",
                        modifier = Modifier.weight(1f)
                    )
                    NutritionSummaryItem(
                        label = "Fat",
                        value = "${nutrition.fats.toInt()}g",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = meal.status == MealStatus.EATEN,
                        onClick = { onStatusChange(MealStatus.EATEN) },
                        label = { Text("Eaten") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    
                    FilterChip(
                        selected = meal.status == MealStatus.SKIPPED,
                        onClick = { onStatusChange(MealStatus.SKIPPED) },
                        label = { Text("Skipped") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
                
                // Details Button
                TextButton(
                    onClick = { showNutritionDetails = !showNutritionDetails }
                ) {
                    Text("Details")
                    Icon(
                        if (showNutritionDetails) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
            
            // Rating and Favorites Row
            if (meal.status == MealStatus.EATEN) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Star Rating
                    StarRatingBar(
                        rating = meal.rating ?: 0f,
                        onRatingChanged = onRatingChange,
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Favorite Button
                    IconButton(
                        onClick = { onFavoriteToggle(!meal.isFavorite) }
                    ) {
                        Icon(
                            if (meal.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (meal.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (meal.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Expanded Nutrition Details
            if (showNutritionDetails && nutritionInfo != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                
                NutritionDetailsSection(nutritionInfo = nutritionInfo)
            }
            
            // Notes
            meal.notes?.let { notes ->
                if (notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes: $notes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionSummaryItem(
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

@Composable
private fun NutritionDetailsSection(
    nutritionInfo: NutritionInfo
) {
    Column {
        Text(
            text = "Detailed Nutrition",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                NutritionDetailItem("Fiber", "${nutritionInfo.fiber.toInt()}g")
                NutritionDetailItem("Sodium", "${nutritionInfo.sodium.toInt()}mg")
            }
            Column(modifier = Modifier.weight(1f)) {
                NutritionDetailItem("Potassium", "${nutritionInfo.potassium.toInt()}mg")
                if (nutritionInfo.micronutrients.isNotEmpty()) {
                    Text(
                        text = "Micronutrients: ${nutritionInfo.micronutrients.size}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun NutritionDetailItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatTimestamp(timestamp: String): String {
    return try {
        val dateTime = LocalDateTime.parse(timestamp)
        dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (e: Exception) {
        "Unknown time"
    }
}

@Composable
private fun StarRatingBar(
    rating: Float,
    onRatingChanged: (Float?) -> Unit,
    modifier: Modifier = Modifier,
    maxRating: Int = 5
) {
    var currentRating by remember { mutableStateOf(rating) }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Rate:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        repeat(maxRating) { index ->
            val starIndex = index + 1
            IconButton(
                onClick = {
                    val newRating = if (currentRating == starIndex.toFloat()) null else starIndex.toFloat()
                    currentRating = newRating ?: 0f
                    onRatingChanged(newRating)
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rate $starIndex stars",
                    tint = if (starIndex <= currentRating) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        if (rating > 0) {
            Text(
                text = "(${rating.toInt()})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}