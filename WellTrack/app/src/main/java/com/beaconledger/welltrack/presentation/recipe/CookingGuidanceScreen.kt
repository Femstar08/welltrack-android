package com.beaconledger.welltrack.presentation.recipe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.presentation.recipe.CookingGuidanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookingGuidanceScreen(
    recipeId: String,
    targetServings: Int = 1,
    onNavigateBack: () -> Unit,
    viewModel: CookingGuidanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(recipeId, targetServings) {
        viewModel.startCookingSession(recipeId, targetServings)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header with recipe info and controls
        CookingHeader(
            recipeName = uiState.scaledRecipe?.recipe?.name ?: "Loading...",
            servings = targetServings,
            onNavigateBack = onNavigateBack,
            onPauseResume = { 
                if (uiState.cookingSession?.status == CookingStatus.PAUSED) {
                    viewModel.resumeSession()
                } else {
                    viewModel.pauseSession()
                }
            },
            onComplete = { viewModel.completeSession() },
            isPaused = uiState.cookingSession?.status == CookingStatus.PAUSED
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            uiState.scaledRecipe != null && uiState.cookingSession != null -> {
                val scaledRecipe = uiState.scaledRecipe
                val cookingSession = uiState.cookingSession
                if (scaledRecipe != null && cookingSession != null) {
                    CookingContent(
                        scaledRecipe = scaledRecipe,
                        cookingSession = cookingSession,
                        activeTimers = uiState.activeTimers,
                        onStepComplete = viewModel::completeStep,
                        onStepUncheck = viewModel::uncheckStep,
                        onStartTimer = viewModel::startTimer,
                        onStopTimer = viewModel::stopTimer
                    )
                }
            }
        }
    }
}

@Composable
private fun CookingHeader(
    recipeName: String,
    servings: Int,
    onNavigateBack: () -> Unit,
    onPauseResume: () -> Unit,
    onComplete: () -> Unit,
    isPaused: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                
                Text(
                    text = "Cooking Guide",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                IconButton(onClick = onComplete) {
                    Icon(Icons.Default.Check, contentDescription = "Complete")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = recipeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "Serving $servings ${if (servings == 1) "person" else "people"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPauseResume,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Close,
                        contentDescription = if (isPaused) "Resume" else "Pause"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPaused) "Resume" else "Pause")
                }
                
                OutlinedButton(
                    onClick = onComplete,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Done, contentDescription = "Complete")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete")
                }
            }
        }
    }
}

@Composable
private fun CookingContent(
    scaledRecipe: ScaledRecipe,
    cookingSession: CookingSession,
    activeTimers: List<CookingTimer>,
    onStepComplete: (Int) -> Unit,
    onStepUncheck: (Int) -> Unit,
    onStartTimer: (Int, String, Int) -> Unit,
    onStopTimer: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Scaled Ingredients Section
        item {
            IngredientsCard(scaledRecipe.scaledIngredients)
        }
        
        // Active Timers Section
        if (activeTimers.isNotEmpty()) {
            item {
                ActiveTimersCard(
                    timers = activeTimers,
                    onStopTimer = onStopTimer
                )
            }
        }
        
        // Cooking Steps Section
        itemsIndexed(scaledRecipe.scaledSteps) { index, step ->
            CookingStepCard(
                step = step,
                stepIndex = index,
                isCompleted = deserializeCompletedSteps(cookingSession.completedSteps).contains(index),
                isCurrentStep = index == cookingSession.currentStepIndex,
                onStepComplete = { onStepComplete(index) },
                onStepUncheck = { onStepUncheck(index) },
                onStartTimer = onStartTimer
            )
        }
    }
}

@Composable
private fun IngredientsCard(ingredients: List<ScaledIngredient>) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ingredients",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = ingredient.name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "${String.format("%.1f", ingredient.scaledQuantity)} ${ingredient.unit}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveTimersCard(
    timers: List<CookingTimer>,
    onStopTimer: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Active Timers",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            timers.forEach { timer ->
                TimerItem(
                    timer = timer,
                    onStopTimer = { onStopTimer(timer.id) }
                )
            }
        }
    }
}

@Composable
private fun TimerItem(
    timer: CookingTimer,
    onStopTimer: () -> Unit
) {
    var remainingTime by remember { mutableStateOf(0L) }
    
    LaunchedEffect(timer) {
        while (timer.isActive && !timer.isCompleted) {
            val elapsed = System.currentTimeMillis() - timer.startTime
            val total = timer.durationMinutes * 60 * 1000L
            remainingTime = maxOf(0L, total - elapsed)
            
            if (remainingTime <= 0L) {
                break
            }
            
            kotlinx.coroutines.delay(1000L)
        }
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timer.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                text = formatTime(remainingTime),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (remainingTime <= 0L) MaterialTheme.colorScheme.error 
                       else MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        
        IconButton(onClick = onStopTimer) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Stop Timer",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CookingStepCard(
    step: CookingStep,
    stepIndex: Int,
    isCompleted: Boolean,
    isCurrentStep: Boolean,
    onStepComplete: () -> Unit,
    onStepUncheck: () -> Unit,
    onStartTimer: (Int, String, Int) -> Unit
) {
    var showTimerDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isCompleted -> MaterialTheme.colorScheme.secondaryContainer
                isCurrentStep -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCurrentStep) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.weight(1f)
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { checked ->
                            if (checked) onStepComplete() else onStepUncheck()
                        }
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "Step ${step.stepNumber}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = step.scaledInstruction,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        
                        if (step.duration != null || step.temperature != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                step.duration?.let { duration ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Duration",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${duration}min",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                
                                step.temperature?.let { temp ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Info,
                                            contentDescription = "Temperature",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = temp,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        
                        if (step.equipment.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Equipment: ${step.equipment.joinToString(", ")}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                if (step.duration != null) {
                    IconButton(onClick = { showTimerDialog = true }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Start Timer",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
    
    if (showTimerDialog) {
        TimerDialog(
            stepNumber = step.stepNumber,
            defaultDuration = step.duration ?: 5,
            onStartTimer = { name, duration ->
                onStartTimer(step.stepNumber, name, duration)
                showTimerDialog = false
            },
            onDismiss = { showTimerDialog = false }
        )
    }
}

@Composable
private fun TimerDialog(
    stepNumber: Int,
    defaultDuration: Int,
    onStartTimer: (String, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var timerName by remember { mutableStateOf("Step $stepNumber Timer") }
    var duration by remember { mutableStateOf(defaultDuration.toString()) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start Timer") },
        text = {
            Column {
                OutlinedTextField(
                    value = timerName,
                    onValueChange = { timerName = it },
                    label = { Text("Timer Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val durationInt = duration.toIntOrNull() ?: defaultDuration
                    onStartTimer(timerName, durationInt)
                }
            ) {
                Text("Start")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

private fun deserializeCompletedSteps(serialized: String): List<Int> {
    if (serialized.isEmpty() || serialized == "[]") return emptyList()
    return try {
        serialized.removeSurrounding("[", "]")
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
    } catch (e: Exception) {
        emptyList()
    }
}