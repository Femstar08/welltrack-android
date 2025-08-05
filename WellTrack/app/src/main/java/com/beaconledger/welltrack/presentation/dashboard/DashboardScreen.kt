package com.beaconledger.welltrack.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.presentation.profile.ProfileSwitchingComponent
import com.beaconledger.welltrack.presentation.profile.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToMealPlanner: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToHealth: () -> Unit,
    onNavigateToProfileCreation: () -> Unit,
    onNavigateToProfileManagement: () -> Unit,
    modifier: Modifier = Modifier,
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val activeProfile by profileViewModel.activeProfile.collectAsState()
    val allProfiles by profileViewModel.allProfiles.collectAsState()
    val hasMultipleProfiles by profileViewModel.hasMultipleProfiles.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val errorMessage by profileViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        profileViewModel.refreshProfiles()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Bar with Profile Switching
        TopAppBar(
            title = {
                Text(
                    text = "WellTrack",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                // Profile switching component
                ProfileSwitchingComponent(
                    activeProfile = activeProfile,
                    allProfiles = allProfiles,
                    onProfileSwitch = { profileId ->
                        profileViewModel.switchToProfile(profileId)
                    },
                    onAddProfile = onNavigateToProfileCreation,
                    onManageProfiles = onNavigateToProfileManagement,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Error handling
        errorMessage?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Main content
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Welcome section
                    WelcomeSection(
                        activeProfile = activeProfile,
                        hasMultipleProfiles = hasMultipleProfiles
                    )
                }

                item {
                    // Quick actions
                    QuickActionsSection(
                        onNavigateToMealPlanner = onNavigateToMealPlanner,
                        onNavigateToRecipes = onNavigateToRecipes,
                        onNavigateToHealth = onNavigateToHealth
                    )
                }

                item {
                    // Today's summary
                    TodaysSummarySection(
                        activeProfile = activeProfile
                    )
                }

                if (hasMultipleProfiles) {
                    item {
                        // Multi-profile insights
                        MultiProfileInsightsSection(
                            allProfiles = allProfiles,
                            activeProfile = activeProfile
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeSection(
    activeProfile: UserProfile?,
    hasMultipleProfiles: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Welcome back, ${activeProfile?.name?.split(" ")?.first() ?: "User"}!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (hasMultipleProfiles) {
                    "Managing health for your family"
                } else {
                    "Let's track your health journey today"
                },
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )

            if (activeProfile?.age != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${activeProfile.age} years • ${activeProfile.activityLevel.displayName.split(" ")[0]}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onNavigateToMealPlanner: () -> Unit,
    onNavigateToRecipes: () -> Unit,
    onNavigateToHealth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Actions",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Meal Planner",
                description = "Plan your meals",
                icon = Icons.Default.Person, // Using Person as placeholder for Restaurant
                onClick = onNavigateToMealPlanner,
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                title = "Recipes",
                description = "Browse recipes",
                icon = Icons.Default.Person, // Using Person as placeholder for MenuBook
                onClick = onNavigateToRecipes,
                modifier = Modifier.weight(1f)
            )

            QuickActionCard(
                title = "VO2 Max",
                description = "Track fitness",
                icon = Icons.Default.Favorite,
                onClick = onNavigateToHealth,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun TodaysSummarySection(
    activeProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Today's Summary",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryItem(
                        title = "Meals",
                        value = "2/3",
                        icon = Icons.Default.Person // Using Person as placeholder for Restaurant
                    )

                    SummaryItem(
                        title = "Water",
                        value = "1.2L",
                        icon = Icons.Default.Person // Using Person as placeholder for WaterDrop
                    )

                    SummaryItem(
                        title = "Steps",
                        value = "8.2K",
                        icon = Icons.Default.Person // Using Person as placeholder for DirectionsWalk
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun MultiProfileInsightsSection(
    allProfiles: List<UserProfile>,
    activeProfile: UserProfile?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Family Insights",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Managing ${allProfiles.size} profiles",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                allProfiles.take(3).forEach { profile ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (profile.userId == activeProfile?.userId) {
                                Icons.Default.CheckCircle
                            } else {
                                Icons.Default.Close
                            },
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (profile.userId == activeProfile?.userId) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = profile.name,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (profile.userId == activeProfile?.userId) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    MaterialTheme {
        // Preview content would go here
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Dashboard Preview")
        }
    }
}