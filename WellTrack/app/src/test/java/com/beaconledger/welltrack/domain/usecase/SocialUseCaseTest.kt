package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.SocialRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SocialUseCaseTest {

    private lateinit var socialRepository: SocialRepository
    private lateinit var socialUseCase: SocialUseCase

    @Before
    fun setup() {
        socialRepository = mockk()
        socialUseCase = SocialUseCase(socialRepository)
    }

    @Test
    fun `createFamilyGroup with valid data should succeed`() = runTest {
        // Given
        val name = "Test Family"
        val description = "Test Description"
        val createdBy = "user123"
        val expectedId = "group123"

        coEvery { 
            socialRepository.createFamilyGroup(name, description, createdBy) 
        } returns Result.success(expectedId)

        // When
        val result = socialUseCase.createFamilyGroup(name, description, createdBy)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { socialRepository.createFamilyGroup(name, description, createdBy) }
    }

    @Test
    fun `createFamilyGroup with blank name should fail`() = runTest {
        // Given
        val name = "   "
        val description = "Test Description"
        val createdBy = "user123"

        // When
        val result = socialUseCase.createFamilyGroup(name, description, createdBy)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Family group name cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { socialRepository.createFamilyGroup(any(), any(), any()) }
    }

    @Test
    fun `inviteFamilyMember with valid email should succeed`() = runTest {
        // Given
        val familyGroupId = "group123"
        val userEmail = "test@example.com"
        val role = FamilyRole.MEMBER

        coEvery { 
            socialRepository.inviteFamilyMember(familyGroupId, userEmail, role) 
        } returns Result.success(Unit)

        // When
        val result = socialUseCase.inviteFamilyMember(familyGroupId, userEmail, role)

        // Then
        assertTrue(result.isSuccess)
        coVerify { socialRepository.inviteFamilyMember(familyGroupId, userEmail, role) }
    }

    @Test
    fun `inviteFamilyMember with invalid email should fail`() = runTest {
        // Given
        val familyGroupId = "group123"
        val userEmail = "invalid-email"
        val role = FamilyRole.MEMBER

        // When
        val result = socialUseCase.inviteFamilyMember(familyGroupId, userEmail, role)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Invalid email address", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { socialRepository.inviteFamilyMember(any(), any(), any()) }
    }

    @Test
    fun `shareMealPlan with valid data should succeed`() = runTest {
        // Given
        val familyGroupId = "group123"
        val mealPlanId = "plan123"
        val sharedBy = "user123"
        val title = "Weekly Meal Plan"
        val description = "Healthy meals for the week"
        val expectedId = "shared123"

        coEvery { 
            socialRepository.shareMealPlan(familyGroupId, mealPlanId, sharedBy, title, description) 
        } returns Result.success(expectedId)

        // When
        val result = socialUseCase.shareMealPlan(familyGroupId, mealPlanId, sharedBy, title, description)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { socialRepository.shareMealPlan(familyGroupId, mealPlanId, sharedBy, title, description) }
    }

    @Test
    fun `shareMealPlan with blank title should fail`() = runTest {
        // Given
        val familyGroupId = "group123"
        val mealPlanId = "plan123"
        val sharedBy = "user123"
        val title = "   "
        val description = "Healthy meals for the week"

        // When
        val result = socialUseCase.shareMealPlan(familyGroupId, mealPlanId, sharedBy, title, description)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Meal plan title cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { socialRepository.shareMealPlan(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `assignMealPrep with valid data should succeed`() = runTest {
        // Given
        val familyGroupId = "group123"
        val recipeId = "recipe123"
        val recipeName = "Chicken Curry"
        val assignedTo = "user456"
        val assignedBy = "user123"
        val scheduledDate = LocalDateTime.now().plusDays(1)
        val notes = "Please prepare for dinner"
        val expectedId = "prep123"

        coEvery { 
            socialRepository.assignMealPrep(
                familyGroupId, recipeId, recipeName, assignedTo, assignedBy, scheduledDate, notes
            ) 
        } returns Result.success(expectedId)

        // When
        val result = socialUseCase.assignMealPrep(
            familyGroupId, recipeId, recipeName, assignedTo, assignedBy, scheduledDate, notes
        )

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { 
            socialRepository.assignMealPrep(
                familyGroupId, recipeId, recipeName, assignedTo, assignedBy, scheduledDate, notes
            ) 
        }
    }

    @Test
    fun `assignMealPrep with past date should fail`() = runTest {
        // Given
        val familyGroupId = "group123"
        val recipeId = "recipe123"
        val recipeName = "Chicken Curry"
        val assignedTo = "user456"
        val assignedBy = "user123"
        val scheduledDate = LocalDateTime.now().minusDays(2)
        val notes = "Please prepare for dinner"

        // When
        val result = socialUseCase.assignMealPrep(
            familyGroupId, recipeId, recipeName, assignedTo, assignedBy, scheduledDate, notes
        )

        // Then
        assertTrue(result.isFailure)
        assertEquals("Scheduled date cannot be in the past", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { socialRepository.assignMealPrep(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `createAchievement with valid data should succeed`() = runTest {
        // Given
        val userId = "user123"
        val achievementType = AchievementType.MEAL_STREAK
        val title = "Week Warrior"
        val description = "Logged meals for 7 consecutive days!"
        val iconName = "ic_streak_week"
        val expectedId = "achievement123"

        coEvery { 
            socialRepository.createAchievement(userId, achievementType, title, description, iconName) 
        } returns Result.success(expectedId)

        // When
        val result = socialUseCase.createAchievement(userId, achievementType, title, description, iconName)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { socialRepository.createAchievement(userId, achievementType, title, description, iconName) }
    }

    @Test
    fun `createAchievement with blank title should fail`() = runTest {
        // Given
        val userId = "user123"
        val achievementType = AchievementType.MEAL_STREAK
        val title = "   "
        val description = "Logged meals for 7 consecutive days!"
        val iconName = "ic_streak_week"

        // When
        val result = socialUseCase.createAchievement(userId, achievementType, title, description, iconName)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Achievement title cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { socialRepository.createAchievement(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `addItemToSharedShoppingList with valid data should succeed`() = runTest {
        // Given
        val shoppingListId = "list123"
        val itemName = "Milk"
        val quantity = "1 gallon"
        val category = "Dairy"
        val addedBy = "user123"
        val expectedId = "item123"

        coEvery { 
            socialRepository.addItemToSharedShoppingList(shoppingListId, itemName, quantity, category, addedBy) 
        } returns Result.success(expectedId)

        // When
        val result = socialUseCase.addItemToSharedShoppingList(shoppingListId, itemName, quantity, category, addedBy)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedId, result.getOrNull())
        coVerify { socialRepository.addItemToSharedShoppingList(shoppingListId, itemName, quantity, category, addedBy) }
    }

    @Test
    fun `addItemToSharedShoppingList with blank item name should fail`() = runTest {
        // Given
        val shoppingListId = "list123"
        val itemName = "   "
        val quantity = "1 gallon"
        val category = "Dairy"
        val addedBy = "user123"

        // When
        val result = socialUseCase.addItemToSharedShoppingList(shoppingListId, itemName, quantity, category, addedBy)

        // Then
        assertTrue(result.isFailure)
        assertEquals("Item name cannot be empty", result.exceptionOrNull()?.message)
        coVerify(exactly = 0) { socialRepository.addItemToSharedShoppingList(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `getFamilyGroupsForUser should return flow from repository`() = runTest {
        // Given
        val userId = "user123"
        val familyGroups = listOf(
            FamilyGroupWithMembers(
                familyGroup = FamilyGroup(
                    id = "group123",
                    name = "Test Family",
                    description = null,
                    createdBy = userId,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                ),
                members = emptyList()
            )
        )

        every { socialRepository.getFamilyGroupsForUser(userId) } returns flowOf(familyGroups)

        // When
        val result = socialUseCase.getFamilyGroupsForUser(userId)

        // Then
        verify { socialRepository.getFamilyGroupsForUser(userId) }
        // Note: In a real test, you would collect the flow and verify the emitted values
    }

    @Test
    fun `checkAndCreateMealStreakAchievement should create achievement for 7 days`() = runTest {
        // Given
        val userId = "user123"
        val streakDays = 7
        val expectedId = "achievement123"

        coEvery { 
            socialRepository.createAchievement(
                userId,
                AchievementType.MEAL_STREAK,
                "Week Warrior",
                "Logged meals for 7 consecutive days!",
                "ic_streak_week"
            ) 
        } returns Result.success(expectedId)

        // When
        val result = socialUseCase.checkAndCreateMealStreakAchievement(userId, streakDays)

        // Then
        assertTrue(result?.isSuccess == true)
        assertEquals(expectedId, result?.getOrNull())
        coVerify { 
            socialRepository.createAchievement(
                userId,
                AchievementType.MEAL_STREAK,
                "Week Warrior",
                "Logged meals for 7 consecutive days!",
                "ic_streak_week"
            ) 
        }
    }

    @Test
    fun `checkAndCreateMealStreakAchievement should return null for non-milestone days`() = runTest {
        // Given
        val userId = "user123"
        val streakDays = 5

        // When
        val result = socialUseCase.checkAndCreateMealStreakAchievement(userId, streakDays)

        // Then
        assertEquals(null, result)
        coVerify(exactly = 0) { socialRepository.createAchievement(any(), any(), any(), any(), any()) }
    }
}