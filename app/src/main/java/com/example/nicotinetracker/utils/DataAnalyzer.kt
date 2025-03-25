package com.example.nicotinetracker.utils

import android.content.Context
import com.example.nicotinetracker.data.PouchDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.roundToInt

class DataAnalyzer(private val context: Context) {
    private val database = PouchDatabase.getDatabase(context)
    private val pouchDao = database.pouchDao()

    // Analýza spotřeby za měsíc
    suspend fun getMonthlyConsumptionTrend(): Map<String, Int> {
        return withContext(Dispatchers.IO) {
            val endTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val startTime = endTime - (30L * 24 * 60 * 60 * 1000) // 30 dní zpět

            pouchDao.getPouchCountByDay(startTime, endTime)
                .associate { it.day to it.count }
        }
    }

    // Výpočet průměrné denní spotřeby
    suspend fun getAverageDailyConsumption(): Double {
        return withContext(Dispatchers.IO) {
            val endTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val startTime = endTime - (30L * 24 * 60 * 60 * 1000) // 30 dní zpět

            val totalPouches = pouchDao.countPouchesInTimeRange(startTime, endTime)
            val days = 30 // fixní perioda pro průměr

            (totalPouches.toDouble() / days).roundToInt().toDouble()
        }
    }

    // Analýza spotřeby podle značek
    suspend fun getBrandConsumptionBreakdown(): Map<String, Int> {
        return withContext(Dispatchers.IO) {
            val endTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val startTime = endTime - (30L * 24 * 60 * 60 * 1000) // 30 dní zpět

            val pouchesWithDetails = pouchDao.getPouchesInTimeRangeWithDetails(startTime, endTime)
            
            pouchesWithDetails
                .mapNotNull { it.brandName ?: it.customBrand }
                .groupingBy { it }
                .eachCount()
        }
    }

    // Výpočet celkového příjmu nikotinu
    suspend fun getTotalNicotineIntake(): Float {
        return withContext(Dispatchers.IO) {
            val endTime = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            val startTime = endTime - (30L * 24 * 60 * 60 * 1000) // 30 dní zpět

            pouchDao.getTotalNicotineInTimeRange(startTime, endTime) ?: 0f
        }
    }

    // Predikce budoucí spotřeby
    suspend fun predictFutureConsumption(): Int {
        val avgDailyConsumption = getAverageDailyConsumption()
        
        // Jednoduchý prediktivní model - průměr za posledních 30 dní
        return avgDailyConsumption.roundToInt()
    }

    // Doporučení optimálního limitu
    suspend fun recommendOptimalLimit(): Int {
        val avgConsumption = getAverageDailyConsumption()
        
        // Doporučení: aktuální průměr + 1, maximálně 10
        return minOf(avgConsumption.roundToInt() + 1, 10)
    }
}
