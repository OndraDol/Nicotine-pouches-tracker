package com.example.nicotinetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update

/**
 * Data Access Object pro práci s typy nikotinových sáčků
 */
@Dao
interface PouchTypeDao {
    /**
     * Vloží nový typ sáčku do databáze
     * @return ID vloženého typu
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pouchType: PouchType): Long
    
    /**
     * Vloží více typů sáčků najednou
     * @return ID vložených typů
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pouchTypes: List<PouchType>): List<Long>
    
    /**
     * Aktualizuje existující typ sáčku
     */
    @Update
    suspend fun update(pouchType: PouchType)
    
    /**
     * Smaže typ sáčku
     */
    @Delete
    suspend fun delete(pouchType: PouchType)
    
    /**
     * Získá všechny typy sáčků seřazené podle jména
     */
    @Query("SELECT * FROM pouch_types ORDER BY name ASC")
    suspend fun getAllPouchTypes(): List<PouchType>
    
    /**
     * Získá konkrétní typ sáčku podle ID
     */
    @Query("SELECT * FROM pouch_types WHERE id = :pouchTypeId")
    suspend fun getPouchTypeById(pouchTypeId: Int): PouchType?
    
    /**
     * Získá všechny typy sáčků pro konkrétní značku
     */
    @Query("SELECT * FROM pouch_types WHERE brandId = :brandId ORDER BY name ASC")
    suspend fun getPouchTypesByBrandId(brandId: Int): List<PouchType>
    
    /**
     * Vyhledá typy sáčků podle části jména
     */
    @Query("SELECT * FROM pouch_types WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchPouchTypes(searchQuery: String): List<PouchType>
    
    /**
     * Získá počet všech typů sáčků
     */
    @Query("SELECT COUNT(*) FROM pouch_types")
    suspend fun getPouchTypesCount(): Int
    
    /**
     * Získá počet typů sáčků pro konkrétní značku
     */
    @Query("SELECT COUNT(*) FROM pouch_types WHERE brandId = :brandId")
    suspend fun getPouchTypesCountByBrandId(brandId: Int): Int
    
    /**
     * Získá typy sáčků seřazené podle síly nikotinu (sestupně)
     */
    @Query("SELECT * FROM pouch_types ORDER BY nicotineStrength DESC")
    suspend fun getPouchTypesByStrength(): List<PouchType>
}
