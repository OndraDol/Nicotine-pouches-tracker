package com.example.nicotinetracker.managers.adaptive

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.utils.DataAnalyzer
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.coroutineScope

class AdaptiveLimitManager(
    private val context: Context, 
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val dataAnalyzer = DataAnalyzer(context)
    private val preferenceManager = PreferenceManager(context)
    private val database = PouchDatabase.getDatabase(context)

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Analýza aktuální spotřeby
            val avgConsumption = dataAnalyzer.getAverageDailyConsumption()
            val recommendedLimit = dataAnalyzer.recommendOptimalLimit()

            // Uložení doporučení
            preferenceManager.setAdaptiveRecommendedLimit(recommendedLimit)

            // Volitelná notifikace
            if (preferenceManager.isAdaptiveLimitNotificationEnabled()) {
                sendRecommendationNotification(recommendedLimit)
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun sendRecommendationNotification(recommendedLimit: Int) {
        // Implementace notifikace s doporučením limitu
    }

    // Manuální doporučení limitu
    fun getReductionPlan(): ReductionPlan {
        val currentLimit = preferenceManager.getDailyLimit()
        val recommendedLimit = dataAnalyzer.recommendOptimalLimit()

        return when {
            recommendedLimit < currentLimit -> ReductionPlan(
                currentLimit = currentLimit,
                recommendedLimit = recommendedLimit,
                reductionStrategy = when {
                    currentLimit - recommendedLimit <= 1 -> ReductionStrategy.GRADUAL
                    currentLimit - recommendedLimit <= 3 -> ReductionStrategy.MODERATE
                    else -> ReductionStrategy.AGGRESSIVE
                }
            )
            else -> ReductionPlan(
                currentLimit = currentLimit,
                recommendedLimit = currentLimit,
                reductionStrategy = ReductionStrategy.MAINTAIN
            )
        }
    }
}

// Datová struktura pro plán redukce
data class ReductionPlan(
    val currentLimit: Int,
    val recommendedLimit: Int,
    val reductionStrategy: ReductionStrategy
)

enum class ReductionStrategy {
    AGGRESSIVE,  // Významné snížení
    MODERATE,   // Postupné snížení
    GRADUAL,    // Jemné snížení
    MAINTAIN    // Udržení aktuálního stavu
}
