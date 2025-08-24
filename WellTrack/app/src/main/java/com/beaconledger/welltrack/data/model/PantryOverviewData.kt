package com.beaconledger.welltrack.data.model

/**
 * Data class representing pantry overview statistics
 */
data class PantryOverviewData(
    val totalItems: Int = 0,
    val lowStockCount: Int = 0,
    val expiringCount: Int = 0,
    val totalValue: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)