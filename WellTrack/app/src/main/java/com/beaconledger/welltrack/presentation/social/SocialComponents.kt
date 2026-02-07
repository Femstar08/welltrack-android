package com.beaconledger.welltrack.presentation.social

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
import androidx.compose.ui.unit.dp
import com.beaconledger.welltrack.data.model.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyGroupCard(
    familyGroup: FamilyGroupWithMembers,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = familyGroup.familyGroup.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (familyGroup.familyGroup.description != null) {
                        Text(
                            text = familyGroup.familyGroup.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit family group"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${familyGroup.members.size} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FamilyMemberItem(
    member: FamilyMemberInfo,
    currentUserId: String,
    canManageMembers: Boolean,
    onRoleChange: (FamilyRole) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile photo placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(1).uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (canManageMembers && member.userId != currentUserId) {
                var showMenu by remember { mutableStateOf(false) }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Member options"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        FamilyRole.values().forEach { role ->
                            if (role != member.role) {
                                DropdownMenuItem(
                                    text = { Text("Make ${role.name.lowercase()}") },
                                    onClick = {
                                        onRoleChange(role)
                                        showMenu = false
                                    }
                                )
                            }
                        }
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Remove member") },
                            onClick = {
                                onRemove()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SharedMealPlanCard(
    sharedMealPlan: SharedMealPlanWithDetails,
    currentUserId: String,
    onView: () -> Unit,
    onUnshare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() },
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sharedMealPlan.sharedMealPlan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shared by ${sharedMealPlan.sharedByName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (sharedMealPlan.sharedMealPlan.sharedBy == currentUserId) {
                    IconButton(onClick = onUnshare) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Unshare meal plan"
                        )
                    }
                }
            }
            
            if (sharedMealPlan.sharedMealPlan.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sharedMealPlan.sharedMealPlan.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Week of ${sharedMealPlan.sharedMealPlan.weekStartDate.format(DateTimeFormatter.ofPattern("MMM dd"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SharedRecipeCard(
    sharedRecipe: SharedRecipeWithDetails,
    currentUserId: String,
    onView: () -> Unit,
    onUnshare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() },
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Recipe Shared",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Shared by ${sharedRecipe.sharedByName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (sharedRecipe.sharedRecipe.sharedBy == currentUserId) {
                    IconButton(onClick = onUnshare) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Unshare recipe"
                        )
                    }
                }
            }
            
            if (sharedRecipe.sharedRecipe.message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = sharedRecipe.sharedRecipe.message,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Shared ${sharedRecipe.sharedRecipe.sharedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CollaborativeMealPrepCard(
    mealPrep: CollaborativeMealPrepWithDetails,
    currentUserId: String,
    onStatusChange: (MealPrepStatus) -> Unit,
    onUpdateNotes: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mealPrep.mealPrep.recipeName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Assigned to ${mealPrep.assignedToName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                MealPrepStatusChip(
                    status = mealPrep.mealPrep.status,
                    onClick = if (mealPrep.mealPrep.assignedTo == currentUserId) {
                        { onStatusChange(getNextStatus(mealPrep.mealPrep.status)) }
                    } else null
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Scheduled for ${mealPrep.mealPrep.scheduledDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (mealPrep.mealPrep.notes != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mealPrep.mealPrep.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MealPrepStatusChip(
    status: MealPrepStatus,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val (color, text) = when (status) {
        MealPrepStatus.ASSIGNED -> MaterialTheme.colorScheme.secondary to "Assigned"
        MealPrepStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary to "In Progress"
        MealPrepStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed"
        MealPrepStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
    }
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
fun SharedAchievementCard(
    sharedAchievement: SharedAchievement,
    achievement: Achievement?,
    currentUserId: String,
    onReaction: (ReactionType) -> Unit,
    onRemoveReaction: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (achievement == null) return
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (!achievement.shareMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = achievement.shareMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Reactions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val userReaction = sharedAchievement.reactions.find { it.userId == currentUserId }
                
                ReactionType.values().forEach { reactionType ->
                    val count = sharedAchievement.reactions.count { it.reactionType == reactionType }
                    val isSelected = userReaction?.reactionType == reactionType
                    
                    ReactionChip(
                        reactionType = reactionType,
                        count = count,
                        isSelected = isSelected,
                        onClick = {
                            if (isSelected) {
                                onRemoveReaction()
                            } else {
                                onReaction(reactionType)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReactionChip(
    reactionType: ReactionType,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val emoji = when (reactionType) {
        ReactionType.LIKE -> "👍"
        ReactionType.LOVE -> "❤️"
        ReactionType.CELEBRATE -> "🎉"
        ReactionType.SUPPORT -> "💪"
    }
    
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.bodySmall)
            if (count > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SharedShoppingListCard(
    shoppingList: SharedShoppingList,
    itemCount: Int,
    completedCount: Int,
    onView: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onView() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = shoppingList.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$completedCount of $itemCount items completed",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (itemCount > 0) {
                    LinearProgressIndicator(
                        progress = completedCount.toFloat() / itemCount.toFloat(),
                        modifier = Modifier.width(80.dp)
                    )
                }
            }
        }
    }
}

private fun getNextStatus(currentStatus: MealPrepStatus): MealPrepStatus {
    return when (currentStatus) {
        MealPrepStatus.ASSIGNED -> MealPrepStatus.IN_PROGRESS
        MealPrepStatus.IN_PROGRESS -> MealPrepStatus.COMPLETED
        MealPrepStatus.COMPLETED -> MealPrepStatus.COMPLETED
        MealPrepStatus.CANCELLED -> MealPrepStatus.ASSIGNED
    }
}