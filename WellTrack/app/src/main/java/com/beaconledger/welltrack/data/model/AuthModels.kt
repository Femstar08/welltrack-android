package com.beaconledger.welltrack.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class SignUpRequest(
    val email: String,
    val password: String,
    val name: String
)

data class AuthResponse(
    val user: AuthUser?,
    val session: AuthSession?,
    val error: String? = null
)

data class AuthUser(
    val id: String,
    val email: String,
    val emailConfirmed: Boolean = false,
    val createdAt: String
)

data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String = "bearer"
)

data class SocialLoginProvider(
    val name: String,
    val displayName: String,
    val iconRes: Int? = null
)

enum class AuthState {
    LOADING,
    AUTHENTICATED,
    UNAUTHENTICATED,
    ERROR
}

sealed class AuthResult {
    object Loading : AuthResult()
    data class Success(val user: AuthUser, val session: AuthSession) : AuthResult()
    data class Error(val message: String) : AuthResult()
}