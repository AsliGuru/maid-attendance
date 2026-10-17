package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getRecordsForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getRecordsForDateOnce(date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%'")
    fun getRecordsForMonth(monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date LIKE :monthPrefix || '%'")
    suspend fun getRecordsForMonthOnce(monthPrefix: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE maid_id = :maidId AND date LIKE :monthPrefix || '%'")
    fun getRecordsForMaidAndMonth(maidId: Long, monthPrefix: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE maid_id = :maidId AND date LIKE :monthPrefix || '%'")
    suspend fun getRecordsForMaidAndMonthOnce(maidId: Long, monthPrefix: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE maid_id = :maidId AND date = :date LIMIT 1")
    fun getRecordForMaidAndDate(maidId: Long, date: String): Flow<AttendanceRecord?>

    @Query("SELECT * FROM attendance_records WHERE maid_id = :maidId AND date = :date LIMIT 1")
    suspend fun getRecordForMaidAndDateOnce(maidId: Long, date: String): AttendanceRecord?

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records ORDER BY date DESC")
    suspend fun getAllRecordsOnce(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: AttendanceRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<AttendanceRecord>): List<Long>

    @Query("DELETE FROM attendance_records WHERE maid_id = :maidId AND date = :date")
    suspend fun deleteRecord(maidId: Long, date: String): Int

    @Query("DELETE FROM attendance_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long): Int

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll(): Int
}
