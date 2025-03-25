package com.example.nicotinetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "pouches",
    foreignKeys = [
        ForeignKey(
            entity = PouchType::class,
            parentColumns = ["id"],
            childColumns = ["pouchTypeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("pouchTypeId")]
)
data class Pouch(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val pouchTypeId: Int? = null,  // Reference na typ sáčku
    val note: String? = null,
    
    // Následující pole jsou použita, pokud uživatel zadá vlastní sáček (bez reference na typ)
    val customBrand: String? = null,
    val customType: String? = null,
    val customNicotineContent: Float? = null,
    val customWeight: Float? = null
) {
    fun getDateFormatted(): String {
        val date = Date(timestamp)
        return date.toString()
    }
}
