package com.example.nicotinetracker.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.nicotinetracker.data.PouchDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ExportManager(private val context: Context) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val database: PouchDatabase = PouchDatabase.getDatabase(context)

    suspend fun exportToJson(fileName: String? = null): Uri {
        return withContext(Dispatchers.IO) {
            // Získání dat z databáze
            val pouches = database.pouchDao().getAllPouchesWithDetails()
            val brands = database.brandDao().getAllBrands()
            val pouchTypes = database.pouchTypeDao().getAllPouchTypes()
            val achievements = database.achievementDao().getAllAchievements()

            // Vytvoření exportní struktury
            val exportData = mapOf(
                "pouches" to pouches,
                "brands" to brands,
                "pouchTypes" to pouchTypes,
                "achievements" to achievements
            )

            // Generování názvu souboru
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val finalFileName = fileName ?: "nicotine_tracker_export_${timestamp}.json"

            // Vytvoření souboru
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            exportDir.mkdirs()
            val exportFile = File(exportDir, finalFileName)

            // Zápis dat
            FileWriter(exportFile).use { writer ->
                gson.toJson(exportData, writer)
            }

            // Vytvoření URI pro sdílení
            FileProvider.getUriForFile(
                context, 
                "${context.packageName}.fileprovider", 
                exportFile
            )
        }
    }

    suspend fun exportToCsv(fileName: String? = null): Uri {
        return withContext(Dispatchers.IO) {
            val pouches = database.pouchDao().getAllPouchesWithDetails()

            // Generování názvu souboru
            val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
            val finalFileName = fileName ?: "nicotine_tracker_export_${timestamp}.csv"

            // Vytvoření souboru
            val exportDir = File(context.getExternalFilesDir(null), "exports")
            exportDir.mkdirs()
            val exportFile = File(exportDir, finalFileName)

            // Zápis CSV
            FileWriter(exportFile).use { writer ->
                // Hlavička
                writer.write("ID,Timestamp,Brand,Type,Nicotine Strength,Weight,Note\n")

                // Data
                pouches.forEach { pouch ->
                    writer.write(
                        "${pouch.id}," +
                        "${LocalDateTime.ofInstant(Instant.ofEpochMilli(pouch.timestamp), ZoneId.systemDefault())}," +
                        "${pouch.brandName ?: pouch.customBrand ?: ""}," +
                        "${pouch.typeName ?: pouch.customType ?: ""}," +
                        "${pouch.nicotineStrength ?: pouch.customNicotineContent ?: ""}," +
                        "${pouch.weight ?: pouch.customWeight ?: ""}," +
                        "${pouch.note?.replace(",", ";") ?: ""}\n"
                    )
                }
            }

            // Vytvoření URI pro sdílení
            FileProvider.getUriForFile(
                context, 
                "${context.packageName}.fileprovider", 
                exportFile
            )
        }
    }

    suspend fun importFromJson(fileUri: Uri) {
        withContext(Dispatchers.IO) {
            val inputStream = context.contentResolver.openInputStream(fileUri)
            val jsonString = inputStream?.bufferedReader().use { it?.readText() }

            // Parsování JSON
            val exportData = gson.fromJson(jsonString, Map::class.java)

            // Začátek transakce
            database.runInTransaction {
                // Import značek
                val brands = (exportData["brands"] as? List<*>)?.mapNotNull { 
                    it as? Map<*, *> 
                }
                brands?.forEach { brandData ->
                    // Logika importu značek
                }

                // Import typů sáčků
                val pouchTypes = (exportData["pouchTypes"] as? List<*>)?.mapNotNull { 
                    it as? Map<*, *> 
                }
                pouchTypes?.forEach { typeData ->
                    // Logika importu typů sáčků
                }

                // Import sáčků
                val pouches = (exportData["pouches"] as? List<*>)?.mapNotNull { 
                    it as? Map<*, *> 
                }
                pouches?.forEach { pouchData ->
                    // Logika importu sáčků
                }

                // Import úspěchů
                val achievements = (exportData["achievements"] as? List<*>)?.mapNotNull { 
                    it as? Map<*, *> 
                }
                achievements?.forEach { achievementData ->
                    // Logika importu úspěchů
                }
            }
        }
    }
}
