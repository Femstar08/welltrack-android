package com.beaconledger.welltrack.presentation.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.presentation.components.ChartComponents
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    goalId: String,
    viewModel: GoalViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(goalId) {
        viewModel.selectGoal(goalId)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top App Bar
        TopAppBar(
            title = { 
                Text(uiState.selectedGoal?.goal?.title ?: "Goal Details") 
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(
                    onClick = { 
                        uiState.selectedGoal?.goal?.let { goal ->
                            viewModel.generatePrediction(goal.id)
                        }
                    }
                ) {
                    Icon(Icons.Default.Analytics, contentDescription = "Generate Prediction")
                }
                
                IconButton(
                    onClick = { viewModel.showProgressDialog(true) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Progress")
                }
            }
        )
        
        when {
            uiState.isLoadingDetails -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.selectedGoal == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Goal not found")
                }
            }
            
            else -> {
                GoalDetailContent(
                    goalWithProgress = uiState.selectedGoal!!,
                    statistics = uiState.selectedGoalStatistics,
                    prediction = uiState.selectedGoalPrediction,
                    progress = uiState.selectedGoalProgress,
                    milestones = uiState.selectedGoalMilestones,
                    onAddProgress = { value, notes ->
                        viewModel.addProgress(goalId, value, notes)
                    },
                    onCompleteMilestone = viewModel::completeMilestone,
                    onAddMilestone = { request ->
                        viewModel.addMilestone(goalId, request)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
    
    // Dialogs
    if (uiState.showProgressDialog) {
        uiState.selectedGoal?.goal?.let { goal ->
            QuickProgressDialog(
                currentValue = goal.currentValue,
                targetValue = goal.targetValue,
                unit = goal.unit,
                onDismiss = { viewModel.showProgressDialog(false) },
                onAddProgress = { value, notes ->
                    viewModel.addProgress(goal.id, value, notes)
                    viewModel.showProgressDialog(false)
                }
            )
        }
    }
    
    if (uiState.showMilestoneDialog) {
        AddMilestoneDialog(
            onDismiss = { viewModel.showMilestoneDialog(false) },
            onAddMilestone = { request ->
                viewModel.addMilestone(goalId, request)
                viewModel.showMilestoneDialog(false)
            }
        )
    }
}

@Composable
fun GoalDetailContent(
    goalWithProgress: GoalWithProgress,
    statistics: GoalStatistics?,
    prediction: GoalPrediction?,
    progress: List<GoalProgress>,
    milestones: List<GoalMilestone>,
    onAddProgress: (Double, String?) -> Unit,
    onCompleteMilestone: (String) -> Unit,
    onAddMilestone: (CreateMilestoneRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Goal Overview Card
        item {
            GoalOverviewDetailCard(
                goal = goalWithProgress.goal,
                statistics = statistics
            )
        }
        
        // Progress Chart
        item {
            ProgressChartCard(
                progress = progress,
                goal = goalWithProgress.goal
            )
        }
        
        // Prediction Card
        prediction?.let { pred ->
            item {
                PredictionCard(prediction = pred)
            }
        }
        
        // Milestones Section
        item {
            MilestonesCard(
                milestones = milestones,
                onCompleteMilestone = onCompleteMilestone,
                onAddMilestone = onAddMilestone
            )
        }
        
        // Recent Progress
        item {
            RecentProgressCard(
                progress = progress.take(10),
                unit = goalWithProgress.goal.unit
            )
        }
        
        // Goal Actions
        item {
            GoalActionsCard(
                goal = goalWithProgress.goal,
                onAddProgress = onAddProgress
            )
        }
    }
}

@Composable
fun GoalOverviewDetailCard(
    goal: Goal,
    statistics: GoalStatistics?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    goal.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                
                GoalPriorityChip(priority = goal.priority)
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress Circle and Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Circular Progress
                val progress = if (goal.targetValue > 0) {
                    (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
                } else {
                    0f
                }
                
                CircularProgressIndicator(
                    progress = progress,
                    size = 120.dp
                )
                
                // Statistics
                statistics?.let { stats ->
                    Column {
                        StatisticItem(
                            label = "Days Remaining",
                            value = "${stats.daysRemaining}",
                            icon = Icons.Default.Schedule
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        StatisticItem(
                            label = "Daily Progress",
                            value = "${stats.averageDailyProgress.toInt()}",
                            icon = Icons.Default.TrendingUp
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TrendIndicator(trend = stats.trendDirection)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress Bar
            GoalProgressSection(
                currentValue = goal.currentValue,
                targetValue = goal.targetValue,
                unit = goal.unit,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun StatisticItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun ProgressChartCard(
    progress: List<GoalProgress>,
    goal: Goal,
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
                text = "Progress Over Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (progress.isNotEmpty()) {
                // Use the ChartComponents from the existing codebase
                ChartComponents.LineChart(
                    data = progress.map { it.value.toFloat() },
                    labels = progress.map { 
                        it.recordedAt.format(DateTimeFormatter.ofPattern("MM/dd"))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No progress data yet",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun PredictionCard(
    prediction: GoalPrediction,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = "Prediction",
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "AI Prediction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Predicted completion: ${prediction.predictedCompletionDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "Confidence: ${(prediction.confidenceScore * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TrendIndicator(trend = prediction.trendAnalysis)
            
            if (prediction.recommendedAdjustments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Recommendations:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                
                prediction.recommendedAdjustments.forEach { recommendation ->
                    Text(
                        text = "• $recommendation",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MilestonesCard(
    milestones: List<GoalMilestone>,
    onCompleteMilestone: (String) -> Unit,
    onAddMilestone: (CreateMilestoneRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.fillMaxWidth(),
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
                Text(
                    text = "Milestones",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                TextButton(
                    onClick = { showAddDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add")
                }
            }
            
            if (milestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                milestones.forEach { milestone ->
                    MilestoneDetailItem(
                        milestone = milestone,
                        onComplete = { onCompleteMilestone(milestone.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Progress indicator
                MilestoneProgressIndicator(
                    milestones = milestones,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "No milestones set",
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }
    }
    
    if (showAddDialog) {
        AddMilestoneDialog(
            onDismiss = { showAddDialog = false },
            onAddMilestone = { request ->
                onAddMilestone(request)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MilestoneDetailItem(
    milestone: GoalMilestone,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = milestone.isCompleted,
                onCheckedChange = { if (!milestone.isCompleted) onComplete() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = milestone.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "Target: ${milestone.targetValue}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                milestone.targetDate?.let { date ->
                    Text(
                        text = "Due: ${date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
        
        if (milestone.isCompleted) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun RecentProgressCard(
    progress: List<GoalProgress>,
    unit: String,
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
                text = "Recent Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            if (progress.isNotEmpty()) {
                progress.forEach { progressEntry ->
                    ProgressEntryItem(
                        progress = progressEntry,
                        unit = unit,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                Text(
                    text = "No progress entries yet",
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun ProgressEntryItem(
    progress: GoalProgress,
    unit: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${progress.value} $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = progress.recordedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            
            progress.notes?.let { notes ->
                Text(
                    text = notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                text = progress.source.name,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun GoalActionsCard(
    goal: Goal,
    onAddProgress: (Double, String?) -> Unit,
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
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onAddProgress(goal.currentValue + 1, "Quick update") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Progress")
                }
                
                OutlinedButton(
                    onClick = { /* Share goal */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share")
                }
            }
        }
    }
}