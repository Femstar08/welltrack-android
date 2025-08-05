package com.beaconledger.welltrack.presentation.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.beaconledger.welltrack.data.model.*

@Composable
fun ProfileSwitchingComponent(
    activeProfile: UserProfile?,
    allProfiles: List<UserProfile>,
    onProfileSwitch: (String) -> Unit,
    onAddProfile: () -> Unit,
    onManageProfiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSwitchDialog by remember { mutableStateOf(false) }
    
    // Main profile selector button
    ProfileSelectorButton(
        activeProfile = activeProfile,
        hasMultipleProfiles = allProfiles.size > 1,
        onClick = { 
            if (allProfiles.size > 1) {
                showSwitchDialog = true 
            }
        },
        modifier = modifier
    )
    
    // Profile switching dialog
    if (showSwitchDialog) {
        ProfileSwitchDialog(
            profiles = allProfiles,
            activeProfileId = activeProfile?.userId,
            onProfileSwitch = { profileId ->
                onProfileSwitch(profileId)
                showSwitchDialog = false
            },
            onAddProfile = {
                onAddProfile()
                showSwitchDialog = false
            },
            onManageProfiles = {
                onManageProfiles()
                showSwitchDialog = false
            },
            onDismiss = { showSwitchDialog = false }
        )
    }
}

@Composable
fun ProfileSelectorButton(
    activeProfile: UserProfile?,
    hasMultipleProfiles: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable { if (hasMultipleProfiles) onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Avatar
            ProfileAvatar(
                profile = activeProfile,
                size = 40.dp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Profile Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activeProfile?.name ?: "No Profile",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (hasMultipleProfiles) {
                    Text(
                        text = "Tap to switch",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // Switch indicator
            if (hasMultipleProfiles) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown, // Using ArrowDropDown as placeholder for SwapHoriz
                    contentDescription = "Switch Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileAvatar(
    profile: UserProfile?,
    size: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        if (profile?.profilePhotoUrl != null) {
            // TODO: Load actual image with Coil
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile Photo",
                modifier = Modifier.size(size * 0.6f),
                tint = Color.White
            )
        } else {
            Text(
                text = profile?.name?.take(2)?.uppercase() ?: "?",
                fontSize = (size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ProfileSwitchDialog(
    profiles: List<UserProfile>,
    activeProfileId: String?,
    onProfileSwitch: (String) -> Unit,
    onAddProfile: () -> Unit,
    onManageProfiles: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Switch Profile",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Profiles List
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(profiles) { profile ->
                        ProfileSwitchItem(
                            profile = profile,
                            isActive = profile.userId == activeProfileId,
                            onClick = { onProfileSwitch(profile.userId) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddProfile,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }
                    
                    Button(
                        onClick = onManageProfiles,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Manage")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileSwitchItem(
    profile: UserProfile,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                profile = profile,
                size = 48.dp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = profile.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isActive) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                
                if (profile.age != null) {
                    Text(
                        text = "${profile.age} years old",
                        fontSize = 14.sp,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        }
                    )
                }
            }
            
            if (isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Active Profile",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CompactProfileSwitcher(
    activeProfile: UserProfile?,
    allProfiles: List<UserProfile>,
    onProfileSwitch: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        // Compact button
        Row(
            modifier = Modifier
                .clickable { 
                    if (allProfiles.size > 1) expanded = true 
                }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileAvatar(
                profile = activeProfile,
                size = 32.dp
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = activeProfile?.name?.split(" ")?.first() ?: "Profile",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            if (allProfiles.size > 1) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Switch Profile",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        // Dropdown menu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            allProfiles.forEach { profile ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileAvatar(
                                profile = profile,
                                size = 24.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(profile.name)
                            if (profile.userId == activeProfile?.userId) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    onClick = {
                        onProfileSwitch(profile.userId)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSwitchingComponentPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileSwitchingComponent(
                activeProfile = UserProfile(
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
                allProfiles = listOf(
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
                onProfileSwitch = {},
                onAddProfile = {},
                onManageProfiles = {}
            )
            
            CompactProfileSwitcher(
                activeProfile = UserProfile(
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
                allProfiles = listOf(
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
                onProfileSwitch = {}
            )
        }
    }
}