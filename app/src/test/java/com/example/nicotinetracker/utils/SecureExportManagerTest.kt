package com.example.nicotinetracker.utils

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class SecureExportManagerTest {
    private lateinit var context: Context
    private lateinit var secureExportManager: SecureExportManager

    data class TestData(
        val name: String,
        val value: Int,
        val items: List<String>
    )

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        secureExportManager = SecureExportManager(context)
    }

    @Test
    fun `export and import encrypted data successfully`() {
        // Příprava testovacích dat
        val testData = TestData(
            name = "Test Export", 
            value = 42, 
            items = listOf("item1", "item2")
        )

        // Export dat
        secureExportManager.encryptExport(testData, "test_export.enc")

        // Import dat
        val importResult = secureExportManager.decryptImport("test_export.enc", TestData::class.java)

        // Ověření importu
        assertTrue(importResult.isSuccess())
        
        val importedData = (importResult as Result.Success).data
        assertEquals(testData.name, importedData.name)
        assertEquals(testData.value, importedData.value)
        assertEquals(testData.items, importedData.items)
    }

    @Test
    fun `verify data integrity`() {
        val validData = listOf("data1", "data2")
        val invalidData = emptyList<String>()

        assertTrue(secureExportManager.verifyDataIntegrity(validData))
        assertFalse(secureExportManager.verifyDataIntegrity(invalidData))
    }

    @Test
    fun `cleanup old exports`() {
        val exportDir = File(context.getExternalFilesDir(null), "secure_exports")
        exportDir.mkdirs()

        // Vytvoření starého souboru
        val oldFile = File(exportDir, "old_export.enc")
        oldFile.createNewFile()
        oldFile.setLastModified(System.currentTimeMillis() - (31 * 24 * 60 * 60 * 1000L))

        // Spuštění úklidu
        secureExportManager.cleanupOldExports(30)

        // Ověření smazání
        assertFalse(oldFile.exists())
    }
}
