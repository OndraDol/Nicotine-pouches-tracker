package com.example.nicotinetracker.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("NicotineTrackerPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val DAILY_LIMIT_KEY = "daily_limit"
        private const val DEFAULT_DAILY_LIMIT = 10
        private const val NOTIFICATIONS_ENABLED_KEY = "notifications_enabled"
        private const val LAST_ACHIEVEMENT_CHECK_TIME_KEY = "last_achievement_check_time"
    }

    fun setDailyLimit(limit: Int) {
        prefs.edit().putInt(DAILY_LIMIT_KEY, limit).apply()
    }

    fun getDailyLimit(): Int {
        return prefs.getInt(DAILY_LIMIT_KEY, DEFAULT_DAILY_LIMIT)
    }
    
    val dailyLimit: Int
        get() = getDailyLimit()
    
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(NOTIFICATIONS_ENABLED_KEY, true)
        set(value) = prefs.edit().putBoolean(NOTIFICATIONS_ENABLED_KEY, value).apply()
        
    fun getLastAchievementCheckTime(): Long {
        return prefs.getLong(LAST_ACHIEVEMENT_CHECK_TIME_KEY, 0)
    }
    
    fun setLastAchievementCheckTime(time: Long) {
        prefs.edit().putLong(LAST_ACHIEVEMENT_CHECK_TIME_KEY, time).apply()
    }
}
