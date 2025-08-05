package com.beaconledger.welltrack.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.beaconledger.welltrack.data.model.ActivityLevel
import com.beaconledger.welltrack.data.model.UserProfile

@Composable
fun ProfileSelectorComponent(
    activeProfile: UserProfile?,
    hasMultipleProfiles: Boolean,
    onProfileSelectorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { if (hasMultipleProfiles) onProfileSelectorClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                if (activeProfile?.profilePhotoUrl != null) {
                    // TODO: Load actual image
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Photo",
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                } else {
                    Text(
                        text = activeProfile?.name?.take(2)?.uppercase() ?: "?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Profile Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activeProfile?.name ?: "No Profile Selected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (activeProfile != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeProfile.age != null) {
                            Text(
                                text = "${activeProfile.age} years",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            
                            Text(
                                text = " • ",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                        
                        Text(
                            text = activeProfile.activityLevel.displayName.split(" ")[0],
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // Dropdown Arrow (only if multiple profiles)
            if (hasMultipleProfiles) {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Switch Profile",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun CompactProfileSelector(
    activeProfile: UserProfile?,
    hasMultipleProfiles: Boolean,
    onProfileSelectorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable { if (hasMultipleProfiles) onProfileSelectorClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Photo
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = activeProfile?.name?.take(1)?.uppercase() ?: "?",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Profile Name
        Text(
            text = activeProfile?.name ?: "Profile",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        
        // Dropdown Arrow (only if multiple profiles)
        if (hasMultipleProfiles) {
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Switch Profile",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ProfileIndicatorBadge(
    profileCount: Int,
    activeProfileName: String,
    modifier: Modifier = Modifier
) {
    if (profileCount > 1) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = profileCount.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = "profiles",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileSelectorComponentPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileSelectorComponent(
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
                hasMultipleProfiles = true,
                onProfileSelectorClick = {}
            )
            
            CompactProfileSelector(
                activeProfile = UserProfile(
                    userId = "1",
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
                ),
                hasMultipleProfiles = true,
                onProfileSelectorClick = {}
            )
            
            ProfileIndicatorBadge(
                profileCount = 3,
                activeProfileName = "John"
            )
        }
    }
}