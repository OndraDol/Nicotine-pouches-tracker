package com.example.nicotinetracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * Data Access Object pro práci se značkami nikotinových sáčků
 */
@Dao
interface BrandDao {
    /**
     * Vloží novou značku do databáze
     * @return ID vložené značky
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(brand: Brand): Long
    
    /**
     * Vloží více značek najednou
     * @return ID vložených značek
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(brands: List<Brand>): List<Long>
    
    /**
     * Aktualizuje existující značku
     */
    @Update
    suspend fun update(brand: Brand)
    
    /**
     * Smaže značku
     */
    @Delete
    suspend fun delete(brand: Brand)
    
    /**
     * Získá všechny značky seřazené podle jména
     */
    @Query("SELECT * FROM brands ORDER BY name ASC")
    suspend fun getAllBrands(): List<Brand>
    
    /**
     * Získá konkrétní značku podle ID
     */
    @Query("SELECT * FROM brands WHERE id = :brandId")
    suspend fun getBrandById(brandId: Int): Brand?
    
    /**
     * Vyhledá značky podle části jména
     */
    @Query("SELECT * FROM brands WHERE name LIKE '%' || :searchQuery || '%' ORDER BY name ASC")
    suspend fun searchBrands(searchQuery: String): List<Brand>
    
    /**
     * Získá počet všech značek
     */
    @Query("SELECT COUNT(*) FROM brands")
    suspend fun getBrandsCount(): Int
}
