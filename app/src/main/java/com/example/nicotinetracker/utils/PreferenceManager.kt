package com.example.nicotinetracker.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class PreferenceManager(context: Context) {

    companion object {
        private const val PREF_NAME = "nicotine_tracker_prefs"
        private const val KEY_DAILY_LIMIT = "daily_limit"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_THEME_MODE = "theme_mode"

        // Konstanty pro režimy motivu
        const val THEME_LIGHT = 1
        const val THEME_DARK = 2
        const val THEME_SYSTEM = 0

        // Výchozí hodnoty
        private const val DEFAULT_DAILY_LIMIT = 10
        private const val DEFAULT_NOTIFICATIONS_ENABLED = true
        private const val DEFAULT_THEME_MODE = THEME_SYSTEM
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Denní limit
    var dailyLimit: Int
        get() = preferences.getInt(KEY_DAILY_LIMIT, DEFAULT_DAILY_LIMIT)
        set(value) = preferences.edit().putInt(KEY_DAILY_LIMIT, value).apply()

    // Notifikace povoleny
    var notificationsEnabled: Boolean
        get() = preferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, DEFAULT_NOTIFICATIONS_ENABLED)
        set(value) = preferences.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    // Režim motivu
    var themeMode: Int
        get() = preferences.getInt(KEY_THEME_MODE, DEFAULT_THEME_MODE)
        set(value) {
            preferences.edit().putInt(KEY_THEME_MODE, value).apply()
            applyTheme(value)
        }

    // Aplikuje vybraný motiv
    fun applyTheme(mode: Int) {
        when (mode) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}