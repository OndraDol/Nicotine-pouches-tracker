package com.example.nicotinetracker.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class CrashReportingTree : Timber.Tree() {
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Logování do Logcat
        when (priority) {
            Log.ERROR -> {
                // Odeslání do Crashlytics
                t?.let { 
                    FirebaseCrashlytics.getInstance().recordException(it)
                }
                
                // Pokud není výjimka, vytvoříme vlastní
                if (t == null) {
                    FirebaseCrashlytics.getInstance().recordException(
                        Exception("Error: $message")
                    )
                }
            }
            Log.WARN -> {
                // Méně závažné chyby
                FirebaseCrashlytics.getInstance().log(message)
            }
        }
    }
}

// Rozšířený error handler
object AdvancedErrorHandler {
    fun handleError(
        context: Context, 
        error: Throwable, 
        userMessage: String? = null
    ) {
        // Zaznamenání do Crashlytics
        FirebaseCrashlytics.getInstance().recordException(error)

        // Volitelná uživatelská zpráva
        userMessage?.let {
            FirebaseCrashlytics.getInstance().log(it)
        }

        // Dodatečné vlastní atributy
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("error_message", error.message ?: "Unknown error")
            setCustomKey("error_type", error.javaClass.simpleName)
        }

        // Případné zobrazení uživatelské notifikace
        showErrorNotification(context, userMessage ?: error.localizedMessage)
    }

    private fun showErrorNotification(context: Context, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Vytvoření notifikačního kanálu pro chyby
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "error_channel", 
                "Systémové chyby", 
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Vytvoření notifikace
        val notification = NotificationCompat.Builder(context, "error_channel")
            .setContentTitle("Došlo k chybě")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_error)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Zobrazení notifikace
        notificationManager.notify(ERROR_NOTIFICATION_ID, notification)
    }

    private const val ERROR_NOTIFICATION_ID = 9999
}
