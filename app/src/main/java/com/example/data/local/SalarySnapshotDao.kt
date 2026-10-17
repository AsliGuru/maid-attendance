package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.SalarySnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface SalarySnapshotDao {
    @Query("SELECT * FROM salary_snapshots WHERE maid_id = :maidId AND month = :month LIMIT 1")
    fun getSnapshot(maidId: Long, month: String): Flow<SalarySnapshot?>

    @Query("SELECT * FROM salary_snapshots WHERE maid_id = :maidId AND month = :month LIMIT 1")
    suspend fun getSnapshotOnce(maidId: Long, month: String): SalarySnapshot?

    @Query("SELECT * FROM salary_snapshots WHERE month = :month")
    fun getSnapshotsForMonth(month: String): Flow<List<SalarySnapshot>>

    @Query("SELECT * FROM salary_snapshots WHERE month = :month")
    suspend fun getSnapshotsForMonthOnce(month: String): List<SalarySnapshot>

    @Query("SELECT * FROM salary_snapshots")
    suspend fun getAllSnapshotsOnce(): List<SalarySnapshot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(snapshot: SalarySnapshot): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(snapshots: List<SalarySnapshot>): List<Long>

    @Query("DELETE FROM salary_snapshots WHERE maid_id = :maidId AND month = :month")
    suspend fun deleteSnapshot(maidId: Long, month: String): Int

    @Query("DELETE FROM salary_snapshots")
    suspend fun deleteAll(): Int
}
