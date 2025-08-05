package com.beaconledger.welltrack.auth

import android.content.Context
import android.content.SharedPreferences
import com.beaconledger.welltrack.data.auth.SupabaseAuthManager
import com.beaconledger.welltrack.data.model.AuthResult
import com.beaconledger.welltrack.data.model.AuthState
import com.beaconledger.welltrack.data.network.SupabaseClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class SupabaseAuthManagerTest {

    @Mock
    private lateinit var context: Context
    
    @Mock
    private lateinit var sharedPreferences: SharedPreferences
    
    @Mock
    private lateinit var sharedPreferencesEditor: SharedPreferences.Editor
    
    @Mock
    private lateinit var supabaseClient: SupabaseClient

    private lateinit var authManager: SupabaseAuthManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Mock SharedPreferences behavior
        whenever(sharedPreferences.edit()).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putString(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.putLong(org.mockito.kotlin.any(), org.mockito.kotlin.any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.remove(org.mockito.kotlin.any())).thenReturn(sharedPreferencesEditor)
        whenever(sharedPreferencesEditor.apply()).then { }
        
        // Mock no existing session
        whenever(sharedPreferences.getString("access_token", null)).thenReturn(null)
        whenever(sharedPreferences.getString("refresh_token", null)).thenReturn(null)
        whenever(sharedPreferences.getString("user_id", null)).thenReturn(null)
        whenever(sharedPreferences.getString("user_email", null)).thenReturn(null)
        whenever(sharedPreferences.getLong("token_expires_at", 0)).thenReturn(0)

        authManager = SupabaseAuthManager(context, sharedPreferences, supabaseClient)
    }

    @Test
    fun `initial state should be unauthenticated when no session exists`() = runTest {
        val authState = authManager.authState.first()
        assertEquals(AuthState.UNAUTHENTICATED, authState)
    }

    @Test
    fun `isAuthenticated should return false when not authenticated`() {
        val isAuthenticated = authManager.isAuthenticated()
        assertEquals(false, isAuthenticated)
    }

    @Test
    fun `getCurrentUserId should return null when not authenticated`() {
        val userId = authManager.getCurrentUserId()
        assertEquals(null, userId)
    }

    // Note: Testing actual Supabase calls would require integration tests
    // These unit tests focus on the manager's state management logic
}