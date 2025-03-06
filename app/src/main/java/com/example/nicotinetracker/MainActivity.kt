package com.example.nicotinetracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottomNavigationView)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_spotreba -> {
                    replaceFragment(SpotrebaFragment())
                    true
                }
                R.id.nav_statistiky -> {
                    replaceFragment(StatistikyFragment())
                    true
                }
                R.id.nav_nastaveni -> {
                    replaceFragment(NastaveniFragment())
                    true
                }
                else -> false
            }
        }

        // Výchozí fragment
        bottomNavigation.selectedItemId = R.id.nav_spotreba
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragment)
            .commit()
    }
}