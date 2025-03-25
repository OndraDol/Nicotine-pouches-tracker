package com.example.nicotinetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.nicotinetracker.viewmodels.PouchViewModel

class SpotrebaFragment : Fragment() {
    private lateinit var pouchViewModel: PouchViewModel

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_spotreba, container, false)
        
        pouchViewModel = ViewModelProvider(this).get(PouchViewModel::class.java)

        // Tlačítko pro přidání sáčku
        view.findViewById<View>(R.id.add_pouch_button).setOnClickListener {
            // Přidání sáčku
            pouchViewModel.addPouch()
            
            // Zobrazení reklamy
            (activity as? MainActivity)?.showAdOnUserAction()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Při zobrazení fragmentu 
        (activity as? MainActivity)?.showAdOnUserAction()
    }
}
