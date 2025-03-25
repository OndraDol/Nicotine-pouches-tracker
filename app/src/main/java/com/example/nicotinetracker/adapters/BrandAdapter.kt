package com.example.nicotinetracker.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.Brand

/**
 * Adaptér pro zobrazení seznamu značek nikotinových sáčků
 */
class BrandAdapter(
    private val onItemClick: (Brand) -> Unit
) : ListAdapter<Brand, BrandAdapter.BrandViewHolder>(BrandDiffCallback()) {

    // Sledování aktuálně vybrané pozice
    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_brand, parent, false)
        return BrandViewHolder(view)
    }

    override fun onBindViewHolder(holder: BrandViewHolder, position: Int) {
        val brand = getItem(position)
        holder.bind(brand, position == selectedPosition)
        
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
            onItemClick(brand)
        }
    }

    /**
     * Nastaví vybranou značku podle ID
     */
    fun setSelectedBrand(brandId: Int) {
        val position = currentList.indexOfFirst { it.id == brandId }
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
     * Vrátí aktuálně vybranou značku nebo null, pokud nic není vybráno
     */
    fun getSelectedBrand(): Brand? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            getItem(selectedPosition)
        } else {
            null
        }
    }

    /**
     * ViewHolder pro položku značky
     */
    inner class BrandViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivBrandLogo: ImageView = itemView.findViewById(R.id.ivBrandLogo)
        private val tvBrandName: TextView = itemView.findViewById(R.id.tvBrandName)
        private val tvBrandDescription: TextView = itemView.findViewById(R.id.tvBrandDescription)
        private val cbBrandSelected: RadioButton = itemView.findViewById(R.id.cbBrandSelected)

        fun bind(brand: Brand, isSelected: Boolean) {
            tvBrandName.text = brand.name
            tvBrandDescription.text = brand.description ?: ""
            
            // Nastavení obrázku značky (pokud je k dispozici)
            // Zde by bylo možné přidat logiku pro načítání obrázků z URL nebo zdrojů
            ivBrandLogo.setImageResource(R.drawable.ic_brand_placeholder)
            
            // Nastavení stavu výběru
            cbBrandSelected.isChecked = isSelected
            (itemView as? com.google.android.material.card.MaterialCardView)?.isChecked = isSelected
        }
    }

    /**
     * Callback pro porovnání položek při aktualizaci seznamu
     */
    private class BrandDiffCallback : DiffUtil.ItemCallback<Brand>() {
        override fun areItemsTheSame(oldItem: Brand, newItem: Brand): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Brand, newItem: Brand): Boolean {
            return oldItem == newItem
        }
    }
}
