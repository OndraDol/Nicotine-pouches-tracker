package com.example.nicotinetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.nicotinetracker.utils.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial

class NastaveniFragment : Fragment() {

    private lateinit var sliderDailyLimit: Slider
    private lateinit var tvLimitValue: TextView
    private lateinit var switchNotification: SwitchMaterial
    private lateinit var rgTheme: RadioGroup
    private lateinit var rbLight: RadioButton
    private lateinit var rbDark: RadioButton
    private lateinit var rbSystem: RadioButton
    private lateinit var btnSaveSettings: MaterialButton

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_nastaveni, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())

        // Inicializace komponentů
        sliderDailyLimit = view.findViewById(R.id.sliderDailyLimit)
        tvLimitValue = view.findViewById(R.id.tvLimitValue)
        switchNotification = view.findViewById(R.id.switchNotification)
        rgTheme = view.findViewById(R.id.rgTheme)
        rbLight = view.findViewById(R.id.rbLight)
        rbDark = view.findViewById(R.id.rbDark)
        rbSystem = view.findViewById(R.id.rbSystem)
        btnSaveSettings = view.findViewById(R.id.btnSaveSettings)

        // Nastavení aktuálních hodnot
        loadCurrentSettings()

        // Nastavení posluchačů událostí
        sliderDailyLimit.addOnChangeListener { _, value, _ ->
            tvLimitValue.text = "${value.toInt()} sáčků"
        }

        btnSaveSettings.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadCurrentSettings() {
        // Načtení aktuálních hodnot z preferencí
        val currentLimit = preferenceManager.dailyLimit
        val notificationsEnabled = preferenceManager.notificationsEnabled
        val themeMode = preferenceManager.themeMode

        // Nastavení hodnot do UI
        sliderDailyLimit.value = currentLimit.toFloat()
        tvLimitValue.text = "$currentLimit sáčků"
        switchNotification.isChecked = notificationsEnabled

        // Nastavení vybraného tématu
        when (themeMode) {
            PreferenceManager.THEME_LIGHT -> rbLight.isChecked = true
            PreferenceManager.THEME_DARK -> rbDark.isChecked = true
            else -> rbSystem.isChecked = true
        }
    }

    private fun saveSettings() {
        // Uložení hodnot z UI do preferencí
        preferenceManager.dailyLimit = sliderDailyLimit.value.toInt()
        preferenceManager.notificationsEnabled = switchNotification.isChecked

        // Zjištění vybraného tématu
        val selectedTheme = when {
            rbLight.isChecked -> PreferenceManager.THEME_LIGHT
            rbDark.isChecked -> PreferenceManager.THEME_DARK
            else -> PreferenceManager.THEME_SYSTEM
        }
        preferenceManager.themeMode = selectedTheme

        Toast.makeText(context, "Nastavení uloženo", Toast.LENGTH_SHORT).show()
    }
}