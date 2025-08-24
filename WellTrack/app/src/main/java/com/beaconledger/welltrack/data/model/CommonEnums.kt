package com.beaconledger.welltrack.data.model

enum class TrendDirection {
    INCREASING,
    DECREASING,
    STABLE,
    IMPROVING,
    DECLINING,
    INSUFFICIENT_DATA
}

enum class AlertType(val displayName: String) {
    // Pantry alerts
    EXPIRY_WARNING("Expiry Warning"),
    EXPIRED("Expired"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock"),
    
    // Budget alerts
    BUDGET_WARNING("Budget Warning"),
    BUDGET_EXCEEDED("Budget Exceeded"),
    WEEKLY_SUMMARY("Weekly Summary"),
    MONTHLY_SUMMARY("Monthly Summary"),
    COST_SPIKE_DETECTED("Cost Spike Detected")
}

enum class FitnessGoal {
    WEIGHT_LOSS, 
    MUSCLE_GAIN, 
    MAINTENANCE, 
    ENDURANCE, 
    STRENGTH, 
    VO2_MAX_IMPROVEMENT
}

