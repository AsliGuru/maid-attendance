package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["maid_id", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["maid_id"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Maid::class,
            parentColumns = ["id"],
            childColumns = ["maid_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AttendanceRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "maid_id")
    val maidId: Long,
    @ColumnInfo(name = "date")
    val date: String, // Format: YYYY-MM-DD
    @ColumnInfo(name = "status")
    val status: AttendanceStatus,
    @ColumnInfo(name = "notes")
    val notes: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
