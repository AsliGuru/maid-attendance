package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.AttendanceDao
import com.example.data.local.MaidDao
import com.example.data.local.SalarySnapshotDao
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.Maid
import com.example.data.model.SalarySnapshot
import com.example.data.preferences.AppPreferences
import com.example.data.repository.MaidAttendanceRepository
import com.example.domain.SalaryCalculator
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MaidAttendanceDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var maidDao: MaidDao
    private lateinit var attendanceDao: AttendanceDao
    private lateinit var salarySnapshotDao: SalarySnapshotDao
    private lateinit var repository: MaidAttendanceRepository
    private lateinit var context: Context

    @Before
    fun createDb() {
        context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        maidDao = db.maidDao()
        attendanceDao = db.attendanceDao()
        salarySnapshotDao = db.salarySnapshotDao()
        val prefs = AppPreferences(context)
        repository = MaidAttendanceRepository(maidDao, attendanceDao, salarySnapshotDao, prefs)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInitialSeedingCreatesSunitaHalfDayOnOctober17() = runBlocking {
        repository.checkAndSeedInitialData()

        val maids = maidDao.getAllMaidsOnce()
        assertEquals(3, maids.size)
        assertTrue(maids.any { it.name == "Sunita" })
        assertTrue(maids.any { it.name == "Kalpana" })
        assertTrue(maids.any { it.name == "Pooja" })

        val sunita = maids.first { it.name == "Sunita" }
        assertEquals(2, sunita.freeLeavesPerMonth)
        val sunitaRec = attendanceDao.getRecordForMaidAndDateOnce(sunita.id, "2026-10-17")
        assertNotNull(sunitaRec)
        assertEquals(AttendanceStatus.HALF_DAY, sunitaRec?.status)

        // Ensure seeding is idempotent
        repository.checkAndSeedInitialData()
        val maidsAfter = maidDao.getAllMaidsOnce()
        assertEquals(3, maidsAfter.size)
    }

    @Test
    fun testAcceptanceTestFiveMaidsPlusSixthMaid() = runBlocking {
        // Section 8 Acceptance Test:
        // 1. Five different maids can be created
        val m1 = Maid(name = "Sunita", role = "Cook", monthlySalary = 6000.0, freeLeavesPerMonth = 2)
        val m2 = Maid(name = "Kalpana", role = "Cleaner", monthlySalary = 4500.0, freeLeavesPerMonth = 1)
        val m3 = Maid(name = "Pooja", role = "Utensils", monthlySalary = 3000.0, freeLeavesPerMonth = 0)
        val m4 = Maid(name = "Meena", role = "Babysitter", monthlySalary = 7500.0, freeLeavesPerMonth = 3)
        val m5 = Maid(name = "Anita", role = "Gardener", monthlySalary = 4000.0, freeLeavesPerMonth = 4)

        val id1 = repository.saveMaid(m1)
        val id2 = repository.saveMaid(m2)
        val id3 = repository.saveMaid(m3)
        val id4 = repository.saveMaid(m4)
        val id5 = repository.saveMaid(m5)

        val maids = repository.getAllMaidsOnce()
        assertEquals(5, maids.size)

        // 2. Each maid has different salary and free leave settings
        val storedM1 = repository.getMaidByIdOnce(id1)!!
        val storedM3 = repository.getMaidByIdOnce(id3)!!
        assertEquals(6000.0, storedM1.monthlySalary, 0.001)
        assertEquals(2, storedM1.freeLeavesPerMonth)
        assertEquals(3000.0, storedM3.monthlySalary, 0.001)
        assertEquals(0, storedM3.freeLeavesPerMonth)

        // 3. Different attendance recorded for all five maids on the same date
        val date = "2026-10-20"
        repository.saveAttendance(id1, date, AttendanceStatus.FULL_DAY)
        repository.saveAttendance(id2, date, AttendanceStatus.HALF_DAY)
        repository.saveAttendance(id3, date, AttendanceStatus.LEAVE)
        repository.saveAttendance(id4, date, AttendanceStatus.FULL_DAY)
        repository.saveAttendance(id5, date, AttendanceStatus.HALF_DAY)

        // 4. Switching between maids and dates displays correct attendance
        assertEquals(AttendanceStatus.FULL_DAY, repository.getRecordForMaidAndDateOnce(id1, date)?.status)
        assertEquals(AttendanceStatus.HALF_DAY, repository.getRecordForMaidAndDateOnce(id2, date)?.status)
        assertEquals(AttendanceStatus.LEAVE, repository.getRecordForMaidAndDateOnce(id3, date)?.status)
        assertEquals(AttendanceStatus.FULL_DAY, repository.getRecordForMaidAndDateOnce(id4, date)?.status)
        assertEquals(AttendanceStatus.HALF_DAY, repository.getRecordForMaidAndDateOnce(id5, date)?.status)

        // 5. Each maid's monthly summary and estimated payable salary are calculated independently
        val calc1 = SalaryCalculator.computeSalary(storedM1.monthlySalary, storedM1.freeLeavesPerMonth, 0, 0)
        val calc3 = SalaryCalculator.computeSalary(storedM3.monthlySalary, storedM3.freeLeavesPerMonth, 1, 0) // 1 unpaid leave on 3000 salary = 100 deduction
        assertEquals(java.math.BigDecimal("6000.00"), calc1.estimatedPayableSalary)
        assertEquals(java.math.BigDecimal("2900.00"), calc3.estimatedPayableSalary)

        // 6. Editing or deleting one maid does not accidentally modify another maid's records
        repository.deleteMaidById(id5)
        assertEquals(4, repository.getAllMaidsOnce().size)
        // Maid 1, 2, 3, 4 records remain intact
        assertEquals(AttendanceStatus.FULL_DAY, repository.getRecordForMaidAndDateOnce(id1, date)?.status)
        assertEquals(AttendanceStatus.LEAVE, repository.getRecordForMaidAndDateOnce(id3, date)?.status)

        // 7. A sixth maid can also be added, proving there is no five-maid limit
        val m6 = Maid(name = "Kavita", role = "All-rounder", monthlySalary = 8000.0, freeLeavesPerMonth = 2)
        val id6 = repository.saveMaid(m6)
        assertTrue(id6 > 0)
        assertEquals(5, repository.getAllMaidsOnce().size)
    }

    @Test
    fun testHistoricalSalarySnapshotPreservation() = runBlocking {
        val maid = Maid(name = "SnapshotMaid", monthlySalary = 6000.0, freeLeavesPerMonth = 2)
        val maidId = repository.saveMaid(maid)

        // Month 1: October 2026. 4 leaves recorded.
        // Free leaves = 2, Unpaid = 2. Daily = 200. Deduction = 400. Payable = 5600.
        val snapshotOct = SalarySnapshot(
            maidId = maidId,
            month = "2026-10",
            monthlySalary = 6000.0,
            freeLeavesPerMonth = 2,
            dailyDeductionRate = 200.0,
            totalLeaveDays = 4.0,
            freeLeavesConsumed = 2.0,
            unpaidLeaveDays = 2.0,
            leaveDeduction = 400.0,
            estimatedPayableSalary = 5600.0,
            isFinalized = true,
            finalizedAt = System.currentTimeMillis()
        )
        repository.saveSalarySnapshot(snapshotOct)

        // Now user updates maid's current salary to 9000 and free leaves to 0
        val updatedMaid = maid.copy(id = maidId, monthlySalary = 9000.0, freeLeavesPerMonth = 0)
        repository.saveMaid(updatedMaid)

        // Historical October snapshot MUST be preserved unchanged!
        val preservedOct = repository.getSalarySnapshotOnce(maidId, "2026-10")
        assertNotNull(preservedOct)
        assertEquals(6000.0, preservedOct!!.monthlySalary, 0.001)
        assertEquals(2, preservedOct.freeLeavesPerMonth)
        assertEquals(5600.0, preservedOct.estimatedPayableSalary, 0.001)
        assertTrue(preservedOct.isFinalized)

        // Future/live calculation for November uses new salary 9000.0 and free leaves 0
        val novCalc = SalaryCalculator.computeSalary(
            monthlySalary = updatedMaid.monthlySalary,
            freeLeavesPerMonth = updatedMaid.freeLeavesPerMonth,
            fullDayLeaves = 2,
            halfDayLeaves = 0
        )
        assertEquals(java.math.BigDecimal("9000.00"), novCalc.monthlySalary)
        assertEquals(0, novCalc.freeLeavesAllowed)
        // 9000 / 30 = 300/day. 2 unpaid leaves = 600. Payable = 8400.
        assertEquals(java.math.BigDecimal("8400.00"), novCalc.estimatedPayableSalary)
    }

    @Test
    fun testScopedCsvExport() = runBlocking {
        val m1Id = repository.saveMaid(Maid(name = "MaidOne", monthlySalary = 5000.0, freeLeavesPerMonth = 2))
        val m2Id = repository.saveMaid(Maid(name = "MaidTwo", monthlySalary = 4000.0, freeLeavesPerMonth = 1))

        repository.saveAttendance(m1Id, "2026-10-01", AttendanceStatus.FULL_DAY)
        repository.saveAttendance(m2Id, "2026-10-01", AttendanceStatus.LEAVE)

        // Scoped export for MaidOne only
        val csvMaidOne = repository.exportCsv(scopedMaidId = m1Id)
        assertTrue(csvMaidOne.contains("MaidOne"))
        assertTrue(!csvMaidOne.contains("MaidTwo"))

        // All maids export
        val csvAll = repository.exportCsv(scopedMaidId = null)
        assertTrue(csvAll.contains("MaidOne"))
        assertTrue(csvAll.contains("MaidTwo"))
    }

    @Test
    fun testJsonBackupRestoreWithFreeLeavesAndSnapshots() = runBlocking {
        val maidId = repository.saveMaid(Maid(name = "BackupMaid", role = "Chef", monthlySalary = 6000.0, freeLeavesPerMonth = 3))
        repository.saveAttendance(maidId, "2026-10-15", AttendanceStatus.FULL_DAY, notes = "Diwali bonus day")

        val snapshot = SalarySnapshot(
            maidId = maidId,
            month = "2026-09",
            monthlySalary = 6000.0,
            freeLeavesPerMonth = 3,
            dailyDeductionRate = 200.0,
            totalLeaveDays = 1.0,
            freeLeavesConsumed = 1.0,
            unpaidLeaveDays = 0.0,
            leaveDeduction = 0.0,
            estimatedPayableSalary = 6000.0,
            isFinalized = true,
            finalizedAt = System.currentTimeMillis()
        )
        repository.saveSalarySnapshot(snapshot)

        val json = repository.exportJson()
        assertTrue(json.contains("BackupMaid"))
        assertTrue(json.contains("freeLeavesPerMonth"))
        assertTrue(json.contains("salarySnapshots"))

        // Clear and restore
        attendanceDao.deleteAll()
        salarySnapshotDao.deleteAll()
        maidDao.deleteAll()

        val restoreResult = repository.restoreFromJson(json)
        assertTrue(restoreResult.isSuccess)

        val restoredMaids = repository.getAllMaidsOnce()
        assertEquals(1, restoredMaids.size)
        assertEquals("BackupMaid", restoredMaids[0].name)
        assertEquals(3, restoredMaids[0].freeLeavesPerMonth)

        val restoredSnapshot = repository.getSalarySnapshotOnce(restoredMaids[0].id, "2026-09")
        assertNotNull(restoredSnapshot)
        assertEquals(6000.0, restoredSnapshot!!.estimatedPayableSalary, 0.001)
    }
}
