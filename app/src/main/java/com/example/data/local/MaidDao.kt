package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Maid
import kotlinx.coroutines.flow.Flow

@Dao
interface MaidDao {
    @Query("SELECT * FROM maids ORDER BY name ASC")
    fun getAllMaids(): Flow<List<Maid>>

    @Query("SELECT * FROM maids ORDER BY name ASC")
    suspend fun getAllMaidsOnce(): List<Maid>

    @Query("SELECT * FROM maids WHERE id = :id LIMIT 1")
    fun getMaidById(id: Long): Flow<Maid?>

    @Query("SELECT * FROM maids WHERE id = :id LIMIT 1")
    suspend fun getMaidByIdOnce(id: Long): Maid?

    @Query("SELECT COUNT(*) FROM maids")
    suspend fun getMaidCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaid(maid: Maid): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(maids: List<Maid>): List<Long>

    @Update
    suspend fun updateMaid(maid: Maid): Int

    @Delete
    suspend fun deleteMaid(maid: Maid): Int

    @Query("DELETE FROM maids WHERE id = :id")
    suspend fun deleteMaidById(id: Long): Int

    @Query("DELETE FROM maids")
    suspend fun deleteAll(): Int
}
