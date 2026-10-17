package com.example.data.model

data class DailyMaidAttendance(
    val maid: Maid,
    val status: AttendanceStatus? = null,
    val recordId: Long? = null,
    val notes: String? = null
)
