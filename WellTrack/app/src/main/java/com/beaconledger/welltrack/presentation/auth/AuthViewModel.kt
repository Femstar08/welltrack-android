package com.beaconledger.welltrack.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.AuthResult
import com.beaconledger.welltrack.data.model.AuthState
import com.beaconledger.welltrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepository.authState
    val currentUser = authRepository.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.signUp(email, password, name)) {
                is AuthResult.Success -> {
                    // Success handled by authState flow
                }
                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }
                is AuthResult.Loading -> {
                    // Loading state handled by _isLoading
                }
            }

            _isLoading.value = false
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.signIn(email, password)) {
                is AuthResult.Success -> {
                    // Success handled by authState flow
                }
                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }
                is AuthResult.Loading -> {
                    // Loading state handled by _isLoading
                }
            }

            _isLoading.value = false
        }
    }

    fun signInWithProvider(provider: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            when (val result = authRepository.signInWithProvider(provider)) {
                is AuthResult.Success -> {
                    // Success handled by authState flow
                }
                is AuthResult.Error -> {
                    _errorMessage.value = result.message
                }
                is AuthResult.Loading -> {
                    // Loading state handled by _isLoading
                }
            }

            _isLoading.value = false
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun isAuthenticated(): Boolean {
        return authRepository.isAuthenticated()
    }
}