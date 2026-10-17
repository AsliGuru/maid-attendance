package com.example.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "maid_attendance_prefs")

class AppPreferences(private val context: Context) {
    companion object {
        private val KEY_IS_SEEDED = booleanPreferencesKey("is_seeded")
        private val KEY_SELECTED_MAID_ID = longPreferencesKey("selected_maid_id")
        private val KEY_SELECTED_DATE = stringPreferencesKey("selected_date")
    }

    val isSeeded: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_IS_SEEDED] ?: false
    }

    suspend fun setSeeded(seeded: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_SEEDED] = seeded
        }
    }

    val selectedMaidId: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_MAID_ID]
    }

    suspend fun setSelectedMaidId(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_MAID_ID] = id
        }
    }

    val selectedDate: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_DATE]
    }

    suspend fun setSelectedDate(date: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SELECTED_DATE] = date
        }
    }
}
