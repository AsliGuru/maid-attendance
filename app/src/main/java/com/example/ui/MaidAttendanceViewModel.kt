package com.example.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.DailyMaidAttendance
import com.example.data.model.Maid
import com.example.data.model.MonthlySummary
import com.example.data.model.SalarySnapshot
import com.example.data.preferences.AppPreferences
import com.example.data.repository.MaidAttendanceRepository
import com.example.domain.SalaryCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class MaidAttendanceUiState(
    val maids: List<Maid> = emptyList(),
    val selectedMaid: Maid? = null,
    val selectedDate: LocalDate = LocalDate.of(2026, 10, 17),
    val currentMonth: YearMonth = YearMonth.of(2026, 10),
    val selectedMaidAttendanceForDate: AttendanceRecord? = null,
    val quickSelectedStatus: AttendanceStatus? = null,
    val dailyAttendanceList: List<DailyMaidAttendance> = emptyList(),
    val monthRecords: List<AttendanceRecord> = emptyList(),
    val selectedMaidMonthlySummary: MonthlySummary = MonthlySummary(),
    val allMaidsMonthlySummary: MonthlySummary = MonthlySummary(),
    val dateSummary: DateSummary = DateSummary(),
    val isSummaryForAllMaids: Boolean = false,
    val showManageMaidsDialog: Boolean = false,
    val showAddEditMaidDialog: Boolean = false,
    val maidToEdit: Maid? = null,
    val maidToDelete: Maid? = null,
    val showBackupExportDialog: Boolean = false,
    val userMessage: String? = null
)

data class DateSummary(
    val fullDaysCount: Int = 0,
    val halfDaysCount: Int = 0,
    val leaveCount: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class MaidAttendanceViewModel(
    private val repository: MaidAttendanceRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    private val _selectedDate = MutableStateFlow(LocalDate.of(2026, 10, 17))
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.of(2026, 10))
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedMaidId = MutableStateFlow<Long?>(null)
    val selectedMaidId: StateFlow<Long?> = _selectedMaidId.asStateFlow()

    private val _quickStatus = MutableStateFlow<AttendanceStatus?>(null)
    val quickStatus: StateFlow<AttendanceStatus?> = _quickStatus.asStateFlow()

    private val _isSummaryForAllMaids = MutableStateFlow(false)
    val isSummaryForAllMaids: StateFlow<Boolean> = _isSummaryForAllMaids.asStateFlow()

    private val _showManageMaidsDialog = MutableStateFlow(false)
    val showManageMaidsDialog: StateFlow<Boolean> = _showManageMaidsDialog.asStateFlow()

    private val _showAddEditMaidDialog = MutableStateFlow(false)
    val showAddEditMaidDialog: StateFlow<Boolean> = _showAddEditMaidDialog.asStateFlow()

    private val _maidToEdit = MutableStateFlow<Maid?>(null)
    val maidToEdit: StateFlow<Maid?> = _maidToEdit.asStateFlow()

    private val _maidToDelete = MutableStateFlow<Maid?>(null)
    val maidToDelete: StateFlow<Maid?> = _maidToDelete.asStateFlow()

    private val _showBackupExportDialog = MutableStateFlow(false)
    val showBackupExportDialog: StateFlow<Boolean> = _showBackupExportDialog.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    // Records for the selected date
    private val recordsForSelectedDate = _selectedDate.flatMapLatest { date ->
        repository.getRecordsForDate(date.format(dateFormatter))
    }

    // Records for the active calendar month
    private val recordsForCurrentMonth = _currentMonth.flatMapLatest { month ->
        repository.getRecordsForMonth(month.format(monthFormatter))
    }

    // Snapshot for active maid and month
    private val activeSalarySnapshot = combine(_selectedMaidId, _currentMonth) { maidId, month ->
        Pair(maidId, month.format(monthFormatter))
    }.flatMapLatest { (maidId, monthStr) ->
        if (maidId != null) {
            repository.getSalarySnapshot(maidId, monthStr)
        } else {
            MutableStateFlow<SalarySnapshot?>(null)
        }
    }

    private data class CalendarSelection(
        val maidId: Long?,
        val date: LocalDate,
        val month: YearMonth
    )

    private val selectionFlow = combine(_selectedMaidId, _selectedDate, _currentMonth) { maidId, date, month ->
        CalendarSelection(maidId, date, month)
    }

    val uiState: StateFlow<MaidAttendanceUiState> = combine(
        repository.allMaids,
        selectionFlow,
        recordsForSelectedDate,
        recordsForCurrentMonth,
        activeSalarySnapshot
    ) { maids, selection, dateRecords, monthRecords, snapshot ->
        val chosenMaid = maids.find { it.id == selection.maidId }
            ?: maids.firstOrNull { it.name == "Sunita" }
            ?: maids.firstOrNull()

        val selectedRecord = chosenMaid?.let { maid ->
            dateRecords.find { it.maidId == maid.id }
        }

        // Selected date counters across all maids
        val fullCount = dateRecords.count { it.status == AttendanceStatus.FULL_DAY }
        val halfCount = dateRecords.count { it.status == AttendanceStatus.HALF_DAY }
        val leaveCount = dateRecords.count { it.status == AttendanceStatus.LEAVE }
        val dateSummary = DateSummary(
            fullDaysCount = fullCount,
            halfDaysCount = halfCount,
            leaveCount = leaveCount
        )

        // Daily list for all maids
        val dailyList = maids.map { maid ->
            val record = dateRecords.find { it.maidId == maid.id }
            DailyMaidAttendance(
                maid = maid,
                status = record?.status,
                recordId = record?.id,
                notes = record?.notes
            )
        }

        // Summary for selected maid (using snapshot if finalized)
        val selectedMaidSummary = if (chosenMaid != null) {
            val maidMonthRecs = monthRecords.filter { it.maidId == chosenMaid.id }
            if (snapshot != null && snapshot.isFinalized) {
                // Return preserved historical snapshot
                val fullDays = maidMonthRecs.count { it.status == AttendanceStatus.FULL_DAY }
                val halfDays = maidMonthRecs.count { it.status == AttendanceStatus.HALF_DAY }
                val leaves = maidMonthRecs.count { it.status == AttendanceStatus.LEAVE }
                MonthlySummary(
                    monthlySalary = snapshot.monthlySalary,
                    freeLeavesAllowed = snapshot.freeLeavesPerMonth,
                    totalFullDays = fullDays,
                    totalHalfDays = halfDays,
                    totalLeaves = leaves,
                    totalLeaveDays = snapshot.totalLeaveDays,
                    freeLeavesConsumed = snapshot.freeLeavesConsumed,
                    unpaidLeaveDays = snapshot.unpaidLeaveDays,
                    dailyDeductionRate = snapshot.dailyDeductionRate,
                    leaveDeduction = snapshot.leaveDeduction,
                    payableSalaryEstimate = snapshot.estimatedPayableSalary,
                    isFinalized = true,
                    finalizedAt = snapshot.finalizedAt
                )
            } else {
                calculateSummary(
                    salary = chosenMaid.monthlySalary,
                    freeLeaves = chosenMaid.freeLeavesPerMonth,
                    records = maidMonthRecs
                )
            }
        } else {
            MonthlySummary()
        }

        // Combined summary for all maids
        val allMaidsSummary = calculateCombinedSummary(
            maids = maids,
            records = monthRecords
        )

        MaidAttendanceUiState(
            maids = maids,
            selectedMaid = chosenMaid,
            selectedDate = selection.date,
            currentMonth = selection.month,
            selectedMaidAttendanceForDate = selectedRecord,
            quickSelectedStatus = _quickStatus.value ?: selectedRecord?.status,
            dailyAttendanceList = dailyList,
            monthRecords = monthRecords,
            selectedMaidMonthlySummary = selectedMaidSummary,
            allMaidsMonthlySummary = allMaidsSummary,
            dateSummary = dateSummary,
            isSummaryForAllMaids = _isSummaryForAllMaids.value,
            showManageMaidsDialog = _showManageMaidsDialog.value,
            showAddEditMaidDialog = _showAddEditMaidDialog.value,
            maidToEdit = _maidToEdit.value,
            maidToDelete = _maidToDelete.value,
            showBackupExportDialog = _showBackupExportDialog.value,
            userMessage = _userMessage.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MaidAttendanceUiState()
    )

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            // Restore last selected maid ID from preferences
            val savedMaidId = preferences.selectedMaidId.first()
            if (savedMaidId != null) {
                _selectedMaidId.value = savedMaidId
            }
            // Ensure valid selected maid
            repository.allMaids.collect { maids ->
                if (_selectedMaidId.value == null && maids.isNotEmpty()) {
                    val sunita = maids.find { it.name == "Sunita" } ?: maids.first()
                    _selectedMaidId.value = sunita.id
                }
            }
        }
    }

    private fun calculateSummary(
        salary: Double,
        freeLeaves: Int,
        records: List<AttendanceRecord>
    ): MonthlySummary {
        val fullDays = records.count { it.status == AttendanceStatus.FULL_DAY }
        val halfDays = records.count { it.status == AttendanceStatus.HALF_DAY }
        val leaves = records.count { it.status == AttendanceStatus.LEAVE }

        val calcResult = SalaryCalculator.computeSalary(
            monthlySalary = salary,
            freeLeavesPerMonth = freeLeaves,
            fullDayLeaves = leaves,
            halfDayLeaves = halfDays
        )

        return MonthlySummary(
            monthlySalary = calcResult.monthlySalary.toDouble(),
            freeLeavesAllowed = calcResult.freeLeavesAllowed,
            totalFullDays = fullDays,
            totalHalfDays = halfDays,
            totalLeaves = leaves,
            totalLeaveDays = calcResult.totalLeaveDays.toDouble(),
            freeLeavesConsumed = calcResult.freeLeavesConsumed.toDouble(),
            unpaidLeaveDays = calcResult.unpaidLeaveDays.toDouble(),
            dailyDeductionRate = calcResult.dailyDeductionRate.toDouble(),
            leaveDeduction = calcResult.leaveDeduction.toDouble(),
            payableSalaryEstimate = calcResult.estimatedPayableSalary.toDouble(),
            isFinalized = false,
            finalizedAt = null
        )
    }

    private fun calculateCombinedSummary(
        maids: List<Maid>,
        records: List<AttendanceRecord>
    ): MonthlySummary {
        var totalFull = 0
        var totalHalf = 0
        var totalLeave = 0
        var totalLeaveDays = 0.0
        var totalFreeLeavesAllowed = 0
        var totalFreeConsumed = 0.0
        var totalUnpaid = 0.0
        var totalDeduction = 0.0
        var totalPayable = 0.0
        var totalBaseSalary = 0.0

        for (maid in maids) {
            totalBaseSalary += maid.monthlySalary
            totalFreeLeavesAllowed += maid.freeLeavesPerMonth

            val maidRecs = records.filter { it.maidId == maid.id }
            val full = maidRecs.count { it.status == AttendanceStatus.FULL_DAY }
            val half = maidRecs.count { it.status == AttendanceStatus.HALF_DAY }
            val leaves = maidRecs.count { it.status == AttendanceStatus.LEAVE }

            totalFull += full
            totalHalf += half
            totalLeave += leaves

            val calc = SalaryCalculator.computeSalary(
                monthlySalary = maid.monthlySalary,
                freeLeavesPerMonth = maid.freeLeavesPerMonth,
                fullDayLeaves = leaves,
                halfDayLeaves = half
            )

            totalLeaveDays += calc.totalLeaveDays.toDouble()
            totalFreeConsumed += calc.freeLeavesConsumed.toDouble()
            totalUnpaid += calc.unpaidLeaveDays.toDouble()
            totalDeduction += calc.leaveDeduction.toDouble()
            totalPayable += calc.estimatedPayableSalary.toDouble()
        }

        val dailyRate = if (totalBaseSalary > 0) totalBaseSalary / 30.0 else 0.0

        return MonthlySummary(
            monthlySalary = totalBaseSalary,
            freeLeavesAllowed = totalFreeLeavesAllowed,
            totalFullDays = totalFull,
            totalHalfDays = totalHalf,
            totalLeaves = totalLeave,
            totalLeaveDays = totalLeaveDays,
            freeLeavesConsumed = totalFreeConsumed,
            unpaidLeaveDays = totalUnpaid,
            dailyDeductionRate = dailyRate,
            leaveDeduction = totalDeduction,
            payableSalaryEstimate = totalPayable,
            isFinalized = false,
            finalizedAt = null
        )
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        if (YearMonth.from(date) != _currentMonth.value) {
            _currentMonth.value = YearMonth.from(date)
        }
        viewModelScope.launch {
            val maidId = _selectedMaidId.value
            if (maidId != null) {
                val record = repository.getRecordForMaidAndDateOnce(maidId, date.format(dateFormatter))
                _quickStatus.value = record?.status
            } else {
                _quickStatus.value = null
            }
        }
    }

    fun changeMonth(offsetMonths: Long) {
        val newMonth = _currentMonth.value.plusMonths(offsetMonths)
        _currentMonth.value = newMonth
        // If selected date is outside new month, adjust to 1st of that month
        if (YearMonth.from(_selectedDate.value) != newMonth) {
            _selectedDate.value = newMonth.atDay(1)
        }
    }

    fun jumpToToday() {
        val today = LocalDate.now()
        selectDate(today)
    }

    fun selectMaid(maid: Maid) {
        _selectedMaidId.value = maid.id
        viewModelScope.launch {
            preferences.setSelectedMaidId(maid.id)
            val record = repository.getRecordForMaidAndDateOnce(
                maid.id,
                _selectedDate.value.format(dateFormatter)
            )
            _quickStatus.value = record?.status
        }
    }

    fun setQuickStatus(status: AttendanceStatus) {
        _quickStatus.value = status
    }

    fun saveQuickAttendance(notes: String? = null) {
        val maidId = _selectedMaidId.value ?: return
        val status = _quickStatus.value ?: AttendanceStatus.FULL_DAY
        val dateStr = _selectedDate.value.format(dateFormatter)

        viewModelScope.launch {
            repository.saveAttendance(maidId, dateStr, status, notes)
            _userMessage.value = "Attendance marked as ${status.displayName} for ${_selectedDate.value}"
        }
    }

    fun clearQuickAttendance() {
        val maidId = _selectedMaidId.value ?: return
        val dateStr = _selectedDate.value.format(dateFormatter)

        viewModelScope.launch {
            repository.clearAttendance(maidId, dateStr)
            _quickStatus.value = null
            _userMessage.value = "Attendance cleared for ${_selectedDate.value}"
        }
    }

    fun setAttendanceDirectly(maidId: Long, status: AttendanceStatus) {
        val dateStr = _selectedDate.value.format(dateFormatter)
        viewModelScope.launch {
            repository.saveAttendance(maidId, dateStr, status)
            if (_selectedMaidId.value == maidId) {
                _quickStatus.value = status
            }
            _userMessage.value = "Updated to ${status.displayName}"
        }
    }

    fun clearAttendanceDirectly(maidId: Long) {
        val dateStr = _selectedDate.value.format(dateFormatter)
        viewModelScope.launch {
            repository.clearAttendance(maidId, dateStr)
            if (_selectedMaidId.value == maidId) {
                _quickStatus.value = null
            }
            _userMessage.value = "Cleared attendance"
        }
    }

    fun toggleSummaryMode(forAllMaids: Boolean) {
        _isSummaryForAllMaids.value = forAllMaids
    }

    fun finalizeMonthSalary() {
        val maid = uiState.value.selectedMaid ?: return
        val monthStr = _currentMonth.value.format(monthFormatter)
        val summary = uiState.value.selectedMaidMonthlySummary

        viewModelScope.launch {
            val snapshot = SalarySnapshot(
                maidId = maid.id,
                month = monthStr,
                monthlySalary = summary.monthlySalary,
                freeLeavesPerMonth = summary.freeLeavesAllowed,
                dailyDeductionRate = summary.dailyDeductionRate,
                totalLeaveDays = summary.totalLeaveDays,
                freeLeavesConsumed = summary.freeLeavesConsumed,
                unpaidLeaveDays = summary.unpaidLeaveDays,
                leaveDeduction = summary.leaveDeduction,
                estimatedPayableSalary = summary.payableSalaryEstimate,
                isFinalized = true,
                finalizedAt = System.currentTimeMillis()
            )
            repository.saveSalarySnapshot(snapshot)
            _userMessage.value = "Finalized salary snapshot for ${maid.name} ($monthStr)"
        }
    }

    fun recalculateMonthSalary() {
        val maidId = _selectedMaidId.value ?: return
        val monthStr = _currentMonth.value.format(monthFormatter)

        viewModelScope.launch {
            repository.deleteSalarySnapshot(maidId, monthStr)
            _userMessage.value = "Recalculated salary with current maid settings"
        }
    }

    fun openManageMaidsDialog() {
        _showManageMaidsDialog.value = true
    }

    fun closeManageMaidsDialog() {
        _showManageMaidsDialog.value = false
    }

    fun openAddMaidDialog() {
        _maidToEdit.value = null
        _showAddEditMaidDialog.value = true
    }

    fun openEditMaidDialog(maid: Maid) {
        _maidToEdit.value = maid
        _showAddEditMaidDialog.value = true
    }

    fun closeAddEditMaidDialog() {
        _showAddEditMaidDialog.value = false
        _maidToEdit.value = null
    }

    fun promptDeleteMaid(maid: Maid) {
        _maidToDelete.value = maid
    }

    fun dismissDeletePrompt() {
        _maidToDelete.value = null
    }

    fun confirmDeleteMaid(maid: Maid) {
        viewModelScope.launch {
            repository.deleteMaid(maid)
            val allRemaining = repository.getAllMaidsOnce().filter { it.id != maid.id }
            if (_selectedMaidId.value == maid.id) {
                val nextMaid = allRemaining.firstOrNull()
                _selectedMaidId.value = nextMaid?.id
                if (nextMaid != null) {
                    preferences.setSelectedMaidId(nextMaid.id)
                }
            }
            _maidToDelete.value = null
            _userMessage.value = "Deleted maid ${maid.name}. Historical records removed."
        }
    }

    fun saveMaidProfile(
        id: Long,
        name: String,
        role: String,
        salary: Double,
        freeLeaves: Int
    ): Boolean {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _userMessage.value = "Maid name cannot be empty."
            return false
        }

        // Duplicate normalized name check
        val normalized = trimmedName.lowercase()
        val existingMaids = uiState.value.maids
        val isDuplicate = existingMaids.any { it.id != id && it.name.trim().lowercase() == normalized }
        if (isDuplicate) {
            _userMessage.value = "A maid named '$trimmedName' already exists. Please choose a distinct name."
            return false
        }

        val validation = SalaryCalculator.validateSalaryAndFreeLeaves(salary, freeLeaves)
        if (!validation.isValid) {
            _userMessage.value = validation.errorMessage ?: "Invalid salary or free leaves."
            return false
        }

        viewModelScope.launch {
            val maid = Maid(
                id = id,
                name = trimmedName,
                role = role.trim().ifEmpty { "General Housekeeping" },
                monthlySalary = salary,
                freeLeavesPerMonth = freeLeaves
            )
            val savedId = repository.saveMaid(maid)
            if (id == 0L) {
                // New maid added - auto select!
                _selectedMaidId.value = savedId
                preferences.setSelectedMaidId(savedId)
            }
            _userMessage.value = "Saved profile for ${maid.name}"
            closeAddEditMaidDialog()
        }
        return true
    }

    fun openBackupExportDialog() {
        _showBackupExportDialog.value = true
    }

    fun closeBackupExportDialog() {
        _showBackupExportDialog.value = false
    }

    fun exportCsvToUri(uri: Uri, context: Context, scopedMaidId: Long? = null) {
        viewModelScope.launch {
            try {
                val csvContent = repository.exportCsv(scopedMaidId)
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
                _userMessage.value = "CSV report successfully exported!"
            } catch (e: Exception) {
                _userMessage.value = "Export failed: ${e.message}"
            }
        }
    }

    fun exportJsonBackupToUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val jsonContent = repository.exportJson()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonContent.toByteArray(Charsets.UTF_8))
                }
                _userMessage.value = "JSON backup successfully created!"
            } catch (e: Exception) {
                _userMessage.value = "Backup failed: ${e.message}"
            }
        }
    }

    fun restoreJsonBackupFromUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).readText()
                } ?: throw IllegalArgumentException("Cannot read selected file.")

                val result = repository.restoreFromJson(jsonString)
                if (result.isSuccess) {
                    _userMessage.value = "Restored ${result.getOrNull()} records successfully!"
                } else {
                    _userMessage.value = "Restore failed: ${result.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _userMessage.value = "Restore failed: ${e.message}"
            }
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    class Factory(
        private val repository: MaidAttendanceRepository,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MaidAttendanceViewModel::class.java)) {
                return MaidAttendanceViewModel(repository, preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
