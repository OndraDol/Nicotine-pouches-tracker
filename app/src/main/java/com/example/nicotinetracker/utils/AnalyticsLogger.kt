package com.example.nicotinetracker.utils

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsLogger(private val context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    // Logování uživatelských akcí
    fun logAction(actionName: String, params: Map<String, String> = emptyMap()) {
        val bundle = Bundle().apply {
            params.forEach { (key, value) ->
                putString(key, value)
            }
        }
        firebaseAnalytics.logEvent(actionName, bundle)
    }

    // Sledování obrazovek
    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    // Sledování uživatelských výzev
    fun logChallengeEvent(
        challengeName: String, 
        status: ChallengeStatus, 
        points: Int
    ) {
        val bundle = Bundle().apply {
            putString("challenge_name", challengeName)
            putString("challenge_status", status.name)
            putInt("challenge_points", points)
        }
        firebaseAnalytics.logEvent("challenge_completed", bundle)
    }

    // Sledování spotřeby
    fun logConsumptionEvent(
        brandName: String, 
        nicotineStrength: Float, 
        dailyLimit: Int
    ) {
        val bundle = Bundle().apply {
            putString("brand", brandName)
            putFloat("nicotine_strength", nicotineStrength)
            putInt("daily_limit", dailyLimit)
        }
        firebaseAnalytics.logEvent("pouch_consumed", bundle)
    }

    // Sledování exportu/importu dat
    fun logDataManagementEvent(
        eventType: DataManagementType, 
        success: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("event_type", eventType.name)
            putBoolean("success", success)
        }
        firebaseAnalytics.logEvent("data_management", bundle)
    }

    // Nastavení ID uživatele (pro personalizované analýzy)
    fun setUserId(userId: String) {
        firebaseAnalytics.setUserId(userId)
    }

    // Nastavení vlastních parametrů
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
    }
}

// Enums pro kategorické události
enum class ChallengeStatus {
    STARTED, COMPLETED, FAILED
}

enum class DataManagementType {
    EXPORT, IMPORT, BACKUP, RESTORE
}
