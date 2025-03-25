package com.example.nicotinetracker.utils

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecurityManager(private val context: Context) {
    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    // Šifrovaná SharedPreferences
    val securePreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Generování symetrického klíče
    fun generateSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES, 
            "AndroidKeyStore"
        )
        
        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
        .setRandomizedEncryptionRequired(true)
        .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    // Šifrování dat
    fun encrypt(data: ByteArray, alias: String): ByteArray {
        val secretKey = getSecretKey(alias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val encryptedData = cipher.doFinal(data)
        val iv = cipher.iv
        
        return iv + encryptedData
    }

    // Dešifrování dat
    fun decrypt(encryptedData: ByteArray, alias: String): ByteArray {
        val secretKey = getSecretKey(alias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        
        val iv = encryptedData.copyOfRange(0, 12)
        val actualData = encryptedData.copyOfRange(12, encryptedData.size)
        
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher.doFinal(actualData)
    }

    // Získání klíče z Android KeyStore
    private fun getSecretKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        return keyStore.getKey(alias, null) as SecretKey? 
            ?: generateSecretKey(alias)
    }

    // Mazání citlivých dat
    fun clearSensitiveData() {
        securePreferences.edit().clear().apply()
    }

    // Detekce rootovaného zařízení
    fun isDeviceRooted(): Boolean {
        return listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        ).any { File(it).exists() }
    }
}
