package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.local.AppDatabase
import com.example.data.preferences.AppPreferences
import com.example.data.repository.MaidAttendanceRepository
import com.example.ui.MaidAttendanceApp
import com.example.ui.MaidAttendanceViewModel
import com.example.ui.theme.MaidAttendanceTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MaidAttendanceViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val preferences = AppPreferences(applicationContext)
        val repository = MaidAttendanceRepository(
            maidDao = database.maidDao(),
            attendanceDao = database.attendanceDao(),
            salarySnapshotDao = database.salarySnapshotDao(),
            preferences = preferences
        )
        MaidAttendanceViewModel.Factory(repository, preferences)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaidAttendanceTheme {
                MaidAttendanceApp(viewModel = viewModel)
            }
        }
    }
}
