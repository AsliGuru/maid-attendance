package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "salary_snapshots",
    indices = [
        Index(value = ["maid_id", "month"], unique = true),
        Index(value = ["maid_id"]),
        Index(value = ["month"])
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
data class SalarySnapshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "maid_id")
    val maidId: Long,
    @ColumnInfo(name = "month")
    val month: String, // Format "YYYY-MM"
    @ColumnInfo(name = "monthly_salary")
    val monthlySalary: Double,
    @ColumnInfo(name = "free_leaves_per_month")
    val freeLeavesPerMonth: Int,
    @ColumnInfo(name = "daily_deduction_rate")
    val dailyDeductionRate: Double,
    @ColumnInfo(name = "total_leave_days")
    val totalLeaveDays: Double,
    @ColumnInfo(name = "free_leaves_consumed")
    val freeLeavesConsumed: Double,
    @ColumnInfo(name = "unpaid_leave_days")
    val unpaidLeaveDays: Double,
    @ColumnInfo(name = "leave_deduction")
    val leaveDeduction: Double,
    @ColumnInfo(name = "estimated_payable_salary")
    val estimatedPayableSalary: Double,
    @ColumnInfo(name = "is_finalized")
    val isFinalized: Boolean = true,
    @ColumnInfo(name = "finalized_at")
    val finalizedAt: Long = System.currentTimeMillis()
)
