package com.beaconledger.welltrack.presentation.social

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.SocialUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val socialUseCase: SocialUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _selectedFamilyGroupId = MutableStateFlow<String?>(null)
    val selectedFamilyGroupId: StateFlow<String?> = _selectedFamilyGroupId.asStateFlow()

    // Family Groups
    val familyGroups: StateFlow<List<FamilyGroupWithMembers>> = currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                socialUseCase.getFamilyGroupsForUser(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Family Members for selected group
    val familyMembers: StateFlow<List<FamilyMemberInfo>> = selectedFamilyGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                socialUseCase.getFamilyMembers(groupId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Shared content for selected group
    val sharedMealPlans: StateFlow<List<SharedMealPlanWithDetails>> = selectedFamilyGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                socialUseCase.getSharedMealPlans(groupId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedRecipes: StateFlow<List<SharedRecipeWithDetails>> = selectedFamilyGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                socialUseCase.getSharedRecipes(groupId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val collaborativeMealPrep: StateFlow<List<CollaborativeMealPrepWithDetails>> = selectedFamilyGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                socialUseCase.getCollaborativeMealPrep(groupId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedAchievements: StateFlow<List<SharedAchievement>> = selectedFamilyGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                socialUseCase.getSharedAchievements(groupId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sharedShoppingLists: StateFlow<List<SharedShoppingList>> = selectedFamilyGroupId
        .flatMapLatest { groupId ->
            if (groupId != null) {
                socialUseCase.getSharedShoppingLists(groupId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User's meal prep assignments
    val myMealPrepAssignments: StateFlow<List<CollaborativeMealPrepWithDetails>> = currentUserId
        .flatMapLatest { userId ->
            if (userId.isNotEmpty()) {
                socialUseCase.getMealPrepAssignedToUser(userId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setCurrentUserId(userId: String) {
        _currentUserId.value = userId
    }

    fun selectFamilyGroup(familyGroupId: String?) {
        _selectedFamilyGroupId.value = familyGroupId
    }

    // Family Group Operations
    fun createFamilyGroup(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            socialUseCase.createFamilyGroup(name, description, currentUserId.value)
                .onSuccess { groupId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Family group created successfully!"
                    )
                    _selectedFamilyGroupId.value = groupId
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create family group"
                    )
                }
        }
    }

    fun inviteFamilyMember(familyGroupId: String, userEmail: String, role: FamilyRole) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            socialUseCase.inviteFamilyMember(familyGroupId, userEmail, role)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Invitation sent successfully!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to send invitation"
                    )
                }
        }
    }

    fun updateFamilyMemberRole(familyGroupId: String, userId: String, role: FamilyRole) {
        viewModelScope.launch {
            socialUseCase.updateFamilyMemberRole(familyGroupId, userId, role)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update member role"
                    )
                }
        }
    }

    fun removeFamilyMember(familyGroupId: String, userId: String) {
        viewModelScope.launch {
            socialUseCase.removeFamilyMember(familyGroupId, userId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Member removed successfully"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to remove member"
                    )
                }
        }
    }

    // Meal Plan Sharing
    fun shareMealPlan(mealPlanId: String, title: String, description: String?) {
        val familyGroupId = selectedFamilyGroupId.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            socialUseCase.shareMealPlan(familyGroupId, mealPlanId, currentUserId.value, title, description)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Meal plan shared successfully!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to share meal plan"
                    )
                }
        }
    }

    fun unshareeMealPlan(sharedMealPlanId: String) {
        viewModelScope.launch {
            socialUseCase.unshareeMealPlan(sharedMealPlanId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Meal plan unshared"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to unshare meal plan"
                    )
                }
        }
    }

    // Recipe Sharing
    fun shareRecipe(recipeId: String, message: String?) {
        val familyGroupId = selectedFamilyGroupId.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            socialUseCase.shareRecipe(familyGroupId, recipeId, currentUserId.value, message)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Recipe shared successfully!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to share recipe"
                    )
                }
        }
    }

    fun unshareRecipe(sharedRecipeId: String) {
        viewModelScope.launch {
            socialUseCase.unshareRecipe(sharedRecipeId)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Recipe unshared"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to unshare recipe"
                    )
                }
        }
    }

    // Collaborative Meal Prep
    fun assignMealPrep(
        recipeId: String,
        recipeName: String,
        assignedTo: String,
        scheduledDate: LocalDateTime,
        notes: String?
    ) {
        val familyGroupId = selectedFamilyGroupId.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            socialUseCase.assignMealPrep(
                familyGroupId,
                recipeId,
                recipeName,
                assignedTo,
                currentUserId.value,
                scheduledDate,
                notes
            )
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Meal prep assigned successfully!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to assign meal prep"
                    )
                }
        }
    }

    fun updateMealPrepStatus(mealPrepId: String, status: MealPrepStatus) {
        viewModelScope.launch {
            socialUseCase.updateMealPrepStatus(mealPrepId, status)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Meal prep status updated"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update status"
                    )
                }
        }
    }

    fun updateMealPrepNotes(mealPrepId: String, notes: String) {
        viewModelScope.launch {
            socialUseCase.updateMealPrepNotes(mealPrepId, notes)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update notes"
                    )
                }
        }
    }

    // Achievement Sharing
    fun shareAchievement(achievementId: String, shareMessage: String?) {
        val familyGroupId = selectedFamilyGroupId.value ?: return
        
        viewModelScope.launch {
            socialUseCase.shareAchievement(achievementId, familyGroupId, shareMessage)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        message = "Achievement shared!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to share achievement"
                    )
                }
        }
    }

    fun addAchievementReaction(sharedAchievementId: String, reactionType: ReactionType) {
        viewModelScope.launch {
            socialUseCase.addAchievementReaction(sharedAchievementId, currentUserId.value, reactionType)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to add reaction"
                    )
                }
        }
    }

    fun removeAchievementReaction(sharedAchievementId: String) {
        viewModelScope.launch {
            socialUseCase.removeAchievementReaction(sharedAchievementId, currentUserId.value)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to remove reaction"
                    )
                }
        }
    }

    // Shared Shopping Lists
    fun createSharedShoppingList(name: String) {
        val familyGroupId = selectedFamilyGroupId.value ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            socialUseCase.createSharedShoppingList(familyGroupId, name, currentUserId.value)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Shopping list created!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to create shopping list"
                    )
                }
        }
    }

    fun addItemToSharedShoppingList(
        shoppingListId: String,
        itemName: String,
        quantity: String,
        category: String?
    ) {
        viewModelScope.launch {
            socialUseCase.addItemToSharedShoppingList(
                shoppingListId,
                itemName,
                quantity,
                category,
                currentUserId.value
            )
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to add item"
                    )
                }
        }
    }

    fun updateShoppingListItemPurchaseStatus(itemId: String, isPurchased: Boolean) {
        viewModelScope.launch {
            val purchasedBy = if (isPurchased) currentUserId.value else null
            socialUseCase.updateShoppingListItemPurchaseStatus(itemId, isPurchased, purchasedBy)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to update item status"
                    )
                }
        }
    }

    fun deleteSharedShoppingListItem(itemId: String) {
        viewModelScope.launch {
            socialUseCase.deleteSharedShoppingListItem(itemId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to delete item"
                    )
                }
        }
    }

    // Sync Operations
    fun syncFamilyData() {
        viewModelScope.launch {
            socialUseCase.syncFamilyData(currentUserId.value)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to sync family data"
                    )
                }
        }
    }

    fun syncSharedContent() {
        val familyGroupId = selectedFamilyGroupId.value ?: return
        
        viewModelScope.launch {
            socialUseCase.syncSharedContent(familyGroupId)
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message ?: "Failed to sync shared content"
                    )
                }
        }
    }

    // UI State Management
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SocialUiState(
    val isLoading: Boolean = false,
    val message: String? = null,
    val error: String? = null
)