package com.example.domain

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.Locale

object SalaryCalculator {

    private val DIVISOR_30 = BigDecimal("30")
    private val HALF = BigDecimal("0.5")

    data class CalculationResult(
        val monthlySalary: BigDecimal,
        val freeLeavesAllowed: Int,
        val fullDayLeaves: Int,
        val halfDayLeaves: Int,
        val totalLeaveDays: BigDecimal,
        val freeLeavesConsumed: BigDecimal,
        val unpaidLeaveDays: BigDecimal,
        val dailyDeductionRate: BigDecimal,
        val leaveDeduction: BigDecimal,
        val estimatedPayableSalary: BigDecimal
    )

    /**
     * Total leave days = Full-Day Leaves + (0.5 × Half-Day Leaves)
     */
    fun calculateTotalLeaveDays(fullDayLeaves: Int, halfDayLeaves: Int): BigDecimal {
        val fullBD = BigDecimal.valueOf(fullDayLeaves.coerceAtLeast(0).toLong())
        val halfBD = BigDecimal.valueOf(halfDayLeaves.coerceAtLeast(0).toLong()).multiply(HALF)
        return fullBD.add(halfBD).setScale(1, RoundingMode.HALF_UP)
    }

    /**
     * Daily deduction rate = Monthly Salary ÷ 30
     * Preserves high precision (10 decimals) to avoid premature rounding errors.
     */
    fun calculateDailyDeductionRate(monthlySalary: BigDecimal): BigDecimal {
        if (monthlySalary <= BigDecimal.ZERO) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
        return monthlySalary.divide(DIVISOR_30, 10, RoundingMode.HALF_UP)
    }

    /**
     * Free leave days consumed = min(Total Leave Days, Free Leaves Per Month)
     */
    fun calculateFreeLeavesConsumed(totalLeaveDays: BigDecimal, freeLeavesAllowed: Int): BigDecimal {
        val allowedBD = BigDecimal.valueOf(freeLeavesAllowed.coerceAtLeast(0).toLong()).setScale(1, RoundingMode.HALF_UP)
        return totalLeaveDays.min(allowedBD).max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)
    }

    /**
     * Unpaid leave days = max(0, Total Leave Days − Free Leaves Per Month)
     */
    fun calculateUnpaidLeaveDays(totalLeaveDays: BigDecimal, freeLeavesAllowed: Int): BigDecimal {
        val allowedBD = BigDecimal.valueOf(freeLeavesAllowed.coerceAtLeast(0).toLong()).setScale(1, RoundingMode.HALF_UP)
        return totalLeaveDays.subtract(allowedBD).max(BigDecimal.ZERO).setScale(1, RoundingMode.HALF_UP)
    }

    /**
     * Complete salary calculation adhering to the revised formula:
     * - Divisor of 30 for every maid.
     * - Leave deduction = Daily deduction rate × Unpaid Leave Days.
     * - Estimated payable salary = max(0, Monthly Salary − Leave Deduction).
     */
    fun computeSalary(
        monthlySalary: Double,
        freeLeavesPerMonth: Int,
        fullDayLeaves: Int,
        halfDayLeaves: Int
    ): CalculationResult {
        val salaryBD = BigDecimal.valueOf(monthlySalary.coerceAtLeast(0.0)).setScale(2, RoundingMode.HALF_UP)
        val validFreeLeaves = freeLeavesPerMonth.coerceIn(0, 30)

        val totalLeaveDays = calculateTotalLeaveDays(fullDayLeaves, halfDayLeaves)
        val freeLeavesConsumed = calculateFreeLeavesConsumed(totalLeaveDays, validFreeLeaves)
        val unpaidLeaveDays = calculateUnpaidLeaveDays(totalLeaveDays, validFreeLeaves)

        val dailyRate = calculateDailyDeductionRate(salaryBD)
        val leaveDeduction = dailyRate.multiply(unpaidLeaveDays).setScale(2, RoundingMode.HALF_UP)
        val payableSalary = salaryBD.subtract(leaveDeduction).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

        return CalculationResult(
            monthlySalary = salaryBD,
            freeLeavesAllowed = validFreeLeaves,
            fullDayLeaves = fullDayLeaves,
            halfDayLeaves = halfDayLeaves,
            totalLeaveDays = totalLeaveDays,
            freeLeavesConsumed = freeLeavesConsumed,
            unpaidLeaveDays = unpaidLeaveDays,
            dailyDeductionRate = dailyRate,
            leaveDeduction = leaveDeduction,
            estimatedPayableSalary = payableSalary
        )
    }

    /**
     * Validates monthly salary and free leaves.
     * Salary must be non-negative.
     * Free leaves must be a whole number between 0 and 30 inclusive.
     */
    fun validateSalaryAndFreeLeaves(salary: Double, freeLeaves: Int): ValidationResult {
        if (salary < 0.0) {
            return ValidationResult(isValid = false, errorMessage = "Monthly salary cannot be negative.")
        }
        if (freeLeaves < 0 || freeLeaves > 30) {
            return ValidationResult(isValid = false, errorMessage = "Free leaves must be between 0 and 30 days.")
        }
        return ValidationResult(isValid = true, errorMessage = null)
    }

    /**
     * Formats an amount using Indian Rupee currency format (e.g., ₹5,000 or ₹4,666.67).
     */
    fun formatRupee(amount: Double, includeDecimals: Boolean = false): String {
        return formatRupee(BigDecimal.valueOf(amount), includeDecimals)
    }

    fun formatRupee(amount: BigDecimal, includeDecimals: Boolean = false): String {
        return try {
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            if (!includeDecimals) {
                formatter.maximumFractionDigits = 0
                formatter.minimumFractionDigits = 0
            } else {
                formatter.maximumFractionDigits = 2
                formatter.minimumFractionDigits = 2
            }
            formatter.format(amount)
        } catch (_: Exception) {
            val decFormat = if (includeDecimals) DecimalFormat("#,##,##0.00") else DecimalFormat("#,##,##0")
            "₹" + decFormat.format(amount)
        }
    }

    /**
     * Formats fractional leave days cleanly, e.g. "2 days", "2.5 days", "0.5 days".
     */
    fun formatDays(days: Double): String {
        return if (days % 1.0 == 0.0) {
            "${days.toInt()} ${if (days == 1.0) "day" else "days"}"
        } else {
            String.format(Locale.US, "%.1f days", days)
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String?
    )
}
