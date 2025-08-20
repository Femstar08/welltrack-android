package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.NotificationRepository
import com.beaconledger.welltrack.domain.repository.MealPlanRepository
import com.beaconledger.welltrack.domain.repository.SupplementRepository
import com.beaconledger.welltrack.domain.repository.PantryRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime

class NotificationUseCaseTest {
    
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var mealPlanRepository: MealPlanRepository
    private lateinit var supplementRepository: SupplementRepository
    private lateinit var pantryRepository: PantryRepository
    private lateinit var notificationUseCase: NotificationUseCase
    
    private val testUserId = "test-user-id"
    
    @Before
    fun setup() {
        notificationRepository = mockk()
        mealPlanRepository = mockk()
        supplementRepository = mockk()
        pantryRepository = mockk()
        
        notificationUseCase = NotificationUseCase(
            notificationRepository = notificationRepository,
            mealPlanRepository = mealPlanRepository,
            supplementRepository = supplementRepository,
            pantryRepository = pantryRepository
        )
    }
    
    @Test
    fun `getUserNotifications returns flow of notifications`() = runTest {
        // Given
        val expectedNotifications = listOf(
            NotificationEntity(
                id = "1",
                userId = testUserId,
                type = NotificationType.MEAL_REMINDER,
                title = "Breakfast Reminder",
                message = "Time for breakfast",
                scheduledTime = LocalDateTime.now()
            )
        )
        every { notificationRepository.getNotificationsForUser(testUserId) } returns flowOf(expectedNotifications)
        
        // When
        val result = notificationUseCase.getUserNotifications(testUserId)
        
        // Then
        result.collect { notifications ->
            assertEquals(expectedNotifications, notifications)
        }
        verify { notificationRepository.getNotificationsForUser(testUserId) }
    }
    
    @Test
    fun `getNotificationPreferences returns user preferences`() = runTest {
        // Given
        val expectedPreferences = NotificationPreferences(
            userId = testUserId,
            mealRemindersEnabled = true,
            supplementRemindersEnabled = true
        )
        every { notificationRepository.getUserNotificationPreferences(testUserId) } returns expectedPreferences
        
        // When
        val result = notificationUseCase.getNotificationPreferences(testUserId)
        
        // Then
        assertEquals(expectedPreferences, result)
        verify { notificationRepository.getUserNotificationPreferences(testUserId) }
    }
    
    @Test
    fun `getNotificationPreferences returns default when null`() = runTest {
        // Given
        every { notificationRepository.getUserNotificationPreferences(testUserId) } returns null
        
        // When
        val result = notificationUseCase.getNotificationPreferences(testUserId)
        
        // Then
        assertEquals(testUserId, result.userId)
        assertTrue(result.mealRemindersEnabled)
        assertTrue(result.supplementRemindersEnabled)
        verify { notificationRepository.getUserNotificationPreferences(testUserId) }
    }
    
    @Test
    fun `updateNotificationPreferences calls repository`() = runTest {
        // Given
        val preferences = NotificationPreferences(userId = testUserId)
        every { notificationRepository.updateNotificationPreferences(preferences) } returns Result.success(Unit)
        
        // When
        val result = notificationUseCase.updateNotificationPreferences(preferences)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.updateNotificationPreferences(preferences) }
    }
    
    @Test
    fun `setupDailyMealReminders creates meal reminders when enabled`() = runTest {
        // Given
        val preferences = NotificationPreferences(
            userId = testUserId,
            mealRemindersEnabled = true
        )
        every { notificationRepository.getUserNotificationPreferences(testUserId) } returns preferences
        every { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.MEAL_REMINDER) } returns Result.success(Unit)
        every { notificationRepository.scheduleMealReminder(any(), any(), any(), any(), any()) } returns Result.success("notification-id")
        
        // When
        val result = notificationUseCase.setupDailyMealReminders(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.MEAL_REMINDER) }
        verify(atLeast = 1) { notificationRepository.scheduleMealReminder(any(), any(), any(), any(), any()) }
    }
    
    @Test
    fun `setupDailyMealReminders skips when disabled`() = runTest {
        // Given
        val preferences = NotificationPreferences(
            userId = testUserId,
            mealRemindersEnabled = false
        )
        every { notificationRepository.getUserNotificationPreferences(testUserId) } returns preferences
        every { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.MEAL_REMINDER) } returns Result.success(Unit)
        
        // When
        val result = notificationUseCase.setupDailyMealReminders(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.MEAL_REMINDER) }
        verify(exactly = 0) { notificationRepository.scheduleMealReminder(any(), any(), any(), any(), any()) }
    }
    
    @Test
    fun `setupSupplementReminders creates reminders for user supplements`() = runTest {
        // Given
        val preferences = NotificationPreferences(
            userId = testUserId,
            supplementRemindersEnabled = true
        )
        val supplements = listOf(
            UserSupplement(
                id = "1",
                userId = testUserId,
                supplementId = "sup-1",
                name = "Vitamin D",
                dosage = "1000 IU",
                frequency = "daily",
                isActive = true,
                createdAt = LocalDateTime.now()
            )
        )
        
        every { notificationRepository.getUserNotificationPreferences(testUserId) } returns preferences
        every { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.SUPPLEMENT_REMINDER) } returns Result.success(Unit)
        every { supplementRepository.getSupplementsForUser(testUserId) } returns flowOf(supplements)
        every { notificationRepository.scheduleSupplementReminder(any(), any(), any(), any(), any(), any()) } returns Result.success("notification-id")
        
        // When
        val result = notificationUseCase.setupSupplementReminders(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.SUPPLEMENT_REMINDER) }
        verify { supplementRepository.getSupplementsForUser(testUserId) }
    }
    
    @Test
    fun `setupPantryExpiryAlerts creates alerts for expiring items`() = runTest {
        // Given
        val preferences = NotificationPreferences(
            userId = testUserId,
            pantryAlertsEnabled = true
        )
        val expiringItems = listOf(
            PantryItem(
                id = "1",
                userId = testUserId,
                ingredientName = "Milk",
                quantity = 1.0,
                unit = "L",
                expiryDate = LocalDateTime.now().plusDays(1),
                purchaseDate = LocalDateTime.now().minusDays(5),
                barcode = null,
                createdAt = LocalDateTime.now()
            )
        )
        
        every { notificationRepository.getUserNotificationPreferences(testUserId) } returns preferences
        every { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.PANTRY_EXPIRY_ALERT) } returns Result.success(Unit)
        every { pantryRepository.getExpiringItems(testUserId, 7) } returns flowOf(expiringItems)
        every { notificationRepository.findRecipesForExpiringIngredient("Milk") } returns listOf("Pancakes", "Smoothie")
        every { notificationRepository.schedulePantryExpiryAlert(any(), any(), any(), any()) } returns Result.success("notification-id")
        
        // When
        val result = notificationUseCase.setupPantryExpiryAlerts(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.PANTRY_EXPIRY_ALERT) }
        verify { pantryRepository.getExpiringItems(testUserId, 7) }
        verify { notificationRepository.findRecipesForExpiringIngredient("Milk") }
        verify { notificationRepository.schedulePantryExpiryAlert(testUserId, "Milk", any(), listOf("Pancakes", "Smoothie")) }
    }
    
    @Test
    fun `snoozeNotification calls repository with correct parameters`() = runTest {
        // Given
        val notificationId = "notification-1"
        val snoozeMinutes = 15
        every { notificationRepository.snoozeNotification(notificationId, snoozeMinutes) } returns Result.success(Unit)
        
        // When
        val result = notificationUseCase.snoozeNotification(notificationId, snoozeMinutes)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.snoozeNotification(notificationId, snoozeMinutes) }
    }
    
    @Test
    fun `markNotificationComplete calls repository with correct parameters`() = runTest {
        // Given
        val notificationId = "notification-1"
        val itemId = "item-1"
        every { notificationRepository.markNotificationComplete(notificationId, itemId) } returns Result.success(Unit)
        
        // When
        val result = notificationUseCase.markNotificationComplete(notificationId, itemId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.markNotificationComplete(notificationId, itemId) }
    }
    
    @Test
    fun `setupAllNotifications calls all setup methods`() = runTest {
        // Given
        val preferences = NotificationPreferences(userId = testUserId)
        every { notificationRepository.getUserNotificationPreferences(testUserId) } returns preferences
        every { notificationRepository.deleteNotificationsByType(any(), any()) } returns Result.success(Unit)
        every { supplementRepository.getSupplementsForUser(testUserId) } returns flowOf(emptyList())
        every { pantryRepository.getExpiringItems(testUserId, 7) } returns flowOf(emptyList())
        every { notificationRepository.scheduleMotivationalNotification(any(), any(), any(), any(), any()) } returns Result.success("notification-id")
        
        // When
        val result = notificationUseCase.setupAllNotifications(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.MEAL_REMINDER) }
        verify { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.SUPPLEMENT_REMINDER) }
        verify { notificationRepository.deleteNotificationsByType(testUserId, NotificationType.PANTRY_EXPIRY_ALERT) }
    }
    
    @Test
    fun `cancelAllNotifications calls repository`() = runTest {
        // Given
        every { notificationRepository.cancelAllNotificationsForUser(testUserId) } returns Result.success(Unit)
        
        // When
        val result = notificationUseCase.cancelAllNotifications(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        verify { notificationRepository.cancelAllNotificationsForUser(testUserId) }
    }
}