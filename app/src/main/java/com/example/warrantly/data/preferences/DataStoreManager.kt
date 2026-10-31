package com.example.warrantly.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "warrantly_prefs")

class DataStoreManager(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val GLOBAL_REMINDERS_ENABLED = booleanPreferencesKey("global_reminders_enabled")
        val REMINDER_DAYS_BEFORE = stringPreferencesKey("reminder_days_before")
        val EXPIRING_SOON_THRESHOLD_DAYS = intPreferencesKey("expiring_soon_threshold_days")
        val DEFAULT_CURRENCY_CODE = stringPreferencesKey("default_currency_code")
    }

    // Flows

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "SYSTEM"
    }

    val globalRemindersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.GLOBAL_REMINDERS_ENABLED] ?: true
    }

    val reminderDaysBefore: Flow<List<Int>> = context.dataStore.data.map { prefs ->
        val raw = prefs[Keys.REMINDER_DAYS_BEFORE] ?: "30,7,1"
        raw.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it > 0 }
            .ifEmpty { listOf(30, 7, 1) }
    }

    val expiringSoonThresholdDays: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.EXPIRING_SOON_THRESHOLD_DAYS] ?: 30
    }

    val defaultCurrencyCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEFAULT_CURRENCY_CODE] ?: "USD"
    }

    // Setters

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    suspend fun setGlobalRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.GLOBAL_REMINDERS_ENABLED] = enabled
        }
    }

    suspend fun setReminderDaysBefore(days: List<Int>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REMINDER_DAYS_BEFORE] = days.joinToString(",")
        }
    }

    suspend fun setExpiringSoonThresholdDays(days: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EXPIRING_SOON_THRESHOLD_DAYS] = days
        }
    }

    suspend fun setDefaultCurrencyCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEFAULT_CURRENCY_CODE] = code
        }
    }
}
