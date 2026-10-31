package com.example.warrantly.data.model

enum class WarrantyStatus(val displayName: String) {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired"),
    UNKNOWN("Unknown")
}
