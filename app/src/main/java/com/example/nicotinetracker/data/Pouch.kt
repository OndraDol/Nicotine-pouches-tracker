```kotlin
package com.example.nicotinetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "pouches")
data class Pouch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null
) {
    fun getDateFormatted(): String {
        val date = Date(timestamp)
        return date.toString()
    }
}
```