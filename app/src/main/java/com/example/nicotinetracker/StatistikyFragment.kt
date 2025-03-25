package com.example.nicotinetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nicotinetracker.viewmodels.StatisticsViewModel

class StatistikyFragment : Fragment() {
    private lateinit var statisticsViewModel: StatisticsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_statistiky, container, false)
        
        statisticsViewModel = ViewModelProvider(this).get(StatisticsViewModel::class.java)

        // Různé interaktivní prvky pro zobrazení reklam
        setupStatisticsInteractions(view)

        return view
    }

    private fun setupStatisticsInteractions(view: View) {
        // Příklady míst pro zobrazení reklam
        view.findViewById<View>(R.id.monthly_stats_card)?.setOnClickListener {
            (activity as? MainActivity)?.showAdOnUserAction()
        }

        view.findViewById<View>(R.id.nicotine_intake_chart)?.setOnClickListener {
            (activity as? MainActivity)?.showAdOnUserAction()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Při zobrazení fragmentu
        (activity as? MainActivity)?.showAdOnUserAction()
    }
}
