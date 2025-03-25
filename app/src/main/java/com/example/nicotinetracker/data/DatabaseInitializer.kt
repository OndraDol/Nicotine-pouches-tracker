package com.example.nicotinetracker.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Pomocná třída pro inicializaci databáze základními značkami a typy sáčků.
 */
class DatabaseInitializer(private val context: Context) {
    
    private val database = PouchDatabase.getDatabase(context)
    private val brandDao = database.brandDao()
    private val pouchTypeDao = database.pouchTypeDao()
    
    fun initializeDatabase(scope: CoroutineScope) {
        scope.launch {
            withContext(Dispatchers.IO) {
                // Kontrola, zda již databáze obsahuje nějaké značky
                if (brandDao.getAllBrands().isEmpty()) {
                    // Inicializace základními značkami a typy sáčků
                    populateDatabase()
                }
            }
        }
    }
    
    private suspend fun populateDatabase() {
        // Vytvoření značek
        val lyftId = brandDao.insert(Brand(name = "LYFT"))
        val velo = brandDao.insert(Brand(name = "Velo"))
        val pablo = brandDao.insert(Brand(name = "Pablo"))
        val nordic = brandDao.insert(Brand(name = "Nordic Spirit"))
        val siberia = brandDao.insert(Brand(name = "Siberia"))
        
        // Vytvoření typů pro LYFT
        pouchTypeDao.insert(PouchType(brandId = lyftId.toInt(), name = "Freeze", nicotineStrength = 9.5f, weight = 0.7f, flavor = "Strong Mint"))
        pouchTypeDao.insert(PouchType(brandId = lyftId.toInt(), name = "Urban Vibe", nicotineStrength = 8.0f, weight = 0.7f, flavor = "Mixed Berry"))
        pouchTypeDao.insert(PouchType(brandId = lyftId.toInt(), name = "Nordic Spirit", nicotineStrength = 10.0f, weight = 0.7f))
        
        // Vytvoření typů pro Velo
        pouchTypeDao.insert(PouchType(brandId = velo.toInt(), name = "Freeze", nicotineStrength = 11.0f, weight = 0.7f, flavor = "Extra Strong Mint"))
        pouchTypeDao.insert(PouchType(brandId = velo.toInt(), name = "Ice Cool", nicotineStrength = 9.6f, weight = 0.7f))
        
        // Vytvoření typů pro Pablo
        pouchTypeDao.insert(PouchType(brandId = pablo.toInt(), name = "Red", nicotineStrength = 16.0f, weight = 0.6f))
        pouchTypeDao.insert(PouchType(brandId = pablo.toInt(), name = "Blue", nicotineStrength = 12.0f, weight = 0.6f))
        
        // Vytvoření typů pro Nordic Spirit
        pouchTypeDao.insert(PouchType(brandId = nordic.toInt(), name = "Regular", nicotineStrength = 9.0f, weight = 0.7f))
        pouchTypeDao.insert(PouchType(brandId = nordic.toInt(), name = "Strong", nicotineStrength = 13.5f, weight = 0.7f))
        
        // Vytvoření typů pro Siberia
        pouchTypeDao.insert(PouchType(brandId = siberia.toInt(), name = "Red", nicotineStrength = 43.0f, weight = 0.8f))
        pouchTypeDao.insert(PouchType(brandId = siberia.toInt(), name = "White", nicotineStrength = 24.0f, weight = 0.8f))
    }
}
