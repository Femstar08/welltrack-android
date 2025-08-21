package com.beaconledger.welltrack.presentation.social

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.beaconledger.welltrack.data.model.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    viewModel: SocialViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    val selectedFamilyGroupId by viewModel.selectedFamilyGroupId.collectAsState()
    val familyGroups by viewModel.familyGroups.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Family", "Shared Content", "Meal Prep", "Achievements", "Shopping")
    
    // Set current user ID (this would typically come from authentication)
    LaunchedEffect(Unit) {
        viewModel.setCurrentUserId("current_user_id") // Replace with actual user ID
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Family Group Selector
        if (familyGroups.isNotEmpty()) {
            FamilyGroupSelector(
                familyGroups = familyGroups,
                selectedGroupId = selectedFamilyGroupId,
                onGroupSelected = { viewModel.selectFamilyGroup(it) },
                onCreateGroup = { /* Show create group dialog */ }
            )
        }
        
        // Tab Row
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> FamilyTab(viewModel = viewModel)
            1 -> SharedContentTab(viewModel = viewModel)
            2 -> MealPrepTab(viewModel = viewModel)
            3 -> AchievementsTab(viewModel = viewModel)
            4 -> ShoppingTab(viewModel = viewModel)
        }
    }
    
    // Show loading indicator
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
    
    // Show messages
    uiState.message?.let { message ->
        LaunchedEffect(message) {
            // Show snackbar or toast
            viewModel.clearMessage()
        }
    }
    
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // Show error snackbar or toast
            viewModel.clearError()
        }
    }
}

@Composable
fun FamilyGroupSelector(
    familyGroups: List<FamilyGroupWithMembers>,
    selectedGroupId: String?,
    onGroupSelected: (String?) -> Unit,
    onCreateGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
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
                    text = "Family Groups",
                    style = MaterialTheme.typography.titleMedium
                )
                
                IconButton(onClick = onCreateGroup) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create family group"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                items(familyGroups) { familyGroup ->
                    FamilyGroupCard(
                        familyGroup = familyGroup,
                        isSelected = familyGroup.familyGroup.id == selectedGroupId,
                        onSelect = { onGroupSelected(familyGroup.familyGroup.id) },
                        onEdit = { /* Show edit dialog */ }
                    )
                }
            }
        }
    }
}

@Composable
fun FamilyTab(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val familyMembers by viewModel.familyMembers.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Family Members",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Button(
                    onClick = { /* Show invite dialog */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Invite")
                }
            }
        }
        
        items(familyMembers) { member ->
            FamilyMemberItem(
                member = member,
                currentUserId = currentUserId,
                canManageMembers = true, // Would check user permissions
                onRoleChange = { role ->
                    viewModel.updateFamilyMemberRole(
                        viewModel.selectedFamilyGroupId.value ?: "",
                        member.userId,
                        role
                    )
                },
                onRemove = {
                    viewModel.removeFamilyMember(
                        viewModel.selectedFamilyGroupId.value ?: "",
                        member.userId
                    )
                }
            )
        }
    }
}

@Composable
fun SharedContentTab(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val sharedMealPlans by viewModel.sharedMealPlans.collectAsState()
    val sharedRecipes by viewModel.sharedRecipes.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Shared Meal Plans",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        items(sharedMealPlans) { sharedMealPlan ->
            SharedMealPlanCard(
                sharedMealPlan = sharedMealPlan,
                currentUserId = currentUserId,
                onView = { /* Navigate to meal plan details */ },
                onUnshare = {
                    viewModel.unshareeMealPlan(sharedMealPlan.sharedMealPlan.id)
                }
            )
        }
        
        item {
            Text(
                text = "Shared Recipes",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        items(sharedRecipes) { sharedRecipe ->
            SharedRecipeCard(
                sharedRecipe = sharedRecipe,
                currentUserId = currentUserId,
                onView = { /* Navigate to recipe details */ },
                onUnshare = {
                    viewModel.unshareRecipe(sharedRecipe.sharedRecipe.id)
                }
            )
        }
    }
}

@Composable
fun MealPrepTab(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val collaborativeMealPrep by viewModel.collaborativeMealPrep.collectAsState()
    val myMealPrepAssignments by viewModel.myMealPrepAssignments.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Assignments",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        items(myMealPrepAssignments) { mealPrep ->
            CollaborativeMealPrepCard(
                mealPrep = mealPrep,
                currentUserId = currentUserId,
                onStatusChange = { status ->
                    viewModel.updateMealPrepStatus(mealPrep.mealPrep.id, status)
                },
                onUpdateNotes = { notes ->
                    viewModel.updateMealPrepNotes(mealPrep.mealPrep.id, notes)
                }
            )
        }
        
        item {
            Text(
                text = "All Family Meal Prep",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        items(collaborativeMealPrep) { mealPrep ->
            CollaborativeMealPrepCard(
                mealPrep = mealPrep,
                currentUserId = currentUserId,
                onStatusChange = { status ->
                    viewModel.updateMealPrepStatus(mealPrep.mealPrep.id, status)
                },
                onUpdateNotes = { notes ->
                    viewModel.updateMealPrepNotes(mealPrep.mealPrep.id, notes)
                }
            )
        }
    }
}

@Composable
fun AchievementsTab(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val sharedAchievements by viewModel.sharedAchievements.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Family Achievements",
                style = MaterialTheme.typography.titleLarge
            )
        }
        
        items(sharedAchievements) { sharedAchievement ->
            SharedAchievementCard(
                sharedAchievement = sharedAchievement,
                achievement = null, // Would fetch achievement details
                currentUserId = currentUserId,
                onReaction = { reactionType ->
                    viewModel.addAchievementReaction(sharedAchievement.id, reactionType)
                },
                onRemoveReaction = {
                    viewModel.removeAchievementReaction(sharedAchievement.id)
                }
            )
        }
    }
}

@Composable
fun ShoppingTab(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val sharedShoppingLists by viewModel.sharedShoppingLists.collectAsState()
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Shared Shopping Lists",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Button(
                    onClick = { /* Show create shopping list dialog */ }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create List")
                }
            }
        }
        
        items(sharedShoppingLists) { shoppingList ->
            SharedShoppingListCard(
                shoppingList = shoppingList,
                itemCount = 0, // Would fetch item count
                completedCount = 0, // Would fetch completed count
                onView = { /* Navigate to shopping list details */ }
            )
        }
    }
}