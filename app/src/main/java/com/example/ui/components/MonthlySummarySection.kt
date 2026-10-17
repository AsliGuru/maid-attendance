package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Maid
import com.example.data.model.MonthlySummary
import com.example.domain.SalaryCalculator
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FullDayGreen
import com.example.ui.theme.HalfDayAmber
import com.example.ui.theme.LeaveRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun MonthlySummarySection(
    currentMonth: YearMonth,
    selectedMaid: Maid?,
    selectedMaidSummary: MonthlySummary,
    allMaidsSummary: MonthlySummary,
    isAllMaidsMode: Boolean,
    onToggleMode: (Boolean) -> Unit,
    onFinalizeSalary: () -> Unit,
    onRecalculateSalary: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val activeSummary = if (isAllMaidsMode) allMaidsSummary else selectedMaidSummary
    var showRecalculateConfirmation by remember { mutableStateOf(false) }

    if (showRecalculateConfirmation) {
        AlertDialog(
            onDismissRequest = { showRecalculateConfirmation = false },
            title = {
                Text(
                    text = "Recalculate Finalized Month?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "This month's salary was locked as a finalized historical snapshot. Recalculating will update it using the maid's current salary (${SalaryCalculator.formatRupee(selectedMaid?.monthlySalary ?: 0.0)}) and free leaves (${selectedMaid?.freeLeavesPerMonth ?: 0} days). Do you wish to continue?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRecalculateConfirmation = false
                        onRecalculateSalary()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Recalculate")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecalculateConfirmation = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_summary_section"),
        shape = RoundedCornerShape(20.dp),
        color = DarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Summary & Salary",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = currentMonth.format(monthFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selector Tabs: Selected Maid vs All Maids
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF14181F))
                    .padding(4.dp)
            ) {
                val selectedMaidLabel = selectedMaid?.name ?: "Selected Maid"
                TabPill(
                    title = selectedMaidLabel,
                    isSelected = !isAllMaidsMode,
                    onClick = { onToggleMode(false) },
                    modifier = Modifier.weight(1f)
                )
                TabPill(
                    title = "All Maids Combined",
                    isSelected = isAllMaidsMode,
                    onClick = { onToggleMode(true) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attendance Metric Counters (4 tiles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryTile(
                    title = "Full Days",
                    value = activeSummary.totalFullDays.toString(),
                    subtext = "Present",
                    color = FullDayGreen,
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    title = "Half Days",
                    value = activeSummary.totalHalfDays.toString(),
                    subtext = "0.5 leave each",
                    color = HalfDayAmber,
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    title = "Full Leaves",
                    value = activeSummary.totalLeaves.toString(),
                    subtext = "1.0 leave each",
                    color = LeaveRed,
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    title = "Total Leaves",
                    value = SalaryCalculator.formatDays(activeSummary.totalLeaveDays),
                    subtext = "${activeSummary.freeLeavesAllowed} free allowed",
                    color = if (activeSummary.unpaidLeaveDays > 0) LeaveRed else FullDayGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Salary Calculation Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("salary_calculation_card"),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF131720),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF262D3B))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Header of salary card with status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED PAYABLE SALARY",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = SalaryCalculator.formatRupee(activeSummary.payableSalaryEstimate, includeDecimals = true),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = TextPrimary
                            )
                        }

                        if (activeSummary.isFinalized) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF1B2E24), RoundedCornerShape(8.dp))
                                    .border(1.dp, FullDayGreen.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = FullDayGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Finalized Snapshot",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 10.sp
                                        ),
                                        color = FullDayGreen
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(PrimaryAccent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .border(1.dp, PrimaryAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Live Estimate",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = PrimaryAccent
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Calculation formula breakdown
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0B0E13), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            SalaryRow(
                                label = if (isAllMaidsMode) "Total Monthly Base Salary" else "Monthly Base Salary",
                                value = SalaryCalculator.formatRupee(activeSummary.monthlySalary)
                            )
                            SalaryRow(
                                label = "Free Leaves Allowed",
                                value = "${activeSummary.freeLeavesAllowed} days"
                            )
                            SalaryRow(
                                label = "Total Leaves (Full + 0.5×Half)",
                                value = SalaryCalculator.formatDays(activeSummary.totalLeaveDays)
                            )
                            SalaryRow(
                                label = "Free Leaves Consumed",
                                value = SalaryCalculator.formatDays(activeSummary.freeLeavesConsumed)
                            )
                            SalaryRow(
                                label = "Unpaid Leave Days",
                                value = SalaryCalculator.formatDays(activeSummary.unpaidLeaveDays)
                            )
                            SalaryRow(
                                label = "Daily Deduction Rate (Salary ÷ 30)",
                                value = SalaryCalculator.formatRupee(activeSummary.dailyDeductionRate, includeDecimals = true) + "/day"
                            )
                            SalaryRow(
                                label = "Leave Deduction (Rate × Unpaid Days)",
                                value = if (activeSummary.leaveDeduction > 0) "- ${SalaryCalculator.formatRupee(activeSummary.leaveDeduction, includeDecimals = true)}" else "₹0.00"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Finalize / Recalculate Actions (only in single-maid mode)
                    if (!isAllMaidsMode && selectedMaid != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (activeSummary.isFinalized) {
                                OutlinedButton(
                                    onClick = { showRecalculateConfirmation = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Recalculate Settings", style = MaterialTheme.typography.labelMedium)
                                }
                            } else {
                                Button(
                                    onClick = onFinalizeSalary,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1E2838),
                                        contentColor = PrimaryAccent
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent.copy(alpha = 0.5f))
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Finalize Month Salary", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fixed 30-day divisor used. Free leaves reset monthly and do not carry forward.",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabPill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF222834) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) TextPrimary else TextSecondary
        )
    }
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF14181F),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = TextMuted
            )
        }
    }
}

@Composable
private fun SalaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = TextPrimary
        )
    }
}
