package com.example.data.repository

import com.example.data.local.AttendanceDao
import com.example.data.local.MaidDao
import com.example.data.local.SalarySnapshotDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.BackupData
import com.example.data.model.Maid
import com.example.data.model.SalarySnapshot
import com.example.data.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MaidAttendanceRepository(
    private val maidDao: MaidDao,
    private val attendanceDao: AttendanceDao,
    private val salarySnapshotDao: SalarySnapshotDao,
    private val preferences: AppPreferences
) {

    val allMaids: Flow<List<Maid>> = maidDao.getAllMaids()

    suspend fun checkAndSeedInitialData() {
        val count = maidDao.getMaidCount()
        val isAlreadySeeded = preferences.isSeeded.first()

        if (count == 0 && !isAlreadySeeded) {
            val sunita = Maid(
                name = "Sunita",
                role = "Cooking & Cleaning",
                monthlySalary = 5000.0,
                freeLeavesPerMonth = 2
            )
            val kalpana = Maid(
                name = "Kalpana",
                role = "Housekeeping",
                monthlySalary = 4500.0,
                freeLeavesPerMonth = 2
            )
            val pooja = Maid(
                name = "Pooja",
                role = "Utensils & Dusting",
                monthlySalary = 3500.0,
                freeLeavesPerMonth = 2
            )

            val sunitaId = maidDao.insertMaid(sunita)
            maidDao.insertMaid(kalpana)
            maidDao.insertMaid(pooja)

            // Seed Sunita's attendance on 17 October 2026 as Half Day
            val initialDate = "2026-10-17"
            val sunitaRecord = AttendanceRecord(
                maidId = sunitaId,
                date = initialDate,
                status = AttendanceStatus.HALF_DAY,
                notes = "Sample initial attendance record"
            )
            attendanceDao.insertOrUpdate(sunitaRecord)

            preferences.setSelectedMaidId(sunitaId)
            preferences.setSelectedDate(initialDate)
            preferences.setSeeded(true)
        }
    }

    fun getMaidById(id: Long): Flow<Maid?> = maidDao.getMaidById(id)

    suspend fun getMaidByIdOnce(id: Long): Maid? = maidDao.getMaidByIdOnce(id)

    suspend fun getAllMaidsOnce(): List<Maid> = maidDao.getAllMaidsOnce()

    suspend fun saveMaid(maid: Maid): Long {
        return if (maid.id == 0L) {
            maidDao.insertMaid(maid)
        } else {
            maidDao.updateMaid(maid)
            maid.id
        }
    }

    suspend fun deleteMaid(maid: Maid): Int = maidDao.deleteMaid(maid)

    suspend fun deleteMaidById(id: Long): Int = maidDao.deleteMaidById(id)

    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForDate(date)

    suspend fun getRecordsForDateOnce(date: String): List<AttendanceRecord> =
        attendanceDao.getRecordsForDateOnce(date)

    fun getRecordsForMonth(monthPrefix: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForMonth(monthPrefix)

    fun getRecordsForMaidAndMonth(maidId: Long, monthPrefix: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getRecordsForMaidAndMonth(maidId, monthPrefix)

    fun getRecordForMaidAndDate(maidId: Long, date: String): Flow<AttendanceRecord?> =
        attendanceDao.getRecordForMaidAndDate(maidId, date)

    suspend fun getRecordForMaidAndDateOnce(maidId: Long, date: String): AttendanceRecord? =
        attendanceDao.getRecordForMaidAndDateOnce(maidId, date)

    suspend fun saveAttendance(
        maidId: Long,
        date: String,
        status: AttendanceStatus,
        notes: String? = null
    ): Long {
        val existing = attendanceDao.getRecordForMaidAndDateOnce(maidId, date)
        val now = System.currentTimeMillis()
        val record = if (existing != null) {
            existing.copy(
                status = status,
                notes = notes ?: existing.notes,
                updatedAt = now
            )
        } else {
            AttendanceRecord(
                maidId = maidId,
                date = date,
                status = status,
                notes = notes,
                createdAt = now,
                updatedAt = now
            )
        }
        return attendanceDao.insertOrUpdate(record)
    }

    suspend fun clearAttendance(maidId: Long, date: String): Int {
        return attendanceDao.deleteRecord(maidId, date)
    }

    // Historical Salary Snapshots
    fun getSalarySnapshot(maidId: Long, month: String): Flow<SalarySnapshot?> =
        salarySnapshotDao.getSnapshot(maidId, month)

    suspend fun getSalarySnapshotOnce(maidId: Long, month: String): SalarySnapshot? =
        salarySnapshotDao.getSnapshotOnce(maidId, month)

    suspend fun saveSalarySnapshot(snapshot: SalarySnapshot): Long =
        salarySnapshotDao.insertOrUpdate(snapshot)

    suspend fun deleteSalarySnapshot(maidId: Long, month: String): Int =
        salarySnapshotDao.deleteSnapshot(maidId, month)

    suspend fun exportCsv(scopedMaidId: Long? = null): String {
        val maids = maidDao.getAllMaidsOnce().associateBy { it.id }
        val allRecords = attendanceDao.getAllRecordsOnce()
        val records = if (scopedMaidId != null) allRecords.filter { it.maidId == scopedMaidId } else allRecords

        val sb = StringBuilder()
        sb.append("Record ID,Maid ID,Maid Name,Date,Status,Leave Equivalent Days,Notes,Updated At\n")
        for (rec in records) {
            val maid = maids[rec.maidId]
            val maidName = maid?.name ?: "Unknown (#${rec.maidId})"
            val leaveDays = when (rec.status) {
                AttendanceStatus.FULL_DAY -> 0.0
                AttendanceStatus.HALF_DAY -> 0.5
                AttendanceStatus.LEAVE -> 1.0
            }
            val notesEscaped = rec.notes?.replace("\"", "\"\"") ?: ""
            sb.append("${rec.id},${rec.maidId},\"$maidName\",${rec.date},${rec.status.displayName},$leaveDays,\"$notesEscaped\",${rec.updatedAt}\n")
        }
        return sb.toString()
    }

    suspend fun exportJson(): String {
        val maids = maidDao.getAllMaidsOnce()
        val records = attendanceDao.getAllRecordsOnce()
        val snapshots = salarySnapshotDao.getAllSnapshotsOnce()
        val backup = BackupData(
            exportTimestamp = System.currentTimeMillis(),
            appVersion = "2.0",
            maids = maids,
            records = records,
            salarySnapshots = snapshots
        )
        return backup.toJsonString()
    }

    suspend fun restoreFromJson(jsonString: String): Result<Int> {
        return try {
            val backup = BackupData.fromJsonString(jsonString)
            if (backup.maids.isEmpty() && backup.records.isEmpty()) {
                return Result.failure(IllegalArgumentException("Backup file contains no data."))
            }

            // Restore maids first to respect foreign keys
            maidDao.insertAll(backup.maids)
            attendanceDao.insertAll(backup.records)
            if (backup.salarySnapshots.isNotEmpty()) {
                salarySnapshotDao.insertAll(backup.salarySnapshots)
            }

            Result.success(backup.records.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
