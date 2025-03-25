package com.example.nicotinetracker.data.achievement

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Databázová entita reprezentující úspěch, kterého může uživatel dosáhnout.
 */
@Entity(tableName = "achievements")
data class Achievement(
    /**
     * Unikátní identifikátor úspěchu (použit string místo auto-generovaného int pro lepší čitelnost a správu).
     */
    @PrimaryKey val id: String,
    
    /**
     * Název úspěchu.
     */
    val name: String,
    
    /**
     * Popis úspěchu a jak ho získat.
     */
    val description: String,
    
    /**
     * ID ikony pro zobrazení v UI.
     */
    val iconResId: Int,
    
    /**
     * Časová značka, kdy byl úspěch odemčen. Null znamená, že úspěch ještě nebyl získán.
     */
    val unlockedAt: Long? = null,
    
    /**
     * Aktuální pokrok k dokončení úspěchu (pro postupné úspěchy).
     */
    val progress: Int = 0,
    
    /**
     * Maximální hodnota pokroku potřebná k dokončení úspěchu.
     */
    val maxProgress: Int,
    
    /**
     * Kategorie úspěchu.
     */
    val category: AchievementCategory
)

/**
 * Kategorie úspěchů.
 */
enum class AchievementCategory {
    /**
     * Úspěchy spojené s pravidelným dodržováním limitu (např. "7 dní v řadě").
     */
    CONSISTENCY,
    
    /**
     * Úspěchy spojené se snížením spotřeby.
     */
    REDUCTION,
    
    /**
     * Úspěchy za dosažení určitých milníků.
     */
    MILESTONE,
    
    /**
     * Speciální výzvy (např. "Žádný sáček po 18. hodině").
     */
    CHALLENGE
}