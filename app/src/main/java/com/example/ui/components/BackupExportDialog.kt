package com.example.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FullDayGreen
import com.example.ui.theme.HalfDayAmber
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BackupExportDialog(
    onDismiss: () -> Unit,
    onExportCsv: (Uri, Context) -> Unit,
    onExportJsonBackup: (Uri, Context) -> Unit,
    onRestoreJsonBackup: (Uri, Context) -> Unit
) {
    val context = LocalContext.current
    val todayStr = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    // CSV Launcher
    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            onExportCsv(uri, context)
            onDismiss()
        }
    }

    // JSON Backup Launcher
    val jsonBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            onExportJsonBackup(uri, context)
            onDismiss()
        }
    }

    // JSON Restore Launcher
    val jsonRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            onRestoreJsonBackup(uri, context)
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("backup_export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Data & Reports",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Text(
                            text = "Export attendance or backup local database",
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

                Spacer(modifier = Modifier.height(18.dp))

                // Option 1: Export CSV
                ActionCard(
                    icon = Icons.Default.TableChart,
                    iconTint = FullDayGreen,
                    title = "Export Attendance as CSV",
                    subtitle = "Spreadsheet-ready report of all maid attendance records and units.",
                    onClick = {
                        csvLauncher.launch("Maid_Attendance_Report_$todayStr.csv")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option 2: JSON Backup
                ActionCard(
                    icon = Icons.Default.FileDownload,
                    iconTint = PrimaryAccent,
                    title = "Full Database Backup (JSON)",
                    subtitle = "Save a secure snapshot of all maids and attendance history to device storage.",
                    onClick = {
                        jsonBackupLauncher.launch("Maid_Attendance_Backup_$todayStr.json")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Option 3: Restore JSON
                ActionCard(
                    icon = Icons.Default.FileUpload,
                    iconTint = HalfDayAmber,
                    title = "Restore from JSON Backup",
                    subtitle = "Import and merge maid profiles and records from a previous backup file.",
                    onClick = {
                        jsonRestoreLauncher.launch(arrayOf("application/json", "text/*"))
                    }
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }
        }
    }
}
