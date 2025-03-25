package com.example.nicotinetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entita reprezentující značku nikotinových sáčků
 */
@Entity(tableName = "brands")
data class Brand(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // Název značky
    val name: String,
    
    // Popis značky (nepovinné)
    val description: String? = null,
    
    // URL k obrázku/logu značky (nepovinné)
    val imageUrl: String? = null,
    
    // Příznak, zda je tato značka vytvořena uživatelem
    val isUserCreated: Boolean = false
)
