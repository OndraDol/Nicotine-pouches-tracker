package com.example.nicotinetracker.data.achievement

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object pro přístup k úspěchům v databázi.
 */
@Dao
interface AchievementDao {
    /**
     * Vrátí všechny úspěchy.
     */
    @Query("SELECT * FROM achievements ORDER BY category, unlockedAt DESC NULLS LAST")
    suspend fun getAllAchievements(): List<Achievement>
    
    /**
     * Vrátí všechny odemčené úspěchy.
     */
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    suspend fun getUnlockedAchievements(): List<Achievement>
    
    /**
     * Vrátí všechny zamčené úspěchy.
     */
    @Query("SELECT * FROM achievements WHERE unlockedAt IS NULL ORDER BY category")
    suspend fun getLockedAchievements(): List<Achievement>
    
    /**
     * Vrátí počet odemčených úspěchů.
     */
    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedAchievementsCount(): Int
    
    /**
     * Vrátí celkový počet úspěchů.
     */
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalAchievementsCount(): Int
    
    /**
     * Vrátí úspěch podle ID.
     */
    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievementById(achievementId: String): Achievement?
    
    /**
     * Vrátí úspěchy podle kategorie.
     */
    @Query("SELECT * FROM achievements WHERE category = :category ORDER BY unlockedAt DESC NULLS LAST")
    suspend fun getAchievementsByCategory(category: AchievementCategory): List<Achievement>
    
    /**
     * Aktualizuje úspěch.
     */
    @Update
    suspend fun updateAchievement(achievement: Achievement)
    
    /**
     * Vkládá nový úspěch nebo přepisuje existující.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)
    
    /**
     * Vkládá seznam úspěchů nebo přepisuje existující.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)
    
    /**
     * Aktualizuje pokrok úspěchu.
     */
    @Query("UPDATE achievements SET progress = :progress WHERE id = :achievementId")
    suspend fun updateAchievementProgress(achievementId: String, progress: Int)
    
    /**
     * Odemkne úspěch nastavením časového razítka.
     */
    @Query("UPDATE achievements SET unlockedAt = :timestamp WHERE id = :achievementId")
    suspend fun unlockAchievement(achievementId: String, timestamp: Long = System.currentTimeMillis())
    
    /**
     * Získá úspěchy odemčené mezi dvěma časovými razítky.
     */
    @Query("SELECT * FROM achievements WHERE unlockedAt BETWEEN :startTime AND :endTime")
    suspend fun getAchievementsUnlockedBetween(startTime: Long, endTime: Long): List<Achievement>
}