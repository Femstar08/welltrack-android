package com.beaconledger.welltrack.data.model

/**
 * Sync statistics
 */
data class SyncStats(
    val pendingUpload: Int,
    val pendingDownload: Int,
    val conflicts: Int,
    val failed: Int,
    val synced: Int
)