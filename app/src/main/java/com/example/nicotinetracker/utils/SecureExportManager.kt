package com.example.nicotinetracker.utils

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import java.io.File
import java.security.GeneralSecurityException

class SecureExportManager(private val context: Context) {
    private val gson = Gson()
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun encryptExport(data: Any, fileName: String) {
        try {
            val exportDir = File(context.getExternalFilesDir(null), "secure_exports")
            exportDir.mkdirs()
            
            val exportFile = File(exportDir, fileName)
            
            val encryptedFile = EncryptedFile.Builder(
                context,
                exportFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_HMAC_SHA256
            ).build()

            val jsonData = gson.toJson(data)
            
            encryptedFile.openFileOutput().use { outputStream ->
                outputStream.write(jsonData.toByteArray())
            }
        } catch (e: GeneralSecurityException) {
            ErrorHandler.handleError(
                Result.Error(
                    exception = e, 
                    message = "Šifrování exportu selhalo"
                )
            )
        }
    }

    fun <T> decryptImport(fileName: String, classType: Class<T>): Result<T> {
        return try {
            val exportDir = File(context.getExternalFilesDir(null), "secure_exports")
            val importFile = File(exportDir, fileName)
            
            val encryptedFile = EncryptedFile.Builder(
                context,
                importFile,
                masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_HMAC_SHA256
            ).build()

            val decryptedBytes = encryptedFile.openFileInput().use { inputStream ->
                inputStream.readBytes()
            }

            val jsonData = String(decryptedBytes)
            val decryptedData = gson.fromJson(jsonData, classType)

            Result.Success(decryptedData)
        } catch (e: Exception) {
            ErrorHandler.handleError(
                Result.Error(
                    exception = e, 
                    message = "Dešifrování importu selhalo"
                )
            )
            Result.Error(exception = e, message = "Import se nezdařil")
        }
    }

    // Metoda pro ověření integrity dat před importem
    fun verifyDataIntegrity(data: Any): Boolean {
        return try {
            // Zde můžete implementovat vlastní logiku ověřování
            // Příklad: kontrola povinných polí, validace struktury
            when (data) {
                is List<*> -> data.isNotEmpty()
                is Map<*, *> -> data.isNotEmpty()
                else -> data != null
            }
        } catch (e: Exception) {
            false
        }
    }

    // Metoda pro mazání starých exportů
    fun cleanupOldExports(daysToKeep: Int = 30) {
        val exportDir = File(context.getExternalFilesDir(null), "secure_exports")
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)

        exportDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
}
