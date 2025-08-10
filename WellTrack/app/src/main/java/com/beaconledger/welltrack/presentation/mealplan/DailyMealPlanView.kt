package com.beaconledger.welltrack.presentation.mealplan

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyMealPlanView(
    dailyMealPlan: DailyMealPlan?,
    selectedDate: LocalDate,
    onMealCompleted: (String) -> Unit,
    onMealSkipped: (String) -> Unit,
    onMealEdit: (PlannedMeal) -> Unit,
    onMealDelete: (String) -> Unit,
    onAddMeal: (MealType) -> Unit,
    onNavigateToRecipe: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Date header
        Text(
            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (dailyMealPlan == null) {
            // Empty state
            EmptyDayView(
                onAddMeal = onAddMeal,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Breakfast
                item {
                    MealSection(
                        mealType = MealType.BREAKFAST,
                        plannedMeal = dailyMealPlan.breakfast,
                        onMealCompleted = onMealCompleted,
                        onMealSkipped = onMealSkipped,
                        onMealEdit = onMealEdit,
                        onMealDelete = onMealDelete,
                        onAddMeal = onAddMeal,
                        onNavigateToRecipe = onNavigateToRecipe
                    )
                }

                // Lunch
                item {
                    MealSection(
                        mealType = MealType.LUNCH,
                        plannedMeal = dailyMealPlan.lunch,
                        onMealCompleted = onMealCompleted,
                        onMealSkipped = onMealSkipped,
                        onMealEdit = onMealEdit,
                        onMealDelete = onMealDelete,
                        onAddMeal = onAddMeal,
                        onNavigateToRecipe = onNavigateToRecipe
                    )
                }

                // Dinner
                item {
                    MealSection(
                        mealType = MealType.DINNER,
                        plannedMeal = dailyMealPlan.dinner,
                        onMealCompleted = onMealCompleted,
                        onMealSkipped = onMealSkipped,
                        onMealEdit = onMealEdit,
                        onMealDelete = onMealDelete,
                        onAddMeal = onAddMeal,
                        onNavigateToRecipe = onNavigateToRecipe
                    )
                }

                // Snacks
                if (dailyMealPlan.snacks.isNotEmpty()) {
                    item {
                        Text(
                            text = "Snacks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(dailyMealPlan.snacks) { snack ->
                        PlannedMealCard(
                            plannedMeal = snack,
                            onMealCompleted = onMealCompleted,
                            onMealSkipped = onMealSkipped,
                            onMealEdit = onMealEdit,
                            onMealDelete = onMealDelete,
                            onNavigateToRecipe = onNavigateToRecipe
                        )
                    }
                }

                // Supplements
                if (dailyMealPlan.supplements.isNotEmpty()) {
                    item {
                        Text(
                            text = "Supplements",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    items(dailyMealPlan.supplements) { supplement ->
                        SupplementCard(
                            plannedSupplement = supplement,
                            onSupplementCompleted = { /* Handle supplement completion */ }
                        )
                    }
                }

                // Nutrition summary
                dailyMealPlan.totalNutrition?.let { nutrition ->
                    item {
                        NutritionSummaryCard(
                            nutrition = nutrition,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyDayView(
    onAddMeal: (MealType) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Home,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "No meals planned for this day",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "Generate a meal plan or add meals manually",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onAddMeal(MealType.BREAKFAST) }
                ) {
                    Text("Add Breakfast")
                }
                
                OutlinedButton(
                    onClick = { onAddMeal(MealType.LUNCH) }
                ) {
                    Text("Add Lunch")
                }
                
                OutlinedButton(
                    onClick = { onAddMeal(MealType.DINNER) }
                ) {
                    Text("Add Dinner")
                }
            }
        }
    }
}

@Composable
private fun MealSection(
    mealType: MealType,
    plannedMeal: PlannedMeal?,
    onMealCompleted: (String) -> Unit,
    onMealSkipped: (String) -> Unit,
    onMealEdit: (PlannedMeal) -> Unit,
    onMealDelete: (String) -> Unit,
    onAddMeal: (MealType) -> Unit,
    onNavigateToRecipe: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mealType.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            if (plannedMeal == null) {
                TextButton(
                    onClick = { onAddMeal(mealType) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (plannedMeal != null) {
            PlannedMealCard(
                plannedMeal = plannedMeal,
                onMealCompleted = onMealCompleted,
                onMealSkipped = onMealSkipped,
                onMealEdit = onMealEdit,
                onMealDelete = onMealDelete,
                onNavigateToRecipe = onNavigateToRecipe
            )
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${mealType.name.lowercase()} planned",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PlannedMealCard(
    plannedMeal: PlannedMeal,
    onMealCompleted: (String) -> Unit,
    onMealSkipped: (String) -> Unit,
    onMealEdit: (PlannedMeal) -> Unit,
    onMealDelete: (String) -> Unit,
    onNavigateToRecipe: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (plannedMeal.status) {
                PlannedMealStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                PlannedMealStatus.SKIPPED -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plannedMeal.customMealName ?: "Recipe Meal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (plannedMeal.status == PlannedMealStatus.COMPLETED) 
                            TextDecoration.LineThrough else TextDecoration.None
                    )
                    
                    if (plannedMeal.servings > 1) {
                        Text(
                            text = "${plannedMeal.servings} servings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    plannedMeal.notes?.let { notes ->
                        Text(
                            text = notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (plannedMeal.status != PlannedMealStatus.COMPLETED) {
                            DropdownMenuItem(
                                text = { Text("Mark as completed") },
                                onClick = {
                                    onMealCompleted(plannedMeal.id)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Check, contentDescription = null) }
                            )
                        }
                        
                        if (plannedMeal.status != PlannedMealStatus.SKIPPED) {
                            DropdownMenuItem(
                                text = { Text("Mark as skipped") },
                                onClick = {
                                    onMealSkipped(plannedMeal.id)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Close, contentDescription = null) }
                            )
                        }
                        
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                onMealEdit(plannedMeal)
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        
                        if (plannedMeal.recipeId != null) {
                            DropdownMenuItem(
                                text = { Text("View recipe") },
                                onClick = {
                                    onNavigateToRecipe(plannedMeal.recipeId)
                                    showMenu = false
                                },
                                leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onMealDelete(plannedMeal.id)
                                showMenu = false
                            },
                            leadingIcon = { 
                                Icon(
                                    Icons.Default.Delete, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                ) 
                            }
                        )
                    }
                }
            }
            
            // Status indicator
            when (plannedMeal.status) {
                PlannedMealStatus.COMPLETED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Completed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                PlannedMealStatus.SKIPPED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Skipped",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    // Show action buttons for planned meals
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onMealCompleted(plannedMeal.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Complete")
                        }
                        
                        OutlinedButton(
                            onClick = { onMealSkipped(plannedMeal.id) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Skip")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupplementCard(
    plannedSupplement: PlannedSupplement,
    onSupplementCompleted: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (plannedSupplement.isCompleted) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        )
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
                    text = plannedSupplement.supplementName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${plannedSupplement.dosage} • ${plannedSupplement.timing.name.lowercase().replace('_', ' ')}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (!plannedSupplement.isCompleted) {
                IconButton(
                    onClick = { onSupplementCompleted(plannedSupplement.id) }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Mark as taken")
                }
            } else {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Completed",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun NutritionSummaryCard(
    nutrition: NutritionInfo,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Daily Nutrition Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem(
                    label = "Calories",
                    value = "${nutrition.calories}",
                    unit = "kcal"
                )
                
                NutritionItem(
                    label = "Protein",
                    value = "${nutrition.proteins.toInt()}",
                    unit = "g"
                )
                
                NutritionItem(
                    label = "Carbs",
                    value = "${nutrition.carbohydrates.toInt()}",
                    unit = "g"
                )
                
                NutritionItem(
                    label = "Fat",
                    value = "${nutrition.fats.toInt()}",
                    unit = "g"
                )
            }
        }
    }
}

@Composable
private fun NutritionItem(
    label: String,
    value: String,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}