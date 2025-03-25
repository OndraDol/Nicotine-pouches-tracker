package com.example.nicotinetracker.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.time.Instant

class AdManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private val adRequest = AdRequest.Builder().build()
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    companion object {
        private const val LAST_AD_TIMESTAMP_KEY = "last_ad_timestamp"
        private const val AD_INTERVAL_HOURS = 5L
        private const val TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    // Inicializace AdMob
    fun initialize() {
        MobileAds.initialize(context) { 
            Log.d("AdManager", "AdMob initialization complete")
        }
    }

    // Veřejná metoda pro kontrolu možnosti zobrazení reklamy
    fun canShowAd(): Boolean {
        val lastAdTimestamp = prefs.getLong(LAST_AD_TIMESTAMP_KEY, 0)
        val currentTime = Instant.now().epochSecond
        val hoursSinceLastAd = (currentTime - lastAdTimestamp) / 3600

        return hoursSinceLastAd >= AD_INTERVAL_HOURS
    }

    // Načtení odměněné reklamy
    fun loadRewardedAd(
        onAdLoaded: () -> Unit = {},
        onAdFailedToLoad: (String) -> Unit = {}
    ) {
        // Pokud nelze zobrazit reklamu, přeskočíme načítání
        if (!canShowAd()) {
            Log.d("AdManager", "Čas pro reklamu ještě nenastal")
            return
        }

        RewardedAd.load(
            context, 
            TEST_AD_UNIT_ID, 
            adRequest, 
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("AdManager", "Reklama se nepodařila načíst: ${adError.message}")
                    rewardedAd = null
                    onAdFailedToLoad(adError.message)
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdManager", "Reklama úspěšně načtena")
                    rewardedAd = ad
                    onAdLoaded()
                }
            }
        )
    }

    // Zobrazení odměněné reklamy
    fun tryShowRewardedAd(
        activity: Activity,
        onAdDismissed: () -> Unit = {},
        onAdFailed: (String) -> Unit = {}
    ) {
        // Kontrola možnosti zobrazení reklamy
        if (!canShowAd()) {
            Log.d("AdManager", "Čas pro reklamu ještě nenastal")
            onAdDismissed()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdManager", "Reklama byla zavřena")
                    
                    // Uložení timestamps posledně zobrazené reklamy
                    prefs.edit()
                        .putLong(LAST_AD_TIMESTAMP_KEY, Instant.now().epochSecond)
                        .apply()

                    rewardedAd = null
                    onAdDismissed()
                    
                    // Načtení další reklamy pro příští použití
                    loadRewardedAd()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.d("AdManager", "Nepodařilo se zobrazit reklamu: ${adError.message}")
                    rewardedAd = null
                    onAdFailed(adError.message)
                }
            }

            ad.show(activity) { 
                Log.d("AdManager", "Reklama byla shlédnuta")
            }
        } else {
            // Pokud reklama není načtena, zkuste ji znovu načíst
            loadRewardedAd(
                onAdLoaded = { 
                    tryShowRewardedAd(activity, onAdDismissed, onAdFailed) 
                }
            )
        }
    }
}
