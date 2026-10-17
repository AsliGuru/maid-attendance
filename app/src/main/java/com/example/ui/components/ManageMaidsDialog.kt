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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.Maid
import com.example.domain.SalaryCalculator
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LeaveRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ManageMaidsDialog(
    maids: List<Maid>,
    selectedMaidId: Long?,
    maidToDelete: Maid?,
    onDismiss: () -> Unit,
    onSelectMaid: (Maid) -> Unit,
    onAddNewMaid: () -> Unit,
    onEditMaid: (Maid) -> Unit,
    onPromptDeleteMaid: (Maid) -> Unit,
    onConfirmDeleteMaid: (Maid) -> Unit,
    onDismissDeletePrompt: () -> Unit
) {
    if (maidToDelete != null) {
        AlertDialog(
            onDismissRequest = onDismissDeletePrompt,
            title = {
                Text(
                    text = "Delete ${maidToDelete.name}?",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${maidToDelete.name} (#${maidToDelete.id})? All recorded attendance and historical salary data for this maid will be permanently removed.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { onConfirmDeleteMaid(maidToDelete) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LeaveRed,
                        contentColor = TextPrimary
                    ),
                    modifier = Modifier.testTag("confirm_delete_maid_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDeletePrompt) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = DarkSurface
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("manage_maids_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Manage Maids",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "${maids.size} registered maids • Tap to select active maid",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Add Maid Action Button
                Button(
                    onClick = onAddNewMaid,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_maid_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add New Maid", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable List of Maids (handles any number of maids)
                if (maids.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No maids found. Tap 'Add New Maid' to begin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(maids, key = { it.id }) { maid ->
                            val isSelected = maid.id == selectedMaidId
                            MaidManagementCard(
                                maid = maid,
                                isSelected = isSelected,
                                onSelect = { onSelectMaid(maid) },
                                onEdit = { onEditMaid(maid) },
                                onDelete = { onPromptDeleteMaid(maid) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@Composable
private fun MaidManagementCard(
    maid: Maid,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val borderColor = if (isSelected) PrimaryAccent else DarkOutline
    val bgColor = if (isSelected) Color(0xFF1B2330) else DarkCard

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onSelect)
            .testTag("manage_maid_item_${maid.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PrimaryAccent.copy(alpha = 0.25f) else Color(0xFF222834)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = maid.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) PrimaryAccent else TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = maid.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) PrimaryAccent else TextPrimary
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active Maid",
                            tint = PrimaryAccent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    text = maid.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "${SalaryCalculator.formatRupee(maid.monthlySalary)}/mo • ${maid.freeLeavesPerMonth} free leaves",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit ${maid.name}",
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${maid.name}",
                    tint = LeaveRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
