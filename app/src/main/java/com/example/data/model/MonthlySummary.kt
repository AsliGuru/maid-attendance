package com.example.data.model

data class MonthlySummary(
    val monthlySalary: Double = 0.0,
    val freeLeavesAllowed: Int = 2,
    val totalFullDays: Int = 0,
    val totalHalfDays: Int = 0,
    val totalLeaves: Int = 0,
    val totalLeaveDays: Double = 0.0,
    val freeLeavesConsumed: Double = 0.0,
    val unpaidLeaveDays: Double = 0.0,
    val dailyDeductionRate: Double = 0.0,
    val leaveDeduction: Double = 0.0,
    val payableSalaryEstimate: Double = 0.0,
    val isFinalized: Boolean = false,
    val finalizedAt: Long? = null
)
