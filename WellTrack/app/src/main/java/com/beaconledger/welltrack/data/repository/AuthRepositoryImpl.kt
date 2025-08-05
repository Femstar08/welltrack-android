package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.auth.SupabaseAuthManager
import com.beaconledger.welltrack.data.auth.SessionManager
import com.beaconledger.welltrack.data.model.AuthResult
import com.beaconledger.welltrack.data.model.AuthState
import com.beaconledger.welltrack.data.model.AuthUser
import com.beaconledger.welltrack.data.model.AuthSession
import com.beaconledger.welltrack.domain.repository.AuthRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val supabaseAuthManager: SupabaseAuthManager,
    private val sessionManager: SessionManager
) : AuthRepository {

    override val authState: StateFlow<AuthState> = supabaseAuthManager.authState
    override val currentUser: StateFlow<AuthUser?> = supabaseAuthManager.currentUser
    override val currentSession: StateFlow<AuthSession?> = supabaseAuthManager.currentSession

    override suspend fun signUp(email: String, password: String, name: String): AuthResult {
        val result = supabaseAuthManager.signUp(email, password, name)
        if (result is AuthResult.Success) {
            sessionManager.updateLastActivity()
        }
        return result
    }

    override suspend fun signIn(email: String, password: String): AuthResult {
        val result = supabaseAuthManager.signIn(email, password)
        if (result is AuthResult.Success) {
            sessionManager.updateLastActivity()
        }
        return result
    }

    override suspend fun signInWithProvider(provider: String): AuthResult {
        val result = supabaseAuthManager.signInWithProvider(provider)
        if (result is AuthResult.Success) {
            sessionManager.updateLastActivity()
        }
        return result
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            val success = supabaseAuthManager.signOut()
            if (success) {
                sessionManager.invalidateSession()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Sign out failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        return try {
            // TODO: Implement token refresh logic
            sessionManager.extendSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isAuthenticated(): Boolean {
        return supabaseAuthManager.isAuthenticated() && sessionManager.checkSessionValidity()
    }

    override fun getCurrentUserId(): String? {
        return if (isAuthenticated()) {
            supabaseAuthManager.getCurrentUserId()
        } else {
            null
        }
    }
}