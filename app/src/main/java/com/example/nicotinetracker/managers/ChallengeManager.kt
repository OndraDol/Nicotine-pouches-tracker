package com.example.nicotinetracker.managers

import android.content.Context
import androidx.work.*
import com.example.nicotinetracker.data.Challenge
import com.example.nicotinetracker.data.ChallengeDao
import com.example.nicotinetracker.data.ChallengeFactory
import com.example.nicotinetracker.data.PouchDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

class ChallengeManager(private val context: Context) {
    private val database: PouchDatabase = PouchDatabase.getDatabase(context)
    private val challengeDao: ChallengeDao = database.challengeDao()
    private val pouchDao = database.pouchDao()

    suspend fun initializeChallenges() {
        withContext(Dispatchers.IO) {
            // Smazání starých výzev
            challengeDao.deleteExpiredChallenges(LocalDateTime.now())

            // Vygenerování nových výzev
            val shortTermChallenges = ChallengeFactory.generateShortTermChallenges()
            val longTermChallenges = ChallengeFactory.generateLongTermChallenges()
            
            challengeDao.insertAll(shortTermChallenges + longTermChallenges)
        }
    }

    fun getActiveChallenges(): Flow<List<Challenge>> {
        return challengeDao.getActiveChallenges(LocalDateTime.now())
    }

    suspend fun updateChallengeProgress() {
        withContext(Dispatchers.IO) {
            val activeChallenges = challengeDao.getActiveChallenges(LocalDateTime.now())
            
            activeChallenges.collect { challenges ->
                challenges.forEach { challenge ->
                    val progress = calculateChallengeProgress(challenge)
                    
                    if (progress >= challenge.targetValue) {
                        challengeDao.markChallengeAsCompleted(challenge.id)
                        // Zde můžete přidat logiku odměn
                    } else {
                        challengeDao.updateChallengeProgress(challenge.id, progress)
                    }
                }
            }
        }
    }

    private suspend fun calculateChallengeProgress(challenge: Challenge): Int {
        return when (challenge.title) {
            "One Week Limit Control" -> {
                // Počet dní, kdy byl dodržen denní limit
                pouchDao.getDaysWithinDailyLimit(
                    challenge.startDate, 
                    challenge.endDate
                )
            }
            "Gradual Reduction Week" -> {
                // Počet dní s redukcí spotřeby
                pouchDao.getDaysWithReducedConsumption(
                    challenge.startDate, 
                    challenge.endDate
                )
            }
            "Weekend Warrior" -> {
                // Počet víkendových dní bez sáčků
                pouchDao.getWeekendDaysWithoutPouches(
                    challenge.startDate, 
                    challenge.endDate
                )
            }
            "Monthly Reduction Master" -> {
                // Procentuální snížení spotřeby
                pouchDao.getMonthlyReductionPercentage(
                    challenge.startDate, 
                    challenge.endDate
                )
            }
            "Consistent Tracker" -> {
                // Počet dní s nepřetržitým sledováním
                pouchDao.getConsecutiveTrackingDays(
                    challenge.startDate, 
                    challenge.endDate
                )
            }
            "Health Conscious Journey" -> {
                // Celkový příjem nikotinu
                pouchDao.getTotalNicotineInTimeRange(
                    challenge.startDate.toEpochSecond(ZoneOffset.UTC) * 1000,
                    challenge.endDate.toEpochSecond(ZoneOffset.UTC) * 1000
                )?.toInt() ?: 0
            }
            else -> 0
        }
    }

    // Plánování pravidelné aktualizace výzev
    fun scheduleChallengeUpdates() {
        val challengeUpdateRequest = PeriodicWorkRequestBuilder<ChallengeUpdateWorker>(
            1, TimeUnit.DAYS  // Denní aktualizace
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "challenge_updates",
            ExistingPeriodicWorkPolicy.KEEP,
            challengeUpdateRequest
        )
    }
}

// Worker pro pravidelnou aktualizaci výzev
class ChallengeUpdateWorker(
    context: Context, 
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val challengeManager = ChallengeManager(applicationContext)
        
        challengeManager.initializeChallenges()
        challengeManager.updateChallengeProgress()
        
        return Result.success()
    }
}
