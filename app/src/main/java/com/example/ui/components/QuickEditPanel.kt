package com.example.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.data.model.AttendanceStatus
import com.example.data.model.Maid
import com.example.domain.SalaryCalculator
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.FullDayBorder
import com.example.ui.theme.FullDayContainer
import com.example.ui.theme.FullDayGreen
import com.example.ui.theme.HalfDayBorder
import com.example.ui.theme.HalfDayContainer
import com.example.ui.theme.HalfDayAmber
import com.example.ui.theme.LeaveBorder
import com.example.ui.theme.LeaveContainer
import com.example.ui.theme.LeaveRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.QuickEditPanelBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditPanel(
    selectedMaid: Maid?,
    allMaids: List<Maid>,
    selectedDate: LocalDate,
    selectedStatus: AttendanceStatus?,
    onSelectMaid: (Maid) -> Unit,
    onStatusSelected: (AttendanceStatus) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onSaveAttendance: () -> Unit,
    onClearAttendance: () -> Unit,
    onEditMaidProfile: (Maid) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showMaidDropdown by remember { mutableStateOf(false) }
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("EEE, dd MMM yyyy") }

    if (showDatePicker) {
        val initialEpochMillis = remember(selectedDate) {
            selectedDate.atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialEpochMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val newDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.of("UTC"))
                                .toLocalDate()
                            onDateSelected(newDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quick_edit_panel"),
        shape = RoundedCornerShape(20.dp),
        color = QuickEditPanelBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title & Selected Status Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Maid Attendance",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextPrimary
                )

                if (selectedStatus != null) {
                    StatusPill(status = selectedStatus)
                } else {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E2229), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Unmarked",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Maid Profile Box with Dropdown Switcher
            if (selectedMaid != null) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF14181F))
                            .clickable { showMaidDropdown = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar / Initials
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF222733)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = selectedMaid.name.take(2).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PrimaryAccent
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = selectedMaid.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TextPrimary
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Switch Maid",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "${selectedMaid.role} • ${SalaryCalculator.formatRupee(selectedMaid.monthlySalary)}/mo • ${selectedMaid.freeLeavesPerMonth} free leaves",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }

                        IconButton(
                            onClick = { onEditMaidProfile(selectedMaid) },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("edit_maid_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Maid Profile",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Dropdown menu to switch maids directly
                    DropdownMenu(
                        expanded = showMaidDropdown,
                        onDismissRequest = { showMaidDropdown = false }
                    ) {
                        allMaids.forEach { maid ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = maid.name,
                                            fontWeight = if (maid.id == selectedMaid.id) FontWeight.Bold else FontWeight.Normal,
                                            color = if (maid.id == selectedMaid.id) PrimaryAccent else TextPrimary
                                        )
                                        Text(
                                            text = "${SalaryCalculator.formatRupee(maid.monthlySalary)} • ${maid.freeLeavesPerMonth} free leaves",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectMaid(maid)
                                    showMaidDropdown = false
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No maid registered. Tap 'Add Maid' above to begin.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Selection Pill
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF14181F))
                    .clickable { showDatePicker = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Selected Date",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = selectedDate.format(displayDateFormatter),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = TextPrimary
                    )
                }

                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = PrimaryAccent
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Three Status Buttons: Full Day, Half Day, Leave
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusButton(
                    status = AttendanceStatus.FULL_DAY,
                    subtext = "Present",
                    isSelected = selectedStatus == AttendanceStatus.FULL_DAY,
                    activeColor = FullDayGreen,
                    activeBg = FullDayContainer,
                    activeBorder = FullDayBorder,
                    onClick = { onStatusSelected(AttendanceStatus.FULL_DAY) },
                    modifier = Modifier.weight(1f)
                )

                StatusButton(
                    status = AttendanceStatus.HALF_DAY,
                    subtext = "0.5 leave",
                    isSelected = selectedStatus == AttendanceStatus.HALF_DAY,
                    activeColor = HalfDayAmber,
                    activeBg = HalfDayContainer,
                    activeBorder = HalfDayBorder,
                    onClick = { onStatusSelected(AttendanceStatus.HALF_DAY) },
                    modifier = Modifier.weight(1f)
                )

                StatusButton(
                    status = AttendanceStatus.LEAVE,
                    subtext = "1.0 leave",
                    isSelected = selectedStatus == AttendanceStatus.LEAVE,
                    activeColor = LeaveRed,
                    activeBg = LeaveContainer,
                    activeBorder = LeaveBorder,
                    onClick = { onStatusSelected(AttendanceStatus.LEAVE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Save Attendance and Clear Attendance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onSaveAttendance,
                    enabled = selectedMaid != null && selectedStatus != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(46.dp)
                        .testTag("save_attendance_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Save Attendance",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                OutlinedButton(
                    onClick = onClearAttendance,
                    enabled = selectedMaid != null && selectedStatus != null,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextSecondary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .testTag("clear_attendance_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusButton(
    status: AttendanceStatus,
    subtext: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    activeBorder: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) activeBg else Color(0xFF14181F),
        label = "bgColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) activeBorder else DarkOutline,
        label = "borderColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else TextSecondary,
        label = "textColor"
    )

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("status_button_${status.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = textColor
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (isSelected) textColor.copy(alpha = 0.8f) else TextMuted
            )
        }
    }
}

@Composable
fun StatusPill(
    status: AttendanceStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status) {
        AttendanceStatus.FULL_DAY -> Triple(FullDayContainer, FullDayGreen, "Full Day (Present)")
        AttendanceStatus.HALF_DAY -> Triple(HalfDayContainer, HalfDayAmber, "Half Day (0.5 Leave)")
        AttendanceStatus.LEAVE -> Triple(LeaveContainer, LeaveRed, "Leave (1.0 Leave)")
    }

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(8.dp))
            .border(1.dp, textColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = textColor
        )
    }
}
