```kotlin
package com.example.nicotinetracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PouchDao {
    @Insert
    suspend fun insert(pouch: Pouch)
    
    @Query("SELECT * FROM pouches ORDER BY timestamp DESC")
    suspend fun getAllPouches(): List<Pouch>
    
    @Query("SELECT * FROM pouches WHERE timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    suspend fun getPouchesInTimeRange(startTime: Long, endTime: Long): List<Pouch>
    
    @Query("SELECT COUNT(*) FROM pouches WHERE timestamp BETWEEN :startTime AND :endTime")
    suspend fun countPouchesInTimeRange(startTime: Long, endTime: Long): Int
}
```