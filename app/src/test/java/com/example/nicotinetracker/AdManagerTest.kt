package com.example.nicotinetracker

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.example.nicotinetracker.utils.AdManager
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.Instant
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
class AdManagerTest {
    private lateinit var context: Context
    private lateinit var adManager: AdManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        adManager = AdManager(context)
    }

    @Test
    fun `test ad interval restriction`() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        
        // Nastavení počátečního timestampu daleko v minulosti
        prefs.edit()
            .putLong("last_ad_timestamp", Instant.now().epochSecond - (6 * 3600))
            .apply()

        // První pokus by měl být povolen (uplynulo více než 5 hodin)
        assertTrue(adManager.canShowAd(), "Ad should be allowed after 5 hours")

        // Uložení aktuálního timestampu
        prefs.edit()
            .putLong("last_ad_timestamp", Instant.now().epochSecond)
            .apply()

        // Druhý pokus by měl být zamítnut (méně než 5 hodin)
        assertFalse(adManager.canShowAd(), "Ad should not be allowed within 5 hours")
    }

    @Test
    fun `test multiple ad attempts`() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        
        // Reset timestampu
        prefs.edit().remove("last_ad_timestamp").apply()

        // První pokus by měl být povolen
        assertTrue(adManager.canShowAd(), "First ad attempt should be allowed")

        // Uložení timestampu
        prefs.edit()
            .putLong("last_ad_timestamp", Instant.now().epochSecond)
            .apply()

        // Následující pokusy by měly být zamítnuty
        assertFalse(adManager.canShowAd(), "Subsequent ad attempts should be blocked")
    }

    @Test
    fun `test edge case ad intervals`() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        
        // Testování hraničních hodnot intervalu
        prefs.edit()
            .putLong("last_ad_timestamp", Instant.now().epochSecond - (5 * 3600 - 60))
            .apply()

        // Těsně před 5 hodinami - reklama by neměla být povolena
        assertFalse(adManager.canShowAd(), "Ad should not be allowed just before 5 hours")

        prefs.edit()
            .putLong("last_ad_timestamp", Instant.now().epochSecond - (5 * 3600 + 60))
            .apply()

        // Těsně po 5 hodinách - reklama by měla být povolena
        assertTrue(adManager.canShowAd(), "Ad should be allowed just after 5 hours")
    }
}
