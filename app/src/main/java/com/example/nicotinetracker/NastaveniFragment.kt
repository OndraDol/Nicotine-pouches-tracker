package com.example.nicotinetracker

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nicotinetracker.databinding.FragmentNastaveniBinding
import com.example.nicotinetracker.managers.adaptive.AdaptiveLimitManager
import com.example.nicotinetracker.services.LimitNotificationService
import com.example.nicotinetracker.ui.dialogs.LimitSuggestionDialog
import com.example.nicotinetracker.ui.dialogs.ReductionPlanDialog
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.launch

class NastaveniFragment : Fragment() {
    private var _binding: FragmentNastaveniBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var adaptiveLimitManager: AdaptiveLimitManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNastaveniBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        adaptiveLimitManager = AdaptiveLimitManager(requireContext())

        // Nastavení denního limitu
        binding.etDailyLimit.setText(preferenceManager.getDailyLimit().toString())

        // Tlačítko pro uložení nastavení
        binding.btnSaveSettings.setOnClickListener {
            val dailyLimitText = binding.etDailyLimit.text.toString()
            try {
                val dailyLimit = dailyLimitText.toInt()
                
                // Uložení denního limitu
                preferenceManager.setDailyLimit(dailyLimit)

                // Zapnutí/vypnutí notifikací
                val notificationsEnabled = binding.switchNotifications.isChecked
                if (notificationsEnabled) {
                    startNotificationService()
                } else {
                    stopNotificationService()
                }

                Toast.makeText(requireContext(), "Nastavení uloženo", Toast.LENGTH_SHORT).show()
            } catch (e: NumberFormatException) {
                Toast.makeText(requireContext(), "Neplatný denní limit", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Tlačítko pro doporučení nového limitu
        binding.btnSuggestLimit.setOnClickListener {
            // Získání doporučení nového limitu
            lifecycleScope.launch {
                val suggestion = adaptiveLimitManager.suggestDailyLimit()
                
                // Zobrazení dialogu s návrhem
                if (suggestion.suggestedLimit != suggestion.currentLimit) {
                    val dialog = LimitSuggestionDialog(suggestion) { newLimit ->
                        // Aktualizace UI po přijetí návrhu
                        binding.etDailyLimit.setText(newLimit.toString())
                        Toast.makeText(requireContext(), "Nový limit nastaven: $newLimit sáčků", Toast.LENGTH_SHORT).show()
                    }
                    dialog.show(parentFragmentManager, "LimitSuggestionDialog")
                } else {
                    // Zobrazení zprávy, pokud není nový návrh
                    Toast.makeText(requireContext(), suggestion.reason, Toast.LENGTH_LONG).show()
                }
            }
        }
        
        // Tlačítko pro plán snižování limitu
        binding.btnReductionPlan.setOnClickListener {
            val dialog = ReductionPlanDialog()
            dialog.show(parentFragmentManager, "ReductionPlanDialog")
        }
    }

    private fun startNotificationService() {
        val serviceIntent = Intent(requireContext(), LimitNotificationService::class.java)
        requireContext().startService(serviceIntent)
    }

    private fun stopNotificationService() {
        val serviceIntent = Intent(requireContext(), LimitNotificationService::class.java)
        requireContext().stopService(serviceIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
