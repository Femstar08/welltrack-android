package com.beaconledger.welltrack.presentation.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.presentation.components.LoadingIndicator
import com.beaconledger.welltrack.presentation.components.ErrorMessage
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    onNavigateToGoalDetail: (String) -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val goalOverview by viewModel.goalOverview.collectAsState()
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(GoalCategory.WEIGHT) }

    LaunchedEffect(Unit) {
        viewModel.loadGoals()
        viewModel.loadGoalOverview()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .semantics {
                contentDescription = "Goals screen with goal overview and active goals list"
            }
    ) {
        // Header with create button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Goals",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            FloatingActionButton(
                onClick = { showCreateGoalDialog = true },
                modifier = Modifier
                    .size(56.dp)
                    .semantics {
                        contentDescription = "Create new goal"
                        role = Role.Button
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add goal"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading && uiState.activeGoals.isEmpty() -> {
                LoadingIndicator()
            }
            uiState.error != null -> {
                ErrorMessage(
                    message = uiState.error!!,
                    onRetry = {
                        viewModel.loadGoals()
                        viewModel.loadGoalOverview()
                    }
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Goal Overview Section
                    item {
                        GoalOverviewCard(
                            overview = goalOverview,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Category Filter
                    item {
                        CategoryFilterRow(
                            selectedCategory = selectedCategory,
                            onCategorySelected = { category ->
                                selectedCategory = category
                                viewModel.filterByCategory(category)
                            },
                            goalCounts = goalOverview.goalsByCategory
                        )
                    }

                    // Active Goals
                    item {
                        Text(
                            text = "Active Goals",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    if (uiState.activeGoals.isEmpty()) {
                        item {
                            EmptyGoalsCard(
                                category = selectedCategory,
                                onCreateGoal = { showCreateGoalDialog = true }
                            )
                        }
                    } else {
                        items(uiState.activeGoals) { goalWithProgress ->
                            GoalProgressCard(
                                goalWithProgress = goalWithProgress,
                                onClick = { onNavigateToGoalDetail(goalWithProgress.goal.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Loading indicator for pagination
                    if (uiState.isLoading && uiState.activeGoals.isNotEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }
                }
            }
        }
    }

    // Create Goal Dialog
    if (showCreateGoalDialog) {
        CreateGoalDialog(
            onDismiss = { showCreateGoalDialog = false },
            onGoalCreated = { goalId ->
                showCreateGoalDialog = false
                onNavigateToGoalDetail(goalId)
            },
            initialCategory = selectedCategory
        )
    }
}

@Composable
private fun GoalOverviewCard(
    overview: GoalOverview,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.semantics {
            contentDescription = "Goal overview with ${overview.totalActiveGoals} active goals, ${overview.completedGoals} completed"
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Goal Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewStatItem(
                    icon = Icons.Default.Flag,
                    label = "Active",
                    value = overview.totalActiveGoals.toString(),
                    color = MaterialTheme.colorScheme.primary
                )

                OverviewStatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "Completed",
                    value = overview.completedGoals.toString(),
                    color = MaterialTheme.colorScheme.tertiary
                )

                OverviewStatItem(
                    icon = Icons.Default.Schedule,
                    label = "Overdue",
                    value = overview.overdueGoals.toString(),
                    color = MaterialTheme.colorScheme.error
                )

                OverviewStatItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Progress",
                    value = "${(overview.averageCompletionRate * 100).toInt()}%",
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Upcoming milestones
            if (overview.upcomingMilestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Upcoming Milestones",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(overview.upcomingMilestones.take(3)) { milestone ->
                        MilestoneChip(milestone = milestone)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = "$label: $value"
        }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: GoalCategory,
    onCategorySelected: (GoalCategory) -> Unit,
    goalCounts: Map<GoalCategory, Int>,
    modifier: Modifier = Modifier
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.semantics {
            contentDescription = "Filter goals by category"
        }
    ) {
        items(GoalCategory.values()) { category ->
            CategoryFilterChip(
                category = category,
                isSelected = category == selectedCategory,
                goalCount = goalCounts[category] ?: 0,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
private fun CategoryFilterChip(
    category: GoalCategory,
    isSelected: Boolean,
    goalCount: Int,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = "${category.name.lowercase().replaceFirstChar { it.uppercase() }} ($goalCount)",
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        },
        modifier = Modifier.semantics {
            contentDescription = "${category.name} category, $goalCount goals"
            role = Role.Tab
        }
    )
}

@Composable
private fun EmptyGoalsCard(
    category: GoalCategory,
    onCreateGoal: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateGoal() }
            .semantics {
                contentDescription = "No ${category.name.lowercase()} goals yet. Tap to create your first goal."
                role = Role.Button
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = getCategoryIcon(category),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No ${category.name.lowercase()} goals yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Create your first ${category.name.lowercase()} goal to start tracking your progress",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onCreateGoal) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Goal")
            }
        }
    }
}

@Composable
private fun MilestoneChip(
    milestone: GoalMilestone
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.semantics {
            contentDescription = "Milestone: ${milestone.title}"
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = milestone.title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getCategoryIcon(category: GoalCategory): ImageVector {
    return when (category) {
        GoalCategory.WEIGHT -> Icons.Default.Scale
        GoalCategory.FITNESS -> Icons.Default.FitnessCenter
        GoalCategory.NUTRITION -> Icons.Default.Restaurant
        GoalCategory.HABITS -> Icons.Default.Repeat
        GoalCategory.HEALTH -> Icons.Default.Health
    }
}