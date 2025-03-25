package com.example.nicotinetracker.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.nicotinetracker.R
import com.example.nicotinetracker.managers.adaptive.AdaptiveLimitManager
import com.example.nicotinetracker.utils.PreferenceManager
import kotlinx.coroutines.launch

/**
 * Dialog pro vytvoření dlouhodobého plánu snižování spotřeby.
 */
class ReductionPlanDialog : DialogFragment() {
    
    private lateinit var adaptiveLimitManager: AdaptiveLimitManager
    private lateinit var preferenceManager: PreferenceManager
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        adaptiveLimitManager = AdaptiveLimitManager(requireContext())
        preferenceManager = PreferenceManager(requireContext())
        
        // Vytvoření view pro dialog
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.dialog_reduction_plan, null)
        
        // Nastavení aktuálního limitu
        val currentLimit = preferenceManager.getDailyLimit()
        val titleTextView = view.findViewById<TextView>(R.id.textViewPlanTitle)
        titleTextView.text = getString(R.string.limit_reduction_plan)
        
        // Nastavení polí
        val targetLimitEditText = view.findViewById<EditText>(R.id.editTextTargetLimit)
        val daysToTargetEditText = view.findViewById<EditText>(R.id.editTextDaysToTarget)
        val resultTextView = view.findViewById<TextView>(R.id.textViewPlanResult)
        
        // Výchozí hodnoty
        targetLimitEditText.setText((currentLimit - 2).coerceAtLeast(1).toString())
        daysToTargetEditText.setText("30")
        
        // Nastavení tlačítek
        val createPlanButton = view.findViewById<Button>(R.id.buttonCreatePlan)
        val closeButton = view.findViewById<Button>(R.id.buttonClosePlan)
        
        createPlanButton.setOnClickListener {
            val targetLimitText = targetLimitEditText.text.toString()
            val daysToTargetText = daysToTargetEditText.text.toString()
            
            if (targetLimitText.isNotEmpty() && daysToTargetText.isNotEmpty()) {
                val targetLimit = targetLimitText.toInt()
                val daysToTarget = daysToTargetText.toInt()
                
                if (targetLimit > 0 && daysToTarget > 0) {
                    // Vytvoření plánu
                    lifecycleScope.launch {
                        val plan = adaptiveLimitManager.createReductionPlan(targetLimit, daysToTarget)
                        resultTextView.text = plan.message
                    }
                }
            }
        }
        
        closeButton.setOnClickListener {
            dismiss()
        }
        
        // Vytvoření dialogu
        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }
}