package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddEditMaidDialog
import com.example.ui.components.AttendanceCalendar
import com.example.ui.components.BackupExportDialog
import com.example.ui.components.DailyAttendanceList
import com.example.ui.components.MaidSelectorBar
import com.example.ui.components.ManageMaidsDialog
import com.example.ui.components.MonthlySummarySection
import com.example.ui.components.QuickEditPanel
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkOutline
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaidAttendanceApp(
    viewModel: MaidAttendanceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    val todayFormatted = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
    }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentColor = TextPrimary,
        contentWindowInsets = WindowInsets.statusBars,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Maid Attendance",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = todayFormatted,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.openManageMaidsDialog() },
                        modifier = Modifier.testTag("manage_maids_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = "Manage Maids",
                            tint = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openBackupExportDialog() },
                        modifier = Modifier.testTag("export_backup_action")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = "Data & Reports",
                            tint = TextPrimary
                        )
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = TextSecondary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Add New Maid") },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryAccent)
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.openAddMaidDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Manage All Maids") },
                                leadingIcon = {
                                    Icon(Icons.Default.Group, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.openManageMaidsDialog()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export & Backup") },
                                leadingIcon = {
                                    Icon(Icons.Default.TableChart, contentDescription = null)
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.openBackupExportDialog()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddMaidDialog() },
                containerColor = PrimaryAccent,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_maid")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Maid",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (uiState.maids.isEmpty()) {
                // Empty state when no maids exist
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = DarkCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkOutline),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Household Maids",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Add your maids to start tracking daily attendance, leaves, and calculating salaries.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { viewModel.openAddMaidDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PrimaryAccent,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add First Maid", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 680.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Prominent Multi-Maid Selector Bar
                    item {
                        MaidSelectorBar(
                            maids = uiState.maids,
                            selectedMaid = uiState.selectedMaid,
                            onSelectMaid = { maid -> viewModel.selectMaid(maid) },
                            onAddNewMaid = { viewModel.openAddMaidDialog() },
                            onManageMaids = { viewModel.openManageMaidsDialog() }
                        )
                    }

                    // 2. Compact Black Quick-Edit Panel
                    item {
                        QuickEditPanel(
                            selectedMaid = uiState.selectedMaid,
                            allMaids = uiState.maids,
                            selectedDate = uiState.selectedDate,
                            selectedStatus = uiState.quickSelectedStatus,
                            onSelectMaid = { maid -> viewModel.selectMaid(maid) },
                            onStatusSelected = { status -> viewModel.setQuickStatus(status) },
                            onDateSelected = { date -> viewModel.selectDate(date) },
                            onSaveAttendance = { viewModel.saveQuickAttendance() },
                            onClearAttendance = { viewModel.clearQuickAttendance() },
                            onEditMaidProfile = { maid -> viewModel.openEditMaidDialog(maid) }
                        )
                    }

                    // 3. Attendance Calendar
                    item {
                        AttendanceCalendar(
                            currentMonth = uiState.currentMonth,
                            selectedDate = uiState.selectedDate,
                            selectedMaid = uiState.selectedMaid,
                            monthRecords = uiState.monthRecords,
                            onDateSelected = { date -> viewModel.selectDate(date) },
                            onPreviousMonth = { viewModel.changeMonth(-1) },
                            onNextMonth = { viewModel.changeMonth(1) },
                            onJumpToToday = { viewModel.jumpToToday() }
                        )
                    }

                    // 4. Daily Attendance List for All Maids on Selected Date
                    item {
                        DailyAttendanceList(
                            selectedDate = uiState.selectedDate,
                            dailyList = uiState.dailyAttendanceList,
                            selectedMaid = uiState.selectedMaid,
                            dateSummary = uiState.dateSummary,
                            onMaidSelected = { maid -> viewModel.selectMaid(maid) },
                            onStatusToggle = { maidId, status -> viewModel.setAttendanceDirectly(maidId, status) },
                            onClearAttendance = { maidId -> viewModel.clearAttendanceDirectly(maidId) }
                        )
                    }

                    // 5. Monthly Attendance Summary & Salary Calculation
                    item {
                        MonthlySummarySection(
                            currentMonth = uiState.currentMonth,
                            selectedMaid = uiState.selectedMaid,
                            selectedMaidSummary = uiState.selectedMaidMonthlySummary,
                            allMaidsSummary = uiState.allMaidsMonthlySummary,
                            isAllMaidsMode = uiState.isSummaryForAllMaids,
                            onToggleMode = { forAll -> viewModel.toggleSummaryMode(forAll) },
                            onFinalizeSalary = { viewModel.finalizeMonthSalary() },
                            onRecalculateSalary = { viewModel.recalculateMonthSalary() }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(60.dp))
                    }
                }
            }
        }
    }

    // Dialog: Manage Maids
    if (uiState.showManageMaidsDialog) {
        ManageMaidsDialog(
            maids = uiState.maids,
            selectedMaidId = uiState.selectedMaid?.id,
            maidToDelete = uiState.maidToDelete,
            onDismiss = { viewModel.closeManageMaidsDialog() },
            onSelectMaid = { maid ->
                viewModel.selectMaid(maid)
                viewModel.closeManageMaidsDialog()
            },
            onAddNewMaid = {
                viewModel.closeManageMaidsDialog()
                viewModel.openAddMaidDialog()
            },
            onEditMaid = { maid ->
                viewModel.closeManageMaidsDialog()
                viewModel.openEditMaidDialog(maid)
            },
            onPromptDeleteMaid = { maid -> viewModel.promptDeleteMaid(maid) },
            onConfirmDeleteMaid = { maid -> viewModel.confirmDeleteMaid(maid) },
            onDismissDeletePrompt = { viewModel.dismissDeletePrompt() }
        )
    }

    // Dialog: Add / Edit Maid Profile
    if (uiState.showAddEditMaidDialog) {
        AddEditMaidDialog(
            maidToEdit = uiState.maidToEdit,
            onDismiss = { viewModel.closeAddEditMaidDialog() },
            onSave = { id, name, role, salary, freeLeaves ->
                viewModel.saveMaidProfile(id, name, role, salary, freeLeaves)
            }
        )
    }

    // Dialog: Export / Backup
    if (uiState.showBackupExportDialog) {
        BackupExportDialog(
            onDismiss = { viewModel.closeBackupExportDialog() },
            onExportCsv = { uri, context ->
                val scopedId = if (uiState.isSummaryForAllMaids) null else uiState.selectedMaid?.id
                viewModel.exportCsvToUri(uri, context, scopedId)
            },
            onExportJsonBackup = { uri, context -> viewModel.exportJsonBackupToUri(uri, context) },
            onRestoreJsonBackup = { uri, context -> viewModel.restoreJsonBackupFromUri(uri, context) }
        )
    }
}
