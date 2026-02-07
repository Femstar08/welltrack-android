package com.beaconledger.welltrack.domain.repository

/**
 * Methods for sharing exported data
 */
enum class ShareMethod(val displayName: String) {
    EMAIL("Email"),
    CLOUD_STORAGE("Cloud Storage"),
    FILE_SHARE("File Share"),
    SECURE_LINK("Secure Link"),
    BLUETOOTH("Bluetooth"),
    USB_TRANSFER("USB Transfer"),
    HEALTHCARE_PROVIDER("Healthcare Provider Portal")
}

/**
 * Configuration for sharing data exports
 */
data class ShareConfig(
    val method: ShareMethod,
    val recipient: String? = null,
    val expirationHours: Int? = null,
    val passwordProtected: Boolean = false,
    val encryptionEnabled: Boolean = true,
    val notifyOnAccess: Boolean = false
)