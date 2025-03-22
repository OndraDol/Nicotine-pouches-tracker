package com.example.nicotinetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nicotinetracker.data.PouchDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StatistikyFragment : Fragment() {

    private lateinit var tvTodayTotal: TextView
    private lateinit var tvWeekTotal: TextView
    private lateinit var tvMonthTotal: TextView
    private lateinit var tvDailyAverage: TextView
    private lateinit var rvHistory: RecyclerView
    private val historyAdapter = HistoryAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistiky, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        tvTodayTotal = view.findViewById(R.id.tvTodayTotal)
        tvWeekTotal = view.findViewById(R.id.tvWeekTotal)
        tvMonthTotal = view.findViewById(R.id.tvMonthTotal)
        tvDailyAverage = view.findViewById(R.id.tvDailyAverage)
        rvHistory = view.findViewById(R.id.rvHistory)
        
        rvHistory.layoutManager = LinearLayoutManager(context)
        rvHistory.adapter = historyAdapter
        
        loadStatistics()
    }
    
    override fun onResume() {
        super.onResume()
        loadStatistics()
    }
    
    private fun loadStatistics() {
        lifecycleScope.launch(Dispatchers.IO) {
            val database = PouchDatabase.getDatabase(requireContext())
            val calendar = Calendar.getInstance()
            
            // Pro dnešek
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
            
            // Pro týden
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfWeek = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfWeek = calendar.timeInMillis
            
            val weekCount = database.pouchDao().countPouchesInTimeRange(startOfWeek, endOfWeek)
            
            // Pro měsíc
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfMonth = calendar.timeInMillis
            
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfMonth = calendar.timeInMillis
            
            val monthCount = database.pouchDao().countPouchesInTimeRange(startOfMonth, endOfMonth)
            
            // Historie - posledních 20 záznamů
            val history = database.pouchDao().getAllPouches().take(20)
            
            // Výpočet průměru
            val allPouches = database.pouchDao().getAllPouches()
            val dailyAverage = if (allPouches.isNotEmpty()) {
                val firstTimestamp = allPouches.minOfOrNull { it.timestamp } ?: 0
                val lastTimestamp = allPouches.maxOfOrNull { it.timestamp } ?: 0
                val diffInDays = ((lastTimestamp - firstTimestamp) / (1000 * 60 * 60 * 24)) + 1
                if (diffInDays > 0) {
                    String.format("%.1f", allPouches.size.toDouble() / diffInDays)
                } else {
                    allPouches.size.toString()
                }
            } else {
                "0"
            }
            
            withContext(Dispatchers.Main) {
                tvTodayTotal.text = "$todayCount sáčků"
                tvWeekTotal.text = "$weekCount sáčků"
                tvMonthTotal.text = "$monthCount sáčků"
                tvDailyAverage.text = "$dailyAverage sáčků"
                historyAdapter.updateItems(history)
            }
        }
    }
}