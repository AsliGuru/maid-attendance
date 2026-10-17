package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maids")
data class Maid(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "role")
    val role: String = "General Housekeeping",
    @ColumnInfo(name = "monthly_salary")
    val monthlySalary: Double = 5000.0,
    @ColumnInfo(name = "free_leaves_per_month", defaultValue = "2")
    val freeLeavesPerMonth: Int = 2,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
