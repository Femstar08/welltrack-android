package com.beaconledger.welltrack.domain.repository

import java.time.LocalDateTime

/**
 * Preview of data to be imported
 */
data class ImportPreview(
    val fileName: String,
    val fileSize: Long,
    val dataType: String,
    val recordCount: Int,
    val dateRange: Pair<LocalDateTime?, LocalDateTime?>,
    val conflicts: List<ImportConflict> = emptyList(),
    val warnings: List<String> = emptyList(),
    val isValid: Boolean = true
)

/**
 * Represents a conflict during import
 */
data class ImportConflict(
    val type: ConflictType,
    val existingData: String,
    val importData: String,
    val recommendation: String
)

enum class ConflictType {
    DUPLICATE_RECORD,
    VALUE_MISMATCH,
    DATE_OVERLAP,
    INVALID_FORMAT
}