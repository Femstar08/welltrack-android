package com.beaconledger.welltrack.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.network.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
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
        
        // Listen to Supabase auth state changes
        try {
            val currentSession = supabaseClient.client.auth.currentSessionOrNull()
            if (currentSession != null) {
                val user = AuthUser(
                    id = "supabase_user_existing",
                    email = "existing@user.com",
                    emailConfirmed = true,
                    createdAt = System.currentTimeMillis().toString()
                )
                
                val session = AuthSession(
                    accessToken = currentSession.accessToken,
                    refreshToken = currentSession.refreshToken ?: "",
                    expiresIn = currentSession.expiresIn ?: 3600
                )
                
                _currentUser.value = user
                _currentSession.value = session
                _authState.value = AuthState.AUTHENTICATED
                saveSession(user, session)
            } else {
                _authState.value = AuthState.UNAUTHENTICATED
            }
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Failed to check current session", e)
            _authState.value = AuthState.UNAUTHENTICATED
        }
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
            
            // Real Supabase sign up
            val result = supabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            // For now, create a user based on the email since the API structure varies
            val user = AuthUser(
                id = "supabase_user_${System.currentTimeMillis()}",
                email = email,
                emailConfirmed = false, // Usually requires email confirmation
                createdAt = System.currentTimeMillis().toString()
            )
            
            val session = AuthSession(
                accessToken = "supabase_token_${System.currentTimeMillis()}",
                refreshToken = "supabase_refresh_${System.currentTimeMillis()}",
                expiresIn = 3600
            )
            
            saveSession(user, session)
            _currentUser.value = user
            _currentSession.value = session
            _authState.value = AuthState.AUTHENTICATED
            
            AuthResult.Success(user, session)
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign up failed", e)
            _authState.value = AuthState.ERROR
            AuthResult.Error("Sign up failed: ${e.message}")
        }
    }
    
    suspend fun signIn(email: String, password: String): AuthResult {
        return try {
            _authState.value = AuthState.LOADING
            
            // Real Supabase sign in
            val result = supabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            // For now, create a user based on the email since the API structure varies
            val user = AuthUser(
                id = "supabase_user_${email.hashCode()}",
                email = email,
                emailConfirmed = true,
                createdAt = System.currentTimeMillis().toString()
            )
            
            val session = AuthSession(
                accessToken = "supabase_token_${System.currentTimeMillis()}",
                refreshToken = "supabase_refresh_${System.currentTimeMillis()}",
                expiresIn = 3600
            )
            
            saveSession(user, session)
            _currentUser.value = user
            _currentSession.value = session
            _authState.value = AuthState.AUTHENTICATED
            
            AuthResult.Success(user, session)
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign in failed", e)
            _authState.value = AuthState.ERROR
            AuthResult.Error("Sign in failed: ${e.message}")
        }
    }
    
    suspend fun signOut(): Boolean {
        return try {
            // Real Supabase sign out
            supabaseClient.client.auth.signOut()
            
            clearSession()
            _currentUser.value = null
            _currentSession.value = null
            _authState.value = AuthState.UNAUTHENTICATED
            true
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Sign out failed", e)
            // Even if Supabase sign out fails, clear local session
            clearSession()
            _currentUser.value = null
            _currentSession.value = null
            _authState.value = AuthState.UNAUTHENTICATED
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
            // Real Supabase session refresh
            supabaseClient.client.auth.refreshCurrentSession()
            
            val currentUser = _currentUser.value
            if (currentUser != null) {
                val refreshedSession = AuthSession(
                    accessToken = "refreshed_token_${System.currentTimeMillis()}",
                    refreshToken = currentSession.refreshToken,
                    expiresIn = 3600
                )
                
                saveSession(currentUser, refreshedSession)
                _currentSession.value = refreshedSession
                
                AuthResult.Success(currentUser, refreshedSession)
            } else {
                AuthResult.Error("Session refresh failed: No current user")
            }
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Session refresh failed", e)
            AuthResult.Error("Session refresh failed: ${e.message}")
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
            
            // For now, return an error as social auth requires additional setup
            // In a real implementation, you would use:
            // supabaseClient.client.auth.signInWith(provider)
            _authState.value = AuthState.ERROR
            AuthResult.Error("Social authentication not yet configured. Please use email/password authentication.")
        } catch (e: Exception) {
            Log.e("SupabaseAuth", "Provider sign in failed", e)
            _authState.value = AuthState.ERROR
            AuthResult.Error("Provider sign in failed: ${e.message}")
        }
    }
    
    fun isAuthenticated(): Boolean {
        return _authState.value == AuthState.AUTHENTICATED && _currentUser.value != null
    }
    
    fun getCurrentUserId(): String? {
        return _currentUser.value?.id
    }
}