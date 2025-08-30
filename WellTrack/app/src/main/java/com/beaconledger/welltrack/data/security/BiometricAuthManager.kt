package com.beaconledger.welltrack.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class BiometricAuthManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    sealed class BiometricResult {
        object Success : BiometricResult()
        data class Error(val message: String) : BiometricResult()
        object UserCancelled : BiometricResult()
        object BiometricNotAvailable : BiometricResult()
        object BiometricNotEnrolled : BiometricResult()
    }
    
    fun isBiometricAvailable(): BiometricResult {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricResult.Success
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> 
                BiometricResult.BiometricNotAvailable
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> 
                BiometricResult.Error("Biometric hardware unavailable")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> 
                BiometricResult.BiometricNotEnrolled
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                BiometricResult.Error("Security update required")
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                BiometricResult.Error("Biometric authentication not supported")
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                BiometricResult.Error("Biometric status unknown")
            else -> BiometricResult.Error("Unknown biometric error")
        }
    }
    
    suspend fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String = "Biometric Authentication",
        subtitle: String = "Use your fingerprint or face to authenticate",
        negativeButtonText: String = "Cancel"
    ): BiometricResult = suspendCancellableCoroutine { continuation ->
        
        val biometricPrompt = BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(context),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    if (continuation.isActive) {
                        continuation.resume(BiometricResult.Success)
                    }
                }
                
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (continuation.isActive) {
                        val result = when (errorCode) {
                            BiometricPrompt.ERROR_USER_CANCELED,
                            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> BiometricResult.UserCancelled
                            BiometricPrompt.ERROR_NO_BIOMETRICS -> BiometricResult.BiometricNotEnrolled
                            BiometricPrompt.ERROR_HW_NOT_PRESENT,
                            BiometricPrompt.ERROR_HW_UNAVAILABLE -> BiometricResult.BiometricNotAvailable
                            else -> BiometricResult.Error(errString.toString())
                        }
                        continuation.resume(result)
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Don't resume here - let user try again
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .build()
        
        biometricPrompt.authenticate(promptInfo)
        
        continuation.invokeOnCancellation {
            biometricPrompt.cancelAuthentication()
        }
    }
    
    fun canUseBiometricAuthentication(): Boolean {
        return isBiometricAvailable() == BiometricResult.Success
    }
}