package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Maid
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.FullDayGreen
import com.example.ui.theme.HalfDayAmber
import com.example.ui.theme.LeaveRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AttendanceCalendar(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    selectedMaid: Maid?,
    monthRecords: List<AttendanceRecord>,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToToday: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitleFormatter = remember { DateTimeFormatter.ofPattern("MMMM yyyy") }
    val today = remember { LocalDate.now() }

    // Map date string -> list of records
    val recordsByDate = remember(monthRecords) {
        monthRecords.groupBy { it.date }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("attendance_calendar"),
        shape = RoundedCornerShape(20.dp),
        color = DarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Calendar Header: Month/Year & Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = currentMonth.format(monthTitleFormatter),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    if (selectedMaid != null) {
                        Text(
                            text = "Showing attendance for ${selectedMaid.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onJumpToToday,
                        modifier = Modifier.testTag("calendar_today_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Today,
                            contentDescription = "Today",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Today",
                            style = MaterialTheme.typography.labelMedium,
                            color = PrimaryAccent
                        )
                    }

                    IconButton(
                        onClick = onPreviousMonth,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("calendar_prev_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Month",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onNextMonth,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("calendar_next_month")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Month",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Weekday Headers (Mon - Sun)
            val daysOfWeek = remember {
                listOf(
                    DayOfWeek.MONDAY,
                    DayOfWeek.TUESDAY,
                    DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY,
                    DayOfWeek.FRIDAY,
                    DayOfWeek.SATURDAY,
                    DayOfWeek.SUNDAY
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { dayOfWeek ->
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        ),
                        color = if (dayOfWeek == DayOfWeek.SUNDAY) LeaveRed.copy(alpha = 0.8f) else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid Days
            val firstDayOfMonth = currentMonth.atDay(1)
            val daysInMonth = currentMonth.lengthOfMonth()
            // DayOfWeek: 1 = Monday, 7 = Sunday
            val leadEmptyCells = firstDayOfMonth.dayOfWeek.value - 1
            val totalCells = leadEmptyCells + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (rowIndex in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (colIndex in 0 until 7) {
                            val cellIndex = rowIndex * 7 + colIndex
                            val dayNumber = cellIndex - leadEmptyCells + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDate = currentMonth.atDay(dayNumber)
                                val dateStr = cellDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val cellRecords = recordsByDate[dateStr] ?: emptyList()
                                val maidRecord = selectedMaid?.let { maid ->
                                    cellRecords.find { it.maidId == maid.id }
                                }
                                val isSelected = cellDate == selectedDate
                                val isToday = cellDate == today

                                CalendarDayCell(
                                    date = cellDate,
                                    dayNumber = dayNumber,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    maidRecord = maidRecord,
                                    totalRecordedForDate = cellRecords.size,
                                    onClick = { onDateSelected(cellDate) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = FullDayGreen, label = "Full Day (1)")
                Spacer(modifier = Modifier.width(14.dp))
                LegendItem(color = HalfDayAmber, label = "Half Day (0.5)")
                Spacer(modifier = Modifier.width(14.dp))
                LegendItem(color = LeaveRed, label = "Leave (0)")
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    date: LocalDate,
    dayNumber: Int,
    isSelected: Boolean,
    isToday: Boolean,
    maidRecord: AttendanceRecord?,
    totalRecordedForDate: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> PrimaryAccent.copy(alpha = 0.2f)
        isToday -> Color(0xFF202633)
        else -> Color.Transparent
    }

    val borderColor = when {
        isSelected -> PrimaryAccent
        isToday -> TextSecondary.copy(alpha = 0.5f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(if (isSelected || isToday) 1.5.dp else 0.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("calendar_day_${date}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                ),
                color = when {
                    isSelected -> PrimaryAccent
                    isToday -> TextPrimary
                    else -> TextPrimary.copy(alpha = 0.9f)
                }
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Attendance status indicator dot
            if (maidRecord != null) {
                val dotColor = when (maidRecord.status) {
                    AttendanceStatus.FULL_DAY -> FullDayGreen
                    AttendanceStatus.HALF_DAY -> HalfDayAmber
                    AttendanceStatus.LEAVE -> LeaveRed
                }
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dotColor)
                )
            } else if (totalRecordedForDate > 0) {
                // Someone has attendance on this day even if current maid doesn't
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(TextMuted)
                )
            } else {
                Spacer(modifier = Modifier.size(6.dp))
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextSecondary
        )
    }
}
