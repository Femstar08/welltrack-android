package com.beaconledger.welltrack.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitchingScreen(
    profiles: List<UserProfile>,
    activeProfileId: String?,
    onProfileSwitch: (String) -> Unit,
    onAddProfile: () -> Unit,
    onEditProfile: (String) -> Unit,
    onCloseSheet: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Switch Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            IconButton(onClick = onCloseSheet) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Profiles List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(profiles) { profile ->
                ProfileSwitchItem(
                    profile = profile,
                    isActive = profile.userId == activeProfileId,
                    onProfileClick = { onProfileSwitch(profile.userId) },
                    onEditClick = { onEditProfile(profile.userId) }
                )
            }
            
            // Add New Profile Item
            item {
                AddProfileItem(
                    onClick = onAddProfile
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Actions
        Text(
            text = "Quick Actions",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                title = "Family Settings",
                description = "Manage family profiles",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO: Navigate to family settings */ }
            )
            
            QuickActionCard(
                title = "Privacy",
                description = "Profile privacy settings",
                modifier = Modifier.weight(1f),
                onClick = { /* TODO: Navigate to privacy settings */ }
            )
        }
    }
}

@Composable
fun ProfileSwitchItem(
    profile: UserProfile,
    isActive: Boolean,
    onProfileClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onProfileClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (profile.profilePhotoUrl != null) {
                    // TODO: Load actual image
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                } else {
                    Text(
                        text = profile.name.take(2).uppercase(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Profile Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = profile.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
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
                
                // Activity Level
                Text(
                    text = profile.activityLevel.displayName.split(" ")[0],
                    fontSize = 12.sp,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    }
                )
            }
            
            // Actions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active Profile",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Profile",
                        modifier = Modifier.size(18.dp),
                        tint = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AddProfileItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            2.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Profile",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = "Add Family Member",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Create a new profile for tracking",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSwitchingScreenPreview() {
    MaterialTheme {
        ProfileSwitchingScreen(
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
            onProfileSwitch = {},
            onAddProfile = {},
            onEditProfile = {},
            onCloseSheet = {}
        )
    }
}