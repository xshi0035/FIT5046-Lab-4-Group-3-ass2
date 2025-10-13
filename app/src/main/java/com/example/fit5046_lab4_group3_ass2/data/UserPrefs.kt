package com.example.fit5046_lab4_group3_ass2.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefs {
    private val FIRST_RUN = booleanPreferencesKey("is_first_run")

    /** default = true (first time) */
    suspend fun isFirstRun(context: Context): Boolean {
        val prefs = context.dataStore.data.first()
        return prefs[FIRST_RUN] ?: true
    }

    suspend fun setOnboarded(context: Context) {
        context.dataStore.edit { it[FIRST_RUN] = false }
    }
}
