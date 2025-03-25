package com.example.nicotinetracker.utils

import android.content.Context
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nicotinetracker.data.Brand
import com.example.nicotinetracker.data.Pouch
import com.example.nicotinetracker.data.PouchDatabase
import com.example.nicotinetracker.data.PouchType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class ExportManagerTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: PouchDatabase
    private lateinit var exportManager: ExportManager
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            context, 
            PouchDatabase::class.java
        ).allowMainThreadQueries().build()

        exportManager = ExportManager(context)

        // Příprava testovacích dat
        runBlocking {
            val brandDao = database.brandDao()
            val pouchTypeDao = database.pouchTypeDao()
            val pouchDao = database.pouchDao()

            // Přidání testovacích značek
            val brand1 = Brand(name = "TestBrand1")
            val brand1Id = brandDao.insert(brand1)

            // Přidání testovacích typů sáčků
            val pouchType1 = PouchType(name = "TestType1", nicotineStrength = 6f)
            val pouchType1Id = pouchTypeDao.insert(pouchType1)

            // Přidání testovacích sáčků
            val pouch1 = Pouch(
                timestamp = System.currentTimeMillis(),
                brandId = brand1Id,
                pouchTypeId = pouchType1Id,
                nicotineStrength = 6f,
                weight = 0.7f,
                note = "Test pouch"
            )
            pouchDao.insert(pouch1)
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun exportData_shouldExportAllData() = runBlocking {
        // Vytvoření dočasného souboru pro export
        val tempFile = File(context.cacheDir, "test_export.json")
        val uri = Uri.fromFile(tempFile)

        // Export dat
        val result = exportManager.exportData(uri)

        // Ověření výsledku
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        
        // Ověření existence souboru
        assertTrue(tempFile.exists())
        assertTrue(tempFile.length() > 0)
    }

    @Test
    fun importData_shouldImportPreviouslyExportedData() = runBlocking {
        // Vytvoření dočasného souboru pro export
        val exportFile = File(context.cacheDir, "test_export.json")
        val exportUri = Uri.fromFile(exportFile)

        // Export dat
        exportManager.exportData(exportUri)

        // Smazání stávajících dat z databáze
        database.clearAllTables()

        // Import dat
        val importResult = exportManager.importData(exportUri)

        // Ověření výsledku importu
        assertTrue(importResult.isSuccess)
        
        val importData = importResult.getOrNull()
        assertTrue(importData != null)
        assertEquals(1, importData?.brands)
        assertEquals(1, importData?.pouchTypes)
        assertEquals(1, importData?.pouches)

        // Ověření, že data jsou skutečně importována
        val pouchDao = database.pouchDao()
        val importedPouches = pouchDao.getAllPouches()
        assertEquals(1, importedPouches.size)
        
        val importedPouch = importedPouches.first()
        assertEquals(6f, importedPouch.nicotineStrength)
        assertEquals(0.7f, importedPouch.weight)
        assertEquals("Test pouch", importedPouch.note)
    }

    @Test
    fun exportToCsv_shouldCreateValidCsvFile() = runBlocking {
        // Vytvoření dočasného souboru pro export CSV
        val tempFile = File(context.cacheDir, "test_export.csv")
        val uri = Uri.fromFile(tempFile)

        // Export do CSV
        val result = exportManager.exportToCsv(uri)

        // Ověření výsledku
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())
        
        // Ověření existence souboru
        assertTrue(tempFile.exists())
        assertTrue(tempFile.length() > 0)

        // Kontrola obsahu CSV
        val csvContent = tempFile.readText()
        assertTrue(csvContent.contains("TestBrand1"))
        assertTrue(csvContent.contains("TestType1"))
        assertTrue(csvContent.contains("Test pouch"))
    }
}
