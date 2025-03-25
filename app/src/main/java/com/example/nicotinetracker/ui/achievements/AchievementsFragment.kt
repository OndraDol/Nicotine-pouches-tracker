package com.example.nicotinetracker.ui.achievements

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.nicotinetracker.MainActivity
import com.example.nicotinetracker.R
import com.example.nicotinetracker.data.achievement.Achievement
import com.example.nicotinetracker.viewmodels.AchievementViewModel

class AchievementsFragment : Fragment() {
    private lateinit var achievementViewModel: AchievementViewModel
    private lateinit var achievementsRecyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_achievements, container, false)
        
        achievementViewModel = ViewModelProvider(this).get(AchievementViewModel::class.java)
        achievementsRecyclerView = view.findViewById(R.id.achievements_recycler_view)
        
        setupRecyclerView()
        observeAchievements()
        setupAdInteractions(view)

        return view
    }

    private fun setupRecyclerView() {
        achievementAdapter = AchievementAdapter { achievement ->
            // Při kliknutí na úspěch zobrazit reklamu
            (activity as? MainActivity)?.showAdOnUserAction()
        }
        achievementsRecyclerView.adapter = achievementAdapter
    }

    private fun observeAchievements() {
        achievementViewModel.achievements.observe(viewLifecycleOwner) { achievements ->
            achievementAdapter.submitList(achievements)
            
            // Příležitost pro reklamu při načtení úspěchů
            (activity as? MainActivity)?.showAdOnUserAction()
        }
    }

    private fun setupAdInteractions(view: View) {
        // Náhodné spouštění reklam při interakci
        view.findViewById<View>(R.id.achievements_root)?.setOnClickListener {
            (activity as? MainActivity)?.showAdOnUserAction()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Při zobrazení fragmentu
        (activity as? MainActivity)?.showAdOnUserAction()
    }
}

class AchievementAdapter(
    private val onAchievementClick: (Achievement) -> Unit = {}
) : RecyclerView.Adapter<AchievementViewHolder>() {
    private var achievements: List<Achievement> = emptyList()

    fun submitList(newAchievements: List<Achievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view, onAchievementClick)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount(): Int = achievements.size
}

class AchievementViewHolder(
    itemView: View, 
    private val onAchievementClick: (Achievement) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    private val titleTextView: TextView = itemView.findViewById(R.id.achievement_title)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.achievement_description)
    private val progressBar: ProgressBar = itemView.findViewById(R.id.achievement_progress)
    
    private var currentAchievement: Achievement? = null

    init {
        itemView.setOnClickListener {
            currentAchievement?.let { achievement ->
                onAchievementClick(achievement)
            }
        }
    }

    fun bind(achievement: Achievement) {
        currentAchievement = achievement
        titleTextView.text = achievement.name
        descriptionTextView.text = achievement.description
        
        progressBar.max = achievement.maxProgress
        progressBar.progress = achievement.progress
    }
}
