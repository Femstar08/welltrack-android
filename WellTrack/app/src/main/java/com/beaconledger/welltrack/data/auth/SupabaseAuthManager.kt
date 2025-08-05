package com.beaconledger.welltrack.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.network.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseAuthManager @Inject constructor(
    private val context: Context,
    private val sharedPreferences: SharedPreferences,
    private val supabaseClient: SupabaseClient
) {
    
    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()
    
    private val _currentSession = MutableStateFlow<AuthSession?>(null)
    val currentSession: StateFlow<AuthSession?> = _currentSession.asStateFlow()
    
    companion object {
        private const val PREF_USER_ID = "user_id"
        private const val PREF_USER_EMAIL = "user_email"
        private const val PREF_ACCESS_TOKEN = "access_token"
        private const val PREF_REFRESH_TOKEN = "refresh_token"
        private const val PREF_EXPIRES_IN = "expires_in"
        private const val PREF_EMAIL_CONFIRMED = "email_confirmed"
        private const val PREF_CREATED_AT = "created_at"
    }
    
    init {
        // Check for existing session on initialization
        loadSavedSession()
    }
    
    private fun loadSavedSession() {
        try {
            val userId = sharedPreferences.getString(PREF_USER_ID, null)
            val userEmail = sharedPreferences.getString(PREF_USER_EMAIL, null)
            val accessToken = sharedPreferences.getString(PREF_ACCESS_TOKEN, null)
            
            if (userId != null && userEmail != null && accessToken != null) {
                val user = AuthUser(
                    id = userId,
                    email = userEmail,
                    emailConfirmed = sharedPreferences.getBoolean(PREF_EMAIL_CONFIRMED, false),
                    createdAt = sharedPreferences.getString(PREF_CREATED_AT, "") ?: ""
                )
                
                val session = AuthSession(
                    accessToken = accessToken,
                    refreshToken = sharedPreferences.getString(PREF_REFRESH_TOKEN, "") ?: "",
                    expiresIn = sharedPreferences.getLong(PREF_EXPIRES_IN, 3600)
                )
                
                _currentUser.value = user
                _currentSession.value = session
                _authState.value = AuthState.AUTHENTICATED
            } else {
                _authState.value = AuthState.UNAUTHENTICATED
            }
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Failed to load saved session", e)
            _authState.value = AuthState.UNAUTHENTICATED
        }
    }
    
    suspend fun signUp(email: String, password: String, name: String): AuthResult {
        return try {
            _authState.value = AuthState.LOADING
            
            // Mock implementation for development
            val mockUser = AuthUser(
                id = "mock-user-${System.currentTimeMillis()}",
                email = email,
                emailConfirmed = true,
                createdAt = "2024-01-01T00:00:00Z"
            )
            
            val mockSession = AuthSession(
                accessToken = "mock-access-token-${System.currentTimeMillis()}",
                refreshToken = "mock-refresh-token",
                expiresIn = 3600
            )
            
            saveSession(mockUser, mockSession)
            _currentUser.value = mockUser
            _currentSession.value = mockSession
            _authState.value = AuthState.AUTHENTICATED
            
            AuthResult.Success(mockUser, mockSession)
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign up failed", e)
            _authState.value = AuthState.ERROR
            AuthResult.Error("Sign up failed")
        }
    }
    
    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            _authState.value = AuthState.LOADING
            
            // Mock implementation for development
            val mockUser = AuthUser(
                id = "mock-user-${System.currentTimeMillis()}",
                email = email,
                emailConfirmed = true,
                createdAt = "2024-01-01T00:00:00Z"
            )
            
            val mockSession = AuthSession(
                accessToken = "mock-access-token-${System.currentTimeMillis()}",
                refreshToken = "mock-refresh-token",
                expiresIn = 3600
            )
            
            saveSession(mockUser, mockSession)
            _currentUser.value = mockUser
            _currentSession.value = mockSession
            _authState.value = AuthState.AUTHENTICATED
            
            AuthResult.Success(mockUser, mockSession)
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign in failed", e)
            _authState.value = AuthState.ERROR
            AuthResult.Error("Sign in failed")
        }
    }
    
    suspend fun signOut(): Boolean {
        return try {
            clearSession()
            _currentUser.value = null
            _currentSession.value = null
            _authState.value = AuthState.UNAUTHENTICATED
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign out failed", e)
            false
        }
    }
    
    suspend fun getCurrentUser(): AuthUser? {
        return _currentUser.value
    }
    
    suspend fun getCurrentSession(): AuthSession? {
        return _currentSession.value
    }
    
    suspend fun refreshSession(): AuthResult? {
        val currentSession = _currentSession.value ?: return null
        
        return try {
            // Mock refresh for development
            val refreshedSession = AuthSession(
                accessToken = "refreshed-access-token-${System.currentTimeMillis()}",
                refreshToken = currentSession.refreshToken,
                expiresIn = 3600
            )
            
            val currentUser = _currentUser.value
            if (currentUser != null) {
                saveSession(currentUser, refreshedSession)
                _currentSession.value = refreshedSession
                AuthResult.Success(currentUser, refreshedSession)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Session refresh failed", e)
            AuthResult.Error("Session refresh failed")
        }
    }
    
    private fun saveSession(user: AuthUser, session: AuthSession) {
        with(sharedPreferences.edit()) {
            putString(PREF_USER_ID, user.id)
            putString(PREF_USER_EMAIL, user.email)
            putString(PREF_ACCESS_TOKEN, session.accessToken)
            putString(PREF_REFRESH_TOKEN, session.refreshToken)
            putLong(PREF_EXPIRES_IN, session.expiresIn)
            putBoolean(PREF_EMAIL_CONFIRMED, user.emailConfirmed)
            putString(PREF_CREATED_AT, user.createdAt)
            apply()
        }
    }
    
    private fun clearSession() {
        with(sharedPreferences.edit()) {
            remove(PREF_USER_ID)
            remove(PREF_USER_EMAIL)
            remove(PREF_ACCESS_TOKEN)
            remove(PREF_REFRESH_TOKEN)
            remove(PREF_EXPIRES_IN)
            remove(PREF_EMAIL_CONFIRMED)
            remove(PREF_CREATED_AT)
            apply()
        }
    }
    
    // Additional methods needed by AuthRepositoryImpl
    suspend fun signInWithProvider(provider: String): AuthResult {
        return try {
            _authState.value = AuthState.LOADING
            
            // Mock implementation for development
            val mockUser = AuthUser(
                id = "mock-user-${System.currentTimeMillis()}",
                email = "user@${provider}.com",
                emailConfirmed = true,
                createdAt = "2024-01-01T00:00:00Z"
            )
            
            val mockSession = AuthSession(
                accessToken = "mock-access-token-${System.currentTimeMillis()}",
                refreshToken = "mock-refresh-token",
                expiresIn = 3600
            )
            
            saveSession(mockUser, mockSession)
            _currentUser.value = mockUser
            _currentSession.value = mockSession
            _authState.value = AuthState.AUTHENTICATED
            
            AuthResult.Success(mockUser, mockSession)
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Provider sign in failed", e)
            _authState.value = AuthState.ERROR
            AuthResult.Error("Provider sign in failed")
        }
    }
    
    fun isAuthenticated(): Boolean {
        return _authState.value == AuthState.AUTHENTICATED && _currentUser.value != null
    }
    
    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }
}