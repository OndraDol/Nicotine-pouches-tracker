package com.example.nicotinetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nicotinetracker.data.Pouch
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SpotrebaFragment : Fragment() {

    private lateinit var btnAddPouch: MaterialButton
    private lateinit var noteLayout: TextInputLayout
    private lateinit var noteEditText: TextInputEditText
    private lateinit var tvTodayCount: TextView
    private lateinit var tvRemainingCount: TextView

    private lateinit var preferenceManager: PreferenceManager
    private var dailyLimit = 10
    private var todayCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_spotreba, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        dailyLimit = preferenceManager.dailyLimit

        btnAddPouch = view.findViewById(R.id.btnAddPouch)
        noteLayout = view.findViewById(R.id.etNote)
        noteEditText = noteLayout.editText as TextInputEditText
        tvTodayCount = view.findViewById(R.id.tvTodayCount)
        tvRemainingCount = view.findViewById(R.id.tvRemainingCount)

        createNotificationChannel()
        updateCounts()

        btnAddPouch.setOnClickListener {
            addNewPouch()
        }
    }

    override fun onResume() {
        super.onResume()
        dailyLimit = preferenceManager.dailyLimit
        updateCounts()
    }

    private fun addNewPouch() {
        val note = noteEditText.text.toString().trim()
        val pouch = Pouch(
            timestamp = System.currentTimeMillis(),
            note = if (note.isEmpty()) null else note
        )

        lifecycleScope.launch(Dispatchers.IO) {
            val database = PouchDatabase.getDatabase(requireContext())
            database.pouchDao().insert(pouch)

            withContext(Dispatchers.Main) {
                Toast.makeText(context, getString(R.string.pouch_added), Toast.LENGTH_SHORT).show()
                noteEditText.text?.clear()
                updateCounts()
            }
        }
    }

    private fun updateCounts() {
        lifecycleScope.launch(Dispatchers.IO) {
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

            val database = PouchDatabase.getDatabase(requireContext())
            todayCount = database.pouchDao().countPouchesInTimeRange(startOfDay, endOfDay)
            val remaining = dailyLimit - todayCount

            withContext(Dispatchers.Main) {
                tvTodayCount.text = getString(R.string.today_count, todayCount)
                tvRemainingCount.text = getString(R.string.remaining_count, remaining, dailyLimit)

                // Změna barvy textu podle zbývajícího počtu
                when {
                    remaining <= 0 -> {
                        tvRemainingCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.limit_critical))
                    }
                    remaining <= 3 -> {
                        tvRemainingCount.setTextColor(ContextCompat.getColor(requireContext(), R.color.limit_warning))
                        // Poslat notifikaci, pokud jsou povoleny
                        if (preferenceManager.notificationsEnabled && remaining > 0) {
                            sendLimitNotification(remaining)
                        }
                    }
                    else -> {
                        tvRemainingCount.setTextColor(tvTodayCount.currentTextColor)
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "limit_notification_channel"
            val channelName = getString(R.string.notification_channel_name)
            val channelDescription = getString(R.string.notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendLimitNotification(remaining: Int) {
        val channelId = "limit_notification_channel"
        val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(getString(R.string.notification_limit_title))
            .setContentText(getString(R.string.notification_limit_text, remaining))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1001, notificationBuilder.build())
    }
}