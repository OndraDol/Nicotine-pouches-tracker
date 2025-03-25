package com.example.nicotinetracker

import android.app.Application
import com.example.nicotinetracker.utils.CrashReportingTree
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NicotineTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Konfigurace Timber pro logování
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            // Produkční strom pro crashreporting
            Timber.plant(CrashReportingTree())
        }

        // Konfigurace Firebase
        configureFirebase()
    }

    private fun configureFirebase() {
        // Nastavení Firebase Analytics
        FirebaseAnalytics.getInstance(this).apply {
            // Povolit/zakázat automatický sběr dat
            setAnalyticsCollectionEnabled(true)
        }

        // Nastavení Firebase Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            // Povolit/zakázat crash reporting
            setCrashlyticsCollectionEnabled(true)

            // Nastavení vlastních klíčů pro lepší diagnostiku
            setCustomKey("app_version", BuildConfig.VERSION_NAME)
            setCustomKey("build_type", BuildConfig.BUILD_TYPE)

            // Volitelné: nastavení uživatelského ID
            // setUserId(getUserId())
        }
    }

    // Ukázka metody pro nastavení uživatelského ID
    private fun getUserId(): String {
        // Implementujte logiku pro získání uživatelského ID
        return "anonymous_user"
    }
}
