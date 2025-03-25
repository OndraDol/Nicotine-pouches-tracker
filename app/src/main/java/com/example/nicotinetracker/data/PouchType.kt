package com.example.nicotinetracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entita reprezentující konkrétní typ nikotinového sáčku
 */
@Entity(
    tableName = "pouch_types",
    foreignKeys = [
        ForeignKey(
            entity = Brand::class,
            parentColumns = ["id"],
            childColumns = ["brandId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("brandId")]
)
data class PouchType(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    // Reference na značku
    val brandId: Int,
    
    // Název/příchuť typu sáčku
    val name: String,
    
    // Obsah nikotinu v mg/g
    val nicotineStrength: Float,
    
    // Hmotnost sáčku v gramech
    val weight: Float,
    
    // Popis (nepovinné)
    val description: String? = null,
    
    // Příznak, zda je tento typ vytvořený uživatelem
    val isUserCreated: Boolean = false
)
