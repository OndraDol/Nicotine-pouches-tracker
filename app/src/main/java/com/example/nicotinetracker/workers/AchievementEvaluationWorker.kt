package com.example.nicotinetracker.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.data.achievement.Achievement
import com.example.nicotinetracker.data.achievement.AchievementCategory
import kotlinx.coroutines.coroutineScope
import java.time.LocalDate

class AchievementEvaluationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val database = PouchDatabase.getDatabase(context)
    private val pouchDao = database.pouchDao()
    private val achievementDao = database.achievementDao()

    override suspend fun doWork(): Result = coroutineScope {
        try {
            // Získání všech statistik
            val totalPouches = pouchDao.countPouchesInTimeRange(
                startTime = LocalDate.now().minusMonths(1).toEpochDay(),
                endTime = LocalDate.now().toEpochDay()
            )
            val totalNicotine = pouchDao.getTotalNicotineInTimeRange(
                startTime = LocalDate.now().minusMonths(1).toEpochDay(),
                endTime = LocalDate.now().toEpochDay()
            ) ?: 0f

            // Evaluace všech kategorií úspěchů
            val achievements = listOf(
                evaluateConsistencyAchievements(totalPouches),
                evaluateReductionAchievements(totalPouches),
                evaluateHealthAchievements(totalNicotine),
                evaluateChallengeAchievements(totalPouches)
            ).flatten()

            // Uložení úspěchů
            achievementDao.insertAll(achievements)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun evaluateConsistencyAchievements(totalPouches: Int): List<Achievement> = listOf(
        Achievement(
            id = "consistency_beginner",
            name = "Začátečník v pravidelnosti",
            description = "Prvních 7 dní pravidelného sledování",
            category = AchievementCategory.CONSISTENCY,
            maxProgress = 7,
            progress = totalPouches.coerceAtMost(7),
            iconResId = R.drawable.ic_achievement_consistency
        ),
        Achievement(
            id = "consistency_master",
            name = "Mistr konzistence",
            description = "30 dní nepřetržitého sledování",
            category = AchievementCategory.CONSISTENCY,
            maxProgress = 30,
            progress = totalPouches.coerceAtMost(30),
            iconResId = R.drawable.ic_achievement_consistency
        )
    )

    private fun evaluateReductionAchievements(totalPouches: Int): List<Achievement> = listOf(
        Achievement(
            id = "reduction_starter",
            name = "První kroky ke snížení",
            description = "Snížení spotřeby o 10%",
            category = AchievementCategory.REDUCTION,
            maxProgress = 100,
            progress = (totalPouches * 10).coerceAtMost(100),
            iconResId = R.drawable.ic_achievement_reduction
        ),
        Achievement(
            id = "reduction_champion",
            name = "Šampion snižování",
            description = "Snížení spotřeby o 50%",
            category = AchievementCategory.REDUCTION,
            maxProgress = 100,
            progress = (totalPouches * 50).coerceAtMost(100),
            iconResId = R.drawable.ic_achievement_reduction
        )
    )

    private fun evaluateHealthAchievements(totalNicotine: Float): List<Achievement> = listOf(
        Achievement(
            id = "health_awareness",
            name = "Zdravotní uvědomění",
            description = "Sledování celkového příjmu nikotinu",
            category = AchievementCategory.HEALTH,
            maxProgress = 500,
            progress = totalNicotine.toInt().coerceAtMost(500),
            iconResId = R.drawable.ic_achievement_health
        ),
        Achievement(
            id = "low_nicotine_champion",
            name = "Nízký příjem nikotinu",
            description = "Udržení příjmu pod 100mg za měsíc",
            category = AchievementCategory.HEALTH,
            maxProgress = 100,
            progress = totalNicotine.toInt().coerceAtMost(100),
            iconResId = R.drawable.ic_achievement_health
        )
    )

    private fun evaluateChallengeAchievements(totalPouches: Int): List<Achievement> = listOf(
        Achievement(
            id = "challenge_beginner",
            name = "První výzva",
            description = "Úspěšně dokončená první výzva",
            category = AchievementCategory.CHALLENGE,
            maxProgress = 1,
            progress = 1,
            iconResId = R.drawable.ic_achievement_challenge
        ),
        Achievement(
            id = "challenge_master",
            name = "Mistr výzev",
            description = "Zvládnutí 5 výzev",
            category = AchievementCategory.CHALLENGE,
            maxProgress = 5,
            progress = totalPouches.coerceAtMost(5),
            iconResId = R.drawable.ic_achievement_challenge
        )
    )
}
