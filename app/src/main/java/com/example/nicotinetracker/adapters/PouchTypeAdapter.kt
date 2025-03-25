package com.example.nicotinetracker.adapters

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.PouchType

/**
 * Adaptér pro zobrazení seznamu typů nikotinových sáčků
 */
class PouchTypeAdapter(
    private val onItemClick: (PouchType) -> Unit
) : ListAdapter<PouchType, PouchTypeAdapter.PouchTypeViewHolder>(PouchTypeDiffCallback()) {

    // Sledování aktuálně vybrané pozice
    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PouchTypeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pouch_type, parent, false)
        return PouchTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: PouchTypeViewHolder, position: Int) {
        val pouchType = getItem(position)
        holder.bind(pouchType, position == selectedPosition)
        
        holder.itemView.setOnClickListener {
            // Aktualizace vybrané pozice
            val previousSelected = selectedPosition
            selectedPosition = position
            
            // Překreslení předchozí a nové položky
            if (previousSelected != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelected)
            }
            notifyItemChanged(selectedPosition)
            
            // Oznámení o výběru položky
            onItemClick(pouchType)
        }
    }

    /**
     * Nastaví vybraný typ sáčku podle ID
     */
    fun setSelectedPouchType(pouchTypeId: Int) {
        val position = currentList.indexOfFirst { it.id == pouchTypeId }
        if (position != RecyclerView.NO_POSITION) {
            val previousSelected = selectedPosition
            selectedPosition = position
            
            if (previousSelected != RecyclerView.NO_POSITION) {
                notifyItemChanged(previousSelected)
            }
            notifyItemChanged(selectedPosition)
        }
    }

    /**
     * Vrátí aktuálně vybraný typ sáčku nebo null, pokud nic není vybráno
     */
    fun getSelectedPouchType(): PouchType? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            getItem(selectedPosition)
        } else {
            null
        }
    }

    /**
     * ViewHolder pro položku typu sáčku
     */
    inner class PouchTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewStrengthIndicator: View = itemView.findViewById(R.id.viewStrengthIndicator)
        private val tvPouchTypeName: TextView = itemView.findViewById(R.id.tvPouchTypeName)
        private val tvPouchTypeDescription: TextView = itemView.findViewById(R.id.tvPouchTypeDescription)
        private val tvWeight: TextView = itemView.findViewById(R.id.tvWeight)
        private val tvNicotineStrength: TextView = itemView.findViewById(R.id.tvNicotineStrength)
        private val cbPouchTypeSelected: RadioButton = itemView.findViewById(R.id.cbPouchTypeSelected)

        fun bind(pouchType: PouchType, isSelected: Boolean) {
            tvPouchTypeName.text = pouchType.name
            tvPouchTypeDescription.text = pouchType.description ?: ""
            tvWeight.text = "${pouchType.weight}g"
            tvNicotineStrength.text = "${pouchType.nicotineStrength}mg/g"
            
            // Nastavení barvy indikátoru síly podle obsahu nikotinu
            val strengthColor = getStrengthColor(pouchType.nicotineStrength)
            viewStrengthIndicator.backgroundTintList = ColorStateList.valueOf(strengthColor)
            
            // Nastavení stavu výběru
            cbPouchTypeSelected.isChecked = isSelected
            (itemView as? com.google.android.material.card.MaterialCardView)?.isChecked = isSelected
        }

        /**
         * Určí barvu indikátoru síly podle obsahu nikotinu
         */
        private fun getStrengthColor(nicotineStrength: Float): Int {
            val context = itemView.context
            return when {
                nicotineStrength <= 5.0f -> ContextCompat.getColor(context, R.color.strength_low)
                nicotineStrength <= 12.0f -> ContextCompat.getColor(context, R.color.strength_medium)
                nicotineStrength <= 20.0f -> ContextCompat.getColor(context, R.color.strength_high)
                else -> ContextCompat.getColor(context, R.color.strength_very_high)
            }
        }
    }

    /**
     * Callback pro porovnání položek při aktualizaci seznamu
     */
    private class PouchTypeDiffCallback : DiffUtil.ItemCallback<PouchType>() {
        override fun areItemsTheSame(oldItem: PouchType, newItem: PouchType): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PouchType, newItem: PouchType): Boolean {
            return oldItem == newItem
        }
    }
}
