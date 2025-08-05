package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.AuthResult
import com.beaconledger.welltrack.data.model.AuthState
import com.beaconledger.welltrack.data.model.AuthUser
import com.beaconledger.welltrack.data.model.AuthSession
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    val currentUser: StateFlow<AuthUser?>
    val currentSession: StateFlow<AuthSession?>
    
    suspend fun signUp(email: String, password: String, name: String): AuthResult
    suspend fun signIn(email: String, password: String): AuthResult
    suspend fun signInWithProvider(provider: String): AuthResult
    suspend fun signOut(): Result<Unit>
    suspend fun refreshToken(): Result<Unit>
    fun isAuthenticated(): Boolean
    fun getCurrentUserId(): String?
}