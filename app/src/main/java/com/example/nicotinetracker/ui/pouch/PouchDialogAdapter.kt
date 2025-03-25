package com.example.nicotinetracker.ui.pouch

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * Adapter pro výběr sáčku v dialogu - obsahuje dva fragmenty: výběr existujícího
 * sáčku a zadání vlastního.
 */
class PouchDialogAdapter(
    activity: FragmentActivity,
    private val callback: PouchSelectionCallback
) : FragmentStateAdapter(activity) {
    
    override fun getItemCount(): Int = 2
    
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SelectExistingPouchFragment(callback)
            1 -> CustomPouchFragment(callback)
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
    
    interface PouchSelectionCallback {
        fun onPouchTypeSelected(pouchTypeId: Int)
        fun onCustomPouchEntered(
            brand: String?, 
            type: String?, 
            nicotineContent: Float?, 
            weight: Float?
        )
    }
}
