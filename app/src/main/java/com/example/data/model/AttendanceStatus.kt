package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class AttendanceStatus(
    val displayName: String,
    val units: Double,
    val shortCode: String
) {
    FULL_DAY("Full Day", 1.0, "F"),
    HALF_DAY("Half Day", 0.5, "H"),
    LEAVE("Leave", 0.0, "L");

    companion object {
        fun fromString(value: String?): AttendanceStatus? {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}
