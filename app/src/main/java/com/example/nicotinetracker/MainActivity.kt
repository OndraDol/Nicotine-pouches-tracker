package com.example.nicotinetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nicotinetracker.utils.AdManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var adManager: AdManager
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializace AdMob
        adManager = AdManager(this)
        adManager.initialize()

        // Načtení první reklamy na pozadí
        adManager.loadRewardedAd()

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            // Pokus o zobrazení reklamy při každé navigaci
            adManager.tryShowRewardedAd(this) {
                when (item.itemId) {
                    R.id.nav_challenges -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ChallengesFragment())
                            .commit()
                    }
                    R.id.nav_statistics -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, StatisticsFragment())
                            .commit()
                    }
                    // Další navigační položky
                }
            }
            true
        }
    }

    // Metoda pro zobrazení reklamy při jakékoliv významné interakci
    fun showAdOnUserAction() {
        adManager.tryShowRewardedAd(this)
    }

    // Příklad metody, která by mohla být volána z různých míst v aplikaci
    fun onPouchAdded() {
        adManager.tryShowRewardedAd(this)
    }

    fun onAchievementUnlocked() {
        adManager.tryShowRewardedAd(this)
    }

    fun onStatisticsViewed() {
        adManager.tryShowRewardedAd(this)
    }
}
