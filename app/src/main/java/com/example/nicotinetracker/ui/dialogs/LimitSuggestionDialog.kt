package com.example.nicotinetracker.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.nicotinetracker.R
import com.example.nicotinetracker.managers.adaptive.DailyLimitSuggestion
import com.example.nicotinetracker.utils.PreferenceManager

/**
 * Dialog pro zobrazení návrhu nového denního limitu.
 */
class LimitSuggestionDialog(
    private val suggestion: DailyLimitSuggestion,
    private val onAcceptSuggestion: (Int) -> Unit
) : DialogFragment() {
    
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        preferenceManager = PreferenceManager(requireContext())
        
        // Vytvoření view pro dialog
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_limit_suggestion, null)
        
        // Nastavení textů
        val titleTextView = view.findViewById<TextView>(R.id.textViewSuggestionTitle)
        val reasonTextView = view.findViewById<TextView>(R.id.textViewSuggestionReason)
        val currentLimitTextView = view.findViewById<TextView>(R.id.textViewCurrentLimit)
        val suggestedLimitTextView = view.findViewById<TextView>(R.id.textViewSuggestedLimit)
        
        titleTextView.text = getString(R.string.limit_suggestion_title)
        reasonTextView.text = suggestion.reason
        currentLimitTextView.text = getString(
            R.string.limit_suggestion_current,
            suggestion.currentLimit
        )
        suggestedLimitTextView.text = getString(
            R.string.limit_suggestion_new,
            suggestion.suggestedLimit
        )
        
        // Nastavení tlačítek
        val acceptButton = view.findViewById<Button>(R.id.buttonAcceptSuggestion)
        val rejectButton = view.findViewById<Button>(R.id.buttonRejectSuggestion)
        
        acceptButton.setOnClickListener {
            // Nastavení nového limitu
            preferenceManager.setDailyLimit(suggestion.suggestedLimit)
            onAcceptSuggestion(suggestion.suggestedLimit)
            dismiss()
        }
        
        rejectButton.setOnClickListener {
            dismiss()
        }
        
        // Vytvoření dialogu
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}