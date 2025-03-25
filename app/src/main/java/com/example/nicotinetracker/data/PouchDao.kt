package com.example.nicotinetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

/**
 * Data Access Object pro práci se záznamy spotřeby nikotinových sáčků
 */
@Dao
interface PouchDao {
    /**
     * Vloží nový záznam spotřeby do databáze
     * @return ID vloženého záznamu
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pouch: Pouch): Long
    
    /**
     * Vloží více záznamů najednou
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pouches: List<Pouch>): List<Long>
    
    /**
     * Aktualizuje existující záznam
     */
    @Update
    suspend fun update(pouch: Pouch)
    
    /**
     * Smaže záznam
     */
    @Delete
    suspend fun delete(pouch: Pouch)
    
    /**
     * Získá všechny záznamy seřazené od nejnovějších
     */
    @Query("SELECT * FROM pouches ORDER BY timestamp DESC")
    suspend fun getAllPouches(): List<Pouch>
    
    /**
     * Získá všechny záznamy seřazené od nejstarších
     */
    @Query("SELECT * FROM pouches ORDER BY timestamp ASC")
    suspend fun getAllPouchesOrderedByTimestamp(): List<Pouch>
    
    /**
     * Získá záznamy v daném časovém rozmezí
     */
    @Query("SELECT * FROM pouches WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getPouchesInTimeRange(startTime: Long, endTime: Long): List<Pouch>
    
    /**
     * Vrátí počet sáčků v daném časovém období
     */
    @Query("SELECT COUNT(*) FROM pouches WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun countPouchesInTimeRange(startTime: Long, endTime: Long): Int
    
    /**
     * Získá záznam podle ID včetně detailů o typu a značce
     */
    @Transaction
    @Query("""
        SELECT p.*, pt.nicotineStrength, pt.weight, pt.name AS typeName, 
               b.name AS brandName
        FROM pouches p
        LEFT JOIN pouch_types pt ON p.pouchTypeId = pt.id
        LEFT JOIN brands b ON pt.brandId = b.id
        WHERE p.id = :pouchId
    """)
    suspend fun getPouchWithDetails(pouchId: Int): PouchWithDetails?
    
    /**
     * Získá všechny záznamy s detaily o typu a značce
     */
    @Transaction
    @Query("""
        SELECT p.*, pt.nicotineStrength, pt.weight, pt.name AS typeName, 
               b.name AS brandName
        FROM pouches p
        LEFT JOIN pouch_types pt ON p.pouchTypeId = pt.id
        LEFT JOIN brands b ON pt.brandId = b.id
        ORDER BY p.timestamp DESC
    """)
    suspend fun getAllPouchesWithDetails(): List<PouchWithDetails>
    
    /**
     * Získá záznamy v daném časovém období s detaily o typu a značce
     */
    @Transaction
    @Query("""
        SELECT p.*, pt.nicotineStrength, pt.weight, pt.name AS typeName, 
               b.name AS brandName
        FROM pouches p
        LEFT JOIN pouch_types pt ON p.pouchTypeId = pt.id
        LEFT JOIN brands b ON pt.brandId = b.id
        WHERE p.timestamp BETWEEN :startTime AND :endTime
        ORDER BY p.timestamp DESC
    """)
    suspend fun getPouchesInTimeRangeWithDetails(startTime: Long, endTime: Long): List<PouchWithDetails>
    
    /**
     * Vypočítá celkové množství nikotinu v mg za dané období
     */
    @Query("""
        SELECT SUM(
            CASE 
                WHEN p.pouchTypeId IS NOT NULL THEN pt.nicotineStrength * pt.weight / 1000
                WHEN p.customNicotineContent IS NOT NULL AND p.customWeight IS NOT NULL THEN p.customNicotineContent * p.customWeight / 1000
                ELSE 0 
            END
        ) 
        FROM pouches p
        LEFT JOIN pouch_types pt ON p.pouchTypeId = pt.id
        WHERE p.timestamp BETWEEN :startTime AND :endTime
    """)
    suspend fun getTotalNicotineInTimeRange(startTime: Long, endTime: Long): Float?
    
    /**
     * Vrátí počet sáčků podle typu
     */
    @Query("SELECT COUNT(*) FROM pouches WHERE pouchTypeId = :pouchTypeId")
    suspend fun countPouchesByType(pouchTypeId: Int): Int
    
    /**
     * Získá počet sáčků seskupených podle dne
     */
    @Query("""
        SELECT strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch', 'localtime') as day, COUNT(*) as count
        FROM pouches
        WHERE timestamp BETWEEN :startTime AND :endTime
        GROUP BY day
        ORDER BY day ASC
    """)
    suspend fun getPouchCountByDay(startTime: Long, endTime: Long): List<DailyCount>
    
    /**
     * Smaže všechny záznamy starší než zadané datum
     */
    @Query("DELETE FROM pouches WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int
}

/**
 * Třída reprezentující záznam spotřeby s detaily o typu a značce
 */
data class PouchWithDetails(
    val id: Int,
    val timestamp: Long,
    val pouchTypeId: Int?,
    val note: String?,
    val customBrand: String?,
    val customType: String?,
    val customNicotineContent: Float?,
    val customWeight: Float?,
    val nicotineStrength: Float?,
    val weight: Float?,
    val typeName: String?,
    val brandName: String?
)

/**
 * Pomocná třída pro dotaz na počet sáčků podle dne
 */
data class DailyCount(
    val day: String,
    val count: Int
)
