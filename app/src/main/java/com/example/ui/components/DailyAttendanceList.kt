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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.DailyMaidAttendance
import com.example.data.model.Maid
import com.example.ui.DateSummary
import com.example.ui.theme.DarkCard
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DailyAttendanceList(
    selectedDate: LocalDate,
    dailyList: List<DailyMaidAttendance>,
    selectedMaid: Maid?,
    dateSummary: DateSummary,
    onMaidSelected: (Maid) -> Unit,
    onStatusToggle: (Long, AttendanceStatus) -> Unit,
    onClearAttendance: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy")

    Column(modifier = modifier.fillMaxWidth()) {
        // Daily Metrics Summary Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DailyMetricCard(
                title = "Full Day",
                count = dateSummary.fullDaysCount,
                color = FullDayGreen,
                bgColor = FullDayContainer,
                borderColor = FullDayBorder,
                modifier = Modifier.weight(1f)
            )

            DailyMetricCard(
                title = "Half Day",
                count = dateSummary.halfDaysCount,
                color = HalfDayAmber,
                bgColor = HalfDayContainer,
                borderColor = HalfDayBorder,
                modifier = Modifier.weight(1f)
            )

            DailyMetricCard(
                title = "Leave",
                count = dateSummary.leaveCount,
                color = LeaveRed,
                bgColor = LeaveContainer,
                borderColor = LeaveBorder,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("daily_attendance_card"),
            shape = RoundedCornerShape(20.dp),
            color = DarkCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Attendance",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = selectedDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (dailyList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No maids added yet. Tap 'Manage Maids' to add maids.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        dailyList.forEach { item ->
                            val isSelectedMaid = selectedMaid?.id == item.maid.id
                            DailyMaidRow(
                                item = item,
                                isSelected = isSelectedMaid,
                                onSelect = { onMaidSelected(item.maid) },
                                onStatusToggle = { status -> onStatusToggle(item.maid.id, status) },
                                onClear = { onClearAttendance(item.maid.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyMetricCard(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    borderColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                ),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun DailyMaidRow(
    item: DailyMaidAttendance,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onStatusToggle: (AttendanceStatus) -> Unit,
    onClear: () -> Unit
) {
    val rowBorder = if (isSelected) PrimaryAccent.copy(alpha = 0.8f) else DarkOutline

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .testTag("maid_row_${item.maid.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) Color(0xFF1E2530) else Color(0xFF15181F),
        border = androidx.compose.foundation.BorderStroke(1.dp, rowBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Maid Avatar / Initial
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PrimaryAccent.copy(alpha = 0.2f) else Color(0xFF232833)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.maid.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) PrimaryAccent else TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Name and role
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.maid.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = item.maid.role,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Quick Status Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionChip(
                    label = "Full",
                    isSelected = item.status == AttendanceStatus.FULL_DAY,
                    color = FullDayGreen,
                    bgColor = FullDayContainer,
                    onClick = { onStatusToggle(AttendanceStatus.FULL_DAY) }
                )

                QuickActionChip(
                    label = "Half",
                    isSelected = item.status == AttendanceStatus.HALF_DAY,
                    color = HalfDayAmber,
                    bgColor = HalfDayContainer,
                    onClick = { onStatusToggle(AttendanceStatus.HALF_DAY) }
                )

                QuickActionChip(
                    label = "Leave",
                    isSelected = item.status == AttendanceStatus.LEAVE,
                    color = LeaveRed,
                    bgColor = LeaveContainer,
                    onClick = { onStatusToggle(AttendanceStatus.LEAVE) }
                )

                if (item.status != null) {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear attendance",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    isSelected: Boolean,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) bgColor else Color(0xFF1E2229))
            .border(
                1.dp,
                if (isSelected) color else DarkOutline,
                RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) color else TextSecondary
        )
    }
}
