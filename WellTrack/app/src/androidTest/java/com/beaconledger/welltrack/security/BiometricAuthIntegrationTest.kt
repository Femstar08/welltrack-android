package com.beaconledger.welltrack.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.data.security.BiometricAuthManager
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class BiometricAuthIntegrationTest {
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var biometricAuthManager: BiometricAuthManager
    
    private lateinit var context: Context
    
    @Before
    fun setup() {
        hiltRule.inject()
        context = ApplicationProvider.getApplicationContext()
    }
    
    @Test
    fun testBiometricManagerInitialization() {
        assertNotNull(biometricAuthManager)
    }
    
    @Test
    fun testBiometricAvailabilityCheck() = runTest {
        val result = biometricAuthManager.isBiometricAvailable()
        
        // The result should be one of the expected BiometricResult types
        assertTrue(
            result is BiometricAuthManager.BiometricResult.Success ||
            result is BiometricAuthManager.BiometricResult.BiometricNotAvailable ||
            result is BiometricAuthManager.BiometricResult.BiometricNotEnrolled ||
            result is BiometricAuthManager.BiometricResult.Error
        )
    }
    
    @Test
    fun testBiometricManagerCompatibility() {
        val biometricManager = BiometricManager.from(context)
        val canAuthenticate = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        
        // Verify that our BiometricAuthManager result aligns with system BiometricManager
        val ourResult = biometricAuthManager.isBiometricAvailable()
        
        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                assertTrue(ourResult is BiometricAuthManager.BiometricResult.Success)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                assertTrue(ourResult is BiometricAuthManager.BiometricResult.BiometricNotAvailable)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                assertTrue(ourResult is BiometricAuthManager.BiometricResult.BiometricNotEnrolled)
            }
            else -> {
                assertTrue(
                    ourResult is BiometricAuthManager.BiometricResult.Error ||
                    ourResult is BiometricAuthManager.BiometricResult.BiometricNotAvailable
                )
            }
        }
    }
    
    @Test
    fun testCanUseBiometricAuthentication() {
        val canUse = biometricAuthManager.canUseBiometricAuthentication()
        val availability = biometricAuthManager.isBiometricAvailable()
        
        // canUseBiometricAuthentication should return true only if biometric is available
        if (availability is BiometricAuthManager.BiometricResult.Success) {
            assertTrue(canUse)
        } else {
            assertTrue(!canUse)
        }
    }
}