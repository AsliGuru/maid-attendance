package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Maid
import com.example.domain.SalaryCalculator
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MaidSelectorBar(
    maids: List<Maid>,
    selectedMaid: Maid?,
    onSelectMaid: (Maid) -> Unit,
    onAddNewMaid: () -> Unit,
    onManageMaids: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("maid_selector_bar"),
        shape = RoundedCornerShape(18.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Household Maids (${maids.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = if (selectedMaid != null) "Active: ${selectedMaid.name}" else "No maid selected",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selectedMaid != null) PrimaryAccent else TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = onManageMaids,
                        modifier = Modifier.testTag("manage_maids_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Manage",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal scrolling carousel of maids + Add Maid button
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(maids, key = { it.id }) { maid ->
                    val isSelected = selectedMaid?.id == maid.id
                    MaidSelectorChip(
                        maid = maid,
                        isSelected = isSelected,
                        onClick = { onSelectMaid(maid) }
                    )
                }

                // Add Maid Action Card
                item {
                    AddMaidPillCard(onClick = onAddNewMaid)
                }
            }
        }
    }
}

@Composable
private fun MaidSelectorChip(
    maid: Maid,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryAccent else DarkOutline
    val backgroundColor = if (isSelected) Color(0xFF1B2330) else DarkCard

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("maid_chip_${maid.id}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PrimaryAccent.copy(alpha = 0.25f) else Color(0xFF242A36)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = maid.name.take(2).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) PrimaryAccent else TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = maid.name,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) PrimaryAccent else TextPrimary
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = "${SalaryCalculator.formatRupee(maid.monthlySalary)} • ${maid.freeLeavesPerMonth} free leaves",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AddMaidPillCard(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF12151D),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("selector_add_maid_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(PrimaryAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = PrimaryAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add Maid",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = PrimaryAccent
            )
        }
    }
}
