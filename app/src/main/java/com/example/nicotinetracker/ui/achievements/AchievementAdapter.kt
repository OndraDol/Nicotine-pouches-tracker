package com.example.nicotinetracker.ui.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.achievement.Achievement
import com.example.nicotinetracker.data.achievement.AchievementCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter pro zobrazení úspěchů v RecyclerView.
 */
class AchievementAdapter : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {
    
    private var achievements: List<Achievement> = emptyList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        holder.bind(achievement)
    }
    
    override fun getItemCount(): Int = achievements.size
    
    /**
     * Aktualizace seznamu úspěchů.
     */
    fun updateAchievements(newAchievements: List<Achievement>) {
        this.achievements = newAchievements
        notifyDataSetChanged()
    }
    
    /**
     * ViewHolder pro položku úspěchu.
     */
    inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.achievementIcon)
        private val titleTextView: TextView = itemView.findViewById(R.id.achievementTitle)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.achievementDescription)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.achievementProgressBar)
        private val progressTextView: TextView = itemView.findViewById(R.id.achievementProgressText)
        private val unlockedTextView: TextView = itemView.findViewById(R.id.achievementUnlockedDate)
        
        /**
         * Nastavení dat pro položku úspěchu.
         */
        fun bind(achievement: Achievement) {
            titleTextView.text = achievement.name
            descriptionTextView.text = achievement.description
            
            // Nastavení ikony podle kategorie
            val iconResId = when {
                achievement.unlockedAt == null -> R.drawable.ic_achievement_locked
                else -> getCategoryIconResId(achievement.category)
            }
            iconImageView.setImageResource(iconResId)
            
            // Nastavení progress baru
            progressBar.max = achievement.maxProgress
            progressBar.progress = achievement.progress
            
            // Nastavení textu progress
            val progressText = itemView.context.getString(
                R.string.achievement_progress,
                achievement.progress,
                achievement.maxProgress
            )
            progressTextView.text = progressText
            
            // Nastavení data odemčení
            if (achievement.unlockedAt != null) {
                val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                val dateText = dateFormat.format(Date(achievement.unlockedAt))
                unlockedTextView.text = dateText
                unlockedTextView.visibility = View.VISIBLE
                
                // Skrýt progress bar pro odemčené úspěchy
                progressBar.visibility = View.GONE
                progressTextView.visibility = View.GONE
            } else {
                unlockedTextView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE
                progressTextView.visibility = View.VISIBLE
            }
        }
        
        /**
         * Získání ID ikony podle kategorie úspěchu.
         */
        private fun getCategoryIconResId(category: AchievementCategory): Int {
            return when (category) {
                AchievementCategory.CONSISTENCY -> R.drawable.ic_achievement_consistency
                AchievementCategory.REDUCTION -> R.drawable.ic_achievement_reduction
                AchievementCategory.MILESTONE -> R.drawable.ic_achievement_milestone
                AchievementCategory.CHALLENGE -> R.drawable.ic_achievement_challenge
            }
        }
    }
}