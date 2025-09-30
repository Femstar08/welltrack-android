package com.beaconledger.welltrack.presentation.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalCard(
    goal: Goal,
    onClick: () -> Unit,
    onAddProgress: (Double, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickProgress by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with title and priority
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    goal.description?.let { description ->
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                GoalPriorityChip(priority = goal.priority)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress section
            GoalProgressSection(
                currentValue = goal.currentValue,
                targetValue = goal.targetValue,
                unit = goal.unit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Timeline and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                GoalTimelineInfo(
                    startDate = goal.startDate,
                    targetDate = goal.targetDate
                )

                Row {
                    IconButton(
                        onClick = { showQuickProgress = true }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Progress",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = onClick) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "View Details"
                        )
                    }
                }
            }
        }
    }

    // Quick progress dialog
    if (showQuickProgress) {
        QuickProgressDialog(
            currentValue = goal.currentValue,
            targetValue = goal.targetValue,
            unit = goal.unit,
            onDismiss = { showQuickProgress = false },
            onAddProgress = { value, notes ->
                onAddProgress(value, notes)
                showQuickProgress = false
            }
        )
    }
}

@Composable
fun GoalProgressCard(
    goalWithProgress: GoalWithProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val goal = goalWithProgress.goal
    val progressPercentage = if (goal.targetValue > 0) {
        ((goal.currentValue / goal.targetValue) * 100).coerceIn(0.0, 100.0).toFloat()
    } else 0f

    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with goal info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!goal.description.isNullOrBlank()) {
                        Text(
                            text = goal.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                GoalPriorityChip(priority = goal.priority)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress section
            GoalProgressSection(
                currentValue = goal.currentValue,
                targetValue = goal.targetValue,
                unit = goal.unit,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Timeline and status
            GoalTimelineInfo(
                startDate = goal.startDate,
                targetDate = goal.targetDate
            )

            // Milestones preview
            if (goalWithProgress.milestones.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                MilestoneProgressIndicator(
                    milestones = goalWithProgress.milestones,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Prediction insight
            goalWithProgress.prediction?.let { prediction ->
                Spacer(modifier = Modifier.height(12.dp))
                TrendIndicator(
                    trend = prediction.trendAnalysis,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun GoalPriorityChip(
    priority: GoalPriority,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (priority) {
        GoalPriority.LOW -> MaterialTheme.colorScheme.outline to "Low"
        GoalPriority.MEDIUM -> MaterialTheme.colorScheme.primary to "Medium"
        GoalPriority.HIGH -> MaterialTheme.colorScheme.tertiary to "High"
        GoalPriority.CRITICAL -> MaterialTheme.colorScheme.error to "Critical"
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun GoalProgressSection(
    currentValue: Double,
    targetValue: Double,
    unit: String,
    modifier: Modifier = Modifier
) {
    val progress = if (targetValue > 0) {
        (currentValue / targetValue).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "progress_animation"
    )
    
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${currentValue.toInt()} / ${targetValue.toInt()} $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = getProgressColor(progress),
            trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun GoalTimelineInfo(
    startDate: LocalDate,
    targetDate: LocalDate,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(startDate, targetDate)
    val daysRemaining = ChronoUnit.DAYS.between(today, targetDate)
    val isOverdue = daysRemaining < 0
    
    Column(modifier = modifier) {
        Text(
            text = if (isOverdue) {
                "Overdue by ${-daysRemaining} days"
            } else {
                "$daysRemaining days remaining"
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (isOverdue) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.outline
            }
        )
        
        Text(
            text = "Target: ${targetDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun CircularProgressIndicator(
    progress: Float,
    size: androidx.compose.ui.unit.Dp = 120.dp,
    strokeWidth: androidx.compose.ui.unit.Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "circular_progress"
    )
    
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(size)
        ) {
            drawCircularProgress(
                progress = animatedProgress,
                strokeWidth = strokeWidth.toPx(),
                color = getProgressColor(progress)
            )
        }
        
        Text(
            text = "${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun DrawScope.drawCircularProgress(
    progress: Float,
    strokeWidth: Float,
    color: Color
) {
    val diameter = size.minDimension
    val radius = diameter / 2f
    val insideRadius = radius - strokeWidth / 2f
    val topLeft = Offset(
        (size.width - diameter) / 2f,
        (size.height - diameter) / 2f
    )
    val size = Size(diameter, diameter)
    
    // Background circle
    drawArc(
        color = color.copy(alpha = 0.2f),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
    
    // Progress arc
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = 360f * progress,
        useCenter = false,
        topLeft = topLeft,
        size = size,
        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    )
}

@Composable
fun MilestoneProgressIndicator(
    milestones: List<GoalMilestone>,
    modifier: Modifier = Modifier
) {
    val completedCount = milestones.count { it.isCompleted }
    val totalCount = milestones.size
    
    if (totalCount == 0) return
    
    Column(modifier = modifier) {
        Text(
            text = "Milestones: $completedCount / $totalCount",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            milestones.forEachIndexed { index, milestone ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(
                            if (milestone.isCompleted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            }
                        )
                )
            }
        }
    }
}

@Composable
fun TrendIndicator(
    trend: TrendAnalysis,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (trend) {
        TrendAnalysis.ON_TRACK -> Triple(
            Icons.Default.TrendingFlat,
            MaterialTheme.colorScheme.primary,
            "On Track"
        )
        TrendAnalysis.AHEAD_OF_SCHEDULE -> Triple(
            Icons.Default.TrendingUp,
            MaterialTheme.colorScheme.tertiary,
            "Ahead"
        )
        TrendAnalysis.BEHIND_SCHEDULE -> Triple(
            Icons.Default.TrendingDown,
            MaterialTheme.colorScheme.error,
            "Behind"
        )
        TrendAnalysis.STAGNANT -> Triple(
            Icons.Default.Remove,
            MaterialTheme.colorScheme.outline,
            "Stagnant"
        )
        TrendAnalysis.DECLINING -> Triple(
            Icons.Default.TrendingDown,
            MaterialTheme.colorScheme.error,
            "Declining"
        )
        TrendAnalysis.ACCELERATING -> Triple(
            Icons.Default.TrendingUp,
            MaterialTheme.colorScheme.tertiary,
            "Accelerating"
        )
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
private fun getProgressColor(progress: Float): Color {
    return when {
        progress >= 0.8f -> MaterialTheme.colorScheme.tertiary
        progress >= 0.5f -> MaterialTheme.colorScheme.primary
        progress >= 0.3f -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
}