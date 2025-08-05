package com.beaconledger.welltrack.presentation.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementScreen(
    profiles: List<UserProfile>,
    activeProfileId: String?,
    onBackClick: () -> Unit,
    onAddProfile: () -> Unit,
    onEditProfile: (String) -> Unit,
    onDeleteProfile: (String) -> Unit,
    onSwitchProfile: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Manage Profiles",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = onAddProfile) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Profile"
                    )
                }
            }
        )
        
        // Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Family Profiles",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Manage profiles for all family members. Each profile maintains separate meal plans, health data, and preferences.",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            lineHeight = 22.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            ProfileStatCard(
                                title = "Total Profiles",
                                value = profiles.size.toString(),
                                modifier = Modifier.weight(1f)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            ProfileStatCard(
                                title = "Active Profile",
                                value = profiles.find { it.userId == activeProfileId }?.name?.split(" ")?.first() ?: "None",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Profiles List
            items(profiles) { profile ->
                ProfileManagementItem(
                    profile = profile,
                    isActive = profile.userId == activeProfileId,
                    onEditClick = { onEditProfile(profile.userId) },
                    onDeleteClick = { onDeleteProfile(profile.userId) },
                    onSwitchClick = { onSwitchProfile(profile.userId) }
                )
            }
            
            // Add Profile Card
            item {
                AddProfileCard(
                    onClick = onAddProfile
                )
            }
            
            // Tips Section
            item {
                ProfileTipsCard()
            }
        }
    }
}

@Composable
fun ProfileStatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ProfileManagementItem(
    profile: UserProfile,
    isActive: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = profile.name,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    
                    if (profile.age != null) {
                        Text(
                            text = "${profile.age} years old",
                            fontSize = 14.sp,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            }
                        )
                    }
                    
                    Text(
                        text = profile.activityLevel.displayName,
                        fontSize = 12.sp,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
                
                if (isActive) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isActive) {
                    OutlinedButton(
                        onClick = onSwitchClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Switch To")
                    }
                }
                
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Edit")
                }
                
                if (!isActive) {
                    OutlinedButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
fun AddProfileCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Profile",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Add Family Member",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Create a new profile for tracking health and nutrition data",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Profile")
            }
        }
    }
}

@Composable
fun ProfileTipsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "💡 Profile Tips",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            val tips = listOf(
                "Each profile maintains separate meal plans and health data",
                "Switch profiles anytime from the dashboard",
                "Set different dietary restrictions for each family member",
                "Track individual fitness goals and progress"
            )
            
            tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileManagementScreenPreview() {
    MaterialTheme {
        ProfileManagementScreen(
            profiles = listOf(
                UserProfile(
                    userId = "1",
                    name = "John Doe",
                    age = 30,
                    activityLevel = ActivityLevel.MODERATE,
                    fitnessGoals = "",
                    dietaryRestrictions = "",
                    allergies = "",
                    preferredIngredients = "",
                    dislikedIngredients = "",
                    cuisinePreferences = "",
                    cookingMethods = "",
                    notificationSettings = "",
                    createdAt = "",
                    updatedAt = ""
                ),
                UserProfile(
                    userId = "2",
                    name = "Jane Smith",
                    age = 28,
                    activityLevel = ActivityLevel.ACTIVE,
                    fitnessGoals = "",
                    dietaryRestrictions = "",
                    allergies = "",
                    preferredIngredients = "",
                    dislikedIngredients = "",
                    cuisinePreferences = "",
                    cookingMethods = "",
                    notificationSettings = "",
                    createdAt = "",
                    updatedAt = ""
                )
            ),
            activeProfileId = "1",
            onBackClick = {},
            onAddProfile = {},
            onEditProfile = {},
            onDeleteProfile = {},
            onSwitchProfile = {}
        )
    }
}