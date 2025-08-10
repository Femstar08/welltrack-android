package com.beaconledger.welltrack.presentation.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.beaconledger.welltrack.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onNavigateToRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealPlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentWeekStart by viewModel.currentWeekStart.collectAsStateWithLifecycle()

    var showPreferencesDialog by remember { mutableStateOf(false) }
    var showAddMealDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf<MealType?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with title and actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meal Plan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row {
                IconButton(
                    onClick = { showPreferencesDialog = true }
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Preferences")
                }
                
                IconButton(
                    onClick = { viewModel.showMealPrepSchedule() }
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = "Meal Prep")
                }
                
                Button(
                    onClick = { viewModel.generateMealPlan() },
                    enabled = !uiState.isGenerating
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.Star, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Week navigation
        WeekNavigationRow(
            currentWeekStart = currentWeekStart,
            onWeekChanged = viewModel::navigateToWeek
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar view
        WeekCalendarView(
            weekStartDate = currentWeekStart,
            selectedDate = selectedDate,
            weeklyMealPlan = uiState.weeklyMealPlan,
            onDateSelected = viewModel::selectDate
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Daily meal plan
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            DailyMealPlanView(
                dailyMealPlan = uiState.dailyMealPlan,
                selectedDate = selectedDate,
                onMealCompleted = viewModel::markMealAsCompleted,
                onMealSkipped = viewModel::markMealAsSkipped,
                onMealEdit = { plannedMeal ->
                    // Handle meal editing
                },
                onMealDelete = viewModel::deletePlannedMeal,
                onAddMeal = { mealType ->
                    selectedMealType = mealType
                    showAddMealDialog = true
                },
                onNavigateToRecipe = onNavigateToRecipe
            )
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show snackbar or error dialog
            viewModel.clearError()
        }
    }

    // Meal prep schedule dialog
    if (uiState.showMealPrepDialog) {
        uiState.mealPrepSchedule?.let { schedule ->
            MealPrepScheduleDialog(
                schedule = schedule,
                onDismiss = viewModel::hideMealPrepSchedule
            )
        }
    }

    // Preferences dialog
    if (showPreferencesDialog) {
        MealPlanPreferencesDialog(
            onDismiss = { showPreferencesDialog = false },
            onSave = { preferences ->
                viewModel.regenerateMealPlan(preferences)
                showPreferencesDialog = false
            }
        )
    }

    // Add meal dialog
    if (showAddMealDialog && selectedMealType != null) {
        AddCustomMealDialog(
            mealType = selectedMealType!!,
            onDismiss = { 
                showAddMealDialog = false
                selectedMealType = null
            },
            onSave = { customMealName, servings, notes ->
                viewModel.addCustomMeal(
                    date = selectedDate,
                    mealType = selectedMealType!!,
                    customMealName = customMealName,
                    servings = servings,
                    notes = notes
                )
                showAddMealDialog = false
                selectedMealType = null
            }
        )
    }
}

@Composable
private fun WeekNavigationRow(
    currentWeekStart: LocalDate,
    onWeekChanged: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { onWeekChanged(currentWeekStart.minusWeeks(1)) }
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Previous week")
        }
        
        Text(
            text = "${currentWeekStart.format(DateTimeFormatter.ofPattern("MMM d"))} - ${currentWeekStart.plusDays(6).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        
        IconButton(
            onClick = { onWeekChanged(currentWeekStart.plusWeeks(1)) }
        ) {
            Icon(Icons.Default.ArrowForward, contentDescription = "Next week")
        }
    }
}

@Composable
private fun WeekCalendarView(
    weekStartDate: LocalDate,
    selectedDate: LocalDate,
    weeklyMealPlan: WeeklyMealPlan?,
    onDateSelected: (LocalDate) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(7) { dayOffset ->
            val date = weekStartDate.plusDays(dayOffset.toLong())
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()
            
            // Get meals for this date
            val mealsForDate = weeklyMealPlan?.plannedMeals?.filter { 
                LocalDate.parse(it.date) == date 
            } ?: emptyList()
            
            CalendarDayCard(
                date = date,
                isSelected = isSelected,
                isToday = isToday,
                mealsCount = mealsForDate.size,
                completedMealsCount = mealsForDate.count { it.status == PlannedMealStatus.COMPLETED },
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
private fun CalendarDayCard(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    mealsCount: Int,
    completedMealsCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(80.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primary
                isToday -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = date.dayOfMonth.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary 
                       else MaterialTheme.colorScheme.onSurface
            )
            
            if (mealsCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(mealsCount) { index ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < completedMealsCount) {
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.primary
                                    } else {
                                        if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)
                                        else MaterialTheme.colorScheme.outline
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}