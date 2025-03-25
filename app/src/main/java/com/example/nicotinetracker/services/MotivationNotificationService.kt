package com.example.nicotinetracker.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.utils.DataAnalyzer
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.absoluteValue

/**
 * Služba pro zasílání motivačních notifikací.
 */
class MotivationNotificationService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private lateinit var preferenceManager: PreferenceManager
    private val random = Random()
    
    companion object {
        private const val MOTIVATION_CHANNEL_ID = "motivation_notification_channel"
        private const val MOTIVATION_NOTIFICATION_ID = 2001
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onCreate() {
        super.onCreate()
        preferenceManager = PreferenceManager(this)
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Spustíme plánovač notifikací
        serviceScope.launch {
            // Počkáme na vhodnou dobu (např. odpoledne)
            scheduleMotivationNotification()
        }
        
        return START_STICKY
    }
    
    /**
     * Naplánování motivační notifikace.
     */
    private suspend fun scheduleMotivationNotification() {
        // Pro jednoduchost posíláme notifikaci každých 24 hodin (v reálné aplikaci by bylo
        // vhodné implementovat více sofistikovaný algoritmus)
        while (true) {
            if (preferenceManager.notificationsEnabled) {
                sendMotivationNotification()
            }
            
            // Čekáme 24 hodin
            delay(24 * 60 * 60 * 1000L)
        }
    }
    
    /**
     * Odeslání motivační notifikace.
     */
    private suspend fun sendMotivationNotification() {
        val database = PouchDatabase.getDatabase(applicationContext)
        val dataAnalyzer = DataAnalyzer(database)
        
        // Získání dat o trendu spotřeby
        val forecast = dataAnalyzer.forecastConsumption()
        
        // Výběr vhodné motivační zprávy podle trendu
        val (title, message) = when {
            forecast.trend == DataAnalyzer.TrendDirection.DECREASING -> {
                Pair(
                    "Skvělá práce!",
                    "Tvá spotřeba klesá. Pokračuj tímto tempem!"
                )
            }
            forecast.trend == DataAnalyzer.TrendDirection.INCREASING -> {
                Pair(
                    "Neztrácej motivaci",
                    "Zkus dnes omezit spotřebu sáčků a dostat se zpět na správnou cestu."
                )
            }
            else -> {
                // Náhodný výběr obecné motivační zprávy
                val messages = listOf(
                    Pair("Denní tip", "Zkus dnes vydržet alespoň 2 hodiny mezi jednotlivými sáčky."),
                    Pair("Dobrá rada", "Plánuj si dopředu, kdy si dáš sáček, a dodržuj tento plán."),
                    Pair("Věděl(a) jsi?", "Postupné snižování nikotinu zvyšuje šanci na úspěch."),
                    Pair("Malé kroky", "I malé snížení spotřeby je krokem správným směrem.")
                )
                messages[random.nextInt(messages.size)]
            }
        }
        
        // Odeslání notifikace
        val notificationId = System.currentTimeMillis().absoluteValue.toInt() % 10000
        
        val notificationBuilder = NotificationCompat.Builder(this, MOTIVATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
    
    /**
     * Vytvoření notifikačního kanálu.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Motivační zprávy"
            val channelDescription = "Motivační zprávy pro podporu snižování spotřeby"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            
            val channel = NotificationChannel(MOTIVATION_CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Zrušíme všechny coroutines při ukončení služby
        serviceScope.launch {
            Job().cancel()
        }
    }
}