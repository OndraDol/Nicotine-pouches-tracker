package com.example.nicotinetracker.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.nicotinetracker.MainActivity
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.Pouch
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Widget pro rychlé přidání spotřeby a zobrazení statistik
 */
class PouchCounterWidget : AppWidgetProvider() {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val ACTION_ADD_POUCH = "com.example.nicotinetracker.widgets.ACTION_ADD_POUCH"
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_ADD_POUCH) {
            // Přidání nového sáčku
            coroutineScope.launch {
                val database = PouchDatabase.getDatabase(context)
                val pouch = Pouch(timestamp = System.currentTimeMillis())
                database.pouchDao().insert(pouch)

                // Aktualizace widgetu
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val thisWidget = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                
                if (thisWidget != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    updateAppWidget(context, appWidgetManager, thisWidget)
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Pro každý widget, který patří do této provider
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.pouch_counter_widget)

        // Nastavení textu aktuálního data
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val currentDate = dateFormat.format(Calendar.getInstance().time)
        views.setTextViewText(R.id.widget_date, currentDate)

        // Vytvoření PendingIntent pro akci přidání sáčku
        val addPouchIntent = Intent(context, PouchCounterWidget::class.java).apply {
            action = ACTION_ADD_POUCH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        
        val pendingFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        
        val addPouchPendingIntent = PendingIntent.getBroadcast(
            context, 
            appWidgetId, 
            addPouchIntent, 
            pendingFlag
        )
        
        views.setOnClickPendingIntent(R.id.widget_add_button, addPouchPendingIntent)

        // Vytvoření PendingIntent pro otevření aplikace
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            openAppIntent, 
            pendingFlag
        )
        
        views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

        // Aktualizace dat ve widgetu
        coroutineScope.launch {
            // Získání denního počtu sáčků a limitu
            val database = PouchDatabase.getDatabase(context)
            val preferenceManager = PreferenceManager(context)
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis
            
            val todayCount = database.pouchDao().countPouchesInTimeRange(startOfDay, endOfDay)
            val dailyLimit = preferenceManager.getDailyLimit()
            val remaining = dailyLimit - todayCount
            
            // Aktualizace UI
            val counterText = "$todayCount / $dailyLimit"
            val remainingText = if (remaining > 0) {
                context.getString(R.string.remaining_count, remaining, dailyLimit)
            } else {
                context.getString(R.string.limit_exceeded)
            }
            
            appWidgetManager.updateAppWidget(appWidgetId, views.apply {
                setTextViewText(R.id.widget_counter, counterText)
                setTextViewText(R.id.widget_remaining, remainingText)
            })
        }
    }
}