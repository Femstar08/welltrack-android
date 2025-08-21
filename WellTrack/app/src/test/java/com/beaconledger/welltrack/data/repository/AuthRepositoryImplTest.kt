package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.auth.SupabaseAuthManager
import com.beaconledger.welltrack.data.database.dao.UserDao
import com.beaconledger.welltrack.data.model.User
import com.beaconledger.welltrack.data.model.UserPreferences
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryImplTest {

    @Mock
    private lateinit var supabaseAuthManager: SupabaseAuthManager

    @Mock
    private lateinit var userDao: UserDao

    private lateinit var repository: AuthRepositoryImpl

    private val testUser = User(
        id = "user1",
        email = "test@example.com",
        name = "Test User",
        profilePhoto = null,
        age = 30,
        fitnessGoals = emptyList(),
        dietaryRestrictions = emptyList(),
        preferences = UserPreferences()
    )

    @Before
    fun setup() {
        repository = AuthRepositoryImpl(supabaseAuthManager, userDao)
    }

    @Test
    fun `signUp creates user account successfully`() = runTest {
        // Given
        whenever(supabaseAuthManager.signUp("test@example.com", "password123"))
            .thenReturn(Result.success("user1"))

        // When
        val result = repository.signUp("test@example.com", "password123")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("user1", result.getOrNull())
        verify(supabaseAuthManager).signUp("test@example.com", "password123")
    }

    @Test
    fun `signIn authenticates user successfully`() = runTest {
        // Given
        whenever(supabaseAuthManager.signIn("test@example.com", "password123"))
            .thenReturn(Result.success("user1"))

        // When
        val result = repository.signIn("test@example.com", "password123")

        // Then
        assertTrue(result.isSuccess)
        assertEquals("user1", result.getOrNull())
        verify(supabaseAuthManager).signIn("test@example.com", "password123")
    }

    @Test
    fun `signOut logs out user successfully`() = runTest {
        // Given
        whenever(supabaseAuthManager.signOut()).thenReturn(Result.success(Unit))

        // When
        val result = repository.signOut()

        // Then
        assertTrue(result.isSuccess)
        verify(supabaseAuthManager).signOut()
    }

    @Test
    fun `getCurrentUser returns current user`() = runTest {
        // Given
        whenever(supabaseAuthManager.getCurrentUser()).thenReturn(testUser)

        // When
        val result = repository.getCurrentUser()

        // Then
        assertEquals(testUser, result)
        verify(supabaseAuthManager).getCurrentUser()
    }

    @Test
    fun `saveUserProfile saves user to database`() = runTest {
        // When
        val result = repository.saveUserProfile(testUser)

        // Then
        assertTrue(result.isSuccess)
        verify(userDao).insertUser(testUser)
    }

    @Test
    fun `updateUserProfile updates user in database`() = runTest {
        // When
        val result = repository.updateUserProfile(testUser)

        // Then
        assertTrue(result.isSuccess)
        verify(userDao).updateUser(testUser)
    }
}