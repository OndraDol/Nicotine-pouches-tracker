package com.example.nicotinetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.nicotinetracker.data.Pouch
import com.example.nicotinetracker.data.PouchDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SpotrebaFragment : Fragment() {

    private lateinit var btnAddPouch: MaterialButton
    private lateinit var noteLayout: TextInputLayout
    private lateinit var noteEditText: TextInputEditText
    private lateinit var tvTodayCount: TextView

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_spotreba, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        btnAddPouch = view.findViewById(R.id.btnAddPouch)
        noteLayout = view.findViewById(R.id.etNote)
        noteEditText = noteLayout.editText as TextInputEditText
        tvTodayCount = view.findViewById(R.id.tvTodayCount)
        
        updateTodayCount()
        
        btnAddPouch.setOnClickListener {
            addNewPouch()
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateTodayCount()
    }
    
    private fun addNewPouch() {
        val note = noteEditText.text.toString().trim()
        val pouch = Pouch(
            timestamp = System.currentTimeMillis(),
            note = if (note.isEmpty()) null else note
        )
        
        lifecycleScope.launch(Dispatchers.IO) {
            val database = PouchDatabase.getDatabase(requireContext())
            database.pouchDao().insert(pouch)
            
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Sáček zaznamenán", Toast.LENGTH_SHORT).show()
                noteEditText.text?.clear()
                updateTodayCount()
            }
        }
    }
    
    private fun updateTodayCount() {
        lifecycleScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endOfDay = calendar.timeInMillis
            
            val database = PouchDatabase.getDatabase(requireContext())
            val count = database.pouchDao().countPouchesInTimeRange(startOfDay, endOfDay)
            
            withContext(Dispatchers.Main) {
                tvTodayCount.text = "Dnes: $count sáčků"
            }
        }
    }
}