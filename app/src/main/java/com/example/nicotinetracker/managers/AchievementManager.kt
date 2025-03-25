package com.example.nicotinetracker.managers

import android.content.Context
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.data.achievement.Achievement
import com.example.nicotinetracker.data.achievement.AchievementCategory
import com.example.nicotinetracker.utils.DataAnalyzer
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar

/**
 * Manager pro správu úspěchů (gamifikace).
 */
class AchievementManager(private val context: Context) {
    private val database by lazy { PouchDatabase.getDatabase(context) }
    private val achievementDao by lazy { database.achievementDao() }
    private val pouchDao by lazy { database.pouchDao() }
    private val dataAnalyzer by lazy { DataAnalyzer(database) }
    private val preferenceManager by lazy { PreferenceManager(context) }

    /**
     * Inicializuje základní úspěchy, pokud ještě neexistují.
     */
    suspend fun initializeAchievements() = withContext(Dispatchers.IO) {
        val count = achievementDao.getTotalAchievementsCount()
        
        // Pouze inicializuj, pokud ještě nejsou vytvořeny
        if (count == 0) {
            val achievements = listOf(
                // Úspěchy za konzistenci
                Achievement(
                    id = "consistency_3_days",
                    name = context.getString(R.string.achievement_consistency_3_days_title),
                    description = context.getString(R.string.achievement_consistency_3_days_desc),
                    iconResId = R.drawable.ic_notification, // Výchozí ikona, později změníme
                    progress = 0,
                    maxProgress = 3,
                    category = AchievementCategory.CONSISTENCY
                ),
                Achievement(
                    id = "consistency_7_days",
                    name = context.getString(R.string.achievement_consistency_7_days_title),
                    description = context.getString(R.string.achievement_consistency_7_days_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 7,
                    category = AchievementCategory.CONSISTENCY
                ),
                Achievement(
                    id = "consistency_30_days",
                    name = context.getString(R.string.achievement_consistency_30_days_title),
                    description = context.getString(R.string.achievement_consistency_30_days_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 30,
                    category = AchievementCategory.CONSISTENCY
                ),
                
                // Úspěchy za snížení
                Achievement(
                    id = "reduction_10_percent",
                    name = context.getString(R.string.achievement_reduction_10_percent_title),
                    description = context.getString(R.string.achievement_reduction_10_percent_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 100, // Bude reprezentováno v procentech
                    category = AchievementCategory.REDUCTION
                ),
                Achievement(
                    id = "reduction_25_percent",
                    name = context.getString(R.string.achievement_reduction_25_percent_title),
                    description = context.getString(R.string.achievement_reduction_25_percent_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 100,
                    category = AchievementCategory.REDUCTION
                ),
                Achievement(
                    id = "reduction_50_percent",
                    name = context.getString(R.string.achievement_reduction_50_percent_title),
                    description = context.getString(R.string.achievement_reduction_50_percent_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 100,
                    category = AchievementCategory.REDUCTION
                ),
                
                // Milníky
                Achievement(
                    id = "milestone_tracking_7_days",
                    name = context.getString(R.string.achievement_milestone_7_days_title),
                    description = context.getString(R.string.achievement_milestone_7_days_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 7,
                    category = AchievementCategory.MILESTONE
                ),
                Achievement(
                    id = "milestone_tracking_30_days",
                    name = context.getString(R.string.achievement_milestone_30_days_title),
                    description = context.getString(R.string.achievement_milestone_30_days_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 30,
                    category = AchievementCategory.MILESTONE
                ),
                Achievement(
                    id = "milestone_tracking_90_days",
                    name = context.getString(R.string.achievement_milestone_90_days_title),
                    description = context.getString(R.string.achievement_milestone_90_days_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 90,
                    category = AchievementCategory.MILESTONE
                ),
                
                // Výzvy
                Achievement(
                    id = "challenge_no_evening",
                    name = context.getString(R.string.achievement_challenge_no_evening_title),
                    description = context.getString(R.string.achievement_challenge_no_evening_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 7, // 7 dní bez večerních sáčků
                    category = AchievementCategory.CHALLENGE
                ),
                Achievement(
                    id = "challenge_weekend_reduction",
                    name = context.getString(R.string.achievement_challenge_weekend_title),
                    description = context.getString(R.string.achievement_challenge_weekend_desc),
                    iconResId = R.drawable.ic_notification,
                    progress = 0,
                    maxProgress = 4, // 4 víkendy se sníženou spotřebou
                    category = AchievementCategory.CHALLENGE
                )
            )
            
            achievementDao.insertAchievements(achievements)
        }
    }

    /**
     * Vyhodnotí všechny úspěchy a aktualizuje jejich stav.
     */
    suspend fun evaluateAllAchievements() = withContext(Dispatchers.IO) {
        evaluateConsistencyAchievements()
        evaluateReductionAchievements()
        evaluateMilestoneAchievements()
        evaluateChallengeAchievements()
    }

    /**
     * Vyhodnotí úspěchy spojené s konzistencí.
     */
    private suspend fun evaluateConsistencyAchievements() {
        val dailyLimit = preferenceManager.getDailyLimit()
        
        // Získat všechny sáčky seřazené podle data
        val pouches = pouchDao.getAllPouchesOrderedByTimestamp()
        if (pouches.isEmpty()) return
        
        // Seskupit sáčky podle dne
        val pouchesByDay = pouches.groupBy { pouch ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = pouch.timestamp
            LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        }
        
        // Vytvořit seznam dnů
        val days = pouchesByDay.keys.sorted()
        if (days.isEmpty()) return
        
        // Spočítat dny v řadě, kdy uživatel nepřekročil limit
        var currentStreak = 0
        var maxStreak = 0
        
        // Začít od nejnovějšího dne a jít zpět
        for (i in days.size - 1 downTo 0) {
            val dayPouches = pouchesByDay[days[i]] ?: emptyList()
            
            if (dayPouches.size <= dailyLimit) {
                currentStreak++
                maxStreak = maxOf(maxStreak, currentStreak)
            } else {
                // Pokud překročil limit, resetuj streak
                currentStreak = 0
            }
        }
        
        // Aktualizace úspěchů za konzistenci
        val streakAchievements = listOf(
            "consistency_3_days" to 3,
            "consistency_7_days" to 7,
            "consistency_30_days" to 30
        )
        
        for ((achievementId, requiredDays) in streakAchievements) {
            val achievement = achievementDao.getAchievementById(achievementId)
            
            if (achievement != null && achievement.unlockedAt == null) {
                // Aktualizuj pokrok
                val progress = minOf(currentStreak, achievement.maxProgress)
                achievementDao.updateAchievementProgress(achievementId, progress)
                
                // Pokud dosáhl cíle, odemkni úspěch
                if (progress >= requiredDays) {
                    achievementDao.unlockAchievement(achievementId)
                }
            }
        }
    }

    /**
     * Vyhodnotí úspěchy spojené se snížením spotřeby.
     */
    private suspend fun evaluateReductionAchievements() {
        val forecast = dataAnalyzer.forecastConsumption()
        
        // Pokud není dost dat nebo trend není klesající, nic nedělej
        if (forecast.trend != DataAnalyzer.TrendDirection.DECREASING) return
        
        val reductionAchievements = listOf(
            "reduction_10_percent" to 10.0,
            "reduction_25_percent" to 25.0,
            "reduction_50_percent" to 50.0
        )
        
        for ((achievementId, requiredReduction) in reductionAchievements) {
            val achievement = achievementDao.getAchievementById(achievementId)
            
            if (achievement != null && achievement.unlockedAt == null) {
                // Použij odhadované snížení pro výpočet pokroku
                val estimatedReduction = forecast.estimatedReduction
                val progress = minOf((estimatedReduction / requiredReduction * 100).toInt(), 100)
                
                achievementDao.updateAchievementProgress(achievementId, progress)
                
                // Pokud dosáhl požadovaného snížení, odemkni úspěch
                if (estimatedReduction >= requiredReduction) {
                    achievementDao.unlockAchievement(achievementId)
                }
            }
        }
    }

    /**
     * Vyhodnotí úspěchy spojené s milníky.
     */
    private suspend fun evaluateMilestoneAchievements() {
        val pouches = pouchDao.getAllPouches()
        if (pouches.isEmpty()) return
        
        // Zjistit první a poslední záznam
        val firstPouch = pouches.minByOrNull { it.timestamp } ?: return
        val lastPouch = pouches.maxByOrNull { it.timestamp } ?: return
        
        // Výpočet počtu dní sledování
        val startDate = LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(firstPouch.timestamp),
            ZoneId.systemDefault()
        )
        val endDate = LocalDate.ofInstant(
            java.time.Instant.ofEpochMilli(lastPouch.timestamp),
            ZoneId.systemDefault()
        )
        
        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1
        
        // Aktualizace úspěchů za milníky sledování
        val trackingAchievements = listOf(
            "milestone_tracking_7_days" to 7L,
            "milestone_tracking_30_days" to 30L,
            "milestone_tracking_90_days" to 90L
        )
        
        for ((achievementId, requiredDays) in trackingAchievements) {
            val achievement = achievementDao.getAchievementById(achievementId)
            
            if (achievement != null && achievement.unlockedAt == null) {
                // Aktualizuj pokrok
                val progress = minOf(daysBetween.toInt(), achievement.maxProgress)
                achievementDao.updateAchievementProgress(achievementId, progress)
                
                // Pokud dosáhl cíle, odemkni úspěch
                if (daysBetween >= requiredDays) {
                    achievementDao.unlockAchievement(achievementId)
                }
            }
        }
    }

    /**
     * Vyhodnotí úspěchy spojené s výzvami.
     */
    private suspend fun evaluateChallengeAchievements() {
        // Vyhodnocení výzvy "Žádný sáček po 18. hodině"
        evaluateNoEveningChallenge()
        
        // Vyhodnocení výzvy "Snížená spotřeba o víkendu"
        evaluateWeekendReductionChallenge()
    }

    /**
     * Vyhodnotí výzvu "Žádný sáček po 18. hodině".
     */
    private suspend fun evaluateNoEveningChallenge() {
        val challengeId = "challenge_no_evening"
        val achievement = achievementDao.getAchievementById(challengeId) ?: return
        
        // Pokud už je odemčený, nic nedělej
        if (achievement.unlockedAt != null) return
        
        // Získat všechny sáčky seřazené podle data
        val pouches = pouchDao.getAllPouchesOrderedByTimestamp()
        if (pouches.isEmpty()) return
        
        // Seskupit sáčky podle dne
        val pouchesByDay = pouches.groupBy { pouch ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = pouch.timestamp
            LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        }
        
        // Vytvořit seznam dnů
        val days = pouchesByDay.keys.sorted()
        if (days.isEmpty()) return
        
        // Začít od nejnovějšího dne a jít zpět
        var consecutiveDays = 0
        val eveningThreshold = LocalTime.of(18, 0)
        
        for (i in days.size - 1 downTo 0) {
            val dayPouches = pouchesByDay[days[i]] ?: emptyList()
            
            // Kontrola, zda byly všechny sáčky přidány před 18:00
            val anyEveningPouch = dayPouches.any { pouch ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = pouch.timestamp
                val pouchTime = LocalTime.of(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
                pouchTime.isAfter(eveningThreshold)
            }
            
            if (!anyEveningPouch) {
                consecutiveDays++
                
                // Aktualizuj pokrok
                val progress = minOf(consecutiveDays, achievement.maxProgress)
                achievementDao.updateAchievementProgress(challengeId, progress)
                
                // Pokud dosáhl cíle, odemkni úspěch
                if (progress >= achievement.maxProgress) {
                    achievementDao.unlockAchievement(challengeId)
                    break
                }
            } else {
                // Resetuj počet dní
                consecutiveDays = 0
                achievementDao.updateAchievementProgress(challengeId, 0)
            }
        }
    }

    /**
     * Vyhodnotí výzvu "Snížená spotřeba o víkendu".
     */
    private suspend fun evaluateWeekendReductionChallenge() {
        val challengeId = "challenge_weekend_reduction"
        val achievement = achievementDao.getAchievementById(challengeId) ?: return
        
        // Pokud už je odemčený, nic nedělej
        if (achievement.unlockedAt != null) return
        
        // Získat všechny sáčky seřazené podle data
        val pouches = pouchDao.getAllPouchesOrderedByTimestamp()
        if (pouches.isEmpty()) return
        
        // Seskupit sáčky podle týdne a dne
        val pouchesByWeekAndDay = pouches.groupBy { pouch ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = pouch.timestamp
            val date = LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
            // Klíč je týden v roce + den v týdnu
            val weekOfYear = date.get(java.time.temporal.WeekFields.of(Locale.getDefault()).weekOfYear())
            val year = date.year
            val dayOfWeek = date.dayOfWeek.value
            Triple(year, weekOfYear, dayOfWeek)
        }
        
        // Spočítat průměrnou spotřebu všedních dnů a víkendů
        var weekdayTotal = 0
        var weekdayCount = 0
        var weekendTotal = 0
        var weekendCount = 0
        
        for ((key, dayPouches) in pouchesByWeekAndDay) {
            val (_, _, dayOfWeek) = key
            
            if (dayOfWeek <= 5) { // Pondělí až pátek
                weekdayTotal += dayPouches.size
                weekdayCount++
            } else { // Sobota a neděle
                weekendTotal += dayPouches.size
                weekendCount++
            }
        }
        
        // Výpočet průměrů
        val weekdayAverage = if (weekdayCount > 0) weekdayTotal.toFloat() / weekdayCount else 0f
        val weekendAverage = if (weekendCount > 0) weekendTotal.toFloat() / weekendCount else 0f
        
        // Kontrola, zda je víkendová spotřeba nižší než všední dny
        if (weekendAverage < weekdayAverage * 0.8f) { // O 20% nižší je úspěch
            // Počet víkendů s nižší spotřebou
            val weekends = pouchesByWeekAndDay.keys.filter { it.third > 5 }.map { it.first to it.second }.distinct()
            val weekendCount = weekends.size
            
            // Aktualizuj pokrok
            val progress = minOf(weekendCount, achievement.maxProgress)
            achievementDao.updateAchievementProgress(challengeId, progress)
            
            // Pokud dosáhl cíle, odemkni úspěch
            if (progress >= achievement.maxProgress) {
                achievementDao.unlockAchievement(challengeId)
            }
        }
    }

    /**
     * Získá nově odemčené úspěchy od posledního volání.
     * Vrací seznam nově odemčených úspěchů.
     */
    suspend fun getNewlyUnlockedAchievements(): List<Achievement> = withContext(Dispatchers.IO) {
        val lastCheckTime = preferenceManager.getLastAchievementCheckTime()
        val currentTime = System.currentTimeMillis()
        
        val newlyUnlocked = achievementDao.getAchievementsUnlockedBetween(lastCheckTime, currentTime)
        
        // Aktualizace času poslední kontroly
        preferenceManager.setLastAchievementCheckTime(currentTime)
        
        newlyUnlocked
    }
}
