package com.beaconledger.welltrack.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Dashboard Header Component (matches React Header.tsx)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardHeader(
    currentUser: String = "John Doe",
    profileCount: Int = 2,
    onProfileSwitchClick: () -> Unit = {},
    onUserProfileClick: () -> Unit = {}
) {
    val currentDate = LocalDate.now().format(
        DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    )
    
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WellTrack Logo
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "W",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = "WellTrack",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = currentDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        },
        actions = {
            // Profile Switcher (matches React multi-profile functionality)
            Surface(
                onClick = onProfileSwitchClick,
                modifier = Modifier
                    .padding(end = 8.dp)
                    .clip(RoundedCornerShape(20.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Switch Profile",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (profileCount > 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = profileCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // User Avatar
            Surface(
                onClick = onUserProfileClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                border = androidx.compose.foundation.BorderStroke(
                    2.dp, 
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                )
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.split(" ").map { it.first() }.joinToString(""),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

// Today's Summary Section (matches React MealCard + NutritionCard)
@Composable
fun TodaysSummarySection(
    mealsLogged: Int = 3,
    totalMeals: Int = 4,
    caloriesConsumed: Int = 1650,
    caloriesTarget: Int = 2000,
    proteinConsumed: Double = 85.0,
    proteinTarget: Double = 100.0,
    onLogMealClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Today's Summary")
        
        // Meals Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
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
                        text = "Today's Meals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$mealsLogged/$totalMeals logged",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Meal Progress Indicators
                val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                val completed = listOf(true, true, true, false)
                
                meals.forEachIndexed { index, meal ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (completed[index]) Icons.Default.CheckCircle else Icons.Default.Circle,
                            contentDescription = null,
                            tint = if (completed[index]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = meal,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (completed[index]) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                if (mealsLogged < totalMeals) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onLogMealClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Next Meal")
                    }
                }
            }
        }
        
        // Nutrition Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Nutrition Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Calories Progress
                NutritionProgressItem(
                    label = "Calories",
                    current = caloriesConsumed,
                    target = caloriesTarget,
                    unit = "kcal",
                    progress = caloriesConsumed.toFloat() / caloriesTarget.toFloat()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Macronutrients
                val macros = listOf(
                    Triple("Protein", proteinConsumed, proteinTarget),
                    Triple("Carbs", 180.0, 250.0),
                    Triple("Fat", 65.0, 80.0)
                )
                
                macros.forEach { (label, current, target) ->
                    NutritionProgressItem(
                        label = label,
                        current = current.toInt(),
                        target = target.toInt(),
                        unit = "g",
                        progress = (current / target).toFloat()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun NutritionProgressItem(
    label: String,
    current: Int,
    target: Int,
    unit: String,
    progress: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$current / $target $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            
            LinearProgressIndicator(
                progress = progress.coerceIn(0f, 1f),
                modifier = Modifier
                    .width(80.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = when {
                    progress >= 0.8f -> MaterialTheme.colorScheme.primary
                    progress >= 0.5f -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

// Quick Actions Section (matches React QuickActions)
@Composable
fun QuickActionsSection(
    onLogMealClick: () -> Unit = {},
    onAddIngredientClick: () -> Unit = {},
    onStartPrepClick: () -> Unit = {},
    onViewShoppingClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Quick Actions")
        
        val actions = listOf(
            QuickActionItem("Log Meal", Icons.Default.Restaurant, onLogMealClick),
            QuickActionItem("Add Ingredient", Icons.Default.Add, onAddIngredientClick),
            QuickActionItem("Start Prep", Icons.Default.PlayArrow, onStartPrepClick),
            QuickActionItem("Shopping List", Icons.Default.ShoppingCart, onViewShoppingClick)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(actions) { action ->
                QuickActionCard(
                    action = action,
                    modifier = Modifier.width(120.dp)
                )
            }
        }
    }
}

data class QuickActionItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun QuickActionCard(
    action: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = action.onClick,
        modifier = modifier.aspectRatio(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = action.label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// Health Insights Section (matches React HealthInsights)
@Composable
fun HealthInsightsSection(
    insights: List<HealthInsight> = sampleInsights()
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Health Insights")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "AI Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                insights.forEach { insight ->
                    InsightItem(insight = insight)
                    if (insight != insights.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

data class HealthInsight(
    val message: String,
    val type: InsightType,
    val priority: InsightPriority
)

enum class InsightType {
    NUTRITION, FITNESS, WELLNESS, ACHIEVEMENT
}

enum class InsightPriority {
    HIGH, MEDIUM, LOW
}

@Composable
fun InsightItem(insight: HealthInsight) {
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Priority indicator
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    when (insight.priority) {
                        InsightPriority.HIGH -> MaterialTheme.colorScheme.error
                        InsightPriority.MEDIUM -> MaterialTheme.colorScheme.tertiary
                        InsightPriority.LOW -> MaterialTheme.colorScheme.primary
                    }
                )
                .align(Alignment.Top)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = insight.message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// Sample data
fun sampleInsights(): List<HealthInsight> = listOf(
    HealthInsight(
        "Your protein intake is 15% below target. Consider adding a protein-rich snack like Greek yogurt or nuts.",
        InsightType.NUTRITION,
        InsightPriority.MEDIUM
    ),
    HealthInsight(
        "Great job maintaining consistent meal timing this week! This helps with metabolism regulation.",
        InsightType.WELLNESS,
        InsightPriority.LOW
    ),
    HealthInsight(
        "You've logged meals 5 days in a row. Keep up the excellent tracking habit!",
        InsightType.ACHIEVEMENT,
        InsightPriority.LOW
    )
)
// Security Section for Dashboard
@Composable
fun SecuritySection(
    onNavigateToSecurity: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Security & Privacy")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Your Data is Protected",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "End-to-end encryption enabled",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Security Features Overview
                val securityFeatures = listOf(
                    SecurityFeature("App Lock", "Enabled", true),
                    SecurityFeature("Biometric Auth", "Available", true),
                    SecurityFeature("Data Encryption", "Active", true),
                    SecurityFeature("Privacy Controls", "Configured", true)
                )
                
                securityFeatures.forEach { feature ->
                    SecurityFeatureItem(feature = feature)
                    if (feature != securityFeatures.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Security Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onNavigateToSecurity,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Settings")
                    }
                    
                    Button(
                        onClick = onNavigateToSecurity,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Security")
                    }
                }
            }
        }
    }
}

data class SecurityFeature(
    val name: String,
    val status: String,
    val isActive: Boolean
)

@Composable
fun SecurityFeatureItem(feature: SecurityFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = feature.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = feature.status,
                style = MaterialTheme.typography.bodySmall,
                color = if (feature.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (feature.isActive) FontWeight.Medium else FontWeight.Normal
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (feature.isActive) Icons.Default.CheckCircle else Icons.Default.Circle,
                contentDescription = null,
                tint = if (feature.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Multi-Profile Insights Section (for family/multi-user support)
@Composable
fun MultiProfileInsightsSection(
    allProfiles: List<com.beaconledger.welltrack.data.model.UserProfile>,
    activeProfile: com.beaconledger.welltrack.data.model.UserProfile?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = "Family Health Overview")
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Managing ${allProfiles.size} profiles",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                allProfiles.take(3).forEach { profile ->
                    ProfileSummaryItem(
                        profile = profile,
                        isActive = profile.id == activeProfile?.id
                    )
                    if (profile != allProfiles.take(3).last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                if (allProfiles.size > 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "And ${allProfiles.size - 3} more...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSummaryItem(
    profile: com.beaconledger.welltrack.data.model.UserProfile,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Avatar
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = profile.name.first().toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
            
            profile.age?.let { age ->
                Text(
                    text = "$age years • ${profile.activityLevel.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        if (isActive) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Active Profile",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}