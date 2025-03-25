package com.example.nicotinetracker.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.preference.PreferenceManager
import java.util.Locale

class LocaleManager(private val context: Context) {

    fun setLocale(languageCode: String): Context {
        // Uložení jazyka do nastavení
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(LANGUAGE_KEY, languageCode).apply()

        // Nastavení nového jazyka
        return updateResources(languageCode)
    }

    fun getLocale(): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(LANGUAGE_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    private fun updateResources(languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = context.resources.configuration
        configuration.setLocale(locale)

        // Pro Android N a vyšší
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(configuration)
        } else {
            // Pro starší verze
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            context
        }
    }

    companion object {
        private const val LANGUAGE_KEY = "app_language"
        const val DEFAULT_LANGUAGE = "en"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_CZECH = "cs"
    }
}
