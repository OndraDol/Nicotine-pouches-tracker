package com.example.nicotinetracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nicotinetracker.MainActivity
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate
import java.time.ZoneOffset

class LimitNotificationService(
    private val context: Context, 
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val preferenceManager = PreferenceManager(context)
    private val database = PouchDatabase.getDatabase(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Kontrola denního limitu
            val dailyLimit = preferenceManager.getDailyLimit()
            val endTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val startTime = endTime - (24L * 60 * 60 * 1000) // 24 hodin zpět

            val currentDayPouchCount = database.pouchDao().countPouchesInTimeRange(startTime, endTime)

            // Vytvoření notifikačních kanálů
            createNotificationChannels()

            // Notifikace podle stavu spotřeby
            when {
                currentDayPouchCount >= dailyLimit -> {
                    // Limit překročen
                    sendLimitExceededNotification(currentDayPouchCount, dailyLimit)
                }
                currentDayPouchCount >= dailyLimit - 2 -> {
                    // Blížící se limit
                    sendApproachingLimitNotification(currentDayPouchCount, dailyLimit)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Kanál pro upozornění na limit
            val limitChannel = NotificationChannel(
                "limit_notifications",
                "Limit Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications about nicotine pouch consumption limit"
            }

            // Kanál pro motivační notifikace
            val motivationChannel = NotificationChannel(
                "motivation_notifications",
                "Motivation Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Motivational notifications to help reduce consumption"
            }

            notificationManager.createNotificationChannels(listOf(limitChannel, motivationChannel))
        }
    }

    private fun sendLimitExceededNotification(currentCount: Int, dailyLimit: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "limit_notifications")
            .setSmallIcon(R.drawable.ic_limit_exceeded)
            .setContentTitle("Denní limit překročen")
            .setContentText("Spotřebovali jste $currentCount sáčků z povoleného limitu $dailyLimit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(LIMIT_EXCEEDED_NOTIFICATION_ID, notification)
    }

    private fun sendApproachingLimitNotification(currentCount: Int, dailyLimit: Int) {
        val remainingPouches = dailyLimit - currentCount

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, "limit_notifications")
            .setSmallIcon(R.drawable.ic_warning)
            .setContentTitle("Blížící se denní limit")
            .setContentText("Zbývá vám již jen $remainingPouches sáčků z denního limitu $dailyLimit.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(APPROACHING_LIMIT_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val LIMIT_EXCEEDED_NOTIFICATION_ID = 1001
        private const val APPROACHING_LIMIT_NOTIFICATION_ID = 1002
    }
}
