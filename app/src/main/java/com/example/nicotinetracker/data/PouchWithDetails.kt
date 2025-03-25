package com.example.nicotinetracker.data

import androidx.room.ColumnInfo
import java.util.Date

/**
 * Třída obsahující všechny detaily o spotřebovaném sáčku,
 * včetně informací o typu a značce pokud jsou dostupné.
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
    
    // Informace z tabulky pouch_types
    @ColumnInfo(name = "nicotineStrength") val typeNicotineStrength: Float?,
    @ColumnInfo(name = "weight") val typeWeight: Float?,
    @ColumnInfo(name = "typeName") val typeName: String?,
    val flavor: String?,
    
    // Informace z tabulky brands
    @ColumnInfo(name = "brandName") val brandName: String?
) {
    fun getDateFormatted(): String {
        val date = Date(timestamp)
        return date.toString()
    }
    
    fun getDisplayBrand(): String {
        return brandName ?: customBrand ?: "Neznámá značka"
    }
    
    fun getDisplayType(): String {
        return when {
            typeName != null -> {
                if (flavor != null) "$typeName - $flavor" else typeName
            }
            customType != null -> customType
            else -> "Neznámý typ"
        }
    }
    
    fun getNicotineStrength(): Float? {
        return typeNicotineStrength ?: customNicotineContent
    }
    
    fun getWeight(): Float? {
        return typeWeight ?: customWeight
    }
    
    fun getTotalNicotine(): Float {
        val strength = getNicotineStrength() ?: 0f
        val weight = getWeight() ?: 0f
        return strength * weight / 1000f // mg/g * g = mg
    }
}
