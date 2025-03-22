```kotlin
package com.example.nicotinetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Pouch::class], version = 1, exportSchema = false)
abstract class PouchDatabase : RoomDatabase() {
    
    abstract fun pouchDao(): PouchDao
    
    companion object {
        @Volatile
        private var INSTANCE: PouchDatabase? = null
        
        fun getDatabase(context: Context): PouchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PouchDatabase::class.java,
                    "pouch_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```
