package com.example

import com.example.domain.SalaryCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SalaryCalculatorTest {

    @Test
    fun testPromptExampleCalculation() {
        // Example from prompt:
        // Monthly salary: ₹5,000
        // Free leaves per month: 3 days
        // Full-day leaves: 4
        // Half-day leaves: 2
        // Total leave: 4 + (0.5 × 2) = 5 days
        // Free leave days consumed: 3 days
        // Unpaid leave: 5 − 3 = 2 days
        // Daily deduction rate: ₹5,000 ÷ 30 = ₹166.666...
        // Leave deduction: approximately ₹333.33
        // Estimated payable salary: approximately ₹4,666.67
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 5000.0,
            freeLeavesPerMonth = 3,
            fullDayLeaves = 4,
            halfDayLeaves = 2
        )

        assertEquals(BigDecimal("5.0"), result.totalLeaveDays)
        assertEquals(BigDecimal("3.0"), result.freeLeavesConsumed)
        assertEquals(BigDecimal("2.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("333.33"), result.leaveDeduction)
        assertEquals(BigDecimal("4666.67"), result.estimatedPayableSalary)
    }

    @Test
    fun testNoLeavesNoDeduction() {
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 6000.0,
            freeLeavesPerMonth = 2,
            fullDayLeaves = 0,
            halfDayLeaves = 0
        )
        assertEquals(BigDecimal("0.0"), result.totalLeaveDays)
        assertEquals(BigDecimal("0.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("0.00"), result.leaveDeduction)
        assertEquals(BigDecimal("6000.00"), result.estimatedPayableSalary)
    }

    @Test
    fun testLeavesWithinFreeAllowanceNoDeduction() {
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 4500.0,
            freeLeavesPerMonth = 3,
            fullDayLeaves = 2,
            halfDayLeaves = 2 // 2 + 1 = 3 days total leave
        )
        assertEquals(BigDecimal("3.0"), result.totalLeaveDays)
        assertEquals(BigDecimal("3.0"), result.freeLeavesConsumed)
        assertEquals(BigDecimal("0.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("0.00"), result.leaveDeduction)
        assertEquals(BigDecimal("4500.00"), result.estimatedPayableSalary)
    }

    @Test
    fun testLeavesExceedingFreeAllowanceDeductsOnlyExcess() {
        // 3 full leaves + 0 half leaves = 3 total. Free = 1. Unpaid = 2.
        // Salary = 3000. Daily rate = 100. Deduction = 200. Payable = 2800.
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 3000.0,
            freeLeavesPerMonth = 1,
            fullDayLeaves = 3,
            halfDayLeaves = 0
        )
        assertEquals(BigDecimal("3.0"), result.totalLeaveDays)
        assertEquals(BigDecimal("1.0"), result.freeLeavesConsumed)
        assertEquals(BigDecimal("2.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("200.00"), result.leaveDeduction)
        assertEquals(BigDecimal("2800.00"), result.estimatedPayableSalary)
    }

    @Test
    fun testHalfDayLeavesConsumeHalfFreeLeaveDay() {
        // 1 half day = 0.5 leave. Free = 2. Consumed = 0.5. Unpaid = 0.
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 6000.0,
            freeLeavesPerMonth = 2,
            fullDayLeaves = 0,
            halfDayLeaves = 1
        )
        assertEquals(BigDecimal("0.5"), result.totalLeaveDays)
        assertEquals(BigDecimal("0.5"), result.freeLeavesConsumed)
        assertEquals(BigDecimal("0.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("0.00"), result.leaveDeduction)
    }

    @Test
    fun testFreeLeavesSetToZero() {
        // Free = 0. Total leaves = 2. Unpaid = 2.
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 3000.0,
            freeLeavesPerMonth = 0,
            fullDayLeaves = 2,
            halfDayLeaves = 0
        )
        assertEquals(BigDecimal("2.0"), result.totalLeaveDays)
        assertEquals(BigDecimal("0.0"), result.freeLeavesConsumed)
        assertEquals(BigDecimal("2.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("200.00"), result.leaveDeduction)
        assertEquals(BigDecimal("2800.00"), result.estimatedPayableSalary)
    }

    @Test
    fun testFreeLeavesSetToThirty() {
        // Free = 30. Total leaves = 15. Unpaid = 0.
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 5000.0,
            freeLeavesPerMonth = 30,
            fullDayLeaves = 10,
            halfDayLeaves = 10 // 10 + 5 = 15
        )
        assertEquals(BigDecimal("15.0"), result.totalLeaveDays)
        assertEquals(BigDecimal("15.0"), result.freeLeavesConsumed)
        assertEquals(BigDecimal("0.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("0.00"), result.leaveDeduction)
        assertEquals(BigDecimal("5000.00"), result.estimatedPayableSalary)
    }

    @Test
    fun testZeroSalary() {
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 0.0,
            freeLeavesPerMonth = 2,
            fullDayLeaves = 5,
            halfDayLeaves = 0
        )
        assertEquals(BigDecimal("0.00"), result.dailyDeductionRate.setScale(2, java.math.RoundingMode.HALF_UP))
        assertEquals(BigDecimal("0.00"), result.leaveDeduction)
        assertEquals(BigDecimal("0.00"), result.estimatedPayableSalary)
    }

    @Test
    fun testAllThirtyDaysRecordedAsLeave() {
        // Salary = 6000. Daily = 200. Free = 0. Unpaid = 30.
        // Deduction = 6000. Payable = 0.
        val result = SalaryCalculator.computeSalary(
            monthlySalary = 6000.0,
            freeLeavesPerMonth = 0,
            fullDayLeaves = 30,
            halfDayLeaves = 0
        )
        assertEquals(BigDecimal("30.0"), result.totalLeaveDays)
        assertEquals(BigDecimal("30.0"), result.unpaidLeaveDays)
        assertEquals(BigDecimal("6000.00"), result.leaveDeduction)
        assertEquals(BigDecimal("0.00"), result.estimatedPayableSalary)
    }

    @Test
    fun testFractionalUnpaidLeaveTotals() {
        // Free = 1. Half day leaves = 3 (1.5 days). Unpaid = 0.5 days.
        // Salary = 3000. Daily rate = 100. Deduction = 50. Payable = 2950.
        val resHalfDay = SalaryCalculator.computeSalary(
            monthlySalary = 3000.0,
            freeLeavesPerMonth = 1,
            fullDayLeaves = 0,
            halfDayLeaves = 3
        )
        assertEquals(BigDecimal("1.5"), resHalfDay.totalLeaveDays)
        assertEquals(BigDecimal("1.0"), resHalfDay.freeLeavesConsumed)
        assertEquals(BigDecimal("0.5"), resHalfDay.unpaidLeaveDays)
        assertEquals(BigDecimal("50.00"), resHalfDay.leaveDeduction)
        assertEquals(BigDecimal("2950.00"), resHalfDay.estimatedPayableSalary)

        // Free = 0. Full = 1, Half = 3 -> Total = 2.5 days. Unpaid = 2.5 days.
        // Daily rate = 100. Deduction = 250. Payable = 2750.
        val resTwoAndHalf = SalaryCalculator.computeSalary(
            monthlySalary = 3000.0,
            freeLeavesPerMonth = 0,
            fullDayLeaves = 1,
            halfDayLeaves = 3
        )
        assertEquals(BigDecimal("2.5"), resTwoAndHalf.totalLeaveDays)
        assertEquals(BigDecimal("2.5"), resTwoAndHalf.unpaidLeaveDays)
        assertEquals(BigDecimal("250.00"), resTwoAndHalf.leaveDeduction)
        assertEquals(BigDecimal("2750.00"), resTwoAndHalf.estimatedPayableSalary)
    }

    @Test
    fun testValidationOfSalaryAndFreeLeaves() {
        assertTrue(SalaryCalculator.validateSalaryAndFreeLeaves(salary = 5000.0, freeLeaves = 2).isValid)
        assertTrue(SalaryCalculator.validateSalaryAndFreeLeaves(salary = 0.0, freeLeaves = 0).isValid)
        assertTrue(SalaryCalculator.validateSalaryAndFreeLeaves(salary = 100000.0, freeLeaves = 30).isValid)

        // Negative salary
        assertFalse(SalaryCalculator.validateSalaryAndFreeLeaves(salary = -10.0, freeLeaves = 2).isValid)

        // Free leaves < 0
        assertFalse(SalaryCalculator.validateSalaryAndFreeLeaves(salary = 5000.0, freeLeaves = -1).isValid)

        // Free leaves > 30
        assertFalse(SalaryCalculator.validateSalaryAndFreeLeaves(salary = 5000.0, freeLeaves = 31).isValid)
    }

    @Test
    fun testFormattingHelpers() {
        val rupee = SalaryCalculator.formatRupee(4666.67, includeDecimals = true)
        assertTrue(rupee.contains("4,666.67") || rupee.contains("4666.67"))

        assertEquals("2 days", SalaryCalculator.formatDays(2.0))
        assertEquals("1 day", SalaryCalculator.formatDays(1.0))
        assertEquals("2.5 days", SalaryCalculator.formatDays(2.5))
        assertEquals("0.5 days", SalaryCalculator.formatDays(0.5))
    }
}
