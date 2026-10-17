package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AttendanceStatus

class Converters {
    @TypeConverter
    fun fromAttendanceStatus(status: AttendanceStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun toAttendanceStatus(value: String?): AttendanceStatus? {
        return value?.let { AttendanceStatus.fromString(it) }
    }
}
