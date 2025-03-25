package com.example.nicotinetracker.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.utils.DataAnalyzer
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Random
import kotlin.math.absoluteValue

/**
 * Worker pro zasílání motivačních notifikací.
 * Spouští se jednou denně v odpoledních hodinách a posílá motivační zprávy
 * podle aktuálního stavu uživatele.
 */
class MotivationNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val preferenceManager = PreferenceManager(context)
    private val database = PouchDatabase.getDatabase(context)
    private val dataAnalyzer = DataAnalyzer(database)
    private val random = Random()
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Kontrola, zda jsou notifikace povoleny
            if (!preferenceManager.notificationsEnabled) {
                return@withContext Result.success()
            }
            
            // Vytvoření kanálu pro notifikace (pro Android 8.0+)
            createNotificationChannelIfNeeded()
            
            // Výběr typu notifikace
            val notificationType = selectNotificationType()
            
            // Získání dat pro vybraný typ notifikace
            val (title, message) = getMotivationContent(notificationType)
            
            // Odeslání notifikace
            sendNotification(title, message)
            
            return@withContext Result.success()
        } catch (e: Exception) {
            // V případě chyby se znovu pokusíme při dalším spuštění
            return@withContext Result.retry()
        }
    }
    
    /**
     * Výběr typu notifikace na základě současného stavu uživatele.
     */
    private suspend fun selectNotificationType(): MotivationMessageType {
        // Získání dat o trendu spotřeby
        val forecast = dataAnalyzer.forecastConsumption()
        
        // Získání dat o dodržování limitu
        val progressScore = dataAnalyzer.calculateProgressScore(preferenceManager.getDailyLimit())
        
        return when {
            // Pokud uživatel snižuje spotřebu, pochval ho
            forecast.trend == DataAnalyzer.TrendDirection.DECREASING && 
                    forecast.estimatedReduction > 10.0 -> {
                MotivationMessageType.REDUCTION_PRAISE
            }
            
            // Pokud uživatel dobře dodržuje limit, pochval ho
            progressScore.compliancePercentage >= 80.0 -> {
                MotivationMessageType.LIMIT_COMPLIANCE
            }
            
            // Pokud uživatel zvyšuje spotřebu, motivuj ho ke snížení
            forecast.trend == DataAnalyzer.TrendDirection.INCREASING -> {
                MotivationMessageType.REDUCTION_SUGGESTION
            }
            
            // V ostatních případech pošli obecnou motivační zprávu
            else -> {
                MotivationMessageType.GENERAL_MOTIVATION
            }
        }
    }
    
    /**
     * Získání obsahu notifikace podle typu.
     */
    private suspend fun getMotivationContent(type: MotivationMessageType): Pair<String, String> {
        val title: String
        val message: String
        
        when (type) {
            MotivationMessageType.REDUCTION_PRAISE -> {
                val forecast = dataAnalyzer.forecastConsumption()
                title = "Výborná práce!"
                message = "Tvá spotřeba klesla o %.1f%%! Pokračuj ve svém úsilí.".format(
                    forecast.estimatedReduction
                )
            }
            
            MotivationMessageType.LIMIT_COMPLIANCE -> {
                val progressScore = dataAnalyzer.calculateProgressScore(preferenceManager.getDailyLimit())
                title = "Disciplína se vyplácí!"
                message = "Dodržuješ svůj limit v %.1f%% dní. Jen tak dál!".format(
                    progressScore.compliancePercentage
                )
            }
            
            MotivationMessageType.REDUCTION_SUGGESTION -> {
                // Zkus navrhnout snížení limitu o 1 sáček
                val currentLimit = preferenceManager.getDailyLimit()
                val suggestedLimit = currentLimit - 1
                
                title = "Co zkusit novou výzvu?"
                message = if (suggestedLimit > 0) {
                    "Co takhle nastavit si nový limit $suggestedLimit sáčků denně? Malá změna, velký krok!"
                } else {
                    "Zkus omezit večerní sáčky. Může to pomoci snížit celkovou spotřebu."
                }
            }
            
            MotivationMessageType.GENERAL_MOTIVATION -> {
                // Náhodný výběr obecné motivační zprávy
                val messages = listOf(
                    Pair("Motivace na dnešek", "Každý den bez překročení limitu je krokem ke zdravějšímu životnímu stylu."),
                    Pair("Věděl(a) jsi?", "Postupné snižování dávky nikotinu zvyšuje šanci na dlouhodobé snížení závislosti."),
                    Pair("Tip na dnešek", "Zkus si najít alternativní aktivitu, když máš chuť na sáček mimo plánovaný čas."),
                    Pair("Denní výzva", "Zkus dnes vydržet alespoň 2 hodiny mezi jednotlivými sáčky.")
                )
                
                val randomIndex = random.nextInt(messages.size)
                val randomMessage = messages[randomIndex]
                title = randomMessage.first
                message = randomMessage.second
            }
        }
        
        return Pair(title, message)
    }
    
    /**
     * Vytvoření notifikačního kanálu (pro Android 8.0+).
     */
    private fun createNotificationChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = MOTIVATION_CHANNEL_ID
            val channelName = "Motivační zprávy"
            val channelDescription = "Motivační zprávy pro podporu snižování spotřeby"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            
            val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Odeslání notifikace.
     */
    private fun sendNotification(title: String, message: String) {
        val notificationId = System.currentTimeMillis().absoluteValue.toInt() % 10000
        
        val notificationBuilder = NotificationCompat.Builder(applicationContext, MOTIVATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    companion object {
        // Tag pro identifikaci workeru
        const val WORK_NAME = "motivation_notification_worker"
        
        // ID notifikačního kanálu
        private const val MOTIVATION_CHANNEL_ID = "motivation_notification_channel"
    }
}

/**
 * Typy motivačních zpráv.
 */
enum class MotivationMessageType {
    // Pochvala za snížení spotřeby
    REDUCTION_PRAISE,
    
    // Pochvala za dodržování limitu
    LIMIT_COMPLIANCE,
    
    // Návrhy na snížení spotřeby
    REDUCTION_SUGGESTION,
    
    // Obecná motivační zpráva
    GENERAL_MOTIVATION
}