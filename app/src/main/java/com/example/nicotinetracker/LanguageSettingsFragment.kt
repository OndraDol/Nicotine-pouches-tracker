package com.example.nicotinetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.example.nicotinetracker.utils.LocaleManager

class LanguageSettingsFragment : Fragment() {

    private lateinit var languageRadioGroup: RadioGroup
    private lateinit var localeManager: LocaleManager

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_language_settings, container, false)
        
        localeManager = LocaleManager(requireContext())
        languageRadioGroup = view.findViewById(R.id.language_radio_group)

        setupLanguageRadioGroup()

        return view
    }

    private fun setupLanguageRadioGroup() {
        val currentLanguage = localeManager.getLocale()

        // Nastavení aktuálního jazyka
        val selectedRadioButtonId = when (currentLanguage) {
            LocaleManager.LANGUAGE_ENGLISH -> R.id.radio_english
            LocaleManager.LANGUAGE_CZECH -> R.id.radio_czech
            else -> R.id.radio_english
        }
        languageRadioGroup.check(selectedRadioButtonId)

        // Listener pro změnu jazyka
        languageRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val languageCode = when (checkedId) {
                R.id.radio_english -> LocaleManager.LANGUAGE_ENGLISH
                R.id.radio_czech -> LocaleManager.LANGUAGE_CZECH
                else -> LocaleManager.LANGUAGE_ENGLISH
            }

            // Nastavení jazyka
            localeManager.setLocale(languageCode)

            // Restart aktivity pro aplikování změn
            activity?.recreate()
        }
    }
}
