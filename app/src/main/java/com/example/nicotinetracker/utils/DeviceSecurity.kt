package com.example.nicotinetracker.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Build
import java.io.File

class DeviceSecurity(private val context: Context) {

    // Detekce rootovaného zařízení
    fun isDeviceRooted(): Boolean {
        return checkRootMethods().also { isRooted ->
            if (isRooted) {
                // Volitelné: Logování pokusu o spuštění na rootovaném zařízení
                AnalyticsLogger(context).logAction(
                    "potential_security_risk", 
                    mapOf("risk_type" to "rooted_device")
                )
            }
        }
    }

    private fun checkRootMethods(): Boolean {
        return listOfNotNull(
            checkRootFiles(),
            checkSuperUserAPK(),
            checkRootPackages(),
            checkTestKeys(),
            checkBuildTags()
        ).any { it }
    }

    // Kontrola existence root souborů
    private fun checkRootFiles(): Boolean {
        val rootPaths = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/data/local/su"
        )
        return rootPaths.any { File(it).exists() }
    }

    // Kontrola existence SuperUser APK
    private fun checkSuperUserAPK(): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.noshufou.android.su", 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Kontrola podezřelých root balíčků
    private fun checkRootPackages(): Boolean {
        val rootPackages = listOf(
            "com.noshufou.android.su",
            "eu.chainfire.supersu",
            "com.koushikdutta.superuser",
            "com.thirdparty.superuser"
        )
        return rootPackages.any { isPackageInstalled(it) }
    }

    // Kontrola test klíčů
    private fun checkTestKeys(): Boolean {
        return Build.TAGS.contains("test-keys")
    }

    // Kontrola build tagů
    private fun checkBuildTags(): Boolean {
        return Build.TAGS.contains("test-keys")
    }

    // Pomocná metoda pro kontrolu instalace balíčku
    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Bezpečnostní politika pro citlivé operace
    fun canPerformSensitiveOperations(): Boolean {
        return when {
            isDeviceRooted() -> false
            isDeviceInDebugMode() -> false
            isEmulator() -> false
            else -> true
        }
    }

    // Detekce zda je zařízení v debug módu
    private fun isDeviceInDebugMode(): Boolean {
        return Build.TAGS.contains("debug")
    }

    // Detekce emulátoru
    private fun isEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK")
            || Build.MANUFACTURER.contains("Genymotion")
    }

    // Pokud je detekováno riziko, můžeme vyvolat speciální akci
    fun handleSecurityRisk(activity: Activity) {
        // Implementace dialogu nebo akce při detekci rizika
        // Například: zablokování aplikace, odhlášení, vymazání dat
    }
}
