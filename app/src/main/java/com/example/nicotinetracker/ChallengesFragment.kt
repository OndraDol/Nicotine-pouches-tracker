package com.example.nicotinetracker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nicotinetracker.data.Challenge
import com.example.nicotinetracker.data.ChallengeDifficulty
import com.example.nicotinetracker.managers.ChallengeManager
import com.example.nicotinetracker.utils.AdManager
import kotlinx.coroutines.launch

class ChallengesFragment : Fragment() {
    private lateinit var challengesRecyclerView: RecyclerView
    private lateinit var challengeAdapter: ChallengeAdapter
    private lateinit var challengeManager: ChallengeManager
    private lateinit var adManager: AdManager

    override fun onCreateView(
        inflater: LayoutInflater, 
        container: ViewGroup?, 
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_challenges, container, false)
        
        // Inicializace AdMob v tomto fragmentu
        adManager = AdManager(requireContext())
        challengeManager = ChallengeManager(requireContext())
        
        challengesRecyclerView = view.findViewById(R.id.challenges_recycler_view)
        
        setupRecyclerView()
        observeChallenges()
        setupAdInteractions()
        
        return view
    }

    private fun setupRecyclerView() {
        challengeAdapter = ChallengeAdapter { challenge ->
            // Při kliknutí na výzvu zobrazit reklamu
            (activity as? MainActivity)?.showAdOnUserAction()
        }
        challengesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = challengeAdapter
        }
    }

    private fun setupAdInteractions() {
        // Náhodné spouštění reklam při interakci
        view?.findViewById<View>(R.id.challenges_root)?.setOnClickListener {
            (activity as? MainActivity)?.showAdOnUserAction()
        }
    }

    private fun observeChallenges() {
        viewLifecycleOwner.lifecycleScope.launch {
            challengeManager.getActiveChallenges().collect { challenges ->
                challengeAdapter.submitList(challenges)
                
                // Příležitost pro reklamu při načtení výzev
                (activity as? MainActivity)?.showAdOnUserAction()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            challengeManager.initializeChallenges()
        }
    }
}

class ChallengeAdapter(
    private val onChallengeClick: (Challenge) -> Unit = {}
) : RecyclerView.Adapter<ChallengeViewHolder>() {
    private var challenges: List<Challenge> = emptyList()

    fun submitList(newChallenges: List<Challenge>) {
        challenges = newChallenges
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_challenge, parent, false)
        return ChallengeViewHolder(view, onChallengeClick)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        holder.bind(challenges[position])
    }

    override fun getItemCount(): Int = challenges.size
}

class ChallengeViewHolder(
    itemView: View, 
    private val onChallengeClick: (Challenge) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    private val titleTextView: TextView = itemView.findViewById(R.id.challenge_title)
    private val descriptionTextView: TextView = itemView.findViewById(R.id.challenge_description)
    private val progressBar: ProgressBar = itemView.findViewById(R.id.challenge_progress)
    private val progressTextView: TextView = itemView.findViewById(R.id.challenge_progress_text)
    private val difficultyTextView: TextView = itemView.findViewById(R.id.challenge_difficulty)
    private val rewardTextView: TextView = itemView.findViewById(R.id.challenge_reward)

    private var currentChallenge: Challenge? = null

    init {
        itemView.setOnClickListener {
            currentChallenge?.let { challenge ->
                onChallengeClick(challenge)
            }
        }
    }

    fun bind(challenge: Challenge) {
        currentChallenge = challenge
        titleTextView.text = challenge.title
        descriptionTextView.text = challenge.description
        
        progressBar.max = challenge.targetValue
        progressBar.progress = challenge.currentProgress
        
        progressTextView.text = "${challenge.currentProgress}/${challenge.targetValue}"
        
        difficultyTextView.text = when (challenge.difficulty) {
            ChallengeDifficulty.EASY -> "Easy"
            ChallengeDifficulty.MEDIUM -> "Medium"
            ChallengeDifficulty.HARD -> "Hard"
            ChallengeDifficulty.EXTREME -> "Extreme"
        }
        
        rewardTextView.text = "${challenge.rewardPoints} Points"
    }
}
