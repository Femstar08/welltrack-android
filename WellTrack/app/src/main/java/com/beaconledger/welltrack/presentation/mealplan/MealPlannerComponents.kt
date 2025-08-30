package com.beaconledger.welltrack.presentation.mealplan

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.beaconledger.welltrack.data.model.MealStatus
import com.beaconledger.welltrack.data.model.MealType
import com.beaconledger.welltrack.data.model.PlannedMealStatus
import com.beaconledger.welltrack.data.model.PlannedMeal
import com.beaconledger.welltrack.data.model.PlannedMealStatus
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

// Meal Planner Header (matches React MealPlannerHeader)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlannerHeader(
    currentWeek: LocalDate,
    selectedUser: String,
    onWeekChange: (String) -> Unit,
    onUserChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val weekStart = currentWeek.with(DayOfWeek.MONDAY)
    val weekEnd = weekStart.plusDays(6)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(16.dp)
    ) {
        // Week Navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onWeekChange("prev") }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous Week",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${weekStart.format(DateTimeFormatter.ofPattern("MMM d"))} - ${weekEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Week ${weekStart.format(DateTimeFormatter.ofPattern("w"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            IconButton(
                onClick = { onWeekChange("next") }
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next Week",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // User Selection (for multi-profile support)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            FilterChip(
                onClick = { /* Show user selection dialog */ },
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (selectedUser == "current") "My Plan" else "Family Plan",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                selected = false,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    labelColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

// Quick Actions Bar (matches React QuickActionsBar)
@Composable
fun QuickActionsBar(
    mealPrepMode: Boolean,
    showSuggestions: Boolean,
    onMealPrepModeChange: (Boolean) -> Unit,
    onShowSuggestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal Prep Mode Toggle
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = mealPrepMode,
                    onCheckedChange = onMealPrepModeChange,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Meal Prep Mode",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (mealPrepMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Smart Suggestions Toggle
            FilterChip(
                onClick = onShowSuggestions,
                label = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Smart Suggestions",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                selected = showSuggestions,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

// Weekly Calendar (matches React WeeklyCalendar)
@Composable
fun WeeklyCalendar(
    currentWeek: LocalDate,
    mealPrepMode: Boolean,
    modifier: Modifier = Modifier
) {
    val weekStart = currentWeek.with(DayOfWeek.MONDAY)
    val weekDays = (0..6).map { weekStart.plusDays(it.toLong()) }
    
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(weekDays) { date ->
            DayMealPlanCard(
                date = date,
                mealPrepMode = mealPrepMode,
                meals = getSampleMealsForDate(date)
            )
        }
    }
}

@Composable
fun DayMealPlanCard(
    date: LocalDate,
    mealPrepMode: Boolean,
    meals: List<PlannedMeal>,
    modifier: Modifier = Modifier
) {
    val isToday = date == LocalDate.now()
    val dayName = date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isToday) 4.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isToday) 
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Day Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("MMM d")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (isToday) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "Today",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Meal Slots
            val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
            
            mealTypes.forEach { mealType ->
                val meal = meals.find { it.mealType.name == mealType.uppercase() }
                MealSlot(
                    mealType = mealType,
                    meal = meal,
                    mealPrepMode = mealPrepMode,
                    onMealClick = { /* Handle meal click */ },
                    onAddMeal = { /* Handle add meal */ }
                )
                
                if (mealType != mealTypes.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun MealSlot(
    mealType: String,
    meal: PlannedMeal?,
    mealPrepMode: Boolean,
    onMealClick: (PlannedMeal) -> Unit,
    onAddMeal: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { 
                if (meal != null) onMealClick(meal) else onAddMeal(mealType)
            },
        shape = RoundedCornerShape(8.dp),
        color = if (meal != null) 
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else 
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Meal Type Icon
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = getMealTypeColor(mealType).copy(alpha = 0.2f)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getMealTypeIcon(mealType),
                        contentDescription = mealType,
                        modifier = Modifier.size(16.dp),
                        tint = getMealTypeColor(mealType)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mealType,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (meal != null) {
                    Text(
                        text = meal.recipeId ?: "Unknown Recipe",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (mealPrepMode) {
                        Text(
                            text = "Prep: Ready",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "Add meal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Status indicators
            if (meal != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (meal.isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else if (mealPrepMode && meal.status == PlannedMealStatus.PLANNED) {
                        Icon(
                            Icons.Default.Fastfood,
                            contentDescription = "Prepared",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add meal",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Smart Suggestions Panel (matches React SmartSuggestions)
@Composable
fun SmartSuggestionsPanel(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Smart Suggestions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            SuggestionCategory(
                title = "Based on Your Preferences",
                suggestions = getSampleSuggestions("preferences")
            )
        }
        
        item {
            SuggestionCategory(
                title = "Pantry Ingredients",
                suggestions = getSampleSuggestions("pantry")
            )
        }
        
        item {
            SuggestionCategory(
                title = "Quick & Easy",
                suggestions = getSampleSuggestions("quick")
            )
        }
    }
}

@Composable
fun SuggestionCategory(
    title: String,
    suggestions: List<MealSuggestion>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        suggestions.forEach { suggestion ->
            SuggestionCard(
                suggestion = suggestion,
                onAddToMealPlan = { /* Handle add to meal plan */ }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun SuggestionCard(
    suggestion: MealSuggestion,
    onAddToMealPlan: (MealSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = suggestion.recipeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${suggestion.prepTime} min • ${suggestion.calories} cal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (suggestion.reason.isNotEmpty()) {
                    Text(
                        text = suggestion.reason,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            
            IconButton(
                onClick = { onAddToMealPlan(suggestion) }
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add to meal plan",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Data classes and helper functions
// Note: Using PlannedMeal from data.model package instead of duplicate here

data class MealSuggestion(
    val id: String,
    val recipeName: String,
    val prepTime: Int,
    val calories: Int,
    val reason: String
)

@Composable
fun getMealTypeColor(mealType: String): Color {
    return when (mealType.lowercase()) {
        "breakfast" -> MaterialTheme.colorScheme.tertiary
        "lunch" -> MaterialTheme.colorScheme.primary
        "dinner" -> MaterialTheme.colorScheme.secondary
        "snack" -> MaterialTheme.colorScheme.outline
        else -> MaterialTheme.colorScheme.primary
    }
}

fun getMealTypeIcon(mealType: String): ImageVector {
    return when (mealType.lowercase()) {
        "breakfast" -> Icons.Default.WbSunny
        "lunch" -> Icons.Default.Fastfood
        "dinner" -> Icons.Default.Fastfood
        "snack" -> Icons.Default.Cake
        else -> Icons.Default.Fastfood
    }
}

// Sample data functions
fun getSampleMealsForDate(date: LocalDate): List<PlannedMeal> {
    return listOf(
        PlannedMeal(
            id = "1",
            mealPlanId = "plan-1",
            userId = "user-1",
            date = date.toString(),
            mealType = MealType.BREAKFAST,
            customMealName = "Overnight Oats with Berries",
            isCompleted = date.isBefore(LocalDate.now())
        ),
        PlannedMeal(
            id = "2",
            mealPlanId = "plan-1",
            userId = "user-1",
            date = date.toString(),
            mealType = MealType.LUNCH,
            customMealName = "Quinoa Buddha Bowl",
            isCompleted = date.isBefore(LocalDate.now())
        )
    )
}

fun getSampleSuggestions(category: String): List<MealSuggestion> {
    return when (category) {
        "preferences" -> listOf(
            MealSuggestion("1", "Mediterranean Chicken Bowl", 30, 420, "Matches your protein goals"),
            MealSuggestion("2", "Vegetarian Stir Fry", 20, 350, "Based on your dietary preferences")
        )
        "pantry" -> listOf(
            MealSuggestion("3", "Pasta with Tomato Sauce", 15, 380, "Uses ingredients you have"),
            MealSuggestion("4", "Rice and Bean Bowl", 25, 340, "Uses pantry staples")
        )
        "quick" -> listOf(
            MealSuggestion("5", "Avocado Toast", 5, 280, "Ready in 5 minutes"),
            MealSuggestion("6", "Smoothie Bowl", 10, 320, "Quick and nutritious")
        )
        else -> emptyList()
    }
}