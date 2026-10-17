package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.AttendanceRecord
import com.example.data.model.Maid
import com.example.data.model.SalarySnapshot

@Database(
    entities = [Maid::class, AttendanceRecord::class, SalarySnapshot::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun maidDao(): MaidDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun salarySnapshotDao(): SalarySnapshotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create maids_new with revised schema (free_leaves_per_month instead of expected_working_days)
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS maids_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        role TEXT NOT NULL,
                        monthly_salary REAL NOT NULL,
                        free_leaves_per_month INTEGER NOT NULL DEFAULT 2,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                // 2. Copy existing data from maids into maids_new
                db.execSQL(
                    """
                    INSERT INTO maids_new (id, name, role, monthly_salary, free_leaves_per_month, created_at)
                    SELECT id, name, role, monthly_salary, 2, created_at FROM maids
                    """.trimIndent()
                )

                // 3. Backup attendance records temporarily so dropping maids cannot trigger cascade deletion
                db.execSQL("CREATE TABLE IF NOT EXISTS attendance_records_backup AS SELECT * FROM attendance_records")

                // 4. Swap maids table
                db.execSQL("DROP TABLE maids")
                db.execSQL("ALTER TABLE maids_new RENAME TO maids")

                // 5. Restore any attendance records if needed and drop temp backup
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO attendance_records (id, maid_id, date, status, notes, created_at, updated_at)
                    SELECT id, maid_id, date, status, notes, created_at, updated_at FROM attendance_records_backup
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE attendance_records_backup")

                // 6. Create salary_snapshots table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS salary_snapshots (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        maid_id INTEGER NOT NULL,
                        month TEXT NOT NULL,
                        monthly_salary REAL NOT NULL,
                        free_leaves_per_month INTEGER NOT NULL,
                        daily_deduction_rate REAL NOT NULL,
                        total_leave_days REAL NOT NULL,
                        free_leaves_consumed REAL NOT NULL,
                        unpaid_leave_days REAL NOT NULL,
                        leave_deduction REAL NOT NULL,
                        estimated_payable_salary REAL NOT NULL,
                        is_finalized INTEGER NOT NULL DEFAULT 1,
                        finalized_at INTEGER NOT NULL,
                        FOREIGN KEY(maid_id) REFERENCES maids(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_salary_snapshots_maid_id_month ON salary_snapshots(maid_id, month)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_salary_snapshots_maid_id ON salary_snapshots(maid_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_salary_snapshots_month ON salary_snapshots(month)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "maid_attendance_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
